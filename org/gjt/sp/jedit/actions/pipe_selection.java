/*
 * pipe_selection.java
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
import org.gjt.sp.jedit.syntax.SyntaxTextArea;

public class pipe_selection extends EditAction
{
	public pipe_selection()
	{
		super("pipe-selection");
	}

	public void actionPerformed(ActionEvent evt)
	{
		View view = getView(evt);
		SyntaxTextArea textArea = view.getTextArea();
		String input = textArea.getSelectedText();
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
			OutputStreamWriter out = new OutputStreamWriter(
				p.getOutputStream());
			BufferedReader in = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
			if(input != null)
				out.write(input);
			out.close();
			buf.setLength(0);
			String line;
			while((line = in.readLine()) != null)
			{
				buf.append(line);
				buf.append('\n');
			}
			in.close();
			view.getTextArea().replaceSelection(buf.toString());
		}
		catch(IOException io)
		{
			Object[] error = { io.toString() };
			jEdit.error(view,"ioerror",error);
		}
	}
}
