/*
 * EnhancedMenuItem.java - Menu item with user-specified accelerator string
 * Copyright (C) 1999 Slava Pestov
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

public class EnhancedMenuItem extends JMenuItem
{
	public EnhancedMenuItem(String label, String keyBinding)
	{
		super(label);
		this.keyBinding = keyBinding;

		acceleratorFont = UIManager
			.getFont("MenuItem.acceleratorFont");
		acceleratorForeground = UIManager
			.getColor("MenuItem.acceleratorForeground");
		acceleratorSelectionForeground = UIManager
			.getColor("MenuItem.acceleratorSelectionForeground");
		
	}

	public Dimension getPreferredSize()
	{
		Dimension d = super.getPreferredSize();
		if(keyBinding != null)
		{
			d.width += (getToolkit().getFontMetrics(acceleratorFont)
				.stringWidth(keyBinding) + 30);
		}
		return d;
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		if(keyBinding != null)
		{
			g.setFont(acceleratorFont);
			g.setColor(getModel().isSelected() ?
				acceleratorSelectionForeground :
				acceleratorForeground);
			FontMetrics fm = g.getFontMetrics();
			Insets insets = getInsets();
			g.drawString(keyBinding,getWidth() - (fm.stringWidth(
				keyBinding) + insets.right + insets.left),
				getFont().getSize() + (insets.top - 1)
				/* XXX magic number */);
		}
	}

	// private members
	private String keyBinding;
	private Font acceleratorFont;
	private Color acceleratorForeground;
	private Color acceleratorSelectionForeground;
}
