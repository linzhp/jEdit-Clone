/*
 * Mode.java - jEdit editing mode
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

package org.gjt.sp.jedit;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import org.gjt.sp.jedit.syntax.TokenMarker;
import org.gjt.sp.jedit.*;

/**
 * The abstract class all jEdit edit modes most extend. An edit mode defines
 * a set of behaviours for editing a specific file type.<p>
 * 
 * The <i>internal</i> name of an edit mode is the string passed to the
 * mode's constructor - it can be obtained with the <code>getName()</code>
 * method. The user visible name is that stored in the
 * <code>mode.<i>internal name</i>.name</code> property.<p>
 *
 * A mode instance can be fetched if it's internal name is known with the
 * <code>jEdit.getMode()</code> method. The
 * <code>jEdit.getModeName()</code> method returns the user visible
 * name of a mode instance.<p>
 *
 * Modes can be added at run-time with the <code>jEdit.addMode()</code>
 * method. An array of currently installed edit modes can be obtained
 * with the <code>jEdit.getModes()</code> method.<p>
 *
 * Currently, edit modes can influence the following:
 * <ul>
 * <li>The indentation performed, if any, when the user presses `Tab'
 * or `Enter'. This can be achieved by implementing the <code>indentLine()</code>
 * method. The default implementation performs simple auto indent.
 * <li>The token marker used for syntax colorizing, if any. This can
 * be achieved by implementing the <code>createTokenMarker()</code>
 * method.
 * </ul>
 * The following properties relate to edit modes:
 * <ul>
 * <li><code>mode.<i>internal name</i>.name</code> - the name of the edit
 * mode, displayed in the `Mode' menu
 * <li><code>mode.<i>internal name</i>.<i>buffer-local property</i></code> -
 * the default value of <i>buffer-local property</i> in this edit mode
 * <li><code>mode.extension.<i>extension</i></code> - the internal name of the
 * mode to be used for editing files with extension <i>extension</i>
 * <li><code>mode.filename.<i>filename</i></code> - the internal name of the
 * mode to be used for editing files named <i>filename</i>
 * <li><code>mode.firstline.<i>first line</i></code> - the internal name
 * of the mode to be used for editing files whose first line is
 * <i>first line</i>
 * </ul>
 *
 * @author Slava Pestov
 * @version $Id$
 *
 * @see org.gjt.sp.jedit.jEdit#getProperty(String)
 * @see org.gjt.sp.jedit.jEdit#getProperty(String,String)
 * @see org.gjt.sp.jedit.jEdit#getMode(String)
 * @see org.gjt.sp.jedit.jEdit#getModeName(Mode)
 * @see org.gjt.sp.jedit.jEdit#getModes()
 * @see org.gjt.sp.jedit.jEdit#addMode(Mode)
 * @see org.gjt.sp.jedit.Buffer#setMode()
 * @see org.gjt.sp.jedit.Buffer#setMode(Mode)
 * @see org.gjt.sp.jedit.syntax.TokenMarker
 */
public abstract class Mode
{
	/**
	 * Creates a new edit mode
	 */
	public Mode(String name)
	{
		this.name = name;
	}

	/**
	 * Called when a buffer enters this mode.
	 * @param buffer The buffer that entered this mode
	 */
	public void enter(Buffer buffer) {}
	
	/**
	 * Called when a view enters this mode.
	 * @param view The view that entered this mode
	 */
	public void enterView(View view) {}

	/**
	 * If auto indent is enabled, this method is called when the `Tab'
	 * or `Enter' key is pressed to perform mode-specific indentation
	 * and return true, or return false if a normal tab is to be inserted.
	 * @param buffer The buffer where the tab key was pressed
	 * @param view The view where the tab key was pressed
	 * @param caret The caret position
	 * @return true if the tab key event should be swallowed (ignored)
	 * false if a real tab should be inserted
	 */
	public boolean indentLine(Buffer buffer, View view, int caret)
	{
		String openBrackets = (String)buffer.getProperty("indentOpenBrackets");
		String closeBrackets = (String)buffer.getProperty("indentCloseBrackets");
		if(openBrackets == null || closeBrackets == null
			|| openBrackets.length() != closeBrackets.length())
		{
			openBrackets = closeBrackets = "";
		}
		int tabSize = buffer.getTabSize();
		boolean noTabs = "yes".equals(buffer.getProperty("noTabs"));
		Element map = buffer.getDefaultRootElement();
		int index = map.getElementIndex(caret);
		if(index == 0)
			return false;
		Element lineElement = map.getElement(index);
		Element prevLineElement = null;
		int prevStart = 0;
		int prevEnd = 0;
		while(--index >= 0)
		{
			prevLineElement = map.getElement(index);
			prevStart = prevLineElement.getStartOffset();
			prevEnd = prevLineElement.getEndOffset();
			if(prevEnd - prevStart > 1)
				break;
		}
		if(prevLineElement == null)
			return false;
		try
		{
			int start = lineElement.getStartOffset();
			int end = lineElement.getEndOffset();
			String line = buffer.getText(start,end - start);
			String prevLine = buffer.getText(prevStart,prevEnd
				- prevStart);

			/*
			 * On the previous line,
			 * { should give us +1
			 * { fred } should give us 0
			 * } fred { should give us +1
			 */
			boolean prevLineStart = true; // False after initial indent
			int prevLineIndent = 0; // Indent width (tab expanded)
			int prevLineBrackets = 0; // Additional bracket indent
			for(int i = 0; i < prevLine.length(); i++)
			{
				char c = prevLine.charAt(i);
				switch(c)
				{
				case ' ':
					if(prevLineStart)
						prevLineIndent++;
					break;
				case '\t':
					if(prevLineStart)
					{
						prevLineIndent += (tabSize
							- (prevLineIndent
							% tabSize));
					}
					break;
				default:
					prevLineStart = false;
					if(closeBrackets.indexOf(c) != -1)
						prevLineBrackets = Math.max(
							prevLineBrackets-1,0);
					else if(openBrackets.indexOf(c) != -1)
						prevLineBrackets++;
					break;
				}
			}
	
			/*
			 * On the current line,
			 * { should give us 0
			 * } should give us -1
			 * } fred { should give us -1
			 * { fred } should give us 0
			 */
			boolean lineStart = true; // False after initial indent
			int lineIndent = 0; // Indent width (tab expanded)
			int lineWidth = 0; // White space count
			int lineBrackets = 0; // Additional bracket indent
			int lineOpenBrackets = 0; // Number of opening brackets
			for(int i = 0; i < line.length(); i++)
			{
				char c = line.charAt(i);
				switch(c)
				{
				case ' ':
					if(lineStart)
					{
						lineIndent++;
						lineWidth++;
					}
					break;
				case '\t':
					if(lineStart)
					{
						lineIndent += (tabSize
							- (lineIndent
							% tabSize));
						lineWidth++;
					}
					break;
				default:
					lineStart = false;
					if(closeBrackets.indexOf(c) != -1)
					{
						if(lineOpenBrackets != 0)
							lineOpenBrackets--;
						else
							lineBrackets--;
					}
					else if(openBrackets.indexOf(c) != -1)
						lineOpenBrackets++;
					break;
				}
			}
							
			prevLineIndent += (prevLineBrackets + lineBrackets)
				* tabSize;

			// Insert a tab if line already has correct indent
			if(lineIndent >= prevLineIndent)
				return false;

			// Do it
			buffer.remove(start,lineWidth);
			buffer.insertString(start,MiscUtilities.createWhiteSpace(
				prevLineIndent,(noTabs ? 0 : tabSize)),null);
			return true;
		}
		catch(BadLocationException bl)
		{
		}
		return false;
	}

	/**
	 * Returns a <code>TokenMarker</code> for this mode. Can return null
	 * if this mode doesn's support syntax colorizing.
	 */
	public TokenMarker createTokenMarker()
	{
		return null;
	}

	/**
	 * Called when a buffer leaves this mode.
	 * @param buffer The buffer that left this mode
	 */
	public void leave(Buffer buffer) {}
	
	/**
	 * Called when a view leaves this mode.
	 * @param view The view that left this mode
	 */
	public void leaveView(View view) {}

	/**
	 * Returns the internal name of this edit mode.
	 */
	public String getName()
	{
		return name;
	}
	
	// private members
	private String name;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.11  1999/05/26 04:46:03  sp
 * Minor API change, soft tabs fixed ,1.7pre1
 *
 * Revision 1.10  1999/05/22 08:33:53  sp
 * FAQ updates, mode selection tweak, patch mode update, javadoc updates, JDK 1.1.8 fix
 *
 * Revision 1.9  1999/04/19 05:47:35  sp
 * ladies and gentlemen, 1.6pre1
 *
 * Revision 1.8  1999/03/21 07:53:14  sp
 * Plugin doc updates, action API change, new method in MiscUtilities, new class
 * loader, new plugin interface
 *
 * Revision 1.7  1999/03/12 07:23:19  sp
 * Fixed serious view bug, Javadoc updates
 *
 */
