/*
 * goto_marker.java
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
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.syntax.SyntaxTextArea;
import org.gjt.sp.jedit.*;

public class goto_marker extends EditAction
{
	public goto_marker()
	{
		super("goto-marker");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		SyntaxTextArea textArea = view.getTextArea();
		String arg = evt.getActionCommand();
		if(arg == null)
			arg = jEdit.input(view,"gotomarker","lastmarker");
		if(arg != null)
		{
			Marker marker = view.getBuffer().getMarker(arg);
			if(marker != null)
				textArea.select(marker.getStart(),
					marker.getEnd());
			else
				view.getToolkit().beep();
		}
	}
}
