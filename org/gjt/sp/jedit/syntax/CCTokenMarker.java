/*
 * CCTokenMarker.java - C++ token marker
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

/**
 * C++ token marker.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class CCTokenMarker extends CTokenMarker
{
	public CCTokenMarker()
	{
		super(true,getKeywords());
	}

	public static KeywordMap getKeywords()
	{
		if(ccKeywords == null)
		{
			ccKeywords = new KeywordMap(false);
			ccKeywords.add("char",Token.KEYWORD3);
			ccKeywords.add("double",Token.KEYWORD3);
			ccKeywords.add("enum",Token.KEYWORD3);
			ccKeywords.add("float",Token.KEYWORD3);
			ccKeywords.add("int",Token.KEYWORD3);
			ccKeywords.add("long",Token.KEYWORD3);
			ccKeywords.add("short",Token.KEYWORD3);
			ccKeywords.add("signed",Token.KEYWORD3);
			ccKeywords.add("struct",Token.KEYWORD3);
			ccKeywords.add("typedef",Token.KEYWORD3);
			ccKeywords.add("union",Token.KEYWORD3);
			ccKeywords.add("unsigned",Token.KEYWORD3);
			ccKeywords.add("void",Token.KEYWORD3);
			ccKeywords.add("auto",Token.KEYWORD1);
			ccKeywords.add("const",Token.KEYWORD1);
			ccKeywords.add("extern",Token.KEYWORD1);
			ccKeywords.add("register",Token.KEYWORD1);
			ccKeywords.add("static",Token.KEYWORD1);
			ccKeywords.add("volatile",Token.KEYWORD1);
			ccKeywords.add("break",Token.KEYWORD1);
			ccKeywords.add("case",Token.KEYWORD1);
			ccKeywords.add("continue",Token.KEYWORD1);
			ccKeywords.add("default",Token.KEYWORD1);
			ccKeywords.add("do",Token.KEYWORD1);
			ccKeywords.add("else",Token.KEYWORD1);
			ccKeywords.add("for",Token.KEYWORD1);
			ccKeywords.add("goto",Token.KEYWORD1);
			ccKeywords.add("if",Token.KEYWORD1);
			ccKeywords.add("return",Token.KEYWORD1);
			ccKeywords.add("sizeof",Token.KEYWORD1);
			ccKeywords.add("switch",Token.KEYWORD1);
			ccKeywords.add("while",Token.KEYWORD1);
			ccKeywords.add("asm",Token.KEYWORD2);
			ccKeywords.add("asmlinkage",Token.KEYWORD2);
			ccKeywords.add("far",Token.KEYWORD2);
			ccKeywords.add("huge",Token.KEYWORD2);
			ccKeywords.add("inline",Token.KEYWORD2);
			ccKeywords.add("near",Token.KEYWORD2);
			ccKeywords.add("pascal",Token.KEYWORD2);
			ccKeywords.add("true",Token.LABEL);
			ccKeywords.add("false",Token.LABEL);
			ccKeywords.add("NULL",Token.LABEL);

			// C++ stuff
			ccKeywords.add("bool",Token.KEYWORD3);
			ccKeywords.add("class",Token.KEYWORD3);
			ccKeywords.add("template",Token.KEYWORD3);
			ccKeywords.add("private",Token.LABEL);
			ccKeywords.add("public",Token.LABEL);
			ccKeywords.add("virtual",Token.KEYWORD1);
			ccKeywords.add("catch",Token.KEYWORD1);
			ccKeywords.add("default",Token.LABEL);
			ccKeywords.add("delete",Token.KEYWORD1);
			ccKeywords.add("friend",Token.KEYWORD1);
			ccKeywords.add("new",Token.KEYWORD1);
			ccKeywords.add("throw",Token.KEYWORD1);
			ccKeywords.add("try",Token.KEYWORD1);
		}
		return ccKeywords;
	}

	// private members
	private static KeywordMap ccKeywords;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/03/13 09:11:46  sp
 * Syntax code updates, code cleanups
 *
 */
