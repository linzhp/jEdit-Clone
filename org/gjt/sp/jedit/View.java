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

import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import org.gjt.sp.jedit.event.*;
import org.gjt.sp.jedit.gui.*;

/**
 * A <code>View</code> edits buffers. There is no public constructor in the
 * View class. Views are created and destroyed by the <code>jEdit</code>
 * class.
 * @see Buffer
 */
public class View extends JFrame
{	
	/**
	 * Reloads various settings from the properties.
	 */
	public void propertiesChanged()
	{
		if("on".equals(jEdit.getProperty("view.showToolbar")))
		{
			if(toolBar == null)
				toolBar = GUIUtilities.loadToolBar("view.toolbar");
			if(toolBar.getParent() == null)
			{
				getContentPane().add(BorderLayout.NORTH,toolBar);
				validate();
			}
		}
		else if(toolBar != null)
		{
			getContentPane().remove(toolBar);
			toolBar = null;
			validate();
		}

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
		textArea.setLineHighlightColor(GUIUtilities.parseColor(
			jEdit.getProperty("view.lineHighlightColor")));
		textArea.setBracketHighlight("on".equals(jEdit.getProperty(
			"view.bracketHighlight")));
		textArea.setBracketHighlightColor(GUIUtilities.parseColor(
			jEdit.getProperty("view.bracketHighlightColor")));
		textArea.setCaretColor(GUIUtilities.parseColor(
			jEdit.getProperty("view.caretColor")));
		textArea.setSelectionColor(GUIUtilities.parseColor(
			jEdit.getProperty("view.selectionColor")));
		textArea.setBackground(GUIUtilities.parseColor(
			jEdit.getProperty("view.bgColor")));
		textArea.setForeground(GUIUtilities.parseColor(
			jEdit.getProperty("view.fgColor")));
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
		Buffer[] bufferArray = jEdit.getBuffers();
		for(int i = 0; i < bufferArray.length; i++)
		{
			Buffer b = bufferArray[i];
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
		String[] recentArray = jEdit.getRecent();
		if(recentArray.length == 0)
		{
			openRecent.add(GUIUtilities.loadMenuItem(this,"no-recent"));
			return;
		}
		for(int i = 0; i < recentArray.length; i++)
		{
			String path = recentArray[i];
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
			clearMarker.add(GUIUtilities.loadMenuItem(this,"no-markers"));
			gotoMarker.add(GUIUtilities.loadMenuItem(this,"no-markers"));
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
	 * Recreates the plugins menu.
	 */
	public void updatePluginsMenu()
	{
		if(plugins.getMenuComponentCount() != 0)
			plugins.removeAll();
		Action[] pluginArray = jEdit.getPlugins();
		if(pluginArray.length == 0)
		{
			plugins.add(GUIUtilities.loadMenuItem(this,"no-plugins"));
			return;
		}
		for(int i = 0; i < pluginArray.length; i++)
		{
			String action = (String)pluginArray[i].getValue(Action.NAME);
			JMenuItem mi = GUIUtilities.loadMenuItem(this,action);
			plugins.add(mi);
		}
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
		Object[] args = { buffer.getName(),
			new Integer(buffer.isReadOnly() ? 1 : 0),
			new Integer(buffer.isDirty() ? 1: 0)};
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
		Buffer oldBuffer = this.buffer;
		if(oldBuffer != null)
		{
			saveCaretInfo();
			Mode mode = oldBuffer.getMode();
			if(mode != null)
				mode.leaveView(this);
		}
		this.buffer = buffer;
		textArea.setDocument(buffer);
		updateMarkerMenus();
		updateTitle();
		updateLineNumber(true);

		Mode mode = buffer.getMode();
		if(mode != null)
			mode.enterView(this);

		// Fire event
		fireViewEvent(new ViewEvent(ViewEvent.BUFFER_CHANGED,this,
			oldBuffer));
	}

	/**
	 * Returns this view's text area.
	 */
	public SyntaxTextArea getTextArea()
	{
		return textArea;
	}
	
	/**
	 * Returns this view's command console.
	 */
	public Console getConsole()
	{
		return console;
	}

	/**
	 * Toggles the visiblity of the view's console.
	 */
	public void toggleConsoleVisibility()
	{
		int value = splitter.getDividerLocation();
		int min = splitter.getMinimumDividerLocation();
		if(value < min)
		{
			int lastLocation = splitter.getLastDividerLocation();
			if(lastLocation >= min)
				splitter.setDividerLocation(lastLocation);
			else
				splitter.setDividerLocation(min);
		}
		else
			splitter.setDividerLocation(0.0);

		console.getCommandField().requestFocus();
	}

	/**
	 * Shows the view's console if it's hidden.
	 */
	public void showConsole()
	{
		int value = splitter.getDividerLocation();
		int min = splitter.getMinimumDividerLocation();
		if(value < min)
		{
			int lastLocation = splitter.getLastDividerLocation();
			if(lastLocation >= min)
				splitter.setDividerLocation(lastLocation);
			else
				splitter.setDividerLocation(min);
		}

		console.getCommandField().requestFocus();
	}

	/**
	 * Returns this view's divider, which separates the text area
	 * and command console.
	 */
	public JSplitPane getSplitPane()
	{
		return splitter;
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

	/**
	 * Adds a view event listener to this view.
	 * @param listener The event listener
	 */
	public void addViewListener(ViewListener listener)
	{
		multicaster.addListener(listener);
	}

	/**	
	 * Removes a view event listener from this view.
	 * @param listener The event listener
	 */
	public void removeViewListener(ViewListener listener)
	{
		multicaster.removeListener(listener);
	}

	/**
	 * Forwards a view event to all registered listeners.
	 * @param evt The event
	 */
	public void fireViewEvent(ViewEvent evt)
	{
		multicaster.fire(evt);
	}

	// package-private members
	View(View view, Buffer buffer)
	{
		multicaster = new EventMulticaster();

		buffers = GUIUtilities.loadMenu(this,"buffers");
		openRecent = GUIUtilities.loadMenu(this,"open-recent");
		clearMarker = GUIUtilities.loadMenu(this,"clear-marker");
		gotoMarker = GUIUtilities.loadMenu(this,"goto-marker");
		plugins = GUIUtilities.loadMenu(this,"plugins");

		bindings = new Hashtable();
		currentPrefix = bindings;
		lineSegment = new Segment();
		
                // Register tab
                addKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,0),
			"indent-line");

		textArea = new SyntaxTextArea();
		scroller = new JScrollPane(textArea,ScrollPaneConstants
			.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants
			.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		lastLine = -1;

		lineNumber = new JLabel();
		lineNumber.setBorder(new EmptyBorder(0,10,0,0)); // ten pixel border on left
		String tip;
		if("on".equals(jEdit.getProperty("view.showTips")))
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

			String[] args = { tip };
			tip = jEdit.getProperty("view.hintBar",args);
		}
		else
			tip = null;
	
		hintBar = new JLabel(tip);

		JPanel status = new JPanel(new BorderLayout());
		status.add(hintBar,BorderLayout.CENTER); // hintBar takes up whatever room there is
		status.add(lineNumber,BorderLayout.EAST); // lineNumber is always full width (or the frame width)

		updatePluginsMenu();

		textArea.setContextMenu(GUIUtilities.loadPopupMenu(this,
			"view.context"));
		setJMenuBar(GUIUtilities.loadMenubar(this,"view.mbar"));

		propertiesChanged();

		FontMetrics fm = getToolkit().getFontMetrics(textArea
			.getFont());

		if(buffer == null)
		{
			Buffer[] buffers = jEdit.getBuffers();
			setBuffer(buffers[buffers.length - 1]);
		}
		else
			setBuffer(buffer);
		updateBuffersMenu();

		console = new Console(this);

		splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			console,scroller);
		splitter.setOneTouchExpandable(true);
		splitter.setPreferredSize(new Dimension(81 * fm.charWidth('m'),
			26 * fm.getHeight()));

		getContentPane().add(BorderLayout.CENTER,splitter);
		getContentPane().add(BorderLayout.SOUTH,status);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		if(view != null)
		{
			setSize(view.getSize());
			Point location = view.getLocation();
			location.x += 20;
			location.y += 20;
			setLocation(location);
		}
		else
		{
			GUIUtilities.loadGeometry(this,"view");
		}

		splitter.setDividerLocation(0.0);

		try
		{
			splitter.setLastDividerLocation(Integer
				.parseInt(jEdit.getProperty(
				"view.divider")));
		}
		catch(Exception e)
		{
		}

		bufferListener = new ViewBufferListener();

		Buffer[] bufferArray = jEdit.getBuffers();
		for(int i = 0; i < bufferArray.length; i++)
			bufferArray[i].addBufferListener(bufferListener);

		jEdit.addEditorListener(editorListener = new ViewEditorListener());
		textArea.addKeyListener(new ViewKeyListener());
		textArea.addCaretListener(new ViewCaretListener());
		addKeyListener(new ViewKeyListener());
		addWindowListener(new ViewWindowListener());
		
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
		else if(name.equals("plugins"))
			return plugins;
		else
			return null;
	}
	
	void close()
	{
		GUIUtilities.saveGeometry(this,"view");

		int location = splitter.getDividerLocation();
		if(location > splitter.getHeight()
			- splitter.getDividerSize() - 15)
			location = splitter.getLastDividerLocation();
		jEdit.setProperty("view.divider",String.valueOf(location));

		console.getCommandField().save();
		console.stop();

		saveCaretInfo();

		Buffer[] bufferArray = jEdit.getBuffers();
		for(int i = 0; i < bufferArray.length; i++)
			bufferArray[i].removeBufferListener(bufferListener);

		jEdit.removeEditorListener(editorListener);
	}

	// private members
	private JMenu buffers;
	private JMenu openRecent;
	private JMenu clearMarker;
	private JMenu gotoMarker;
	private JMenu plugins;
	private Hashtable bindings;
	private Hashtable currentPrefix;
	private JScrollPane scroller;
	private SyntaxTextArea textArea;
	private Console console;
	private JSplitPane splitter;
	private JLabel lineNumber;
	private JLabel hintBar;
	private Segment lineSegment;
	private int lastLine;
	private Buffer buffer;
	private boolean showTip;
	private JToolBar toolBar;
	private EventMulticaster multicaster;
	private BufferListener bufferListener;
	private EditorListener editorListener;

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
		{
			if(currLine != lastLine)
				textArea.setHighlightedLine(start,end);
			lastLine = currLine;
		}
		else
		{
			textArea.setHighlightedLine(0,0);
			lastLine = -1;
		}
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

	// event listeners
	class ViewBufferListener implements BufferListener
	{
		public void bufferDirtyChanged(BufferEvent evt)
		{
			if(evt.getBuffer() == buffer)
				updateTitle();
			updateBuffersMenu();
		}

		public void bufferMarkersChanged(BufferEvent evt)
		{
			if(evt.getBuffer() == buffer)
				updateMarkerMenus();
		}
	
		public void bufferModeChanged(BufferEvent evt)
		{
			if(evt.getBuffer() == buffer)
				textArea.repaint();
		}
	}

	class ViewEditorListener extends EditorAdapter
	{
		public void bufferCreated(EditorEvent evt)
		{
			updateBuffersMenu();

			evt.getBuffer().addBufferListener(bufferListener);
		}
	
		public void bufferClosed(EditorEvent evt)
		{
			updateOpenRecentMenu();
	
			// XXX: should revert to 1.2 behaviour
			Buffer[] bufferArray = jEdit.getBuffers();
			if(bufferArray.length != 0)
				setBuffer(bufferArray[bufferArray.length - 1]);
	
			updateBuffersMenu();

			evt.getBuffer().removeBufferListener(bufferListener);
		}
	
		public void propertiesChanged(EditorEvent evt)
		{
			View.this.propertiesChanged();
		}
	}

	class ViewCaretListener implements CaretListener
	{
		public void caretUpdate(CaretEvent evt)
		{
			updateLineNumber(evt.getDot(),false);
		}
	}

	class ViewKeyListener extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			int keyCode = evt.getKeyCode();
			int modifiers = evt.getModifiers();
			if((modifiers & ~InputEvent.SHIFT_MASK) != 0 ||
				evt.isActionKey() || keyCode == KeyEvent.VK_TAB)
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
						new ActionEvent(View.this,
						ActionEvent.ACTION_PERFORMED,
						cmd));
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
		}
	}

	class ViewWindowListener extends WindowAdapter
	{
		public void windowClosing(WindowEvent evt)
		{
			jEdit.closeView(View.this);
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.51  1999/03/20 05:23:32  sp
 * Code cleanups
 *
 * Revision 1.50  1999/03/20 04:52:55  sp
 * Buffer-specific options panel finished, attempt at fixing OS/2 caret bug, code
 * cleanups
 *
 * Revision 1.49  1999/03/19 08:32:22  sp
 * Added a status bar to views, Escape key now works in dialog boxes
 *
 * Revision 1.48  1999/03/18 04:24:57  sp
 * HistoryTextField hacking, some other minor changes
 *
 * Revision 1.47  1999/03/17 05:32:51  sp
 * Event system bug fix, history text field updates (but it still doesn't work), code cleanups, lots of banging head against wall
 *
 * Revision 1.46  1999/03/15 03:12:34  sp
 * Fixed compile error with javac that jikes silently ignored (FUCK YOU IBM),
 * maybe some other stuff fixed too
 *
 * Revision 1.45  1999/03/14 02:22:13  sp
 * Syntax colorizing tweaks, server bug fix
 *
 * Revision 1.44  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
