/*
 * netscape_open_url.java - Command
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

package org.gjt.sp.jedit.cmd;

import com.sun.java.swing.JOptionPane;
import java.io.IOException;
import java.util.Hashtable;
import org.gjt.sp.jedit.*;

public class netscape_open_url implements Command
{
	public void exec(Buffer buffer, View view, String arg, Hashtable args)
	{
		if(buffer.isDirty())
		{
			int result = JOptionPane.showConfirmDialog(view,
				jEdit.props.getProperty("savefirst.message"),
				jEdit.props.getProperty("savefirst.title"),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if(result == JOptionPane.YES_OPTION)
				buffer.save(view,null);
			else if(result == JOptionPane.CANCEL_OPTION)
				return;
		}
		String[] remoteArgs = { "netscape", "-remote",
			"openURL(" + buffer.getPath() + ")" };
		try
		{
			Process remote = Runtime.getRuntime()
				.exec(remoteArgs);
			if(remote.waitFor() != 0)
			{
				String[] netscapeArgs = { "netscape",
					buffer.getPath() };
				Runtime.getRuntime().exec(netscapeArgs);
			}
		}
		catch(IOException io)
		{
			String[] errorArgs = { io.toString() };
			jEdit.error(view,"ioerror",errorArgs);
		}
		catch(InterruptedException i)
		{
		}
	}
}
