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
		String openBrackets = (String)buffer.getProperty("indentOpenBrackets");
		String closeBrackets = (String)buffer.getProperty("indentCloseBrackets");
		if(openBrackets == null || closeBrackets == null
			|| openBrackets.length() != closeBrackets.length())
		{
			openBrackets = closeBrackets = "";
		}
		int tabSize = buffer.getTabSize();
		boolean noTabs = "yes".equals(buffer.getProperty("noTabs"));
		Element map = buffer.getDefaultRootElement();
		int index = map.getElementIndex(caret);
		if(index == 0)
			return false;
		Element lineElement = map.getElement(index);
		Element prevLineElement = null;
		int prevStart = 0;
		int prevEnd = 0;
		while(--index >= 0)
		{
			prevLineElement = map.getElement(index);
			prevStart = prevLineElement.getStartOffset();
			prevEnd = prevLineElement.getEndOffset();
			if(prevEnd - prevStart > 1)
				break;
		}
		if(prevLineElement == null)
			return false;
		try
		{
			int start = lineElement.getStartOffset();
			int end = lineElement.getEndOffset();
			String line = buffer.getText(start,end - start);
			String prevLine = buffer.getText(prevStart,prevEnd
				- prevStart);

			/*
			 * On the previous line,
			 * { should give us +1
			 * { fred } should give us 0
			 * } fred { should give us +1
			 */
			boolean prevLineStart = true; // False after initial indent
			int prevLineIndent = 0; // Indent width (tab expanded)
			int prevLineBrackets = 0; // Additional bracket indent
			for(int i = 0; i < prevLine.length(); i++)
			{
				char c = prevLine.charAt(i);
				switch(c)
				{
				case ' ':
					if(prevLineStart)
						prevLineIndent++;
					break;
				case '\t':
					if(prevLineStart)
					{
						prevLineIndent += (tabSize
							- (prevLineIndent
							% tabSize));
					}
					break;
				default:
					prevLineStart = false;
					if(closeBrackets.indexOf(c) != -1)
						prevLineBrackets = Math.max(
							prevLineBrackets-1,0);
					else if(openBrackets.indexOf(c) != -1)
						prevLineBrackets++;
					break;
				}
			}
	
			/**
			 * On the current line,
			 * { should give us 0
			 * } should give us -1
			 * } fred { should give us -1
			 * { fred } should give us 0
			 */
			boolean lineStart = true; // False after initial indent
			int lineIndent = 0; // Indent width (tab expanded)
			int lineWidth = 0; // White space count
			int lineBrackets = 0; // Additional bracket indent
			int lineOpenBrackets = 0; // Number of opening brackets
			for(int i = 0; i < line.length(); i++)
			{
				char c = line.charAt(i);
				switch(c)
				{
				case ' ':
					if(lineStart)
					{
						lineIndent++;
						lineWidth++;
					}
					break;
				case '\t':
					if(lineStart)
					{
						lineIndent += (tabSize
							- (lineIndent
							% tabSize));
						lineWidth++;
					}
					break;
				default:
					lineStart = false;
					if(closeBrackets.indexOf(c) != -1)
					{
						if(lineOpenBrackets != 0)
							lineOpenBrackets--;
						else
							lineBrackets--;
					}
					else if(openBrackets.indexOf(c) != -1)
						lineOpenBrackets++;
					break;
				}
			}
							
			prevLineIndent += (prevLineBrackets + lineBrackets)
				* tabSize;

			// Insert a tab if line already has correct indent
			if(lineIndent >= prevLineIndent)
				return false;

			// Do it
			buffer.remove(start,lineWidth);
			buffer.insertString(start,jEdit.createWhiteSpace(
				prevLineIndent,tabSize,noTabs),null);
			return true;
		}
		catch(BadLocationException bl)
		{
		}
		return false;
	}

	public TokenMarker createTokenMarker()
	{
		return null;
	}
}


