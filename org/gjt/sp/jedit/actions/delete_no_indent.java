/*
 * delete_no_indent.java
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

public class delete_no_indent extends EditAction
{
	public delete_no_indent()
	{
		super("delete-no-indent");
	}

	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		if(!view.getTextArea().isEditable())
		{
			view.getToolkit().beep();
			return;
		}
		Buffer buffer = view.getBuffer();
		Element map = buffer.getDefaultRootElement();
		Element lineElement = map.getElement(map.getElementIndex(
			view.getTextArea().getCaretPosition()));
		int start = lineElement.getStartOffset();
		int end = lineElement.getEndOffset();
		try
		{
			String str = buffer.getText(start,end - start);
			int whitespace = start + TextUtilities
				.getLeadingWhiteSpace(str);
			buffer.remove(whitespace,end - whitespace - 1);
		}
		catch(BadLocationException bl)
		{
		}
	}
}
