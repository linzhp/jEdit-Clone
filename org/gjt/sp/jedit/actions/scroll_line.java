/*
 * scroll_line.java
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

import javax.swing.text.Element;
import java.awt.event.ActionEvent;
import java.awt.Rectangle;
import org.gjt.sp.jedit.gui.SyntaxTextArea;
import org.gjt.sp.jedit.*;

public class scroll_line extends EditAction
{
	public scroll_line()
	{
		super("scroll-line");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		SyntaxTextArea textArea = view.getTextArea();
		Element map = view.getBuffer().getDefaultRootElement();
		int startLine = map.getElementIndex(textArea.getSelectionStart());
		int endLine = map.getElementIndex(textArea.getSelectionEnd()) + 1;
		int height = textArea.getToolkit().getFontMetrics(textArea
			.getFont()).getHeight();
		int scrollHeight = ((endLine - startLine) + 1) * height;
		int viewHeight = view.getSize().height;
		Rectangle rect = new Rectangle(0,startLine * height
			- ((viewHeight - scrollHeight) / 2),0,viewHeight);
		rect.height = Math.min(viewHeight,textArea.getSize().height
			- rect.y);
		textArea.scrollRectToVisible(rect);
	}
}
