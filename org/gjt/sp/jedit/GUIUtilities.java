/*
 * GUIUtilities.java - Various GUI utility functions
 * Copyright (C) 1999 Slava Pestov
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.StringTokenizer;
import org.gjt.sp.jedit.gui.*;

/**
 * Class with several useful GUI functions.<p>
 *
 * It provides methods for:
 * <ul>
 * <li>Loading menu bars, menus and menu items from the properties
 * <li>Loading popup menus from the properties
 * <li>Loading tool bars and tool bar buttons from the properties
 * <li>Displaying various common dialog boxes
 * <li>Converting string representations of colors to color objects
 * <li>Loading and saving window geometry from the properties
 * </ul>
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class GUIUtilities
{
	/**
	 * Loads a menubar from the properties for the specified view.
	 * @param view The view to load the menubar for
	 * @param name The property with the white space separated
	 * list of menus
	 */
	public static JMenuBar loadMenubar(View view, String name)
	{
		if(name == null)
			return null;
		JMenuBar mbar = new JMenuBar();
		String menus = jEdit.getProperty(name);
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
	 * The white space separated list of menu items is obtained from
	 * the property named <code><i>name</i></code>. The menu label is
	 * obtained from the <code><i>name</i>.label</code> property.
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
			String label = jEdit.getProperty(name + ".label");
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
		String menuItems = jEdit.getProperty(name);
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
	 * Loads a popup menu from the properties for the specified view.
	 * @param view The view to load the popup menu for
	 * @param name The property with the white space separated list of
	 * menu items
	 */
	public static JPopupMenu loadPopupMenu(View view, String name)
	{
		JPopupMenu menu = new JPopupMenu();
		menu.setInvoker(view);
		String menuItems = jEdit.getProperty(name);
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
	 * The menu label is obtained from the <code><i>name</i>.label</code>
	 * property. The keyboard shortcut is obtained from the
	 * <code><i>name</i>.shortcut</code> property.
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
                String label = jEdit.getProperty(name.concat(".label"));
		String keyStroke = jEdit.getProperty(name.concat(".shortcut"));
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
		
		EditAction a = jEdit.getAction(action);
		if(a == null)
			mi.setEnabled(false);
		else
			mi.addActionListener(a);
		mi.setActionCommand(arg);
		return mi;
	}

	/**
	 * Converts a string to a keystroke. The string should be of the
	 * form <i>modifiers</i>+<i>shortcut</i> where <i>modifiers</i>
	 * is any combination of A for Alt, C for Control, S for Shift
	 * or M for Meta, and <i>shortcut</i> is either a single character,
	 * or a keycode name from the <code>KeyEvent</code> class, without
	 * the <code>VK_</code> prefix.
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
	 * Loads a toolbar from the properties.
	 * @param name The property with the white space separated list
	 * of tool bar buttons
	 */
	public static JToolBar loadToolBar(String name)
	{
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);

		String buttons = jEdit.getProperty(name);
		if(buttons == null)
			return null;

		StringTokenizer st = new StringTokenizer(buttons);
		while(st.hasMoreElements())
		{
			String buttonName = st.nextToken();
			if(buttonName.equals("-"))
				toolBar.addSeparator();
			else
			{
				JButton button = loadToolButton(buttonName);
				if(button != null)
					toolBar.add(button);
			}
		}

		return toolBar;
	}

	/**
	 * Loads a tool bar button. The tooltip is constructed from
	 * the <code><i>name</i>.label</code> and
	 * <code><i>name</i>.shortcut</code> properties and the icon is loaded
	 * from the resource named '/org/gjt/sp/jedit/toolbar/' suffixed
	 * with the value of the <code><i>name</i>.icon</code> property.
	 * @param name The name of the button
	 */
	public static JButton loadToolButton(String name)
	{
		String iconName = jEdit.getProperty(name + ".icon");
		if(iconName == null)
		{
			System.out.println("Tool button icon is null: " + name);
			return null;
		}
		URL url = GUIUtilities.class.getResource("toolbar/" + iconName);
		if(url == null)
		{
			System.out.println("Tool button icon is null: " + name);
			return null;
		}
		JButton button = new JButton(new ImageIcon(url));
		button.setRequestFocusEnabled(false);

		String toolTip = jEdit.getProperty(name + ".label");
		if(toolTip == null)
		{
			System.out.println("Tool button label is null: " + name);
			return null;
		}
		int index = toolTip.indexOf('$');
		if(index != -1)
		{
			toolTip = toolTip.substring(0,index)
				.concat(toolTip.substring(index + 1));
		}
		if(toolTip.endsWith("..."))
			toolTip = toolTip.substring(0,toolTip.length() - 3);
		String shortcut = jEdit.getProperty(name + ".shortcut");
		if(shortcut != null)
			toolTip = toolTip + " (" + shortcut + ")";
		button.setToolTipText(toolTip);

		index = name.indexOf('@');
		String actionCommand;
		if(index != -1)
		{
			actionCommand = name.substring(index + 1);
			name = name.substring(0,index);
		}
		else
			actionCommand = null;
		EditAction action = jEdit.getAction(name);
		if(action == null)
			button.setEnabled(false);
		else
		{
			button.addActionListener(action);
			button.setActionCommand(actionCommand);
		}
		return button;
	}

	/**
	 * Displays a dialog box.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property. The message
	 * is formatted by the property manager with <code>args</code> as
	 * positional parameters.
	 * @param frame The frame to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 */
	public static void message(JFrame frame, String name, Object[] args)
	{
		JOptionPane.showMessageDialog(frame,
			jEdit.getProperty(name.concat(".message"),args),
			jEdit.getProperty(name.concat(".title"),args),
			JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Displays an error dialog box.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property. The message
	 * is formatted by the property manager with <code>args</code> as
	 * positional parameters.
	 * @param frame The frame to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 */
	public static void error(JFrame frame, String name, Object[] args)
	{
		JOptionPane.showMessageDialog(frame,
			jEdit.getProperty(name.concat(".message"),args),
			jEdit.getProperty(name.concat(".title"),args),
			JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays an input dialog box and returns any text the user entered.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property.
	 * @param frame The frame to display the dialog for
	 * @param name The name of the dialog
	 * @param def The text to display by default in the input field
	 */
	public static String input(JFrame frame, String name, Object def)
	{
		String retVal = (String)JOptionPane.showInputDialog(frame,
			jEdit.getProperty(name.concat(".message")),
			jEdit.getProperty(name.concat(".title")),
			JOptionPane.QUESTION_MESSAGE,null,null,def);
		return retVal;
	}

	/**
	 * Displays an input dialog box and returns any text the user entered.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property.
	 * @param frame The frame to display the dialog for
	 * @param name The name of the dialog
	 * @param def The property whose text to display in the input field
	 */
	public static String inputProperty(JFrame frame, String name, String def)
	{
		String retVal = (String)JOptionPane.showInputDialog(frame,
			jEdit.getProperty(name.concat(".message")),
			jEdit.getProperty(name.concat(".title")),
			JOptionPane.QUESTION_MESSAGE,
			null,null,jEdit.getProperty(def));
		if(retVal != null)
			jEdit.setProperty(def,retVal);
		return retVal;
	}

	/**
	 * Converts a color name to a color object. The name must either be
	 * a known string, such as `red', `green', etc (complete list is in
	 * the <code>java.awt.Color</code> class) or a hex color value
	 * prefixed with `#', for example `#ff0088'.
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
	 * Converts a color object to its hex value. The hex value
	 * prefixed is with `#', for example `#ff0088'.
	 * @param c The color object
	 */
	public static String getColorHexString(Color c)
	{
		String colString = Integer.toHexString(c.getRGB() & 0xffffff);
		return "#000000".substring(0,7 - colString.length()).concat(colString);
	}


	/**
	 * Loads a windows's geometry from the properties.
	 * The geometry is loaded from the <code><i>name</i>.x</code>,
	 * <code><i>name</i>.y</code>, <code><i>name</i>.width</code> and
	 * <code><i>name</i>.height</code> properties.
	 *
	 * @param win The window
	 * @param name The window name
	 */
	public static void loadGeometry(Window win, String name)
	{
		int x, y, width, height;

		try
		{
			width = Integer.parseInt(jEdit.getProperty(name + ".width"));
			height = Integer.parseInt(jEdit.getProperty(name + ".height"));
		}
		catch(NumberFormatException nf)
		{
			Dimension size = win.getSize();
			width = size.width;
			height = size.height;
		}

		try
		{
			x = Integer.parseInt(jEdit.getProperty(name + ".x"));
			y = Integer.parseInt(jEdit.getProperty(name + ".y"));
		}
		catch(NumberFormatException nf)
		{
			Dimension screen = win.getToolkit().getScreenSize();
			x = (screen.width - width) / 2;
			y = (screen.height - height) / 2;
		}

		win.setLocation(x,y);
		win.setSize(width,height);
	}

	/**
	 * Saves a window's geometry to the properties.
	 * The geometry is saved to the <code><i>name</i>.x</code>,
	 * <code><i>name</i>.y</code>, <code><i>name</i>.width</code> and
	 * <code><i>name</i>.height</code> properties.
	 * @param win The window
	 * @param name The window name
	 */
	public static void saveGeometry(Window win, String name)
	{
		Point location = win.getLocation();
		Dimension size = win.getSize();
		jEdit.setProperty(name + ".x",String.valueOf(location.x));
		jEdit.setProperty(name + ".y",String.valueOf(location.y));
		jEdit.setProperty(name + ".width",String.valueOf(size.width));
		jEdit.setProperty(name + ".height",String.valueOf(size.height));

	}

	// private members
	private GUIUtilities() {}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.16  1999/04/24 01:55:28  sp
 * MiscUtilities.constructPath() bug fixed, event system bug(s) fix
 *
 * Revision 1.15  1999/03/26 04:14:45  sp
 * EnhancedMenuItem tinkering, fixed compile error, fixed backup bug
 *
 * Revision 1.14  1999/03/21 08:37:15  sp
 * Slimmer action system, history text field update
 *
 * Revision 1.13  1999/03/20 01:55:42  sp
 * New color option pane, fixed search & replace bug
 *
 * Revision 1.12  1999/03/20 00:26:48  sp
 * Console fix, backed out new JOptionPane code, updated tips
 *
 * Revision 1.11  1999/03/19 07:12:10  sp
 * JOptionPane changes, did a fromdos of the source
 *
 * Revision 1.10  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 * Revision 1.9  1999/03/12 07:54:47  sp
 * More Javadoc updates
 *
 */
