/*
 * help.java - Action
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

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.HelpViewer;
import org.gjt.sp.util.Log;

public class help extends EditAction
{
	public void actionPerformed(ActionEvent evt)
	{
		String jEditHome = MiscUtilities.constructPath(
			jEdit.getJEditHome(),"doc");
		jEditHome = jEditHome.replace(File.separatorChar,'/');

		String actionCommand = evt.getActionCommand();
		if(actionCommand == null)
			actionCommand = "users-guide/index.html";

		if(!actionCommand.startsWith("jeditresource:"))
			actionCommand = "file:" + jEditHome + '/' + actionCommand;

		try
		{
			HelpViewer.gotoURL(new URL(actionCommand));
		}
		catch(MalformedURLException mf)
		{
			Log.log(Log.ERROR,this,mf);
		}
	}
}
