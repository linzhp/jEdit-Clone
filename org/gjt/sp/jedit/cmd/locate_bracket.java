/*
 * locate_bracket.java - Command
 * Copyright (C) 1998 Slava Pestov
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

package org.gjt.sp.jedit.cmd;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.util.Hashtable;
import org.gjt.sp.jedit.*;

public class locate_bracket implements Command
{
	public void exec(Buffer buffer, View view, String arg, Hashtable args)
	{
		String openBrackets = (String)buffer
			.getProperty("openBrackets");
		if(openBrackets == null)
			openBrackets = "<([{";
		String closeBrackets = (String)buffer
			.getProperty("closeBrackets");
		if(closeBrackets == null)
			closeBrackets = ">)]}";
		if(closeBrackets.length() != openBrackets.length())
		{
			view.getToolkit().beep();
			return;
		}
		int dot = view.getTextArea().getCaretPosition();
		if(dot != 0)
			dot--;
		char bracket;
		try
		{
			bracket = buffer.getText(dot,1).charAt(0);
			int index = openBrackets.indexOf(bracket);
			if(index != -1)
			{
				char closeBracket = closeBrackets.charAt(index);
				scanForward(buffer,view,dot,bracket,
					closeBracket);
			}
			else
			{
				index = closeBrackets.indexOf(bracket);
				if(index == -1)
				{
					view.getToolkit().beep();
					return;
				}
				char openBracket = openBrackets.charAt(index);
				scanBackward(buffer,view,dot,openBracket,
					bracket);
			}
		}
		catch(BadLocationException bl)
		{
			view.getToolkit().beep();
			return;
		}
	}

	private void scanBackward(Buffer buffer, View view, int dot,
		char openBracket, char closeBracket)
		throws BadLocationException
	{
		int count;
		Element map = buffer.getDefaultRootElement();
		// check current line
		int lineNo = map.getElementIndex(dot);
		Element lineElement = map.getElement(lineNo);
		int start = lineElement.getStartOffset();
		int offset = scanBackwardLine(buffer.getText(start,dot
			- start),openBracket,closeBracket,0);
		count = -offset - 1;
		if(offset >= 0)
		{
			offset += start;
			view.getTextArea().setCaretPosition(offset + 1);
			return;
		}
		// check previous lines
		for(int i = lineNo - 1; i >= 0; i--)
		{
			lineElement = map.getElement(i);
			start = lineElement.getStartOffset();
			offset = scanBackwardLine(buffer.getText(start,
				lineElement.getEndOffset() - start),
				openBracket,closeBracket,count);
			count = -offset - 1;
			if(offset >= 0)
			{
				offset += start;
				view.getTextArea().setCaretPosition(offset+1);
				return;
			}
		}
		// not found
		view.getToolkit().beep();
	}
	
	// the return value is as follows:
	// >= 0: offset in line where bracket was found
	// < 0: -1 - count
	private int scanBackwardLine(String line, char openBracket,
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

	private void scanForward(Buffer buffer, View view, int dot,
		char openBracket, char closeBracket)
		throws BadLocationException
	{
		int count;
		Element map = buffer.getDefaultRootElement();
		// check current line
		int lineNo = map.getElementIndex(dot);
		Element lineElement = map.getElement(lineNo);
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset();
		int offset = scanForwardLine(buffer.getText(dot + 1,end
			- (dot + 1)),openBracket,closeBracket,0);
		count = -offset - 1;
		if(offset >= 0)
		{
			offset += (dot + 1);
			view.getTextArea().setCaretPosition(offset + 1);
			return;
		}
		// check following lines
		for(int i = lineNo + 1; i < map.getElementCount(); i++)
		{
			lineElement = map.getElement(i);
			start = lineElement.getStartOffset();
			offset = scanForwardLine(buffer.getText(start,
				lineElement.getEndOffset() - start),
				openBracket,closeBracket,count);
			count = -offset - 1;
			if(offset >= 0)
			{
				offset += start;
				view.getTextArea().setCaretPosition(offset+1);
				return;
			}
		}
		// not found
		view.getToolkit().beep();
	}

	// the return value is as follows:
	// >= 0: offset in line where bracket was found
	// < 0: -1 - count
	private int scanForwardLine(String line, char openBracket,
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
