/*
 * SyntaxTextArea.java - jEdit's own text component
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

package org.gjt.sp.jedit.syntax;

import javax.swing.JEditorPane;
import java.awt.*;

public class SyntaxTextArea extends JEditorPane
{
	// public members
	public SyntaxTextArea(int columns, int rows)
	{
		setEditorKit(new SyntaxEditorKit());
		this.columns = columns;
		this.rows = rows;
	}

	// fix for horizontal scrollbar
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}

	public Dimension getPreferredSize()
	{
		FontMetrics fm = getToolkit().getFontMetrics(getFont());
		Dimension d = super.getPreferredSize();
		d.width = Math.max(d.width,columns * fm.charWidth('m'));
		d.height = Math.max(d.height,rows * fm.getHeight());
		return d;
	}

	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	// private members
	private int columns;
	private int rows;
}
