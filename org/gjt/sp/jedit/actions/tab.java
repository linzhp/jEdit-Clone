/*
 * tab.java
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
import org.gjt.sp.jedit.syntax.SyntaxTextArea;
import org.gjt.sp.jedit.*;

public class tab extends EditAction
{
	public tab()
	{
		super("tab");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		try
		{
			SyntaxTextArea textArea = view.getTextArea();
			int start = textArea.getSelectionStart();
			int end = textArea.getSelectionEnd();
			Element map = buffer.getDefaultRootElement();
			Element lineElement = map.getElement(
				map.getElementIndex(textArea
				.getCaretPosition()));
			int lineStart = lineElement.getStartOffset();
			int lineEnd = lineElement.getEndOffset() - 1;
			if(start == end)
			{
				start = lineStart;
				end = lineEnd;
			}
			end -= start;
			int tabSize = buffer.getTabSize();
			String text = doTab(buffer.getText(start,end),
				tabSize);
			buffer.remove(start,end);
			buffer.insertString(start,text,null);
		}
		catch(BadLocationException bl)
		{
		}
	}

	private String doTab(String in, int tabSize)
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0, width = 0, whitespace = 0;
			i < in.length(); i++, width++)
		{
			switch(in.charAt(i))
			{
			case ' ':
				whitespace++;
				break;
			case '\t':
				int tab = tabSize - (width % tabSize);
				width += tab;
				whitespace += tab;
				break;
			case '\n':
				whitespace = width = 0;
				buf.append('\n');
				break;
			default:
				if(whitespace != 0)
				{
					if(whitespace >= tabSize / 2)
					{
						int indent = whitespace +
							((width-whitespace)
							% tabSize);
						int tabs = indent / tabSize;
						int spaces = indent % tabSize;
						while(tabs-- > 0)
							buf.append('\t');
						while(spaces-- > 0)
							buf.append(' ');
					}
					else
					{
						while(whitespace-- > 0)
							buf.append(' ');
					}
					whitespace = 0;
				}
				buf.append(in.charAt(i));
				break;
			}
		}	
                return buf.toString();
	}
}
