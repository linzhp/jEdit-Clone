/*
 * PropsTokenMarker.java - Java props/DOS INI token marker
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
import org.gjt.sp.jedit.mode.javascript;
import org.gjt.sp.jedit.jEdit;

/**
 * Java properties/DOS INI token marker.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class PropsTokenMarker extends TokenMarker
{
	public static final byte VALUE = Token.INTERNAL_FIRST;

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
			case '#': case ';':
				if(i == offset && token == Token.NULL)
				{
					addToken(line.count,Token.COMMENT1);
					lastOffset = length;
					break loop;
				}
				break;
			case '[':
				if(i == offset && token == Token.NULL)
				{
					token = Token.KEYWORD2;
					addToken(i - lastOffset,Token.NULL);
					lastOffset = i;
				}
				break;
			case ']':
				if(token == Token.KEYWORD2)
				{
					token = Token.NULL;
					addToken(i1 - lastOffset,
						Token.KEYWORD2);
					lastOffset = i1;
				}
				break;
			case '=':
				if(token == Token.NULL)
				{
					token = VALUE; // Can't have [...] after =
					addToken(i - lastOffset,Token.KEYWORD1);
					lastOffset = i;
				}
				break;
			}
		}
		if(lastOffset != length)
			addToken(length - lastOffset,Token.NULL);
		return Token.NULL;
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.5  1999/06/05 00:22:58  sp
 * LGPL'd syntax package
 *
 * Revision 1.4  1999/06/03 08:24:14  sp
 * Fixing broken CVS
 *
 * Revision 1.5  1999/05/31 08:11:10  sp
 * Syntax coloring updates, expand abbrev bug fix
 *
 * Revision 1.4  1999/05/31 04:38:51  sp
 * Syntax optimizations, HyperSearch for Selection added (Mike Dillon)
 *
 * Revision 1.3  1999/04/19 05:38:20  sp
 * Syntax API changes
 *
 * Revision 1.2  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
