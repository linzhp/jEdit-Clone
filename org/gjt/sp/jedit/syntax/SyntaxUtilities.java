/*
 * SyntaxUtilities.java - Utility functions used by syntax colorizing
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

package org.gjt.sp.jedit.syntax;

import javax.swing.text.*;
import java.awt.Color;

/**
 * Class with several segment and bracket matching functions used by
 * jEdit's syntax colorizing subsystem. It also provides a way to get
 * the default color table.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class SyntaxUtilities
{
	/**
	 * Checks if a subregion of a <code>Segment</code> is equal to a
	 * string.
	 * @param ignoreCase True if case should be ignored, false otherwise
	 * @param text The segment
	 * @param offset The offset into the segment
	 * @param match The string to match
	 */
	public static boolean regionMatches(boolean ignoreCase, Segment text,
					    int offset, String match)
	{
		int length = offset + match.length();
		char[] textArray = text.array;
		if(length > textArray.length)
			return false;
		for(int i = offset, j = 0; i < length; i++, j++)
		{
			char c1 = textArray[i];
			char c2 = match.charAt(j);
			if(ignoreCase)
			{
				c1 = Character.toUpperCase(c1);
				c2 = Character.toUpperCase(c2);
			}
			if(c1 != c2)
				return false;
		}
		return true;
	}
	
	/**
	 * Checks if a subregion of a <code>Segment</code> is equal to a
	 * character array.
	 * @param ignoreCase True if case should be ignored, false otherwise
	 * @param text The segment
	 * @param offset The offset into the segment
	 * @param match The character array to match
	 */
	public static boolean regionMatches(boolean ignoreCase, Segment text,
					    int offset, char[] match)
	{
		int length = offset + match.length;
		char[] textArray = text.array;
		if(length > textArray.length)
			return false;
		for(int i = offset, j = 0; i < length; i++, j++)
		{
			char c1 = textArray[i];
			char c2 = match[j];
			if(ignoreCase)
			{
				c1 = Character.toUpperCase(c1);
				c2 = Character.toUpperCase(c2);
			}
			if(c1 != c2)
				return false;
		}
		return true;
	}

	/**
	 * Finds the previous instance of an opening bracket in the buffer.
	 * The closing bracket is needed as well to handle nested brackets
	 * properly.
	 * @param doc The document to search in
	 * @param dot The starting position
	 * @param openBracket The opening bracket
	 * @param closeBracket The closing bracket
	 * @exception BadLocationException if `dot' is out of range
	 */
	public static int locateBracketBackward(Document doc, int dot,
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
			return start + offset;

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
				return start + offset;
		}

		// not found
		return -1;
	}

	/**
	 * Finds the next instance of a closing bracket in the buffer.
	 * The opening bracket is needed as well to handle nested brackets
	 * properly.
	 * @param doc The document to search in
	 * @param dot The starting position
	 * @param openBracket The opening bracket
	 * @param closeBracket The closing bracket
	 * @exception BadLocationException if `dot' is out of range
	 */
	public static int locateBracketForward(Document doc, int dot,
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
			return dot + offset + 1;

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
				return start + offset;
		}

		// not found
		return -1;
	}

	/**
	 * Returns the default style table. This can be passed to the
	 * <code>setStyles()</code> method of <code>SyntaxDocument</code>
	 * to use the default syntax styles.
	 */
	public static SyntaxStyle[] getDefaultSyntaxStyles()
	{
		SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];

		styles[Token.COMMENT1] = new SyntaxStyle(Color.black,true,false);
		styles[Token.COMMENT2] = new SyntaxStyle(new Color(0x990033),true,false);
		styles[Token.KEYWORD1] = new SyntaxStyle(Color.black,false,true);
		styles[Token.KEYWORD2] = new SyntaxStyle(Color.magenta,false,false);
		styles[Token.KEYWORD3] = new SyntaxStyle(new Color(0x009600),false,false);
		styles[Token.LITERAL1] = new SyntaxStyle(new Color(0x650099),false,false);
		styles[Token.LITERAL2] = new SyntaxStyle(new Color(0x650099),false,true);
		styles[Token.LABEL] = new SyntaxStyle(new Color(0x990033),false,true);
		styles[Token.OPERATOR] = new SyntaxStyle(Color.black,false,true);
		styles[Token.INVALID] = new SyntaxStyle(Color.red,false,true);

		return styles;
	}

	// private members
	private SyntaxUtilities() {}

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
 * Revision 1.6  1999/06/07 06:36:32  sp
 * Syntax `styling' (bold/italic tokens) added,
 * plugin options dialog for plugin option panes
 *
 * Revision 1.5  1999/06/05 00:22:58  sp
 * LGPL'd syntax package
 *
 * Revision 1.4  1999/04/19 05:38:20  sp
 * Syntax API changes
 *
 * Revision 1.3  1999/04/02 02:39:46  sp
 * Updated docs, console fix, getDefaultSyntaxColors() method, hypersearch update
 *
 * Revision 1.2  1999/03/27 02:46:17  sp
 * SyntaxTextArea is now modular
 *
 * Revision 1.1  1999/03/13 09:11:46  sp
 * Syntax code updates, code cleanups
 *
 */
