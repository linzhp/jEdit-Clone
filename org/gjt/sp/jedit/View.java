/*
 * View.java - jEdit view
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

import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import org.gjt.sp.jedit.gui.SyntaxTextArea;

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
	 * Reloads various settings from the properties.
	 */
	public void propertiesChanged()
	{
		String family = jEdit.getProperty("view.font");
		int size;
		try
		{
			size = Integer.parseInt(jEdit.getProperty(
				"view.fontsize"));
		}
		catch(NumberFormatException nf)
		{
			size = 14;
		}
		int style;
		try
		{
			style = Integer.parseInt(jEdit.getProperty(
				"view.fontstyle"));
		}
		catch(NumberFormatException nf)
		{
			style = Font.PLAIN;
		}
		Font font = new Font(family,style,size);
		textArea.setFont(font);
		textArea.setLineHighlight("on".equals(jEdit.getProperty(
			"view.lineHighlight")));
		textArea.setLineHighlightColor(jEdit.parseColor(jEdit
			.getProperty("view.lineHighlightColor")));
		textArea.setBracketHighlight("on".equals(jEdit.getProperty(
			"view.bracketHighlight")));
		textArea.setBracketHighlightColor(jEdit.parseColor(jEdit
			.getProperty("view.bracketHighlightColor")));
		textArea.setCaretColor(jEdit.parseColor(jEdit
			.getProperty("view.caretColor")));
		textArea.setSelectionColor(jEdit.parseColor(jEdit
			.getProperty("view.selectionColor")));
		textArea.setBackground(jEdit.parseColor(jEdit
			.getProperty("view.bgColor")));
		textArea.setForeground(jEdit.parseColor(jEdit
			.getProperty("view.fgColor")));
		textArea.setBlockCaret("on".equals(jEdit.getProperty(
			"view.blockCaret")));
		try
		{
			textArea.getCaret().setBlinkRate(Integer.parseInt(jEdit
				.getProperty("view.caretBlinkRate")));
		}
		catch(NumberFormatException nf)
		{
			textArea.getCaret().setBlinkRate(0);
		}
		try
		{
			textArea.setElectricBorders(Integer.parseInt(jEdit
				.getProperty("view.electricBorders")));
		}
		catch(NumberFormatException nf)
		{
			textArea.setElectricBorders(0);
		}
		updateOpenRecentMenu();
		if(buffer != null) /* ie, after startup */
			updateLineNumber(true);
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
			Object[] args = { name, new Integer(b.isReadOnly() ? 1: 0),
				new Integer(b.isDirty() ? 1 : 0),
				new Integer(0), null };
			JRadioButtonMenuItem menuItem =
				new JRadioButtonMenuItem(jEdit.getProperty(
					"view.title",args));
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
	 * Recreates the error list menu.
	 */
	public void updateErrorListMenu()
	{
		if(errors.getMenuComponentCount() != 0)
			errors.removeAll();
		Action gotoError = jEdit.getAction("goto-error");
		Enumeration enum = jEdit.getErrors();
		if(enum == null)
		{
			errors.add(jEdit.loadMenuItem(this,"no-errors"));
			return;
		}
		int count = 0;
		while(enum.hasMoreElements())
		{
			CompilerError error = (CompilerError)enum.nextElement();
			String path = error.getPath();
			int index = path.lastIndexOf(File.separatorChar);
			if(index != -1)
				path = path.substring(index + 1);
			JMenuItem menuItem = new JMenuItem(path
				+ ":" + (error.getLineNo() + 1) + ":"
				+ error.getError());
			menuItem.setActionCommand(String.valueOf(count++));
			menuItem.addActionListener(gotoError);
			errors.add(menuItem);
		}
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
	 * Recreates the line separator menu.
	 */
	public void updateLineSepMenu()
	{
		if(lineSep.getMenuComponentCount() != 0)
			lineSep.removeAll();
		Action action = jEdit.getAction("select-line-sep");
		String newline = (String)buffer.getProperty(Buffer.LINESEP);
		if(newline == null)
			newline = System.getProperty("line.separator");
		ButtonGroup grp = new ButtonGroup();
		JMenuItem menuItem = new JRadioButtonMenuItem(jEdit.getProperty(
			"lineSep.unix"));
		menuItem.setActionCommand("\n");
		menuItem.addActionListener(action);
		menuItem.getModel().setSelected("\n".equals(newline));
		grp.add(menuItem);
		lineSep.add(menuItem);
		menuItem = new JRadioButtonMenuItem(jEdit.getProperty(
			"lineSep.windows"));
		menuItem.setActionCommand("\r\n");
		menuItem.addActionListener(action);
		menuItem.getModel().setSelected("\r\n".equals(newline));
		grp.add(menuItem);
		lineSep.add(menuItem);
		menuItem = new JRadioButtonMenuItem(jEdit.getProperty(
			"lineSep.mac"));
		menuItem.setActionCommand("\r");
		menuItem.addActionListener(action);
		menuItem.getModel().setSelected("\r".equals(newline));
		grp.add(menuItem);
		lineSep.add(menuItem);
	}

	/**
	 * Updates the line number indicator.
	 * @param force True if it should be updated even if the caret
	 * hasn't moved since the last update
	 */
	public void updateLineNumber(boolean force)
	{
		updateLineNumber(textArea.getCaretPosition(),force);
	}

	/**
	 * Updates the title bar and read only status of the text
	 * area.
	 */
	public void updateTitle()
	{
		String tip;
		if(showTip)
		{
			try
			{
				tip = jEdit.getProperty("tip." +
					(Math.abs(new Random().nextInt()) %
					Integer.parseInt(jEdit.getProperty(
						"tip.count"))));
			}
			catch(Exception e)
			{
				tip = "Oops";
			}
			showTip = false;
		}
		else
			tip = null;
		Object[] args = { buffer.getName(),
			new Integer(buffer.isReadOnly() ? 1 : 0),
			new Integer(buffer.isDirty() ? 1: 0),
			new Integer(tip != null ? 1 : 0),
			tip };
		setTitle(jEdit.getProperty("view.title",args));
		textArea.setEditable(!buffer.isReadOnly());
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
	 * @param buffer The buffer to edit.
	 */
	public void setBuffer(Buffer buffer)
	{
		if(this.buffer != null)
			saveCaretInfo();
		this.buffer = buffer;
		textArea.setDocument(buffer);
		updateLineSepMenu();
		updateMarkerMenus();
		updateModeMenu();
		updateTitle();
		updateLineNumber(true);
	}

	/**
	 * Returns this view's text area.
	 */
	public SyntaxTextArea getTextArea()
	{
		return textArea;
	}
	
	/**
	 * Saves the caret information to the current buffer.
	 */
	public void saveCaretInfo()
	{
		buffer.setCaretInfo(textArea.getSelectionStart(),
			textArea.getSelectionEnd());
	}

	/**
	 * Adds a key binding to this view.
	 * @param key The key stroke
	 * @param cmd The action command
	 */
	public void addKeyBinding(KeyStroke key1, String cmd)
	{
		bindings.put(key1,cmd);
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
		Object o = bindings.get(key1);
		if(!(o instanceof Hashtable))
		{
			o = new Hashtable();
			bindings.put(key1,o);
		}
		((Hashtable)o).put(key2,cmd);
	}

	// event handlers
	public void caretUpdate(CaretEvent evt)
	{
		updateLineNumber(evt.getDot(),false);
	}

	public void keyPressed(KeyEvent evt)
	{
		int keyCode = evt.getKeyCode();
		int modifiers = evt.getModifiers();
		if((modifiers & ~InputEvent.SHIFT_MASK) != 0 ||
			evt.isActionKey())
		{
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode,
				modifiers);
			Object o = currentPrefix.get(keyStroke);
			if(o == null && currentPrefix != bindings)
			{
				getToolkit().beep();
				currentPrefix = bindings;
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
						+ " binding: " + s);
					currentPrefix = bindings;
					evt.consume();
					return;
				}
				jEdit.getAction(s).actionPerformed(
					new ActionEvent(this,ActionEvent
					.ACTION_PERFORMED,cmd));
				currentPrefix = bindings;
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
			currentPrefix = bindings;
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
			if("yes".equals(buffer.getProperty("noTabs")))
			{
				StringBuffer buf = new StringBuffer();
				for(int i = buffer.getTabSize(); i > 0; i--)
				{
					// this is broken cos if we press
					// tab in a non %tabSize==0 column,
					// but I can't be fucked fixing it
					/// right now
					buf.append(' ');
				}
				textArea.replaceSelection(buf.toString());
			}
			else
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
		showTip = ("on".equals(jEdit.getProperty("view.showTips")));
		
		buffers = jEdit.loadMenu(this,"buffers");
		openRecent = jEdit.loadMenu(this,"open-recent");
		clearMarker = jEdit.loadMenu(this,"clear-marker");
		gotoMarker = jEdit.loadMenu(this,"goto-marker");
		errors = jEdit.loadMenu(this,"errors");
		plugins = jEdit.loadMenu(this,"plugins");
		mode = jEdit.loadMenu(this,"mode");
		lineSep = jEdit.loadMenu(this,"line-separator");

		bindings = new Hashtable();
		currentPrefix = bindings;
		lineSegment = new Segment();
		
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

		lineNumber = new JLabel();
		textArea.addKeyListener(this);
		textArea.addCaretListener(this);
		textArea.setBorder(null);
		updateErrorListMenu();
		updatePluginsMenu();

		JMenuBar mbar = jEdit.loadMenubar(this,"view.mbar");
		mbar.add(new JPanel()); // silly hack to move ruler to right side
		mbar.add(lineNumber);
		setJMenuBar(mbar);

		propertiesChanged();

		FontMetrics fm = getToolkit().getFontMetrics(textArea
			.getFont());
		JViewport viewport = scroller.getViewport();
		viewport.setPreferredSize(new Dimension(w * fm.charWidth('m'),
			h * fm.getHeight()));

		if(buffer == null)
			setBuffer((Buffer)jEdit.getBuffers().nextElement());
		else
			setBuffer(buffer);
		updateBuffersMenu();

		getContentPane().add("Center",scroller);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addKeyListener(this);
		addWindowListener(this);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		show();
	}

	JMenu getMenu(String name)
	{
		if(name.equals("buffers"))
			return buffers;
		else if(name.equals("open-recent"))
			return openRecent;
		else if(name.equals("clear-marker"))
			return clearMarker;
		else if(name.equals("goto-marker"))
			return gotoMarker;
		else if(name.equals("errors"))
			return errors;
		else if(name.equals("plugins"))
			return plugins;
		else if(name.equals("mode"))
			return mode;
		else if(name.equals("line-separator"))
			return lineSep;
		else
			return null;
	}
	
	// private members
	private JMenu buffers;
	private JMenu openRecent;
	private JMenu clearMarker;
	private JMenu gotoMarker;
	private JMenu errors;
	private JMenu plugins;
	private JMenu mode;
	private JMenu lineSep;
	private Hashtable bindings;
	private Hashtable currentPrefix;
	private JScrollPane scroller;
	private SyntaxTextArea textArea;
	private JLabel lineNumber;
	private Segment lineSegment;
	private Buffer buffer;
	private boolean showTip;

	private void updateLineNumber(int dot, boolean force)
	{
		int currLine;
		Element map = buffer.getDefaultRootElement();
		currLine = map.getElementIndex(dot);
		Element lineElement = map.getElement(currLine);
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset();
		int numLines = map.getElementCount();
		Object[] args = { new Integer((dot - start) + 1),
			new Integer(currLine + 1),
			new Integer(numLines),
			new Integer(((currLine + 1) * 100) / numLines) };
		lineNumber.setText(jEdit.getProperty("view.lineNumber",args));
		if(textArea.getSelectionStart() == textArea.getSelectionEnd())
			textArea.setHighlightedLine(start,end);
		else
			textArea.setHighlightedLine(0,0);
		try
		{
			if(dot != 0)
			{
				dot--;
				buffer.getText(dot,1,lineSegment);
				char bracket = lineSegment.array[lineSegment
					.offset];
				int otherBracket;
				switch(bracket)
				{
				case '(':
					otherBracket = buffer.locateBracketForward(
						dot,'(',')');
					break;
				case ')':
					otherBracket = buffer.locateBracketBackward(
						dot,'(',')');
					break;
				case '[':
					otherBracket = buffer.locateBracketForward(
						dot,'[',']');
					break;
				case ']':
					otherBracket = buffer.locateBracketBackward(
						dot,'[',']');
					break;
				case '{':
					otherBracket = buffer.locateBracketForward(
						dot,'{','}');
					break;
				case '}':
					otherBracket = buffer.locateBracketBackward(
						dot,'{','}');
					break;
				default:
					otherBracket = -1;
					break;
				}
				textArea.setHighlightedBracket(otherBracket);
			}
			else
				textArea.setHighlightedBracket(-1);
		}
		catch(BadLocationException bl)
		{
			//bl.printStackTrace();
		}
	}
}
