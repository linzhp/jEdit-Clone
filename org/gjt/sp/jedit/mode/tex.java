/*
 * tex.java - TeX editing mode
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

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.*;

public class tex extends Mode
{
	public tex()
	{
		super("tex");
	}
	
	// For the amstex, latex subclasses
	protected tex(String name)
	{
		super(name);
	}
	
	public void enter(Buffer buffer)
	{
		/* We use a really simple algorithm to determine
		 * if the file is AMSTeX, LaTeX, or plain TeX.
		 *
		 * If the first 100 lines contain \begin, LaTeX
		 * mode is assumed. Otherwise, if \documentclass
		 * is found, AMSTeX is used. If both fail, plain
		 * TeX is used.
		 */
		Element map = buffer.getDefaultRootElement();
		int count = Math.min(100,map.getElementCount());
		boolean beginFound = false;
		boolean docStyleFound = false;
		
		try
		{
			for(int i = 0; i < count; i++)
			{
				Element lineElement = map.getElement(i);
				int start = lineElement.getStartOffset();
				int end = lineElement.getEndOffset();
				String line = buffer.getText(start,
					end - start - 1);
				if(line.indexOf("\\begin") != -1)
				{
					beginFound = true;
					break;
				}
				else if(line.indexOf("\\documentstyle") != -1)
					docStyleFound = true;
			}
		}
		catch(BadLocationException bl)
		{
			System.out.println("WARNING: Your computer's CPU is"
				+ " on fire. Reboot NOW!!!");
		}

		if(beginFound)
			buffer.setMode(jEdit.getMode("latex"));
		else if(docStyleFound)
			buffer.setMode(jEdit.getMode("amstex"));
	}

	public TokenMarker createTokenMarker()
	{
		return new TeXTokenMarker();
	}
}
