/*
 * prev_error.java - Action
 * Copyright (C) 1999 Slava Pestov
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

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.gui.Console;
import org.gjt.sp.jedit.*;

public class prev_error extends EditAction
{
	public prev_error()
	{
		super("prev-error");
	}

	public void actionPerformed(ActionEvent evt)
	{
		try
		{
			View view = getView(evt);
			Console console = view.getConsole();
			int errorNo = console.getCurrentError();
			CompilerError error = console.getError(--errorNo);
			if(error == null)
			{
				view.getToolkit().beep();
				return;
			}
			JOptionPane.showMessageDialog(view,error.getError(),
				jEdit.getProperty("error.title"),
				JOptionPane.INFORMATION_MESSAGE);
			console.setCurrentError(errorNo);
			Buffer buffer = error.openFile();
			int lineNo = error.getLineNo();
			int start = buffer.getDefaultRootElement()
				.getElement(lineNo).getStartOffset();
			if(view.getBuffer() == buffer)
				view.getTextArea().setCaretPosition(start);
			else
			{
				buffer.setCaretInfo(start,start);
				view.setBuffer(buffer);
				view.updateBuffersMenu();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
