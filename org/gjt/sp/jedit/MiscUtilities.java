/*
 * MiscUtilities.java - Various miscallaneous utility functions
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

package org.gjt.sp.jedit;

import gnu.regexp.*;
import java.io.*;

/**
 * Class with several useful miscallaneous functions.<p>
 *
 * It provides methods for converting file names to class names, for
 * constructing path names, and for various indentation calculations.<p>
 *
 * It also provides several regular expression-related methods.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class MiscUtilities
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
	 * The values that can be stored in the
	 * <code>search.regexp.value</code> property to specify the regexp
	 * syntax.
	 */
	public static final String[] SYNTAX_LIST = { AWK, ED, EGREP, EMACS,
		GREP, PERL4, PERL5, SED };

	/**
	 * Converts a file name to a class name. All slash characters are
	 * replaced with periods and the trailing '.class' is removed.
	 * @param name The file name
	 */
	public static String fileToClass(String name)
	{
		char[] clsName = name.toCharArray();
		for(int i = clsName.length - 6; i >= 0; i--)
			if(clsName[i] == '/')
				clsName[i] = '.';
		return new String(clsName,0,clsName.length - 6);
	}

	/**
	 * Converts a clas name to a file name. All periods are replaced
	 * with slashes and the '.class' extension is added.
	 * @param name The class name
	 */
	public static String classToFile(String name)
	{
		return name.replace('.','/').concat(".class");
	}

	/**
	 * Constructs an absolute path name from a directory and another
	 * path name.
	 * @param parent The directory
	 * @param path The path name
	 */
	public static String constructPath(String parent, String path)
	{
		// absolute pathnames
		if(path.startsWith(File.separator))
			return canonPath(path);
		// windows pathnames, eg C:\document
		else if(path.length() >= 3 && path.charAt(1) == ':')
			return canonPath(path);
		// relative pathnames
		else if(parent == null)
			parent = System.getProperty("user.dir");
		// do it!
		if(parent.endsWith(File.separator))
			return canonPath(parent + path);
		else
			return canonPath(parent + File.separator + path);
	}

	/**
	 * Returns the number of leading white space characters in the
	 * specified string.
	 * @param str The string
	 */
	public static int getLeadingWhiteSpace(String str)
	{
		int whitespace = 0;
loop:		for(;whitespace < str.length();)
		{
			switch(str.charAt(whitespace))
			{
			case ' ': case '\t':
				whitespace++;
				break;
			default:
				break loop;
			}
		}
		return whitespace;
	}

	/**
	 * Returns the width of the leading white space in the specified
	 * string.
	 * @param str The string
	 * @param tabSize The tab size
	 */
	public static int getLeadingWhiteSpaceWidth(String str, int tabSize)
	{
		int whitespace = 0;
loop:		for(int i = 0; i < str.length(); i++)
		{
			switch(str.charAt(i))
			{
			case ' ':
				whitespace++;
				break;
			case '\t':
				whitespace += (tabSize - whitespace % tabSize);
				break;
			default:
				break loop;
			}
		}
		return whitespace;
	}

	/**
	 * Creates a string of white space with the specified length.
	 * @param len The length
	 * @param tabSize The tab size, or 0 if tabs are not to be used
	 */
	public static String createWhiteSpace(int len, int tabSize)
	{
		StringBuffer buf = new StringBuffer();
		if(tabSize == 0)
		{
			while(len-- > 0)
				buf.append(' ');
		}
		else		
		{
			int count = len / tabSize;
			while(count-- > 0)
				buf.append('\t');
			count = len % tabSize;
			while(count-- > 0)
				buf.append(' ');
		}
		return buf.toString();
	}

	/**
	 * Returns the current regular expression.
	 * @exception REException if the stored regular expression is invalid
	 */
	public static RE getRE()
		throws REException
	{
		String pattern = jEdit.getProperty("search.find.value");
		if(pattern == null || "".equals(pattern))
			return null;
		return new RE(pattern,("on".equals(jEdit.getProperty(
			"search.ignoreCase.toggle")) ? RE.REG_ICASE : 0)
			| RE.REG_MULTILINE,getRESyntax(
			jEdit.getProperty("search.regexp.value")));
	}

	/**
	 * Converts a syntax name to an <code>RESyntax</code> instance.
	 * @param name The syntax name
	 */
	public static RESyntax getRESyntax(String name)
	{
		if(AWK.equals(name))
			return RESyntax.RE_SYNTAX_AWK;
		else if(ED.equals(name))
			return RESyntax.RE_SYNTAX_ED;
		else if(EGREP.equals(name))
			return RESyntax.RE_SYNTAX_EGREP;
		else if(EMACS.equals(name))
			return RESyntax.RE_SYNTAX_EMACS;
		else if(GREP.equals(name))
			return RESyntax.RE_SYNTAX_GREP;
		else if(SED.equals(name))
			return RESyntax.RE_SYNTAX_SED;
		else if(PERL4.equals(name))
			return RESyntax.RE_SYNTAX_PERL4;
		else
			return RESyntax.RE_SYNTAX_PERL5;
	}

	// private members
	private MiscUtilities() {}

	private static String canonPath(String path)
	{
		try
		{
			return new File(path).getCanonicalPath();
		}
		catch(IOException io)
		{
			return path;
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.10  1999/05/26 04:46:03  sp
 * Minor API change, soft tabs fixed ,1.7pre1
 *
 * Revision 1.9  1999/04/24 01:55:28  sp
 * MiscUtilities.constructPath() bug fixed, event system bug(s) fix
 *
 * Revision 1.8  1999/04/23 07:35:10  sp
 * History engine reworking (shared history models, history saved to
 * .jedit-history)
 *
 * Revision 1.7  1999/04/19 05:47:35  sp
 * ladies and gentlemen, 1.6pre1
 *
 * Revision 1.6  1999/03/21 07:53:14  sp
 * Plugin doc updates, action API change, new method in MiscUtilities, new class
 * loader, new plugin interface
 *
 * Revision 1.5  1999/03/19 07:12:10  sp
 * JOptionPane changes, did a fromdos of the source
 *
 * Revision 1.4  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.3  1999/03/12 07:54:47  sp
 * More Javadoc updates
 *
 */
