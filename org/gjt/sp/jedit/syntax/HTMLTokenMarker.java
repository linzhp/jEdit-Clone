/*
 * HTMLTokenMarker.java - HTML token marker
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
import org.gjt.sp.jedit.jEdit;

public class HTMLTokenMarker extends TokenMarker
{
	// public members
	public static final String COMMENT = "comment";
	public static final String TAG = "tag";
	public static final String ENTITY = "entity";

	public Token markTokens(Segment line, int lineIndex)
	{
		ensureCapacity(lineIndex);
		lastToken = null;
		String token = lineIndex == 0 ? null : lineInfo[lineIndex - 1];
		int offset = line.offset;
		int lastOffset = offset;
		int length = line.count + offset;
		for(int i = offset; i < length; i++)
		{
			switch(line.array[i])
			{
			case '<':
				// check if it's a comment
				if(length - i > 3)
				{
					if(jEdit.regionMatches(false,line,i,
							       "<!--"))
					{
						token = COMMENT;
						addToken(i - lastOffset,null);
						lastOffset = i;
						break;
					}
				}
				if(token == null)
				{
					token = TAG;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				break;
			case '>':
				if(token == TAG)
				{
					token = null;
					addToken((i+1) - lastOffset,TAG);
					lastOffset = i + 1;
				}
				else if(token == COMMENT)
				{
					if(i >= 2 && jEdit.regionMatches(false,
						line,i - 2,"-->"))
					{
						token = null;
						addToken((i+1) - lastOffset,
							 COMMENT);
						lastOffset = i + 1;
					}
				}
				break;
			case '&':
				if(token == null)
				{
					token = ENTITY;
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				break;
			case ';':
				if(token == ENTITY)
				{
					token = null;
					addToken((i+1) - lastOffset,ENTITY);
					lastOffset = i + 1;
				}
				break;
			}
		}
		if(lastOffset != length)
			addToken(length - lastOffset,token);
		lineInfo[lineIndex] = token;
		lastLine = lineIndex;
		if(lastToken != null)
		{
			lastToken.nextValid = false;
			return firstToken;
		}
		else
			return null;
	}
}
