/*
 * TSQLTokenMarker.java - Transact-SQL token marker
 * Copyright (C) 1999 mike dillon
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

public class TSQLTokenMarker extends TokenMarker
{
	private int offset, lastOffset, lastKeyword, length;

	// public members
	public TSQLTokenMarker(KeywordMap keywords)
	{
		this.keywords = keywords;
	}

	public Token markTokens(Segment line, int lineIndex)
	{
		ensureCapacity(lineIndex);
		lastToken = null;
		String token = lineIndex == 0 ? null : lineInfo[lineIndex - 1];
		offset = lastOffset = lastKeyword = line.offset;
		length = line.count + offset;

loop:
		for(int i = offset; i < length; i++)
		{
			if (isKeywordCharacter(line.array[i])) continue;
			switch(line.array[i])
			{
			case '*':
				if(token == Token.COMMENT1 && length - i >= 1 && line.array[i+1] == '/')
				{
					token = null;
					i++;
					addToken((i + 1) - lastOffset,Token.COMMENT1);
					lastOffset = i + 1;
				}
				else if (token == null)
				{
					searchBack(line, i);
					addToken(1,Token.OPERATOR1);
					lastOffset = i + 1;
				}
				break;
			case '[':
				if(token == null)
				{
					searchBack(line, i);
					token = Token.LITERAL1;
					literalChar = '[';
					lastOffset = i;
				}
				break;
			case ']':
				if(token == Token.LITERAL1 && literalChar == '[')
				{
					token = null;
					literalChar = 0;
					addToken((i + 1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
				}
				break;
			case '.': case ',': case '(': case ')':
				if (token == null) {
					searchBack(line, i);
					addToken(1, null);
					lastOffset = i + 1;
				}
				break;
			case '+': case '%': case '&': case '|': case '^':
			case '~': case '<': case '>': case '=':
				if (token == null) {
					searchBack(line, i);
					addToken(1,Token.OPERATOR1);
					lastOffset = i + 1;
				}
				break;
			case ' ': case '\t':
				if (token == null) {
					searchBack(line, i, false);
				}
				break;
			case ':':
				if(token == null)
				{
					addToken((i+1) - lastOffset,Token.LABEL);
					lastOffset = i + 1;
				}
				break;
			case '/':
				if(token == null)
				{
					if (length - i >= 2 && line.array[i + 1] == '*')
					{
						searchBack(line, i);
						token = Token.COMMENT1;
						lastOffset = i;
						i++;
					}
					else
					{
						searchBack(line, i);
						addToken(1,Token.OPERATOR1);
						lastOffset = i + 1;
					}
				}
				break;
			case '-':
				if(token == null)
				{
					if (length - i >= 2 && line.array[i+1] == '-')
					{
						searchBack(line, i);
						addToken(length - i,Token.COMMENT1);
						lastOffset = length;
						break loop;
					}
					else
					{
						searchBack(line, i);
						addToken(1,Token.OPERATOR1);
						lastOffset = i + 1;
					}
				}
				break;
			case '!':
				if(token == null && length - i >= 2 &&
				(line.array[i+1] == '=' || line.array[i+1] == '<' || line.array[i+1] == '>'))
				{
					searchBack(line, i);
					addToken(1,Token.OPERATOR1);
					lastOffset = i + 1;
				}
				break;
			case '"': case '\'':
				if(token == null)
				{
					token = Token.LITERAL1;
					literalChar = line.array[i];
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == Token.LITERAL1 && literalChar == line.array[i])
				{
					token = null;
					literalChar = 0;
					addToken((i + 1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
				}
				break;
			default:
				break;
			}
		}
		if(token == null)
			searchBack(line, length, false);
		if(lastOffset != length)
			addToken(length - lastOffset,token);
		lineInfo[lineIndex] = token;
		if(lastToken != null)
		{
			lastToken.nextValid = false;
			return firstToken;
		}
		else
			return null;
	}

	// private members
	private KeywordMap keywords;
	private char literalChar = 0;

	private static boolean isKeywordCharacter(char c)
	{
		return Character.isLetter(c) || c == '_' || c == '@';
	}

	private void searchBack(Segment line, int pos)
	{
		searchBack(line, pos, true);
	}

	private void searchBack(Segment line, int pos, boolean padNull)
	{
		int len = pos - lastKeyword;
		String id = keywords.lookup(line,lastKeyword,len);
		if(id != null)
		{
			if(lastKeyword != lastOffset)
				addToken(lastKeyword - lastOffset,null);
			addToken(len,id);
			lastOffset = pos;
		}
		lastKeyword = pos + 1;
		if (padNull && lastOffset < pos)
			addToken(pos - lastOffset, null);
	}
}
