/*
 * Cmd_clear_marker.java - Command
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

import com.sun.java.swing.JOptionPane;
import com.sun.java.swing.JTextArea;
import java.util.Hashtable;

public class Cmd_clear_marker implements Command
{
	public void init(Hashtable args)
	{
	}

	public void exec(Hashtable args)
	{
		String arg = (String)args.get(ARG);
		View view = (View)args.get(VIEW);
		if(view == null)
			return;
		JTextArea textArea = view.getTextArea();
		try
		{
			if(arg == null)
			{
				arg = (String)JOptionPane.showInputDialog(view,
					jEdit.props.getProperty(
					"clearmarker.message"),
					jEdit.props.getProperty(
					"clearmarker.title"),
					JOptionPane.QUESTION_MESSAGE);
			}
			if(arg != null)
				view.getBuffer().removeMarker(arg);
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException("Aiee!!! Text area"
				+ " out of sync");
		}
	}
}
