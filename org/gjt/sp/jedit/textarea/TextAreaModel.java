/*
 * TextAreaModel.java - Maintains the text area's state
 * Copyright (C) 1999 Slava Pestov
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

package org.gjt.sp.jedit.textarea;

import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.FontMetrics;
import org.gjt.sp.jedit.syntax.SyntaxDocument;

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
		y += fm.getLeading() + fm.getDescent();
		int height = fm.getHeight();
		if(y % height != 0)
			y += height;
		return y / height + textArea.getFirstLine();
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
		return getText(start,getLineEndOffset(lineIndex) - start);
	}

	public void getLineText(int lineIndex, Segment segment)
	{
		int start = getLineStartOffset(lineIndex);
		getText(start,getLineEndOffset(lineIndex) - start,segment);
	}

	// protected members
	protected JEditTextArea textArea;
	protected SyntaxDocument document;
	protected DocumentHandler documentHandler;

	protected void updateDisplay(DocumentEvent evt)
	{
		DocumentEvent.ElementChange ch = evt.getChange(
			document.getDefaultRootElement());

		TextAreaPainter painter = textArea.getPainter();

		int count;
		if(ch == null)
			count = 0;
		else
			count = ch.getChildrenAdded().length +
			ch.getChildrenRemoved().length;

		if(count == 0)
		{
			int line = getLineOfOffset(evt.getOffset());
			painter.invalidateLineRange(line,line);
		}
		else
		{
			int index = ch.getIndex();
			painter.invalidateLineRange(index,
				document.getDefaultRootElement()
				.getElementCount() - index);
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
