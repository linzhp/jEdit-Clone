/*
 * RESearchMatcher.java - Regular expression matcher
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

package org.gjt.sp.jedit.search;

import gnu.regexp.*;

/**
 * A regular expression string matcher.
 * @author Slava Pestov
 * @version $Id$
 */
public class RESearchMatcher implements SearchMatcher
{
	/**
	 * AWK regexp syntax.
	 */
	public static final String AWK = "awk";
	
	/**
	 * ED regexp syntax.
	 */
	public static final String ED = "ed";
	
	/**
	 * EGREP regexp syntax.
	 */
	public static final String EGREP = "egrep";
	
	/**
	 * EMACS regexp syntax.
	 */
	public static final String EMACS = "emacs";
	
	/**
	 * GREP regexp syntax.
	 */
	public static final String GREP = "grep";
	
	/**
	 * PERL4 regexp syntax.
	 */
	public static final String PERL4 = "perl4";
	
	/**
	 * PERL5 regexp syntax.
	 */
	public static final String PERL5 = "perl5";
	
	/**
	 * SED regexp syntax.
	 */
	public static final String SED = "sed";

	/**
	 * The values that can be passed to the <code>setSyntax()</code>
	 * method.
	 */
	public static final String[] SYNTAX_LIST = { SearchAndReplace.NONE,
		AWK, ED, EGREP, EMACS, GREP, PERL4, PERL5, SED };

	/**
	 * Creates a new regular expression string matcher.
	 * @param search The search string
	 * @param replace The replacement string
	 * @param ignoreCase True if the matcher should be case insensitive,
	 * false otherwise
	 * @param syntax The regular expression syntax
	 */
	public RESearchMatcher(String search, String replace,
		boolean ignoreCase, String _syntax)
	{
		RESyntax syntax;
		if(AWK.equals(_syntax))
			syntax = RESyntax.RE_SYNTAX_AWK;
		else if(ED.equals(_syntax))
			syntax = RESyntax.RE_SYNTAX_ED;
		else if(EGREP.equals(_syntax))
			syntax = RESyntax.RE_SYNTAX_EGREP;
		else if(EMACS.equals(_syntax))
			syntax = RESyntax.RE_SYNTAX_EMACS;
		else if(GREP.equals(_syntax))
			syntax = RESyntax.RE_SYNTAX_GREP;
		else if(SED.equals(_syntax))
			syntax = RESyntax.RE_SYNTAX_SED;
		else if(PERL4.equals(_syntax))
			syntax = RESyntax.RE_SYNTAX_PERL4;
		else if(PERL5.equals(_syntax))
			syntax = RESyntax.RE_SYNTAX_PERL5;
		else
			throw new IllegalArgumentException("Invalid syntax");

		try
		{
			re = new RE(search,(ignoreCase ? RE.REG_ICASE : 0)
				| RE.REG_MULTILINE,syntax);
		}
		catch(REException e)
		{
			throw new IllegalArgumentException(e.getMessage());
		}

		this.replace = replace;
	}

	/**
	 * Returns the offset of the first match of the specified text
	 * within this matcher.
	 * @param text The text to search in
	 * @return an array where the first element is the start offset
	 * of the match, and the second element is the end offset of
	 * the match
	 */
	public int[] nextMatch(String text)
	{
		REMatch match = re.getMatch(text);
		if(match == null)
			return null;
		int[] result = { match.getStartIndex(),
			match.getEndIndex() };
		return result;
	}

	/**
	 * Returns the specified text, with any substitution specified
	 * within this matcher performed.
	 * @param text The text
	 */
	public String substitute(String text)
	{
		return re.substituteAll(text,replace);
	}

	// private members
	private String replace;
	private RE re;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/05/29 08:06:56  sp
 * Search and replace overhaul
 *
 */
