/*
 * jEdit.java - Main class of the jEdit editor
 * Copyright (C) 1998 Slava Pestov
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
import java.net.Socket;
import java.util.*;

/**
 * The main class of the jEdit text editor.
 * <p>
 * This class contains three important objects:
 * <ul>
 * <li><code>props</code> - The property manager
 * <li><code>cmds</code> - The command manager
 * <li><code>buffers</code> - The buffer manager
 * </ul>
 * @see PropsMgr
 * @see CommandMgr
 * @see BufferMgr
 * @see Command
 * @see Mode
 * @see Buffer
 * @see View
 */
public class jEdit
{
	// public members

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
	 * The regexp syntax list.
	 * <p>
	 * The values in this list can be stored in the
	 * <code>search.regexp.value</code> property to change the regexp
	 * syntax.
	 */
	public static final String[] SYNTAX_LIST = { AWK, ED, EGREP, EMACS,
		GREP, PERL4, PERL5, SED };

	/**
	 * The jEdit version.
	 */
	public static final String VERSION = "1.2final";
	
	/**
	 * The jEdit build.
	 * <p>
	 * This is the date when a change was last made to the source code,
	 * in <code>YYYYMMDD</code> format.
	 */
	public static final String BUILD = "19981213";
	
	/**
	 * The property manager.
	 */
	public static final PropsMgr props = new PropsMgr();

	/**
	 * The command manager.
	 * <p>
	 * The command manager can be used to fetch commands and modes and
	 * to load plugins.
	 */
	public static final CommandMgr cmds = new CommandMgr();
	
	/**
	 * The buffer manager.
	 */
	public static final BufferMgr buffers = new BufferMgr();
	
	/**
	 * The main method of the jEdit application.
	 * <p>
	 * This should never be invoked directly.
	 * @param args The command line arguments
	 */
	public static void main(String[] args)
	{
		boolean endOpts = false;
		boolean noUsrProps = false;
		boolean readOnly = false;
		portFile = new File(System.getProperty("user.home"),
			".jedit-server");
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
				jEditHome = System.getProperty("user.dir");
		}
		for(int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			if(arg.startsWith("-") && !endOpts)
			{
				if(arg.equals("--"))
					endOpts = true;
				else if(arg.equals("-usage"))
					usage();
				else if(arg.equals("-version"))
					version();
				else if(arg.equals("-nousrprops"))
					noUsrProps = true;
				else if(arg.equals("-noserver"))
					portFile = null;
				else if(arg.startsWith("-portfile="))
					portFile = new File(arg.substring(10));
				else if(arg.equals("-readonly"))
					readOnly = true;
				else
				{
					System.err.println("Unknown option: "
						+ arg);
					usage();
				}
				args[i] = null;
			}
		}
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
				out.writeBytes("-cwd=" + System
					.getProperty("user.dir") + "\n");
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
				System.exit(0);
				return;
			}
			catch(Exception e)
			{
				System.out.println("Stale port file deleted");
				portFile.delete();
			}
		}
		props.loadSystemProps();
		try
		{
			cmds.initMode("autoindent");
			cmds.initMode("bat");
			cmds.initMode("c");
			cmds.initMode("html");
			cmds.initMode("java_mode");
			cmds.initMode("makefile");
			cmds.initMode("sh");
			cmds.initMode("tex");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			jEdit.error(null,"loadmodeerr",null);
		}
		cmds.loadPlugins(jEditHome + File.separator + "jars");
		cmds.loadPlugins(System.getProperty("user.home") +
			File.separator + ".jedit-jars");
		if(!noUsrProps)
			props.loadUserProps();	
		propertiesChanged();
		String userDir = System.getProperty("user.dir");
		boolean opened = false;
		Buffer buffer = null;
		for(int i = 0; i < args.length; i++)
		{
			if(args[i] == null)
				continue;
			opened = true;
			buffer = buffers.openFile(null,userDir,args[i],
				readOnly,false);
		}
		if(!opened)
			buffer = buffers.newFile(null);
		try
		{
			cmds.execHook(buffer,buffers.newView(buffer),
				"post_startup");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Object[] _args = { e.toString() };
			jEdit.error(null,"execcmderr",_args);
		}
	}

	/**
	 * Reloads the look and feel, server, auto indent and recent files
	 * settings from the properties.
	 * <p>
	 * This should be called after any of these properties have been
	 * changed:
	 * <ul>
	 * <li><code>lf</code>
	 * <li><code>view.font</code>
	 * <li><code>view.fontsize</code>
	 * <li><code>view.fontstyle</code>
	 * <li><code>view.autoindent</code>
	 * <li><code>buffer.syntax</code>
	 * <li><code>daemon.server.toggle</code>
	 * <li><code>daemon.autosave.interval</code>
	 * <li><code>buffermgr.recent</code>
	 * </ul>
	 * @see PropsMgr
	 */
	public static void propertiesChanged()
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
		if(server != null)
		{
			server.stopServer();
			server = null;
		}
		if(autosave != null)
			autosave.stop();
		if("on".equals(props.getProperty("daemon.server.toggle"))
			&& portFile != null)
			server = new Server(portFile);
			
		String family = jEdit.props.getProperty("view.font",
			"Monospaced");
		int size, style;
		try
		{
			size = Integer.parseInt(jEdit.props
				.getProperty("view.fontsize"));
		}
		catch(NumberFormatException nf)
		{
			size = 12;
		}
		try
		{
			style = Integer.parseInt(jEdit.props
				.getProperty("view.fontstyle"));
		}
		catch(NumberFormatException nf)
		{
			style = 0;
		}
		font = new Font(family,style,size);

		autoindent = "on".equals(jEdit.props.getProperty(
			"view.autoindent"));
		syntax = "on".equals(jEdit.props.getProperty("buffer.syntax"));
		autosave = new Autosave();
		buffers.loadRecent();
	}
	
	/**
	 * Loads a menubar from the properties for the specified view.
	 * <p>
	 * The menubar format is described in menus.txt. (Help-&gt;Menus
	 * in jEdit).
	 * @param view The view to load the menubar for
	 * @param name The property with the list of menus
	 * @see #loadMenu(View,String)
	 * @see #loadMenuItem(View,String)
	 * @see #parseKeyStroke(String)
	 */
	public static JMenuBar loadMenubar(View view, String name)
	{
		if(name == null)
			return null;
		Vector vector = new Vector();
		JMenuBar mbar = new JMenuBar();
		String menus = props.getProperty(name);
		if(menus != null)
		{
			StringTokenizer st = new StringTokenizer(menus);
			while(st.hasMoreTokens())
			{
				JMenu menu = loadMenu(view,st.nextToken(),
					vector);
				vector.removeAllElements();
				if(menu != null)
					mbar.add(menu);
			}
		}
		return mbar;
	}

	/**
	 * Loads a menu from the properties for the specified view.
	 * <p>
	 * The menubar format is described in menus.txt. (Help-&gt;Menus
	 * in jEdit).
	 * @param view The view to load the menu for
	 * @param name The menu name
	 * @see #loadMenubar(View,String)
	 * @see #loadMenuItem(View,String)
	 * @see #parseKeyStroke(String)
	 */
	public static JMenu loadMenu(View view, String name)
	{
		return loadMenu(view,name,null);
	}
	
	/**
	 * Loads a menu item from the properties for the specified view.
	 * <p>
	 * The menubar format is described in menus.txt. (Help-&gt;Menus
	 * in jEdit).
	 * @param view The view to load the menu item for
	 * @param name The menu item name
	 * @see #loadMenubar(View,String)
	 * @see #loadMenu(View,String)
	 * @see #parseKeyStroke(String)
	 */
	public static JMenuItem loadMenuItem(View view, String name)
	{
		return loadMenuItem(view,name,new Vector());
	}
	
	/**
	 * Converts a string to a keystroke.
	 * <p>
	 * The keystroke format is described in menus.txt. (Help-&gt;Menus
	 * in jEdit).
	 * @param keyStroke A string description of the key stroke
	 * @see #loadMenubar(View,String)
	 * @see #loadMenu(View,String)
	 * @see #loadMenuItem(View,String)
	 */
	public static KeyStroke parseKeyStroke(String keyStroke)
	{
		if(keyStroke == null)
			return null;
		int modifiers = 0;
		int ch = '\0';
		int index = keyStroke.indexOf('-');
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
			Object[] args = { keyStroke };
			error(null,"invkeystroke",args);
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
				Object[] args = { keyStroke };
				error(null,"invkeystroke",args);
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
	 * @see PropsMgr#getProperty(String,Object[])
	 */
	public static void message(View view, String name, Object[] args)
	{
		JOptionPane.showMessageDialog(view,
			props.getProperty(name.concat(".message"),args),
			props.getProperty(name.concat(".title"),args),
			JOptionPane.INFORMATION_MESSAGE);
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
	 * @see PropsMgr#getProperty(String,Object[])
	 */
	public static void error(View view, String name, Object[] args)
	{
		JOptionPane.showMessageDialog(view,
			props.getProperty(name.concat(".message"),args),
			props.getProperty(name.concat(".title"),args),
			JOptionPane.ERROR_MESSAGE);
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
	 * @see PropsMgr#getProperty(String,Object[])
	 */
	public static String input(View view, String name, String def)
	{
		String retVal = (String)JOptionPane.showInputDialog(view,
			jEdit.props.getProperty(name.concat(".message")),
			jEdit.props.getProperty(name.concat(".title")),
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			jEdit.props.getProperty(def));
		if(retVal != null)
			jEdit.props.put(def,retVal);
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
		String pattern = jEdit.props.getProperty("search.find.value");
		if(pattern == null || "".equals(pattern))
			return null;
		return new RE(pattern,"on".equals(jEdit.props.getProperty(
			"search.ignoreCase.toggle")) ? RE.REG_ICASE : 0,
			jEdit.getRESyntax(jEdit.props.getProperty(
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
	 * Returns the default font.
	 */
	public static Font getFont()
	{
		return font;
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
		char[] textArray = text.array;
		int length = offset + Math.min(match.length(),text.count);
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
				return new Color(Integer.parseInt(
					name.substring(1),16));
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
			return path;
		// windows pathnames, eg C:\document
		else if(path.length() >= 3 && path.charAt(1) == ':'
			&& path.charAt(2) == '\\')
			return path;
		// relative pathnames
		else if(parent == null)
			parent = System.getProperty("user.dir");
		// do it!
		if(parent.endsWith(File.separator))
			return parent + path;
		else
			return parent + File.separator + path;
	}

	/**
	 * Exits cleanly from jEdit, prompting the user if any unsaved files
	 * should be saved first.
	 * @param view The view from which this exit was called
	 */
	public static void exit(View view)
	{
		if(!buffers.closeAll(view))
			return;
		if(server != null)
		{
			server.stopServer();
			server = null;
		}
		buffers.saveRecent();
		props.saveUserProps();
		System.out.println("Thank you for using jEdit. Send an e-mail"
			+ " to <sp@gjt.org>.");
		System.exit(0);
	}

	// private members
	private static File portFile;
	private static Font font;
	private static boolean autoindent;
	private static boolean syntax;
	private static Server server;
	private static Autosave autosave;
	private static String jEditHome;

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
		System.err.println("    -portfile=<file>: Write server port to"
			+ " <file>");
		System.err.println("    -readonly: Open files read-only");
		System.exit(2);
	}

	private static void version()
	{
		System.err.println("jEdit " + VERSION + " build " + BUILD);
		System.exit(2);
	}

	private static JMenu loadMenu(View view, String name, Vector vector)
	{
		if(name == null)
			return null;
		JMenu menu = view.getMenu(name);
		if(menu != null)
			return menu;
		if(vector == null)
			vector = new Vector();
		else if(vector.contains(name))
		{
			vector.addElement(name);
			Object[] args = { vector.toString() };
			error(view,"recursmenu",args);
			return null;
		}
		vector.addElement(name);
		menu = new JMenu(props.getProperty(name.concat(".label")));
		String menuItems = props.getProperty(name);
		if(menuItems != null)
		{
			StringTokenizer st = new StringTokenizer(menuItems);
			while(st.hasMoreTokens())
			{
				String menuItemName = st.nextToken();
				if(menuItemName.equals("-"))
					menu.add(new JSeparator());
				else
				{
					JMenuItem menuItem = loadMenuItem(view,
						menuItemName,vector);
					if(menuItem != null)
						menu.add(menuItem);
				}
			}
		}
		return menu;
	}

	private static JMenuItem loadMenuItem(View view, String name,
		Vector vector)
	{
		if(name == null)
			return null;
		if(name.startsWith("%"))
			return loadMenu(view,name.substring(1),vector);
		String label = props.getProperty(name.concat(".label"));
		if(label == null)
			return null;
		KeyStroke keyStroke = parseKeyStroke(props.getProperty(name
			.concat(".shortcut")));
		JMenuItem menuItem = new JMenuItem(label);
		if(keyStroke != null)
			menuItem.setAccelerator(keyStroke);
		menuItem.setActionCommand(name);
		if(view != null)
			menuItem.addActionListener(view);
		return menuItem;
	}
}
