/*
 * rot13.java - Simple plugin
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

import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.gui.SyntaxTextArea;
import org.gjt.sp.jedit.*;

public class rot13 extends EditAction
{
	public rot13()
	{
		super("rot13",true);
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		SyntaxTextArea textArea = view.getTextArea();
		String selection = textArea.getSelectedText();
		if(selection != null)
			textArea.replaceSelection(doRot13(selection));
		else
			view.getToolkit().beep();
	}

	private String doRot13(String str)
	{
		char[] chars = str.toCharArray();
		for(int i = 0; i < chars.length; i++)
		{
			char c = chars[i];
			if(c >= 'a' && c <= 'z')
				c = (char)('a' + ((c - 'a') + 13) % 26);
			else if(c >= 'A' && c <= 'Z')
				c = (char)('A' + ((c - 'A') + 13) % 26);
			chars[i] = c;
		}
		return new String(chars);
	}
}
