/*
 * compile.java
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
import java.io.*;
import javax.swing.JOptionPane;
import org.gjt.sp.jedit.gui.CommandOutput;
import org.gjt.sp.jedit.*;

public class compile extends EditAction
{
	public compile()
	{
		super("compile");
	}

	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		Buffer buffer = view.getBuffer();
		String compiler = (String)buffer.getProperty("compiler");

		if(compiler != null && System.getProperty("os.name")
			.indexOf("Windows") != -1)
		{
			// append .exe to command name on Windows
			int dotIndex = compiler.indexOf('.');
			int spaceIndex = compiler.indexOf(' ');
			if(dotIndex == -1 || dotIndex > spaceIndex)
			{
				compiler = compiler.substring(0,spaceIndex)
					+ ".exe" + compiler.substring(spaceIndex);
			}
		}

		compiler = (String)JOptionPane.showInputDialog(view,
			jEdit.getProperty("compile.message"),
			jEdit.getProperty("compile.title"),
			JOptionPane.QUESTION_MESSAGE,null,null,
			compiler);
		if(compiler == null)
			return;
		buffer.putProperty("compile",compiler);
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < compiler.length(); i++)
		{
			switch(compiler.charAt(i))
			{
			case '%':
				if(i != compiler.length() - 1)
				{
					switch(compiler.charAt(++i))
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
				buf.append(compiler.charAt(i));
			}
		}
		compiler = buf.toString();
		try
		{
			Process proc = Runtime.getRuntime().exec(compiler);
			proc.getOutputStream().close();
			new CommandOutput(view,compiler,proc);
		}
		catch(IOException io)
		{
			Object[] args = { io.getMessage() };
			jEdit.error(view,"ioerror",args);
		}
	}
}
