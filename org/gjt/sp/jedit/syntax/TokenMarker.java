/*
 * TokenMarker.java - Generic token marker
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
package org.gjt.sp.jedit.syntax;

import javax.swing.text.Segment;
import java.util.*;

/**
 * A token marker that splits lines of text into tokens. Each token carries
 * a length field and an indentification tag that can be mapped to a color
 * for painting that token.
 * <p>
 * For performance reasons, the linked list of tokens is reused after each
 * line is tokenized. Therefore, the <code>Token.nextValid</code> flag is
 * necessary - a token instance might exist, but it might not actually be
 * valid.
 * @see org.gjt.sp.jedit.Token
 */
public abstract class TokenMarker
{
	// public members

	/**
	 * Creates a new <code>TokenMarker</code>. This calls the
	 * <code>init()</code> method.
	 */
	public TokenMarker()
	{
		init();
	}

	/**
	 * Clears the token marker's state. This should be called before
	 * a new document is to be tokenized.
	 */
	public void init()
	{
		lineInfo = new String[1000];
		length = 0;
		lastToken = null;
	}

	/**
	 * An abstract method that is called to split a line up into
	 * tokens.
	 * <p>
	 * At the start of this method, <code>lastToken</code> should
	 * be set to null so that <code>addToken()</code> is aware that
	 * a new line is being tokenized. At the end, <code>firstToken</code>
	 * should be returned. Tokens can be added to the list with
	 * <code>addToken()</code>.
	 * @param line The line
	 * @param lineIndex The line number
	 */
	public abstract Token markTokens(Segment line, int lineIndex);

	/**
	 * Informs the token marker that lines have been inserted into
	 * the document. This inserts a gap in the <code>lineInfo</code>
	 * array.
	 * @param lineIndex The line number
	 * @param lines The number of lines 
	 */
	public void insertLines(int lineIndex, int lines)
	{
		if(--lines <= 0) // Lines seems to be +=1...
			return;
		int len = lineIndex + lines;
		length += lines;
		ensureCapacity(len);
		System.arraycopy(lineInfo,lineIndex,lineInfo,len,
			lineInfo.length - len);
	}
	
	/**
	 * Informs the token marker that line have been deleted from
	 * the document. This removes the lines in question from the
	 * <code>lineInfo</code> array.
	 * @param lineIndex The line number
	 * @param lines The number of lines
	 */
	public void deleteLines(int lineIndex, int lines)
	{
		if (--lines <= 0) // Lines seems to be +=1...
			return;
		int len = lineIndex + lines;
		length -= lines;
		System.arraycopy(lineInfo,len,lineInfo,
			lineIndex,lineInfo.length - len);
	}

	// protected members

	/**
	 * The first token in the list. This should be used as the return
	 * value from <code>markTokens()</code>.
	 */
	protected Token firstToken;

	/**
	 * The last token in the list. New tokens are added here.
	 * This should be set to null before a new line is to be tokenized.
	 */
	protected Token lastToken;

	/**
	 * An array for storing information about lines. It is enlarged and
	 * shrunk automatically by the <code>insertLines()</code> and
	 * <code>deleteLines()</code> methods.
	 */
	protected String[] lineInfo;

	/**
	 * The length of the <code>lineInfo</code> array.
	 */
	protected int length;

	/**
	 * Ensures that the <code>lineInfo</code> array can contain the
	 * specified index. This enlarges it if necessary. No action is
	 * taken if the array is large enough already.
	 * @param index The array index
	 */
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

	/**
	 * Adds a token to the list.
	 * @param length The length of the token
	 * @param id The id of the token
	 */
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
