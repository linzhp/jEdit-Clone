/*
 * open_link.java
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

public class open_link extends EditAction
{
	public open_link()
	{
		super("open-link");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		int caret = view.getTextArea().getCaretPosition();
		Element map = buffer.getDefaultRootElement();
		Element lineElement = map.getElement(map.getElementIndex(caret));
		int start = lineElement.getStartOffset();
		String line;
		try
		{
			line = buffer.getText(start,lineElement.getEndOffset()
				- start);
		}
		catch(BadLocationException bl)
		{
			bl.printStackTrace();
			return;
		}
		int linkStart = -1;
		int linkEnd = -1;
		for(int i = caret - start - 1; i >= 0; i--)
		{
			if(line.charAt(i) == '|')
			{
				linkStart = i + 1;;
				break;
			}
		}
		for(int i = caret - start; i < line.length(); i++)
		{
			if(line.charAt(i) == '|')
			{
				linkEnd = i;
				break;
			}
		}
		if(linkStart != -1 && linkEnd != -1)
			jEdit.openFile(view,null,line.substring(linkStart,
				linkEnd),false,false);
		else
			view.getToolkit().beep();
	}
}
