/*
 * autoindent.java - Auto indent editing mode
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

package org.gjt.sp.jedit.mode;

import javax.swing.text.*;
import org.gjt.sp.jedit.syntax.TokenMarker;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.View;

public class autoindent implements Mode
{
	// public members
	public void enter(Buffer buffer) {}

	public void leave(Buffer buffer) {}

	public boolean indentLine(Buffer buffer, View view, int caret)
	{
		int tabSize = buffer.getTabSize();
		boolean noTabs = "yes".equals(buffer.getProperty("noTabs"));
		Element map = buffer.getDefaultRootElement();
		int index = map.getElementIndex(caret);
		if(index == 0)
			return false;
		Element lineElement = map.getElement(index);
		Element prevLineElement = map.getElement(index - 1);
		try
		{
			int start = lineElement.getStartOffset();
			int len = lineElement.getEndOffset() - start - 1;
			int prevStart = prevLineElement.getStartOffset();
			int prevLen = prevLineElement.getEndOffset()
				- prevStart - 1;
			String indent = autoIndent(tabSize,buffer.getText(
				prevStart,prevLen),buffer.getText(start,len),
				noTabs);
			if(indent == null)
				return false;
			buffer.insertString(start,indent,null);
		}
		catch(BadLocationException bl)
		{
		}
		return true;
	}

	public TokenMarker createTokenMarker()
	{
		return null;
	}

	// private members
	private String autoIndent(int tabSize, String prevLine, String line,
		boolean noTabs)
	{
		int count = jEdit.getLeadingWhiteSpaceWidth(line,tabSize);
		int prevCount = jEdit.getLeadingWhiteSpaceWidth(prevLine,
			tabSize);
		if(prevCount <= count)
			return null;
		return jEdit.createWhiteSpace(prevCount - count,tabSize,noTabs);
	}
}
