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
	 * Tool bar position at the top of the view.
	 */
	public static final int TOP = 0;

	/**
	 * Tool bar position at the bottom of the view.
	 */
	public static final int BOTTOM = 1;

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
				addToolBar(TOP,toolBar);
				validate();
			}
		}
		else if(toolBar != null)
		{
			removeToolBar(toolBar);
			toolBar = null;
			validate();
		}

		showFullPath = "on".equals(jEdit.getProperty("view.showFullPath"));

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

		textArea.updateHighlighters();
		console.propertiesChanged();
	}
	
	/**
	 * Recreates the buffers menu.
	 */
	public void updateBuffersMenu()
	{
		if(buffers.getMenuComponentCount() != 0)
			buffers.removeAll();

		buffers.add(GUIUtilities.loadMenuItem(this,"prev-buffer"));
		buffers.add(GUIUtilities.loadMenuItem(this,"next-buffer"));
		buffers.addSeparator();

		ButtonGroup grp = new ButtonGroup();
		Buffer[] bufferArray = jEdit.getBuffers();
		for(int i = 0; i < bufferArray.length; i++)
		{
			Buffer b = bufferArray[i];
			String name = b.getPath();
			Object[] args = { (b.isNewFile() ? b.getName() : name),
				new Integer(b.isReadOnly() ? 1: 0),
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
		EditAction action = jEdit.getAction("open-path");
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
		EditAction clearMarkerAction = jEdit.getAction("clear-marker");
		EditAction gotoMarkerAction = jEdit.getAction("goto-marker");
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

		String[] pluginMenus = jEdit.getPluginMenus();
		EditAction[] pluginArray = jEdit.getPluginActions();

		if(pluginMenus.length == 0 && pluginArray.length == 0)
		{
			plugins.add(GUIUtilities.loadMenuItem(this,"no-plugins"));
			return;
		}

		for(int i = 0; i < pluginMenus.length; i++)
		{
			String menu = pluginMenus[i];
			plugins.add(GUIUtilities.loadMenu(this,menu));
		}

		if(pluginMenus.length != 0 && pluginArray.length != 0)
		{
			plugins.addSeparator();
		}

		for(int i = 0; i < pluginArray.length; i++)
		{
			String action = (String)pluginArray[i].getName();
			JMenuItem mi = GUIUtilities.loadMenuItem(this,action);
			plugins.add(mi);
		}
	}

	/**
	 * Updates the line number indicator.
	 */
	public void updateLineNumber()
	{
		int dot = textArea.getCaretPosition();

		Element map = buffer.getDefaultRootElement();
		int currLine = map.getElementIndex(dot);
		Element lineElement = map.getElement(currLine);
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset();
		int numLines = map.getElementCount();
		Object[] args = { new Integer((dot - start) + 1),
			new Integer(currLine + 1),
			new Integer(numLines),
			new Integer(((currLine + 1) * 100) / numLines) };
		lineNumber.setText(jEdit.getProperty("view.lineNumber",args));
	}

	/**
	 * Updates the title bar and read only status of the text
	 * area.
	 */
	public void updateTitle()
	{
		Object[] args = { ((showFullPath && !buffer.isNewFile())
			? buffer.getPath() : buffer.getName()),
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
	 * Sets the buffer being edited by this view, without updating
	 * the `Buffers' menu. This is for internal use by the
	 * select-buffer action only, don't call this method in other
	 * situations.
	 * @param buffer The buffer to edit.
	 */
	public void _setBuffer(Buffer buffer)
	{
		if(this.buffer == buffer)
			return;

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
		updateLineNumber();

		Mode mode = buffer.getMode();
		if(mode != null)
			mode.enterView(this);

		focusOnTextArea();

		// Fire event
		fireViewEvent(new ViewEvent(ViewEvent.BUFFER_CHANGED,this,
			oldBuffer));
	}

	/**
	 * Sets the buffer being edited by this view.
	 * @param buffer The buffer to edit.
	 */
	public void setBuffer(Buffer buffer)
	{
		_setBuffer(buffer);
		updateBuffersMenu();
	}

	/**
	 * Sets the focus onto the text area.
	 */
	public void focusOnTextArea()
	{
		/* Focus on the scroll pane */
		javax.swing.FocusManager.getCurrentManager().focusNextComponent(
			textArea.getParent());
	}

	/**
	 * Returns this view's text area.
	 */
	public JEditTextArea getTextArea()
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
		if(value <= min)
		{
			int lastLocation = splitter.getLastDividerLocation();
			if(lastLocation > min)
				splitter.setDividerLocation(lastLocation);
			else
				splitter.resetToPreferredSizes();
			console.getCommandField().requestFocus();
		}
		else
		{
			splitter.setDividerLocation(0.0);
			focusOnTextArea();
		}
	}

	/**
	 * Shows the view's console if it's hidden.
	 */
	public void showConsole()
	{
		int value = splitter.getDividerLocation();
		int min = splitter.getMinimumDividerLocation();
		if(value <= min)
		{
			int lastLocation = splitter.getLastDividerLocation();
			if(lastLocation > min)
				splitter.setDividerLocation(lastLocation);
			else
				splitter.setDividerLocation(min);
		}

		console.getCommandField().requestFocus();
	}

	/**
	 * Adds a tool bar to this view.
	 * @param pos The tool bar's position, either <code>TOP</code>
	 * or <code>BOTTOM</code>.
	 * @param toolBar The tool bar
	 */
	public void addToolBar(int pos, Component toolBar)
	{
		if(pos == TOP)
			topToolBars.add(toolBar);
		else if(pos == BOTTOM)
			bottomToolBars.add(toolBar);
		invalidate();
		validate();
	}

	/**
	 * Removes a tool bar from this view.
	 * @param toolBar The tool bar
	 */
	public void removeToolBar(Component toolBar)
	{
		topToolBars.remove(toolBar);
		bottomToolBars.remove(toolBar);
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

		jEdit.addEditorListener(editorListener = new EditorHandler());
		bufferListener = new BufferHandler();

		Buffer[] bufferArray = jEdit.getBuffers();
		for(int i = 0; i < bufferArray.length; i++)
		{
			bufferArray[i].addBufferListener(bufferListener);
		}

		// Register indentation keys
		addKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),
			"indent-on-enter");
                addKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,0),
			"indent-on-tab");

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

		setJMenuBar(GUIUtilities.loadMenubar(this,"view.mbar"));

		topToolBars = new Box(BoxLayout.Y_AXIS);
		bottomToolBars = new Box(BoxLayout.Y_AXIS);

		console = new Console(this);

		textArea = new JEditTextArea();
		textArea.setContextMenu(GUIUtilities.loadPopupMenu(this,
			"view.context"));
		scroller = new JScrollPane(textArea,ScrollPaneConstants
			.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants
			.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setMinimumSize(new Dimension(0,0));
		
		propertiesChanged();

		if(buffer == null)
			setBuffer(bufferArray[bufferArray.length - 1]);
		else
			setBuffer(buffer);

		splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			console,scroller);
		splitter.setOneTouchExpandable(true);

		FontMetrics fm = getToolkit().getFontMetrics(textArea
			.getFont());

		splitter.setPreferredSize(new Dimension(81 * fm.charWidth('m'),
			26 * fm.getHeight()));

		getContentPane().add(BorderLayout.NORTH,topToolBars);
		getContentPane().add(BorderLayout.CENTER,splitter);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.CENTER,bottomToolBars);
		panel.add(BorderLayout.SOUTH,status);
		getContentPane().add(BorderLayout.SOUTH,panel);

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

		try
		{
			splitter.setDividerLocation(Integer
				.parseInt(jEdit.getProperty(
				"view.divider")));
		}
		catch(Exception e)
		{
			splitter.setDividerLocation(0.0);
		}

		updateLineNumber();

		textArea.addKeyListener(new KeyHandler());
		textArea.addCaretListener(new CaretHandler());
		addKeyListener(new KeyHandler());
		addWindowListener(new WindowHandler());
		
		show();

		focusOnTextArea();
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
		jEdit.setProperty("view.divider",String.valueOf(location));

		console.stop();

		saveCaretInfo();

		jEdit.removeEditorListener(editorListener);

		Buffer[] bufferArray = jEdit.getBuffers();
		for(int i = 0; i < bufferArray.length; i++)
			bufferArray[i].removeBufferListener(bufferListener);
	}

	// private members
	private JMenu buffers;
	private JMenu openRecent;
	private JMenu clearMarker;
	private JMenu gotoMarker;
	private JMenu plugins;
	private Hashtable bindings;
	private Hashtable currentPrefix;
	private Box topToolBars;
	private Box bottomToolBars;
	private Component toolBar;
	private JScrollPane scroller;
	private JEditTextArea textArea;
	private Console console;
	private JSplitPane splitter;
	private JLabel lineNumber;
	private JLabel hintBar;
	private Buffer buffer;
	private boolean showTip;
	private boolean showFullPath;
	private EventMulticaster multicaster;
	private BufferListener bufferListener;
	private EditorListener editorListener;

	private void handleKeyEvent(KeyEvent evt)
	{
		int keyCode = evt.getKeyCode();
		int modifiers = evt.getModifiers();
		if((modifiers & ~InputEvent.SHIFT_MASK) != 0
			|| evt.isActionKey()
			|| keyCode == KeyEvent.VK_TAB
			|| keyCode == KeyEvent.VK_ENTER)
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
				EditAction action = jEdit.getAction(s);
				if(action == null)
				{
					System.out.println("Invalid key"
						+ " binding: " + s);
					currentPrefix = bindings;
					evt.consume();
					return;
				}
				jEdit.getAction(s).actionPerformed(
					new ActionEvent(this,
					ActionEvent.ACTION_PERFORMED,cmd));
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

	// event listeners
	class BufferHandler implements BufferListener
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

	class EditorHandler extends EditorAdapter
	{
		public void bufferCreated(EditorEvent evt)
		{
			updateBuffersMenu();
			
			evt.getBuffer().addBufferListener(bufferListener);
		}
	
		public void bufferClosed(EditorEvent evt)
		{
			Buffer buf = evt.getBuffer();

			buf.removeBufferListener(bufferListener);

			if(buf == buffer)
			{
				Buffer[] bufferArray = jEdit.getBuffers();
				if(bufferArray.length != 0)
					_setBuffer(bufferArray[bufferArray.length - 1]);
			}

			updateOpenRecentMenu();
			updateBuffersMenu();
		}
	
		public void propertiesChanged(EditorEvent evt)
		{
			View.this.propertiesChanged();
		}
	}

	class CaretHandler implements CaretListener
	{
		public void caretUpdate(CaretEvent evt)
		{
			updateLineNumber();
		}
	}

	class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			handleKeyEvent(evt);
		}
	}

	class WindowHandler extends WindowAdapter
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
 * Revision 1.74  1999/05/20 06:26:32  sp
 * Find selection update
 *
 * Revision 1.73  1999/05/18 07:26:40  sp
 * HelpViewer cursor tweak, minor view bug fix
 *
 * Revision 1.72  1999/05/14 04:56:15  sp
 * Docs updated, default: fix in C/C++/Java mode, full path in title bar toggle
 *
 * Revision 1.71  1999/05/08 00:13:00  sp
 * Splash screen change, minor documentation update, toolbar API fix
 *
 * Revision 1.70  1999/05/04 04:51:25  sp
 * Fixed HistoryTextField for Swing 1.1.1
 *
 * Revision 1.69  1999/05/03 08:28:14  sp
 * Documentation updates, key binding editor, syntax text area bug fix
 *
 * Revision 1.68  1999/05/03 04:28:01  sp
 * Syntax colorizing bug fixing, console bug fix for Swing 1.1.1
 *
 * Revision 1.67  1999/05/02 00:07:21  sp
 * Syntax system tweaks, console bugfix for Swing 1.1.1
 *
 * Revision 1.66  1999/04/25 03:39:37  sp
 * Documentation updates, console updates, history text field updates
 *
 * Revision 1.65  1999/04/24 01:55:28  sp
 * MiscUtilities.constructPath() bug fixed, event system bug(s) fix
 *
 * Revision 1.64  1999/04/23 07:35:10  sp
 * History engine reworking (shared history models, history saved to
 * .jedit-history)
 *
 */
