/*
 * Mode_autoindent.java - Auto indent editing mode
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

import com.sun.java.swing.text.*;

public class Mode_autoindent implements Mode
{
	private static String comments;
	
	public Mode_autoindent()
	{
	}

	public static void propertiesChanged()
	{
		comments = jEdit.props.getProperty("autoindentchars");
	}

	public void enter(Buffer buffer)
	{
		propertiesChanged();
	}

	public void leave(Buffer buffer) {}

	public boolean indentLine(Buffer buffer, View view, int caret)
	{
		int tabSize = view.getTextArea().getTabSize();
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
			String line = autoindent(tabSize,buffer.getText(
				prevStart,prevLen),buffer.getText(start,len));
			if(line == null)
				return false;
			buffer.remove(start,len);
			buffer.insertString(start,line,null);
		}
		catch(BadLocationException bl)
		{
		}
		return true;
	}

	private String autoindent(int tabSize, String prevLine, String line)
	{
		StringBuffer buf = new StringBuffer();
		int count = getWhiteSpace(tabSize,line);
		int prevCount = getWhiteSpace(tabSize,prevLine);
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
		buf.append(line);
		return buf.toString();
	}

	private int getWhiteSpace(int tabSize, String line)
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
