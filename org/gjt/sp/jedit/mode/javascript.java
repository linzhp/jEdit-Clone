/*
 * javascript.java - JavaScript editing mode
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

public class javascript extends autoindent
{
	public TokenMarker createTokenMarker()
	{
		return new CTokenMarker(false,getKeywords());
	}

	public static KeywordMap getKeywords()
	{
		if(keywords == null)
		{
			keywords = new KeywordMap(false);
			keywords.add("function",Token.KEYWORD3);
			keywords.add("var",Token.KEYWORD3);
			keywords.add("else",Token.KEYWORD1);
			keywords.add("for",Token.KEYWORD1);
			keywords.add("if",Token.KEYWORD1);
			keywords.add("in",Token.KEYWORD1);
			keywords.add("new",Token.KEYWORD1);
			keywords.add("return",Token.KEYWORD1);
			keywords.add("while",Token.KEYWORD1);
			keywords.add("with",Token.KEYWORD1);
			keywords.add("break",Token.LABEL);
			keywords.add("continue",Token.LABEL);
			keywords.add("false",Token.LABEL);
			keywords.add("this",Token.LABEL);
			keywords.add("true",Token.LABEL);
		}
		return keywords;
	}

	private static KeywordMap keywords;
}
