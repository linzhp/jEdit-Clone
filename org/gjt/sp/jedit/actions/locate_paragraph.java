/*
 * locate_paragraph.java
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

public class locate_paragraph extends EditAction
{
	public locate_paragraph()
	{
		super("locate-paragraph");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		int dot = view.getTextArea().getCaretPosition();
		Element map = buffer.getDefaultRootElement();
		int lineNo = map.getElementIndex(dot);
		int start = 0;
		int end = buffer.getLength();
		// scan backward, looking for zero-length element
		for(int i = lineNo; i >= 0; i--)
		{
			Element line = map.getElement(i);
			int elemStart = line.getStartOffset();
			if(elemStart + 1 == line.getEndOffset())
			{
				start = elemStart;
				break;
			}
		}
		// scan forward, loowing for zero-length element
		for(int i = lineNo + 1; i < map.getElementCount(); i++)
		{
			Element line = map.getElement(i);
			int elemStart = line.getStartOffset();
			if(elemStart + 1 == line.getEndOffset())
			{
				end = elemStart;
				break;
			}
		}
		view.getTextArea().select(start,end);
	}
}
