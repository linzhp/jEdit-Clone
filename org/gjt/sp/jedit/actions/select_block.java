/*
 * select_block.java
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
import javax.swing.text.Element;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.*;

public class select_block extends EditAction
{
	public select_block()
	{
		super("select-block");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		String openBrackets = (String)buffer
			.getProperty("openBrackets");
		String closeBrackets = (String)buffer
			.getProperty("closeBrackets");
		if(closeBrackets.length() != openBrackets.length())
		{
			view.getToolkit().beep();
			return;
		}
		int count;
		int blockStart = 0;
		int blockEnd = buffer.getLength();
		int selStart = view.getTextArea().getSelectionStart();
		int selEnd = view.getTextArea().getSelectionEnd();
		Element map = buffer.getDefaultRootElement();
		int lineNo = map.getElementIndex(selStart);
		Element lineElement = map.getElement(lineNo);
		int start = lineElement.getStartOffset();
		try
		{
			int offset = scanBackwardLine(buffer.getText(start,
				selStart - start),openBrackets,
						      closeBrackets,0);
			count = -1 - offset;
			if(offset >= 0)
			{
				blockStart = start + offset;
				count = 0;
			}
			else
			{
				for(int i = lineNo - 1; i >= 0; i--)
				{
					lineElement = map.getElement(i);
					start = lineElement.getStartOffset();
					offset = scanBackwardLine(buffer.getText(
						start,lineElement
						.getEndOffset() - start),
						openBrackets,closeBrackets,
						count);
					count = -1 - offset;
					if(offset >= 0)
					{
						blockStart = start + offset;
						count = 0;
						break;
					}
				}
			}
			lineNo = map.getElementIndex(selEnd);
			lineElement = map.getElement(lineNo);
			offset = scanForwardLine(buffer.getText(selEnd,
				lineElement.getEndOffset() - selEnd),
				openBrackets,closeBrackets,count);
			count = -1 - offset;
			if(offset >= 0)
			{
				blockEnd = selEnd + offset;
				count = 0;
			}
			else
			{
				for(int i = lineNo + 1; i < map.getElementCount();
				    i++)
				{
					lineElement = map.getElement(i);
					start = lineElement.getStartOffset();
					offset = scanForwardLine(buffer.getText(
						start,lineElement.getEndOffset()
						- start),openBrackets,
								 closeBrackets,
								 count);
					count = -1 - offset;
					if(offset >= 0)
					{
						blockEnd = start + offset;
						count = 0;
						break;
					}
				}
			}
		}
		catch(BadLocationException bl)
		{
		}
		view.getTextArea().select(blockStart,blockEnd);
	}

	private int scanBackwardLine(String line, String openBrackets,
		String closeBrackets, int count)
	{
		for(int i = line.length() - 1; i >= 0; i--)
		{
			char c = line.charAt(i);
			if(closeBrackets.indexOf(c) != -1)
				count++;
			else if(openBrackets.indexOf(c) != -1)
			{
				if(--count < 0)
					return i;
			}
		}
		return -1 - count;
	}

	private int scanForwardLine(String line, String openBrackets,
		String closeBrackets, int count)
	{
		for(int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			if(openBrackets.indexOf(c) != -1)
				count++;
			else if(closeBrackets.indexOf(c) != -1)
			{
				if(--count < 0)
					return i + 1;
			}
		}
		return -1 - count;
	}
}
