/*
 * EditorOptionPane.java - Editor options panel
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
import org.gjt.sp.jedit.*;

public class EditorOptionPane extends OptionPane
{
	public EditorOptionPane()
	{
		super("editor");
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = cons.gridy = 0;
		cons.gridwidth = 3;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		JLabel label = new JLabel(jEdit.getProperty("options.editor"
			+ ".font"),SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		font = new JComboBox(getToolkit().getFontList());
		font.setSelectedItem(jEdit.getProperty("view.font"));
		layout.setConstraints(font,cons);
		add(font);

		cons.gridx = 0;
		cons.gridy = 1;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.editor.fontstyle"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		String[] styles = { "plain", "bold", "italic", "boldItalic" };
		style = new JComboBox(styles);
		style.setSelectedItem(jEdit.getProperty("view.fontstyle"));
		layout.setConstraints(style,cons);
		add(style);

		cons.gridx = 0;
		cons.gridy = 2;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.editor.fontsize"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		String[] sizes = { "9", "10", "12", "14", "18", "24" };
		size = new JComboBox(sizes);
		size.setEditable(true);
		size.setSelectedItem(jEdit.getProperty("view.fontsize"));
		layout.setConstraints(size,cons);
		add(size);

		cons.gridx = 0;
		cons.gridy = 3;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.editor.width"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		viewWidth = new JTextField(jEdit.getProperty("view.geometry.w"));
		layout.setConstraints(viewWidth,cons);
		add(viewWidth);

		cons.gridx = 0;
		cons.gridy = 4;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.editor.height"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		viewHeight = new JTextField(jEdit.getProperty("view.geometry.h"));
		layout.setConstraints(viewHeight,cons);
		add(viewHeight);
		
		cons.gridx = 0;
		cons.gridy = 5;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.editor.tabSize"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		tabSize = new JTextField(jEdit.getProperty("buffer.tabSize"));
		layout.setConstraints(tabSize,cons);
		add(tabSize);

		cons.gridx = 0;
		cons.gridy = 6;
		cons.gridwidth = cons.REMAINDER;
		lineHighlight = new JCheckBox(jEdit.getProperty("options.editor"
			+ ".lineHighlight"));
		lineHighlight.getModel().setSelected("on".equals(jEdit
			.getProperty("view.lineHighlight")));
		layout.setConstraints(lineHighlight,cons);
		add(lineHighlight);

		cons.gridy = 7;
		bracketHighlight = new JCheckBox(jEdit.getProperty("options.editor"
			+ ".bracketHighlight"));
		bracketHighlight.getModel().setSelected("on".equals(jEdit
			.getProperty("view.bracketHighlight")));
		layout.setConstraints(bracketHighlight,cons);
		add(bracketHighlight);

		cons.gridy = 8;
		syntax = new JCheckBox(jEdit.getProperty("options.editor"
			+ ".syntax"));
		syntax.getModel().setSelected("on".equals(jEdit.getProperty(
			"buffer.syntax")));
		layout.setConstraints(syntax,cons);
		add(syntax);

		cons.gridy = 9;
		autoIndent = new JCheckBox(jEdit.getProperty("options.editor"
			+ ".autoIndent"));
		autoIndent.getModel().setSelected("on".equals(jEdit.getProperty(
			"view.autoindent")));
		layout.setConstraints(autoIndent,cons);
		add(autoIndent);

		cons.gridy = 10;
		blinkCaret = new JCheckBox(jEdit.getProperty("options.editor"
			+ ".blinkCaret"));
		blinkCaret.getModel().setSelected(!"0".equals(jEdit.getProperty(
			"view.caretBlinkRate")));
		layout.setConstraints(blinkCaret,cons);
		add(blinkCaret);
	}

	public void save()
	{
		jEdit.setProperty("view.font",(String)font.getSelectedItem());
		jEdit.setProperty("view.fontsize",(String)size.getSelectedItem());
		jEdit.setProperty("view.fontstyle",(String)style.getSelectedItem());
		jEdit.setProperty("view.geometry.w",viewWidth.getText());
		jEdit.setProperty("view.geometry.h",viewHeight.getText());
		jEdit.setProperty("buffer.tabSize",tabSize.getText());
		jEdit.setProperty("view.lineHighlight",lineHighlight.getModel()
			.isSelected() ? "on" : "off");
		jEdit.setProperty("view.bracketHighlight",bracketHighlight.getModel()
			.isSelected() ? "on" : "off");
		jEdit.setProperty("buffer.syntax",syntax.getModel().isSelected()
			? "on" : "off");
		jEdit.setProperty("view.autoindent",autoIndent.getModel()
			.isSelected() ? "on" : "off");
		jEdit.setProperty("view.caretBlinkRate",blinkCaret.getModel()
			.isSelected() ? "500" : "0");
	}

	// private members
	private JComboBox font;
	private JComboBox style;
	private JComboBox size;
	private JTextField viewWidth;
	private JTextField viewHeight;
	private JTextField tabSize;
	private JCheckBox lineHighlight;
	private JCheckBox bracketHighlight;
	private JCheckBox syntax;
	private JCheckBox autoIndent;
	private JCheckBox blinkCaret;
}
