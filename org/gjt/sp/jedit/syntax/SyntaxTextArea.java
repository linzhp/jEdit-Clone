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

import com.sun.java.swing.JEditorPane;
import java.awt.*;

public class SyntaxTextArea extends JEditorPane
{
	// public members
	public SyntaxTextArea(int rows, int columns)
	{
		this.rows = rows;
		this.columns = columns;
		setEditorKit(new SyntaxEditorKit());
	}

	public Dimension getPreferredSize()
	{
		Dimension d = super.getPreferredSize();
		FontMetrics fm = getToolkit().getFontMetrics(getFont());
		d.width = Math.max(d.width,fm.charWidth('m') * columns);
		d.height = Math.max(d.height,fm.getHeight() * rows);
		return d;
	}

	public Dimension getMinimumSize()
	{
		FontMetrics fm = getToolkit().getFontMetrics(getFont());
		Dimension d = new Dimension();
		d.width = fm.charWidth('m') * columns;
		d.height = fm.getHeight() * rows;
		return d;
	}

	// private members
	private int columns;
	private int rows;
}