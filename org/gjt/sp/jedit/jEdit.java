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
import gnu.regexp.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.zip.*;
import java.util.*;
import org.gjt.sp.jedit.event.*;
import org.gjt.sp.jedit.gui.SplashScreen;

/**
 * The main class of the jEdit text editor.
 */
public class jEdit
{
	/**
	 * The jEdit version.
	 */
	public static final String VERSION = "1.5pre2";
	
	/**
	 * The date when a change was last made to the source code,
	 * in <code>YYYYMMDD</code> format.
	 */
	public static final String BUILD = "19990319";

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
					desktop = false;
				else if(arg.equals("-nosplash"))
					showSplash = false;
				else if(arg.startsWith("-usrprops="))
					usrProps = arg.substring(10);
				else if(arg.startsWith("-portfile="))
					portFile = new File(arg.substring(10));
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
		multicaster = new EventMulticaster();

		// Add PROPERTIES_CHANGED listener
		addEditorListener(new JEditEditorListener());
		
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
				jEditHome = userDir;
		}
		jEditHome = jEditHome + File.separator;

		// Load properties
		try
		{
			loadProps(jEdit.class.getResourceAsStream(
				"/org/gjt/sp/jedit/jedit.props"),
				jEditHome + "jedit.props");
			loadProps(jEdit.class.getResourceAsStream(
				"/org/gjt/sp/jedit/jedit_gui.props"),jEditHome
				+ "jedit_gui.props");
			loadProps(jEdit.class.getResourceAsStream(
				"/org/gjt/sp/jedit/jedit_keys.props"),jEditHome
				+ "jedit_keys.props");
			loadProps(jEdit.class.getResourceAsStream(
				"/org/gjt/sp/jedit/jedit_predef.props"),jEditHome
				+ "jedit_predef.props");
			loadProps(jEdit.class.getResourceAsStream(
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
				+ "- jedit_predef.props\n"
				+ "- jedit_tips.props\n"
				+ "Try reinstalling jEdit.");
			return;
		}

		// Load edit modes
		addMode(new org.gjt.sp.jedit.mode.autoindent());
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
		addAction(new org.gjt.sp.jedit.actions.exit());
		addAction(new org.gjt.sp.jedit.actions.expand_abbrev());
		addAction(new org.gjt.sp.jedit.actions.find());
		addAction(new org.gjt.sp.jedit.actions.find_next());
		addAction(new org.gjt.sp.jedit.actions.find_selection());
		addAction(new org.gjt.sp.jedit.actions.format());
		addAction(new org.gjt.sp.jedit.actions.goto_anchor());
		addAction(new org.gjt.sp.jedit.actions.goto_end_indent());
		addAction(new org.gjt.sp.jedit.actions.goto_line());
		addAction(new org.gjt.sp.jedit.actions.goto_marker());
		addAction(new org.gjt.sp.jedit.actions.help());
		addAction(new org.gjt.sp.jedit.actions.hypersearch());
                addAction(new org.gjt.sp.jedit.actions.indent_line());
		addAction(new org.gjt.sp.jedit.actions.insert_date());
		addAction(new org.gjt.sp.jedit.actions.join_lines());
		addAction(new org.gjt.sp.jedit.actions.locate_bracket());
		addAction(new org.gjt.sp.jedit.actions.new_file());
		addAction(new org.gjt.sp.jedit.actions.new_view());
		addAction(new org.gjt.sp.jedit.actions.next_error());
		addAction(new org.gjt.sp.jedit.actions.next_paragraph());
		addAction(new org.gjt.sp.jedit.actions.open_file());
		addAction(new org.gjt.sp.jedit.actions.open_path());
		addAction(new org.gjt.sp.jedit.actions.open_selection());
		addAction(new org.gjt.sp.jedit.actions.open_url());
		addAction(new org.gjt.sp.jedit.actions.options());
		addAction(new org.gjt.sp.jedit.actions.paste());
		addAction(new org.gjt.sp.jedit.actions.paste_predefined());
		addAction(new org.gjt.sp.jedit.actions.paste_previous());
		addAction(new org.gjt.sp.jedit.actions.pipe_selection());
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
		addAction(new org.gjt.sp.jedit.actions.select_line_sep());
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
		addAction(new org.gjt.sp.jedit.actions.toggle_console());
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
		if(splash != null)
			splash.dispose();
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
			autosave.interrupt();
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
			if(EditAction.class.isAssignableFrom(clazz))
				addAction((EditAction)clazz.newInstance());
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
	 * Returns an array of installed plugins.
	 */
	public static Action[] getPlugins()
	{
		Action[] array = new Action[plugins.size()];
		plugins.copyInto(array);
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
		if(name == null)
			return null;
		for(int i = 0; i < modes.size(); i++)
		{
			Mode mode = (Mode)modes.elementAt(i);
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
			path = MiscUtilities.constructPath(parent,path);
			
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
		if(marker != null)
			gotoMarker(buffer,null,marker);
		if(view != null)
			view.setBuffer(buffer);
		buffers.addElement(buffer);
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
		if(!_closeBuffer(view,buffer))
			return false;
		if(buffers.size() == 1)
			exit(view);
		buffers.removeElement(buffer);
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
		view.close();

		fireEditorEvent(new EditorEvent(EditorEvent.VIEW_CLOSED,
			view,view.getBuffer()));

		if(views.size() == 1)
			exit(view);
		else
		{
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
	 * Returns the current regular expression.
	 * @exception REException if the stored regular expression is invalid
	 */
	public static RE getRE()
		throws REException
	{
		String pattern = getProperty("history.find.0");
		if(pattern == null || "".equals(pattern))
			return null;
		return new RE(pattern,("on".equals(getProperty(
			"search.ignoreCase.toggle")) ? RE.REG_ICASE : 0)
			| RE.REG_MULTILINE,getRESyntax(getProperty(
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
	 * Adds a string to the clipboard history.
	 * @param str The string
	 */
	public static void addToClipHistory(String str)
	{
		clipHistory.removeElement(str);
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
		view.close();

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
				Mode mode = buffer.getMode();
				String clazz = (mode == null ? "none"
					: mode.getClass().getName());
				setProperty("desktop." + bufNum + ".mode",
					clazz.substring(clazz.lastIndexOf('.')
					+ 1));
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
			+ " to <sp@gjt.org>.");
		System.exit(0);
	}

	// private members
	private static String jEditHome;
	private static String usrProps;
	private static File portFile;
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
		System.err.println("    -noserver: Don't start server");
		System.err.println("    -nodesktop: Ignore saved desktop");
		System.err.println("    -nosplash: Don't show splash screen");
		System.err.println("    -usrprops=<file>: Read user properties"
			+ " from <file>");
		System.err.println("    -portfile=<file>: Write server port to"
			+ " <file>");
		System.err.println("    -readonly: Open files read-only");
		System.err.println("    -+<line>: Go to line <line>");
		System.err.println();
		System.err.println("Report bugs to Slava Pestov <sp@gjt.org>.");
	}

	private static void version()
	{
		System.err.println("jEdit " + VERSION + " build " + BUILD);
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
				boolean readOnly = getProperty("desktop." + i + ".readOnly")
					.equals("yes");
				boolean current = getProperty("desktop." + i + ".current")
					.equals("yes");
				int selStart = Integer.parseInt(getProperty(
					"desktop." + i + ".selStart"));
				int selEnd = Integer.parseInt(getProperty(
					"desktop." + i + ".selEnd"));
				Buffer buffer = openFile(null,null,path,readOnly,
					false);
				buffer.setCaretInfo(selStart,selEnd);
				buffer.setMode(getMode(mode));
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
				buffer.getAutosaveFile().delete();
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

	private static class JEditEditorListener extends EditorAdapter
	{
		public void propertiesChanged(EditorEvent evt)
		{
			jEdit.propertiesChanged();
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
				int success = 0;
				int offset = 0;
				String clsName = MiscUtilities.fileToClass(entryName);
				while (success < len) {
					len -= success;
					offset += success;
					success = in.read(cls,offset,len);
					if (success == -1)
					{
						System.err.println("Error loading class "
							+ clsName + " from "
							+ jar.getName());
						return null;
					}
				}
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
				Buffer[] bufferArray = jEdit.getBuffers();
				for(int i = 0; i < bufferArray.length; i++)
					bufferArray[i].autosave();
				if(interrupted())
					return;
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
			portFile.delete();
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
 * Revision 1.53  1999/03/19 06:03:34  sp
 * Fixed history text field bug, some other small changes maybe
 *
 * Revision 1.52  1999/03/18 04:24:57  sp
 * HistoryTextField hacking, some other minor changes
 *
 * Revision 1.51  1999/03/17 05:32:51  sp
 * Event system bug fix, history text field updates (but it still doesn't work), code cleanups, lots of banging head against wall
 *
 * Revision 1.50  1999/03/16 04:34:45  sp
 * HistoryTextField updates, moved generate-text to a plugin, fixed spelling mistake in EditAction Javadocs
 *
 * Revision 1.49  1999/03/15 03:40:23  sp
 * Search and replace updates, TSQL mode/token marker updates
 *
 * Revision 1.48  1999/03/15 03:12:34  sp
 * Fixed compile error with javac that jikes silently ignored (FUCK YOU IBM),
 * maybe some other stuff fixed too
 *
 * Revision 1.47  1999/03/14 02:22:13  sp
 * Syntax colorizing tweaks, server bug fix
 *
 * Revision 1.46  1999/03/14 00:08:07  sp
 * Build number updated
 *
 * Revision 1.45  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.44  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
