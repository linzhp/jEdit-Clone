/*
 * Options.java - Options dialog
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

import com.sun.java.swing.ButtonGroup;
import com.sun.java.swing.JCheckBox;
import com.sun.java.swing.JDialog;
import com.sun.java.swing.JLabel;
import com.sun.java.swing.JPanel;
import com.sun.java.swing.JRadioButton;
import com.sun.java.swing.JSeparator;
import com.sun.java.swing.JTextField;
import com.sun.java.swing.SwingConstants;
import com.sun.java.swing.UIManager;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Options extends JDialog
implements ActionListener
{	
	private JRadioButton metal;
	private JRadioButton motif;
	private JRadioButton windows;
	private JCheckBox server;
	private JTextField maxRecent;
	private JTextField autosave;
	private JTextField backups;
	private JCheckBox autoIndent;
	private JRadioButton lineWrapOff;
	private JRadioButton charWrap;
	private JRadioButton wordWrap;
	
	public Options(View view)
	{
		super(view,jEdit.props.getProperty("options.title"),true);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = constraints.BOTH;
		constraints.anchor = constraints.EAST;
		//constraints.weightx = 1.0f;
		Container content = getContentPane();
		content.setLayout(layout);
		JPanel panel = createLfPanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		constraints.gridy = 1;
		JSeparator separator = new JSeparator();
		layout.setConstraints(separator,constraints);
		content.add(separator);
		constraints.gridy = 2;
		panel = createOpeningPanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		constraints.gridy = 3;
		separator = new JSeparator();
		layout.setConstraints(separator,constraints);
		content.add(separator);
		constraints.gridy = 4;
		panel = createAutosavePanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		constraints.gridy = 5;
		panel = createBackupsPanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		constraints.gridy = 6;
		separator = new JSeparator();
		layout.setConstraints(separator,constraints);
		content.add(separator);
		constraints.gridy = 7;
		panel = createEditingPanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		show();
	}

	private JPanel createLfPanel()
	{
		JPanel content = new JPanel();
		JLabel label = new JLabel(jEdit.props
			.getProperty("options.lf"),SwingConstants.RIGHT);
		content.add(label);
		//String lf = UIManager.getLookAndFeel();
		ButtonGroup grp = new ButtonGroup();
		metal = new JRadioButton(jEdit.props
			.getProperty("options.lf.metal"));
		grp.add(metal);
		content.add(metal);
		motif = new JRadioButton(jEdit.props
			.getProperty("options.lf.motif"));
		grp.add(motif);
		content.add(motif);
		windows = new JRadioButton(jEdit.props
			.getProperty("options.lf.windows"));
		grp.add(windows);
		content.add(windows);
		return content;
	}

	private JPanel createOpeningPanel()
	{
		JPanel content = new JPanel();
		server = new JCheckBox(jEdit.props
			.getProperty("options.opening.server"));
		content.add(server);
		JLabel label = new JLabel(jEdit.props
			.getProperty("options.opening.maxrecent"),
			SwingConstants.RIGHT);
		content.add(label);
		maxRecent = new JTextField(String.valueOf(jEdit.props
			.getProperty("maxrecent")),4);
		content.add(maxRecent);
		return content;
	}

	private JPanel createAutosavePanel()
	{
		JPanel content = new JPanel();
		JLabel label = new JLabel(jEdit.props.getProperty(
			"options.saving.autosave"),SwingConstants.RIGHT);
		content.add(label);
		autosave = new JTextField(jEdit.props.getProperty(
			"autosave.interval"),4);
		if(!"on".equals(jEdit.props.getProperty("autosave")))
			autosave.setText("0");
		content.add(autosave);
		return content;
	}

	private JPanel createBackupsPanel()
	{
		JPanel content = new JPanel();
		JLabel label = new JLabel(jEdit.props.getProperty(
			"options.saving.backups"),SwingConstants.RIGHT);
		content.add(label);
		backups = new JTextField(jEdit.props.getProperty(
			"backups"),4);
		content.add(backups);
		return content;
	}

	private JPanel createEditingPanel()
	{
		JPanel content = new JPanel();
		autoIndent = new JCheckBox(jEdit.props.getProperty(
			"options.editing.autoindent"),"on".equals(jEdit.props
			.getProperty("editor.autoindent")));
		content.add(autoIndent);
		ButtonGroup grp = new ButtonGroup();
		String lineWrap = jEdit.props.getProperty("editor.linewrap");
		lineWrapOff = new JRadioButton(jEdit.props.getProperty(
			"options.editing.linewrap.off"),"off".equals(
			lineWrap));
		grp.add(lineWrapOff);
		content.add(lineWrapOff);
		charWrap = new JRadioButton(jEdit.props.getProperty(
			"options.editing.linewrap.char"),"char".equals(
			lineWrap));
		grp.add(charWrap);
		content.add(charWrap);
		wordWrap = new JRadioButton(jEdit.props.getProperty(
			"options.editing.linewrap.word"),"word".equals(
			lineWrap));
		grp.add(wordWrap);
		content.add(wordWrap);
		return content;
	}
	
	public void actionPerformed(ActionEvent evt)
	{
	}
}
