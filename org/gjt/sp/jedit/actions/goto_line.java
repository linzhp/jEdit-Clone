/*
 * goto_line.java
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

import javax.swing.*;
import javax.swing.text.Element;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.syntax.SyntaxTextArea;
import org.gjt.sp.jedit.*;

public class goto_line extends EditAction
{
	public goto_line()
	{
		super("goto-line");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		SyntaxTextArea textArea = view.getTextArea();
		Element map = buffer.getDefaultRootElement();
		String line = (String)JOptionPane.showInputDialog(view,
			jEdit.getProperty("gotoline.message"),
		jEdit.getProperty("gotoline.title"),
			JOptionPane.QUESTION_MESSAGE,null,null,
			String.valueOf(map.getElementIndex(textArea
				.getCaretPosition()) + 1));
		if(line != null)
		{
			Element element = map.getElement(Integer
				.parseInt(line) - 1);
			if(element != null)
				view.getTextArea().setCaretPosition(element
					.getStartOffset());
		}
	}
}
