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
			keywords.add("package","preprocessor");
			keywords.add("import","preprocessor");
			keywords.add("byte","type");
			keywords.add("char","type");
			keywords.add("short","type");
			keywords.add("int","type");
			keywords.add("long","type");
			keywords.add("float","type");
			keywords.add("double","type");
			keywords.add("boolean","type");
			keywords.add("void","type");
			keywords.add("class","type");
			keywords.add("interface","type");
			keywords.add("abstract","modifier");
			keywords.add("final","modifier");
			keywords.add("private","modifier");
			keywords.add("protected","modifier");
			keywords.add("public","modifier");
			keywords.add("static","modifier");
			keywords.add("synchronized","modifier");
			keywords.add("volatile","modifier");
			keywords.add("transient","modifier");
			keywords.add("break","keyword");
			keywords.add("case","label");
			keywords.add("continue","keyword");
			keywords.add("default","label");
			keywords.add("do","keyword");
			keywords.add("else","keyword");
			keywords.add("for","keyword");
			keywords.add("if","keyword");
			keywords.add("instanceof","keyword");
			keywords.add("new","keyword");
			keywords.add("return","keyword");
			keywords.add("switch","keyword");
			keywords.add("while","keyword");
			keywords.add("try","keyword");
			keywords.add("catch","keyword");
			keywords.add("extends","keyword");
			keywords.add("finally","keyword");
			keywords.add("implements","keyword");
			keywords.add("throws","keyword");
			keywords.add("this","constant");
			keywords.add("null","constant");
			keywords.add("true","constant");
			keywords.add("false","constant");
		}
		return keywords;
	}

	private static KeywordMap keywords;
}
