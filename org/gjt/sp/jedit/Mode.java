/*
 * Mode.java - jEdit editing mode
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

package org.gjt.sp.jedit;

import java.util.Hashtable;
import jstyle.JSTokenMarker;

/**
 * An edit mode. At the moment, edit modes can define indent behaviour
 * and syntax colorizing rules. Other uses will exist in the future.
 * @see CommandMgr#getMode
 * @see CommandMgr#getModeName
 * @see Buffer#getMode
 * @see Buffer#getModeName
 * @see Buffer#setMode
 */
public interface Mode
{
	/**
	 * Called when a buffer enters this mode.
	 * @param buffer The buffer that entered this mode
	 */
	public abstract void enter(Buffer buffer);
	
	/**
	 * Called when the `Tab' key is pressed. This should perform
	 * mode-specific indentation and return true, or return false if a
	 * normal tab is to be inserted.
	 * @param buffer The buffer where the tab key was pressed
	 * @param view The view where the tab key was pressed
	 * @param caret The caret position
	 * @return true if the tab key event should be swallowed (ignored)
	 * false if a real tab should be inserted
	 */
	public abstract boolean indentLine(Buffer buffer, View view,
		int caret);

	/**
	 * Returns a JSTokenMarker for this mode. Can return null if this
	 * mode doesn's support syntax colorizing.
	 */
	public abstract JSTokenMarker createTokenMarker();

	/**
	 * Called when a buffer leaves this mode.
	 * @param buffer The buffer that left this mode
	 */
	public abstract void leave(Buffer buffer);
}
