/*
 * format.java - Command
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

import com.sun.java.swing.text.BadLocationException;
import java.util.Hashtable;
import org.gjt.sp.jedit.*;

public class format implements Command
{
	public void exec(Buffer buffer, View view, String arg, Hashtable args)
	{
		Object o = buffer.getProperty("maxLineLen");
		int maxLineLength;
		if(o instanceof Integer)
			maxLineLength = ((Integer)o).intValue();
		else
			maxLineLength = 72;
		o = buffer.getProperty("minLineLen");
		int minLineLength;
		if(o instanceof Integer)
			minLineLength = ((Integer)o).intValue();
		else
			minLineLength = 20;
		if(arg == null)
			arg = view.getTextArea().getSelectedText();
		if(arg != null)
			view.getTextArea().replaceSelection(doFormat(arg,
				minLineLength,maxLineLength));
		else
		{
			try
			{
				arg = buffer.getText(0,buffer.getLength());
				buffer.remove(0,buffer.getLength());
				buffer.insertString(0,doFormat(arg,
					minLineLength,maxLineLength),null);
			}
			catch(BadLocationException bl)
			{
				return;
			}
		}
	}

	private String doFormat(String text, int minLineLength,
		int maxLineLength)
	{
		StringBuffer buf = new StringBuffer();
		StringBuffer word = new StringBuffer();
		int lineLength = 0;
		boolean newline = false;
		boolean newlineIgnore = false;
		// remove spaces from start of text
		boolean space = true;
		char[] chars = text.toCharArray();
		for(int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			lineLength++;
			switch(c)
			{
			case '\n':
				buf.append(word);		
				word.setLength(0);
				if(newlineIgnore)
					break;
				if(newline)
				{
					// already seen a newline.
					// start a new paragraph
					buf.append("\n\n");
					newline = false;
					newlineIgnore = true;
					lineLength = 0;
					break;
				}
				if(lineLength > maxLineLength ||
					lineLength < minLineLength)
				{
					buf.append(c);
					newline = false;
					newlineIgnore = false;
					lineLength = 0;
					break;
				}
				newline = true;
				newlineIgnore = false;
				space = true;
				buf.append(' ');
				break;
			case ' ':
				// multiple spaces are ignored
				if(space)
					break;
				// append word
				buf.append(word);
				word.setLength(0);
				space = true;
				buf.append(' ');
				break;
			default:
				space = newline = newlineIgnore = false;
				word.append(c);
				// do word wrap
				if(lineLength > maxLineLength)
				{
					buf.append('\n');
					// remove all spaces from start
					// of next line
					space = true;
					lineLength = 0;
				}
				break;
			}
		}
		buf.append(word);
		return buf.toString();
	}
}
