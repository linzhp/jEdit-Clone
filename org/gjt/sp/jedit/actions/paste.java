/*
 * paste.java
 * Copyright (C) 1998, 1999 Slava Pestov
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
 * You should have received a paste of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.actions;

import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.gui.HistoryModel;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.*;

public class paste extends EditAction
{
	public paste()
	{
		super("paste");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		JEditTextArea textArea = getView(evt).getTextArea();

		if(textArea.isEditable())
		{
			Clipboard clipboard = textArea.getToolkit()
				.getSystemClipboard();
			Transferable content = clipboard.getContents(this);

	        	if(content != null)
	        	{
		        	try
	                	{
					String text = (String)content
						.getTransferData(
						DataFlavor.stringFlavor);
					HistoryModel.getModel("clipboard")
						.addItem(text);

					// The MacOS MRJ doesn't convert
					// \r to \n, so do it here
					textArea.setSelectedText(
						text.replace('\r','\n'));
					return;
				}
				catch(Exception e)
				{
					textArea.getToolkit().beep();
				}
			}
		}

		textArea.getToolkit().beep();
	}
}
