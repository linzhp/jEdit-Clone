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

import javax.swing.*;
import java.awt.*;

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
		setLayout(gridBag = new GridBagLayout());
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

	// protected members

	/**
	 * The layout manager.
	 */
	protected GridBagLayout gridBag;

	/**
	 * The number of components already added to the layout manager.
	 */
	protected int y;

	/**
	 * Adds a labeled component to the option pane.
	 * @param label The label
	 * @param comp The component
	 */
	protected void addComponent(String label, Component comp)
	{
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy = y++;
		cons.gridheight = 1;
		cons.gridwidth = 3;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;

		cons.gridx = 0;
		JLabel l = new JLabel(label,SwingConstants.RIGHT);
		gridBag.setConstraints(l,cons);
		add(l);

		cons.gridx = 3;
		cons.gridwidth = 1;
		gridBag.setConstraints(comp,cons);
		add(comp);
	}

	/**
	 * Adds a component to the option pane.
	 * @param comp The component
	 */
	protected void addComponent(Component comp)
	{
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy = y++;
		cons.gridheight = 1;
		cons.gridwidth = cons.REMAINDER;
		cons.fill = GridBagConstraints.NONE;
		cons.anchor = GridBagConstraints.WEST;
		cons.weightx = 1.0f;

		gridBag.setConstraints(comp,cons);
		add(comp);
	}

	// private members
	private String name;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.2  1999/05/01 00:55:11  sp
 * Option pane updates (new, easier API), syntax colorizing updates
 *
 * Revision 1.1  1999/04/21 07:39:18  sp
 * FAQ added, plugins can now add panels to the options dialog
 *
 */
