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

import com.sun.java.swing.*;
import com.sun.java.swing.event.*;
import com.sun.java.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.gjt.sp.jedit.syntax.SyntaxTextArea;

/**
 * A <code>View</code> edits buffers. There is no public constructor in the
 * View class. Views are created and destroyed by the <code>BufferMgr</code>
 * class.
 * @see Buffer
 * @see BufferMgr
 * @see BufferMgr#newView(View)
 * @see BufferMgr#closeView(View)
 * @see BufferMgr#getViews()
 */
public class View extends JFrame
implements ActionListener, CaretListener, KeyListener, WindowListener
{	
	// public members

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
		/*String linewrap = jEdit.props.getProperty("view.linewrap");
		if("word".equals(linewrap))
		{
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
		}
		else if("char".equals(linewrap))
		{
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(false);
		}*/
		SwingUtilities.updateComponentTreeUI(this);
		updateOpenRecentMenu();
	}
	
	/**
	 * Recreates the plugins menu.
	 * @see #updateBuffersMenu()
	 * @see #updateOpenRecentMenu()
	 * @see #updateMarkerMenus()
	 * @see #updateModeMenu()
	 */
	public void updatePluginsMenu()
	{
		plugins.removeAll();
		Enumeration enum = jEdit.cmds.getPlugins();
		if(!enum.hasMoreElements())
		{
			JMenuItem menuItem = jEdit.loadMenuItem(this,
				"no_plugins");
			menuItem.setEnabled(false);
			plugins.add(menuItem);
			return;
		}
		while(enum.hasMoreElements())
		{
			Object ooo = enum.nextElement();
			JMenuItem menuItem = jEdit.loadMenuItem(this,ooo
				.getClass().getName()
				.substring(21));
			if(menuItem != null)
				plugins.add(menuItem);
		}
	}
	
	/**
	 * Recreates the buffers menu.
	 * @see #updatePluginsMenu()
	 * @see #updateOpenRecentMenu()
	 * @see #updateMarkerMenus()
	 * @see #updateModeMenu()
	 */
	public void updateBuffersMenu()
	{
		buffers.removeAll();
		ButtonGroup grp = new ButtonGroup();
		Enumeration enum = jEdit.buffers.getBuffers();
		while(enum.hasMoreElements())
		{
			Buffer b = (Buffer)enum.nextElement();
			String name = b.getPath();
			Object[] args = { name, b.getModeName(),
				new Integer(b.isDirty() ? 1 : 0) };
			JRadioButtonMenuItem menuItem =
				new JRadioButtonMenuItem(jEdit.props
					.getProperty("view.menulabel",args));
			menuItem.getModel().setGroup(grp);
			if(buffer == b)
				menuItem.setSelected(true);
			menuItem.setActionCommand("select_buffer@"
				.concat(name));
			menuItem.addActionListener(this);
			buffers.add(menuItem);
		}
	}
	
	/**
	 * Recreates the open recent menu.
	 * @see #updatePluginsMenu()
	 * @see #updateBuffersMenu()
	 * @see #updateMarkerMenus()
	 * @see #updateModeMenu()
	 */
	public void updateOpenRecentMenu()
	{
		openRecent.removeAll();
		Enumeration enum = jEdit.buffers.getRecent();
		if(!enum.hasMoreElements())
		{
			JMenuItem menuItem = jEdit.loadMenuItem(this,
				"no_recent");
			if(menuItem == null)
				return;
			menuItem.setEnabled(false);
			openRecent.add(menuItem);
			return;
		}
		while(enum.hasMoreElements())
		{
			String name = (String)enum.nextElement();
			JMenuItem menuItem = new JMenuItem(name);
			menuItem.setActionCommand("open_file@".concat(name));
			menuItem.addActionListener(this);
			openRecent.add(menuItem);
		}
	}
	
	/**
	 * Recreates the goto marker and clear marker menus.
	 * @see #updatePluginsMenu()
	 * @see #updateBuffersMenu()
	 * @see #updateOpenRecentMenu()
	 * @see #updateModeMenu()
	 */
	public void updateMarkerMenus()
	{
		clearMarker.removeAll();
		gotoMarker.removeAll();
		Enumeration enum = buffer.getMarkers();
		int n = 1;
		if(!enum.hasMoreElements())
		{
			JMenuItem menuItem = jEdit.loadMenuItem(this,
				"no_markers");
			if(menuItem == null)
				return;
			menuItem.setEnabled(false);
			clearMarker.add(menuItem);
			menuItem = jEdit.loadMenuItem(this,"no_markers");
			if(menuItem == null)
				return;
			menuItem.setEnabled(false);
			gotoMarker.add(menuItem);
			return;
		}
		while(enum.hasMoreElements())
		{
			String name = ((Marker)enum.nextElement())
				.getName();
			JMenuItem menuItem = new JMenuItem(name);
			menuItem.setActionCommand("clear_marker@"
				.concat(name));
			menuItem.addActionListener(this);
			clearMarker.add(menuItem);
			menuItem = new JMenuItem(name);
			menuItem.setActionCommand("goto_marker@".concat(name));
			menuItem.addActionListener(this);
			if(n <= 20)
			{
				char key = (char)('0' + n % 10);
				int mask = InputEvent.ALT_MASK;
				if(n > 10)
					mask |= InputEvent.SHIFT_MASK;
				menuItem.setAccelerator(KeyStroke.getKeyStroke(
					key,mask));
				n++;
			}
			gotoMarker.add(menuItem);
		}
	}

	/**
	 * Recreates the mode menu.
	 * @see #updatePluginsMenu()
	 * @see #updateBuffersMenu()
	 * @see #updateOpenRecentMenu()
	 * @see #updateMarkerMenus()
	 */
	public void updateModeMenu()
	{
		mode.removeAll();
		Mode bufferMode = buffer.getMode();
		ButtonGroup grp = new ButtonGroup();
		Enumeration enum = jEdit.cmds.getModes();
		JMenuItem menuItem = new JRadioButtonMenuItem(jEdit.cmds
			.getModeName(null));
		grp.add(menuItem);
		if(buffer.getMode() == null)
			menuItem.getModel().setSelected(true);
		menuItem.setActionCommand("select_mode");
		menuItem.addActionListener(this);
		mode.add(menuItem);
		while(enum.hasMoreElements())
		{
			Mode m = (Mode)enum.nextElement();
			String name = jEdit.cmds.getModeName(m);
			menuItem = new JRadioButtonMenuItem(name);
			menuItem.setActionCommand("select_mode@".concat(m
				.getClass().getName().substring(22)));
			grp.add(menuItem);
			if(m == bufferMode)
				menuItem.getModel().setSelected(true);
			menuItem.addActionListener(this);
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
				
		if(buffer == null)
			this.buffer = (Buffer)jEdit.buffers.getBuffers()
				.nextElement();
		else
			this.buffer = buffer;
		textArea.setDocument(this.buffer);
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
	
	// event handlers
	public void actionPerformed(ActionEvent evt)
	{
		jEdit.cmds.execCommand(buffer,this,evt.getActionCommand());
	}

	public void caretUpdate(CaretEvent evt)
	{
		updateStatus(evt.getDot(),false);
	}

	public void keyPressed(KeyEvent evt)
	{
		if(evt.getKeyCode() == KeyEvent.VK_TAB)
		{
			Mode mode = buffer.getMode();
			if(mode != null && jEdit.getAutoIndent())
			{
				if(mode.indentLine(buffer,this,textArea
					.getCaretPosition()))
				{
					evt.consume();
					return;
				}
			}
			textArea.replaceSelection("\t");
			evt.consume();
		}
	}

	public void keyTyped(KeyEvent evt) {}
	public void keyReleased(KeyEvent evt) {}
	
	public void windowOpened(WindowEvent evt) {}
	
	public void windowClosing(WindowEvent evt)
	{
		jEdit.buffers.closeView(this);
	}
	
	public void windowClosed(WindowEvent evt) {}
	public void windowIconified(WindowEvent evt) {}
	public void windowDeiconified(WindowEvent evt) {}
	public void windowActivated(WindowEvent evt) {}
	public void windowDeactivated(WindowEvent evt) {}

	// package-private members
	View(View view)
	{
		plugins = jEdit.loadMenu(this,"plugins");
		buffers = jEdit.loadMenu(this,"buffers");
		openRecent = jEdit.loadMenu(this,"open_recent");
		clearMarker = jEdit.loadMenu(this,"clear_marker");
		gotoMarker = jEdit.loadMenu(this,"goto_marker");
		mode = jEdit.loadMenu(this,"mode");
		int x;
		int y;
		try
		{
			x = Integer.parseInt(jEdit.props.getProperty(
				"view.geometry.x"));
		}
		catch(Exception e)
		{
			x = 100;
		}
		try
		{
			y = Integer.parseInt(jEdit.props.getProperty(
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
			w = Integer.parseInt(jEdit.props.getProperty(
				"view.geometry.w"));
		}
		catch(Exception e)
		{
		}
		try
		{
			h = Integer.parseInt(jEdit.props.getProperty(
				"view.geometry.h"));
		}
		catch(Exception e)
		{
		}
		textArea = new SyntaxTextArea();
		scroller = new JScrollPane(textArea,ScrollPaneConstants
			.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants
			.HORIZONTAL_SCROLLBAR_AS_NEEDED);	
		status = new JLabel("Tastes like chicken!");
		if(view == null)
			setBuffer(null);
		else
			setBuffer(view.getBuffer());
		textArea.addCaretListener(this);
		textArea.addKeyListener(this);
		textArea.setBorder(null);
		updatePluginsMenu();
		setJMenuBar(jEdit.loadMenubar(this,"view.mbar"));
		propertiesChanged();
		FontMetrics fm = getToolkit().getFontMetrics(textArea
			.getFont());
		JViewport viewport = scroller.getViewport();
		viewport.setPreferredSize(new Dimension(w * fm.charWidth('m'),
			h * fm.getHeight()));
		// testing
		// viewport.setBackingStoreEnabled(true);
		getContentPane().add("Center",scroller);
		getContentPane().add("South",status);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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
		else if(name.equals("open_recent"))
			return openRecent;
		else if(name.equals("clear_marker"))
			return clearMarker;
		else if(name.equals("goto_marker"))
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
		status.setText(jEdit.props.getProperty("view.status",args));
		args[0] = this.buffer.getPath();
		setTitle(jEdit.props.getProperty("view.title",args));
		updateBuffersMenu();
		textArea.setEditable(!buffer.isReadOnly());
	}
}
