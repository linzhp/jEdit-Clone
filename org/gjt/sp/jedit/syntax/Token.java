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
 * A linked list of tokens. Each token has three fields - a token
 * identifier, which is a string that can be looked up in the
 * dictionary returned by <code>SyntaxDocument.getColors()</code>
 * to get a color value, a length value which is the length of the
 * token in the text, and a pointer to the next token in the list,
 * which may or may not be valid, depending on the value of the
 * <code>nextValid</code> flag.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class Token
{
	/**
	 * Alternate text token id. This will be rendered in the
	 * same color as normal text. It is provided for internal
	 * use by token markers (eg, HTML mode uses it to mark
	 * JavaScripts)
	 */
	public static final String ALTTXT = "alt";

	/**
	 * Comment 1 token id. This can be used to mark a comment.
	 */
	public static final String COMMENT1 = "comment1";

	/**
	 * Comment 2 token id. This can be used to mark a comment.
	 */
	public static final String COMMENT2 = "comment2";

	
	/**
	 * Literal 1 token id. This can be used to mark a string
	 * literal (eg, C mode uses this to mark "..." literals)
	 */
	public static final String LITERAL1 = "literal1";

	/**
	 * Literal 2 token id. This can be used to mark a string
	 * literal (eg, C mode uses this to mark '...' literals)
	 */
	public static final String LITERAL2 = "literal2";

	/**
	 * Label token id. This can be used to mark labels
	 * (eg, C mode uses this to mark ...: sequences)
	 */
	public static final String LABEL = "label";

	/**
	 * Keyword 1 token id. This can be used to mark a
	 * keyword. This should be used for general language
	 * constructs.
	 */
	public static final String KEYWORD1 = "keyword1";

	/**
	 * Keyword 2 token id. This can be used to mark a
	 * keyword. This should be used for preprocessor
	 * commands, or variables.
	 */
	public static final String KEYWORD2 = "keyword2";

	/**
	 * Keyword 3 token id. This can be used to mark a
	 * keyword. This should be used for data types.
	 */
	public static final String KEYWORD3 = "keyword3";

	/**
	 * Operator token id. This can be used to mark an
	 * operator. (eg, SQL mode marks +, -, etc with this
	 * token type)
	 */
	public static final String OPERATOR = "operator";

	/**
	 * Invalid token id. This can be used to mark invalid
	 * or incomplete tokens, so the user can easily spot
	 * syntax errors.
	 */
	public static final String INVALID = "invalid";

	/**
	 * The length of this token.
	 */
	public int length;

	/**
	 * The id of this token.
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
 * Revision 1.8  1999/04/01 04:13:00  sp
 * Bug fixing for 1.5final
 *
 * Revision 1.7  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.6  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
