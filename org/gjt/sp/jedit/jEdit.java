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

import javax.swing.text.Segment;
import javax.swing.*;
import gnu.regexp.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.zip.*;
import java.util.*;
import org.gjt.sp.jedit.gui.*;

/**
 * The main class of the jEdit text editor.
 */
public class jEdit
{
	/**
	 * The jEdit version.
	 */
	public static final String VERSION = "1.4pre1";
	
	/**
	 * The date when a change was last made to the source code,
	 * in <code>YYYYMMDD</code> format.
	 */
	public static final String BUILD = "19990126";

	/**
	 * AWK regexp syntax.
	 */
	public static final String AWK = "awk";
	
	/**
	 * ED regexp syntax.
	 */
	public static final String ED = "ed";
	
	/**
	 * EGREP regexp syntax.
	 */
	public static final String EGREP = "egrep";
	
	/**
	 * EMACS regexp syntax.
	 */
	public static final String EMACS = "emacs";
	
	/**
	 * GREP regexp syntax.
	 */
	public static final String GREP = "grep";
	
	/**
	 * PERL4 regexp syntax.
	 */
	public static final String PERL4 = "perl4";
	
	/**
	 * PERL5 regexp syntax.
	 */
	public static final String PERL5 = "perl5";
	
	/**
	 * SED regexp syntax.
	 */
	public static final String SED = "sed";
	
	/**
	 * The user properties file.
	 */
	public static final String USER_PROPS = System.getProperty("user.home")
		+ File.separator + ".jedit-props";
	/**
	 * The values that can be stored in the
	 * <code>search.regexp.value</code> property to specify the regexp
	 * syntax.
	 */
	public static final String[] SYNTAX_LIST = { AWK, ED, EGREP, EMACS,
		GREP, PERL4, PERL5, SED };

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
		usrProps = System.getProperty("user.home") + File.separator
			+ ".jedit-props";
		portFile = new File(userHome,".jedit-server");
		desktopFile = new File(userHome,".jedit-desktop");
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
				else if(arg.equals("-noserver"))
					portFile = null;
				else if(arg.equals("-nodesktop"))
					desktopFile = null;
				else if(arg.equals("-nosplash"))
					showSplash = false;
				else if(arg.startsWith("-usrprops="))
					usrProps = arg.substring(10);
				else if(arg.startsWith("-portfile="))
					portFile = new File(arg.substring(10));
				else if(arg.startsWith("-desktopfile="))
					desktopFile = new File(arg.substring(13));
				else if(arg.equals("-readonly"))
					readOnly = true;
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
		if(portFile != null && portFile.exists())
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
				System.out.println("jEdit: stale port file deleted");
				portFile.delete();
			}
		}

		// Show the kool splash screen
		SplashScreen splash = null;
		if(showSplash)
			splash = new SplashScreen();

		// Create several objects
		props = new Properties();
		actionHash = new Hashtable();
		modes = new Vector();
		loader = new JarClassLoader();
		plugins = new Vector();
		buffers = new Vector();
		views = new Vector();
		recent = new Vector();
		clipHistory = new Vector();
		
		// Determine installation directory
		jEditHome = System.getProperty("jedit.home");
		if(jEditHome == null)
		{
			String classpath = System
				.getProperty("java.class.path");
			int index = classpath.toLowerCase()
				.indexOf("jedit.jar");
			if(index > 0)
			{
				int start = classpath.lastIndexOf(File
					.pathSeparator,index) + 1;
				jEditHome = classpath.substring(start,
					index - 1);
			}
			else
				jEditHome = userDir;
		}
		jEditHome = jEditHome + File.separator;

		// Load properties
		try
		{
			loadProps(props.getClass().getResourceAsStream(
				"/org/gjt/sp/jedit/jedit.props"),
				jEditHome + "jedit.props");
			loadProps(props.getClass().getResourceAsStream(
				"/org/gjt/sp/jedit/jedit_gui.props"),jEditHome
				+ "jedit_gui.props");
			loadProps(props.getClass().getResourceAsStream(
				"/org/gjt/sp/jedit/jedit_keys.props"),jEditHome
				+ "jedit_keys.props");
			loadProps(props.getClass().getResourceAsStream(
				"/org/gjt/sp/jedit/jedit_tips.props"),jEditHome
				+ "jedit_tips.props");	
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
			return;
		}

		// Load edit modes
		addMode(new org.gjt.sp.jedit.mode.amstex());
		addMode(new org.gjt.sp.jedit.mode.autoindent());
		addMode(new org.gjt.sp.jedit.mode.bat());
                addMode(new org.gjt.sp.jedit.mode.c());
                addMode(new org.gjt.sp.jedit.mode.cc());
		addMode(new org.gjt.sp.jedit.mode.html());
		addMode(new org.gjt.sp.jedit.mode.java_mode());
		addMode(new org.gjt.sp.jedit.mode.javascript());
		addMode(new org.gjt.sp.jedit.mode.latex());
		addMode(new org.gjt.sp.jedit.mode.makefile());
		addMode(new org.gjt.sp.jedit.mode.sh());
		addMode(new org.gjt.sp.jedit.mode.tex());

		// Load actions
		addAction(new org.gjt.sp.jedit.actions.about());
		addAction(new org.gjt.sp.jedit.actions.block_comment());
		addAction(new org.gjt.sp.jedit.actions.box_comment());
		addAction(new org.gjt.sp.jedit.actions.browser_open_sel());
		addAction(new org.gjt.sp.jedit.actions.browser_open_url());
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
		addAction(new org.gjt.sp.jedit.actions.execute());
		addAction(new org.gjt.sp.jedit.actions.exit());
		addAction(new org.gjt.sp.jedit.actions.expand_abbrev());
		addAction(new org.gjt.sp.jedit.actions.find());
		addAction(new org.gjt.sp.jedit.actions.find_next());
		addAction(new org.gjt.sp.jedit.actions.find_selection());
		addAction(new org.gjt.sp.jedit.actions.format());
		addAction(new org.gjt.sp.jedit.actions.generate_text());
		addAction(new org.gjt.sp.jedit.actions.goto_anchor());
		addAction(new org.gjt.sp.jedit.actions.goto_end_indent());
		addAction(new org.gjt.sp.jedit.actions.goto_line());
		addAction(new org.gjt.sp.jedit.actions.goto_marker());
		addAction(new org.gjt.sp.jedit.actions.help());
		addAction(new org.gjt.sp.jedit.actions.hypersearch());
		addAction(new org.gjt.sp.jedit.actions.insert_date());
		addAction(new org.gjt.sp.jedit.actions.join_lines());
		addAction(new org.gjt.sp.jedit.actions.locate_bracket());
		addAction(new org.gjt.sp.jedit.actions.make());
		addAction(new org.gjt.sp.jedit.actions.new_file());
		addAction(new org.gjt.sp.jedit.actions.new_view());
		addAction(new org.gjt.sp.jedit.actions.next_paragraph());
		addAction(new org.gjt.sp.jedit.actions.open_file());
		addAction(new org.gjt.sp.jedit.actions.open_path());
		addAction(new org.gjt.sp.jedit.actions.open_selection());
		addAction(new org.gjt.sp.jedit.actions.open_url());
		addAction(new org.gjt.sp.jedit.actions.options());
		addAction(new org.gjt.sp.jedit.actions.paste());
		addAction(new org.gjt.sp.jedit.actions.paste_previous());
		addAction(new org.gjt.sp.jedit.actions.pipe_selection());
		addAction(new org.gjt.sp.jedit.actions.prev_paragraph());
		addAction(new org.gjt.sp.jedit.actions.print());
		addAction(new org.gjt.sp.jedit.actions.redo());
		addAction(new org.gjt.sp.jedit.actions.replace());
		addAction(new org.gjt.sp.jedit.actions.replace_all());
		addAction(new org.gjt.sp.jedit.actions.replace_next());
		addAction(new org.gjt.sp.jedit.actions.save());
		addAction(new org.gjt.sp.jedit.actions.save_as());
		addAction(new org.gjt.sp.jedit.actions.save_url());
		addAction(new org.gjt.sp.jedit.actions.scroll_line());
		addAction(new org.gjt.sp.jedit.actions.select_all());
		addAction(new org.gjt.sp.jedit.actions.select_anchor());
		addAction(new org.gjt.sp.jedit.actions.select_block());
		addAction(new org.gjt.sp.jedit.actions.select_buffer());
		addAction(new org.gjt.sp.jedit.actions.select_mode());
		addAction(new org.gjt.sp.jedit.actions.select_next_paragraph());
		addAction(new org.gjt.sp.jedit.actions.select_no_indent());
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

		// Load plugins
		loadPlugins(jEditHome + "jars");
		loadPlugins(System.getProperty("user.home") + File.separator
			+ ".jedit-jars");

		// Load user properties
		props = new Properties(props);
		if(usrProps != null)
		{
			try
			{
				loadProps(null,USER_PROPS);
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

		propertiesChanged();

		// Set look and feel
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
		
		// Load recent file list
		for(int i = 0; i < maxRecent; i++)
		{
			String recentFile = getProperty("recent." + i);
			if(recentFile != null)
				recent.addElement(recentFile);
		}

		// Load clip history
		for(int i = 0; i < maxClipHistory; i++)
		{
			String clip = getProperty("clipHistory." + i);
			if(clip != null)
				clipHistory.addElement(clip);
		}

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
			if("on".equals(getProperty("saveDesktop"))
				&& desktopFile != null && desktopFile.exists())
			{
				try
				{
					BufferedReader in = new BufferedReader(
						new FileReader(desktopFile));
					String line;
					while((line = in.readLine()) != null)
					{
						Buffer b = parseDesktopEntry(line);
						if(b != null)
							buffer = b;
					}
					in.close();
				}
				catch(FileNotFoundException fnf)
				{
				}
				catch(IOException io)
				{
					System.err.println("Error while loading desktop:");
					io.printStackTrace();
				}
			}
		}
		if(buffer == null)
			buffer = newFile(null);

		// Dispose of the splash screen
		if(splash != null)
			splash.dispose();

		// Create the view
		newView(buffer);
	}

	/**
	 * Loads the properties from the specified input stream, or if it's
	 * null, the specified file.
	 * @param in The input stream
	 * @param name The file name
	 * @exception FileNotFoundException if the file could not be found
	 * @exception IOException if an I/O error occured
	 */
	public static void loadProps(InputStream in, String name)
		throws FileNotFoundException, IOException
	{
		if(in == null)
			in = new FileInputStream(name);
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
			autosave.stop();
		if("on".equals(getProperty("server"))
			&& portFile != null)
			server = new Server();
			
		autoindent = "on".equals(getProperty("view.autoindent"));
		syntax = "on".equals(getProperty("buffer.syntax"));
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
		for(int i = recent.size() - maxRecent; i > 0; i--)
		{
			recent.removeElementAt(i);
		}
		try
		{
			maxClipHistory = Integer.parseInt(getProperty(
				"clipHistory"));
		}
		catch(NumberFormatException nf)
		{
			maxClipHistory = 100;
		}
		for(int i = clipHistory.size() - maxClipHistory; i > 0; i--)
		{
			clipHistory.removeElementAt(i);
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
			try
			{
				loadPlugin(directory + File.separator + plugins[i]);
			}
			catch(Exception e)
			{
				System.err.println("Error loading plugin: "
					+ plugins[i]);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Loads a plugin.
	 * @param name The plugin path name
	 * @exception IOException if an I/O error occured
	 * @exception ClassNotFoundException if a referenced class is not
	 * contained within the plugin
	 * @exception InstantiationException if a mode or command doesn't
	 * have a zero-argument constructor
	 * @exception IllegalAccessException if a mode or command constructor
	 * isn't declared <code>public</code>.
	 */
	public static void loadPlugin(String name)
		throws IOException, ClassNotFoundException,
		InstantiationException, IllegalAccessException
	{
		if(!name.endsWith(".jar"))
			return;
		System.out.print("Loading plugin ");
		System.out.println(name);
		Vector classes = new Vector();
		ZipFile jar = new ZipFile(name);
		Enumeration entries = jar.entries();
		while(entries.hasMoreElements())
		{
			String cls = loader.loadEntry(jar,(ZipEntry)entries
				.nextElement());
			if(cls != null)
				classes.addElement(cls);
		}
		Enumeration enum = classes.elements();
		while(enum.hasMoreElements())
		{
			Class clazz = loader.loadClass((String)enum.nextElement());
			if(Action.class.isAssignableFrom(clazz))
				addAction((Action)clazz.newInstance());
			else if(Mode.class.isAssignableFrom(clazz))
				addMode((Mode)clazz.newInstance());
		}
	}

	/**
	 * Registers an action with the editor.
	 * @param action The action
	 */
	public static void addAction(Action action)
	{
		actionHash.put(action.getValue(Action.NAME),action);
		if(Boolean.TRUE.equals(action.getValue(EditAction.PLUGIN)))
			plugins.addElement(action);
	}

	/**
	 * Returns a named action.
	 * @param action The action
	 */
	public static Action getAction(String action)
	{
		return (Action)actionHash.get(action);
	}

	/**
	 * Returns the list of actions registered with the editor.
	 */
	public static Action[] getActions()
	{
		if(actions == null)
		{
			actions = new Action[actionHash.size()];
			Enumeration enum = actionHash.elements();
			int i = 0;
			while(enum.hasMoreElements())
			{
				actions[i++] = (Action)enum.nextElement();
			}
		}
		return actions;
	}

	/**
	 * Returns the list of plugins registered with the editor.
	 */
	public static Enumeration getPlugins()
	{
		return plugins.elements();
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
		if(name == null)
			return null;
		Enumeration enum = modes.elements();
		while(enum.hasMoreElements())
		{
			Mode mode = (Mode)enum.nextElement();
			String clsName = mode.getClass().getName();
			if(clsName.substring(clsName.lastIndexOf('.') + 1).equals(name))
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
		if(mode == null)
			return jEdit.props.getProperty("mode.none.name");
		else
		{
			String clsName = mode.getClass().getName();
			return jEdit.props.getProperty("mode." +
				clsName.substring(clsName.lastIndexOf('.') + 1)
				+ ".name");
		}
	}

	/**
	 * Returns all edit modes registered with the editor.
	 */
	public static Enumeration getModes()
	{
		return modes.elements();
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
		URL url = null;
		try
		{
			url = new URL(path);
		}
		catch(MalformedURLException mu)
		{
			path = jEdit.constructPath(parent,path);
			
		}
		for(int i = 0; i < buffers.size(); i++)
		{
			Buffer buffer = (Buffer)buffers.elementAt(i);
			if(buffer.getPath().equals(path))
			{
				if(view != null)
				{
					buffers.removeElementAt(i);
					buffers.addElement(buffer);
					if(marker != null)
						gotoMarker(buffer,view,
							marker);
					if(view.getBuffer() != buffer)
						view.setBuffer(buffer);
				}
				return buffer;
			}
		}
		Buffer buffer = new Buffer(url,path,readOnly,newFile);
		if(!newFile)
		{
			if(recent.contains(path))
				recent.removeElement(path);
			recent.insertElementAt(path,0);
			if(recent.size() > maxRecent)
				recent.removeElementAt(maxRecent);
		}
		if(marker != null)
			gotoMarker(buffer,null,marker);
		if(view != null)
			view.setBuffer(buffer);
		buffers.addElement(buffer);
		Enumeration enum = getViews();
		while(enum.hasMoreElements())
		{
			View v = (View)enum.nextElement();
			v.updateBuffersMenu();
			v.updateOpenRecentMenu();
		}
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
		if(!_closeBuffer(view,buffer))
			return false;
		int index = buffers.indexOf(buffer);
		buffers.removeElement(buffer);
		if(buffers.isEmpty())
			exit(view);
		Buffer prev = (Buffer)buffers.elementAt(Math.max(0,index - 1));
		Enumeration enum = getViews();
		while(enum.hasMoreElements())
		{
			view = (View)enum.nextElement();
			if(view.getBuffer() == buffer)
				view.setBuffer(prev);
			view.updateBuffersMenu();
		}
		return true;
	}

	/**
	 * Returns the buffer with the specified path name.
	 * @param path The path name
	 */
	public static Buffer getBuffer(String path)
	{
		Enumeration enum = getBuffers();
		while(enum.hasMoreElements())
		{
			Buffer buffer = (Buffer)enum.nextElement();
			if(buffer.getPath().equals(path))
				return buffer;
		}
		return null;
	}

	/**
	 * Returns an enumeration of open buffers.
	 */
	public static Enumeration getBuffers()
	{
		return buffers.elements();
	}

	/**
	 * Creates a new view of a buffer.
	 * @param buffer The buffer
	 */
	public static View newView(Buffer buffer)
	{
		View view = new View(buffer);
		views.addElement(view);
		return view;
	}

	/**
	 * Closes a view. jEdit will exit if this was the last open view.
	 */
	public static void closeView(View view)
	{
		if(views.size() == 1)
			jEdit.exit(view);
		else
		{
			view.dispose();
			views.removeElement(view);
		}
	}

	/**
	 * Returns an enumeration of all open views.
	 */
	public static Enumeration getViews()
	{
		return views.elements();
	}

	/**
	 * Returns an enumeration of recently opened files.
	 */
	public static Enumeration getRecent()
	{
		return recent.elements();
	}

	/**
	 * Loads a menubar from the properties for the specified view.
	 * @param view The view to load the menubar for
	 * @param name The property with the list of menus
	 */
	public static JMenuBar loadMenubar(View view, String name)
	{
		if(name == null)
			return null;
		JMenuBar mbar = new JMenuBar();
		String menus = getProperty(name);
		if(menus != null)
		{
			StringTokenizer st = new StringTokenizer(menus);
			while(st.hasMoreTokens())
			{
				JMenu menu = loadMenu(view,st.nextToken());
				if(menu != null)
					mbar.add(menu);
			}
		}
		return mbar;
	}

	/**
	 * Loads a menu from the properties for the specified view.
	 * @param view The view to load the menu for
	 * @param name The menu name
	 */
	public static JMenu loadMenu(View view, String name)
	{
		if(name == null)
			return null;
		JMenu menu = view.getMenu(name);
		if(menu == null)
		{
			String label = getProperty(name + ".label");
			if(label == null)
			{
				System.err.println("menu label is null: "
					+ name);
				return null;
			}
			int index = label.indexOf('$');
			if(index != -1 && label.length() - index > 1)
			{
				menu = new JMenu(label.substring(0,index)
					.concat(label.substring(++index)));
				menu.setMnemonic(Character.toLowerCase(label
					.charAt(index)));
			}
			else
				menu = new JMenu(label);
		}
		else
			return menu;
		String menuItems = getProperty(name);
		if(menuItems != null)
		{
			StringTokenizer st = new StringTokenizer(menuItems);
			while(st.hasMoreTokens())
			{
				String menuItemName = st.nextToken();
				if(menuItemName.equals("-"))
					menu.addSeparator();
				else
				{
					JMenuItem mi = loadMenuItem(view,menuItemName);
					if(mi != null)
						menu.add(mi);
				}
			}
		}
		return menu;
	}

	/**
	 * Loads a menu item from the properties for the specified view.
	 * @param view The view to load the menu for
	 * @param name The menu item name
	 */
	public static JMenuItem loadMenuItem(View view, String name)
	{
		if(name.startsWith("%"))
			return loadMenu(view,name.substring(1));
		String arg;
		String action;
		int index = name.indexOf('@');
		if(index != -1)
		{
			arg = name.substring(index+1);
			action = name.substring(0,index);
		}
		else
		{
			arg = null;
			action = name;
		}
		JMenuItem mi;
                String label = getProperty(name.concat(".label"));
		String keyStroke = getProperty(name.concat(".shortcut"));
		if(label == null)
		{
			System.err.println("Menu item label is null: "
				+ name);
			return null;
		}
		if(keyStroke != null)
		{
			index = keyStroke.indexOf(' ');
			if(index == -1)
				view.addKeyBinding(parseKeyStroke(keyStroke),
					name);
			else
				view.addKeyBinding(parseKeyStroke(keyStroke
					.substring(0,index)),parseKeyStroke(
					keyStroke.substring(index+1)),name);
		}
		index = label.indexOf('$');
                if(index != -1 && label.length() - index > 1)
		{
			mi = new EnhancedMenuItem(label.substring(0,index)
				.concat(label.substring(++index)),keyStroke);
                        mi.setMnemonic(Character.toLowerCase(label.charAt(index)));
		}
		else
			mi = new EnhancedMenuItem(label,keyStroke);
		
		Action a = (Action)actionHash.get(action);
		if(a == null)
			mi.setEnabled(false);
		else
			mi.addActionListener(a);
		mi.setActionCommand(arg);
		return mi;
	}

	/**
	 * Converts a string to a keystroke.
	 * <p>
	 * The keystroke format is described in menus.txt. (Help-&gt;Menus
	 * in jEdit).
	 * @param keyStroke A string description of the key stroke
	 */
	public static KeyStroke parseKeyStroke(String keyStroke)
	{
		if(keyStroke == null)
			return null;
		int modifiers = 0;
		int ch = '\0';
		int index = keyStroke.indexOf('+');
		if(index != -1)
		{
			for(int i = 0; i < index; i++)
			{
				switch(keyStroke.charAt(i))
				{
				case 'A':
					modifiers |= InputEvent.ALT_MASK;
					break;
				case 'C':
					modifiers |= InputEvent.CTRL_MASK;
					break;
				case 'M':
					modifiers |= InputEvent.META_MASK;
					break;
				case 'S':
					modifiers |= InputEvent.SHIFT_MASK;
					break;
				}
			}
		}
		String key = keyStroke.substring(index + 1);
		if(key.length() == 1)
			ch = Character.toUpperCase(key.charAt(0));
		else if(key.length() == 0)
		{
			System.err.println("Invalid key stroke: " + keyStroke);
			return null;
		}
		else
		{
			try
			{
				ch = KeyEvent.class.getField("VK_".concat(key))
					.getInt(null);
			}
			catch(Exception e)
			{
				System.err.println("Invalid key stroke: "
					+ keyStroke);
				return null;
			}
		}		
		return KeyStroke.getKeyStroke(ch,modifiers);
	}
	
	/**
	 * Displays a dialog box.
	 * <p>
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property. The message
	 * is formatted by the property manager with <code>args</code> as
	 * positional parameters.
	 * @param view The view to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 */
	public static void message(View view, String name, Object[] args)
	{
		JOptionPane.showMessageDialog(view,getProperty(name.concat(
			".message"),args),getProperty(name.concat(".title"),
			args),JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Displays an error dialog box.
	 * <p>
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property. The message
	 * is formatted by the property manager with <code>args</code> as
	 * positional parameters.
	 * @param view The view to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 */
	public static void error(View view, String name, Object[] args)
	{
		JOptionPane.showMessageDialog(view,getProperty(name.concat(
			".message"),args),getProperty(name.concat(".title"),
			args),JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays an input dialog box and returns any text the user entered.
	 * <p>
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property.
	 * @param view The view to display the dialog for
	 * @param name The name of the dialog
	 * @param def The text to display by default in the input field
	 */
	public static String input(View view, String name, Object def)
	{
		String retVal = (String)JOptionPane.showInputDialog(view,
			getProperty(name.concat(".message")),getProperty(name
			.concat(".title")),JOptionPane.QUESTION_MESSAGE,null,
			null,def);
		return retVal;
	}

	/**
	 * Displays an input dialog box and returns any text the user entered.
	 * <p>
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property.
	 * @param view The view to display the dialog for
	 * @param name The name of the dialog
	 * @param def The property whose text to display in the input field
	 */
	public static String inputProperty(View view, String name, String def)
	{
		String retVal = (String)JOptionPane.showInputDialog(view,
			getProperty(name.concat(".message")),getProperty(name
			.concat(".title")),JOptionPane.QUESTION_MESSAGE,null,
			null,getProperty(def));
		if(retVal != null)
			setProperty(def,retVal);
		return retVal;
	}
	
	/**
	 * Returns the jEdit install directory.
	 */
	public static String getJEditHome()
	{
		return jEditHome;
	}

	/**
	 * Returns the current regular expression.
	 * <p>
	 * It is created from the <code>search.find.value</code>,
	 * <code>search.ignoreCase.value</code> and
	 * <code>search.regexp.value</code> properties.
	 * @exception REException if the stored regular expression is invalid
	 */
	public static RE getRE()
		throws REException
	{
		String pattern = getProperty("search.find.value");
		if(pattern == null || "".equals(pattern))
			return null;
		return new RE(pattern,("on".equals(getProperty(
			"search.ignoreCase.toggle")) ? RE.REG_ICASE : 0)
			| RE.REG_MULTILINE,jEdit.getRESyntax(getProperty(
			"search.regexp.value")));
	}

	/**
	 * Converts a syntax name to an <code>RESyntax</code> instance.
	 * @param name The syntax name
	 */
	public static RESyntax getRESyntax(String name)
	{
		if(AWK.equals(name))
			return RESyntax.RE_SYNTAX_AWK;
		else if(ED.equals(name))
			return RESyntax.RE_SYNTAX_ED;
		else if(EGREP.equals(name))
			return RESyntax.RE_SYNTAX_EGREP;
		else if(EMACS.equals(name))
			return RESyntax.RE_SYNTAX_EMACS;
		else if(GREP.equals(name))
			return RESyntax.RE_SYNTAX_GREP;
		else if(SED.equals(name))
			return RESyntax.RE_SYNTAX_SED;
		else if(PERL4.equals(name))
			return RESyntax.RE_SYNTAX_PERL4;
		else
			return RESyntax.RE_SYNTAX_PERL5;
	}

	/**
	 * Returns true if syntax colorizing is enabled.
	 */
	public static boolean getSyntaxColorizing()
	{
		return syntax;
	}

	/**
	 * Returns true if auto indent is enabled.
	 */
	public static boolean getAutoIndent()
	{
		return autoindent;
	}

	/**
	 * Checks if a subregion of a <code>Segment</code> is equal to a
	 * string.
	 * @param ignoreCase True if case should be ignored, false otherwise
	 * @param text The segment
	 * @param offset The offset into the segment
	 * @param match The string to match
	 */
	public static boolean regionMatches(boolean ignoreCase, Segment text,
					    int offset, String match)
	{
		if(text.count < match.length())
			return false;
		int length = offset + match.length();
		char[] textArray = text.array;
		for(int i = offset, j = 0; i < length; i++, j++)
		{
			char c1 = textArray[i];
			char c2 = match.charAt(j);
			if(ignoreCase)
			{
				c1 = Character.toUpperCase(c1);
				c2 = Character.toUpperCase(c2);
			}
			if(c1 != c2)
				return false;
		}
		return true;
	}
	
	/**
	 * Converts a color name to a color object.
	 * @param name The color name
	 */
	public static Color parseColor(String name)
	{
		if(name == null)
			return Color.black;
		else if(name.startsWith("#"))
		{
			try
			{
				return Color.decode(name);
			}
			catch(NumberFormatException nf)
			{
				return Color.black;
			}
		}
		else if("red".equals(name))
			return Color.red;
		else if("green".equals(name))
			return Color.green;
		else if("blue".equals(name))
			return Color.blue;
		else if("yellow".equals(name))
			return Color.yellow;
		else if("orange".equals(name))
			return Color.orange;
		else if("white".equals(name))
			return Color.white;
		else if("lightGray".equals(name))
			return Color.lightGray;
		else if("gray".equals(name))
			return Color.gray;
		else if("darkGray".equals(name))
			return Color.darkGray;
		else if("black".equals(name))
			return Color.black;
		else if("cyan".equals(name))
			return Color.cyan;
		else if("magenta".equals(name))
			return Color.magenta;
		else if("pink".equals(name))
			return Color.pink;
		else
			return Color.black;
	}

	/**
	 * Converts a file name to a class name.
	 * <p>
	 * All slashes characters are replaced with periods and the trailing
	 * '.class' is removed.
	 * @param name The file name
	 */
	public static String fileToClass(String name)
	{
		char[] clsName = name.toCharArray();
		for(int i = clsName.length - 6; i >= 0; i--)
			if(clsName[i] == '/')
				clsName[i] = '.';
		return new String(clsName,0,clsName.length - 6);
	}

	/**
	 * Constructs an absolute path name from a directory and another
	 * path name.
	 * @param parent The directory
	 * @param path The path name
	 */
	public static String constructPath(String parent, String path)
	{
		// absolute pathnames
		if(path.startsWith(File.separator))
			return canonPath(path);
		// windows pathnames, eg C:\document
		else if(path.length() >= 3 && path.charAt(1) == ':'
			&& path.charAt(2) == '\\')
			return canonPath(path);
		// relative pathnames
		else if(parent == null)
			parent = System.getProperty("user.dir");
		// do it!
		if(parent.endsWith(File.separator))
			return canonPath(parent + path);
		else
			return canonPath(parent + File.separator + path);
	}

	/**
	 * Returns the number of leading white space characters in the
	 * specified string.
	 * @param str The string
	 */
	public static int getLeadingWhiteSpace(String str)
	{
		for(int whitespace = 0; whitespace < str.length();)
		{
			switch(str.charAt(whitespace))
			{
			case ' ': case '\t':
				whitespace++;
				break;
			default:
				return whitespace;
			}
		}
		return 0;
	}

	/**
	 * Returns the width of the leading white space in the specified
	 * string.
	 * @param str The string
	 * @param tabSize The tab size
	 */
	public static int getLeadingWhiteSpaceWidth(String str, int tabSize)
	{
		int whitespace = 0;
loop:		for(int i = 0; i < str.length(); i++)
		{
			switch(str.charAt(i))
			{
			case ' ':
				whitespace++;
				break;
			case '\t':
				whitespace += (tabSize - whitespace % tabSize);
				break;
			default:
				break loop;
			}
		}
		return whitespace;
	}

	/**
	 * Creates a string of white space with the specified length.
	 * @param len The length
	 * @param tabSize The tab size
	 * @param noTabs True if tabs should not be used
	 */
	public static String createWhiteSpace(int len, int tabSize,
		boolean noTabs)
	{
		StringBuffer buf = new StringBuffer();
		if(noTabs)
		{
			while(len-- > 0)
				buf.append(' ');
		}
		else		
		{
			int count = len / tabSize;
			while(count-- > 0)
				buf.append('\t');
			count = len % tabSize;
			while(count-- > 0)
				buf.append(' ');
		}
		return buf.toString();
	}

	/**
	 * Adds a string to the clipboard history.
	 * @param str The string
	 */
	public static void addToClipHistory(String str)
	{
		int index = clipHistory.indexOf(str);
		if(index != -1)
			clipHistory.removeElementAt(index);
		clipHistory.addElement(str);
	}

	/**
	 * Returns an enumeration of the clipboard history.
	 */
	public static Vector getClipHistory()
	{
		return clipHistory;
	}

	/**
	 * Exits cleanly from jEdit, prompting the user if any unsaved files
	 * should be saved first.
	 * @param view The view from which this exit was called
	 */
	public static void exit(View view)
	{
		// Write the `desktop' file
		if("on".equals(getProperty("saveDesktop"))
			&& desktopFile != null)
		{
			view.saveCaretInfo();
			try
			{
				FileWriter out = new FileWriter(desktopFile);
				for(int i = 0; i < buffers.size(); i++)
				{
					Buffer buffer = (Buffer)buffers.elementAt(i);
					if(buffer.isNewFile())
						continue;
					out.write(buffer.getPath());
					out.write(File.pathSeparator);
					out.write(buffer.isReadOnly() ? "readOnly" : "");
					out.write(File.pathSeparator);
					Mode mode = buffer.getMode();
					String clazz;
					if(mode != null)
					{
						clazz = buffer.getMode()
							.getClass().getName();
						clazz = clazz.substring(clazz
							.lastIndexOf('.') + 1);
					}
					else
						clazz = "";
					out.write(clazz);
					out.write(File.pathSeparator);
					out.write(String.valueOf(buffer.getSavedSelStart()));
					out.write(File.pathSeparator);
					out.write(String.valueOf(buffer.getSavedSelEnd()));
					out.write(File.pathSeparator);
					if(view.getBuffer() == buffer)
						out.write('*');
					out.write("\r\n");
				}
				out.close();
			}
			catch(IOException io)
			{
				System.err.println("Error while saving desktop:");
				io.printStackTrace();
			}
		}

		// Close all buffers
		for(int i = buffers.size() - 1; i >= 0; i--)	
		{
			if(!_closeBuffer(view,(Buffer)buffers.elementAt(i)))
				return;
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

		// Save the clip history
		for(int i = 0; i < clipHistory.size(); i++)
		{
			String clip = (String)clipHistory.elementAt(i);
			setProperty("clipHistory." + i,clip);
		}

		// Write the user properties file
		if(usrProps != null)
		{
			try
			{
				OutputStream out = new FileOutputStream(
					usrProps);
				props.save(out,"jEdit properties file");
				out.close();
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
		}

		// Byebye...
		System.out.println("Thank you for using jEdit. Send an e-mail"
			+ " to <sp@gjt.org>.");
		System.exit(0);
	}

	// private members
	private static String jEditHome;
	private static String usrProps;
	private static File portFile;
	private static File desktopFile;
	private static Properties props;
	private static boolean autoindent;
	private static boolean syntax;
	private static Server server;
	private static Autosave autosave;
	private static JarClassLoader loader;
	private static Hashtable actionHash;
	private static Action[] actions;
	private static Vector modes;
	private static Vector plugins;
	private static int untitledCount;
	private static Vector buffers;
	private static Vector views;
	private static Vector recent;
	private static int maxRecent;
	private static Vector clipHistory;
	private static int maxClipHistory;

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
		System.err.println("    -noserver: Don't start server");
		System.err.println("    -nodesktop: Ignore saved desktop");
		System.err.println("    -nosplash: Don't show splash screen");
		System.err.println("    -usrprops=<file>: Read user properties"
			+ " from <file>");
		System.err.println("    -portfile=<file>: Write server port to"
			+ " <file>");
		System.err.println("    -desktopfile=<file>: Save desktop to"
			+ " <file>");
		System.err.println("    -readonly: Open files read-only");
		System.err.println();
		System.err.println("Report bugs to Slava Pestov <sp@gjt.org>.");
	}

	private static void version()
	{
		System.err.println("jEdit " + VERSION + " build " + BUILD);
	}

	private static Buffer parseDesktopEntry(String line)
	{
		try
		{
			int pathIndex = line.indexOf(File.pathSeparator);
			String path = line.substring(0,pathIndex);
			int roIndex = line.indexOf(File.pathSeparator,
				pathIndex+1);
			boolean readOnly = (roIndex - pathIndex > 1);
			int modeIndex = line.indexOf(File.pathSeparator,
				roIndex+1);
			String mode = line.substring(roIndex+1,modeIndex);
			int selStartIndex = line.indexOf(File.pathSeparator,
				modeIndex+1);
			int selStart = Integer.parseInt(line.substring(modeIndex+1,
				selStartIndex));
			int selEndIndex = line.indexOf(File.pathSeparator,
				selStartIndex+1);
			int selEnd = Integer.parseInt(line.substring(selStartIndex+1,
				selEndIndex));
			boolean current = (line.length() - selEndIndex > 1);
			Buffer buffer = openFile(null,null,path,readOnly,false);
			buffer.setCaretInfo(selStart,selEnd);
			buffer.setMode(getMode(mode));
			if(current)
				return buffer;
		}
		catch(Exception e)
		{
			System.err.println("Error while parsing desktop file:");
			e.printStackTrace();
		}
		return null;
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
				buffer.getAutosaveFile().delete();
			else
				return false;
		}
		return true;
	}

	private static String canonPath(String path)
	{
		try
		{
			return new File(path).getCanonicalPath();
		}
		catch(IOException io)
		{
			return path;
		}
	}

	private static class JarClassLoader extends ClassLoader
	{
		JarClassLoader()
		{
			classes = new Hashtable();
		}
	
		public Class loadClass(String name, boolean resolveIt)
			throws ClassNotFoundException
		{
			Class clazz = findLoadedClass(name);
			if(clazz != null)
				return clazz;
			byte[] data = (byte[])classes.get(name);
			if(data == null)
				return findSystemClass(name);
			Class cls = defineClass(name,data,0,data.length);
			if(resolveIt)
				resolveClass(cls);
			return cls;
		}

		public String loadEntry(ZipFile jar, ZipEntry entry)
			throws IOException
		{
			InputStream in = jar.getInputStream(entry);
			String entryName = entry.getName();
			if(entryName.endsWith(".class"))
			{
				int len = (int)entry.getSize();
				byte[] cls = new byte[len];
				in.read(cls,0,len);
				String clsName = jEdit.fileToClass(entryName);
				classes.put(clsName,cls);
				return clsName;
			}
			else if(entryName.endsWith(".props"))
				jEdit.loadProps(in,entryName);
			in.close();
			return null;
		}

		private Hashtable classes;
	}

	private static class Autosave extends Thread
	{
		public Autosave()
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
				}
				Enumeration enum = jEdit.getBuffers();
				while(enum.hasMoreElements())
					((Buffer)enum.nextElement()).autosave();
			}
		}
	}

	private static class Server extends Thread
	{
		private ServerSocket server;
		private long authInfo;

		public Server()
		{
			super("***jEdit server thread***");
			setDaemon(true);
			start();
		}

		public void run()
		{
			if(portFile.exists())
				return;
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
					final Socket client = server.accept();
					System.out.println("jEdit: connection from "
						+ client.getInetAddress());
					// Paranoid thread safety
					// (We have a nasty DoS here if client
					// opens connecton and never closes it,
					// but it's not too catastrophic since
					// the autosaver continues running)
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							doClient(client);
						}
					});
				}
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
		}

		private void doClient(Socket client)
		{
			View view = null;
			String authString = null;
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
					jEdit.newView(buffer);
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
			portFile.delete();
		}
	}
}
