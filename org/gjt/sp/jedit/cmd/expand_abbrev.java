/*
 * expand_abbrev.java - Command
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

public class expand_abbrev implements Command
{
	public void exec(Buffer buffer, View view, String arg, Hashtable args)
	{
		String separators = (String)buffer.getProperty("wordSep");
		if(separators == null)
			separators = ".,:;!?";
		int dot = view.getTextArea().getSelectionStart();
		Element map = buffer.getDefaultRootElement();
		int lineNo = map.getElementIndex(dot);
		Element lineElement = map.getElement(lineNo);
		int start = lineElement.getStartOffset();
		int len = lineElement.getEndOffset() - start;
		String line;
		try
		{
			line = buffer.getText(start,len); // chop newline
			// scan backwards to find word
			int wordStart = start;
loop:			for(int i = dot - 1; i >= start; i--)
			{
				char c = line.charAt(i - start);
				switch(c)
				{
				case ' ':
				case '\t':
					wordStart = i + 1;
					break loop;
				default:
					if(separators.indexOf(c) != -1)
					{
						wordStart = i;
						break loop;
					}
				}
			}
			if(wordStart == dot)
			{
				view.getToolkit().beep();
				return;
			}
			String word = line.substring(wordStart - start,
						     dot - start);
			// loop through lines in file looking for previous
			// occurance of word
			for(int i = lineNo - 1; i >= 0; i--)
			{
				lineElement = map.getElement(i);
				int lineStart = lineElement.getStartOffset();
				int lineLen = lineElement.getEndOffset()
					- lineStart;
				line = buffer.getText(lineStart, lineLen);
				int index = line.indexOf(word);
				if(index != -1)
				{
					int wordEnd = lineLen;
loop2:					for(int j = index + 1; j < lineLen; j++)
					{
						char c = line.charAt(j);
						switch(c)
						{
						case ' ':
						case '\t':
							wordEnd = j;
							break loop2;
						default:
							if(separators.indexOf(c)
								!= -1)
							{
								wordEnd = j;
								break loop2;
							}
						}
					}
					view.getTextArea().replaceSelection(
						line.substring(index +
							word.length(),
							wordEnd));
					return;
				}
			}
		}
		catch(BadLocationException bl)
		{
		}
		view.getToolkit().beep();
	}
}
