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

import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.io.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.SyntaxTextArea;

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
		int start = textArea.getSelectionStart();
		int end = textArea.getSelectionEnd();
		Buffer buffer = view.getBuffer();
		String command = GUIUtilities.inputProperty(view,"execute","console");
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
			boolean stripNewline;
			String input = buffer.getText(start,end-start);
			if(input != null && input.length() != 0)
			{
				stripNewline = (input.charAt(input.length()-1) != '\n');
				out.write(input);
			}
			else
				stripNewline = true;
			out.close();
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
				GUIUtilities.error(view,"cmderr",args);
				return;
			}
			BufferedReader in = new BufferedReader(
				new InputStreamReader(p.getInputStream()));
			while((line = in.readLine()) != null)
			{
				buf.append(line);
				buf.append('\n');
			}
			in.close();
			if(stripNewline && buf.length() != 0
				&& buf.charAt(buf.length()-1) == '\n')
				buf.setLength(buf.length()-1);
			buffer.remove(start,end-start);
			buffer.insertString(start,buf.toString(),null);
		}
		catch(IOException io)
		{
			Object[] error = { io.toString() };
			GUIUtilities.error(view,"ioerror",error);
		}
		catch(BadLocationException b)
		{
		}
	}
}
