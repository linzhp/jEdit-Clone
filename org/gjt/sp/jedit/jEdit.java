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

import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.zip.*;
import java.util.*;
import org.gjt.sp.jedit.event.*;
import org.gjt.sp.jedit.gui.*;

/**
 * The main class of the jEdit text editor.
 */
public class jEdit
{
	/**
	 * Returns the jEdit version as a human-readable string.
	 */
	public static String getVersion()
	{
		return "1.6pre7";
	}

	/**
	 * Returns the internal version. String.compareTo() can be used
	 * to compare different internal versions.
	 */
	public static String getBuild()
	{
		// (major) (minor) (<99 = preX, 99 = final) (bug fix)
		return "01.06.07.00";
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
		String userHome = System.getProperty("user.home");
		String userDir = System.getProperty("user.dir");
		usrProps = userHome + File.separator + ".jedit-props";
		historyFile = userHome + File.separator + ".jedit-history";
		portFile = userHome + File.separator + ".jedit-server";
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
				else if(arg.equals("-nousrprops"))
					usrProps = null;
				else if(arg.equals("-nohistory"))
					historyFile = null;
				else if(arg.equals("-noserver"))
					portFile = null;
				else if(arg.equals("-nodesktop"))
					desktop = false;
				else if(arg.equals("-nosplash"))
					showSplash = false;
				else if(arg.startsWith("-usrprops="))
					usrProps = arg.substring(10);
				else if(arg.startsWith("-history="))
					historyFile = arg.substring(9);
				else if(arg.startsWith("-portfile="))
					portFile = arg.substring(10);
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

		// Try to connect to the server
		if(portFile != null && new File(portFile).exists())
		{
			try
			{
				BufferedReader in = new BufferedReader(
					new FileReader(portFile));
				Socket socket = new Socket("localhost",
					Integer.parseInt(in.readLine()));
				DataOutputStream out = new DataOutputStream(
					socket.getOutputStream());
				out.writeBytes(in.readLine());
				out.write('\n');
				if(readOnly)
					out.writeBytes("-readonly\n");
				out.writeBytes("-cwd=" + userDir + "\n");
				if(lineNo != -1)
					out.writeBytes("-+" + lineNo + "\n");
				boolean opened = false;
				for(int i = 0; i < args.length; i++)
				{
					if(args[i] == null)
						continue;
					out.writeBytes(args[i]);
					out.write('\n');
					opened = true;
				}
				if(!opened)
					out.write('\n');
				socket.close();
				in.close();
				return;
			}
			catch(Exception e)
			{
				System.out.println("jEdit: connection to server failed");
			}
		}

		// Ok, server isn't running; start jEdit

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
		if(historyFile != null)
			HistoryModel.loadHistory(historyFile);

		// Start plugins
		JARClassLoader.initPlugins();

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

		// Create the view
		newView(null,buffer);

		// Dispose of the splash screen
		GUIUtilities.hideSplashScreen();
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
		props.load(in);
		in.close();
	}

	/**
	 * Fetches a property, returning null if it's not defined.
	 * @param name The property
	 */
	public static String getProperty(String name)
	{
		return props.getProperty(name);
	}

	/**
	 * Fetches a property, returning the default value if it's not
	 * defined.
	 * @param name The property
	 * @param def The default value
	 */
	public static String getProperty(String name, String def)
	{
		return props.getProperty(name,def);
	}

	/**
	 * Returns the property with the specified name, formatting it with
	 * the <code>java.text.MessageFormat.format()</code> method.
	 * @param name The property
	 * @param args The positional parameters
	 */
	public static String getProperty(String name, Object[] args)
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
	public static void setProperty(String name, String value)
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
	public static void setDefaultProperty(String name, String value)
	{
		defaultProps.put(name,value);
	}

	/**
	 * Unsets (clears) a property.
	 * @param name The property
	 */
	public static void unsetProperty(String name)
	{
		props.remove(name);
	}

	/**
	 * Reloads various settings from the properties.
	 */
	public static void propertiesChanged()
	{
		if(server != null)
		{
			server.stopServer();
			server = null;
		}
		if(autosave != null)
			autosave.interrupt();

		if("on".equals(getProperty("server"))
			&& portFile != null)
			server = new Server();
			
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
	}
	
	/**
	 * Loads all plugins in a directory.
	 * @param directory The directory
	 */
	public static void loadPlugins(String directory)
	{
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
				String[] args = { plugin };
				System.err.println(jEdit.getProperty(
					"jar.error.load",args));
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
		if(actions == null)
		{
			actions = new EditAction[actionHash.size()];
			Enumeration enum = actionHash.elements();
			int i = 0;
			while(enum.hasMoreElements())
			{
				actions[i++] = (EditAction)enum.nextElement();
			}
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
		for(int i = 0; i < buffers.size(); i++)
		{
			Buffer buffer = (Buffer)buffers.elementAt(i);
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
		}
		Buffer buffer = new Buffer(view,url,path,readOnly,newFile);
		buffers.addElement(buffer);
		if(marker != null)
			gotoMarker(buffer,null,marker);
		if(view != null)
			view.setBuffer(buffer);
		
		fireEditorEvent(new EditorEvent(EditorEvent.BUFFER_CREATED,
			view,buffer));

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
			if(buffers.size() == 1)
				exit(view);
			else
				buffers.removeElement(buffer);
		}
		else
			return false;

		fireEditorEvent(new EditorEvent(EditorEvent.BUFFER_CLOSED,
			view,buffer));

		return true;
	}

	/**
	 * Returns the buffer with the specified path name.
	 * @param path The path name
	 */
	public static Buffer getBuffer(String path)
	{
		Buffer[] bufferArray = getBuffers();
		for(int i = 0; i < bufferArray.length; i++)
		{
			Buffer buffer = bufferArray[i];
			if(buffer.getPath().equals(path))
				return buffer;
		}
		return null;
	}

	/**
	 * Returns an array of open buffers.
	 */
	public static Buffer[] getBuffers()
	{
		Buffer[] array = new Buffer[buffers.size()];
		buffers.copyInto(array);
		return array;
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
			view.saveCaretInfo();
		View newView = new View(view,buffer);
		views.addElement(newView);
		fireEditorEvent(new EditorEvent(EditorEvent.VIEW_CREATED,
			newView,buffer));
		return newView;
	}

	/**
	 * Closes a view. jEdit will exit if this was the last open view.
	 */
	public static void closeView(View view)
	{
		if(views.size() == 1)
			exit(view); /* exit does editor event & save */
		else
		{
			fireEditorEvent(new EditorEvent(EditorEvent.VIEW_CLOSED,
				view,view.getBuffer()));

			view.close();

			view.dispose();
			views.removeElement(view);
		}


	}

	/**
	 * Returns an array of all open views.
	 */
	public static View[] getViews()
	{
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
	 * Adds an editor event listener to the global editor listener
	 * list.
	 * @param listener The editor event listener
	 */
	public static void addEditorListener(EditorListener listener)
	{
		multicaster.addListener(listener);
	}

	/**
	 * Removes an editor event listener from the global editor listener
	 * list.
	 * @param listener The editor event listener
	 */
	public static void removeEditorListener(EditorListener listener)
	{
		multicaster.removeListener(listener);
	}

	/**
	 * Fires an editor event to all registered listeners.
	 * @param evt The event
	 */
	public static void fireEditorEvent(EditorEvent evt)
	{
		multicaster.fire(evt);
	}

	/**
	 * Exits cleanly from jEdit, prompting the user if any unsaved files
	 * should be saved first.
	 * @param view The view from which this exit was called
	 */
	public static void exit(View view)
	{
		// Save the `desktop'
		if("on".equals(getProperty("saveDesktop")))
		{
			int bufNum = 0;
			view.saveCaretInfo();
			for(int i = 0; i < buffers.size(); i++)
			{
				Buffer buffer = (Buffer)buffers.elementAt(i);
				if(buffer.isNewFile())
					continue;
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
			}
			unsetProperty("desktop." + bufNum + ".path");
		}

		// Close all buffers
		for(int i = buffers.size() - 1; i >= 0; i--)	
		{
			if(!_closeBuffer(view,(Buffer)buffers.elementAt(i)))
				return;
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

		// Stop the server
		if(server != null)
			server.stopServer();

		// Save the recent file list
		for(int i = 0; i < recent.size(); i++)
		{
			String file = (String)recent.elementAt(i);
			setProperty("recent." + i,file);
		}
		unsetProperty("recent." + maxRecent);

		// Save the history lists
		HistoryModel.saveHistory(historyFile);

		// Write the user properties file
		if(usrProps != null)
		{
			try
			{
				OutputStream out = new FileOutputStream(
					usrProps);
				props.save(out,"Use the -nousrprops switch"
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
	private static String usrProps;
	private static String historyFile;
	private static String portFile;
	private static Properties defaultProps;
	private static Properties props;
	private static Server server;
	private static Autosave autosave;
	private static Hashtable actionHash;
	private static EditAction[] actions;
	private static Vector plugins;
	private static Vector pluginMenus;
	private static Vector pluginActions;
	private static Vector modes;
	private static Vector optionPanes;
	private static int untitledCount;
	private static Vector buffers;
	private static Vector views;
	private static Vector recent;
	private static int maxRecent;
	private static EventMulticaster multicaster;

	private jEdit() {}

	private static void usage()
	{
		System.err.println("Usage: jedit [<options>] [<files>]");
		System.err.println("Valid options:");
		System.err.println("    --: End of options");
		System.err.println("    -version: Print jEdit version and"
			+ " exit");
		System.err.println("    -usage: Print this message and exit");
		System.err.println("    -nousrprops: Don't load user"
			+ " properties");
		System.err.println("    -nohistory: Don't load history lists");
		System.err.println("    -noserver: Don't start server");
		System.err.println("    -nodesktop: Ignore saved desktop");
		System.err.println("    -nosplash: Don't show splash screen");
		System.err.println("    -usrprops=<file>: Read user properties"
			+ " from <file>");
		System.err.println("    -history=<file>: Load history list"
			+ " from <file>");
		System.err.println("    -portfile=<file>: Write server port to"
			+ " <file>");
		System.err.println("    -readonly: Open files read-only");
		System.err.println("    -+<line>: Go to line number <line> of"
			+ " opened file");
		System.err.println();
		System.err.println("Report bugs to Slava Pestov <sp@gjt.org>.");
	}

	private static void version()
	{
		System.err.println("jEdit " + getVersion() + " build " +
			getBuild());
	}

	/**
	 * Initialise various objects, register protocol handlers,
	 * register editor listener, and determine installation
	 * directory.
	 */
	private static void initMisc()
	{
		buffers = new Vector();
		views = new Vector();
		multicaster = new EventMulticaster();

		// Add our protcols to java.net.URL's list
		System.getProperties().put("java.protocol.handler.pkgs",
			"org.gjt.sp.jedit.proto|" +
			System.getProperty("java.protocol.handler.pkgs",""));

		// Add PROPERTIES_CHANGED listener
		addEditorListener(new EditorHandler());
		
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
		addAction(new org.gjt.sp.jedit.actions.browser_open_sel());
		addAction(new org.gjt.sp.jedit.actions.browser_open_url());
		addAction(new org.gjt.sp.jedit.actions.buffer_options());
		addAction(new org.gjt.sp.jedit.actions.clear());
		addAction(new org.gjt.sp.jedit.actions.clear_marker());
		addAction(new org.gjt.sp.jedit.actions.close_file());
		addAction(new org.gjt.sp.jedit.actions.close_view());
		addAction(new org.gjt.sp.jedit.actions.compile());
		addAction(new org.gjt.sp.jedit.actions.copy());
		addAction(new org.gjt.sp.jedit.actions.cut());
		addAction(new org.gjt.sp.jedit.actions.delete_end_line());
		addAction(new org.gjt.sp.jedit.actions.delete_line());
		addAction(new org.gjt.sp.jedit.actions.delete_no_indent());
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
                addAction(new org.gjt.sp.jedit.actions.indent_on_enter());
                addAction(new org.gjt.sp.jedit.actions.indent_on_tab());	
		addAction(new org.gjt.sp.jedit.actions.insert_date());
		addAction(new org.gjt.sp.jedit.actions.join_lines());
		addAction(new org.gjt.sp.jedit.actions.locate_bracket());
		addAction(new org.gjt.sp.jedit.actions.new_file());
		addAction(new org.gjt.sp.jedit.actions.new_view());
		addAction(new org.gjt.sp.jedit.actions.next_buffer());
		addAction(new org.gjt.sp.jedit.actions.next_error());
		addAction(new org.gjt.sp.jedit.actions.next_paragraph());
		addAction(new org.gjt.sp.jedit.actions.open_file());
		addAction(new org.gjt.sp.jedit.actions.open_path());
		addAction(new org.gjt.sp.jedit.actions.open_selection());
		addAction(new org.gjt.sp.jedit.actions.open_url());
		addAction(new org.gjt.sp.jedit.actions.paste());
		addAction(new org.gjt.sp.jedit.actions.paste_predefined());
		addAction(new org.gjt.sp.jedit.actions.paste_previous());
		addAction(new org.gjt.sp.jedit.actions.plugin_help());
		addAction(new org.gjt.sp.jedit.actions.prev_buffer());
		addAction(new org.gjt.sp.jedit.actions.prev_error());
		addAction(new org.gjt.sp.jedit.actions.prev_paragraph());
		addAction(new org.gjt.sp.jedit.actions.print());
		addAction(new org.gjt.sp.jedit.actions.redo());
		addAction(new org.gjt.sp.jedit.actions.reload());
		addAction(new org.gjt.sp.jedit.actions.replace());
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
		addAction(new org.gjt.sp.jedit.actions.select_no_indent());
		addAction(new org.gjt.sp.jedit.actions.select_prev_paragraph());
		addAction(new org.gjt.sp.jedit.actions.send());
		addAction(new org.gjt.sp.jedit.actions.set_anchor());
		addAction(new org.gjt.sp.jedit.actions.set_marker());
		addAction(new org.gjt.sp.jedit.actions.shift_left());
		addAction(new org.gjt.sp.jedit.actions.shift_right());
		addAction(new org.gjt.sp.jedit.actions.tab());
		addAction(new org.gjt.sp.jedit.actions.toggle_console());
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

		addOptionPane(org.gjt.sp.jedit.options.GeneralOptionPane.class);
		addOptionPane(org.gjt.sp.jedit.options.EditorOptionPane.class);
		addOptionPane(org.gjt.sp.jedit.options.KeyTableOptionPane.class);
		addOptionPane(org.gjt.sp.jedit.options.ColorTableOptionPane.class);
	}

	/**
	 * Loads plugins.
	 */
	private static void initPlugins()
	{
		plugins = new Vector();
		pluginMenus = new Vector();
		loadPlugins(jEditHome + "jars");
		loadPlugins(System.getProperty("user.home") + File.separator
			+ ".jedit-jars");
	}

	/**
	 * Loads user properties.
	 */
	private static void initUserProperties()
	{
		props = new Properties(defaultProps);

		if(usrProps != null)
		{
			try
			{
				loadProps(new FileInputStream(usrProps));
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
		if(b == null && buffers.size() > 0)
			b = (Buffer)buffers.elementAt(buffers.size() - 1);
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

	private static class EditorHandler extends EditorAdapter
	{
		public void propertiesChanged(EditorEvent evt)
		{
			jEdit.propertiesChanged();
		}
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

	private static class Server extends Thread
	{
		private ServerSocket server;
		private long authInfo;

		Server()
		{
			super("***jEdit server thread***");
			setDaemon(true);
			start();
		}

		public void run()
		{
			try
			{
				server = new ServerSocket(0);
				int port = server.getLocalPort();
			
				Random random = new Random();
				authInfo = Math.abs(random.nextLong());
			
				BufferedWriter out = new BufferedWriter(
					new FileWriter(portFile));
				out.write(String.valueOf(port));
				out.write('\n');
				out.write(String.valueOf(authInfo));
				out.write('\n');
				out.close();
				for(;;)
				{
					Socket client = server.accept();
					System.out.println("jEdit: connection from "
						+ client.getInetAddress());
					// Paranoid thread safety
					// (We have a nasty DoS here if client
					// opens connecton and never closes it,
					// but it's not too catastrophic since
					// the autosaver continues running)
					SwingUtilities.invokeLater(new
						ServerClientHandler(client,
						authInfo));
				}
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
		}

		public void stopServer()
		{
			if(server == null)
				return;
			stop();
			try
			{
				server.close();
				server = null;
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
			new File(portFile).delete();
		}
	}

	private static class ServerClientHandler implements Runnable
	{
		private Socket client;
		private long authInfo;

		ServerClientHandler(Socket client, long authInfo)
		{
			this.client = client;
			this.authInfo = authInfo;
		}

		public void run()
		{
			View view = null;
			String authString = null;
			int lineNo = -1;
			try
			{
				BufferedReader in = new BufferedReader(
					new InputStreamReader(client
					.getInputStream()));
				authString = in.readLine();
				long auth = Long.parseLong(authString);
				if(auth != authInfo)
				{
					System.err.println("jEdit: wrong authorization key: "
						+ auth);
					client.close();
					return;
				}
				String filename;
				String cwd = "";
				boolean endOpts = false;
				boolean readOnly = false;
				Buffer buffer = null;
				while((filename = in.readLine()) != null)
				{
					if(filename.startsWith("-") && !endOpts)
					{
						if(filename.equals("--"))
							endOpts = true;
						else if(filename.equals(
							"-readonly"))
							readOnly = true;
						else if(filename.startsWith(
							"-cwd="))
							cwd = filename.substring(5);
						else if(filename.startsWith("-+"))
						{
							try
							{
								lineNo = Integer.parseInt(
									filename.substring(2));
							}
							catch(NumberFormatException nf)
							{
							}
						}
					}
					else
					{
						if(filename.length() == 0)
							buffer = jEdit.newFile(null);
						else
							buffer = jEdit.openFile(
								null,
								cwd,filename,
								readOnly,
								false);
					}
				}
				if(buffer != null)
				{
					if(lineNo != -1)
					{
						Element lineElement = buffer
							.getDefaultRootElement()
							.getElement(lineNo - 1);
						if(lineElement != null)
						{
							buffer.setCaretInfo(lineElement
								.getStartOffset(),
								lineElement
								.getStartOffset());
						}
					}
					jEdit.newView(null,buffer);
				}
			}
			catch(NumberFormatException nf)
			{
				System.err.println("jEdit: invalid authorization key: "
					+ authString);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				client.close();
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.99  1999/05/12 05:23:41  sp
 * Fixed compile % -vs- $ bug, also HistoryModel \ bug
 *
 * Revision 1.98  1999/05/09 03:50:16  sp
 * HistoryTextField is now a text field again
 *
 * Revision 1.97  1999/05/08 06:37:21  sp
 * jEdit.VERSION/BUILD becomes jEdit.getVersion()/getBuild(), plugin dependencies
 *
 * Revision 1.96  1999/05/08 00:13:00  sp
 * Splash screen change, minor documentation update, toolbar API fix
 *
 * Revision 1.95  1999/05/06 07:16:14  sp
 * Plugins can use classes from other loaded plugins
 *
 * Revision 1.94  1999/05/06 05:16:17  sp
 * Syntax text are compile fix, FAQ updated
 *
 * Revision 1.93  1999/05/05 07:20:45  sp
 * jEdit 1.6pre5
 *
 * Revision 1.92  1999/05/04 04:51:25  sp
 * Fixed HistoryTextField for Swing 1.1.1
 *
 * Revision 1.91  1999/05/03 08:28:14  sp
 * Documentation updates, key binding editor, syntax text area bug fix
 *
 * Revision 1.90  1999/05/03 04:28:01  sp
 * Syntax colorizing bug fixing, console bug fix for Swing 1.1.1
 *
 * Revision 1.89  1999/05/02 00:07:21  sp
 * Syntax system tweaks, console bugfix for Swing 1.1.1
 *
 * Revision 1.88  1999/05/01 00:55:11  sp
 * Option pane updates (new, easier API), syntax colorizing updates
 *
 * Revision 1.87  1999/04/30 23:20:38  sp
 * Improved colorization of multiline tokens
 *
 * Revision 1.86  1999/04/27 06:53:38  sp
 * JARClassLoader updates, shell script token marker update, token marker compiles
 * now
 *
 */
