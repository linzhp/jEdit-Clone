/*
 * PatchTokenMarker.java - DIFF patch token marker
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
package org.gjt.sp.jedit.syntax;

import javax.swing.text.Segment;

public class PatchTokenMarker extends TokenMarker
{
	public Token markTokens(Segment line, int lineIndex)
	{
		lastToken = null;
		if(line.count == 0)
			return null;
		switch(line.array[line.offset])
		{
		case '+': case '>':
			addToken(line.count,Token.KEYWORD1);
			break;
		case '-': case '<':
			addToken(line.count,Token.KEYWORD2);
			break;
		case '@':
			addToken(line.count,Token.KEYWORD3);
			break;
	        default:
			addToken(line.count,null);
			break;
		}
		lastToken.nextValid = false;
		return firstToken;
	}
}
