/*
 * execute.java
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

import java.awt.event.ActionEvent;
import java.io.*;
import org.gjt.sp.jedit.*;

public class execute extends EditAction
{
	public execute()
	{
		super("execute");
	}

	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		String command = jEdit.input(view,"execute","execute.cmd");
		if(command == null)
			return;
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < command.length(); i++)
		{
			switch(command.charAt(i))
			{
			case '%':
				if(i != command.length() - 1)
				{
					switch(command.charAt(++i))
					{
					case 'u':
						buf.append(buffer.getPath());
						break;
					case 'p':
						buf.append(buffer.getFile()
							.getPath());
						break;
					default:
						buf.append('%');
						break;
					}
					break;
				}
			default:
				buf.append(command.charAt(i));
			}
		}
		try
		{
			Process p = Runtime.getRuntime().exec(buf.toString());
			BufferedReader err = new BufferedReader(
				new InputStreamReader(p.getErrorStream()));
			buf.setLength(0);
			String line;
			while((line = err.readLine()) != null)
			{
				buf.append(line);
				buf.append('\n');
			}
			err.close();
			if(buf.length() != 0)
			{
				Object[] args = { buf.toString() };
				jEdit.error(view,"cmderr",args);
				return;
			}
		}
		catch(IOException io)
		{
			Object[] error = { io.toString() };
			jEdit.error(view,"ioerror",error);
		}
	}
}
