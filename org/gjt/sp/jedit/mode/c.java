/*
 * c.java - C editing mode
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

public class c extends autoindent
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
			keywords.add("bool","type");
			keywords.add("char","type");
			keywords.add("class","type");
			keywords.add("double","type");
			keywords.add("enum","type");
			keywords.add("float","type");
			keywords.add("int","type");
			keywords.add("long","type");
			keywords.add("short","type");
			keywords.add("signed","type");
			keywords.add("struct","type");
			keywords.add("typedef","type");
			keywords.add("union","type");
			keywords.add("unsigned","type");
			keywords.add("void","type");
			keywords.add("auto","modifier");
			keywords.add("const","modifier");
			keywords.add("extern","modifier");
			keywords.add("private","label");
			keywords.add("public","label");
			keywords.add("register","modifier");
			keywords.add("static","modifier");
			keywords.add("virtual","modifier");
			keywords.add("volatile","modifier");
			keywords.add("break","keyword");
			keywords.add("case","label");
			keywords.add("catch","keyword");
			keywords.add("continue","keyword");
			keywords.add("default","label");
			keywords.add("delete","keyword");
			keywords.add("do","keyword");
			keywords.add("else","keyword");
			keywords.add("for","keyword");
			keywords.add("friend","keyword");
			keywords.add("goto","keyword");
			keywords.add("if","keyword");
			keywords.add("new","keyword");
			keywords.add("return","keyword");
			keywords.add("sizeof","keyword");
			keywords.add("switch","keyword");
			keywords.add("throw","keyword");
			keywords.add("try","keyword");
			keywords.add("while","keyword");
			keywords.add("asm","non_standard");
			keywords.add("asmlinkage","non_standard");
			keywords.add("far","non_standard");
			keywords.add("huge","non_standard");
			keywords.add("inline","non_standard");
			keywords.add("near","non_standard");
			keywords.add("pascal","non_standard");
			keywords.add("true","constant");
			keywords.add("false","constant");
			keywords.add("NULL","constant");
		}
		return keywords;
	}

	private static KeywordMap keywords;
}
