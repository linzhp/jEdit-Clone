/*
 * browser_open_url.java
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
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.actions;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.gjt.sp.jedit.*;

public class browser_open_url extends EditAction
{
	public browser_open_url()
	{
		super("browser-open-url");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();

		if(buffer.isDirty())
		{
			String[] args = { buffer.getName() };
			int result = JOptionPane.showConfirmDialog(view,
				jEdit.getProperty("notsaved.message",args),
				jEdit.getProperty("notsaved.title"),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if(result == JOptionPane.YES_OPTION)
				buffer.save(view,null);
			else if(result != JOptionPane.NO_OPTION)
				return;
		}
		String[] args = { jEdit.getProperty("browser"),
			buffer.getPath() };
		try
		{
			Runtime.getRuntime().exec(args);
		}
		catch(IOException io)
		{
			String[] errorArgs = { io.toString() };
			jEdit.error(view,"wwwerror",errorArgs);
		}
	}
}
