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
	public static final String VERSION = "0.2";
	public static final String BUILD = "19980928";
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
		System.err.println("    -version: Print jEdit version");
		System.err.println("    -usage: Print this message");
		System.err.println("    -nousrprops: Don't load user"
			+ " properties");
		System.err.println("    -helpdir=<path>: Documentation"
			+ " directory");
		System.err.println("    -plugindir=<path>: Plugin search"
			+ " path");
		System.err.println("    -server=<file>: Write server port to"
			+ " <file>");
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
		portFile = new File(System.getProperty("user.home"),
			props.getProperty("server.portfile",".jedit-server"));
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
				else if(arg.startsWith("-helpdir="))
					props.setDefault("helpdir",arg
						.substring(9)
						+ File.separator);
				else if(arg.startsWith("-plugindir="))
					cmds.loadPlugins(arg.substring(11));
				else if(arg.startsWith("-server="))
					portFile = new File(arg.substring(8));
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
		if(!noUsrProps)
			props.loadUserProps();	
		propertiesChanged();
		buffers.openBuffers(args);
	}

	public static void propertiesChanged()
	{
		String lf = props.getProperty("lf","xp");
		try
		{
			if(lf.equals("xp"))
				UIManager.setLookAndFeel(UIManager
				.getCrossPlatformLookAndFeelClassName());
			else if(lf.equals("system"))
				UIManager.setLookAndFeel(UIManager
				.getSystemLookAndFeelClassName());
			else
				UIManager.setLookAndFeel(lf);
		}
		catch(Exception ex)
		{
			System.err.println("Error loading L&F!");
			ex.printStackTrace();
		}
		if(server != null)
			server.stopServer();
		if(autosave != null)
			autosave.stop();
		if("on".equals(props.getProperty("server")))
			server = new Server(portFile);
		if("on".equals(props.getProperty("autosave")))
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
	
	public static void exit(View view)
	{
		if(server != null)
		{
			server.stopServer();
			server = null;
		}
		if(!buffers.closeAll(view))
			return;
		buffers.saveRecent();
		props.saveUserProps();
		System.exit(0);
	}
}
