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

import com.sun.java.swing.ButtonGroup;
import com.sun.java.swing.JFrame;
import com.sun.java.swing.JLabel;
import com.sun.java.swing.JMenu;
import com.sun.java.swing.JMenuBar;
import com.sun.java.swing.JMenuItem;
import com.sun.java.swing.JRadioButtonMenuItem;
import com.sun.java.swing.JScrollPane;
import com.sun.java.swing.JTextArea;
import com.sun.java.swing.KeyStroke;
import com.sun.java.swing.event.CaretEvent;
import com.sun.java.swing.event.CaretListener;
import com.sun.java.swing.text.BadLocationException;
import com.sun.java.swing.text.Element;
import com.sun.java.swing.text.DefaultEditorKit;
import com.sun.java.swing.text.JTextComponent;
import com.sun.java.swing.text.Keymap;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;
import java.util.Hashtable;

public class View extends JFrame
implements ActionListener, KeyListener, CaretListener, WindowListener
{	
	private JMenu plugins;
	private JMenu buffers;
	private JMenu openRecent;
	private JMenu clearMarker;
	private JMenu gotoMarker;
	private Hashtable dynamicMenus;
	private JTextArea textArea;
	private JLabel status;
	private boolean autoindent;
	private int lastLine;
	private JMenuBar menuBar;
	private Buffer buffer;
	
	public View(View view)
	{
		dynamicMenus = new Hashtable();
		dynamicMenus.put("plugins",plugins = jEdit.loadMenu(this,
			"plugins"));
		dynamicMenus.put("buffers",buffers = jEdit.loadMenu(this,
			"buffers"));
		dynamicMenus.put("open_recent",openRecent = jEdit
			.loadMenu(this,"open_recent"));
		dynamicMenus.put("clear_marker",clearMarker = jEdit
			.loadMenu(this,"clear_marker"));
		dynamicMenus.put("goto_marker",gotoMarker = jEdit
			.loadMenu(this,"goto_marker"));
		textArea = new JTextArea();
		status = new JLabel();
		if(view == null)
			setBuffer(null);
		else
			setBuffer(view.getBuffer());
		textArea.addCaretListener(this);
		textArea.addKeyListener(this);
		textArea.setBorder(null);
		updateStatus();
		updatePluginsMenu();
		updateBuffersMenu();
		updateMarkerMenus();
		menuBar = jEdit.loadMenubar(this,"editor_mbar");
		setJMenuBar(menuBar);
		propertiesChanged();
		getContentPane().add("Center",new JScrollPane(textArea));
		getContentPane().add("South",status);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		Dimension screen = getToolkit().getScreenSize();
		int x,y,w,h;
		try
		{
			x = Integer.parseInt(jEdit.props
				.getProperty("editor.x"));
		}
		catch(NumberFormatException e)
		{
			x = screen.width / 2 - 300;
		}
		try
		{
			y = Integer.parseInt(jEdit.props
				.getProperty("editor.y"));
		}
		catch(NumberFormatException e)
		{
			y = screen.height / 2 - 200;
		}

		try
		{
			w = Integer.parseInt(jEdit.props
				.getProperty("editor.w"));
		}
		catch(NumberFormatException e)
		{
			w = 600;
		}

		try
		{
			h = Integer.parseInt(jEdit.props
				.getProperty("editor.h"));
		}
		catch(NumberFormatException e)
		{
			h = 400;
		}
		setBounds(x,y,w,h);
		show();
	}

	public void propertiesChanged()
	{
		String family = jEdit.props.getProperty("editor.font",
			"Monospaced");
		int size, style;
		try
		{
			size = Integer.parseInt(jEdit.props
				.getProperty("editor.fontsize"));
		}
		catch(NumberFormatException e)
		{
			size = 12;
		}
		try
		{
			style = Integer.parseInt(jEdit.props
				.getProperty("editor.fontstyle"));
		}
		catch(NumberFormatException e)
		{
			style = 0;
		}
		Font font = new Font(family,style,size);
		textArea.setFont(font);
		status.setFont(font);
		try
		{
			textArea.setTabSize(Integer.parseInt(jEdit.props
				.getProperty("editor.tabsize")));
		}
		catch(Exception e)
		{
		}
		String linewrap = jEdit.props.getProperty("editor.linewrap");
		if("word".equals(linewrap))
		{
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
		}
		else if("char".equals(linewrap))
		{
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(false);
		}
		autoindent = "on".equals(jEdit.props.getProperty(
			"editor.autoindent"));
		updateOpenRecentMenu();
	}
	
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
			JMenuItem menuItem = jEdit.loadMenuItem(this,
				enum.nextElement().getClass().getName()
				.substring(4));
			if(menuItem != null)
				plugins.add(menuItem);
		}
	}
	
	public void updateBuffersMenu()
	{
		buffers.removeAll();
		ButtonGroup grp = new ButtonGroup();
		Enumeration enum = jEdit.buffers.getBuffers();
		while(enum.hasMoreElements())
		{
			String name = ((Buffer)enum.nextElement()).getPath();
			JRadioButtonMenuItem menuItem =
				new JRadioButtonMenuItem(name);
			menuItem.getModel().setGroup(grp);
			if(name.equals(buffer.getPath()))
				menuItem.setSelected(true);
			menuItem.setActionCommand("select_buffer@"
				.concat(name));
			menuItem.addActionListener(this);
			buffers.add(menuItem);
		}
	}

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
			menuItem.setActionCommand("open@".concat(name));
			menuItem.addActionListener(this);
			openRecent.add(menuItem);
		}
	}
	
	public void updateMarkerMenus()
	{
		clearMarker.removeAll();
		gotoMarker.removeAll();
		Enumeration enum = buffer.getMarkers();
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
			String name = ((Marker)enum.nextElement()).getName();
			JMenuItem menuItem = new JMenuItem(name);
			menuItem.setActionCommand("clear_marker@"
				.concat(name));
			menuItem.addActionListener(this);
			clearMarker.add(menuItem);
			menuItem = new JMenuItem(name);
			menuItem.setActionCommand("goto_marker@".concat(name));
			menuItem.addActionListener(this);
			gotoMarker.add(menuItem);
		}
	}
	
	public JMenu getDynamicMenu(String name)
	{
		return (JMenu)dynamicMenus.get(name);
	}

	public void updateStatus(boolean force)
	{
		updateStatus(textArea.getCaretPosition(),force);
	}

	public void updateStatus()
	{
		updateStatus(false);
	}
	
	private void updateStatus(int dot, boolean force)
	{
		int currLine;
		try
		{
			currLine = textArea.getLineOfOffset(dot) + 1;
		}
		catch(BadLocationException bl)
		{
			throw new IllegalArgumentException("Aiee!!! text"
				+ " area out of sync");
		}
		if(lastLine == currLine && !force)
			return;
		lastLine = currLine;
		int numLines = textArea.getLineCount();
		Object[] args = { buffer.getPath(),
			new Integer(buffer.isNewFile() ? 1 : 0),
			new Integer(buffer.isDirty() ? 1 : 0),
			new Integer(currLine),
			new Integer(numLines),
			new Integer(currLine * 100 / numLines) };
		status.setText(jEdit.props.getProperty("status",args));
		setTitle(this.buffer.getPath());
	}
			
	public Buffer getBuffer()
	{
		return buffer;
	}

	public void setBuffer(Buffer buffer)
	{
		if(buffer == null)
			this.buffer = jEdit.buffers.getBufferAt(0);
		else
			this.buffer = buffer;
		textArea.setDocument(this.buffer);
		updateBuffersMenu();
		updateStatus(true);
	}

	public JTextArea getTextArea()
	{
		return textArea;
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		jEdit.cmds.execCommand(this,evt.getActionCommand());
	}

	public void caretUpdate(CaretEvent evt)
	{
		updateStatus(evt.getDot(),false);
	}

	public void keyPressed(KeyEvent evt)
	{
		// COLOSTOMY BAG
		// this needs to be rewritten totally
		if(evt.getKeyCode() == KeyEvent.VK_ENTER && autoindent)
		{
			try
			{
				int caret = textArea.getCaretPosition();
				Element map = getBuffer()
					.getDefaultRootElement();
				Element lineElement = map.getElement(map
					.getElementIndex(caret));
				int start = lineElement.getStartOffset();
				char[] line = buffer.getText(start,
					lineElement.getEndOffset() - start)
					.toCharArray();
				int i = 0;
loop:				for(i = 0; i < line.length; i++)
				{
					char c = line[i];
					switch(c)
					{
					case ' ':
					case '\t':
					case '#':
					case '%':
					case '-':
					break;
					default:
					break loop;
					}
				}
				buffer.insertString(caret,"\n".concat(
					new String(line,0,i)),null);
				evt.consume();
			}
			catch(BadLocationException bl)
			{
				bl.printStackTrace();
			}
		}
	}

	public void keyTyped(KeyEvent evt) {}
	public void keyReleased(KeyEvent evt) {}
	
	public void windowOpened(WindowEvent evt) {}
	
	public void windowClosing(WindowEvent evt)
	{
		jEdit.cmds.execCommand(this,"close_view");
	}
	
	public void windowClosed(WindowEvent evt) {}
	public void windowIconified(WindowEvent evt) {}
	public void windowDeiconified(WindowEvent evt) {}
	public void windowActivated(WindowEvent evt) {}
	public void windowDeactivated(WindowEvent evt) {}
}
