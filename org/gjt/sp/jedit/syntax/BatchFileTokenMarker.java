/*
 * BatchFileTokenMarker.java - Batch file token marker
 * Copyright (C) 1998, 1999 Slava Pestov
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 */
package org.gjt.sp.jedit.syntax;

import javax.swing.text.Segment;

/**
 * Batch file token marker.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class BatchFileTokenMarker extends TokenMarker
{
	public byte markTokensImpl(byte token, Segment line, int lineIndex)
	{
		char[] array = line.array;
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
loop:		for(int i = offset; i < length; i++)
		{
			int i1 = (i+1);

			switch(array[i])
			{
			case '%':
				if(token == Token.NULL)
				{
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
					if(length - i <= 3 || array[i+2]
					   == ' ')
					{
						addToken(2,Token.KEYWORD2);
						i++;
						lastOffset = i1;
					}
					else
						token = Token.KEYWORD2;
				}
				else if(token == Token.KEYWORD2)
				{
					token = Token.NULL;
					addToken(i1 - lastOffset,Token.KEYWORD2);
					lastOffset = i1;
				}
				break;
			case '"':
				if(token == Token.NULL)
				{
					token = Token.LITERAL1;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				else if(token == Token.LITERAL1)
				{
					token = Token.NULL;
					addToken(i1 - lastOffset,Token.LITERAL1);
					lastOffset = i1;
				}
				break;
			case '=':
				if(token == Token.NULL && lastOffset == offset)
				{
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				break;
			case ':':
				if(i == offset)
				{
					addToken(line.count,Token.LABEL);
					lastOffset = length;
					break loop;
				}
				break;
			case ' ':
				if(lastOffset == offset)
				{
					if(i > 2 && SyntaxUtilities
						.regionMatches(true,line,
					       i - 3,"rem"))
					{
						addToken(length - lastOffset,
							 Token.COMMENT1);
						lastOffset = length;
						break loop;
					}
					else if(token == Token.NULL)
					{
						addToken(i - lastOffset,
							 Token.KEYWORD1);
						lastOffset = i;
					}
				}
				break;
			}
		}
		if(lastOffset != length)
		{
			if(token != Token.NULL)
				token = Token.INVALID;
			else if(lastOffset == offset)
				token = Token.KEYWORD1;
			addToken(length - lastOffset,token);
		}
		return Token.NULL;
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.16  1999/06/05 00:22:58  sp
 * LGPL'd syntax package
 *
 * Revision 1.15  1999/06/03 08:24:13  sp
 * Fixing broken CVS
 *
 * Revision 1.16  1999/05/31 08:11:10  sp
 * Syntax coloring updates, expand abbrev bug fix
 *
 * Revision 1.15  1999/05/31 04:38:51  sp
 * Syntax optimizations, HyperSearch for Selection added (Mike Dillon)
 *
 * Revision 1.14  1999/04/19 05:38:20  sp
 * Syntax API changes
 *
 * Revision 1.13  1999/03/26 05:13:04  sp
 * Enhanced menu item updates
 *
 * Revision 1.12  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.11  1999/03/13 00:09:07  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 * Revision 1.10  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
