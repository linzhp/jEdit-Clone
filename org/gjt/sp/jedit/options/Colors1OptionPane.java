/*
 * Colors1OptionPane.java - Colors #1 options panel
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

package org.gjt.sp.jedit.options;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.gjt.sp.jedit.jEdit;

public class Colors1OptionPane extends ColorsOptionPane
{
	public Colors1OptionPane()
	{
		super("colors1");
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints cons = new GridBagConstraints();

		cons.gridx = cons.gridy = 0;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		JLabel label = new JLabel(jEdit.getProperty(
			"options.colors1.bgColor"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		backgroundColor = createColorButton("view.bgColor");
		layout.setConstraints(backgroundColor,cons);
		add(backgroundColor);

		cons.gridx = 0;
		cons.gridy = 1;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty("options.colors1.fgColor"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		foregroundColor = createColorButton("view.fgColor");
		layout.setConstraints(foregroundColor,cons);
		add(foregroundColor);

		cons.gridx = 0;
		cons.gridy = 2;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty("options.colors1.caretColor"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		caretColor = createColorButton("view.caretColor");
		layout.setConstraints(caretColor,cons);
		add(caretColor);
		
		cons.gridx = 0;
		cons.gridy = 3;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty("options.colors1.selectionColor"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		selectionColor = createColorButton("view.selectionColor");
		layout.setConstraints(selectionColor,cons);
		add(selectionColor);

		cons.gridx = 0;
		cons.gridy = 4;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty("options.colors1.lineHighlightColor"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		lineHighlightColor = createColorButton("view.lineHighlightColor");
		layout.setConstraints(lineHighlightColor,cons);
		add(lineHighlightColor);

		cons.gridx = 0;
		cons.gridy = 5;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty("options.colors1.bracketHighlightColor"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		bracketHighlightColor = createColorButton("view.bracketHighlightColor");
		layout.setConstraints(bracketHighlightColor,cons);
		add(bracketHighlightColor);
	}

	public void save()
	{
		saveColorButton("view.bgColor",backgroundColor);
		saveColorButton("view.fgColor",foregroundColor);
		saveColorButton("view.caretColor",caretColor);
		saveColorButton("view.selectionColor",selectionColor);
		saveColorButton("view.lineHighlightColor",lineHighlightColor);
		saveColorButton("view.bracketHighlightColor",
			bracketHighlightColor);
	}

	// private members
	private JButton backgroundColor;
	private JButton foregroundColor;
	private JButton caretColor;
	private JButton selectionColor;
	private JButton lineHighlightColor;
	private JButton bracketHighlightColor;
}
