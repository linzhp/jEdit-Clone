/*
 * TextAreaModel.java - Maintains the text area's state
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
import java.awt.*;
import org.gjt.sp.jedit.syntax.*;

/**
 * The text area state. It ensures that displayed lines are up to date,
 * and translates screen co-ordiates to offsets and vice versa.
 * @author Slava Pestov
 * @version $Id$
 */
public class TextAreaModel
{
	/**
	 * Creates a new text area model. This should be not be called
	 * directly.
	 */
	public TextAreaModel(JEditTextArea textArea, SyntaxDocument document)
	{
		this.textArea = textArea;
		documentHandler = new DocumentHandler();
		setDocument(document);

		editable = true;
		bracketLine = bracketPosition = -1;
	}

	public int getLineHeight()
	{
		return textArea.getPainter().getFontMetrics().getHeight();
	}

	public int lineToY(int line)
	{
		FontMetrics fm = textArea.getPainter().getFontMetrics();
		return (line - textArea.getFirstLine()) * fm.getHeight()
			- (fm.getLeading() + fm.getMaxDescent());
	}

	public int yToLine(int y)
	{
		FontMetrics fm = textArea.getPainter().getFontMetrics();
		int height = fm.getHeight();
		return Math.max(0,Math.min(getLineCount() - 1,
			y / height + textArea.getFirstLine()));
	}

	public int offsetToX(int line, int offset)
	{
		TokenMarker tokenMarker = getTokenMarker();

		int returnValue;

		/* Use painter's cached info for speed */
		TextAreaPainter painter = textArea.getPainter();
		FontMetrics fm = painter.getFontMetrics();
		Segment lineSegment = painter.currentLine;

		if(painter.currentLineIndex != line)
			getLineText(line,lineSegment);

		/* Because this is called in the middle of paintLine(),
		 * we need to save the segment's state.
		 */
		int segmentOffset = lineSegment.offset;
		int segmentCount = lineSegment.count;

		int x = textArea.getHorizontalOffset();

		/* If syntax coloring is disabled, do simple translation */
		if(tokenMarker == null)
		{
			lineSegment.count = offset;
			returnValue = x + Utilities.getTabbedTextWidth(lineSegment,
				fm,x,painter,0);
		}
		/* If syntax coloring is enabled, we have to do this because
		 * tokens can vary in width */
		else
		{
			Token tokens = tokenMarker.markTokens(lineSegment,line);

			Toolkit toolkit = painter.getToolkit();
			Font defaultFont = painter.getFont();
			SyntaxStyle[] styles = painter.getStyles();

			for(;;)
			{
				byte id = tokens.id;
				if(id == Token.END)
				{
					returnValue = x;
					break;
				}

				if(id == Token.NULL)
					fm = toolkit.getFontMetrics(defaultFont);
				else
				{
					fm = toolkit.getFontMetrics(styles[id]
						.getStyledFont(defaultFont));
				}

				int length = tokens.length;

				if(offset + segmentOffset < lineSegment.offset + length)
				{
					lineSegment.count = offset - (lineSegment.offset - segmentOffset);
					returnValue = x + Utilities.getTabbedTextWidth(
						lineSegment,fm,x,painter,0);
					break;
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

		lineSegment.offset = segmentOffset;
		lineSegment.count = segmentCount;

		return returnValue;
	}

	public int xToOffset(int line, int x)
	{
		TokenMarker tokenMarker = getTokenMarker();

		/* Use painter's cached info for speed */
		TextAreaPainter painter = textArea.getPainter();
		FontMetrics fm = painter.getFontMetrics();
		Segment lineSegment = painter.currentLine;
			
		if(painter.currentLineIndex != line)
			getLineText(line,lineSegment);

		char[] segmentArray = lineSegment.array;
		int segmentOffset = lineSegment.offset;
		int segmentCount = lineSegment.count;

		int width = textArea.getHorizontalOffset();

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
					if(x + charWidth / 2 <= width)
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
			Token tokens = tokenMarker.markTokens(lineSegment,line);

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
					fm = toolkit.getFontMetrics(defaultFont);
				else
				{
					fm = toolkit.getFontMetrics(styles[id]
						.getStyledFont(defaultFont));
				}

				int length = tokens.length;

				for(int i = 0; i < length; i++)
				{
					char c = segmentArray[segmentOffset + offset + i];
					int charWidth;
					if(c == '\t')
						charWidth = (int)painter.nextTabStop(width,offset + i)
							- width;
					else
						charWidth = fm.charWidth(c);
	
					if(x - charWidth / 2 <= width)
						return offset + i;
	
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

	public SyntaxDocument getDocument()
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
		textArea.updateScrollBars();
		textArea.getPainter().offscreenRepaint();
		textArea.repaint();
	}

	public TokenMarker getTokenMarker()
	{
		return document.getTokenMarker();
	}

	public void setTokenMarker(TokenMarker tokenMarker)
	{
		document.setTokenMarker(tokenMarker);
	}

	public int getLength()
	{
		return document.getLength();
	}

	public int getLineCount()
	{
		return document.getDefaultRootElement().getElementCount();
	}

	public int getLineOfOffset(int offset)
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

	public String getText(int start, int end)
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

	public void getText(int start, int end, Segment segment)
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

	public String getLineText(int lineIndex)
	{
		int start = getLineStartOffset(lineIndex);
		return getText(start,getLineEndOffset(lineIndex) - start - 1);
	}

	public void getLineText(int lineIndex, Segment segment)
	{
		int start = getLineStartOffset(lineIndex);
		getText(start,getLineEndOffset(lineIndex) - start - 1,segment);
	}

	public int getSelectionStart()
	{
		return selectionStart;
	}

	public int getSelectionStartLine()
	{
		return selectionStartLine;
	}

	public void setSelectionStart(int selectionStart)
	{
		select(selectionStart,getSelectionEnd());
	}

	public int getSelectionEnd()
	{
		return selectionEnd;
	}

	public int getSelectionEndLine()
	{
		return selectionEndLine;
	}

	public void setSelectionEnd(int selectionEnd)
	{
		select(getSelectionStart(),selectionEnd);
	}

	public int getCaretPosition()
	{
		return (biasLeft ? selectionStart : selectionEnd);
	}

	public int getCaretLine()
	{
		return (biasLeft ? selectionStartLine : selectionEndLine);
	}

	public int getMarkPosition()
	{
		return (biasLeft ? selectionEnd : selectionStart);
	}

	public int getMarkLine()
	{
		return (biasLeft ? selectionEndLine : selectionStartLine);
	}

	public void setCaretPosition(int caret)
	{
		select(caret,caret);
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

		if(newStart < 0 || newEnd > getLength())
		{
			throw new IllegalArgumentException("Bounds out of"
				+ " range: " + newStart + "," +
				newEnd);
		}

		int newStartLine = getLineOfOffset(newStart);
		int newEndLine = getLineOfOffset(newEnd);

		TextAreaPainter painter = textArea.getPainter();

		if(painter.isBracketHighlightEnabled())
		{
			if(bracketLine != -1)
				painter._invalidateLineRange(bracketLine,bracketLine);
			updateBracketHighlight(end);
			if(bracketLine != -1)
				painter._invalidateLineRange(bracketLine,bracketLine);
		}

		painter._invalidateLineRange(selectionStartLine,selectionEndLine);
		painter._invalidateLineRange(newStartLine,newEndLine);

		selectionStart = newStart;
		selectionEnd = newEnd;
		selectionStartLine = newStartLine;
		selectionEndLine = newEndLine;
		biasLeft = newBias;

		int line = getCaretLine();
		int lineStart = getLineStartOffset(line);
		if(!textArea.scrollTo(line,getCaretPosition() - lineStart))
			textArea.getPainter().repaint();
	}

	public String getSelectedText()
	{
		return getText(selectionStart,selectionEnd);
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
			document.remove(selectionStart,selectionEnd);
			document.insertString(selectionStart,
				selectedText,null);
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
			throw new InternalError("Cannot replace"
				+ " selection");
		}
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}
	
	public int getBracketPosition()
	{
		return bracketPosition;
	}

	public int getBracketLine()
	{
		return bracketLine;
	}

	public char getBracketCharacter()
	{
		return bracket;
	}

	// protected members
	protected JEditTextArea textArea;
	protected SyntaxDocument document;
	protected DocumentHandler documentHandler;

	protected int selectionStart;
	protected int selectionStartLine;
	protected int selectionEnd;
	protected int selectionEndLine;
	protected boolean biasLeft;

	protected int bracketPosition;
	protected int bracketLine;
	protected char bracket;

	protected boolean editable;

	protected void updateBracketHighlight(int newCaretPosition)
	{
		try
		{
			if(newCaretPosition != 0)
			{
				newCaretPosition--;
				Segment lineSegment = textArea.getPainter().currentLine;
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
			throw new InternalError("Error with"
				+ " bracket matching");
		}

		bracketLine = bracketPosition = -1;
	}

	protected void documentChanged(DocumentEvent evt)
	{
		DocumentEvent.ElementChange ch = evt.getChange(
			document.getDefaultRootElement());

		TextAreaPainter painter = textArea.getPainter();

		int count;
		if(ch == null)
			count = 0;
		else
			count = ch.getChildrenAdded().length -
				ch.getChildrenRemoved().length;

		if(count == 0)
		{
			int line = getLineOfOffset(evt.getOffset());
			painter._invalidateLineRange(line,line);
		}
		else
		{
			int index = ch.getIndex();
			painter._invalidateLineRange(index,
				document.getDefaultRootElement()
				.getElementCount());
			textArea.updateScrollBars();
		}
	}
		
	class DocumentHandler implements DocumentListener
	{
		public void insertUpdate(DocumentEvent evt)
		{
			documentChanged(evt);

			int offset = evt.getOffset();
			int length = evt.getLength();

			int newStart;
			int newEnd;

			if(selectionStart >= offset)
				newStart = selectionStart + length;
			else
				newStart = selectionStart;
			if(selectionEnd >= offset)
				newEnd = selectionEnd + length;
			else
				newEnd = selectionEnd;

			select(newStart,newEnd);
		}
	
		public void removeUpdate(DocumentEvent evt)
		{
			documentChanged(evt);

			int offset = evt.getOffset();
			int length = evt.getLength();

			int newStart;
			int newEnd;

			if(selectionStart >= offset)
				newStart = selectionStart - length;
			else
				newStart = selectionStart;
			if(selectionEnd >= offset)
				newEnd = selectionEnd - length;
			else
				newEnd = selectionEnd;

			select(newStart,newEnd);
		}

		public void changedUpdate(DocumentEvent evt)
		{
		}
	}
}

/*
 * ChangeLog:
 * $Log$
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
