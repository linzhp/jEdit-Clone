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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Vector;
import com.sun.java.swing.JButton;
import com.sun.java.swing.JMenu;
import com.sun.java.swing.JMenuBar;
import com.sun.java.swing.JMenuItem;
import com.sun.java.swing.JOptionPane;
import com.sun.java.swing.JSeparator;
import com.sun.java.swing.JToolBar;
import com.sun.java.swing.KeyStroke;
import com.sun.java.swing.UIManager;

public class jEdit
{
	public static final String VERSION = "0.9";
	public static final String BUILD = "19981008";
	public static final PropsMgr props = new PropsMgr();
	public static final CommandMgr cmds = new CommandMgr();
	public static final BufferMgr buffers = new BufferMgr();
	private static File portFile;
	private static Server server;
	private static Autosave autosave;
	
	public static void usage()
	{
		System.err.println("Usage: jedit [<options>] [<files>]");
		System.err.println("Valid options:");
		System.err.println("    --: End of options");
		System.err.println("    -version: Print jEdit version and"
			+ " exit");
		System.err.println("    -usage: Print this message and exit");
		System.err.println("    -nousrprops: Don't load user"
			+ " properties");
		System.err.println("    -portfile=<file>: Write server port to"
			+ " <file>");
		System.err.println("    -readonly: Open files read-only");
		System.exit(1);
	}

	public static void version()
	{
		System.err.println("jEdit " + VERSION + " build " + BUILD);
		System.exit(1);
	}

	public static void main(String[] args)
	{
		boolean endOpts = false;
		boolean noUsrProps = false;
		boolean readOnly = false;
		portFile = new File(System.getProperty("user.home"),
			props.getProperty("server.portfile",".jedit-server"));
		String jeditHome = System.getProperty("jedit.home",".");
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
		cmds.loadPlugins(jeditHome + File.separator + "jars");
		cmds.loadPlugins(System.getProperty("user.home") +
			File.separator + ".jedit-jars");
		props.setDefault("helpdir",jeditHome + File.separator + "doc");
		props.loadSystemProps();
		if(!noUsrProps)
			props.loadUserProps();	
		propertiesChanged();
		buffers.openFiles(args,readOnly);
	}

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
			server.stopServer();
		if(autosave != null)
			autosave.stop();
		if("on".equals(props.getProperty("server")))
			server = new Server(portFile);
		autosave = new Autosave();
		buffers.loadRecent();
	}
	
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

	public static JMenu loadMenu(View view, String name)
	{
		return loadMenu(view,name,null);
	}
	
	public static JMenu loadMenu(View view, String name, Vector vector)
	{
		JMenu menu = view.getDynamicMenu(name);
		if(menu != null)
			return menu;
		if(vector == null)
			vector = new Vector();
		else if(vector.contains(name))
			throw new IllegalArgumentException("Aiee!!! Recursive"
				+ " menu definition");
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

	public static JMenuItem loadMenuItem(View view, String name)
	{
		return loadMenuItem(view,name,new Vector());
	}
	
	public static JMenuItem loadMenuItem(View view, String name,
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
			throw new IllegalArgumentException("Aiee!!! invalid"
				+ " keystroke");
		else
		{
			try
			{
				ch = KeyEvent.class.getField("VK_".concat(key))
					.getInt(null);
			}
			catch(Exception e)
			{
				throw new IllegalArgumentException("Aiee!!!"
					+ " unknown keystroke name");
			}
		}		
		return KeyStroke.getKeyStroke(ch,modifiers);
	}
		
	public static void message(View view, String name, Object[] args)
	{
		JOptionPane.showMessageDialog(view,
			props.getProperty(name.concat(".message"),args),
			props.getProperty(name.concat(".title"),args),
			JOptionPane.INFORMATION_MESSAGE);
	}

	public static void error(View view, String name, Object[] args)
	{
		JOptionPane.showMessageDialog(view,
			props.getProperty(name.concat(".message"),args),
			props.getProperty(name.concat(".title"),args),
			JOptionPane.ERROR_MESSAGE);
	}

	// We really need to use a better algorithm
	public static int find(char[] pattern, char[] line, int start)
	{
		int length = line.length - (pattern.length - 1);
		for(int i = start; i < length; i++)
		{
			boolean matches = false;
			for(int j = 0; j < pattern.length; j++)
			{
				if(i + j > line.length)
				{
					matches = false;
					break;
				}
				if(pattern[j] == line[i + j])
					matches = true;
				else
				{
					matches = false;
					break;
				}
			}
			if(matches)
				return i;
		}
		return -1;
	}

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
		System.exit(0);
	}
}
