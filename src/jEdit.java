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

import com.sun.java.swing.*;
import gnu.regexp.*;
import java.awt.event.*;
import java.io.File;
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
	public static final String VERSION = "1.1.2";
	
	/**
	 * The jEdit build.
	 * <p>
	 * This is the date when a change was last made to the source code,
	 * in <code>YYYYMMDD</code> format.
	 */
	public static final String BUILD = "19981116";
	
	/**
	 * The property manager.
	 * <p>
	 * The property manager can be used to set and fetch properties and
	 * load and save property files.
	 * @see PropsMgr#loadProps(InputStream,String,String)
	 * @see PropsMgr#getProperty(String,Object[])
	 * @see PropsMgr#saveProps(OutputStream,String)
	 */
	public static final PropsMgr props = new PropsMgr();

	/**
	 * The command manager.
	 * <p>
	 * The command manager can be used to fetch commands and modes and
	 * to load plugins.
	 * @see CommandMgr#loadPlugins(String)
	 * @see CommandMgr#loadPlugin(String)
	 * @see CommandMgr#getCommand(String)
	 * @see CommandMgr#execCommand(Buffer,View,String)
	 * @see CommandMgr#getPlugins()
	 * @see CommandMgr#getMode(String)
	 * @see CommandMgr#getModeName(Mode)
	 * @see CommandMgr#getModes()
	 * @see CommandMgr#addHook(String,Object)
	 * @see CommandMgr#removeHook(String,Object)
	 * @see CommandMgr#removeHook(String)
	 * @see CommandMgr#execHook(Buffer,View,String)
	 * @see Command
	 * @see Mode
	 */
	public static final CommandMgr cmds = new CommandMgr();
	
	/**
	 * The buffer manager.
	 * <p>
	 * The buffer manager can be used to open and close views and buffers.
	 * @see BufferMgr#openURL(View)
	 * @see BufferMgr#openFile(View)
	 * @see BufferMgr#openFile(View,String,String,boolean,boolean)
	 * @see BufferMgr#newFile(View)
	 * @see BufferMgr#closeBuffer(View,Buffer)
	 * @see BufferMgr#getBuffer(String)
	 * @see BufferMgr#getBuffers()
	 * @see BufferMgr#newView(View)
	 * @see BufferMgr#closeView(View)
	 * @see BufferMgr#getViews()
	 * @see BufferMgr#getRecent()
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
		String jeditHome = System.getProperty("jedit.home",
			System.getProperty("user.dir"));
		System.getProperties().put("jedit.home",jeditHome);
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
		props.loadSystemProps();
		cmds.loadPlugins(jeditHome + File.separator + "jars");
		cmds.loadPlugins(System.getProperty("user.home") +
			File.separator + ".jedit-jars");
		if(!noUsrProps)
			props.loadUserProps();	
		propertiesChanged();
		buffers.newFile(null);
		String userDir = System.getProperty("user.dir");
		View view = buffers.newView(null);
		for(int i = 0; i < args.length; i++)
		{
			if(args[i] == null)
				continue;
			buffers.openFile(view,userDir,args[i],readOnly,false);
		}
		cmds.execHook(null,null,"post_startup");
	}

	/**
	 * Reloads the look and feel, server, auto indent and recent files
	 * settings from the properties.
	 * <p>
	 * This should be called after any of these properties have been
	 * changed:
	 * <ul>
	 * <li><code>lf</code>
	 * <li><code>daemon.server.toggle</code>
	 * <li><code>daemon.autosave.interval</code>
	 * <li><code>buffermgr.recent</code>
	 * </ul>
	 * @see PropsMgr
	 * @see View#propertiesChanged()
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
	 * Converts all tabs in the specified string to spaces.
	 * @param tabSize The tab size
	 * @param in The input string
	 */
	public static String untab(int tabSize, String in)
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < in.length(); i++)
		{
			switch(in.charAt(i))
			{
			case '\t':
				int count = tabSize - (i % tabSize);
				while(count-- >= 0)
					buf.append(' ');
				break;
			default:
				buf.append(in.charAt(i));
				break;
			}
		}
		return buf.toString();
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
		for(int i = clsName.length - 1; i >= 5; i--)
			if(clsName[i] == '/')
				clsName[i] = '.';
		return new String(clsName,0,clsName.length - 6);
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
			+ " to <slava_pestov@geocities.com>.");
		System.exit(0);
	}

	// private members
	private static File portFile;
	private static Server server;
	private static Autosave autosave;

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
