/*
 * select_prev_line.java
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

import javax.swing.text.Element;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.syntax.SyntaxTextArea;

public class select_prev_line extends EditAction
{
	public select_prev_line()
	{
		super("select-prev-line");
	}

	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		SyntaxTextArea textArea = view.getTextArea();
		Buffer buffer = view.getBuffer();
		Element map = buffer.getDefaultRootElement();
		Element lineElement = map.getElement(map.getElementIndex(
			textArea.getSelectionStart()));
		int start = Math.max(0,lineElement.getStartOffset() - 1);
		textArea.select(start,Math.max(textArea.getSelectionEnd(),
			lineElement.getEndOffset() - 1));
	}
}
