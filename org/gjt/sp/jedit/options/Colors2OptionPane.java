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
			"options.colors2.comment1Color"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		comment1Color = createColorButton("buffer.colors.comment1");
		layout.setConstraints(comment1Color,cons);
		add(comment1Color);

		// [CHANGED] JL
		// This adds a sixth syntax color choice for color pane.
		// Just a copy&paste from the above. Label looks for property
		// "options.colors2.javadocColor" which doesn't exist but should
		// probably contain the label for this button. ColorButton is
		// created for reference javadocColor and loads the color for
		// comment2 from "buffer.colors.comment2".

		// SP: I changed the `color' button above to `color1' and
		// changed this to `color2'
		cons.gridx = 0;
		cons.gridy = 1;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty(
		    "options.colors2.comment2Color"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		comment2Color = createColorButton("buffer.colors.comment2");
		layout.setConstraints(comment2Color,cons);
		add(comment2Color);
		// [CHANGED] end changes

		cons.gridx = 0;
		cons.gridy = 2;
		cons.gridwidth = 3;
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
		cons.gridy = 3;
		cons.gridwidth = 3;
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
		cons.gridy = 4;
		cons.gridwidth = 3;
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
		cons.gridy = 5;
		cons.gridwidth = 3;
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
		cons.gridy = 6;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty(
			"options.colors2.keyword3Color"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		keyword3Color = createColorButton("buffer.colors.keyword3");
		layout.setConstraints(keyword3Color,cons);
		add(keyword3Color);

		cons.gridx = 0;
		cons.gridy = 7;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty(
			"options.colors2.operatorColor"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		operatorColor = createColorButton("buffer.colors.operator");
		layout.setConstraints(operatorColor,cons);
		add(operatorColor);

		cons.gridx = 0;
		cons.gridy = 8;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty(
			"options.colors2.invalidColor"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		invalidColor = createColorButton("buffer.colors.invalid");
		layout.setConstraints(invalidColor,cons);
		add(invalidColor);
	}

	public void save()
	{
		saveColorButton("buffer.colors.comment1",comment1Color);
		// [CHANGED] JL -- saves comment2 color from javadocColorButton
		saveColorButton("buffer.colors.comment2",comment2Color);
		saveColorButton("buffer.colors.literal1",literalColor);
		saveColorButton("buffer.colors.literal2",literalColor);
		saveColorButton("buffer.colors.label",labelColor);
		saveColorButton("buffer.colors.keyword1",keyword1Color);
		saveColorButton("buffer.colors.keyword2",keyword2Color);
		saveColorButton("buffer.colors.keyword3",keyword3Color);
		saveColorButton("buffer.colors.operator",operatorColor);
		saveColorButton("buffer.colors.invalid",invalidColor);
	}

	// private members
	private JButton comment1Color;
	private JButton comment2Color;  // [CHANGED] JL -- button for javadocs
	private JButton literalColor;
	private JButton labelColor;
	private JButton keyword1Color;
	private JButton keyword2Color;
	private JButton keyword3Color;
	private JButton operatorColor;
	private JButton invalidColor;
}
