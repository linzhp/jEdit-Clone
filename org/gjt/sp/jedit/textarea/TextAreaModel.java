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
import java.awt.FontMetrics;
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
			- fm.getLeading() - fm.getDescent();
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

		/* Use painter's cached info for speed */
		TextAreaPainter painter = textArea.getPainter();
		FontMetrics fm = painter.getFontMetrics();
		Segment currentLine = painter.currentLine;

		if(painter.currentLineIndex != line)
		{
			getLineText(line,currentLine);
			painter.currentLineIndex = line;
		}

		if(tokenMarker == null)
		{
			int oldCount = currentLine.count;
			currentLine.count = offset;
			int x = Utilities.getTabbedTextWidth(currentLine,
				fm,0,painter,0);
			currentLine.count = oldCount;
			return x + textArea.getHorizontalOffset();
		}
		else
			return 0;
	}

	public int xToOffset(int line, int x)
	{
		x -= textArea.getHorizontalOffset();

		TokenMarker tokenMarker = getTokenMarker();

		/* Use painter's cached info for speed */
		TextAreaPainter painter = textArea.getPainter();
		FontMetrics fm = painter.getFontMetrics();
		Segment currentLine = painter.currentLine;

		if(painter.currentLineIndex != line)
		{
			getLineText(line,currentLine);
			painter.currentLineIndex = line;
		}

		if(tokenMarker == null)
		{
			int offset = currentLine.offset;
			int count = currentLine.count;
			char[] array = currentLine.array;
			int width = 0;
			for(int i = 0; i < count; i++)
			{
				char c = array[i + offset];
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

			return count;
		}
		else
			return 0;
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
		start = Math.max(0,start);
		end = Math.min(getLength(),end);

		if(start <= end)
		{
			selectionStart = start;
			selectionEnd = end;
			biasLeft = false;
		}
		else
		{
			selectionStart = end;
			selectionEnd = start;
			biasLeft = true;
		}

		int oldSelectionStartLine = selectionStartLine;
		int oldSelectionEndLine = selectionEndLine;

		selectionStartLine = getLineOfOffset(selectionStart);
		selectionEndLine = getLineOfOffset(selectionEnd);

		textArea.getPainter()._invalidateLineRange(
			Math.min(oldSelectionStartLine,selectionStartLine),
			Math.max(oldSelectionEndLine,selectionEndLine));

		int line = getCaretLine();
		int lineStart = getLineStartOffset(line);
		if(!textArea.scrollTo(line,getCaretPosition() - start))
			textArea.getPainter().repaint();
	}

	public int getCaretPosition()
	{
		return (biasLeft ? selectionStart : selectionEnd);
	}

	public int getCaretLine()
	{
		return (biasLeft ? selectionStartLine : selectionEndLine);
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
 * Revision 1.5  1999/06/27 04:53:16  sp
 * Text selection implemented in text area, assorted bug fixes
 *
 * Revision 1.4  1999/06/25 06:54:08  sp
 * Text area updates
 *
 */
