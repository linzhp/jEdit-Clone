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

public class cc extends c
{
	public static KeywordMap getKeywords()
	{
		if(keywords == null)
		{
			keywords = new KeywordMap(false);
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
		return c.getKeywords();
	}
}
