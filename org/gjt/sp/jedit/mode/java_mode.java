/*
 * java_mode.java - Java editing mode
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

package org.gjt.sp.jedit.mode;

import org.gjt.sp.jedit.syntax.*;

public class java_mode extends autoindent
{
	public TokenMarker createTokenMarker()
	{
		return new CTokenMarker(false,getKeywordMap());
	}

	public static KeywordMap getKeywordMap()
	{
		if(keywords == null)
		{
			keywords = new KeywordMap(false);
			keywords.add("package",Token.KEYWORD2);
			keywords.add("import",Token.KEYWORD2);
			keywords.add("byte",Token.KEYWORD3);
			keywords.add("char",Token.KEYWORD3);
			keywords.add("short",Token.KEYWORD3);
			keywords.add("int",Token.KEYWORD3);
			keywords.add("long",Token.KEYWORD3);
			keywords.add("float",Token.KEYWORD3);
			keywords.add("double",Token.KEYWORD3);
			keywords.add("boolean",Token.KEYWORD3);
			keywords.add("void",Token.KEYWORD3);
			keywords.add("class",Token.KEYWORD3);
			keywords.add("interface",Token.KEYWORD3);
			keywords.add("abstract",Token.KEYWORD1);
			keywords.add("final",Token.KEYWORD1);
			keywords.add("private",Token.KEYWORD1);
			keywords.add("protected",Token.KEYWORD1);
			keywords.add("public",Token.KEYWORD1);
			keywords.add("static",Token.KEYWORD1);
			keywords.add("synchronized",Token.KEYWORD1);
			keywords.add("volatile",Token.KEYWORD1);
			keywords.add("transient",Token.KEYWORD1);
			keywords.add("break",Token.KEYWORD1);
			keywords.add("case",Token.KEYWORD1);
			keywords.add("continue",Token.KEYWORD1);
			keywords.add("default",Token.KEYWORD1);
			keywords.add("do",Token.KEYWORD1);
			keywords.add("else",Token.KEYWORD1);
			keywords.add("for",Token.KEYWORD1);
			keywords.add("if",Token.KEYWORD1);
			keywords.add("instanceof",Token.KEYWORD1);
			keywords.add("new",Token.KEYWORD1);
			keywords.add("return",Token.KEYWORD1);
			keywords.add("switch",Token.KEYWORD1);
			keywords.add("while",Token.KEYWORD1);
			keywords.add("throw",Token.KEYWORD1);
			keywords.add("try",Token.KEYWORD1);
			keywords.add("catch",Token.KEYWORD1);
			keywords.add("extends",Token.KEYWORD1);
			keywords.add("finally",Token.KEYWORD1);
			keywords.add("implements",Token.KEYWORD1);
			keywords.add("throws",Token.KEYWORD1);
			keywords.add("this",Token.LABEL);
			keywords.add("null",Token.LABEL);
			keywords.add("super",Token.LABEL);
			keywords.add("true",Token.LABEL);
			keywords.add("false",Token.LABEL);
		}
		return keywords;
	}

	private static KeywordMap keywords;
}
