/*
 * uncomment.java
 * Copyright (C) 1999 Slava Pestov
 *
 * This	free software; you can redistribute it and/or
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

package org.gjt.sp.jedit.actions;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.gui.SyntaxTextArea;
import org.gjt.sp.jedit.*;

public class uncomment extends EditAction
{
	public uncomment()
	{
		super("uncomment");
	}

	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		SyntaxTextArea textArea = view.getTextArea();
		Buffer buffer = view.getBuffer();

		String commentStart = (String)buffer.getProperty("commentStart");
		String commentEnd = (String)buffer.getProperty("commentEnd");
		String blockComment = (String)buffer.getProperty("blockComment");
		String boxComment = (String)buffer.getProperty("boxComment");

		buffer.beginCompoundEdit();

		Element map = buffer.getDefaultRootElement();
		int startLine = map.getElementIndex(textArea.getSelectionStart());
		int endLine = map.getElementIndex(textArea.getSelectionEnd());
		try
		{
			for(int i = startLine; i <= endLine; i++)
			{
				Element lineElement = map.getElement(i);
				int start = (i == startLine ? textArea
					.getSelectionStart() : lineElement
					.getStartOffset());
				String line = buffer.getText(start,lineElement
					.getEndOffset() - start - 1);
				uncommentLine(line,start,buffer,lineElement,
					commentStart,commentEnd,blockComment,
					boxComment);
			}
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
		}
		textArea.select(textArea.getCaretPosition(),
			textArea.getCaretPosition());
		buffer.endCompoundEdit();
	}

	private void uncommentLine(String line, int start, Buffer buffer,
		Element lineElement, String commentStart, String commentEnd,
		String blockComment, String boxComment)
		throws BadLocationException
	{
		// calculate leading indent
		int indent = TextUtilities.getLeadingWhiteSpace(line);
		int istart = start + indent;

		// Remove comment strings from the start and end of the line
		if(commentStart != null && commentEnd != null
			&& boxComment != null)
		{
			int commentStartLen = commentStart.length();
			int commentEndLen = commentEnd.length();
			int boxCommentLen = boxComment.length();

			if(line.regionMatches(indent,commentStart,0,
				commentStartLen))
			{
				buffer.remove(istart,commentStartLen);
			}
			else if(line.regionMatches(indent,commentEnd,0,
				commentEndLen))
			{
				buffer.remove(istart,commentEndLen);
			}
			else if(line.regionMatches(indent,boxComment,0,
				boxCommentLen))
			{
				buffer.remove(istart,boxCommentLen);
			}

			int end = lineElement.getEndOffset() - 1;
			line = buffer.getText(start,end - start);
			int len = line.length();
			if(line.regionMatches(len - commentStartLen,
				commentStart,0,commentStartLen))
			{
				buffer.remove(end - commentStartLen,
					commentStartLen);
			}
			else if(line.regionMatches(len - commentEndLen,
				commentEnd,0,commentEndLen))
			{
				buffer.remove(end - commentEndLen,
					commentEndLen);
			}
		}

		if(blockComment != null)
		{
			int blockCommentLen = blockComment.length();
			if(line.regionMatches(indent,blockComment,0,
				blockCommentLen))
			{
				buffer.remove(start,blockCommentLen);
			}
		}
	}
}
