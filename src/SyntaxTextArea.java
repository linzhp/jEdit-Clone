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

import com.sun.java.swing.JTextArea;

public class SyntaxTextArea extends JTextArea
{
	// public members
	public SyntaxTextArea(int width, int height)
	{
		super(width,height);
	}

	/*
	 * This will be a proper syntax colorizing text component
	 * in jEdit 1.2. I did the JTextArea->SyntaxTextArea changeover
	 * in jEdit 1.1 so that there's less work in 1.2
	 */
}
