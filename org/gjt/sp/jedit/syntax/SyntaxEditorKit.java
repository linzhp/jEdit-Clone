/*
 * SyntaxEditorKit.java - jEdit's own editor kit
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

package org.gjt.sp.jedit.syntax;

import javax.swing.text.*;
import javax.swing.*;

/**
 * An implementation of <code>EditorKit</code> used for syntax colorizing.
 * It implements a view factory that maps elements to syntax colorizing
 * views.<p>
 *
 * This editor kit can be plugged into text components to give them
 * colorization features. It can be used in other applications, not
 * just jEdit. The syntax colorizing package doesn't depend on any
 * jEdit classes.
 *
 * @author Slava Pestov
 * @version $Id$
 *
 * @see org.gjt.sp.jedit.syntax.SyntaxView
 */
public class SyntaxEditorKit extends DefaultEditorKit implements ViewFactory
{
	/**
	 * Returns an instance of a view factory that can be used for
	 * creating views from elements. This implementation returns
	 * the current instance, because this class already implements
	 * <code>ViewFactory</code>.
	 */
	public ViewFactory getViewFactory()
	{
		return this;
	}

	/**
	 * Creates a view from an element that can be used for painting that
	 * element. This implementation returns a new <code>SyntaxView</code>
	 * instance.
	 * @param elem The element
	 */
	public View create(Element elem)
	{
		return new SyntaxView(elem);
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.7  1999/03/13 09:11:46  sp
 * Syntax code updates, code cleanups
 *
 * Revision 1.6  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.5  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
