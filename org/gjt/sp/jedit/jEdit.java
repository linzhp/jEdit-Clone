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
import javax.swing.text.Segment;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.text.MessageFormat;
import java.util.zip.*;
import java.util.*;
import org.gjt.sp.jedit.event.*;
import org.gjt.sp.jedit.gui.HistoryModel;
import org.gjt.sp.jedit.remote.impl.RemoteEditorImpl;
import org.gjt.sp.jedit.remote.*;
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
		return "2.0pre8";
	}

	/**
	 * Returns the internal version. String.compareTo() can be used
	 * to compare different internal versions.
	 */
	public static String getBuild()
	{
		// (major) (minor) (<99 = preX, 99 = final) (bug fix)
		return "02.00.08.00";
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
		boolean wait = false;
		boolean quit = false;
		settingsDirectory = System.getProperty("user.home") +
			File.separator + ".jedit";
		serverPath = "org.gjt.sp.jedit/RemoteEditor/"
			+ System.getProperty("user.name");
		boolean desktop = true;
		boolean showSplash = true;
		int lineNo = -1;

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
				else if(arg.equals("-rmi"))
				{
					if(serverPath != null)
						serverPath = "org.gjt.sp.jedit/RemoteEditor/" +
						System.getProperty("user.name");
				}
				else if(arg.startsWith("-rmi="))
					serverPath = arg.substring(5);
				else if(arg.equals("-normi"))
					serverPath = null;
				else if(arg.equals("-wait"))
					wait = true;
				else if(arg.equals("-quit"))
					quit = true;
				else if(arg.equals("-nodesktop"))
					desktop = false;
				else if(arg.equals("-nosplash"))
					showSplash = false;
				else if(arg.equals("-readonly"))
					readOnly = true;
				else if(arg.startsWith("-+"))
				{
					try
					{
						lineNo = Integer.parseInt(arg
							.substring(2));
					}
					catch(NumberFormatException nf)
					{
					}
				}
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

		String userDir = System.getProperty("user.dir");

		// Try to connect to the RMI server
		if(serverPath != null)
		{
			try
			{
				RemoteEditor editor = (RemoteEditor)Naming
					.lookup(serverPath);

				if(quit)
				{
					editor.exit(null);
					System.exit(0);
				}

				RemoteBuffer buf = null;

				for(int i = 0; i < args.length; i++)
				{
					String file = args[i];
					if(file == null)
						continue;
					buf = editor.openFile(null,userDir,file,
						readOnly,false);
				}

				if(buf == null)
					buf = editor.newFile(null);

				RemoteView view = editor.newView(null,buf);
				if(lineNo != -1)
				{
					int pos = buf.getLineStartOffset(lineNo);
					if(pos != -1)
						view.setCaretPosition(pos);
				}
				if(wait)
					editor.waitForClose(view);

				// Clean up
				editor = null;
				buf = null;
				System.runFinalization();
				System.gc();
				return;
			}
			catch(java.rmi.ConnectException c)
			{
			}
			catch(NotBoundException nb)
			{
			}
			catch(Exception e)
			{
				System.err.println("Error while contacting jEdit RMI"
					+ " service:");
				e.printStackTrace();
			}
		}

		// If RMI is disabled and we were passed the -quit, flag,
		// exit now
		if(quit)
			System.exit(0);

		// Show the kool splash screen
		if(showSplash)
			GUIUtilities.showSplashScreen();

		// Get things rolling
		initMisc();
		initSystemProperties();
		initModes();
		initActions();
		initOptions();
		initPlugins();
		initUserProperties();
		initPLAF();
		propertiesChanged();
		initRecent();
		if(settingsDirectory != null)
		{
			HistoryModel.loadHistory(settingsDirectory + File.separator
				+ "history");
		}
		SearchAndReplace.load();

		// Start plugins
		JARClassLoader.initPlugins();

		// Only do this after plugin actions are available
		initKeyBindings();

		// Load files specified on the command line
		Buffer buffer = null;
		for(int i = 0; i < args.length; i++)
		{
			if(args[i] == null)
				continue;
			buffer = openFile(null,userDir,args[i],readOnly,false);
			if(lineNo != -1)
			{
				Element lineElement = buffer.getDefaultRootElement()
					.getElement(lineNo - 1);
				if(lineElement != null)
				{
					buffer.setCaretInfo(lineElement.getStartOffset(),
						lineElement.getStartOffset());
				}
			}
		}
		if(buffer == null)
		{
			if("on".equals(getProperty("saveDesktop")) && desktop)
				buffer = loadDesktop();
		}
		if(buffer == null)
			buffer = newFile(null);

		// Start the RMI service last, to prevent races
		// if clients connect before we're all done
		initRMI();

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
		props.put(name,value);

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
		props.remove(name);
	}

	/**
	 * Reloads various settings from the properties.
	 */
	public static void propertiesChanged()
	{
		if(autosave != null)
			autosave.interrupt();

		autosave = new Autosave();

		try
		{
			maxRecent = Integer.parseInt(getProperty(
				"recent"));
		}
		catch(NumberFormatException nf)
		{
			maxRecent = 8;
		}

		if(inputHandler != null)
			initKeyBindings();

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
		for(int i = 0; i < plugins.length; i++)
		{
			String plugin = plugins[i];
			if(!plugin.toLowerCase().endsWith(".jar"))
				continue;
			try
			{
				new JARClassLoader(directory + File.separator
					+ plugin);
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
	 * Registers a plugin with the editor. This will also call
	 * the <code>start()</code> method of the plugin.
	 * @see org.gjt.sp.jedit.Plugin#start()
	 */
	public static void addPlugin(Plugin plugin)
	{
		plugins.addElement(plugin);
		plugin.start();
	}

	/**
	 * Returns a plugin by it's class name.
	 * @param name The plugin to return
	 */
	public static Plugin getPlugin(String name)
	{
		for(int i = 0; i < plugins.size(); i++)
		{
			Plugin p = (Plugin)plugins.elementAt(i);
			if(p.getClass().getName().equals(name))
				return p;
		}

		return null;
	}

	/**
	 * Returns an array of installed plugins.
	 */
	public static Plugin[] getPlugins()
	{
		Plugin[] pluginArray = new Plugin[plugins.size()];
		plugins.copyInto(pluginArray);
		return pluginArray;
	}

	/**
	 * Registers a plugin menu with the editor. The menu
	 * resulting from the call to <code>GUIUtilities.loadMenu(menu)</code>
	 * will be added to the plugins menu.
	 * @param menu The menu's name
	 * @see org.gjt.sp.jedit.GUIUtilities#loadMenu(org.gjt.sp.jedit.View,java.lang.String)
	 */
	public static void addPluginMenu(String menu)
	{
		pluginMenus.addElement(menu);
	}

	/**
	 * Returns an array of registered plugin menus.
	 */
	public static String[] getPluginMenus()
	{
		String[] pluginMenuArray = new String[pluginMenus.size()];
		pluginMenus.copyInto(pluginMenuArray);
		return pluginMenuArray;
	}

	/**
	 * Registers an action with the editor.
	 * @param action The action
	 */
	public static void addAction(EditAction action)
	{
		actionHash.put(action.getName(),action);
	}

	/**
	 * Registers an action with the editor and adds it to the plugin
	 * action list (so it will appear in the plugins menu).
	 * @param action The action
	 */
	public static void addPluginAction(EditAction action)
	{
		actionHash.put(action.getName(),action);
		pluginActions.addElement(action);
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
	 * Returns an array of installed plugin actions.
	 */
	public static EditAction[] getPluginActions()
	{
		EditAction[] array = new EditAction[pluginActions.size()];
		pluginActions.copyInto(array);
		return array;
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
	 * Registers an option pane with the editor.
	 * @param clazz The option pane's class. This must be a
	 * subclass of <code>OptionPane</code>.
	 * @see org.gjt.sp.jedit.OptionPane
	 */
	public static void addOptionPane(Class clazz)
	{
		optionPanes.addElement(clazz);
	}

	/**
	 * Returns an array of registered option pane classes.
	 * These should be instantiated and cast to <code>OptionPane</code>
	 * objects.
	 */
	public static Class[] getOptionPanes()
	{
		Class[] optionPaneArray = new Class[optionPanes.size()];
		optionPanes.copyInto(optionPaneArray);
		return optionPaneArray;
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
			// Handle openFile(#marker)
			if(path.length() == 0)
			{
				if(view == null)
					return null;
				Buffer buffer = view.getBuffer();
				gotoMarker(buffer,view,marker);
				return buffer;
			}
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
						gotoMarker(buffer,view,
							marker);
					view.setBuffer(buffer);
				}
				return buffer;
			}
			buffer = buffer.next;
		}

		// Show the wait cursor because there certainly is going
		// to be a file load
		if(view != null)
			GUIUtilities.showWaitCursor(view);

		buffer = new Buffer(view,url,path,readOnly,newFile);
		addBufferToList(buffer);

		if(marker != null)
			gotoMarker(buffer,null,marker);

			if(view != null)
		{
			view.setBuffer(buffer);

			// Hide wait cursor
			GUIUtilities.hideWaitCursor(view);
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
		Object[] args = { new Integer(++untitledCount) };
		return openFile(view,null,getProperty("untitled",
			args),false,true);
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
		if(_closeBuffer(view,buffer))
		{
			if(buffersFirst == buffersLast)
				exit(view);
			else
				removeBufferFromList(buffer);
		}
		else
			return false;

		buffer.close();

		fireEditorEvent(EditorEvent.BUFFER_CLOSED,view,buffer);

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
	 * Returns the buffer with the specified unique identifier.
	 * @param uid The unique identifier
	 * @see org.gjt.sp.jedit.Buffer#getUID()
	 */
	public static Buffer getBuffer(int uid)
	{
		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			if(buffer.getUID() == uid)
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
		Vector buffers = new Vector();
		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			buffers.addElement(buffer);
			buffer = buffer.next;
		}
		Buffer[] array = new Buffer[buffers.size()];
		buffers.copyInto(array);
		return array;
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
			GUIUtilities.showWaitCursor(view);
			view.saveCaretInfo();
		}

		View newView = new View(view,buffer);
		addViewToList(newView);

		if(view != null)
			GUIUtilities.hideWaitCursor(view);

		fireEditorEvent(EditorEvent.VIEW_CREATED,newView,buffer);
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
	 * Returns the view with the specified unique identifier.
	 * @param uid The unique identifier
	 * @see org.gjt.sp.jedit.View#getUID()
	 */
	public static View getView(int uid)
	{
		View view = viewsFirst;
		while(view != null)
		{
			if(view.getUID() == uid)
				return view;
			view = view.next;
		}
		return null;
	}

	/**
	 * Returns an array of all open views.
	 */
	public static View[] getViews()
	{
		Vector views = new Vector();
		View view = viewsFirst;
		while(view != null)
		{
			views.addElement(view);
			view = view.next;
		}
		View[] array = new View[views.size()];
		views.copyInto(array);
		return array;
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
	public static final void addEditorListener(EditorListener listener)
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
		// For RMI's sake
		if(view == null)
			view = viewsFirst;

		// Save the `desktop'
		if("on".equals(getProperty("saveDesktop")))
		{
			int bufNum = 0;
			view.saveCaretInfo();
			Buffer buffer = buffersFirst;
			while(buffer != null)
			{
				if(buffer.isNewFile())
				{
					buffer = buffer.next;
					continue;
				}
				setProperty("desktop." + bufNum + ".path",
					buffer.getPath());
				setProperty("desktop." + bufNum + ".mode",
					buffer.getMode().getName());
				setProperty("desktop." + bufNum + ".readOnly",
					buffer.isReadOnly() ? "yes" : "no");
				setProperty("desktop." + bufNum + ".current",
					view.getBuffer() == buffer ? "yes" : "no");
				setProperty("desktop." + bufNum + ".selStart",
					String.valueOf(buffer.getSavedSelStart()));
				setProperty("desktop." + bufNum + ".selEnd",
					String.valueOf(buffer.getSavedSelEnd()));
				bufNum++;
				buffer = buffer.next;
			}
			unsetProperty("desktop." + bufNum + ".path");
		}

		// Close all buffers
		Buffer buffer = buffersFirst;
		while(buffer != null)
		{
			if(!_closeBuffer(view,buffer))
				return;
			buffer = buffer.next;
		}

		// Stop RMI service
		if(remoteEditor != null)
		{
			try
			{
				Naming.unbind(serverPath);
				remoteEditor.stop();
			}
			catch(AccessException a)
			{
			}
			catch(java.rmi.ConnectException c)
			{
			}
			catch(Exception e)
			{
				System.err.println("Error stopping RMI service:");
				e.printStackTrace();
			}
		}

		// Only save view properties here - save unregisters
		// listeners, and we would have problems if the user
		// closed a view but cancelled an unsaved buffer close
		view.close();

		// Stop all plugins
		for(int i = 0; i < plugins.size(); i++)
		{
			((Plugin)plugins.elementAt(i)).stop();
		}

		// Save the recent file list
		for(int i = 0; i < recent.size(); i++)
		{
			String file = (String)recent.elementAt(i);
			setProperty("recent." + i,file);
		}
		unsetProperty("recent." + maxRecent);

		// Save the history lists
		HistoryModel.saveHistory(settingsDirectory + File.separator
			+ "history");

		// Save search and replace state
		SearchAndReplace.save();

		// Write the user properties file
		if(settingsDirectory != null)
		{
			try
			{
				OutputStream out = new FileOutputStream(
					settingsDirectory + File.separator
					+ "properties");
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

	// private members
	private static String jEditHome;
	private static String settingsDirectory;
	private static String serverPath;
	private static RemoteEditorImpl remoteEditor;
	private static Properties defaultProps;
	private static Properties props;
	private static Autosave autosave;
	private static Hashtable actionHash;
	private static Vector plugins;
	private static Vector pluginMenus;
	private static Vector pluginActions;
	private static Vector modes;
	private static Vector optionPanes;
	private static int untitledCount;
	private static Vector recent;
	private static int maxRecent;
	private static InputHandler inputHandler;
	private static EventListenerList listenerList;

	// buffer link list
	private static Buffer buffersFirst;
	private static Buffer buffersLast;

	// view link list
	private static View viewsFirst;
	private static View viewsLast;

	private jEdit() {}

	private static void usage()
	{
		System.err.println("Usage: jedit [<options>] [<files>]");
		System.err.println("Valid options:");
		System.err.println("	--: End of options");
		System.err.println("	-version: Print jEdit version and"
			+ " exit");
		System.err.println("	-usage: Print this message and exit");
		System.err.println("	-nosettings: Don't load user-specific"
			+ " settings");
		System.err.println("	-settings=<path>: Load user-specific"
			+ " settings from <path>");
		System.err.println("	-normi: Don't start RMI service");
		System.err.println("	-rmi: Start RMI service");
		System.err.println("	-rmi=<path>: Start RMI service bound"
			+ " to <path>");
		System.err.println("	-wait: If connecting to another"
			+ " running instance, wait until");
		System.err.println("	the user closes the new view before"
			+ " exiting");
		System.err.println("	-quit: Quit the current instance using RMI");
		System.err.println("	-nodesktop: Ignore saved desktop");
		System.err.println("	-nosplash: Don't show splash screen");
		System.err.println("	-readonly: Open files read-only");
		System.err.println("	-+<line>: Go to line number <line> of"
			+ " opened file");
		System.err.println();
		System.err.println("Report bugs to Slava Pestov <sp@gjt.org>.");
	}

	private static void version()
	{
		System.err.println("jEdit " + getVersion() + " build " +
			getBuild());
	}

	private static void fireEditorEvent(int id, View view, Buffer buffer)
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

	/**
	 * Initialise various objects, register protocol handlers,
	 * register editor listener, and determine installation
	 * directory.
	 */
	private static void initMisc()
	{
		listenerList = new EventListenerList();

		// Add our protcols to java.net.URL's list
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
		jEditHome = jEditHome + File.separator;

		if(settingsDirectory != null)
		{
			File _settingsDirectory = new File(settingsDirectory);
			if(!_settingsDirectory.exists())
				_settingsDirectory.mkdir();

			File _jarsDirectory = new File(settingsDirectory,"jars");
			if(!_jarsDirectory.exists())
				_jarsDirectory.mkdir();
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
			loadProps(jEdit.class.getResourceAsStream(
				"/org/gjt/sp/jedit/jedit_tips.props"));
		}
		catch(Exception e)
		{
			System.err.println(">> ERROR LOADING SYSTEM PROPERTIES <<\n"
				+ "One of the following property files could not be loaded:\n"
				+ "- jedit.props\n"
				+ "- jedit_gui.props\n"
				+ "- jedit_keys.props\n"
				+ "- jedit_tips.props\n"
				+ "Try reinstalling jEdit.");
			System.exit(1);
		}
	}

	/**
	 * Load edit modes.
	 */
	private static void initModes()
	{
		modes = new Vector();

		addMode(new org.gjt.sp.jedit.mode.text());
		addMode(new org.gjt.sp.jedit.mode.amstex());
		addMode(new org.gjt.sp.jedit.mode.bat());
		addMode(new org.gjt.sp.jedit.mode.c());
		addMode(new org.gjt.sp.jedit.mode.cc());
		addMode(new org.gjt.sp.jedit.mode.html());
		addMode(new org.gjt.sp.jedit.mode.java_mode());
		addMode(new org.gjt.sp.jedit.mode.javascript());
		addMode(new org.gjt.sp.jedit.mode.latex());
		addMode(new org.gjt.sp.jedit.mode.makefile());
		addMode(new org.gjt.sp.jedit.mode.patch());
		addMode(new org.gjt.sp.jedit.mode.perl());
		addMode(new org.gjt.sp.jedit.mode.props());
		addMode(new org.gjt.sp.jedit.mode.sh());
		addMode(new org.gjt.sp.jedit.mode.tex());
		addMode(new org.gjt.sp.jedit.mode.tsql());
	}

	/**
	 * Load actions.
	 */
	private static void initActions()
	{
		actionHash = new Hashtable();
		pluginActions = new Vector();

		addAction(new org.gjt.sp.jedit.actions.about());
		addAction(new org.gjt.sp.jedit.actions.block_comment());
		addAction(new org.gjt.sp.jedit.actions.box_comment());
		addAction(new org.gjt.sp.jedit.actions.buffer_options());
		addAction(new org.gjt.sp.jedit.actions.clear());
		addAction(new org.gjt.sp.jedit.actions.clear_marker());
		addAction(new org.gjt.sp.jedit.actions.close_file());
		addAction(new org.gjt.sp.jedit.actions.close_view());
		addAction(new org.gjt.sp.jedit.actions.copy());
		addAction(new org.gjt.sp.jedit.actions.cut());
		addAction(new org.gjt.sp.jedit.actions.delete_end_line());
		addAction(new org.gjt.sp.jedit.actions.delete_line());
		addAction(new org.gjt.sp.jedit.actions.delete_paragraph());
		addAction(new org.gjt.sp.jedit.actions.delete_start_line());
		addAction(new org.gjt.sp.jedit.actions.exchange_anchor());
		addAction(new org.gjt.sp.jedit.actions.exit());
		addAction(new org.gjt.sp.jedit.actions.expand_abbrev());
		addAction(new org.gjt.sp.jedit.actions.find());
		addAction(new org.gjt.sp.jedit.actions.find_next());
		addAction(new org.gjt.sp.jedit.actions.find_selection());
		addAction(new org.gjt.sp.jedit.actions.format());
		addAction(new org.gjt.sp.jedit.actions.global_options());
		addAction(new org.gjt.sp.jedit.actions.goto_anchor());
		addAction(new org.gjt.sp.jedit.actions.goto_end_indent());
		addAction(new org.gjt.sp.jedit.actions.goto_line());
		addAction(new org.gjt.sp.jedit.actions.goto_marker());
		addAction(new org.gjt.sp.jedit.actions.help());
		addAction(new org.gjt.sp.jedit.actions.hypersearch());
		addAction(new org.gjt.sp.jedit.actions.hypersearch_selection());
		addAction(new org.gjt.sp.jedit.actions.indent_on_enter());
		addAction(new org.gjt.sp.jedit.actions.indent_on_tab());
		addAction(new org.gjt.sp.jedit.actions.insert_date());
		addAction(new org.gjt.sp.jedit.actions.join_lines());
		addAction(new org.gjt.sp.jedit.actions.locate_bracket());
		addAction(new org.gjt.sp.jedit.actions.multifile_search());
		addAction(new org.gjt.sp.jedit.actions.new_file());
		addAction(new org.gjt.sp.jedit.actions.new_view());
		addAction(new org.gjt.sp.jedit.actions.next_buffer());
		addAction(new org.gjt.sp.jedit.actions.next_paragraph());
		addAction(new org.gjt.sp.jedit.actions.open_file());
		addAction(new org.gjt.sp.jedit.actions.open_path());
		addAction(new org.gjt.sp.jedit.actions.open_selection());
		addAction(new org.gjt.sp.jedit.actions.open_url());
		addAction(new org.gjt.sp.jedit.actions.paste());
		addAction(new org.gjt.sp.jedit.actions.paste_predefined());
		addAction(new org.gjt.sp.jedit.actions.paste_previous());
		addAction(new org.gjt.sp.jedit.actions.plugin_options());
		addAction(new org.gjt.sp.jedit.actions.prev_buffer());
		addAction(new org.gjt.sp.jedit.actions.prev_paragraph());
		addAction(new org.gjt.sp.jedit.actions.print());
		addAction(new org.gjt.sp.jedit.actions.redo());
		addAction(new org.gjt.sp.jedit.actions.reload());
		addAction(new org.gjt.sp.jedit.actions.replace_all());
		addAction(new org.gjt.sp.jedit.actions.replace_in_selection());
		addAction(new org.gjt.sp.jedit.actions.replace_next());
		addAction(new org.gjt.sp.jedit.actions.save());
		addAction(new org.gjt.sp.jedit.actions.save_all());
		addAction(new org.gjt.sp.jedit.actions.save_as());
		addAction(new org.gjt.sp.jedit.actions.save_url());
		addAction(new org.gjt.sp.jedit.actions.scroll_line());
		addAction(new org.gjt.sp.jedit.actions.select_all());
		addAction(new org.gjt.sp.jedit.actions.select_anchor());
		addAction(new org.gjt.sp.jedit.actions.select_block());
		addAction(new org.gjt.sp.jedit.actions.select_buffer());
		addAction(new org.gjt.sp.jedit.actions.select_line_range());
		addAction(new org.gjt.sp.jedit.actions.select_next_paragraph());
		addAction(new org.gjt.sp.jedit.actions.select_prev_paragraph());
		addAction(new org.gjt.sp.jedit.actions.send());
		addAction(new org.gjt.sp.jedit.actions.set_anchor());
		addAction(new org.gjt.sp.jedit.actions.set_marker());
		addAction(new org.gjt.sp.jedit.actions.shift_left());
		addAction(new org.gjt.sp.jedit.actions.shift_right());
		addAction(new org.gjt.sp.jedit.actions.tab());
		addAction(new org.gjt.sp.jedit.actions.to_lower());
		addAction(new org.gjt.sp.jedit.actions.to_upper());
		addAction(new org.gjt.sp.jedit.actions.undo());
		addAction(new org.gjt.sp.jedit.actions.untab());
		addAction(new org.gjt.sp.jedit.actions.wing_comment());
		addAction(new org.gjt.sp.jedit.actions.word_count());
	}

	/**
	 * Initializes option panels.
	 */
	private static void initOptions()
	{
		optionPanes = new Vector();
	}

	/**
	 * Loads plugins.
	 */
	private static void initPlugins()
	{
		plugins = new Vector();
		pluginMenus = new Vector();
		loadPlugins(jEditHome + "jars");
		if(settingsDirectory != null)
			loadPlugins(settingsDirectory + File.separator + "jars");
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
				loadProps(new FileInputStream(settingsDirectory
					+ File.separator + "properties"));
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
		String lf = props.getProperty("lf");
		try
		{
			if(lf != null)
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
		recent = new Vector();

		for(int i = 0; i < maxRecent; i++)
		{
			String recentFile = getProperty("recent." + i);
			if(recentFile != null)
				recent.addElement(recentFile);
		}
	}

	/**
	 * Initializes the jEdit RMI service.
	 */
	private static void initRMI()
	{
		if(serverPath != null)
		{
			if(remoteEditor == null)
			{
				try
				{
					remoteEditor = new RemoteEditorImpl();
					Naming.rebind(serverPath,remoteEditor);
					System.out.println("Started RMI service on "
						+ serverPath);
				}
				catch(AccessException a)
				{
					System.err.println("The RMI registry cannot"
						+ " determine the local host.");
					System.err.println("On Unix, try changing the"
						+ "/etc/hosts file so that");
					System.err.println("127.0.0.1 is mapped to this"
						+ " machine's host name, and is aliased");
					System.err.println("to localhost.");
					a.printStackTrace();

				}
				catch(java.rmi.ConnectException c)
				{
					System.err.println("Error starting RMI service "
						+ serverPath + ":");
					System.err.println("rmiregistry not running");
				}
				catch(Exception e)
				{
					System.err.println("Error starting RMI service "
						+ serverPath + ":");
					e.printStackTrace();
					remoteEditor = null;
				}
			}
		}
	}

	/**
	 * Loads the key bindings.
	 */
	private static void initKeyBindings()
	{
		// This is also called from propertiesChanged(), so we
		// save some gc by reusing the old input handler
		if(inputHandler == null)
			inputHandler = new DefaultInputHandler();
		else
			inputHandler.removeAllKeyBindings();

		// Register menu key bindings
		inputHandler.removeAllKeyBindings();
		EditAction[] actions = jEdit.getActions();
		for(int i = 0; i < actions.length; i++)
		{
			String binding = jEdit.getProperty(actions[i]
				.getName() + ".shortcut");
			if(binding != null)
				inputHandler.addKeyBinding(binding,actions[i]);
		}

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

	private static Buffer loadDesktop()
	{
		Buffer b = null;
		try
		{
			int i = 0;
			String path;
			while((path = getProperty("desktop." + i + ".path")) != null)
			{
				String mode = getProperty("desktop." + i + ".mode");
				boolean readOnly = "yes".equals(getProperty(
					"desktop." + i + ".readOnly"));
				boolean current = "yes".equals(getProperty(
					"desktop." + i + ".current"));
				int selStart = Integer.parseInt(getProperty(
					"desktop." + i + ".selStart"));
				int selEnd = Integer.parseInt(getProperty(
					"desktop." + i + ".selEnd"));
				Buffer buffer = openFile(null,null,path,readOnly,
					false);
				buffer.setCaretInfo(selStart,selEnd);
				Mode m = getMode(mode);
				if(m != null)
					buffer.setMode(m);
				if(current)
					b = buffer;
				i++;
			}
		}
		catch(Exception e)
		{
			System.err.println("Error while loading desktop:");
			e.printStackTrace();
		}
		if(b == null && buffersLast != null)
			b = buffersLast;
		return b;
	}

	private static void gotoMarker(Buffer buffer, View view,
		String marker)
	{
		Marker m = buffer.getMarker(marker);
		if(m == null)
			return;
		if(view == null || view.getBuffer() != buffer)
			buffer.setCaretInfo(m.getStart(),m.getEnd());
		else if(view != null)
			view.getTextArea().select(m.getStart(),m.getEnd());
	}

	private static void addBufferToList(Buffer buffer)
	{
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
		if(buffer == buffersFirst)
			buffersFirst = buffer.next;
		else
			buffer.prev.next = buffer.next;

		if(buffer == buffersLast)
			buffersLast = buffersLast.prev;
	}

	private static void addViewToList(View view)
	{
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
		if(view == viewsFirst)
			viewsFirst = view.next;
		else
			view.prev.next = view.next;

		if(view == viewsLast)
			viewsLast = viewsLast.prev;
	}

	private static boolean _closeBuffer(View view, Buffer buffer)
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
			else if(result == JOptionPane.NO_OPTION)
			{
				buffer.getAutosaveFile().delete();
				buffer.setDirty(false);
			}
			else
				return false;
		}
		if(!buffer.isNewFile())
		{
			String path = buffer.getPath();
			if(recent.contains(path))
				recent.removeElement(path);
			recent.insertElementAt(path,0);
			if(recent.size() > maxRecent)
				recent.removeElementAt(maxRecent);
		}
		return true;
	}

	private static class Autosave extends Thread
	{
		Autosave()
		{
			super("***jEdit autosave thread***");
			setDaemon(true);
			start();
		}

		public void run()
		{
			int interval;
			try
			{
				interval = Integer.parseInt(jEdit.getProperty(
					"autosave"));
			}
			catch(NumberFormatException nf)
			{
				interval = 15;
			}
			if(interval == 0)
				return;
			interval *= 1000;
			for(;;)
			{
				try
				{
					Thread.sleep(interval);
				}
				catch(InterruptedException i)
				{
					return;
				}
				if(interrupted())
					return;
				Buffer[] bufferArray = jEdit.getBuffers();
				for(int i = 0; i < bufferArray.length; i++)
					bufferArray[i].autosave();
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.125  1999/08/21 01:48:18  sp
 * jEdit 2.0pre8
 *
 * Revision 1.124  1999/07/29 08:50:21  sp
 * Misc stuff for 1.7pre7
 *
 * Revision 1.123  1999/07/21 05:45:14  sp
 * AltGr fix, documentation restructuring
 *
 * Revision 1.122  1999/07/16 23:45:49  sp
 * 1.7pre6 BugFree version
 *
 * Revision 1.121  1999/07/08 06:06:04  sp
 * Bug fixes and miscallaneous updates
 *
 * Revision 1.120  1999/07/05 04:38:39  sp
 * Massive batch of changes... bug fixes, also new text component is in place.
 * Have fun
 *
 * Revision 1.119  1999/06/23 08:02:14  sp
 * Text area updates
 *
 * Revision 1.118  1999/06/22 06:14:39	sp
 * RMI updates, text area updates, flag to disable geometry saving
 *
 * Revision 1.117  1999/06/20 02:15:45	sp
 * Syntax coloring optimizations
 *
 * Revision 1.116  1999/06/16 03:29:59	sp
 * Added <title> tags to docs, configuration data is now stored in a
 * ~/.jedit directory, style option pane finished
 *
 * Revision 1.115  1999/06/15 05:03:54	sp
 * RMI interface complete, save all hack, views & buffers are stored as a link
 * list now
 *
 * Revision 1.114  1999/06/14 08:21:07	sp
 * Started rewriting `jEdit server' to use RMI (doesn't work yet)
 *
 * Revision 1.113  1999/06/13 05:47:02	sp
 * Minor changes required for LatestVersion plugin
 *
 * Revision 1.112  1999/06/12 02:30:27	sp
 * Find next can now perform multifile searches, multifile-search command added,
 * new style option pane
 *
 * Revision 1.111  1999/06/07 06:36:32	sp
 * Syntax `styling' (bold/italic tokens) added,
 * plugin options dialog for plugin option panes
 *
 * Revision 1.110  1999/06/05 07:17:08	sp
 * Cascading makefiles, HyperSearch tweak, doc updates
 *
 */
