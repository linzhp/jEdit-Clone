/*
 * SearchAndReplace.java - Search and replace
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

import javax.swing.text.Element;
import javax.swing.JOptionPane;
import org.gjt.sp.jedit.gui.JEditTextArea;
import org.gjt.sp.jedit.*;

/**
 * Class that implements regular expression and literal search within
 * jEdit buffers.
 * @author Slava Pestov
 * @version $Id$
 */
public class SearchAndReplace
{
	/**
	 * Sets the current search string.
	 * @param search The new search string
	 */
	public static void setSearchString(String search)
	{
		SearchAndReplace.search = search;
		matcher = null;
	}

	/**
	 * Returns the current search string.
	 */
	public static String getSearchString()
	{
		return search;
	}

	/**
	 * Sets the current replacement string.
	 * @param search The new replacement string
	 */
	public static void setReplaceString(String replace)
	{
		SearchAndReplace.replace = replace;
		matcher = null;
	}

	/**
	 * Returns the current replacement string.
	 */
	public static String getReplaceString()
	{
		return replace;
	}

	/**
	 * Sets the ignore case flag.
	 * @param ignoreCase True if searches should be case insensitive,
	 * false otherwise
	 */
	public static void setIgnoreCase(boolean ignoreCase)
	{
		SearchAndReplace.ignoreCase = ignoreCase;
		matcher = null;
	}

	/**
	 * Returns the state of the ignore case flag.
	 * @return True if searches should be case insensitive,
	 * false otherwise
	 */
	public static boolean getIgnoreCase()
	{
		return ignoreCase;
	}

	/**
	 * Sets the state of the regular expression flag.
	 * @param regexp True if regular expression searches should be
	 * performed
	 */
	public static void setRegexp(boolean regexp)
	{
		SearchAndReplace.regexp = regexp;
		matcher = null;
	}

	/**
	 * Returns the state of the regular expression flag.
	 * @return True if regular expression searches should be performed
	 */
	public static boolean getRegexp()
	{
		return regexp;
	}

	/**
	 * Sets the current search string matcher. Note that calling
	 * <code>setSearchString</code>, <code>setReplaceString</code>,
	 * <code>setIgnoreCase</code> or <code>setRegExp</code> will
	 * reset the matcher to the default.
	 */
	public static void setSearchMatcher(SearchMatcher matcher)
	{
		SearchAndReplace.matcher = matcher;
	}

	/**
	 * Returns the current search string matcher.
	 */
	public static SearchMatcher getSearchMatcher()
	{
		if(matcher != null)
			return matcher;

		if(search == null || "".equals(search))
			return null;

		if(regexp)
			return new RESearchMatcher(search,replace,ignoreCase);
		else
			return new LiteralSearchMatcher(search,replace,ignoreCase);
	}

	/**
	 * Sets the current search file set.
	 * @param fileset The file set to perform searches in
	 */
	public static void setSearchFileSet(SearchFileSet fileset)
	{
		SearchAndReplace.fileset = fileset;
	}

	/**
	 * Returns the current search file set.
	 */
	public static SearchFileSet getSearchFileSet()
	{
		return fileset;
	}

	/**
	 * Finds the next instance of the search string in this buffer,
	 * starting at the end of the selected text, or the caret position
	 * if nothing is selected.
	 * @param view The view
	 * @param buffer The buffer
	 * @param done For internal use. False if a `keep searching'
	 * dialog should be shown if no more matches have been found.
	 */
	public static boolean find(View view, Buffer buffer, boolean done)
	{
		return find(view,buffer,view.getTextArea().getSelectionEnd(),done);
	}

	/**
	 * Finds the next instance of the search string in the specified buffer.
	 * @param view The view
	 * @param buffer The buffer
	 * @param start Location where to start the search
	 * @param done For internal use. False if a `keep searching'
	 * dialog should be shown if no more matches have been found.
	 */
	public static boolean find(View view, Buffer buffer, int start, boolean done)
	{
		try
		{
			SearchMatcher matcher = getSearchMatcher();
			if(matcher == null)
			{
				view.getToolkit().beep();
				return false;
			}

			String text = buffer.getText(start,
				buffer.getLength() - start);
			int[] match = matcher.nextMatch(text);
			if(match != null)
			{
				view.getTextArea().select(start + match[0],
					start + match[1]);
				return true;
			}
			if(done)
			{
				view.getToolkit().beep();
				return false;
			}

			int result = JOptionPane.showConfirmDialog(view,
				jEdit.getProperty("keepsearching.message"),
				jEdit.getProperty("keepsearching.title"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			if(result == JOptionPane.YES_OPTION)
				return find(view,buffer,0,true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Object[] args = { e.getMessage() };
			if(args[0] == null)
				args[0] = e.toString();
			GUIUtilities.error(view,"searcherror",args);
		}
		return false;
	}

	/**
	 * Replaces the current selection with the replacement string.
	 * @param view The view
	 * @param buffer The buffer
	 * @return True if the operation was successful, false otherwise
	 */
	public static boolean replace(View view, Buffer buffer)
	{
		JEditTextArea textArea = view.getTextArea();
		int selStart = textArea.getSelectionStart();
		int selEnd = textArea.getSelectionEnd();
		if(selStart == selEnd)
		{
			view.getToolkit().beep();
			return false;
		}
		boolean retVal = replace(view,buffer,selStart,selEnd);
		textArea.setSelectionStart(selStart);
		return retVal;
	}

	/**
	 * Replaces all occurances of the search string with the replacement
	 * string.
	 */
	public static boolean replaceAll(View view)
	{
		boolean retval = false;
		Buffer[] buffers = fileset.getSearchBuffers(view);
		for(int i = 0; i < buffers.length; i++)
		{
			Buffer buffer = buffers[i];
			retval |= replace(view,buffer,0,buffer.getLength());
		}
		return retval;
	}

	/**
	 * Replaces all occurances of the search string with the replacement
	 * string.
	 * @param view The view
	 * @param buffer The buffer
	 * @param start The index where to start the search
	 * @param end The end offset of the search
	 * @return True if the replace operation was successful, false
	 * if no matches were found
	 */
	public static boolean replace(View view, Buffer buffer,
		int start, int end)
	{
		if(!view.getTextArea().isEditable())
		{
			view.getToolkit().beep();
			return false;
		}
		boolean found = false;
		buffer.beginCompoundEdit();
		try
		{
			SearchMatcher matcher = getSearchMatcher();
			if(matcher == null)
			{
				view.getToolkit().beep();
				return false;
			}

			int[] match;

			Element map = buffer.getDefaultRootElement();
			int startLine = map.getElementIndex(start);
			int endLine = map.getElementIndex(end);

			for(int i = startLine; i <= endLine; i++)
			{
				Element lineElement = map.getElement(i);
				int lineStart;
				int lineEnd;

				if(i == startLine)
					lineStart = start;
				else
					lineStart = lineElement.getStartOffset();

				if(i == endLine)
					lineEnd = end;
				else
					lineEnd = lineElement.getEndOffset() - 1;

				lineEnd -= lineStart;
				String line = buffer.getText(lineStart,lineEnd);
				String newLine = matcher.substitute(line);
				if(line.equals(newLine)) // XXX slow
					continue;
				buffer.remove(lineStart,lineEnd);
				buffer.insertString(lineStart,newLine,null);

				end += (newLine.length() - lineEnd);
				found = true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			found = false;
			Object[] args = { e.getMessage() };
			if(args[0] == null)
				args[0] = e.toString();
			GUIUtilities.error(view,"searcherror",args);
		}
		buffer.endCompoundEdit();
		return found;
	}

	/**
	 * Loads search and replace state from the properties.
	 */
	public static void load()
	{
		String filesetStr = jEdit.getProperty("search.multifile.value");
		if("all".equals(filesetStr))
			fileset = new AllBufferSet();
		else
			fileset = new CurrentBufferSet();
		search = jEdit.getProperty("search.find.value");
		replace = jEdit.getProperty("search.replace.value");
		regexp = "on".equals(jEdit.getProperty("search.regexp.toggle"));
		ignoreCase = "on".equals(jEdit.getProperty("search.ignoreCase.toggle"));
	}

	/**
	 * Saves search and replace state to the properties.
	 */
	public static void save()
	{
		jEdit.setProperty("search.multifile.value",
			(fileset instanceof AllBufferSet) ? "all" : "current");
		jEdit.setProperty("search.find.value",(search == null ? ""
			: search));
		jEdit.setProperty("search.replace.value",(replace == null ? ""
			: replace));
		jEdit.setProperty("search.ignoreCase.toggle",
			ignoreCase ? "on" : "off");
		jEdit.setProperty("search.regexp.toggle",
			regexp ? "on" : "off");
	}
		
	// private members
	private static String search;
	private static String replace;
	private static boolean regexp;
	private static boolean ignoreCase;
	private static SearchMatcher matcher;
	private static SearchFileSet fileset;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.4  1999/06/03 08:24:13  sp
 * Fixing broken CVS
 *
 * Revision 1.4  1999/05/31 04:38:51  sp
 * Syntax optimizations, HyperSearch for Selection added (Mike Dillon)
 *
 * Revision 1.3  1999/05/30 01:28:43  sp
 * Minor search and replace updates
 *
 * Revision 1.2  1999/05/29 08:06:56  sp
 * Search and replace overhaul
 *
 */
