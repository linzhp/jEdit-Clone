/*
 * DefaultSyntaxDocument.java - Simple implementation of SyntaxDocument
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

import javax.swing.event.*;
import javax.swing.text.*;
import java.util.*;

/**
 * A simple implementation of <code>SyntaxDocument</code>. It also takes
 * care of inserting and deleting lines from the token marker's state.
 *
 * @author Slava Pestov
 * @version $Id$
 *
 * @see org.gjt.sp.jedit.syntax.SyntaxDocument
 */
public class DefaultSyntaxDocument extends PlainDocument
implements SyntaxDocument
{
	/**
	 * Creates a new <code>DefaultSyntaxDocument</code> instance.
	 */
	public DefaultSyntaxDocument()
	{
		colors = new Hashtable();
		addDocumentListener(new SyntaxDocumentListener());
	}

	/**
	 * Returns the token marker that is to be used to split lines
	 * of this document up into tokens. May return null if this
	 * document is not to be colorized.
	 */
	public TokenMarker getTokenMarker()
	{
		return tokenMarker;
	}

	/**
	 * Sets the token marker that is to be used to split lines of
	 * this document up into tokens. May throw an exception if
	 * this is not supported for this type of document.
	 * @param tm The new token marker
	 */
	public void setTokenMarker(TokenMarker tm)
	{
		tokenMarker = tm;
		if(tm == null)
			return;
		tokenMarker.insertLines(0,getDefaultRootElement()
			.getElementCount());
		tokenizeLines();
	}

	/**
	 * Returns the dictionary that maps token identifiers to
	 * <code>java.awt.Color</code> objects.
	 */
	public Dictionary getColors()
	{
		return colors;
	}

	/**
	 * Sets the dictionary that maps token identifiers to
	 * <code>java.awt.Color</code> ojects. May throw an exception
	 * if this is not supported for this type of document.
	 * @param colors The new color dictionary
	 */
	public void setColors(Dictionary colors)
	{
		this.colors = colors;
	}

	/**
	 * Reparses the document, by passing all lines to the token
	 * marker. This should be called after the document is first
	 * loaded.
	 */
	public void tokenizeLines()
	{
		tokenizeLines(0,getDefaultRootElement().getElementCount());
	}

	/**
	 * Reparses the document, by passing the specified lines to the
	 * token marker. This should be called after a large quantity of
	 * text is first inserted.
	 * @param start The first line to parse
	 * @param len The number of lines, after the first one to parse
	 */
	public void tokenizeLines(int start, int len)
	{
		if(tokenMarker == null)
			return;

		Segment lineSegment = new Segment();
		Element map = getDefaultRootElement();

		len += start;

		try
		{
			for(int i = start; i < len; i++)
			{
				Element lineElement = map.getElement(i);
				int lineStart = lineElement.getStartOffset();
				getText(lineStart,lineElement.getEndOffset()
					- lineStart - 1,lineSegment);
				tokenMarker.markTokens(lineSegment,i);
			}
		}
		catch(BadLocationException bl)
		{
		}
	}

	// protected members
	protected TokenMarker tokenMarker;
	protected Dictionary colors;

	/**
	 * An implementation of <code>DocumentListener</code> that
	 * inserts and deletes lines from the token marker's state.
	 */
	public class SyntaxDocumentListener
	implements DocumentListener
	{
		public void insertUpdate(DocumentEvent evt)
		{
			if(tokenMarker == null)
				return;
			DocumentEvent.ElementChange ch = evt.getChange(
				getDefaultRootElement());
			if(ch == null)
				return;
			Element[] children = ch.getChildrenAdded();
			if(children == null)
				return;
			tokenMarker.insertLines(ch.getIndex() + 1,
				children.length - 1);
		}
	
		public void removeUpdate(DocumentEvent evt)
		{
			if(tokenMarker == null)
				return;
			DocumentEvent.ElementChange ch = evt.getChange(
				getDefaultRootElement());
			if(ch == null)
				return;
			Element[] children = ch.getChildrenRemoved();
			if(children == null)
				return;
			tokenMarker.deleteLines(ch.getIndex() + 1,
				children.length - 1);
		}
	
		public void changedUpdate(DocumentEvent evt)
		{
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.2  1999/03/29 06:30:25  sp
 * Documentation updates, fixed bug in DefaultSyntaxDocument, fixed bug in
 * goto-line
 *
 * Revision 1.1  1999/03/22 04:35:48  sp
 * Syntax colorizing updates
 *
 * Revision 1.1  1999/03/13 09:11:46  sp
 * Syntax code updates, code cleanups
 *
 */
