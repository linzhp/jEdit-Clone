/*
 * JavaLineEnumeration.java - Java line enumerator
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

package org.gjt.sp.jedit.mode;

import com.sun.java.swing.text.*;
import jstyle.JSLineEnumeration;
import org.gjt.sp.jedit.Buffer;

public class JavaLineEnumeration implements JSLineEnumeration
{
	private Buffer buffer;
	private Element map;
	private int line;
	
	public JavaLineEnumeration(Buffer buffer, Element map, int line)
	{
		this.buffer = buffer;
		this.map = map;
		this.line = line;
	}

	public boolean hasPreviousLines()
	{
		return line != 0;
	}

	public String previousLine()
	{
		Element lineElement = map.getElement(line--);
		int start = lineElement.getStartOffset();
		String retString;
		try
		{
			return buffer.getText(start,lineElement
				.getEndOffset() - start - 1);
		}
		catch (BadLocationException bl)
		{
			bl.printStackTrace();
			return "";
		}
	}
}
