/*
 * View.java - jEdit view
 * Copyright (C) 1998, 1999, 2000 Slava Pestov
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
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.search.SearchAndReplace;
import org.gjt.sp.jedit.syntax.SyntaxStyle;
import org.gjt.sp.jedit.syntax.Token;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.util.Log;

/**
 * A window that edits buffers. There is no public constructor in the
 * View class. Views are created and destroyed by the <code>jEdit</code>
 * class.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class View extends JFrame implements EBComponent
{
	/**
	 * Displays the specified string in the status area of this view.
	 * @param str The string to display
	 */
	public void pushStatus(String str)
	{
		((StatusBar)textArea.getStatus()).pushStatus(str);
	}

	/**
	 * Displays the previous status bar message.
	 */
	public void popStatus()
	{
		((StatusBar)textArea.getStatus()).popStatus();
	}

	/**
	 * Returns the buffer being edited by this view.
	 */
	public final Buffer getBuffer()
	{
		return buffer;
	}

	/**
	 * Sets the buffer being edited by this view. This calls
	 * <code>loadIfNecessary()</code> on the buffer.
	 * @param buffer The buffer to edit.
	 */
	public void setBuffer(Buffer buffer)
	{
		if(this.buffer == buffer)
			return;

		// Ensure new buffer is valid
		buffer.loadIfNecessary(this);

		Buffer oldBuffer = this.buffer;
		if(oldBuffer != null)
		{
			saveCaretInfo();
			Mode mode = oldBuffer.getMode();
			if(mode != null)
				mode.leaveView(this);
		}
		this.buffer = buffer;

		// until we can find a better way to store caret info...
		unsplit();
		/* JEditTextArea[] textAreas = getTextAreas();
		for(int i = 0; i < textAreas.length; i++)
		{
			JEditTextArea textArea = textAreas[i]; */
			textArea.setDocument(buffer);
			((StatusBar)textArea.getStatus()).repaint();
		/* } */

		loadCaretInfo();
		updateMarkerMenus();
		updateTitle();

		Mode mode = buffer.getMode();
		if(mode != null)
			mode.enterView(this);

		// Don't fire event for the initial buffer set
		if(oldBuffer != null)
			EditBus.send(new ViewUpdate(this,ViewUpdate.BUFFER_CHANGED));

		if(bufferTabs != null)
			bufferTabs.selectBufferTab(buffer);
		updateBuffersMenu();

		focusOnTextArea();
	}

	/**
	 * Sets the focus onto the text area.
	 */
	public final void focusOnTextArea()
	{
		textArea.requestFocus();
	}

	/**
	 * Returns the view's quicksearch text field, or null if the toolbar
	 * is not showing.
	 */
	public final HistoryTextField getQuickSearch()
	{
		if(quicksearch.isShowing())
			return quicksearch;
		else
			return null;
	}

	/**
	 * Returns the view's text area.
	 */
	public final JEditTextArea getTextArea()
	{
		return textArea;
	}

	/**
	 * Splits the view.
	 * @since jEdit 2.3pre2
	 */
	public void split(int orientation)
	{
		if(splitPane == null)
		{
			saveCaretInfo();
			splitPane = new JSplitPane(orientation,textArea,
				textArea = createTextArea());
			loadCaretInfo();
			splitPane.setBorder(null);
			if(bufferTabs != null)
				bufferTabs.update();
			else
			{
				JComponent parent = (JComponent)textArea.getParent();
				parent.add(splitPane);
				parent.revalidate();
			}
		}
		else
			splitPane.setOrientation(orientation);

		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				splitPane.setDividerLocation(0.5);
				focusOnTextArea();
			}
		});
	}

	/**
	 * Unsplits the view.
	 * @since jEdit 2.3pre2
	 */
	public void unsplit()
	{
		if(splitPane != null)
		{
			JComponent parent = (JComponent)splitPane.getParent();
			parent.remove(splitPane);
			splitPane = null;
			if(bufferTabs != null)
				bufferTabs.update();
			else
				parent.revalidate();
		}

		focusOnTextArea();
	}

	/**
	 * Returns the split pane.
	 * @since jEdit 2.3pre2
	 */
	public JSplitPane getSplitPane()
	{
		return splitPane;
	}

	/**
	 * Returns all text areas.
	 * @since jEdit 2.3pre2
	 */
	public JEditTextArea[] getTextAreas()
	{
		if(splitPane == null)
		{
			JEditTextArea[] ta = { textArea };
			return ta;
		}
		else
		{
			JEditTextArea[] ta = {
				(JEditTextArea)splitPane.getLeftComponent(),
				(JEditTextArea)splitPane.getRightComponent()
			};
			return ta;
		}
	}

	/**
	 * Adds a tool bar to this view.
	 * @param toolBar The tool bar
	 */
	public void addToolBar(Component toolBar)
	{
		toolBars.add(toolBar);
		getRootPane().revalidate();
	}

	/**
	 * Removes a tool bar from this view.
	 * @param toolBar The tool bar
	 */
	public void removeToolBar(Component toolBar)
	{
		toolBars.remove(toolBar);
		getRootPane().revalidate();
	}

	/**
	 * Saves the caret information to the current buffer.
	 */
	public void saveCaretInfo()
	{
		buffer.putProperty(Buffer.SELECTION_START,new Integer(
			textArea.getSelectionStart()));
		buffer.putProperty(Buffer.SELECTION_END,new Integer(
			textArea.getSelectionEnd()));
		buffer.putProperty(Buffer.SELECTION_RECT,new Boolean(
			textArea.isSelectionRectangular()));
		buffer.putProperty(Buffer.SCROLL_VERT,new Integer(
			textArea.getFirstLine()));
		buffer.putProperty(Buffer.SCROLL_HORIZ,new Integer(
			textArea.getHorizontalOffset()));
		buffer.putProperty(Buffer.OVERWRITE,new Boolean(
			textArea.isOverwriteEnabled()));
	}

	/**
	 * Loads the caret information from the curret buffer.
	 */
	public void loadCaretInfo()
	{
		Integer start = (Integer)buffer.getProperty(Buffer.SELECTION_START);
		Integer end = (Integer)buffer.getProperty(Buffer.SELECTION_END);
		Boolean rectSel = (Boolean)buffer.getProperty(Buffer.SELECTION_RECT);
		Integer firstLine = (Integer)buffer.getProperty(Buffer.SCROLL_VERT);
		Integer horizontalOffset = (Integer)buffer.getProperty(Buffer.SCROLL_HORIZ);
		Boolean overwrite = (Boolean)buffer.getProperty(Buffer.OVERWRITE);

		if(start != null && end != null
			&& firstLine != null && horizontalOffset != null)
		{
			textArea.select(Math.min(start.intValue(),
				buffer.getLength()),
				Math.min(end.intValue(),
				buffer.getLength()));
			textArea.setFirstLine(firstLine.intValue());
			textArea.setHorizontalOffset(horizontalOffset.intValue());
		}

		if(rectSel != null && overwrite != null)
		{
			textArea.setSelectionRectangular(rectSel.booleanValue());
			textArea.setOverwriteEnabled(overwrite.booleanValue());
		}
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
	 * Shows the wait cursor.
	 */
	public void showWaitCursor()
	{
		if(waitCount++ == 0)
		{
			Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
			setCursor(cursor);
			JEditTextArea[] textAreas = getTextAreas();
			for(int i = 0; i < textAreas.length; i++)
			{
				JEditTextArea textArea = textAreas[i];
				textArea.getPainter().setCursor(cursor);
			}
		}
	}

	/**
	 * Hides the wait cursor.
	 */
	public void hideWaitCursor()
	{
		if(waitCount > 0)
			waitCount--;

		if(waitCount == 0)
		{
			Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
			setCursor(cursor);
			cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
			JEditTextArea[] textAreas = getTextAreas();
			for(int i = 0; i < textAreas.length; i++)
			{
				JEditTextArea textArea = textAreas[i];
				textArea.getPainter().setCursor(cursor);
			}
		}
	}

	/**
	 * Returns the next view in the list.
	 */
	public final View getNext()
	{
		return next;
	}

	/**
	 * Returns the previous view in the list.
	 */
	public final View getPrev()
	{
		return prev;
	}

	public void handleMessage(EBMessage msg)
	{
		if(msg instanceof PropertiesChanged)
			propertiesChanged();
		else if(msg instanceof MacrosChanged)
			updateMacrosMenu();
		else if(msg instanceof SearchSettingsChanged)
		{
			ignoreCase.repaint();
			regexp.repaint();
			multifile.repaint();
		}
		else if(msg instanceof BufferUpdate)
			handleBufferUpdate((BufferUpdate)msg);
	}

	public JMenu getMenu(String name)
	{
		if(name.equals("buffers"))
			return buffers;
		else if(name.equals("recent-files"))
			return recent;
		else if(name.equals("current-directory"))
			return currentDirectory;
		else if(name.equals("clear-marker"))
			return clearMarker;
		else if(name.equals("goto-marker"))
			return gotoMarker;
		else if(name.equals("macros"))
			return macros;
		else if(name.equals("plugins"))
			return plugins;
		else if(name.equals("help-menu"))
			return help;
		else
			return null;
	}

	// package-private members
	View prev;
	View next;

	View(View view, Buffer buffer)
	{
		setIconImage(GUIUtilities.getEditorIcon());

		// Dynamic menus
		buffers = GUIUtilities.loadMenu(this,"buffers");
		recent = GUIUtilities.loadMenu(this,"recent-files");
		currentDirectory = new CurrentDirectoryMenu(this);
		clearMarker = GUIUtilities.loadMenu(this,"clear-marker");
		gotoMarker = GUIUtilities.loadMenu(this,"goto-marker");
		macros = GUIUtilities.loadMenu(this,"macros");
		help = GUIUtilities.loadMenu(this,"help-menu");
		plugins = GUIUtilities.loadMenu(this,"plugins");
		updateMacrosMenu();
		updateHelpMenu();
		updatePluginsMenu();

		EditBus.addToBus(this);

		setJMenuBar(GUIUtilities.loadMenuBar(this,"view.mbar"));

		toolBars = new Box(BoxLayout.Y_AXIS);

		quicksearch = new HistoryTextField("find");
		Dimension dim = quicksearch.getPreferredSize();
		dim.width = Integer.MAX_VALUE;
		quicksearch.setMaximumSize(dim);
		quicksearch.addActionListener(new ActionHandler());
		quicksearch.addKeyListener(new KeyHandler());

		textArea = createTextArea();

		if(view == null)
			propertiesChanged();
		else
			loadPropertiesFromView(view);

		if(buffer == null)
			setBuffer(jEdit.getFirstBuffer());
		else
			setBuffer(buffer);

		getContentPane().add(BorderLayout.NORTH,toolBars);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowHandler());
	}

	void close()
	{
		closed = true;
		GUIUtilities.saveGeometry(this,"view");
		saveCaretInfo();
		EditBus.removeFromBus(this);
		dispose();
	}

	// private members
	private Buffer buffer;
	private boolean closed;

	private JMenu buffers;
	private JMenu recent;
	private JMenu currentDirectory;
	private JMenu clearMarker;
	private JMenu gotoMarker;
	private JMenu macros;
	private JMenu plugins;
	private JMenu help;

	private Box toolBars;
	private JToolBar toolBar;
	private HistoryTextField quicksearch;

	// we need to keep these instances so that we can call repaint()
	// when search & replace settings change
	private EnhancedButton ignoreCase;
	private EnhancedButton regexp;
	private EnhancedButton multifile;

	private BufferTabs bufferTabs;
	private JSplitPane splitPane;
	private JEditTextArea textArea;

	private int waitCount;

	private boolean showFullPath;

	/**
	 * Reloads various settings from the properties.
	 */
	private void propertiesChanged()
	{
		loadToolBar();
		initBufferTabs();

		showFullPath = "on".equals(jEdit.getProperty("view.showFullPath"));
		if(buffer != null)
			updateTitle();

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

		JEditTextArea[] textAreas = getTextAreas();
		for(int i = 0; i < textAreas.length; i++)
		{
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
			painter.setEOLMarkersPainted("on".equals(jEdit.getProperty(
				"view.eolMarkers")));
			painter.setInvalidLinesPainted("on".equals(jEdit.getProperty(
				"view.paintInvalid")));
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

			textArea.setCaretBlinkEnabled("on".equals(jEdit.getProperty(
				"view.caretBlink")));

			textArea.putClientProperty(InputHandler.SMART_HOME_END_PROPERTY,
				new Boolean("yes".equals(jEdit.getProperty("view.homeEnd"))));
			try
			{
				textArea.setElectricScroll(Integer.parseInt(jEdit
					.getProperty("view.electricBorders")));
			}
			catch(NumberFormatException nf)
			{
				textArea.setElectricScroll(0);
			}
		}

		loadStyles();

		updateRecentMenu();
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

			JEditTextArea[] textAreas = getTextAreas();
			for(int i = 0; i < textAreas.length; i++)
			{
				JEditTextArea textArea = textAreas[i];
				textArea.getPainter().setStyles(styles);
			}
		}
		catch(Exception e)
		{
			Log.log(Log.ERROR,this,e);
		}
	}

	private void loadPropertiesFromView(View view)
	{
		loadToolBar();
		initBufferTabs();

		showFullPath = view.showFullPath;

		JEditTextArea[] textAreas = getTextAreas();
		for(int i = 0; i < textAreas.length; i++)
		{
			JEditTextArea textArea = textAreas[i];
			TextAreaPainter painter = view.textArea.getPainter();
			TextAreaPainter myPainter = textArea.getPainter();
			myPainter.setFont(painter.getFont());
			myPainter.setLineHighlightEnabled(painter.isLineHighlightEnabled());
			myPainter.setLineHighlightColor(painter.getLineHighlightColor());
			myPainter.setBracketHighlightEnabled(painter.isBracketHighlightEnabled());
			myPainter.setBracketHighlightColor(painter.getBracketHighlightColor());
			myPainter.setEOLMarkersPainted(painter.getEOLMarkersPainted());
			myPainter.setInvalidLinesPainted(painter.getInvalidLinesPainted());
			myPainter.setEOLMarkerColor(painter.getEOLMarkerColor());
			myPainter.setCaretColor(painter.getCaretColor());
			myPainter.setSelectionColor(painter.getSelectionColor());
			myPainter.setBackground(painter.getBackground());
			myPainter.setForeground(painter.getForeground());
			myPainter.setBlockCaretEnabled(painter.isBlockCaretEnabled());

			textArea.setCaretBlinkEnabled(view.textArea.isCaretBlinkEnabled());
			textArea.putClientProperty(InputHandler.SMART_HOME_END_PROPERTY,
				view.textArea.getClientProperty(InputHandler.SMART_HOME_END_PROPERTY));
			textArea.setElectricScroll(view.textArea.getElectricScroll());

			myPainter.setStyles(painter.getStyles());
		}

		updateRecentMenu();
	}

	private void loadToolBar()
	{
		if("on".equals(jEdit.getProperty("view.showToolbar")))
		{
			if(toolBar == null)
			{
				toolBar = GUIUtilities.loadToolBar("view.toolbar");
				toolBar.addSeparator();
				toolBar.add(new JLabel(jEdit.getProperty("view.quicksearch")));
				Box box = new Box(BoxLayout.Y_AXIS);
				box.add(Box.createVerticalGlue());
				box.add(quicksearch);
				box.add(Box.createVerticalGlue());
				toolBar.add(box);
				toolBar.addSeparator();

				toolBar.add(ignoreCase = GUIUtilities.loadToolButton(
					"ignore-case"));
				toolBar.add(regexp = GUIUtilities.loadToolButton(
					"regexp"));
				toolBar.add(multifile = GUIUtilities.loadToolButton(
					"multifile-search"));
			}
			if(toolBar.getParent() == null)
				addToolBar(toolBar);
		}
		else if(toolBar != null)
		{
			removeToolBar(toolBar);
			toolBar = null;
		}
	}

	private JEditTextArea createTextArea()
	{
		JEditTextArea textArea = new JEditTextArea();

		// Add the line number display
		textArea.add(JEditTextArea.LEFT_OF_SCROLLBAR,new StatusBar(textArea));

		// Set up the right-click popup menu
		textArea.setRightClickPopup(GUIUtilities
			.loadPopupMenu(this,"view.context"));

		textArea.setInputHandler(jEdit.getInputHandler().copy());
		textArea.addCaretListener(new CaretHandler());
		textArea.addFocusListener(new FocusHandler());

		if(buffer != null)
			textArea.setDocument(buffer);

		return textArea;
	}

	private void initBufferTabs()
	{
		Container parent;

		if(bufferTabs != null)
			parent = bufferTabs.getParent();
		else if(splitPane != null)
			parent = splitPane.getParent();
		else
			parent = textArea.getParent();

		if(parent == null)
			parent = getContentPane();

		Container comp;
		if(splitPane == null)
			comp = textArea;
		else
			comp = splitPane;

		if("on".equals(jEdit.getProperty("view.showBufferTabs")))
		{
			if(bufferTabs == null)
			{
				bufferTabs = new BufferTabs(this);
				parent.remove(comp);

				// BorderLayout adds to center by default,
				// but this will also work with other layouts.
				parent.add(bufferTabs);
			}
		}
		else
		{
			if(bufferTabs != null)
			{
				parent.remove(bufferTabs);
				bufferTabs = null;
				parent.add(comp);
			}
			else if(comp.getParent() == null)
			{
				parent.add(comp);
			}
		}

		getRootPane().revalidate();
		getRootPane().repaint();
	}

	/**
	 * Updates the title bar and read only status of the text
	 * area.
	 */
	private void updateTitle()
	{
		Object[] args = { ((showFullPath && !buffer.isNewFile())
			? buffer.getPath() : buffer.getName()),
			new Integer(buffer.isReadOnly() ? 1 : 0),
			new Integer(buffer.isDirty() ? 1: 0),
			new Integer(buffer.isNewFile() ? 1: 0)};
		setTitle(jEdit.getProperty("view.title",args));

		JEditTextArea[] textAreas = getTextAreas();
		for(int i = 0; i < textAreas.length; i++)
		{
			JEditTextArea textArea = textAreas[i];
			textArea.setEditable(!buffer.isReadOnly());
		}
	}

	/**
	 * Recreates the buffers menu.
	 */
	private void updateBuffersMenu()
	{
		if(jEdit.getBufferCount() == 0)
		{
			// if this becomes zero, it is guaranteed that
			// a new untitled buffer will open again. So
			// we just ignore this and update the menu
			// when the untitled buffer comes in
			return;
		}

		// Because the buffers menu contains normal items as
		// well as dynamically-generated stuff, we are careful
		// to only remove the dynamic crap here...
		for(int i = buffers.getMenuComponentCount() - 1; i >= 0; i--)
		{
			if(buffers.getMenuComponent(i) instanceof JSeparator)
				break;
			else
				buffers.remove(i);
		}

		Buffer[] bufferArray = jEdit.getBuffers();
		ButtonGroup grp = new ButtonGroup();
		for(int i = 0; i < bufferArray.length; i++)
		{
			Buffer b = bufferArray[i];
			String name = b.getPath();
			Object[] args = { name,
				new Integer(b.isReadOnly() ? 1: 0),
				new Integer(b.isDirty() ? 1 : 0),
				new Integer(b.isNewFile() ? 1 : 0) };
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
	 * Recreates the recent menu.
	 */
	private void updateRecentMenu()
	{
		if(recent.getMenuComponentCount() != 0)
			recent.removeAll();
		EditAction action = jEdit.getAction("open-path");
		String[] recentArray = jEdit.getRecent();
		if(recentArray.length == 0)
		{
			recent.add(GUIUtilities.loadMenuItem(this,"no-recent"));
			return;
		}
		for(int i = 0; i < recentArray.length; i++)
		{
			String path = recentArray[i];
			EnhancedMenuItem menuItem = new EnhancedMenuItem(path,
				null,action,path);
			recent.add(menuItem);
		}
	}

	/**
	 * Recreates the goto marker and clear marker menus.
	 */
	private void updateMarkerMenus()
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
			EnhancedMenuItem menuItem = new EnhancedMenuItem(name,
				null,clearMarkerAction,name);
			clearMarker.add(menuItem);
			menuItem = new EnhancedMenuItem(name,null,
				gotoMarkerAction,name);
			gotoMarker.add(menuItem);
		}
	}

	/**
	 * Recreates the macros menu.
	 */
	private void updateMacrosMenu()
	{
		// Because the macros menu contains normal items as
		// well as dynamically-generated stuff, we are careful
		// to only remove the dynamic crap here...
		for(int i = macros.getMenuComponentCount() - 1; i >= 0; i--)
		{
			if(macros.getMenuComponent(i) instanceof JSeparator)
				break;
			else
				macros.remove(i);
		}

		int count = macros.getMenuComponentCount();

		Vector macroVector = Macros.getMacros();
		createMacrosMenu(macros,macroVector,0);

		if(count == macros.getMenuComponentCount())
			macros.add(GUIUtilities.loadMenuItem(this,"no-macros"));
	}

	private void createMacrosMenu(JMenu menu, Vector vector, int start)
	{
		EditAction action = jEdit.getAction("play-macro");

		for(int i = start; i < vector.size(); i++)
		{
			Object obj = vector.elementAt(i);
			if(obj instanceof Macros.Macro)
			{
				Macros.Macro macro = (Macros.Macro)obj;
				String label = macro.label;
				String path = macro.path;
				EnhancedMenuItem menuItem = new EnhancedMenuItem(
					label,jEdit.getProperty(macro.name + ".shortcut"),
					action,path);
				menu.add(menuItem);
			}
			else if(obj instanceof Vector)
			{
				Vector subvector = (Vector)obj;
				String name = (String)subvector.elementAt(0);
				JMenu submenu = new JMenu(name);
				createMacrosMenu(submenu,subvector,1);
				if(submenu.getMenuComponentCount() == 0)
				{
					submenu.add(GUIUtilities.loadMenuItem(
						this,"no-macros"));
				}
				menu.add(submenu);
			}
		}
	}

	/**
	 * Recreates the plugins menu.
	 */
	private void updatePluginsMenu()
	{
		if(plugins.getMenuComponentCount() != 0)
			plugins.removeAll();

		// Query plugins for menu items
		Vector pluginMenus = new Vector();
		Vector pluginMenuItems = new Vector();

		EditPlugin[] pluginArray = jEdit.getPlugins();
		for(int i = 0; i < pluginArray.length; i++)
		{
			try
			{
				pluginArray[i].createMenuItems(this,pluginMenus,
					pluginMenuItems);
			}
			catch(Throwable t)
			{
				Log.log(Log.ERROR,this,"Error creating menu items"
					+ " for plugin");
				Log.log(Log.ERROR,this,t);
			}
		}

		if(pluginMenus.isEmpty() && pluginMenuItems.isEmpty())
		{
			plugins.add(GUIUtilities.loadMenuItem(this,"no-plugins"));
			return;
		}

		// Sort them
		MenuItemCompare comp = new MenuItemCompare();
		MiscUtilities.quicksort(pluginMenus,comp);
		MiscUtilities.quicksort(pluginMenuItems,comp);

		for(int i = 0; i < pluginMenus.size(); i++)
			plugins.add((JMenu)pluginMenus.elementAt(i));

		if(!pluginMenus.isEmpty() && !pluginMenuItems.isEmpty())
			plugins.addSeparator();

		for(int i = 0; i < pluginMenuItems.size(); i++)
			plugins.add((JMenuItem)pluginMenuItems.elementAt(i));
	}

	private void updateHelpMenu()
	{
		EditAction action = jEdit.getAction("help");

		EditPlugin[] plugins = jEdit.getPlugins();
		for(int i = 0; i < plugins.length; i++)
		{
			EditPlugin plugin = plugins[i];
			String name = plugin.getClass().getName();

			String label = jEdit.getProperty("plugin." + name + ".name");
			String version = jEdit.getProperty("plugin." + name + ".version");
			String docs = jEdit.getProperty("plugin." + name + ".docs");
			if(docs != null)
			{
				java.net.URL docsURL = plugin.getClass().getResource(docs);
				if(label != null && version != null && docsURL != null)
				{
					help.add(new EnhancedMenuItem(label + " ("
						+ version + ")",null,action,
						docsURL.toString()));
				}
			}
		}
	}

	private static class MenuItemCompare implements MiscUtilities.Compare
	{
		public int compare(Object obj1, Object obj2)
		{
			return ((JMenuItem)obj1).getText().compareTo(
				((JMenuItem)obj2).getText());
		}
	}

	private void handleBufferUpdate(BufferUpdate msg)
	{
		Buffer _buffer = msg.getBuffer();
		if(msg.getWhat() == BufferUpdate.CREATED)
		{
			updateBuffersMenu();

			if(bufferTabs != null)
				bufferTabs.addBufferTab(_buffer);
		}
		else if(msg.getWhat() == BufferUpdate.CLOSED)
		{
			if(_buffer == buffer)
			{
				Buffer newBuffer = _buffer.getPrev();
				if(newBuffer != null && !newBuffer.isClosed())
					setBuffer(newBuffer);
				else if(jEdit.getBufferCount() != 0)
					setBuffer(jEdit.getFirstBuffer());
			}

			updateRecentMenu();
			updateBuffersMenu();

			if(bufferTabs != null)
				bufferTabs.removeBufferTab(_buffer);
		}
		else if(msg.getWhat() == BufferUpdate.DIRTY_CHANGED)
		{
			if(_buffer == buffer)
				updateTitle();
			updateBuffersMenu();

			if(bufferTabs != null)
				bufferTabs.updateBufferTab(_buffer);
		}
		else if(msg.getWhat() == BufferUpdate.MARKERS_CHANGED)
		{
			if(_buffer == buffer)
				updateMarkerMenus();
		}
		else if(msg.getWhat() == BufferUpdate.MODE_CHANGED)
		{
			if(_buffer == buffer)
			{
				JEditTextArea[] textAreas = getTextAreas();
				for(int i = 0; i < textAreas.length; i++)
				{
					JEditTextArea textArea = textAreas[i];
					textArea.getPainter().repaint();
				}
			}
		}
	}

	// event listeners
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			if(evt.getSource() == quicksearch)
			{
				String text = quicksearch.getText();
				if(text != null && text.length() != 0)
				{
					quicksearch.addCurrentToHistory();
					quicksearch.setText(null);
					SearchAndReplace.setSearchString(text);
					SearchAndReplace.find(View.this);
					focusOnTextArea();
				}
				else
					new SearchDialog(View.this,null);
			}
		}
	}

	class CaretHandler implements CaretListener
	{
		public void caretUpdate(CaretEvent evt)
		{
			JEditTextArea textArea = (JEditTextArea)evt.getSource();
			((StatusBar)textArea.getStatus()).repaint();
		}
	}

	class FocusHandler implements FocusListener
	{
		public void focusGained(FocusEvent evt)
		{
			textArea = (JEditTextArea)evt.getSource();
		}

		public void focusLost(FocusEvent evt)
		{
		}
	}

	class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			if(evt.getSource() == quicksearch
				&& evt.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				focusOnTextArea();
			}
		}
	}

	class WindowHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent evt)
		{
			jEdit.closeView(View.this);
		}
	}

	class StatusBar extends JComponent
	{
		Stack status;
		JEditTextArea textArea;

		StatusBar(JEditTextArea textArea)
		{
			status = new Stack();

			StatusBar.this.textArea = textArea;
			StatusBar.this.setDoubleBuffered(true);
			StatusBar.this.setFont(UIManager.getFont("Label.font"));
			StatusBar.this.setForeground(UIManager.getColor("Label.foreground"));
			StatusBar.this.setBackground(UIManager.getColor("Label.background"));
		}

		void pushStatus(String str)
		{
			status.push(str);
			StatusBar.this.repaint();
		}

		void popStatus()
		{
			if(status.isEmpty())
				return;

			status.pop();
			StatusBar.this.repaint();
		}


		public void paint(Graphics g)
		{
			FontMetrics fm = g.getFontMetrics();

			int dot = textArea.getCaretPosition();

			int currLine = StatusBar.this.textArea.getCaretLine();
			int start = StatusBar.this.textArea.getLineStartOffset(currLine);
			int numLines = StatusBar.this.textArea.getLineCount();

			String str;
			if(status.isEmpty())
			{
				str = ("col " + ((dot - start) + 1) + " line "
					+ (currLine + 1) + "/"
					+ numLines + " "
					+ (((currLine + 1) * 100) / numLines) + "%");
			}
			else
				str = (String)status.peek();

			g.drawString(str,0,(StatusBar.this.getHeight()
				+ fm.getAscent()) / 2);
		}

		public Dimension getPreferredSize()
		{
			return new Dimension(200,0);
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.129  2000/01/29 03:27:20  sp
 * Split window functionality added
 *
 * Revision 1.128  2000/01/29 01:56:51  sp
 * Buffer tabs updates, some other stuff
 *
 * Revision 1.127  2000/01/28 09:24:16  sp
 * Buffer tabs updated (uses better impl == less bugs)
 *
 * Revision 1.126  2000/01/28 00:20:58  sp
 * Lots of stuff
 *
 * Revision 1.125  2000/01/22 23:36:42  sp
 * Improved file close behaviour
 *
 * Revision 1.124  2000/01/21 00:35:29  sp
 * Various updates
 *
 * Revision 1.123  2000/01/17 07:03:41  sp
 * File->Current Dir menu, other stuff
 *
 * Revision 1.122  2000/01/16 06:09:27  sp
 * Bug fixes
 *
 * Revision 1.121  2000/01/15 04:15:51  sp
 * Help menu updates, misc. GUI updates
 *
 * Revision 1.120  2000/01/14 04:23:50  sp
 * 2.3pre2 stuff
 *
 * Revision 1.119  1999/12/22 06:36:40  sp
 * 2.3pre1 stuff
 *
 */
