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
			keywords.add("function","type");
			keywords.add("var","type");
			keywords.add("else","keyword");
			keywords.add("for","keyword");
			keywords.add("if","keyword");
			keywords.add("in","keyword");
			keywords.add("new","keyword");
			keywords.add("return","keyword");
			keywords.add("while","keyword");
			keywords.add("with","keyword");
			keywords.add("break","label");
			keywords.add("continue","label");
			keywords.add("false","constant");
			keywords.add("this","constant");
			keywords.add("true","constant");
		}
		return keywords;
	}

	private static KeywordMap keywords;
}
