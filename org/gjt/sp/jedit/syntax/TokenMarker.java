/*
 * TokenMarker.java - Generic token marker
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

import javax.swing.text.Segment;
import java.util.*;

public abstract class TokenMarker
{
	// public members
	public TokenMarker()
	{
		init();
	}

	public void init()
	{
		lineInfo = new String[100];
		lastLine = -1;
		length = 0;
		lastToken = null;
	}

	public abstract Token markTokens(Segment line, int lineIndex);

	public void insertLine(int lineIndex)
	{
		length = Math.max(length,lineIndex);
		ensureCapacity(length);
		System.arraycopy(lineInfo,lineIndex,lineInfo,lineIndex + 1,
			length - lineIndex);
	}
		
	public void deleteLine(int lineIndex)
	{
		length = Math.max(length,lineIndex);
		ensureCapacity(length);
		System.arraycopy(lineInfo,lineIndex + 1,lineInfo,lineIndex,
			length - lineIndex);
	}

	// protected members
	protected int lastLine;
	protected Token firstToken;
	protected Token lastToken;
	protected String[] lineInfo;
	protected int length;

	protected void ensureCapacity(int index)
	{
		if(lineInfo.length <= index)
		{
			String[] lineInfoN = new String[(index + 1) * 2];
			System.arraycopy(lineInfo,0,lineInfoN,0,
					 lineInfo.length);
			lineInfo = lineInfoN;
		}
	}

	protected void addToken(int length, String id)
	{
		if(firstToken == null)
		{
			firstToken = new Token(length,id);
			lastToken = firstToken;
		}
		else if(lastToken == null)
		{
			lastToken = firstToken;
			firstToken.length = length;
			firstToken.id = id;
		}
		else if(lastToken.next == null)
		{
			lastToken.next = new Token(length,id);
			lastToken.nextValid = true;
			lastToken = lastToken.next;
		}
		else
		{
			lastToken.nextValid = true;
			lastToken = lastToken.next;
			lastToken.length = length;
			lastToken.id = id;
		}
	}
}
