/*
 * PluginOptions.java - Plugin options dialog
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

package org.gjt.sp.jedit.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.gjt.sp.jedit.*;

/**
 * The global (editor-wide) settings dialog.
 * @author Slava Pestov
 * @version $Id$
 */
public class PluginOptions extends OptionsDialog
{
	public PluginOptions(View view)
	{
		super(view,jEdit.getProperty("plugin-options.title"));

		Class[] optionPanes = jEdit.getOptionPanes();
		for(int i = 0; i < optionPanes.length; i++)
		{
			Class clazz = optionPanes[i];

			try
			{
				OptionPane pane = (OptionPane)clazz
					.newInstance();
				addOptionPane(pane);
			}
			catch(Exception e)
			{
				System.out.println("Error creating option pane "
					+ clazz.getName());
				e.printStackTrace();
			}
		}

		GUIUtilities.hideWaitCursor(view);

		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		show();
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.2  1999/07/16 23:45:49  sp
 * 1.7pre6 BugFree version
 *
 * Revision 1.1  1999/06/07 06:36:32  sp
 * Syntax `styling' (bold/italic tokens) added,
 * plugin options dialog for plugin option panes
 *
 */
