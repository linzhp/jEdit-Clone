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
			openBrackets = "([{";
		String closeBrackets = (String)buffer
			.getProperty("closeBrackets");
		if(closeBrackets == null)
			closeBrackets = ")]}";
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
				int offset = buffer.locateBracketForward(dot,
					bracket,closeBracket);
				if(offset == -1)
					view.getToolkit().beep();
				else
					view.getTextArea().setCaretPosition(
						offset + 1);
				return;
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
				int offset = buffer.locateBracketBackward(dot,
					openBracket,bracket);
				if(offset == -1)
					view.getToolkit().beep();
				else
					view.getTextArea().setCaretPosition(
						offset + 1);
			}
		}
		catch(BadLocationException bl)
		{
		}
	}
}
