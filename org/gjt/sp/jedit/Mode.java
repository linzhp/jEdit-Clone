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
import org.gjt.sp.jedit.syntax.TokenMarker;

/**
 * The interface all jEdit edit modes most subclass. An edit mode defines
 * a set of behaviours for editing a specific file type.<p>
 * 
 * The <i>internal</i> name of an edit mode is the class name, without
 * the package prefix. The user visible name is that stored in the
 * <code>mode.<i>internal name</i>.name</code> property.<p>
 *
 * A mode instance can be fetched if it's internal name is known with the
 * <code>jEdit.getMode()</code> method. The
 * <code>jEdit.getModeName()</code> method returns the user visible
 * name of a mode instance.<p>
 *
 * An array of currently installed edit modes can be obtained with the
 * <code>jEdit.getModes()</code> method.<p>
 *
 * When jEdit loads a plugin (JAR file) all classes that implement the
 * Mode interface are automatically added to the mode list. While jEdit is
 * running, modes can also be added with the <code>jEdit.addMode()</code>
 * method.<p>
 *
 * Currently, edit modes can influence the following:
 * <ul>
 * <li>The indentation performed, if any, when the user presses `Tab'.
 * this can be achieved by implementing the <code>indentLine()</code>
 * method.
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
 * @see org.gjt.sp.jedit.jEdit#addMode()
 * @see org.gjt.sp.jedit.Buffer#setMode()
 * @see org.gjt.sp.jedit.Buffer#setMode(Mode)
 * @see org.gjt.sp.jedit.syntax.TokenMarker
 */
public interface Mode
{
	/**
	 * Called when a buffer enters this mode.
	 * @param buffer The buffer that entered this mode
	 */
	public void enter(Buffer buffer);
	
	/**
	 * Called when a view enters this mode.
	 * @param view The view that entered this mode
	 */
	public void enterView(View view);

	/**
	 * If auto indent is enabled, this method is called when the `Tab'
	 * key is pressed to perform mode-specific indentation
	 * and return true, or return false if a normal tab is to be inserted.
	 * @param buffer The buffer where the tab key was pressed
	 * @param view The view where the tab key was pressed
	 * @param caret The caret position
	 * @return true if the tab key event should be swallowed (ignored)
	 * false if a real tab should be inserted
	 */
	public boolean indentLine(Buffer buffer, View view, int caret);

	/**
	 * Returns a <code>TokenMarker</code> for this mode. Can return null
	 * if this mode doesn's support syntax colorizing.
	 */
	public TokenMarker createTokenMarker();

	/**
	 * Called when a buffer leaves this mode.
	 * @param buffer The buffer that left this mode
	 */
	public void leave(Buffer buffer);
	
	/**
	 * Called when a view leaves this mode.
	 * @param view The view that left this mode
	 */
	public void leaveView(View view);
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.7  1999/03/12 07:23:19  sp
 * Fixed serious view bug, Javadoc updates
 *
 */
