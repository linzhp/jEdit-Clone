/*
 * paste_string_register.java
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
import org.gjt.sp.jedit.gui.HistoryModel;
import org.gjt.sp.jedit.*;

public class paste_string_register extends EditAction
{
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);

		if(!view.getTextArea().isEditable())
		{
			view.getToolkit().beep();
			return;
		}

		String actionCommand = evt.getActionCommand();
		if(actionCommand == null || actionCommand.length() != 1)
		{
			view.pushStatus(jEdit.getProperty("view.status.paste-string-register"));
			view.getInputHandler().grabNextKeyStroke(this);
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

			Registers.Register register = Registers.getRegister(ch);
			if(register == null)
			{
				view.getToolkit().beep();
				return;
			}
			else
			{
				String selection = register.toString();
				if(selection == null)
				{
					view.getToolkit().beep();
					return;
				}

				int repeatCount = view.getInputHandler().getRepeatCount();
				StringBuffer buf = new StringBuffer();
				for(int i = 0; i < repeatCount; i++)
					buf.append(selection);

				view.getTextArea().setSelectedText(buf.toString());
				HistoryModel.getModel("clipboard").addItem(selection);
			}
		}
	}

	public boolean isRepeatable()
	{
		return false;
	}
}
