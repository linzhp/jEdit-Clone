/*
 * set_filename_register.java
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

public class set_filename_register extends EditAction
{
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);

		String actionCommand = evt.getActionCommand();
		if(actionCommand == null || actionCommand.length() != 1)
		{
			view.pushStatus(jEdit.getProperty("view.status.set-filename-register"));
			view.getTextArea().getInputHandler().grabNextKeyStroke(this);
		}
		else
		{
			view.popStatus();

			char ch = actionCommand.charAt(0);
			if(ch == '\0')
			{
				view.getToolkit().beep();
				return;
			}

			Registers.setRegister(ch,new Registers.StringRegister(
				view.getBuffer().getPath()));
		}
	}
}
