/*
 * goto_error.java - Action
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

import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.*;

public class goto_error extends EditAction
{
	public goto_error()
	{
		super("goto-error");
	}

	public void actionPerformed(ActionEvent evt)
	{
		try
		{
			int errorNo = Integer.parseInt(evt.getActionCommand());
			jEdit.setCurrentError(errorNo);
			CompilerError error = jEdit.getError(errorNo);
			View view = getView(evt);
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
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
