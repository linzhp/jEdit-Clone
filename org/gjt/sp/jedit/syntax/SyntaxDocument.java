/*
 * SyntaxDocument.java - Interface all colorized documents must implement
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
package org.gjt.sp.jedit.syntax;

import javax.swing.text.Document;
import java.util.Dictionary;

/**
 * The interface a document must implement to be colorizable by the
 * <code>SyntaxEditorKit</code>. It defines two methods, one that returns
 * the <code>TokenMarker</code> that will split a line into a list of
 * tokens, and a method that returns a dictionary that maps identification
 * tags returned by the token marker into <code>Color</code> objects. The
 * possible token identifiers are defined as static fields in the
 * <code>Token</code> class.
 *
 * @author Slava Pestov
 * @version $Id$
 *
 * @see org.gjt.sp.jedit.syntax.SyntaxEditorKit
 * @see org.gjt.sp.jedit.syntax.TokenMarker
 * @see org.gjt.sp.jedit.syntax.Token
 */
public interface SyntaxDocument extends Document
{
	/**
	 * Returns the token marker that is to be used to split lines
	 * of this document up into tokens. May return null if this
	 * document is not to be colorized.
	 */
	public TokenMarker getTokenMarker();

	/**
	 * Returns the dictionary that maps token identifiers to
	 * <code>java.awt.Color</code> objects.
	 */
	public Dictionary getColors();
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/03/13 09:11:46  sp
 * Syntax code updates, code cleanups
 *
 */
