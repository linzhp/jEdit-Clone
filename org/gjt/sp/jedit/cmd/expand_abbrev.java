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

import com.sun.java.swing.text.BadLocationException;
import com.sun.java.swing.text.Element;
import java.util.Hashtable;
import org.gjt.sp.jedit.*;

public class expand_abbrev implements Command
{
	public void exec(Buffer buffer, View view, String arg, Hashtable args)
	{
		String separators = (String)buffer.getProperty("ws");
		if(separators == null)
			separators = ".,:;!?";
		int dot = view.getTextArea().getCaretPosition();
		Element map = buffer.getDefaultRootElement();
		Element lineElement = map.getElement(map.getElementIndex(dot));
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset() - start;
		String line;
		try
		{
			line = buffer.getText(start,end);
		}
		catch(BadLocationException bl)
		{
			return;
		}
		dot -= start;
		int wordStart = start;
		int wordEnd = end;
loop1:		for(wordStart = dot - 1; wordStart >= 0; wordStart--)
		{
			char c = line.charAt(wordStart);
			switch(c)
			{
			case ' ': case '\t': case '\n':
				break loop1;
			default:
				if(separators.indexOf(c) != -1)
					break loop1;
				else
					break;
			}
		}
loop2:		for(wordEnd = dot; wordEnd < end; wordEnd++)
		{
			char c = line.charAt(wordEnd);
			switch(c)
			{
			case ' ': case '\t': case '\n':
				break loop2;
			default:
				if(separators.indexOf(c) != -1)
					break loop2;
				else
					break;
			}
		}
		try
		{
			wordStart++;
			wordEnd = wordEnd - wordStart;
			wordStart = start + wordStart;
			String expansion = jEdit.props.getProperty("abbrev."
				.concat(buffer.getText(wordStart,wordEnd)));
			if(expansion != null)
			{
				buffer.remove(wordStart,wordEnd);
				buffer.insertString(wordStart,expansion,null);
			}
			else
				view.getToolkit().beep();
		}
		catch(BadLocationException bl)
		{
		}
	}
}
