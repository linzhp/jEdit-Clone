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
	}

	public int getLineHeight()
	{
		return textArea.getPainter().getFontMetrics().getHeight();
	}

	public int lineToY(int line)
	{
		FontMetrics fm = textArea.getPainter().getFontMetrics();
		return (line - textArea.getFirstLine()) * fm.getHeight()
			- fm.getLeading() - fm.getMaxDescent();
	}

	public int yToLine(int y)
	{
		FontMetrics fm = textArea.getPainter().getFontMetrics();
		y += fm.getLeading() + fm.getMaxDescent();
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
		{
			getLineText(line,lineSegment);
		}

		/* Because this is called in the middle of paintLine(),
		 * we need to save the segment's state.
		 */
		int segmentOffset = lineSegment.offset;
		int segmentCount = lineSegment.count;

		/* If syntax coloring is disabled, do simple translation */
		if(tokenMarker == null)
		{
			lineSegment.count = offset;
			int x = Utilities.getTabbedTextWidth(lineSegment,
				fm,0,painter,0);
			returnValue = x;
		}
		/* If syntax coloring is enabled, we have to do this because
		 * tokens can vary in width */
		else
		{
			// XXX: token list is computed twice for lines
			// that are highlighted
			Token tokens = tokenMarker.markTokens(lineSegment,line);

			int x = 0;
			int index = 0;
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

				if(offset < index + length)
				{
					lineSegment.count = offset - index;
					returnValue = x + Utilities.getTabbedTextWidth(
						lineSegment,fm,x,painter,segmentOffset + index);
					break;
				}
				else
				{
					lineSegment.count = length;
					x += Utilities.getTabbedTextWidth(
						lineSegment,fm,x,painter,
						lineSegment.offset + index);
					lineSegment.offset += length;
					index += length;
				}
				tokens = tokens.next;
			}
		}

		lineSegment.offset = segmentOffset;
		lineSegment.count = segmentCount;

		return returnValue + textArea.getHorizontalOffset();
	}

	public int xToOffset(int line, int x)
	{
		x -= textArea.getHorizontalOffset();

		TokenMarker tokenMarker = getTokenMarker();

		/* Use painter's cached info for speed */
		TextAreaPainter painter = textArea.getPainter();
		FontMetrics fm = painter.getFontMetrics();
		Segment lineSegment = painter.currentLine;
		char[] segmentArray = lineSegment.array;
		int segmentOffset = lineSegment.offset;
		int segmentCount = lineSegment.count;
			
		if(painter.currentLineIndex != line)
		{
			getLineText(line,lineSegment);
			painter.currentLineIndex = line;
		}

		if(tokenMarker == null)
		{
			int width = 0;
			for(int i = 0; i < segmentCount; i++)
			{
				char c = segmentArray[i + segmentOffset];
				int charWidth;
				if(c == '\t')
					charWidth = (int)painter.nextTabStop(width,i)
						- width;
				else
					charWidth = fm.charWidth(c);

				if(x - charWidth / 2 < width)
					return i;

				width += charWidth;
			}

			return segmentCount;
		}
		else
		{
			// XXX: token list is computed twice for lines
			// that are highlighted
			Token tokens = tokenMarker.markTokens(
				lineSegment,line);

			int offset = 0;
			Toolkit toolkit = painter.getToolkit();
			Font defaultFont = painter.getFont();
			SyntaxStyle[] styles = painter.getStyles();

			int width = 0;

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
	
					if(x - charWidth / 2 < width)
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
		select(selectionStart,selectionEnd);
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
		select(selectionStart,selectionEnd);
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

		textArea.getPainter()._invalidateLineRange(
			Math.min(selectionStartLine,newStartLine),
			Math.max(selectionEndLine,newEndLine));

		int line = (newBias ? newStartLine : newEndLine);
		int lineStart = getLineStartOffset(line);
		if(!textArea.scrollTo(line,(newBias ? newStart : newEnd) - start))
			textArea.getPainter().repaint();

		selectionStart = newStart;
		selectionEnd = newEnd;
		selectionStartLine = newStartLine;
		selectionEndLine = newEndLine;
		biasLeft = newBias;
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

	public boolean getLeftBias()
	{
		return biasLeft;
	}

	public void setLeftBias(boolean biasLeft)
	{
		this.biasLeft = biasLeft;
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

	protected void updateDisplay(DocumentEvent evt)
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
			painter.invalidateCurrentLine();
		else
		{
			int index = ch.getIndex();
			painter.invalidateLineRange(index,
				document.getDefaultRootElement()
				.getElementCount());
			textArea.updateScrollBars();
		}
	}
		
	class DocumentHandler implements DocumentListener
	{
		public void insertUpdate(DocumentEvent evt)
		{
			updateDisplay(evt);
		}
	
		public void removeUpdate(DocumentEvent evt)
		{
			updateDisplay(evt);
		}

		public void changedUpdate(DocumentEvent evt)
		{
		}
	}
}

/*
 * ChangeLog:
 * $Log$
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
