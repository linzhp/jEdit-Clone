/*
 * jEdit.java - Main class of the jEdit editor
 * Copyright (C) 1998, 1999 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit;

import javax.swing.event.EventListenerList;
import javax.swing.text.Element;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
import org.gjt.sp.jedit.event.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.search.SearchAndReplace;
import org.gjt.sp.jedit.textarea.DefaultInputHandler;
import org.gjt.sp.jedit.textarea.InputHandler;

/**
 * The main class of the jEdit text editor.
 * @author Slava Pestov
 * @version $Id$
 */
public class jEdit
{
	/**
	 * Returns the jEdit version as a human-readable string.
	 */
	public static String getVersion()
	{
		return MiscUtilities.buildToVersion(getBuild());
	}

	/**
	 * Returns the internal version. String.compareTo() can be used
	 * to compare different internal versions.
	 */
	public static String getBuild()
	{
		// (major) (minor) (<99 = preX, 99 = final) (bug fix)
		return "02.02.02.00";
	}

	/**
	 * The main method of the jEdit application.
	 * <p>
	 * This should never be invoked directly.
	 * @param args The command line arguments
	 */
	public static void main(String[] args)
	{
		// Parse command line
		boolean endOpts = false;
		boolean readOnly = false;
		boolean reuseView = false;
		settingsDirectory = MiscUtilities.constructPath(
			System.getProperty("user.home"),".jedit");
		String portFile = "server";
		boolean defaultSession = true;
		session = "default";
		boolean showSplash = true;

		for(int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			if(arg.startsWith("-") && !endOpts)
			{
				if(arg.equals("--"))
					endOpts = true;
				else if(arg.equals("-usage"))
				{
					version();
					System.err.println();
					usage();
					System.exit(1);
				}
				else if(arg.equals("-version"))
				{
					version();
					System.exit(1);
				}
				else if(arg.equals("-nosettings"))
					settingsDirectory = null;
				else if(arg.startsWith("-settings="))
					settingsDirectory = arg.substring(10);
				else if(arg.startsWith("-noserver"))
					portFile = null;
				else if(arg.startsWith("-server="))
					portFile = arg.substring(8);
				else if(arg.equals("-nosession"))
					session = null;
				else if(arg.startsWith("-session="))
				{
					session = arg.substring(9);
					defaultSession = false;
				}
				else if(arg.equals("-nosplash"))
					showSplash = false;
				else if(arg.equals("-readonly"))
					readOnly = true;
				else if(arg.equals("-reuseview"))
					reuseView = true;
				else
				{
					System.err.println("Unknown option: "
						+ arg);
					usage();
					System.exit(1);
				}
				args[i] = null;
			}
		}

		if(settingsDirectory != null && portFile != null)
			portFile = MiscUtilities.constructPath(settingsDirectory,portFile);
		else
			portFile = null;

		if(session != null)
			session = Sessions.createSessionFileName(session);

		// Connect to server
		String userDir = System.getProperty("user.dir");

		if(portFile != null && new File(portFile).exists())
		{
			int port, key;
			try
			{
				BufferedReader in = new BufferedReader(new FileReader(portFile));
				port = Integer.parseInt(in.readLine());
				key = Integer.parseInt(in.readLine());
				in.close();

				Socket socket = new Socket(InetAddress.getLocalHost(),port);
				Writer out = new OutputStreamWriter(socket.getOutputStream());
				out.write(String.valueOf(key));
				out.write('\n');

				if(!defaultSession && session != null)
					out.write("session=" + session + "\n");
				if(readOnly)
					out.write("readonly\n");
				if(reuseView)
					out.write("reuseview\n");
				out.write("parent=" + userDir + "\n");
				out.write("--\n");

				for(int i = 0; i < args.length; i++)
				{
					if(args[i] != null)
					{
						out.write(args[i]);
						out.write('\n');
					}
				}

				out.close();

				System.exit(0);
			}
			catch(Exception e)
			{
				System.err.println("Error connecting to server:");
				e.printStackTrace();
			}
		}

		// GUIUtilities.static() adds a listener to our list,
		// so it must be available before GUIUtilities is first used
		listenerList = new EventListenerList();

		// Show the kool splash screen
		if(showSplash)
			GUIUtilities.showSplashScreen();

		// Get things rolling
		initMisc();
		initSystemProperties();
		initPlugins();
		initUserProperties();
		initActions();
		initModes();
		initPLAF();
		initKeyBindings();
		propertiesChanged();
		initRecent();
		if(settingsDirectory != null)
		{
			HistoryModel.loadHistory(MiscUtilities.constructPath(
				settingsDirectory,"history"));
		}
		SearchAndReplace.load();

		// Start plugins
		JARClassLoader.initPlugins();

		// Start server
		if(portFile != null)
			server = new EditServer(portFile);

		// Load files specified on the command line
		Buffer buffer = null;
		for(int i = 0; i < args.length; i++)
		{
			if(args[i] == null)
				continue;
			buffer = openFile(null,userDir,args[i],readOnly,false);
		}
		if(buffer == null)
		{
			if(defaultSession)
			{
				if("on".equals(getProperty("saveDesktop")))
					buffer = Sessions.loadSession(session,true);
			}
			else
				buffer = Sessions.loadSession(session,false);
		}
		if(buffer == null)
			buffer = newFile(null);

		// Create the view and hide the splash screen.
		// Paranoid thread safety courtesy of Sun.
		final Buffer _buffer = buffer;

		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				newView(null,_buffer);
				GUIUtilities.hideSplashScreen();
			}
		});
	}

	/**
	 * Loads the properties from the specified input stream. This
	 * calls the <code>load()</code> method of the properties object
	 * and closes the stream.
	 * @param in The input stream
	 * @exception IOException if an I/O error occured
	 */
	public static void loadProps(InputStream in)
		throws IOException
	{
		props.load(new BufferedInputStream(in));
		in.close();
	}

	/**
	 * Fetches a property, returning null if it's not defined.
	 * @param name The property
	 */
	public static final String getProperty(String name)
	{
		return props.getProperty(name);
	}

	/**
	 * Fetches a property, returning the default value if it's not
	 * defined.
	 * @param name The property
	 * @param def The default value
	 */
	public static final String getProperty(String name, String def)
	{
		return props.getProperty(name,def);
	}

	/**
	 * Returns the property with the specified name, formatting it with
	 * the <code>java.text.MessageFormat.format()</code> method.
	 * @param name The property
	 * @param args The positional parameters
	 */
	public static final String getProperty(String name, Object[] args)
	{
		if(name == null)
			return null;
		if(args == null)
			return props.getProperty(name,name);
		else
			return MessageFormat.format(props.getProperty(name,
				name),args);
	}

	/**
	 * Sets a property to a new value.
	 * @param name The property
	 * @param value The new value
	 */
	public static final void setProperty(String name, String value)
	{
		/* if value is null:
		 * - if default is null, unset user prop
		 * - else set user prop to ""
		 * else
		 * - if default equals value, ignore
		 * - if default doesn't equal value, set user
		 */
		if(value == null || value.length() == 0)
		{
			String prop = (String)defaultProps.get(name);
			if(prop == null || prop.length() == 0)
				props.remove(name);
			else
				props.put(name,"");
		}
		else
		{
			if(!value.equals(getProperty(name)))
				props.put(name,value);
		}
	}

	/**
	 * Sets a property to a new value. This method does not override
	 * properties set in the user property list, nor are the
	 * properties set with this method saved in the user properties.
	 * @param name The property
	 * @param value The new value
	 */
	public static final void setDefaultProperty(String name, String value)
	{
		defaultProps.put(name,value);
	}

	/**
	 * Unsets (clears) a property.
	 * @param name The property
	 */
	public static final void unsetProperty(String name)
	{
		if(defaultProps.get(name) != null)
			props.put(name,"");
		else
			props.remove(name);
	}

	/**
	 * Reloads various settings from the properties.
	 */
	public static void propertiesChanged()
	{
		// Auto save
		if(autosave != null)
			autosave.interrupt();

		autosave = new Autosave();

		// Recent files
		try
		{
			maxRecent = Integer.parseInt(getProperty("recent"));
		}
		catch(NumberFormatException nf)
		{
			maxRecent = 8;
		}

		fireEditorEvent(EditorEvent.PROPERTIES_CHANGED,null,null);
	}

	/**
	 * Loads all plugins in a directory.
	 * @param directory The directory
	 */
	public static void loadPlugins(String directory)
	{
		String[] args = { directory };
		System.out.println(jEdit.getProperty(
			"jar.scanningdir",args));

		File file = new File(directory);
		if(!(file.exists() || file.isDirectory()))
			return;
		String[] plugins = file.list();
		if(plugins == null)
			return;

		MiscUtilities.quicksort(plugins,new MiscUtilities.StringICaseCompare());
		for(int i = 0; i < plugins.length; i++)
		{
			String plugin = plugins[i];
			if(!plugin.toLowerCase().endsWith(".jar"))
				continue;
			try
			{
				new JARClassLoader(MiscUtilities.constructPath(
					directory,plugin));
			}
			catch(IOException io)
			{
				String[] args2 = { plugin };
				System.err.println(jEdit.getProperty(
					"jar.error.load",args2));
				io.printStackTrace();
			}
		}
	}

	/**
	 * Adds a plugin to the editor.
	 * @param plugin The plugin
	 */
	public static void addPlugin(EditPlugin plugin)
	{
		plugin.start();
		plugins.addElement(plugin);
	}

	/**
	 * Returns a plugin by it's class name.
	 * @param name The plugin to return
	 */
	public static EditPlugin getPlugin(String name)
	{
		for(int i = 0; i < plugins.size(); i++)
		{
			EditPlugin p = (EditPlugin)plugins.elementAt(i);
			if(p.getClass().getName().equals(name))
				return p;
		}

		return null;
	}

	/**
	 * Returns an array of installed plugins.
	 */
	public static EditPlugin[] getPlugins()
	{
		EditPlugin[] pluginArray = new EditPlugin[plugins.size()];
		plugins.copyInto(pluginArray);
		return pluginArray;
	}

	/**
	 * Registers an action with the editor.
	 * @param action The action
	 */
	public static void addAction(EditAction action)
	{
		String name = action.getName();
		actionHash.put(name,action);

		// Register key binding
		String binding = getProperty(name + ".shortcut");
		if(binding != null)
			inputHandler.addKeyBinding(binding,action);
	}

	/**
	 * Returns a named action.
	 * @param action The action
	 */
	public static EditAction getAction(String action)
	{
		return (EditAction)actionHash.get(action);
	}

	/**
	 * Returns the list of actions registered with the editor.
	 */
	public static EditAction[] getActions()
	{
		EditAction[] actions = new EditAction[actionHash.size()];
		Enumeration enum = actionHash.elements();
		int i = 0;
		while(enum.hasMoreElements())
		{
			actions[i++] = (EditAction)enum.nextElement();
		}
		return actions;
	}

	/**
	 * Registers an edit mode with the editor.
	 * @param mode The edit mode
	 */
	public static void addMode(Mode mode)
	{
		modes.addElement(mode);
	}

	/**
	 * Returns the edit mode with the specified name.
	 * @param name The edit mode
	 */
	public static Mode getMode(String name)
	{
		for(int i = 0; i < modes.size(); i++)
		{
			Mode mode = (Mode)modes.elementAt(i);
			if(mode.getName().equals(name))
				return mode;
		}
		return null;
	}

	/**
	 * Returns the localised name of an edit mode.
	 * @param mode The edit mode
	 */
	public static String getModeName(Mode mode)
	{
		return jEdit.props.getProperty("mode." +
			mode.getName() + ".name");
	}

	/**
	 * Returns an array of installed edit modes.
	 */
	public static Mode[] getModes()
	{
		Mode[] array = new Mode[modes.size()];
		modes.copyInto(array);
		return array;
	}

	/**
	 * Opens a file.
	 * @param view The view to open the file in
	 * @param parent The parent directory of the file
	 * @param path The path name of the file
	 * @param readOnly True if the file should be read only
	 * @param newFile True if the file should not be loaded from disk
	 */
	public static Buffer openFile(View view, String parent, String path,
		boolean readOnly, boolean newFile)
	{
		if(view != null && parent == null)
			parent = view.getBuffer().getFile().getParent();

		int index = path.indexOf('#');
		String marker = null;
		if(index != -1)
		{
			marker = path.substring(index + 1);
			path = path.substring(0,index);
		}

		// Java doesn't currently support saving to file:// URLs,
		// hence the crude hack here
		if(path.startsWith("file:"))
			path = path.substring(5);

		URL url = null;
		try
		{
			url = new URL(path);
		}
		catch(MalformedURLException mu)
		{
			path = MiscUtilities.constructPath(parent,path);

		}
		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			if(buffer.getPath().equals(path))
			{
				if(view != null)
				{
					if(marker != null)
						gotoMarker(buffer,view,marker);
					view.setBuffer(buffer);
				}
				return buffer;
			}
			buffer = buffer.next;
		}

		// Show the wait cursor because there certainly is going
		// to be a file load
		if(view != null)
			view.showWaitCursor();

		buffer = new Buffer(view,url,path,readOnly,newFile);
		addBufferToList(buffer);

		if(marker != null)
			gotoMarker(buffer,null,marker);

		if(view != null)
		{
			view.setBuffer(buffer);

			// Hide wait cursor
			view.hideWaitCursor();
		}

		fireEditorEvent(EditorEvent.BUFFER_CREATED,view,buffer);

		return buffer;
	}

	/**
	 * Creates a new `untitled' file.
	 * @param view The view to create the file in
	 */
	public static Buffer newFile(View view)
	{
		// Find the highest Untitled-n file
		int untitledCount = 0;
		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			if(buffer.getName().startsWith("Untitled-"))
			{
				try
				{
					untitledCount = Math.max(untitledCount,
						Integer.parseInt(buffer.getName()
						.substring(9)));
				}
				catch(NumberFormatException nf)
				{
				}
			}
			buffer = buffer.next;
		}

		return openFile(view,null,"Untitled-" + (untitledCount+1),false,true);
	}

	/**
	 * Closes a buffer. If there are unsaved changes, the user is
	 * prompted if they should be saved first.
	 * @param view The view
	 * @param buffer The buffer
	 * @return True if the buffer was really closed, false otherwise
	 */
	public static boolean closeBuffer(View view, Buffer buffer)
	{
		if(buffer.isDirty())
		{
			Object[] args = { buffer.getName() };
			int result = JOptionPane.showConfirmDialog(view,
				getProperty("notsaved.message",args),
				getProperty("notsaved.title"),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if(result == JOptionPane.YES_OPTION)
			{
				if(!buffer.save(view,null))
					return false;
			}
			else if(result != JOptionPane.NO_OPTION)
				return false;
		}

		_closeBuffer(view,buffer);
		return true;
	}

	/**
	 * Closes the buffer, even if it has unsaved changes.
	 * @param view The view
	 * @param buffer The buffer
	 *
	 * @since jEdit 2.2pre1
	 */
	public static void _closeBuffer(View view, Buffer buffer)
	{
		removeBufferFromList(buffer);
		buffer.close();
		fireEditorEvent(EditorEvent.BUFFER_CLOSED,view,buffer);

		if(!buffer.isNewFile())
		{
			String path = buffer.getPath();
			if(recent.contains(path))
				recent.removeElement(path);
			recent.insertElementAt(path,0);
			if(recent.size() > maxRecent)
				recent.removeElementAt(maxRecent);
		}

		// Create a new file when the last is closed
		if(buffersFirst == null && buffersLast == null)
			newFile(view);
	}

	/**
	 * Closes all open buffers.
	 */
	public static boolean closeAllBuffers(View view)
	{
		boolean dirty = false;

		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			if(buffer.isDirty())
			{
				dirty = true;
				break;
			}
			buffer = buffer.next;
		}

		if(dirty)
		{
			boolean ok = new CloseDialog(view).isOK();
			if(!ok)
				return false;
		}

		// close remaining buffers (CloseDialog only touches dirty
		// ones)
		buffer = buffersFirst;
		while(buffer != null)
		{
			_closeBuffer(view,buffer);
			buffer = buffer.next;
		}

		return true;
	}

	/**
	 * Returns the buffer with the specified path name.
	 * @param path The path name
	 */
	public static Buffer getBuffer(String path)
	{
		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			if(buffer.getPath().equals(path))
				return buffer;
			buffer = buffer.next;
		}
		return null;
	}

	/**
	 * Returns an array of open buffers.
	 */
	public static Buffer[] getBuffers()
	{
		Buffer[] buffers = new Buffer[bufferCount];
		Buffer buffer = buffersFirst;
		for(int i = 0; i < bufferCount; i++)
		{
			buffers[i] = buffer;
			buffer = buffer.next;
		}
		return buffers;
	}

	/**
	 * Returns the first buffer.
	 */
	public static Buffer getFirstBuffer()
	{
		return buffersFirst;
	}

	/**
	 * Returns the last buffer.
	 */
	public static Buffer getLastBuffer()
	{
		return buffersLast;
	}

	/**
	 * Returns the current input handler (key binding to action mapping)
	 * @see org.gjt.sp.jedit.textarea.InputHandler
	 */
	public static InputHandler getInputHandler()
	{
		return inputHandler;
	}

	/**
	 * Creates a new view of a buffer.
	 * @param view The view from which to take the geometry, buffer and
	 * caret position from
	 * @param buffer The buffer
	 */
	public static View newView(View view, Buffer buffer)
	{
		if(view != null)
		{
			view.showWaitCursor();
			view.saveCaretInfo();
		}

		View newView = new View(view,buffer);
		addViewToList(newView);

		fireEditorEvent(EditorEvent.VIEW_CREATED,newView,newView.getBuffer());

		// Do this crap here so that the view is created
		// and added to the list before it is shown
		// (for the sake of plugins that add stuff to views)
		newView.pack();

		if(view != null)
		{
			newView.setSize(view.getSize());
			Point location = view.getLocation();
			location.x += 20;
			location.y += 20;
			newView.setLocation(location);

			view.hideWaitCursor();
		}
		else
		{
			GUIUtilities.loadGeometry(newView,"view");
		}

		newView.show();
		newView.focusOnTextArea();
		newView.addWindowListener(windowHandler);

		return newView;
	}

	/**
	 * Closes a view. jEdit will exit if this was the last open view.
	 */
	public static void closeView(View view)
	{
		if(viewsFirst == viewsLast)
			exit(view); /* exit does editor event & save */
		else
		{
			fireEditorEvent(EditorEvent.VIEW_CLOSED,view,view.getBuffer());

			view.close();
			removeViewFromList(view);
		}
	}

	/**
	 * Returns an array of all open views.
	 */
	public static View[] getViews()
	{
		View[] views = new View[viewCount];
		View view = viewsFirst;
		for(int i = 0; i < viewCount; i++)
		{
			views[i] = view;
			view = view.next;
		}
		return views;
	}

	/**
	 * Returns the first view.
	 */
	public static View getFirstView()
	{
		return viewsFirst;
	}

	/**
	 * Returns the last view.
	 */
	public static View getLastView()
	{
		return viewsLast;
	}

	/**
	 * Returns an array of recently opened files.
	 */
	public static String[] getRecent()
	{
		String[] array = new String[recent.size()];
		recent.copyInto(array);
		return array;
	}

	/**
	 * Returns the jEdit install directory.
	 */
	public static String getJEditHome()
	{
		return jEditHome;
	}

	/**
	 * Returns the user settings directory.
	 */
	public static String getSettingsDirectory()
	{
		return settingsDirectory;
	}

	/**
	 * Adds an editor event listener to the global editor listener
	 * list.
	 * @param listener The editor event listener
	 */
	public static void addEditorListener(EditorListener listener)
	{
		listenerList.add(EditorListener.class,listener);
	}

	/**
	 * Removes an editor event listener from the global editor listener
	 * list.
	 * @param listener The editor event listener
	 */
	public static final void removeEditorListener(EditorListener listener)
	{
		listenerList.remove(EditorListener.class,listener);
	}

	/**
	 * Exits cleanly from jEdit, prompting the user if any unsaved files
	 * should be saved first.
	 * @param view The view from which this exit was called
	 */
	public static void exit(View view)
	{
		if(settingsDirectory != null && session != null)
			Sessions.saveSession(view,session);

		// Close all buffers
		if(!closeAllBuffers(view))
			return;

		// Stop autosave thread
		autosave.stop();

		// Stop server here
		if(server != null)
			server.stopServer();

		// Save view properties here - save unregisters
		// listeners, and we would have problems if the user
		// closed a view but cancelled an unsaved buffer close
		view.close();

		// Stop all plugins
		for(int i = 0; i < plugins.size(); i++)
		{
			((EditPlugin)plugins.elementAt(i)).stop();
		}

		// Save the recent file list
		for(int i = 0; i < recent.size(); i++)
		{
			String file = (String)recent.elementAt(i);
			setProperty("recent." + i,file);
		}
		unsetProperty("recent." + maxRecent);

		// Save the history lists
		if(settingsDirectory != null)
		{
			HistoryModel.saveHistory(MiscUtilities.constructPath(
				settingsDirectory, "history"));
		}

		// Save search and replace state
		SearchAndReplace.save();

		// Write the user properties file
		if(settingsDirectory != null)
		{
			try
			{
				OutputStream out = new FileOutputStream(
					MiscUtilities.constructPath(
						settingsDirectory,
						"properties"));
				props.save(out,"Use the -nosettings switch"
					+ " if you want to edit this file in jEdit");
				out.close();
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
		}

		// Byebye...
		System.out.println("Thank you for using jEdit. Send an e-mail"
			+ " to Slava Pestov <sp@gjt.org>.");
		System.exit(0);
	}

	// package-private members

	// View class needs to fire VIEW_CREATED editor events
	static void fireEditorEvent(int id, View view, Buffer buffer)
	{
		EditorEvent evt = null;
		Object[] listeners = listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i-= 2)
		{
			if(listeners[i] == EditorListener.class)
			{
				if(evt == null)
					evt = new EditorEvent(id,view,buffer);
				evt.fire((EditorListener)listeners[i+1]);
			}
		}
	}

	// private members
	private static String jEditHome;
	private static String settingsDirectory;
	private static String session;
	private static Properties defaultProps;
	private static Properties props;
	private static Autosave autosave;
	private static EditServer server;
	private static Hashtable actionHash;
	private static Vector plugins;
	private static Vector modes;
	private static Vector recent;
	private static int maxRecent;
	private static InputHandler inputHandler;
	private static EventListenerList listenerList;
	private static WindowHandler windowHandler;

	// buffer link list
	private static int bufferCount;
	private static Buffer buffersFirst;
	private static Buffer buffersLast;

	// view link list
	private static int viewCount;
	private static View viewsFirst;
	private static View viewsLast;

	private jEdit() {}

	private static void usage()
	{
		System.err.println("Usage: jedit [<options>] [<files>]");

		System.err.println("Common options:");
		System.err.println("	<filename>#<marker>: Positions caret"
			+ " at marker <marker>");
		System.err.println("	<filename>#+<line>: Positions caret"
			+ " at line number <line>");
		System.err.println("	--: End of options");
		System.err.println("	-version: Print jEdit version and"
			+ " exit");
		System.err.println("	-usage: Print this message and exit");
		System.err.println("	-readonly: Open files read-only");
		System.err.println("	-nosession: Don't load default session");
		System.err.println("	-session=<name>: Load session from"
			+ " $HOME/.jedit/sessions/<name>");
		System.err.println("	-noserver: Don't start editor server");
		System.err.println("	-server=<name>: Reads/writes server"
			+ " info to $HOME/.jedit/<name>");

		System.err.println();
		System.err.println("Server-only options:");
		System.err.println("	-nosettings: Don't load user-specific"
			+ " settings");
		System.err.println("	-settings=<path>: Load user-specific"
			+ " settings from <path>");
		System.err.println("	-nosplash: Don't show splash screen");
		System.err.println();
		System.err.println("Client-only options:");
		System.err.println("	-reuseview: Don't open new view in");

		System.err.println();
		System.err.println("Report bugs to Slava Pestov <sp@gjt.org>.");
	}

	private static void version()
	{
		System.err.println("jEdit " + getVersion());
	}

	/**
	 * Initialise various objects, register protocol handlers,
	 * register editor listener, and determine installation
	 * directory.
	 */
	private static void initMisc()
	{
		inputHandler = new DefaultInputHandler();
		windowHandler = new WindowHandler();

		// Add our protocols to java.net.URL's list
		System.getProperties().put("java.protocol.handler.pkgs",
			"org.gjt.sp.jedit.proto|" +
			System.getProperty("java.protocol.handler.pkgs",""));

		// Determine installation directory
		jEditHome = System.getProperty("jedit.home");
		if(jEditHome == null)
		{
			String classpath = System
				.getProperty("java.class.path");
			int index = classpath.toLowerCase()
				.indexOf("jedit.jar");
			int start = classpath.lastIndexOf(File
				.pathSeparator,index) + 1;
			if(index > start)
			{
				jEditHome = classpath.substring(start,
					index - 1);
			}
			else
				jEditHome = System.getProperty("user.dir");
		}

		if(settingsDirectory != null)
		{
			File _settingsDirectory = new File(settingsDirectory);
			if(!_settingsDirectory.exists())
				_settingsDirectory.mkdirs();
			File _macrosDirectory = new File(settingsDirectory,"macros");
			if(!_macrosDirectory.exists())
				_macrosDirectory.mkdir();
			File _sessionsDirectory = new File(settingsDirectory,"sessions");
			if(!_sessionsDirectory.exists())
				_sessionsDirectory.mkdir();
		}
	}

	/**
	 * Load system properties.
	 */
	private static void initSystemProperties()
	{
		defaultProps = props = new Properties();

		try
		{
			loadProps(jEdit.class.getResourceAsStream(
				"/org/gjt/sp/jedit/jedit.props"));
			loadProps(jEdit.class.getResourceAsStream(
				"/org/gjt/sp/jedit/jedit_gui.props"));
			loadProps(jEdit.class.getResourceAsStream(
				"/org/gjt/sp/jedit/jedit_keys.props"));
		}
		catch(Exception e)
		{
			System.err.println(">> ERROR LOADING SYSTEM PROPERTIES <<\n"
				+ "One of the following property files could not be loaded:\n"
				+ "- jedit.props\n"
				+ "- jedit_gui.props\n"
				+ "- jedit_keys.props\n"
				+ "Try reinstalling jEdit.");
			System.exit(1);
		}
	}

	/**
	 * Load edit modes.
	 */
	private static void initModes()
	{
		/* Try to guess the eventual size to avoid unnecessary
		 * copying */
		modes = new Vector(18 /* modes built into jEdit */
			+ 10 /* give plugins some space */);

		addMode(new Mode("bat"));
		addMode(new Mode("c"));
		addMode(new Mode("cc"));
		addMode(new Mode("eiffel"));
		addMode(new Mode("html"));
		addMode(new Mode("idl"));
		addMode(new Mode("java"));
		addMode(new Mode("javascript"));
		addMode(new Mode("makefile"));
		addMode(new Mode("patch"));
		addMode(new Mode("perl"));
		addMode(new Mode("props"));
		addMode(new Mode("python"));
		addMode(new Mode("shell"));
		addMode(new Mode("tex"));
		addMode(new Mode("text"));
		addMode(new Mode("tsql"));
		addMode(new Mode("xml"));
	}

	/**
	 * Load actions.
	 */
	private static void initActions()
	{
		actionHash = new Hashtable();

		addAction(new org.gjt.sp.jedit.actions.about());
		addAction(new org.gjt.sp.jedit.actions.append_string_register());
		addAction(new org.gjt.sp.jedit.actions.block_comment());
		addAction(new org.gjt.sp.jedit.actions.box_comment());
		addAction(new org.gjt.sp.jedit.actions.buffer_options());
		addAction(new org.gjt.sp.jedit.actions.clear_marker());
		addAction(new org.gjt.sp.jedit.actions.clear_register());
		addAction(new org.gjt.sp.jedit.actions.close_all());
		addAction(new org.gjt.sp.jedit.actions.close_file());
		addAction(new org.gjt.sp.jedit.actions.close_view());
		addAction(new org.gjt.sp.jedit.actions.copy());
		addAction(new org.gjt.sp.jedit.actions.copy_string_register());
		addAction(new org.gjt.sp.jedit.actions.cut());
		addAction(new org.gjt.sp.jedit.actions.cut_string_register());
		addAction(new org.gjt.sp.jedit.actions.delete_end_line());
		addAction(new org.gjt.sp.jedit.actions.delete_line());
		addAction(new org.gjt.sp.jedit.actions.delete_paragraph());
		addAction(new org.gjt.sp.jedit.actions.delete_start_line());
		addAction(new org.gjt.sp.jedit.actions.edit_macro());
		addAction(new org.gjt.sp.jedit.actions.exchange_caret_register());
		addAction(new org.gjt.sp.jedit.actions.exit());
		addAction(new org.gjt.sp.jedit.actions.expand_abbrev());
		addAction(new org.gjt.sp.jedit.actions.find());
		addAction(new org.gjt.sp.jedit.actions.find_next());
		addAction(new org.gjt.sp.jedit.actions.find_selection());
		addAction(new org.gjt.sp.jedit.actions.global_options());
		addAction(new org.gjt.sp.jedit.actions.goto_end_indent());
		addAction(new org.gjt.sp.jedit.actions.goto_line());
		addAction(new org.gjt.sp.jedit.actions.goto_marker());
		addAction(new org.gjt.sp.jedit.actions.goto_register());
		addAction(new org.gjt.sp.jedit.actions.help());
		addAction(new org.gjt.sp.jedit.actions.hypersearch());
		addAction(new org.gjt.sp.jedit.actions.hypersearch_selection());
		addAction(new org.gjt.sp.jedit.actions.indent_line());
		addAction(new org.gjt.sp.jedit.actions.indent_on_enter());
		addAction(new org.gjt.sp.jedit.actions.indent_on_tab());
		addAction(new org.gjt.sp.jedit.actions.join_lines());
		addAction(new org.gjt.sp.jedit.actions.load_session());
		addAction(new org.gjt.sp.jedit.actions.locate_bracket());
		addAction(new org.gjt.sp.jedit.actions.multifile_search());
		addAction(new org.gjt.sp.jedit.actions.new_file());
		addAction(new org.gjt.sp.jedit.actions.new_view());
		addAction(new org.gjt.sp.jedit.actions.next_bracket_exp());
		addAction(new org.gjt.sp.jedit.actions.next_buffer());
		addAction(new org.gjt.sp.jedit.actions.next_paragraph());
		addAction(new org.gjt.sp.jedit.actions.open_file());
		addAction(new org.gjt.sp.jedit.actions.open_path());
		addAction(new org.gjt.sp.jedit.actions.open_selection());
		addAction(new org.gjt.sp.jedit.actions.open_url());
		addAction(new org.gjt.sp.jedit.actions.paste());
		addAction(new org.gjt.sp.jedit.actions.paste_previous());
		addAction(new org.gjt.sp.jedit.actions.paste_string_register());
		addAction(new org.gjt.sp.jedit.actions.play_last_macro());
		addAction(new org.gjt.sp.jedit.actions.play_temp_macro());
		addAction(new org.gjt.sp.jedit.actions.play_macro());
		addAction(new org.gjt.sp.jedit.actions.plugin_options());
		addAction(new org.gjt.sp.jedit.actions.prev_bracket_exp());
		addAction(new org.gjt.sp.jedit.actions.prev_buffer());
		addAction(new org.gjt.sp.jedit.actions.prev_paragraph());
		addAction(new org.gjt.sp.jedit.actions.print());
		addAction(new org.gjt.sp.jedit.actions.record_macro());
		addAction(new org.gjt.sp.jedit.actions.record_temp_macro());
		addAction(new org.gjt.sp.jedit.actions.redo());
		addAction(new org.gjt.sp.jedit.actions.reload());
		addAction(new org.gjt.sp.jedit.actions.replace_all());
		addAction(new org.gjt.sp.jedit.actions.replace_in_selection());
		addAction(new org.gjt.sp.jedit.actions.rescan_macros());
		addAction(new org.gjt.sp.jedit.actions.save());
		addAction(new org.gjt.sp.jedit.actions.save_all());
		addAction(new org.gjt.sp.jedit.actions.save_as());
		addAction(new org.gjt.sp.jedit.actions.save_session());
		addAction(new org.gjt.sp.jedit.actions.save_url());
		addAction(new org.gjt.sp.jedit.actions.scroll_line());
		addAction(new org.gjt.sp.jedit.actions.search_and_replace());
		addAction(new org.gjt.sp.jedit.actions.select_all());
		addAction(new org.gjt.sp.jedit.actions.select_block());
		addAction(new org.gjt.sp.jedit.actions.select_buffer());
		addAction(new org.gjt.sp.jedit.actions.select_caret_register());
		addAction(new org.gjt.sp.jedit.actions.select_line_range());
		addAction(new org.gjt.sp.jedit.actions.select_next_paragraph());
		addAction(new org.gjt.sp.jedit.actions.select_none());
		addAction(new org.gjt.sp.jedit.actions.select_prev_paragraph());
		addAction(new org.gjt.sp.jedit.actions.set_caret_register());
		addAction(new org.gjt.sp.jedit.actions.set_filename_register());
		addAction(new org.gjt.sp.jedit.actions.set_replace_string());
		addAction(new org.gjt.sp.jedit.actions.set_search_parameters());
		addAction(new org.gjt.sp.jedit.actions.set_search_string());
		addAction(new org.gjt.sp.jedit.actions.send());
		addAction(new org.gjt.sp.jedit.actions.set_marker());
		addAction(new org.gjt.sp.jedit.actions.shift_left());
		addAction(new org.gjt.sp.jedit.actions.shift_right());
		addAction(new org.gjt.sp.jedit.actions.stop_recording());
		addAction(new org.gjt.sp.jedit.actions.tab());
		addAction(new org.gjt.sp.jedit.actions.undo());
		addAction(new org.gjt.sp.jedit.actions.untab());
		addAction(new org.gjt.sp.jedit.actions.view_registers());
		addAction(new org.gjt.sp.jedit.actions.wing_comment());
		addAction(new org.gjt.sp.jedit.actions.word_count());
	}

	/**
	 * Loads plugins.
	 */
	private static void initPlugins()
	{
		plugins = new Vector();
		loadPlugins(MiscUtilities.constructPath(jEditHome,"jars"));
		if(settingsDirectory != null)
		{
			File jarsDirectory = new File(settingsDirectory,"jars");
			if(!jarsDirectory.exists())
				jarsDirectory.mkdir();
			loadPlugins(jarsDirectory.getPath());
		}
	}

	/**
	 * Loads user properties.
	 */
	private static void initUserProperties()
	{
		props = new Properties(defaultProps);

		if(settingsDirectory != null)
		{
			try
			{
				loadProps(new FileInputStream(
					MiscUtilities.constructPath(
					settingsDirectory,"properties")));
			}
			catch(FileNotFoundException fnf)
			{
			}
			catch(IOException e)
			{
				System.err.println("Error while loading user"
					+ " properties:");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets the Swing look and feel.
	 */
	private static void initPLAF()
	{
		String lf = getProperty("lookAndFeel");
		try
		{
			if(lf != null && lf.length() != 0)
				UIManager.setLookAndFeel(lf);
		}
		catch(Exception e)
		{
			System.err.println("Error loading L&F!");
			e.printStackTrace();
		}
	}

	/**
	 * Loads the recent file list.
	 */
	private static void initRecent()
	{
		recent = new Vector(maxRecent);

		for(int i = 0; i < maxRecent; i++)
		{
			String recentFile = getProperty("recent." + i);
			if(recentFile != null)
				recent.addElement(recentFile);
		}
	}

	/**
	 * Loads the key bindings.
	 */
	private static void initKeyBindings()
	{
		// Register text area key bindings
		ActionListener[] textActions = DefaultInputHandler.ACTIONS;
		for(int i = 0; i < textActions.length; i++)
		{
			String binding = jEdit.getProperty(DefaultInputHandler
				.ACTION_NAMES[i] + ".shortcut");
			if(binding != null)
			{
				inputHandler.addKeyBinding(binding,textActions[i]);
			}
		}
	}

	private static void gotoMarker(Buffer buffer, View view, String marker)
	{
		if(marker.length() == 0)
			return;

		int start, end;

		// Handle line number
		if(marker.charAt(0) == '+')
		{
			try
			{
				int line = Integer.parseInt(marker.substring(1));
				Element lineElement = buffer.getDefaultRootElement()
					.getElement(line + 1);
				start = end = lineElement.getStartOffset();
			}
			catch(Exception e)
			{
				return;
			}
		}
		// Handle marker
		else
		{
			Marker m = buffer.getMarker(marker);
			if(m == null)
				return;
			start = m.getStart();
			end = m.getEnd();
		}

		if(view == null || view.getBuffer() != buffer)
			buffer.setCaretInfo(start,end,false);
		else if(view != null)
			view.getTextArea().select(start,end);
	}

	private static void addBufferToList(Buffer buffer)
	{
		bufferCount++;

		if(buffersFirst == null)
			buffersFirst = buffersLast = buffer;
		else
		{
			buffer.prev = buffersLast;
			buffersLast.next = buffer;
			buffersLast = buffer;
		}
	}

	private static void removeBufferFromList(Buffer buffer)
	{
		bufferCount--;

		if(buffer == buffersFirst && buffer == buffersLast)
		{
			buffersFirst = buffersLast = null;
			return;
		}

		if(buffer == buffersFirst)
		{
			buffersFirst = buffer.next;
			buffer.next.prev = null;
		}
		else
		{
			buffer.prev.next = buffer.next;
		}

		if(buffer == buffersLast)
		{
			buffersLast = buffersLast.prev;
			buffer.prev.next = null;
		}
		else
		{
			buffer.next.prev = buffer.prev;
		}
	}

	private static void addViewToList(View view)
	{
		viewCount++;

		if(viewsFirst == null)
			viewsFirst = viewsLast = view;
		else
		{
			view.prev = viewsLast;
			viewsLast.next = view;
			viewsLast = view;
		}
	}

	private static void removeViewFromList(View view)
	{
		viewCount--;

		if(view == viewsFirst)
		{
			viewsFirst = view.next;
			view.next.prev = null;
		}
		else
		{
			view.prev.next = view.next;
		}

		if(view == viewsLast)
		{
			viewsLast = viewsLast.prev;
			view.prev.next = null;
		}
		else
		{
			view.next.prev = view.prev;
		}
	}

	// Since window closing is handled by the editor itself,
	// and is the same for all views, it is ok to do it here
	static class WindowHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent evt)
		{
			closeView((View)evt.getSource());
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.147  1999/10/30 02:44:18  sp
 * Miscallaneous stuffs
 *
 * Revision 1.146  1999/10/28 09:07:21  sp
 * Directory list search
 *
 * Revision 1.145  1999/10/26 07:43:59  sp
 * Session loading and saving, directory list search started
 *
 * Revision 1.144  1999/10/24 06:04:00  sp
 * QuickSearch in tool bar, auto indent updates, macro recorder updates
 *
 * Revision 1.143  1999/10/24 02:06:41  sp
 * Miscallaneous pre1 stuff
 *
 * Revision 1.142  1999/10/23 03:48:22  sp
 * Mode system overhaul, close all dialog box, misc other stuff
 *
 * Revision 1.141  1999/10/22 07:05:37  sp
 * Version number changed to 2.1final
 *
 * Revision 1.140  1999/10/21 07:59:24  sp
 * Eiffel mode and better font list added
 *
 * Revision 1.139  1999/10/19 09:10:13  sp
 * pre5 bug fixing
 *
 * Revision 1.138  1999/10/17 04:16:28  sp
 * Bug fixing
 *
 * Revision 1.137  1999/10/16 09:43:00  sp
 * Final tweaking and polishing for jEdit 2.1final
 *
 * Revision 1.136  1999/10/10 06:38:45  sp
 * Bug fixes and quicksort routine
 */
