/*
 * SyntaxEditorKit.java - jEdit's own editor kit
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

package org.gjt.sp.jedit.syntax;

import javax.swing.text.*;
import javax.swing.*;
import org.gjt.sp.jedit.jEdit;

/**
 * An editor kit that creates syntax colorizing views for elements.
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class SyntaxEditorKit extends DefaultEditorKit implements ViewFactory
{
	// public members

	/**
	 * Returns an instance of a view factory that can be used for
	 * creating views from elements. This implementation returns
	 * the current object, because this class already implements
	 * <code>ViewFactory</code>.
	 */
	public ViewFactory getViewFactory()
	{
		return this;
	}

	/**
	 * Creates a view from an element. This implementation returns
	 * a new <code>SyntaxView</code>
	 * @param elem The element
	 */
	public View create(Element elem)
	{
		return new SyntaxView(elem);
	}

	/**
	 * Returns the list of actions supported by this editor kit.
	 * This implementation returns a combination of the standard
	 * Swing actions and those registered with the jEdit class.
	 */
	public Action[] getActions()
	{
		return TextAction.augmentList(super.getActions(),
			jEdit.getActions());
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.5  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
