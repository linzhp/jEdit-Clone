/*
 * JEditTextArea.java - jEdit's text component
 * Copyright (C) 1999 Slava Pestov
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 */

package org.gjt.sp.jedit.textarea;

import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.*;
import org.gjt.sp.jedit.syntax.*;

/**
 * jEdit's text area component.
 * @author Slava Pestov
 * @version $Id$
 */
public class JEditTextArea extends Container
{
	/**
	 * Creates a new JEditTextArea with the default settings.
	 */
	public JEditTextArea()
	{
		this(TextAreaDefaults.getDefaults());
	}

	/**
	 * Creates a new JEditTextArea with the specified settings.
	 * @param defaults The default settings
	 */
	public JEditTextArea(TextAreaDefaults defaults)
	{
		// Enable the necessary events
		enableEvents(AWTEvent.KEY_EVENT_MASK);

		// Initialize some misc. stuff
		painter = new TextAreaPainter(this,defaults);
		AutoScroll scroller = new AutoScroll();
		scrollTimer = new Timer(200,scroller);
		documentHandler = new DocumentHandler();
		listenerList = new EventListenerList();
		caretEvent = new MutableCaretEvent();
		lineSegment = new Segment();
		bracketLine = bracketPosition = -1;
		blink = true;

		// Initialize the GUI
		setLayout(new ScrollLayout());
		add(CENTER,painter);
		add(RIGHT,vertical = new JScrollBar(JScrollBar.VERTICAL));
		add(BOTTOM,horizontal = new JScrollBar(JScrollBar.HORIZONTAL));

		// Add some event listeners
		vertical.addAdjustmentListener(new AdjustHandler());
		horizontal.addAdjustmentListener(new AdjustHandler());
		painter.addComponentListener(new ComponentHandler());
		painter.addMouseListener(new MouseHandler());
		painter.addMouseMotionListener(scroller);
		addFocusListener(new FocusHandler());

		// Load the defaults
		setInputHandler(defaults.inputHandler);
		setDocument(defaults.document);
		editable = defaults.editable;
		caretVisible = defaults.caretVisible;
		caretBlinks = defaults.caretBlinks;
		electricScroll = defaults.electricScroll;

		// We don't seem to get the initial focus event?
		focusedComponent = this;
	}

	/**
	 * Returns the object responsible for painting this text area.
	 */
	public final TextAreaPainter getPainter()
	{
		return painter;
	}

	/**
	 * Returns the input handler.
	 */
	public final InputHandler getInputHandler()
	{
		return inputHandler;
	}

	/**
	 * Sets the input handler.
	 * @param inputHandler The new input handler
	 */
	public void setInputHandler(InputHandler inputHandler)
	{
		if(this.inputHandler != null)
		{
			removeKeyListener(this.inputHandler);
			this.inputHandler.uninstall(this);
		}
		if(inputHandler != null)
		{
			addKeyListener(inputHandler);
			inputHandler.install(this);
		}
		this.inputHandler = inputHandler;
	}

	/**
	 * Returns true if the caret is blinking, false otherwise.
	 */
	public final boolean isCaretBlinkEnabled()
	{
		return caretBlinks;
	}

	/**
	 * Toggles caret blinking.
	 * @param caretBlinks True if the caret should blink, false otherwise
	 */
	public void setCaretBlinkEnabled(boolean caretBlinks)
	{
		this.caretBlinks = caretBlinks;
		if(!caretBlinks)
			blink = false;

		painter.invalidateSelectedLines();
	}

	/**
	 * Returns true if the caret is visible, false otherwise.
	 */
	public final boolean isCaretVisible()
	{
		return (!caretBlinks || blink) && caretVisible;
	}

	/**
	 * Sets if the caret should be visible.
	 * @param caretVisible True if the caret should be visible, false
	 * otherwise
	 */
	public void setCaretVisible(boolean caretVisible)
	{
		this.caretVisible = caretVisible;
		blink = true;

		painter.invalidateSelectedLines();
	}

	/**
	 * Blinks the caret.
	 */
	public final void blinkCaret()
	{
		if(caretBlinks)
		{
			blink = !blink;
			painter.invalidateSelectedLines();
		}
		else
			blink = true;
	}

	/**
	 * Returns the number of lines from the top and button of the
	 * text area that are always visible.
	 */
	public final int getElectricScroll()
	{
		return electricScroll;
	}

	/**
	 * Sets the number of lines from the top and bottom of the text
	 * area that are always visible
	 * @param electricScroll The number of lines always visible from
	 * the top or bottom
	 */
	public final void setElectricScroll(int electricScroll)
	{
		this.electricScroll = electricScroll;
	}

	/**
	 * Updates the state of the scroll bars. This should be called
	 * if the number of lines in the document changes, or when the
	 * size of the text are changes.
	 */
	public void updateScrollBars()
	{
		if(vertical != null && visibleLines != 0)
		{
			vertical.setValues(firstLine,visibleLines,0,getLineCount());
			vertical.setBlockIncrement(visibleLines);
		}

		int width = painter.getSize().width;
		if(horizontal != null && width != 0)
		{
			horizontal.setValues(-horizontalOffset,width,0,width * 5);
			horizontal.setUnitIncrement(painter.getFontMetrics()
				.charWidth('w'));
			horizontal.setBlockIncrement(width / 2);
		}
	}

	/**
	 * Returns the line displayed at the text area's origin.
	 */
	public final int getFirstLine()
	{
		return firstLine;
	}

	/**
	 * Sets the line displayed at the text area's origin without
	 * updating the scroll bars.
	 */
	public void setFirstLine(int firstLine)
	{
		if(firstLine == this.firstLine)
			return;
		int oldFirstLine = this.firstLine;
		this.firstLine = firstLine;
		if(firstLine != vertical.getValue())
			updateScrollBars();
		painter.scrollRepaint(oldFirstLine,firstLine);
	}

	/**
	 * Returns the number of lines visible in this text area.
	 */
	public final int getVisibleLines()
	{
		return visibleLines;
	}

	/**
	 * Returns the horizontal offset of drawn lines.
	 */
	public final int getHorizontalOffset()
	{
		return horizontalOffset;
	}

	/**
	 * Sets the horizontal offset of drawn lines. This can be used to
	 * implement horizontal scrolling.
	 * @param horizontalOffset offset The new horizontal offset
	 */
	public void setHorizontalOffset(int horizontalOffset)
	{
		if(horizontalOffset == this.horizontalOffset)
			return;
		this.horizontalOffset = horizontalOffset;
		if(horizontalOffset != horizontal.getValue())
			updateScrollBars();
		painter.invalidateOffscreen();
		painter.repaint();
	}

	/**
	 * Ensures that the specified line and offset is visible by scrolling
	 * the text area if necessary.
	 * @param line The line to scroll to
	 * @param offset The offset in the line to scroll to
	 */
	public void scrollTo(int line, int offset)
	{
		// visibleLines == 0 before the component is realized
		// we can't do any proper scrolling then, so we have
		// this hack...
		if(visibleLines == 0)
		{
			setFirstLine(Math.max(0,line - electricScroll));
			return;
		}

		if(line < firstLine + electricScroll)
		{
			setFirstLine(Math.max(0,line - electricScroll));
		}
		else if(line + electricScroll >= firstLine + visibleLines)
		{
			int newline = (line - visibleLines) + electricScroll + 1;
			if(newline < 0)
				newline = 0;
			if(newline + visibleLines >= getLineCount())
				newline = getLineCount() - visibleLines;
			setFirstLine(newline);
		}

		int x = offsetToX(line,offset);
		int width = painter.getFontMetrics().charWidth('w');

		if(x < 0)
		{
			setHorizontalOffset(Math.min(0,horizontalOffset
				- x + width));
		}
		else if(x + width >= painter.getSize().width)
		{
			setHorizontalOffset(horizontalOffset +
				(painter.getSize().width - x)
				- width);
		}
	}

	public int lineToY(int line)
	{
		FontMetrics fm = painter.getFontMetrics();
		return (line - firstLine) * fm.getHeight()
			- (fm.getLeading() + fm.getMaxDescent());
	}

	public int yToLine(int y)
	{
		FontMetrics fm = painter.getFontMetrics();
		int height = fm.getHeight();
		return Math.max(0,Math.min(getLineCount() - 1,
			y / height + firstLine));
	}

	public int offsetToX(int line, int offset)
	{
		TokenMarker tokenMarker = getTokenMarker();

		/* Use painter's cached info for speed */
		FontMetrics fm = painter.getFontMetrics();

		getLineText(line,lineSegment);

		int segmentOffset = lineSegment.offset;
		int x = horizontalOffset;

		/* If syntax coloring is disabled, do simple translation */
		if(tokenMarker == null)
		{
			lineSegment.count = offset;
			return x + Utilities.getTabbedTextWidth(lineSegment,
				fm,x,painter,0);
		}
		/* If syntax coloring is enabled, we have to do this because
		 * tokens can vary in width */
		else
		{
			Token tokens;
			if(painter.currentLineIndex == line)
				tokens = painter.currentLineTokens;
			else
				tokens = tokenMarker.markTokens(lineSegment,line);

			Toolkit toolkit = painter.getToolkit();
			Font defaultFont = painter.getFont();
			SyntaxStyle[] styles = painter.getStyles();

			for(;;)
			{
				byte id = tokens.id;
				if(id == Token.END)
				{
					return x;
				}

				if(id == Token.NULL)
					fm = painter.getFontMetrics();
				else
					fm = styles[id].getFontMetrics(defaultFont);

				int length = tokens.length;

				if(offset + segmentOffset < lineSegment.offset + length)
				{
					lineSegment.count = offset - (lineSegment.offset - segmentOffset);
					return x + Utilities.getTabbedTextWidth(
						lineSegment,fm,x,painter,0);
				}
				else
				{
					lineSegment.count = length;
					x += Utilities.getTabbedTextWidth(
						lineSegment,fm,x,painter,0);
					lineSegment.offset += length;
				}
				tokens = tokens.next;
			}
		}
	}

	public int xToOffset(int line, int x)
	{
		TokenMarker tokenMarker = getTokenMarker();

		/* Use painter's cached info for speed */
		FontMetrics fm = painter.getFontMetrics();

		getLineText(line,lineSegment);

		char[] segmentArray = lineSegment.array;
		int segmentOffset = lineSegment.offset;
		int segmentCount = lineSegment.count;

		int width = horizontalOffset;

		if(tokenMarker == null)
		{
			for(int i = 0; i < segmentCount; i++)
			{
				char c = segmentArray[i + segmentOffset];
				int charWidth;
				if(c == '\t')
					charWidth = (int)painter.nextTabStop(width,i)
						- width;
				else
					charWidth = fm.charWidth(c);

				if(painter.isBlockCaretEnabled())
				{
					if(x - charWidth <= width)
						return i;
				}
				else
				{
					if(x - charWidth / 2 <= width)
						return i;
				}

				width += charWidth;
			}

			return segmentCount;
		}
		else
		{
			Token tokens;
			if(painter.currentLineIndex == line)
				tokens = painter.currentLineTokens;
			else
				tokens = tokenMarker.markTokens(lineSegment,line);

			int offset = 0;
			Toolkit toolkit = painter.getToolkit();
			Font defaultFont = painter.getFont();
			SyntaxStyle[] styles = painter.getStyles();

			for(;;)
			{
				byte id = tokens.id;
				if(id == Token.END)
					return offset;

				if(id == Token.NULL)
					fm = painter.getFontMetrics();
				else
					fm = styles[id].getFontMetrics(defaultFont);

				int length = tokens.length;

				for(int i = 0; i < length; i++)
				{
					if(offset + i >= segmentCount)
						throw new InternalError("oops");
					char c = segmentArray[segmentOffset + offset + i];
					int charWidth;
					if(c == '\t')
						charWidth = (int)painter.nextTabStop(width,offset + i)
							- width;
					else
						charWidth = fm.charWidth(c);

					if(painter.isBlockCaretEnabled())
					{
						if(x - charWidth <= width)
							return offset + i;
					}
					else
					{
						if(x - charWidth / 2 <= width)
							return offset + i;
					}

					width += charWidth;
				}

				offset += length;
				tokens = tokens.next;
			}
		}
	}

	public int xyToOffset(int x, int y)
	{
		int line = yToLine(y);
		int start = getLineStartOffset(line);
		return start + xToOffset(line,x);
	}

	public final SyntaxDocument getDocument()
	{
		return document;
	}

	public void setDocument(SyntaxDocument document)
	{
		if(this.document == document)
			return;
		if(this.document != null)
			this.document.removeDocumentListener(documentHandler);
		this.document = document;

		document.addDocumentListener(documentHandler);

		select(0,0);
		updateScrollBars();
		painter.invalidateOffscreen();
		painter.repaint();
	}

	public final TokenMarker getTokenMarker()
	{
		return document.getTokenMarker();
	}

	public final void setTokenMarker(TokenMarker tokenMarker)
	{
		document.setTokenMarker(tokenMarker);
	}

	public final int getDocumentLength()
	{
		return document.getLength();
	}

	public final int getLineCount()
	{
		return document.getDefaultRootElement().getElementCount();
	}

	public final int getLineOfOffset(int offset)
	{
		return document.getDefaultRootElement().getElementIndex(offset);
	}

	public int getLineStartOffset(int line)
	{
		Element lineElement = document.getDefaultRootElement()
			.getElement(line);
		if(lineElement == null)
			return -1;
		else
			return lineElement.getStartOffset();
	}

	public int getLineEndOffset(int line)
	{
		Element lineElement = document.getDefaultRootElement()
			.getElement(line);
		if(lineElement == null)
			return -1;
		else
			return lineElement.getEndOffset();
	}

	public int getLineLength(int line)
	{
		Element lineElement = document.getDefaultRootElement()
			.getElement(line);
		if(lineElement == null)
			return -1;
		else
			return lineElement.getEndOffset()
				- lineElement.getStartOffset();
	}

	public final String getText(int start, int end)
	{
		try
		{
			return document.getText(start,end);
		}
		catch(BadLocationException bl)
		{
			return null;
		}
	}

	public final void getText(int start, int end, Segment segment)
	{
		try
		{
			document.getText(start,end,segment);
		}
		catch(BadLocationException bl)
		{
			segment.offset = segment.count = 0;
		}
	}

	public final String getLineText(int lineIndex)
	{
		int start = getLineStartOffset(lineIndex);
		return getText(start,getLineEndOffset(lineIndex) - start - 1);
	}

	public final void getLineText(int lineIndex, Segment segment)
	{
		int start = getLineStartOffset(lineIndex);
		getText(start,getLineEndOffset(lineIndex) - start - 1,segment);
	}

	public final int getSelectionStart()
	{
		return selectionStart;
	}

	public final int getSelectionStartLine()
	{
		return selectionStartLine;
	}

	public final void setSelectionStart(int selectionStart)
	{
		select(selectionStart,selectionStart);
	}

	public final int getSelectionEnd()
	{
		return selectionEnd;
	}

	public final int getSelectionEndLine()
	{
		return selectionEndLine;
	}

	public final void setSelectionEnd(int selectionEnd)
	{
		select(selectionStart,selectionEnd);
	}

	public final int getCaretPosition()
	{
		return (biasLeft ? selectionStart : selectionEnd);
	}

	public final int getCaretLine()
	{
		return (biasLeft ? selectionStartLine : selectionEndLine);
	}

	public final int getMarkPosition()
	{
		return (biasLeft ? selectionEnd : selectionStart);
	}

	public final int getMarkLine()
	{
		return (biasLeft ? selectionEndLine : selectionStartLine);
	}

	public final void setCaretPosition(int caret)
	{
		select(caret,caret);
	}

	public final void selectAll()
	{
		select(0,getDocumentLength());
	}

	public void select(int start, int end)
	{
		int newStart, newEnd;
		boolean newBias;
		if(start <= end)
		{
			newStart = start;
			newEnd = end;
			newBias = false;
		}
		else
		{
			newStart = end;
			newEnd = start;
			newBias = true;
		}

		if(newStart < 0 || newEnd > getDocumentLength())
		{
			throw new IllegalArgumentException("Bounds out of"
				+ " range: " + newStart + "," +
				newEnd);
		}

		if(newStart == selectionStart && newEnd == selectionEnd
			&& newBias == biasLeft)
			return;

		int newStartLine = getLineOfOffset(newStart);
		int newEndLine = getLineOfOffset(newEnd);

		if(painter.isBracketHighlightEnabled())
		{
			if(bracketLine != -1)
				painter._invalidateLine(bracketLine);
			updateBracketHighlight(end);
			if(bracketLine != -1)
				painter._invalidateLine(bracketLine);
		}

		painter._invalidateLineRange(selectionStartLine,selectionEndLine);
		painter._invalidateLineRange(newStartLine,newEndLine);

		selectionStart = newStart;
		selectionEnd = newEnd;
		selectionStartLine = newStartLine;
		selectionEndLine = newEndLine;
		biasLeft = newBias;

		// When the user is typing, etc, we don't want the caret
		// to blink
		blink = true;
		caretTimer.restart();

		// Clear the `magic' caret position used by up/down
		magicCaret = -1;

		int line = getCaretLine();
		int lineStart = getLineStartOffset(line);
		int offset = Math.max(0,Math.min(getLineLength(line) - 1,
			getCaretPosition() - lineStart));
		scrollTo(line,offset);
		painter.repaint();

		fireCaretEvent();
	}

	public final String getSelectedText()
	{
		return getText(selectionStart,
			selectionEnd - selectionStart);
	}

	public void setSelectedText(String selectedText)
	{
		if(!editable)
		{
			throw new InternalError("Text component"
				+ " read only");
		}

		try
		{
			document.remove(selectionStart,
				selectionEnd - selectionStart);
			document.insertString(selectionStart,
				selectedText,null);
			setCaretPosition(selectionEnd);
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
			throw new InternalError("Cannot replace"
				+ " selection");
		}
	}

	public final boolean isEditable()
	{
		return editable;
	}

	public final void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public final JPopupMenu getRightClickPopup()
	{
		return popup;
	}

	public final void setRightClickPopup(JPopupMenu popup)
	{
		this.popup = popup;
	}

	public final int getMagicCaretPosition()
	{
		return magicCaret;
	}

	public final void setMagicCaretPosition(int magicCaret)
	{
		this.magicCaret = magicCaret;
	}

	public void overwriteSetSelectedText(String str)
	{
		// Don't overstrike if there is a selection
		if(!overwrite || selectionStart != selectionEnd)
		{
			setSelectedText(str);
			return;
		}

		// Don't overstrike if we're on the end of
		// the line
		int caret = getCaretPosition();
		int caretLineEnd = getLineEndOffset(getCaretLine());
		if(caretLineEnd - caret <= str.length())
		{
			setSelectedText(str);
			return;
		}

		try
		{
			document.remove(caret,str.length());
			document.insertString(caret,str,null);
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
		}
	}

	public final boolean isOverwriteEnabled()
	{
		return overwrite;
	}

	public final void setOverwriteEnabled(boolean overwrite)
	{
		this.overwrite = overwrite;
		painter.invalidateSelectedLines();
	}

	public final int getBracketPosition()
	{
		return bracketPosition;
	}

	public final int getBracketLine()
	{
		return bracketLine;
	}

	public final char getBracketCharacter()
	{
		return bracket;
	}

	public final void addCaretListener(CaretListener listener)
	{
		listenerList.add(CaretListener.class,listener);
	}

	public final void removeCaretListener(CaretListener listener)
	{
		listenerList.remove(CaretListener.class,listener);
	}

	public void cut()
	{
		if(editable)
		{
			copy();
			setSelectedText("");
		}
	}

	public void copy()
	{
		if(editable)
		{
			Clipboard clipboard = getToolkit().getSystemClipboard();
			StringSelection selection = new StringSelection(
				getSelectedText());
			clipboard.setContents(selection,null);
		}
	}

	public void paste()
	{
		if(editable)
		{
			Clipboard clipboard = getToolkit().getSystemClipboard();
			try
			{
				String selection = (String)(clipboard
					.getContents(this).getTransferData(
					DataFlavor.stringFlavor));

				// The MacOS MRJ doesn't convert \r to \n,
				// so do it here
				setSelectedText(selection.replace('\r','\n'));
			}
			catch(Exception e)
			{
				getToolkit().beep();
				System.err.println("Clipboard does not"
					+ " contain a string");
			}
		}
	}

	public void removeNotify()
	{
		super.removeNotify();
		if(focusedComponent == this)
			focusedComponent = null;
		if(scrollTimer.isRunning())
			scrollTimer.stop();
	}

	// protected members
	protected static String CENTER = "center";
	protected static String RIGHT = "right";
	protected static String BOTTOM = "bottom";

	protected static JEditTextArea focusedComponent;
	protected static Timer caretTimer;
	
	protected TextAreaPainter painter;

	protected JPopupMenu popup;

	protected Timer scrollTimer;

	protected EventListenerList listenerList;
	protected MutableCaretEvent caretEvent;

	protected boolean caretBlinks;
	protected boolean caretVisible;
	protected boolean blink;

	protected boolean editable;

	protected int firstLine;
	protected int visibleLines;
	protected int electricScroll;

	protected int horizontalOffset;
	
	protected JScrollBar vertical;
	protected JScrollBar horizontal;
	protected boolean scrollBarsInitialized;

	protected InputHandler inputHandler;
	protected SyntaxDocument document;
	protected DocumentHandler documentHandler;

	protected Segment lineSegment;

	protected int selectionStart;
	protected int selectionStartLine;
	protected int selectionEnd;
	protected int selectionEndLine;
	protected boolean biasLeft;

	protected int bracketPosition;
	protected int bracketLine;
	protected char bracket;

	protected int magicCaret;
	protected boolean overwrite;

	protected void fireCaretEvent()
	{
		Object[] listeners = listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i--)
		{
			if(listeners[i] == CaretListener.class)
			{
				((CaretListener)listeners[i+1]).caretUpdate(caretEvent);
			}
		}
	}

	protected void updateBracketHighlight(int newCaretPosition)
	{
		try
		{
			if(newCaretPosition != 0)
			{
				newCaretPosition--;
				Segment lineSegment = painter.currentLine;
				getText(newCaretPosition,1,lineSegment);
				char ch = lineSegment.array[lineSegment.offset];
				int[] match;
				switch(ch)
				{
				case '(':
					match = TextUtilities.locateBracketForward(
						document,newCaretPosition,'(',')');
					bracket = ')';
					break;
				case ')':
					match = TextUtilities.locateBracketBackward(
						document,newCaretPosition,'(',')');
					bracket = '(';
					break;
				case '[':
					match = TextUtilities.locateBracketForward(
						document,newCaretPosition,'[',']');
					bracket = ']';
					break;
				case ']':
					match = TextUtilities.locateBracketBackward(
						document,newCaretPosition,'[',']');
					bracket = '[';
					break;
				case '{':
					match = TextUtilities.locateBracketForward(
						document,newCaretPosition,'{','}');
					bracket = '}';
					break;
				case '}':
					match = TextUtilities.locateBracketBackward(
						document,newCaretPosition,'{','}');
					bracket = '{';
					break;
				default:
					match = null;
					bracket = '\0';
					break;
				}
				if(match != null)
				{
					bracketLine = match[0];
					bracketPosition = match[1];
					return;
				}
			}
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
		}

		bracketLine = bracketPosition = -1;
	}

	protected void documentChanged(DocumentEvent evt)
	{
		DocumentEvent.ElementChange ch = evt.getChange(
			document.getDefaultRootElement());

		int count;
		if(ch == null)
			count = 0;
		else
			count = ch.getChildrenAdded().length -
				ch.getChildrenRemoved().length;

		if(count == 0)
		{
			int line = getLineOfOffset(evt.getOffset());
			painter._invalidateLine(line);
		}
		else
		{
			int index = ch.getIndex();
			painter._invalidateLineRange(index,
				document.getDefaultRootElement()
				.getElementCount());
			updateScrollBars();
		}
	}

	class ScrollLayout implements LayoutManager
	{
		public void addLayoutComponent(String name, Component comp)
		{
			if(name.equals(CENTER))
				center = comp;
			else if(name.equals(RIGHT))
				right = comp;
			else if(name.equals(BOTTOM))
				bottom = comp;
		}

		public void removeLayoutComponent(Component comp)
		{
			if(center == comp)
				center = null;
			if(right == comp)
				right = null;
			if(bottom == comp)
				bottom = null;
		}

		public Dimension preferredLayoutSize(Container parent)
		{
			Dimension dim = new Dimension();
			Dimension centerPref = center.getPreferredSize();
			dim.height = centerPref.height;
			dim.width = centerPref.width;
			Dimension rightPref = right.getPreferredSize();
			dim.width += rightPref.height;
			Dimension bottomPref = bottom.getPreferredSize();
			dim.height += bottomPref.height;

			return dim;
		}

		public Dimension minimumLayoutSize(Container parent)
		{
			Dimension dim = new Dimension();
			Dimension centerPref = center.getMinimumSize();
			dim.height = centerPref.height;
			dim.width = centerPref.width;
			Dimension rightPref = center.getMinimumSize();
			dim.width += rightPref.height;
			Dimension bottomPref = center.getMinimumSize();
			dim.height += bottomPref.height;

			return dim;
		}

		public void layoutContainer(Container parent)
		{
			Dimension size = parent.getSize();
			int rightWidth = right.getPreferredSize().width;
			int bottomHeight = bottom.getPreferredSize().height;
			right.setBounds(size.width - rightWidth,0,
				rightWidth,size.height - bottomHeight);
			bottom.setBounds(0,size.height - bottomHeight,
				size.width - rightWidth,bottomHeight);
			center.setBounds(0,0,size.width - rightWidth,
				size.height - bottomHeight);
		}

		// private members
		private Component center;
		private Component right;
		private Component bottom;
	}

	static class CaretBlinker implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			if(focusedComponent != null)
				focusedComponent.blinkCaret();
		}
	}

	class AutoScroll implements ActionListener, MouseMotionListener
	{
		private int x, y;

		public void actionPerformed(ActionEvent evt)
		{
			select(getMarkPosition(),xyToOffset(x,y));
		}

		public void mouseDragged(MouseEvent evt)
		{
			if(popup != null && popup.isVisible())
				return;

			x = evt.getX();
			y = evt.getY();
			if(!scrollTimer.isRunning())
				scrollTimer.start();
		}
			
		public void mouseMoved(MouseEvent evt) {}
	}

	class MutableCaretEvent extends CaretEvent
	{
		MutableCaretEvent()
		{
			super(JEditTextArea.this);
		}

		public int getDot()
		{
			return getCaretPosition();
		}

		public int getMark()
		{
			return getMarkPosition();
		}
	}

	class AdjustHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent evt)
		{
			if(!scrollBarsInitialized)
				return;

			if(evt.getAdjustable() == vertical)
				setFirstLine(vertical.getValue());
			else
				setHorizontalOffset(-horizontal.getValue());
		}
	}

	class ComponentHandler extends ComponentAdapter
	{
		public void componentResized(ComponentEvent evt)
		{
			int height = painter.getSize().height;
			int lineHeight = painter.getFontMetrics().getHeight();
			int oldVisibleLines = visibleLines;
			visibleLines = height / lineHeight;
			painter.invalidateOffscreen();
			updateScrollBars();

			scrollBarsInitialized = true;
		}
	}

	class DocumentHandler implements DocumentListener
	{
		public void insertUpdate(DocumentEvent evt)
		{
			documentChanged(evt);

			int offset = evt.getOffset();
			int length = evt.getLength();

			boolean repaint = true;

			int newStart;
			int newEnd;

			if(selectionStart >= offset)
			{
				newStart = selectionStart + length;
				repaint = false;
			}
			else
				newStart = selectionStart;
			if(selectionEnd >= offset)
			{
				newEnd = selectionEnd + length;
				repaint = false;
			}
			else
				newEnd = selectionEnd;

			select(newStart,newEnd);

			if(repaint)
				painter.repaint();
		}
	
		public void removeUpdate(DocumentEvent evt)
		{
			documentChanged(evt);

			int offset = evt.getOffset();
			int length = evt.getLength();

			boolean repaint = true;

			int newStart;
			int newEnd;

			if(selectionStart > offset)
			{
				if(selectionStart > offset + length)
					newStart = selectionStart - length;
				else
					newStart = offset;
				repaint = false;
			}
			else
				newStart = selectionStart;

			if(selectionEnd > offset)
			{
				if(selectionEnd > offset + length)
					newEnd = selectionEnd - length;
				else
					newEnd = offset;
				repaint = false;
			}
			else
				newEnd = selectionEnd;

			select(newStart,newEnd);

			if(repaint)
				repaint();
		}

		public void changedUpdate(DocumentEvent evt)
		{
		}
	}

	class FocusHandler implements FocusListener
	{
		public void focusGained(FocusEvent evt)
		{
			setCaretVisible(true);
			focusedComponent = JEditTextArea.this;
		}

		public void focusLost(FocusEvent evt)
		{
			setCaretVisible(false);
			focusedComponent = null;
		}
	}

	class MouseHandler extends MouseAdapter
	{
		public void mousePressed(MouseEvent evt)
		{
			requestFocus();

			if((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0
				&& popup != null)
			{
				popup.show(painter,evt.getX(),evt.getY());
				return;
			}

			int line = yToLine(evt.getY());
			int offset = xToOffset(line,evt.getX());
			int dot = getLineStartOffset(line) + offset;

			switch(evt.getClickCount())
			{
			case 1:
				doSingleClick(evt,line,offset,dot);
				break;
			case 2:
				// It uses the bracket matching stuff, so
				// it can throw a BLE
				try
				{
					doDoubleClick(evt,line,offset,dot);
				}
				catch(BadLocationException bl)
				{
					bl.printStackTrace();
				}
				break;
			case 3:
				doTripleClick(evt,line,offset,dot);
				break;
			}
		}

		public void mouseReleased(MouseEvent evt)
		{
			if(scrollTimer.isRunning())
				scrollTimer.stop();
		}

		private void doSingleClick(MouseEvent evt, int line, 
			int offset, int dot)
		{
			if((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0)
				setSelectionEnd(dot);
			else
				setCaretPosition(dot);
		}

		private void doDoubleClick(MouseEvent evt, int line,
			int offset, int dot) throws BadLocationException
		{
			String lineText = getLineText(line);

			String openBrackets = (String)document.getProperty("openBrackets");
			String closeBrackets = (String)document.getProperty("closeBrackets");

			char ch = lineText.charAt(Math.max(0,offset - 1));
			int index = openBrackets.indexOf(ch);
			if(index != -1)
			{
				char closeBracket = closeBrackets.charAt(index);
				int[] match = TextUtilities.locateBracketForward(
					document,dot - 1,ch,closeBracket);
				if(match != null)
				{
					int mark = getLineStartOffset(match[0])
						+ match[1];
					select(dot - 1,mark + 1);
					return;
				}
			}
			else
			{
				index = closeBrackets.indexOf(ch);
				if(index != -1)
				{
					char openBracket = openBrackets.charAt(index);
					int[] match = TextUtilities.locateBracketBackward(
						document,dot - 1,openBracket,ch);
					if(match != null)
					{
						int mark = getLineStartOffset(match[0])
							+ match[1];
						select(dot,mark);
						return;
					}
				}
			}

			// Ok, it's not a bracket... select the word
			String noWordSep = (String)document.getProperty("noWordSep");

			// If the user clicked on a non-letter char,
			// we select the surrounding non-letters
			boolean selectNoLetter = (!Character
				.isLetterOrDigit(ch)
				&& noWordSep.indexOf(ch) == -1);

			int wordStart = 0;

			for(int i = offset - 1; i >= 0; i--)
			{
				ch = lineText.charAt(i);
				if(selectNoLetter ^ (!Character
					.isLetterOrDigit(ch) &&
					noWordSep.indexOf(ch) == -1))
				{
					wordStart = i + 1;
					break;
				}
			}

			int wordEnd = lineText.length();
			for(int i = offset; i < lineText.length(); i++)
			{
				ch = lineText.charAt(i);
				if(selectNoLetter ^ (!Character
					.isLetterOrDigit(ch) &&
					noWordSep.indexOf(ch) == -1))
				{
					wordEnd = i;
					break;
				}
			}

			int lineStart = getLineStartOffset(line);
			select(lineStart + wordStart,lineStart + wordEnd);
		}

		private void doTripleClick(MouseEvent evt, int line,
			int offset, int dot)
		{
			select(getLineStartOffset(line),getLineEndOffset(line)-1);
		}
	}

	static
	{
		caretTimer = new Timer(500,new CaretBlinker());
		caretTimer.setInitialDelay(500);
		caretTimer.start();
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.11  1999/07/08 06:06:04  sp
 * Bug fixes and miscallaneous updates
 *
 * Revision 1.10  1999/07/05 04:38:39  sp
 * Massive batch of changes... bug fixes, also new text component is in place.
 * Have fun
 *
 * Revision 1.9  1999/06/30 07:08:02  sp
 * Text area bug fixes
 *
 * Revision 1.8  1999/06/30 05:01:55  sp
 * Lots of text area bug fixes and optimizations
 *
 * Revision 1.7  1999/06/29 09:01:24  sp
 * Text area now does bracket matching, eol markers, also xToOffset() and
 * offsetToX() now work better
 *
 * Revision 1.6  1999/06/28 09:17:20  sp
 * Perl mode javac compile fix, text area hacking
 *
 * Revision 1.5  1999/06/27 04:53:16  sp
 * Text selection implemented in text area, assorted bug fixes
 *
 * Revision 1.4  1999/06/25 06:54:08  sp
 * Text area updates
 *
 */
