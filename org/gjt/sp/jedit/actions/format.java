/*
 * format.java
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

package org.gjt.sp.jedit.actions;

import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.*;

public class format extends EditAction
{
	public format()
	{
		super("format");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		Object o = buffer.getProperty("maxLineLen");
		int maxLineLength;
		if(o instanceof Integer)
			maxLineLength = ((Integer)o).intValue();
		else
			maxLineLength = 72;
		String text = view.getTextArea().getSelectedText();
		if(text != null)
			view.getTextArea().replaceSelection(doFormat(text,
				maxLineLength));
		else
		{
			try
			{
				text = buffer.getText(0,buffer.getLength());
				buffer.remove(0,buffer.getLength());
				buffer.insertString(0,doFormat(text,
					maxLineLength),null);
			}
			catch(BadLocationException bl)
			{
				return;
			}
		}
	}

	private String doFormat(String text, int maxLineLength)
	{
		StringBuffer buf = new StringBuffer();
		StringBuffer word = new StringBuffer();
		int lineLength = 0;
		boolean newline = true;
		boolean newlineIgnore = false;
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
				space = true;
				if(i == 0)
				{
					buf.append('\n');
					newline = false;
					newlineIgnore = true;
					break;
				}
				if(newlineIgnore)
				{
					lineLength--;
					break;
				}
				if(newline || i == chars.length - 2)
				{
					// already seen a newline.
					// start a new paragraph
					buf.append("\n\n");
					newline = false;
					newlineIgnore = true;
					// this is NOT 0, but rather the
					// number of characters already read
					// before the wrap
					lineLength = word.length();
					break;
				}
				if(lineLength >= maxLineLength ||
					i == chars.length - 1)
				{
					buf.append(c);
					newline = false;
					newlineIgnore = false;
					lineLength = 0;
					break;
				}
				newline = true;
				newlineIgnore = false;
				buf.append(' ');
				break;
			case ' ':
				if(space)
				{
					lineLength--;
					break;
				}
				buf.append(word);
				word.setLength(0);
				buf.append(' ');
				space = true;
				break;
			default:
				newline = newlineIgnore = space = false;
				word.append(c);
				// do word wrap
				if(lineLength >= maxLineLength)
				{
					buf.append('\n');
					// see case '\n' for explanation
					lineLength = word.length();
				}
				break;
			}
		}
		buf.append(word);
		return buf.toString();
	}
}
