/*
 * toggle_line_numbers.java
 * Copyright (C) 2000 mike dillon
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
import org.gjt.sp.jedit.textarea.Gutter;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.View;

public class toggle_line_numbers extends EditAction
{
	public void actionPerformed(ActionEvent evt)
	{
		Gutter gutter = getView(evt).getTextArea().getGutter();
		gutter.setLineNumberingEnabled(!gutter.isLineNumberingEnabled());
	}

	public boolean isToggle()
	{
		return true;
	}

	public boolean isSelected(java.awt.Component comp)
	{
		return getView(comp).getTextArea().getGutter().isLineNumberingEnabled();
	}
}
