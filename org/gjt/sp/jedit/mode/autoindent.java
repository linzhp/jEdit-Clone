/*
 * autoindent.java - Auto indent editing mode
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

package org.gjt.sp.jedit.mode;

import com.sun.java.swing.text.*;
import jstyle.JSTokenMarker;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.View;

public class autoindent implements Mode
{
	// public members
	public void enter(Buffer buffer) {}

	public void leave(Buffer buffer) {}

	public boolean indentLine(Buffer buffer, View view, int caret)
	{
		String comments = (String)buffer.getProperty("autoIndentChars");
		if(comments == null)
			comments = "#->%";
		int tabSize = buffer.getTabSize();
		Element map = buffer.getDefaultRootElement();
		int index = map.getElementIndex(caret);
		if(index == 0)
			return false;
		Element lineElement = map.getElement(index);
		Element prevLineElement = map.getElement(index - 1);
		try
		{
			int start = lineElement.getStartOffset();
			int len = lineElement.getEndOffset() - start - 1;
			int prevStart = prevLineElement.getStartOffset();
			int prevLen = prevLineElement.getEndOffset()
				- prevStart - 1;
			String indent = autoIndent(tabSize,buffer.getText(
				prevStart,prevLen),buffer.getText(start,len),
				comments);
			if(indent == null)
				return false;
			buffer.insertString(start,indent,null);
		}
		catch(BadLocationException bl)
		{
		}
		return true;
	}

	public JSTokenMarker createTokenMarker()
	{
		return null;
	}

	// private members
	private String autoIndent(int tabSize, String prevLine, String line,
		String comments)
	{
		StringBuffer buf = new StringBuffer();
		int count = getWhiteSpace(tabSize,line,comments);
		int prevCount = getWhiteSpace(tabSize,prevLine,comments);
		if(prevCount <= count)
			return null;
		else
		{
			prevCount -= count;
			count = prevCount / 8;
			while(count-- > 0)
				buf.append('\t');
			count = prevCount % 8;
			while(count-- > 0)
				buf.append(' ');
		}
		return buf.toString();
	}

	private int getWhiteSpace(int tabSize, String line, String comments)
	{
		int count = 0;
loop:		for(int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			switch(c)
			{
			case ' ':
				count++;
				break;
			case '\t':
				count += (tabSize - count % tabSize);
				break;
			default:
				if(comments.indexOf(c) == -1)
					break loop;
				count++;
				break;
			}
		}
		return count;
	}
}
