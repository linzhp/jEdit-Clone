/*
 * Token.java - Generic token
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

/**
 * A linked list of tokens.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class Token
{
	// public members

	/**
	 * Alternate text token id.
	 */
	public static final String ALTTXT = "alt";

	/**
	 * Comment 1 token id.
	 */
	public static final String COMMENT1 = "comment1";

	/**
	 * Comment 2 token id.
	 */
	public static final String COMMENT2 = "comment2";

	
	/**
	 * Literal 1 token id.
	 */
	public static final String LITERAL1 = "literal1";

	/**
	 * Literal 2 token id.
	 */
	public static final String LITERAL2 = "literal2";

	/**
	 * Label token id.
	 */
	public static final String LABEL = "label";

	/**
	 * Keyword 1 token id.
	 */
	public static final String KEYWORD1 = "keyword1";

	/**
	 * Keyword 2 token id.
	 */
	public static final String KEYWORD2 = "keyword2";

	/**
	 * Keyword 3 token id.
	 */
	public static final String KEYWORD3 = "keyword3";

	/**
	 * Operator token id.
	 */
	public static final String OPERATOR = "operator";

	/**
	 * Invalid token id.
	 */
	public static final String INVALID = "invalid";

	/**
	 * The length of this token.
	 */
	public int length;

	/**
	 * The id of this token. This can be looked up in the table returned
	 * by <code>Buffer.getColors()</code> to obtain a color value.
	 */
	public String id;

	/**
	 * The next token in the linked list.
	 */
	public Token next;

	/**
	 * Set to true if the next token is valid, false otherwise.
	 */
	public boolean nextValid;

	/**
	 * Creates a new token.
	 * @param length The length of the token
	 * @param id The id of the token
	 */
	public Token(int length, String id)
	{
		this.length = length;
		this.id = id;
	}

	/**
	 * Returns a string representation of this token.
	 */
	public String toString()
	{
		return id + "[length=" + length + (nextValid ? ",nextValid]"
						   : "nextInvalid]");
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.6  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
