/*
 * java_mode.java - Java editing mode
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

import com.sun.java.swing.text.Element;
import jstyle.*;
import org.gjt.sp.jedit.*;

public class java_mode implements Mode
{
	private JSLineBeautifier beautifier;
	
	public java_mode()
	{
		beautifier = new jstyle.JSLineBeautifier();
	}

	public void enter(Buffer buffer)
	{
		buffer.setTokenMarker(new JSJavaTokenMarker());
		buffer.loadColors("java");
	}

	public void leave(Buffer buffer)
	{
		buffer.setTokenMarker(null);
		buffer.clearColors();
	}

	public boolean indentLine(Buffer buffer, View view, int caret)
	{
		Element map = buffer.getDefaultRootElement();
		int index = map.getElementIndex(caret);
		JavaLineEnumeration enum = new JavaLineEnumeration(buffer,map,
			index);
		String line;
		synchronized(this)
		{
			beautifier.init();
			beautifier.setTabIndentation(buffer.getTabSize());
			beautifier.setBracketIndent(buffer.getProperty("ib")
				!= null);
			beautifier.setSwitchIndent(buffer.getProperty("fs")
				!= null);
			line = beautifier.beautifyLine(enum);
		}
		Element lineElement = map.getElement(index);
		try
		{
			int offset = lineElement.getStartOffset();
			int len = lineElement.getEndOffset() - offset - 1;
			buffer.remove(offset,len);
			buffer.insertString(offset,line,null);
			view.getTextArea().setCaretPosition(caret + (line
				.length() - len));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
