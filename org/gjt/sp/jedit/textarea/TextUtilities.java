/*
 * TextUtilities.java - Utility functions used by the text area classes
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

import javax.swing.text.*;

/**
 * Class with several bracket matching functions used by the text area component.
 * @author Slava Pestov
 * @version $Id$
 */
public class TextUtilities
{
	/**
	 * Finds the previous instance of an opening bracket in the document.
	 * The closing bracket is needed as well to handle nested brackets
	 * properly.
	 * @param doc The document to search in
	 * @param dot The starting position
	 * @param openBracket The opening bracket
	 * @param closeBracket The closing bracket
	 * @return An integer array, the first element being the line the
	 * second being the offset
	 * @exception BadLocationException if `dot' is out of range
	 */
	public static int[] locateBracketBackward(Document doc, int dot,
		char openBracket, char closeBracket)
		throws BadLocationException
	{
		int count;
		Element map = doc.getDefaultRootElement();

		// check current line
		int lineNo = map.getElementIndex(dot);
		Element lineElement = map.getElement(lineNo);
		int start = lineElement.getStartOffset();
		int offset = scanBackwardLine(doc.getText(start,dot - start),
			openBracket,closeBracket,0);
		count = -offset - 1;
		if(offset >= 0)
		{
			int[] returnValue = { lineNo, offset };
			return returnValue;
		}

		// check previous lines
		for(int i = lineNo - 1; i >= 0; i--)
		{
			lineElement = map.getElement(i);
			start = lineElement.getStartOffset();
			offset = scanBackwardLine(doc.getText(start,
				lineElement.getEndOffset() - start),
				openBracket,closeBracket,count);
			count = -offset - 1;
			if(offset >= 0)
			{
				int[] returnValue = { i, offset };
				return returnValue;
			}
		}

		// not found
		return null;
	}

	/**
	 * Finds the next instance of a closing bracket in the document.
	 * The opening bracket is needed as well to handle nested brackets
	 * properly.
	 * @param doc The document to search in
	 * @param dot The starting position
	 * @param openBracket The opening bracket
	 * @param closeBracket The closing bracket
	 * @return An integer array, the first element being the line the
	 * second being the offset
	 * @exception BadLocationException if `dot' is out of range
	 */
	public static int[] locateBracketForward(Document doc, int dot,
		char openBracket, char closeBracket)
		throws BadLocationException
	{
		int count;
		Element map = doc.getDefaultRootElement();

		// check current line
		int lineNo = map.getElementIndex(dot);
		Element lineElement = map.getElement(lineNo);
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset();
		int offset = scanForwardLine(doc.getText(dot + 1,end - (dot + 1)),
			openBracket,closeBracket,0);
		count = -offset - 1;
		if(offset >= 0)
		{
			int[] returnValue = { lineNo, (dot - start) + offset + 1 };
			return returnValue;
		}

		// check following lines
		for(int i = lineNo + 1; i < map.getElementCount(); i++)
		{
			lineElement = map.getElement(i);
			start = lineElement.getStartOffset();
			offset = scanForwardLine(doc.getText(start,
				lineElement.getEndOffset() - start),
				openBracket,closeBracket,count);
			count = -offset - 1;
			if(offset >= 0)
			{
				int[] returnValue = { i, offset };
				return returnValue;
			}
		}

		// not found
		return null;
	}

	// private members

	// the return value is as follows:
	// >= 0: offset in line where bracket was found
	// < 0: -1 - count
	private static int scanBackwardLine(String line, char openBracket,
		char closeBracket, int count)
	{
		for(int i = line.length() - 1; i >= 0; i--)
		{
			char c = line.charAt(i);
			if(c == closeBracket)
				count++;
			else if(c == openBracket)
			{
				if(--count < 0)
					return i;
			}
		}
		return -1 - count;
	}

	// the return value is as follows:
	// >= 0: offset in line where bracket was found
	// < 0: -1 - count
	private static int scanForwardLine(String line, char openBracket,
		char closeBracket, int count)
	{
		for(int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			if(c == openBracket)
				count++;
			else if(c == closeBracket)
			{
				if(--count < 0)
					return i;
			}
		}
		return -1 - count;
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/06/29 09:03:18  sp
 * oops, forgot to add TextUtilities.java
 *
 */
