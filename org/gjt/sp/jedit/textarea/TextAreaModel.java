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
		return (line  - textArea.getFirstLine()) * getLineHeight();
	}

	public int yToLine(int y)
	{
		int height = getLineHeight();
		int lineCount = document.getDefaultRootElement()
			.getElementCount();
		int line = y / height + textArea.getFirstLine();
		if(line >= lineCount)
			return lineCount - 1;
		else
			return line;
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

	// protected members
	protected JEditTextArea textArea;
	protected SyntaxDocument document;
	protected DocumentHandler documentHandler;

	protected void insertLines(int index, int length)
	{
		TextAreaPainter painter = textArea.getPainter();
		painter.offscreenRepaintLineRange(this,index,
			document.getDefaultRootElement()
			.getElementCount() - index);
		painter.repaint();
	}

	protected void deleteLines(int index, int length)
	{
		TextAreaPainter painter = textArea.getPainter();
		painter.offscreenRepaintLineRange(this,index,
			document.getDefaultRootElement()
			.getElementCount() - index);
		painter.repaint();
	}

	class DocumentHandler implements DocumentListener
	{
		public void insertUpdate(DocumentEvent evt)
		{
			DocumentEvent.ElementChange ch = evt.getChange(
				document.getDefaultRootElement());
			if(ch == null)
				return;
			insertLines(ch.getIndex() + 1,ch.getChildrenAdded().length
				- ch.getChildrenRemoved().length);
		}
	
		public void removeUpdate(DocumentEvent evt)
		{
			DocumentEvent.ElementChange ch = evt.getChange(
				document.getDefaultRootElement());
			if(ch == null)
				return;
			deleteLines(ch.getIndex() + 1,ch.getChildrenRemoved().length
				- ch.getChildrenAdded().length);
		}

		public void changedUpdate(DocumentEvent evt)
		{
		}
	}
}
