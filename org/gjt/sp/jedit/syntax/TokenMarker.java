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
 * for painting that token.<p>
 *
 * For performance reasons, the linked list of tokens is reused after each
 * line is tokenized. Therefore, the return value of <code>markTokens</code>
 * should only be used for immediate painting. Notably, it cannot be
 * cached.<p>
 *
 * <b>Note:</b> when using the token marker in your own code, you
 * <b>must</b> call <code>insertLines()</code> with the total number
 * of lines in the document to be tokenized, otherwise various problems
 * will occur. This should only be done once per document.
 *
 * @author Slava Pestov
 * @version $Id$
 *
 * @see org.gjt.sp.jedit.syntax.Token
 */
public abstract class TokenMarker
{
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
	 * @param index The first line number
	 * @param lines The number of lines 
	 */
	public void insertLines(int index, int lines)
	{
		if(lines <= 0)
			return;
		length += lines;
		ensureCapacity(length);
		int len = index + lines;
		System.arraycopy(lineInfo,index,lineInfo,len,
			lineInfo.length - len);
	}
	
	/**
	 * Informs the token marker that line have been deleted from
	 * the document. This removes the lines in question from the
	 * <code>lineInfo</code> array.
	 * @param index The first line number
	 * @param lines The number of lines
	 */
	public void deleteLines(int index, int lines)
	{
		if (lines <= 0)
			return;
		int len = index + lines;
		length -= lines;
		System.arraycopy(lineInfo,len,lineInfo,
			index,lineInfo.length - len);
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
	 * Creates a new <code>TokenMarker</code>. This DOES NOT create
	 * a lineInfo array; an initial call to <code>insertLines()</code>
	 * does that.
	 */
	protected TokenMarker()
	{
	}

	/**
	 * Ensures that the <code>lineInfo</code> array can contain the
	 * specified index. This enlarges it if necessary. No action is
	 * taken if the array is large enough already.<p>
	 *
	 * It should be unnecessary to call this under normal
	 * circumstances; <code>insertLine()</code> should take care of
	 * enlarging the line info array automatically.
	 *
	 * @param index The array index
	 */
	protected void ensureCapacity(int index)
	{
		if(lineInfo == null)
			lineInfo = new String[index + 1];
		else if(lineInfo.length <= index)
		{
			String[] lineInfoN = new String[(index + 1) * 2];
			System.arraycopy(lineInfo,0,lineInfoN,0,
					 lineInfo.length);
			lineInfo = lineInfoN;
		}
	}

	/**
	 * Adds a token to the token list.
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

/*
 * ChangeLog:
 * $Log$
 * Revision 1.12  1999/03/15 03:40:23  sp
 * Search and replace updates, TSQL mode/token marker updates
 *
 * Revision 1.11  1999/03/14 04:13:40  sp
 * Fixed ArrayIndexOutOfBounds in TokenMarker, minor Javadoc updates, minor documentation updates
 *
 * Revision 1.10  1999/03/14 02:22:13  sp
 * Syntax colorizing tweaks, server bug fix
 *
 * Revision 1.9  1999/03/13 09:11:46  sp
 * Syntax code updates, code cleanups
 *
 * Revision 1.8  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.7  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
