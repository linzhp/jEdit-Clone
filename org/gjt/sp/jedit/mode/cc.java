/*
 * cc.java - C++ editing mode
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

package org.gjt.sp.jedit.mode;

import org.gjt.sp.jedit.syntax.*;

public class cc extends autoindent
{
	public TokenMarker createTokenMarker()
	{
		return new CTokenMarker(true,getKeywords());
	}

	public static KeywordMap getKeywords()
	{
		if(keywords == null)
		{
			keywords = new KeywordMap(false);
			keywords.add("char",Token.KEYWORD3);
			keywords.add("double",Token.KEYWORD3);
			keywords.add("enum",Token.KEYWORD3);
			keywords.add("float",Token.KEYWORD3);
			keywords.add("int",Token.KEYWORD3);
			keywords.add("long",Token.KEYWORD3);
			keywords.add("short",Token.KEYWORD3);
			keywords.add("signed",Token.KEYWORD3);
			keywords.add("struct",Token.KEYWORD3);
			keywords.add("typedef",Token.KEYWORD3);
			keywords.add("union",Token.KEYWORD3);
			keywords.add("unsigned",Token.KEYWORD3);
			keywords.add("void",Token.KEYWORD3);
			keywords.add("auto",Token.KEYWORD1);
			keywords.add("const",Token.KEYWORD1);
			keywords.add("extern",Token.KEYWORD1);
			keywords.add("register",Token.KEYWORD1);
			keywords.add("static",Token.KEYWORD1);
			keywords.add("volatile",Token.KEYWORD1);
			keywords.add("break",Token.KEYWORD1);
			keywords.add("case",Token.KEYWORD1);
			keywords.add("continue",Token.KEYWORD1);
			keywords.add("default",Token.KEYWORD1);
			keywords.add("do",Token.KEYWORD1);
			keywords.add("else",Token.KEYWORD1);
			keywords.add("for",Token.KEYWORD1);
			keywords.add("goto",Token.KEYWORD1);
			keywords.add("if",Token.KEYWORD1);
			keywords.add("return",Token.KEYWORD1);
			keywords.add("sizeof",Token.KEYWORD1);
			keywords.add("switch",Token.KEYWORD1);
			keywords.add("while",Token.KEYWORD1);
			keywords.add("asm",Token.KEYWORD2);
			keywords.add("asmlinkage",Token.KEYWORD2);
			keywords.add("far",Token.KEYWORD2);
			keywords.add("huge",Token.KEYWORD2);
			keywords.add("inline",Token.KEYWORD2);
			keywords.add("near",Token.KEYWORD2);
			keywords.add("pascal",Token.KEYWORD2);
			keywords.add("true",Token.LABEL);
			keywords.add("false",Token.LABEL);
			keywords.add("NULL",Token.LABEL);

			// C++ stuff
			keywords.add("bool",Token.KEYWORD3);
			keywords.add("class",Token.KEYWORD3);
			keywords.add("template",Token.KEYWORD3);
			keywords.add("private",Token.LABEL);
			keywords.add("public",Token.LABEL);
			keywords.add("virtual",Token.KEYWORD1);
			keywords.add("catch",Token.KEYWORD1);
			keywords.add("default",Token.LABEL);
			keywords.add("delete",Token.KEYWORD1);
			keywords.add("friend",Token.KEYWORD1);
			keywords.add("new",Token.KEYWORD1);
			keywords.add("throw",Token.KEYWORD1);
			keywords.add("try",Token.KEYWORD1);
		}
		return keywords;
	}

	// private members
	private static KeywordMap keywords;
}
