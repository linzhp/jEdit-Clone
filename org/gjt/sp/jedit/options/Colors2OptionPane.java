/*
 * Colors2OptionPane.java - Colors #2 options panel
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

public class Colors2OptionPane extends ColorsOptionPane
{
	public Colors2OptionPane()
	{
		super("colors2");
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints cons = new GridBagConstraints();

		cons.gridx = cons.gridy = 0;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		JLabel label = new JLabel(jEdit.getProperty(
			"options.colors2.commentColor"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		commentColor = createColorButton("buffer.colors.comment1");
		layout.setConstraints(commentColor,cons);
		add(commentColor);

		cons.gridx = 0;
		cons.gridy = 1;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty(
			"options.colors2.literalColor"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		literalColor = createColorButton("buffer.colors.literal1");
		layout.setConstraints(literalColor,cons);
		add(literalColor);

		cons.gridx = 0;
		cons.gridy = 2;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty(
			"options.colors2.labelColor"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		labelColor = createColorButton("buffer.colors.label");
		layout.setConstraints(labelColor,cons);
		add(labelColor);

		cons.gridx = 0;
		cons.gridy = 3;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty(
			"options.colors2.keyword1Color"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		keyword1Color = createColorButton("buffer.colors.keyword1");
		layout.setConstraints(keyword1Color,cons);
		add(keyword1Color);

		cons.gridx = 0;
		cons.gridy = 4;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty(
			"options.colors2.keyword2Color"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		keyword2Color = createColorButton("buffer.colors.keyword2");
		layout.setConstraints(keyword2Color,cons);
		add(keyword2Color);

		cons.gridx = 0;
		cons.gridy = 5;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		label = new JLabel(jEdit.getProperty(
			"options.colors2.keyword3Color"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		keyword3Color = createColorButton("buffer.colors.keyword3");
		layout.setConstraints(keyword3Color,cons);
		add(keyword3Color);
	}

	public void save()
	{
		saveColorButton("buffer.colors.comment1",commentColor);
		saveColorButton("buffer.colors.comment2",commentColor);
		saveColorButton("buffer.colors.literal1",literalColor);
		saveColorButton("buffer.colors.literal2",literalColor);
		saveColorButton("buffer.colors.label",labelColor);
		saveColorButton("buffer.colors.keyword1",keyword1Color);
		saveColorButton("buffer.colors.keyword2",keyword2Color);
		saveColorButton("buffer.colors.keyword3",keyword3Color);
	}

	// private members
	private JButton commentColor;
	private JButton literalColor;
	private JButton labelColor;
	private JButton keyword1Color;
	private JButton keyword2Color;
	private JButton keyword3Color;
}
