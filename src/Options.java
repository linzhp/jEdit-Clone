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
import com.sun.java.swing.JTextField;
import com.sun.java.swing.border.TitledBorder;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
	private JTextField maxrecent;
	
	public Options(View view)
	{
		super(view,jEdit.props.getProperty("options.title"),true);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = constraints.HORIZONTAL;
		constraints.gridwidth = constraints.REMAINDER;
		getContentPane().setLayout(layout);
		JPanel panel = createLfPanel();
		layout.setConstraints(panel,constraints);
		getContentPane().add(panel);
		panel = createOpeningPanel();
		layout.setConstraints(panel,constraints);
		getContentPane().add(panel);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		show();
	}

	private JPanel createLfPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(jEdit.props.getProperty(
			"options.lf")));
		ButtonGroup grp = new ButtonGroup();
		metal = new JRadioButton(jEdit.props.getProperty(
			"options.lf.metal"));
		grp.add(metal);
		panel.add(metal);
		motif = new JRadioButton(jEdit.props.getProperty(
			"options.lf.motif"));
		grp.add(motif);
		panel.add(motif);
		windows = new JRadioButton(jEdit.props.getProperty(
			"options.lf.windows"));
		grp.add(windows);
		panel.add(windows);
		return panel;
	}

	private JPanel createOpeningPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(jEdit.props.getProperty(
			"options.opening")));
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth=2;
		constraints.gridheight=1;
		constraints.anchor=constraints.WEST;
		constraints.fill=constraints.BOTH;
		panel.setLayout(layout);
		server = new JCheckBox(jEdit.props.getProperty(
			"options.opening.server"));
		layout.setConstraints(server,constraints);
		panel.add(server);
		constraints.gridwidth=1;
		constraints.gridy=2;
		JLabel label = new JLabel(jEdit.props.getProperty(
			"options.opening.maxrecent"));
		layout.setConstraints(label,constraints);
		panel.add(label);
		maxrecent = new JTextField(2);
		layout.setConstraints(maxrecent,constraints);
		panel.add(maxrecent);
		return panel;
	}
	
	public void actionPerformed(ActionEvent evt)
	{
	}
}
