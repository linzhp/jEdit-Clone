/*
 * ColorsOptionPane.java - Abstract colors options panel
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

package org.gjt.sp.jedit.options;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.gjt.sp.jedit.*;

public abstract class ColorsOptionPane extends OptionPane
implements ActionListener
{
	public ColorsOptionPane(String name)
	{
		super(name);
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		if(source instanceof JButton)
		{
			JButton button = (JButton)source;
			Color color = JColorChooser.showDialog(this,
				jEdit.getProperty("colorChooser.title"),
				button.getBackground());
			if(color != null)
				button.setBackground(color);
		}
	}

	// protected members
	protected JButton createColorButton(String property)
	{
		JButton button = new JButton("    ");
		button.setBackground(GUIUtilities.parseColor(jEdit
			.getProperty(property)));
		button.addActionListener(this);
		return button;
	}

	protected void saveColorButton(String property, JButton button)
	{
		int value = (button.getBackground().getRGB() & 0xffffff);
		jEdit.setProperty(property,'#' + Integer.toHexString(value));
	}
}
