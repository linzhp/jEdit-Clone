/*
 * TextAreaEvents.java - Handles events and passes them to an input handler
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

package org.gjt.sp.jedit.textarea;

/**
 * The text area event manager. It handles events, and passes them to
 * the current input manager.
 * @author Slava Pestov
 * @version $Id$
 */
public class TextAreaEvents
{
	/**
	 * Creates a new event manager. This should be not be called
	 * directly.
	 */
	public TextAreaEvents(JEditTextArea textArea)
	{
		this.textArea = textArea;
	}

	// protected members
	protected JEditTextArea textArea;
}
