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
import org.gjt.sp.jedit.syntax.SyntaxStyle;
import org.gjt.sp.jedit.syntax.Token;
import org.gjt.sp.jedit.textarea.*;

/**
 * A window that edits buffers. There is no public constructor in the
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

		TextAreaPainter painter = textArea.getPainter();

		painter.setFont(font);
		painter.setLineHighlightEnabled("on".equals(jEdit.getProperty(
			"view.lineHighlight")));
		painter.setLineHighlightColor(GUIUtilities.parseColor(
			jEdit.getProperty("view.lineHighlightColor")));
		painter.setBracketHighlightEnabled("on".equals(jEdit.getProperty(
			"view.bracketHighlight")));
		painter.setBracketHighlightColor(GUIUtilities.parseColor(
			jEdit.getProperty("view.bracketHighlightColor")));
		painter.setEOLMarkerEnabled("on".equals(jEdit.getProperty(
			"view.eolMarkers")));
		painter.setEOLMarkerColor(GUIUtilities.parseColor(
			jEdit.getProperty("view.eolMarkerColor")));
		painter.setCaretColor(GUIUtilities.parseColor(
			jEdit.getProperty("view.caretColor")));
		painter.setSelectionColor(GUIUtilities.parseColor(
			jEdit.getProperty("view.selectionColor")));
		painter.setBackground(GUIUtilities.parseColor(
			jEdit.getProperty("view.bgColor")));
		painter.setForeground(GUIUtilities.parseColor(
			jEdit.getProperty("view.fgColor")));
		painter.setBlockCaretEnabled("on".equals(jEdit.getProperty(
			"view.blockCaret")));
		painter.setCopyAreaBroken("on".equals(jEdit.getProperty(
			"view.copyAreaDisabled")));

		textArea.setCaretBlinkEnabled("on".equals(jEdit.getProperty(
			"view.caretBlink")));

		try
		{
			textArea.setElectricScroll(Integer.parseInt(jEdit
				.getProperty("view.electricBorders")));
		}
		catch(NumberFormatException nf)
		{
			textArea.setElectricScroll(0);
		}

		loadStyles();

		updateOpenRecentMenu();
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

		int currLine = textArea.getCaretLine();
		int start = textArea.getLineStartOffset(currLine);
		int numLines = textArea.getLineCount();

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
	public final Buffer getBuffer()
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

		if(buffer == null)
			throw new NullPointerException("Not supported yet");

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

		int start = Math.min(buffer.getLength(),buffer.getSavedSelStart());
		int end = Math.min(buffer.getLength(),buffer.getSavedSelEnd());
		textArea.select(start,end);

		updateMarkerMenus();
		updateTitle();
		updateLineNumber();

		Mode mode = buffer.getMode();
		if(mode != null)
			mode.enterView(this);

		focusOnTextArea();

		// Fire event
		fireViewEvent(ViewEvent.BUFFER_CHANGED,oldBuffer);
	}

	/**
	 * Sets the buffer being edited by this view.
	 * @param buffer The buffer to edit.
	 */
	public final void setBuffer(Buffer buffer)
	{
		_setBuffer(buffer);
		updateBuffersMenu();
	}

	/**
	 * Sets the focus onto the text area.
	 */
	public void focusOnTextArea()
	{
		textArea.requestFocus();
	}

	/**
	 * Returns this view's text area.
	 */
	public final JEditTextArea getTextArea()
	{
		return textArea;
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
	 * Returns this view's unique identifier (UID). UIDs are
	 * guaranteed to be unique during a jEdit session - they
	 * are not reused (unless more than 2^32 views are created,
	 * but that isn't a very realistic condition).<p>
	 *
	 * A UID can be converted back to a view with the
	 * jEdit.getView() method.
	 *
	 * @see org.gjt.sp.jedit.jEdit#getView(int)
	 */
	public final int getUID()
	{
		return uid;
	}

	/**
	 * Returns true if this view has been closed with
	 * <code>jEdit.closeView()</code>.
	 */
	public final boolean isClosed()
	{
		return closed;
	}

	/**
	 * Adds a view event listener to this view.
	 * @param listener The event listener
	 */
	public final void addViewListener(ViewListener listener)
	{
		listenerList.add(ViewListener.class,listener);
	}

	/**	
	 * Removes a view event listener from this view.
	 * @param listener The event listener
	 */
	public final void removeViewListener(ViewListener listener)
	{
		listenerList.remove(ViewListener.class,listener);
	}

	// package-private members
	View prev;
	View next;
	int uid;

	View(View view, Buffer buffer)
	{
		uid = UID++;
		listenerList = new EventListenerList();

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

		textArea = new JEditTextArea();

		// Set up the right-click popup menu
		textArea.setRightClickPopup(GUIUtilities
			.loadPopupMenu(this,"view.context"));

		// Set the input handler
		textArea.setInputHandler(jEdit.getInputHandler());

		propertiesChanged();

		if(buffer == null)
			setBuffer(bufferArray[bufferArray.length - 1]);
		else
			setBuffer(buffer);

		getContentPane().add(BorderLayout.NORTH,topToolBars);
		getContentPane().add(BorderLayout.CENTER,textArea);

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

		updateLineNumber();

		textArea.addCaretListener(new CaretHandler());
		
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
		closed = true;
		GUIUtilities.saveGeometry(this,"view");
		saveCaretInfo();
		jEdit.removeEditorListener(editorListener);
		Buffer[] bufferArray = jEdit.getBuffers();
		for(int i = 0; i < bufferArray.length; i++)
			bufferArray[i].removeBufferListener(bufferListener);
		dispose();
	}

	// private members
	private static int UID;

	private JMenu buffers;
	private JMenu openRecent;
	private JMenu clearMarker;
	private JMenu gotoMarker;
	private JMenu plugins;
	private JPopupMenu popup;
	private Hashtable bindings;
	private Hashtable currentPrefix;
	private Box topToolBars;
	private Box bottomToolBars;
	private Component toolBar;
	private JScrollPane scroller;
	private JEditTextArea textArea;
	private JLabel lineNumber;
	private JLabel hintBar;
	private Buffer buffer;
	private boolean showTip;
	private boolean showFullPath;
	private EventListenerList listenerList;
	private BufferListener bufferListener;
	private EditorListener editorListener;
	private boolean closed;

	private void fireViewEvent(int id, Buffer buffer)
	{
		ViewEvent evt = null;
		Object[] listeners = listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i-= 2)
		{
			if(listeners[i] == ViewListener.class)
			{
				if(evt == null)
					evt = new ViewEvent(id,this,buffer);
				evt.fire((ViewListener)listeners[i+1]);
			}
		}
	}

	private void loadStyles()
	{
		try
		{
			SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];

			styles[Token.COMMENT1] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.comment1"));
			styles[Token.COMMENT2] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.comment2"));
			styles[Token.KEYWORD1] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.keyword1"));
			styles[Token.KEYWORD2] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.keyword2"));
			styles[Token.KEYWORD3] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.keyword3"));
			styles[Token.LABEL] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.label"));
			styles[Token.LITERAL1] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.literal1"));
			styles[Token.LITERAL2] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.literal2"));
			styles[Token.OPERATOR] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.operator"));
			styles[Token.INVALID] = GUIUtilities.parseStyle(
				jEdit.getProperty("view.style.invalid"));

			textArea.getPainter().setStyles(styles);
		}
		catch(Exception e)
		{
			System.out.println("Error loading syntax styles:");
			e.printStackTrace();
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
			{
				textArea.getPainter().invalidateOffscreen();
				textArea.repaint();
			}
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
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.86  1999/07/16 23:45:49  sp
 * 1.7pre6 BugFree version
 *
 * Revision 1.85  1999/07/08 06:06:04  sp
 * Bug fixes and miscallaneous updates
 *
 * Revision 1.84  1999/07/05 04:38:39  sp
 * Massive batch of changes... bug fixes, also new text component is in place.
 * Have fun
 *
 * Revision 1.83  1999/06/27 04:53:16  sp
 * Text selection implemented in text area, assorted bug fixes
 *
 * Revision 1.82  1999/06/25 06:54:08  sp
 * Text area updates
 *
 * Revision 1.81  1999/06/22 06:14:39  sp
 * RMI updates, text area updates, flag to disable geometry saving
 *
 * Revision 1.80  1999/06/20 07:00:59  sp
 * Text component rewrite started
 *
 * Revision 1.79  1999/06/20 02:15:45  sp
 * Syntax coloring optimizations
 *
 * Revision 1.78  1999/06/16 03:29:59  sp
 * Added <title> tags to docs, configuration data is now stored in a
 * ~/.jedit directory, style option pane finished
 *
 * Revision 1.77  1999/06/15 05:03:54  sp
 * RMI interface complete, save all hack, views & buffers are stored as a link
 * list now
 *
 * Revision 1.76  1999/05/28 02:00:25  sp
 * SyntaxView bug fix, faq update, MiscUtilities.isURL() method added
 *
 * Revision 1.75  1999/05/27 03:09:22  sp
 * Console unbundled
 *
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
 */
