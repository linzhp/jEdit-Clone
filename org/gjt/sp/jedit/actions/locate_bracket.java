/*
 * locate_bracket.java
 * Copyright (C) 1998, 1999 Slava Pestov
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
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.jedit.*;

public class locate_bracket extends EditAction
{
	public locate_bracket()
	{
		super("locate-bracket");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		JEditTextArea textArea = view.getTextArea();

		String openBrackets = (String)buffer
			.getProperty("openBrackets");
		String closeBrackets = (String)buffer
			.getProperty("closeBrackets");
		if(closeBrackets.length() != openBrackets.length())
		{
			view.getToolkit().beep();
			return;
		}
		int dot = textArea.getCaretPosition();
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
				int[] match = TextUtilities.locateBracketForward(
					buffer,dot,bracket,closeBracket);
				if(match == null)
					view.getToolkit().beep();
				else
				{
					int offset = textArea.getLineStartOffset(match[0])
						+ match[1];
					textArea.setCaretPosition(offset + 1);
				}
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
				int[] match = TextUtilities.locateBracketBackward(
					buffer,dot,openBracket,bracket);
				if(match == null)
					view.getToolkit().beep();
				else
				{
					int offset = textArea.getLineStartOffset(match[0])
						+ match[1];
					textArea.setCaretPosition(offset + 1);
				}
			}
		}
		catch(BadLocationException bl)
		{
		}
	}
}
