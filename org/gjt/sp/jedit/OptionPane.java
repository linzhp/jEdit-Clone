/*
 * OptionPane.java - Option pane interface
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

import javax.swing.JPanel;

/**
 * The class all option panes must extend.  An option pane is a tab
 * in the `Global Options' dialog.<p>
 *
 * The <i>internal name</i> of an option pane is that passed to
 * the constructor. It can be obtained with the <code>getName()</code>
 * method.<p>
 *
 * The following properties apply to option panes:
 * <ul>
 * <li><code>options.<i>internal name</i>.label</code> - the label of the
 * tab in the options dialog
 * </ul>
 *
 * Option panes can be registered with the editor by passing the
 * option pane's class to <code>jEdit.addOptionPane()</code>. A list
 * of registered option panes can be obtained with
 * <code>jEdit.getOptionPanes()</code>.
 *
 * @see org.gjt.sp.jedit.jEdit
 */
public abstract class OptionPane extends JPanel
{
	/**
	 * Creates a new option pane.
	 * @param name The internal name
	 */
	public OptionPane(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the internal name of this option pane.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Called when the options dialog's `ok' button is closed.
	 * This should save any properties saved in this option
	 * pane.
	 */
	public void save() {}

	// private members
	private String name;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/04/21 07:39:18  sp
 * FAQ added, plugins can now add panels to the options dialog
 *
 */
