/*
 * View.java - jEdit view
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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.gjt.sp.jedit.syntax.SyntaxTextArea;

/**
 * A <code>View</code> edits buffers. There is no public constructor in the
 * View class. Views are created and destroyed by the <code>BufferMgr</code>
 * class.
 * @see Buffer
 */
public class View extends JFrame
implements CaretListener, KeyListener, WindowListener
{	
	/**
	 * Reloads the font, auto indent and word wrap settings
	 * from the properties.
	 * <p>
	 * This should be called after any of these properties have been
	 * changed:
	 * <ul>
	 * <li><code>lf</code>
	 * <li><code>view.linewrap</code>
	 * <li><code>buffermgr.recent</code>
	 * @see PropsMgr
	 */
	public void propertiesChanged()
	{
		Font font = jEdit.getFont();
		textArea.setFont(font);
		status.setFont(font);
		updateOpenRecentMenu();
		SwingUtilities.updateComponentTreeUI(this);
	}
	
	/**
	 * Recreates the plugins menu.
	 */
	public void updatePluginsMenu()
	{
		if(plugins.getMenuComponentCount() != 0)
			buffers.removeAll();
		Enumeration enum = jEdit.getPlugins();
		if(!enum.hasMoreElements())
		{
			plugins.add(jEdit.loadMenuItem(this,"no-plugins"));
			return;
		}
		while(enum.hasMoreElements())
		{
			String action = (String)((Action)enum.nextElement())
				.getValue(Action.NAME);
			JMenuItem mi = jEdit.loadMenuItem(this,action);
			plugins.add(mi);
		}
	}

	/**
	 * Recreates the buffers menu.
	 */
	public void updateBuffersMenu()
	{
		if(buffers.getMenuComponentCount() != 0)
			buffers.removeAll();
		ButtonGroup grp = new ButtonGroup();
		Enumeration enum = jEdit.getBuffers();
		while(enum.hasMoreElements())
		{
			Buffer b = (Buffer)enum.nextElement();
			String name = b.getPath();
			Object[] args = { name, b.getModeName(),
				new Integer(b.isDirty() ? 1 : 0) };
			JRadioButtonMenuItem menuItem =
				new JRadioButtonMenuItem(jEdit.getProperty(
					"view.menulabel",args));
			menuItem.addActionListener(jEdit.getAction("select-buffer"));
			grp.add(menuItem);
			menuItem.setActionCommand(name);
			if(buffer == b)
				menuItem.getModel().setSelected(true);
			buffers.add(menuItem);
		}
	}
	
	/**
	 * Recreates the open recent menu.
	 */
	public void updateOpenRecentMenu()
	{
		if(openRecent.getMenuComponentCount() != 0)
			openRecent.removeAll();
		Action action = jEdit.getAction("open-path");
		Enumeration enum = jEdit.getRecent();
		if(!enum.hasMoreElements())
		{
			openRecent.add(jEdit.loadMenuItem(this,"no-recent"));
			return;
		}
		while(enum.hasMoreElements())
		{
			String path = (String)enum.nextElement();
			JMenuItem menuItem = new JMenuItem(path);
			menuItem.setActionCommand(path);
			menuItem.addActionListener(action);
			openRecent.add(menuItem);
		}
	}
	
	/**
	 * Recreates the goto marker and clear marker menus.
	 */
	public void updateMarkerMenus()
	{
		if(clearMarker.getMenuComponentCount() != 0)
			clearMarker.removeAll();
		if(gotoMarker.getMenuComponentCount() != 0)
			gotoMarker.removeAll();
		Action clearMarkerAction = jEdit.getAction("clear-marker");
		Action gotoMarkerAction = jEdit.getAction("goto-marker");
		Enumeration enum = buffer.getMarkers();
		if(!enum.hasMoreElements())
		{
			clearMarker.add(jEdit.loadMenuItem(this,"no-markers"));
			gotoMarker.add(jEdit.loadMenuItem(this,"no-markers"));
			return;
		}
		while(enum.hasMoreElements())
		{
			String name = ((Marker)enum.nextElement())
				.getName();
			JMenuItem menuItem = new JMenuItem(name);
			menuItem.setActionCommand(name);
			menuItem.addActionListener(clearMarkerAction);
			clearMarker.add(menuItem);
			menuItem = new JMenuItem(name);
			menuItem.setActionCommand(name);
			menuItem.addActionListener(gotoMarkerAction);
			gotoMarker.add(menuItem);
		}
	}

	/**
	 * Recreates the mode menu.
	 */
	public void updateModeMenu()
	{
		if(mode.getMenuComponentCount() != 0)
			mode.removeAll();
		Mode bufferMode = buffer.getMode();
		ButtonGroup grp = new ButtonGroup();
		Enumeration enum = jEdit.getModes();
		JMenuItem menuItem = new JRadioButtonMenuItem(jEdit
			.getModeName(null));
		menuItem.addActionListener(jEdit.getAction("select-mode"));
		menuItem.setActionCommand(null);
		if(bufferMode == null)
			menuItem.getModel().setSelected(true);
		grp.add(menuItem);
		mode.add(menuItem);
		while(enum.hasMoreElements())
		{
			Mode m = (Mode)enum.nextElement();
			String name = jEdit.getModeName(m);
			menuItem = new JRadioButtonMenuItem(name);
			menuItem.addActionListener(jEdit.getAction("select-mode"));
			String clazz = m.getClass().getName();
			menuItem.setActionCommand(clazz.substring(clazz
				.lastIndexOf('.') + 1));
			grp.add(menuItem);
			if(m == bufferMode)
				menuItem.getModel().setSelected(true);
			mode.add(menuItem);
		}
	}
	
	/**
	 * Updates the status bar.
	 * @param force True if it should be updated even if the caret
	 * hasn't moved since the last update
	 */
	public void updateStatus(boolean force)
	{
		updateStatus(textArea.getCaretPosition(),force);
	}

	/**
	 * Returns the buffer being edited by this view.
	 */
	public Buffer getBuffer()
	{
		return buffer;
	}

	/**
	 * Sets the buffer being edited by this view.
	 * @param buffer The buffer to edit. If this is null, the first buffer
	 * will be edited
	 */
	public void setBuffer(Buffer buffer)
	{
		final JScrollBar h = scroller.getHorizontalScrollBar();
		final JScrollBar v = scroller.getVerticalScrollBar();
		if(this.buffer != null)
			this.buffer.setCaretInfo(h.getValue(),v.getValue(),
				textArea.getSelectionStart(),
				textArea.getSelectionEnd());
		this.buffer = buffer;
		textArea.setDocument(buffer);
		updateBuffersMenu();
		updateMarkerMenus();
		updateModeMenu();
		final int[] caretInfo = this.buffer.getCaretInfo();
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				textArea.select(caretInfo[2],caretInfo[3]);
				h.setValue(caretInfo[0]);
				v.setValue(caretInfo[1]);
				textArea.requestFocus();
				updateStatus(true);
			}
		});
	}

	/**
	 * Returns this view's text area.
	 */
	public SyntaxTextArea getTextArea()
	{
		return textArea;
	}
	
	/**
	 * Adds a key binding to this view.
	 * @param key The key stroke
	 * @param cmd The action command
	 */
	public void addKeyBinding(KeyStroke key1, String cmd)
	{
		prefixes.put(key1,cmd);
	}

	/**
	 * Adds a multi-keystroke key binding to this view.
	 * @param key1 The first key stroke
	 * @param key2 The second key stroke
	 * @param cmd The action command
	 */
	public void addKeyBinding(KeyStroke key1, KeyStroke key2,
		String cmd)
	{
		Object o = prefixes.get(key1);
		if(!(o instanceof Hashtable))
		{
			o = new Hashtable();
			prefixes.put(key1,o);
		}
		((Hashtable)o).put(key2,cmd);
	}

	// event handlers

	public void caretUpdate(CaretEvent evt)
	{
		updateStatus(evt.getDot(),false);
	}

	public void keyPressed(KeyEvent evt)
	{
		int keyCode = evt.getKeyCode();
		if((evt.getModifiers() & ~InputEvent.SHIFT_MASK) != 0 ||
			evt.isActionKey())
		{
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode,
				evt.getModifiers());
			Object o = currentPrefix.get(keyStroke);
			if(o == null && currentPrefix != prefixes)
			{
				getToolkit().beep();
				evt.consume();
				return;
			}
			else if(o instanceof String)
			{
				String s = (String)o;
				int index = s.indexOf('@');
				String cmd;
				if(index != -1)
				{
					cmd = s.substring(index+1);
					s = s.substring(0,index);
				}
				else
					cmd = null;
				Action action = jEdit.getAction(s);
				if(action == null)
				{
					System.out.println("Invalid key"
						+ " binding:" + s);
					return;
				}
				jEdit.getAction(s).actionPerformed(
					new ActionEvent(this,ActionEvent
					.ACTION_PERFORMED,cmd));
				currentPrefix = prefixes;
				evt.consume();
				return;
			}
			else if(o instanceof Hashtable)
			{
				currentPrefix = (Hashtable)o;
				evt.consume();
				return;
			}
		}
		else if(keyCode != KeyEvent.VK_SHIFT
			&& keyCode != KeyEvent.VK_CONTROL
			&& keyCode != KeyEvent.VK_ALT)
		{
			currentPrefix = prefixes;
		}
				
		Mode mode = buffer.getMode();
		if(mode instanceof KeyListener)
			((KeyListener)mode).keyTyped(evt);
		else if(mode != null && jEdit.getAutoIndent()
			&& keyCode == KeyEvent.VK_TAB)
		{
			if(mode.indentLine(buffer,this,textArea
				.getCaretPosition()))
			{
				evt.consume();
				return;
			}
			textArea.replaceSelection("\t");
			evt.consume();
		}
	}

	public void keyTyped(KeyEvent evt)
	{
		Mode mode = buffer.getMode();
		if(mode instanceof KeyListener)
			((KeyListener)mode).keyTyped(evt);
	}

	public void keyReleased(KeyEvent evt)
	{
		Mode mode = buffer.getMode();
		if(mode instanceof KeyListener)
			((KeyListener)mode).keyReleased(evt);
	}

	public void windowOpened(WindowEvent evt) {}
	
	public void windowClosing(WindowEvent evt)
	{
		jEdit.closeView(this);
	}
	
	public void windowClosed(WindowEvent evt) {}
	public void windowIconified(WindowEvent evt) {}
	public void windowDeiconified(WindowEvent evt) {}
	public void windowActivated(WindowEvent evt) {}
	public void windowDeactivated(WindowEvent evt) {}

	// package-private members
	View(Buffer buffer)
	{
		plugins = jEdit.loadMenu(this,"plugins");
		buffers = jEdit.loadMenu(this,"buffers");
		openRecent = jEdit.loadMenu(this,"open-recent");
		clearMarker = jEdit.loadMenu(this,"clear-marker");
		gotoMarker = jEdit.loadMenu(this,"goto-marker");
		mode = jEdit.loadMenu(this,"mode");
		prefixes = new Hashtable();
		currentPrefix = prefixes;
		int x;
		int y;
		try
		{
			x = Integer.parseInt(jEdit.getProperty(
				"view.geometry.x"));
		}
		catch(Exception e)
		{
			x = 100;
		}
		try
		{
			y = Integer.parseInt(jEdit.getProperty(
				"view.geometry.y"));
		}
		catch(Exception e)
		{
			y = 50;
		}
		int w = 80;
		int h = 30;
		try
		{
			w = Integer.parseInt(jEdit.getProperty(
				"view.geometry.w"));
		}
		catch(Exception e)
		{
		}
		try
		{
			h = Integer.parseInt(jEdit.getProperty(
				"view.geometry.h"));
		}
		catch(Exception e)
		{
		}
		textArea = new SyntaxTextArea();
		scroller = new JScrollPane(textArea,ScrollPaneConstants
			.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants
			.HORIZONTAL_SCROLLBAR_AS_NEEDED);	
		status = new JLabel("Try our new product: soap! `It's fresh!'");
		if(buffer == null)
			setBuffer((Buffer)jEdit.getBuffers().nextElement());
		else
			setBuffer(buffer);
		textArea.addKeyListener(this);
		textArea.addCaretListener(this);
		textArea.setBorder(null);
		updatePluginsMenu();
		setJMenuBar(jEdit.loadMenubar(this,"view.mbar"));
		propertiesChanged();
		FontMetrics fm = getToolkit().getFontMetrics(textArea
			.getFont());
		JViewport viewport = scroller.getViewport();
		viewport.setPreferredSize(new Dimension(w * fm.charWidth('m'),
			h * fm.getHeight()));
		getContentPane().add("Center",scroller);
		getContentPane().add("South",status);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addKeyListener(this);
		addWindowListener(this);
		pack();
		setLocation(x,y);
		show();
	}

	JMenu getMenu(String name)
	{
		if(name.equals("plugins"))
			return plugins;
		else if(name.equals("buffers"))
			return buffers;
		else if(name.equals("open-recent"))
			return openRecent;
		else if(name.equals("clear-marker"))
			return clearMarker;
		else if(name.equals("goto-marker"))
			return gotoMarker;
		else if(name.equals("mode"))
			return mode;
		else
			return null;
	}
	
	// private members
	private JMenu plugins;
	private JMenu buffers;
	private JMenu openRecent;
	private JMenu clearMarker;
	private JMenu gotoMarker;
	private JMenu mode;
	private Hashtable prefixes;
	private Hashtable currentPrefix;
	private JScrollPane scroller;
	private SyntaxTextArea textArea;
	private JLabel status;
	private int lastLine;
	private Buffer buffer;

	private void updateStatus(int dot, boolean force)
	{
		int currLine;
		Element map = buffer.getDefaultRootElement();
		currLine = map.getElementIndex(dot) + 1;
		if(lastLine == currLine && !force)
			return;
		lastLine = currLine;
		int numLines = map.getElementCount();
		Object[] args = { buffer.getName(),
			buffer.getModeName(),
			new Integer(buffer.isNewFile() ? 1 : 0),
			new Integer(buffer.isReadOnly() ? 1 : 0),
			new Integer(buffer.isDirty() ? 1 : 0),
			new Integer(currLine),
			new Integer(numLines),
			new Integer((currLine * 100) / numLines) };
		status.setText(jEdit.getProperty("view.status",args));
		args[0] = this.buffer.getPath();
		setTitle(jEdit.getProperty("view.title",args));
		updateBuffersMenu();
		textArea.setEditable(!buffer.isReadOnly());
	}
}
