/*
 * open_file.java
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

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.io.File;
import org.gjt.sp.jedit.*;

public class open_file extends EditAction
{
	public open_file()
	{
		super("open-file");
	}

	public void actionPerformed(ActionEvent evt)
	{
		String arg = evt.getActionCommand();
		View view = getView(evt);
		if(arg != null)
			jEdit.openFile(view,null,arg,false,false);
		else
		{
			JFileChooser chooser = new JFileChooser(view.getBuffer()
				.getFile().getParent());
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int retVal = chooser.showDialog(view,null);
			if(retVal == JFileChooser.APPROVE_OPTION)
			{
				File file = chooser.getSelectedFile();
				if(file != null)
					jEdit.openFile(view,null,file
						.getAbsolutePath(),false,false);
			}
		}
	}
}
