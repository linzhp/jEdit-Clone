/*
 * EditAction.java - Swing action subclass
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

import javax.swing.text.JTextComponent;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.util.Enumeration;

/**
 * An action implementation useful for jEdit commands. It provides support
 * for finding out the current view and buffer.
 */
public abstract class EditAction extends AbstractAction
{
	/**
	 * Creates a new <code>EditAction</code>.
	 * @param name The name of the action
	 */
	public EditAction(String name)
	{
		super(name);
	}

	/**
	 * Determines the view to use for the action.
	 */
	public static View getView(ActionEvent evt)
	{
		if(evt != null)
		{
			Object o = evt.getSource();
			if(o instanceof Component)
			{
				// find the parent view
				Component c = (Component)o;
				for(;;)
				{
					if(c instanceof View)
						return (View)c;
					else if(c == null)
						break;
					if(c instanceof JPopupMenu)
						c = ((JPopupMenu)c)
							.getInvoker();
					else
						c = c.getParent();
				}
			}
		}
		// this shouldn't happen
		System.out.println("BUG: getView() returning null");
		System.out.println("Report this to Slava Pestov <sp@gjt.org>");
		return null;
	}

	/**
	 * Determines the buffer to use for the action.
	 */
	public static Buffer getBuffer(ActionEvent evt)
	{
		// Call getBuffer() method of view
		View view = getView(evt);
		if(view != null)
			return view.getBuffer();
		return null;
	}
}
