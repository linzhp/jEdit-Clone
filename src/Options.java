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

import com.sun.java.swing.*;
import com.sun.java.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

public class Options extends JDialog
implements ActionListener
{
	public static final String METAL = "com.sun.java.swing.plaf.metal"
		+ ".MetalLookAndFeel";
	public static final String MOTIF = "com.sun.java.swing.plaf.motif"
		+ ".MotifLookAndFeel";
	public static final String WINDOWS = "com.sun.java.swing.plaf.windows"
		+ ".WindowsLookAndFeel";
	public static final String LF = "\n";
	public static final String CRLF = "\r\n";
	public static final String CR = "\r";
	private JRadioButton metal;
	private JRadioButton motif;
	private JRadioButton windows;
	private JCheckBox server;
	private JTextField maxrecent;
	private JTextField autosave;
	private JTextField backups;
	private JRadioButton newlineUnix;
	private JRadioButton newlineWindows;
	private JRadioButton newlineMac;
	private JComboBox font;
	private JComboBox fontSize;
	private JRadioButton off;
	private JRadioButton charWrap;
	private JRadioButton wordWrap;
	private JTextField tabSize;
	private JCheckBox autoindent;
	private JTextField top;
	private JTextField left;
	private JTextField bottom;
	private JTextField right;
	private JButton ok;
	private JButton cancel;

	public Options(View view)
	{
		super(view,jEdit.props.getProperty("options.title"),true);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = constraints.BOTH;
		constraints.weightx = 1.0f;
		Container content = getContentPane();
		content.setLayout(layout);
		JPanel panel = createLfPanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		constraints.gridy = 1;
		panel = createOpeningPanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		constraints.gridy = 2;
		panel = createSavingPanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		constraints.gridy = 3;
		panel = createEditingPanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		constraints.gridy = 4;
		panel = createPrintingPanel();
		layout.setConstraints(panel,constraints);
		content.add(panel);
		constraints.gridy = 5;
		panel = createButtonsPanel();
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
		content.setBorder(new TitledBorder(jEdit.props.getProperty(
			"options.lf")));
		String lf = UIManager.getLookAndFeel().getClass().getName();
		ButtonGroup grp = new ButtonGroup();
		metal = new JRadioButton(jEdit.props.getProperty(
			"options.lf.metal"));
		metal.getModel().setSelected(METAL.equals(lf));
		grp.add(metal);
		content.add(metal);
		motif = new JRadioButton(jEdit.props.getProperty(
			"options.lf.motif"));
		motif.getModel().setSelected(MOTIF.equals(lf));
		grp.add(motif);
		content.add(motif);
		windows = new JRadioButton(jEdit.props.getProperty(
			"options.lf.windows"));
		windows.getModel().setSelected(WINDOWS.equals(lf));
		grp.add(windows);
		content.add(windows);
		return content;
	}

	private JPanel createOpeningPanel()
	{
		JPanel content = new JPanel();
		content.setBorder(new TitledBorder(jEdit.props.getProperty(
			"options.opening")));
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = constraints.BOTH;
		constraints.weightx = 1.0f;
		server = new JCheckBox(jEdit.props.getProperty(
			"options.opening.server"));
		server.getModel().setSelected("on".equals(jEdit.props
			.getProperty("daemon.server.toggle")));
		layout.setConstraints(server,constraints);
		content.add(server);
		constraints.gridy = 1;
		JLabel label = new JLabel(jEdit.props.getProperty(
			"options.opening.maxrecent"),SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		content.add(label);
		constraints.gridx = 1;
		maxrecent = new JTextField(jEdit.props.getProperty(
			"buffermgr.recent"),5);
		layout.setConstraints(maxrecent,constraints);
		content.add(maxrecent);
		return content;
	}

	private JPanel createSavingPanel()
	{
		JPanel content = new JPanel();
		content.setBorder(new TitledBorder(jEdit.props.getProperty(
			"options.saving")));
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = constraints.BOTH;
		constraints.weightx = 1.0f;
		constraints.gridwidth = 3;
		JLabel label = new JLabel(jEdit.props.getProperty(
			"options.saving.autosave"),SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		content.add(label);
		constraints.gridx = 3;
		autosave = new JTextField(jEdit.props.getProperty("daemon."
			+ "autosave.interval"),5);
		layout.setConstraints(autosave,constraints);
		content.add(autosave);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 3;
		label = new JLabel(jEdit.props.getProperty(
			"options.saving.backups"),SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		content.add(label);
		constraints.gridx = 3;
		backups = new JTextField(jEdit.props.getProperty("buffer."
			+ "backup.count"),5);
		layout.setConstraints(backups,constraints);
		content.add(backups);
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		label = new JLabel(jEdit.props.getProperty(
			"options.saving.newline"),SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		content.add(label);
		String newline = jEdit.props.getProperty("buffer.line."
			+ "separator",System.getProperty("line.separator"));
		ButtonGroup grp = new ButtonGroup();
		constraints.gridx = 1;
		newlineUnix = new JRadioButton(jEdit.props.getProperty(
			"options.saving.newline.unix"));
		grp.add(newlineUnix);
		newlineUnix.getModel().setSelected(LF.equals(newline));
		layout.setConstraints(newlineUnix,constraints);
		content.add(newlineUnix);
		constraints.gridx = 2;
		newlineWindows = new JRadioButton(jEdit.props.getProperty(
			"options.saving.newline.windows"));
		grp.add(newlineWindows);
		newlineWindows.getModel().setSelected(CRLF.equals(newline));
		layout.setConstraints(newlineWindows,constraints);
		content.add(newlineWindows);
		constraints.gridx = 3;
		newlineMac = new JRadioButton(jEdit.props.getProperty(
			"options.saving.newline.mac"));
		grp.add(newlineMac);
		newlineMac.getModel().setSelected(CR.equals(newline));
		layout.setConstraints(newlineMac,constraints);
		content.add(newlineMac);
		return content;
	}
	
	private JPanel createEditingPanel()
	{
		JPanel content = new JPanel();
		content.setBorder(new TitledBorder(jEdit.props.getProperty(
			"options.editing")));
		GridBagLayout layout = new GridBagLayout();
		content.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = constraints.BOTH;
		constraints.weightx = 1.0f;
		constraints.gridwidth = 1;
		JLabel label = new JLabel(jEdit.props.getProperty(
			"options.editing.font"),SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		content.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		font = new JComboBox(getToolkit().getFontList());
		font.setSelectedItem(jEdit.props.getProperty("view.font"));
		layout.setConstraints(font,constraints);
		content.add(font);
		constraints.gridx = 3;
		constraints.gridwidth = 1;
		Object[] sizes = { "9", "10", "12", "14", "18", "24" };
		fontSize = new JComboBox(sizes);
		fontSize.setSelectedItem(jEdit.props.getProperty(
			"view.fontsize"));
		layout.setConstraints(fontSize,constraints);
		content.add(fontSize);
		constraints.gridx = 0;
		constraints.gridy = 1;
		label = new JLabel(jEdit.props.getProperty(
			"options.editing.wrap"),SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		content.add(label);
		String wrap = jEdit.props.getProperty("view.linewrap");
		ButtonGroup grp = new ButtonGroup();
		constraints.gridx = 1;
		off = new JRadioButton(jEdit.props.getProperty(
			"options.editing.wrap.off"));
		off.getModel().setSelected("off".equals(wrap));
		grp.add(off);
		layout.setConstraints(off,constraints);
		content.add(off);
		constraints.gridx = 2;
		charWrap = new JRadioButton(jEdit.props.getProperty(
			"options.editing.wrap.char"));
		charWrap.getModel().setSelected("char".equals(wrap));
		grp.add(charWrap);
		layout.setConstraints(charWrap,constraints);
		content.add(charWrap);
		constraints.gridx = 3;
		wordWrap = new JRadioButton(jEdit.props.getProperty(
			"options.editing.wrap.word"));
		wordWrap.getModel().setSelected("word".equals(wrap));
		grp.add(wordWrap);
		layout.setConstraints(wordWrap,constraints);
		content.add(wordWrap);
		constraints.gridx = 0;
		constraints.gridy = 2;
		label = new JLabel(jEdit.props.getProperty(
			"options.editing.tabsize"),SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		content.add(label);
		constraints.gridx = 1;
		tabSize = new JTextField(jEdit.props.getProperty(
			"view.tabsize"),5);
		layout.setConstraints(tabSize,constraints);
		content.add(tabSize);
		constraints.gridx = 2;
		constraints.gridwidth = constraints.REMAINDER;
		autoindent = new JCheckBox(jEdit.props.getProperty(
			"options.editing.autoindent"));
		autoindent.getModel().setSelected("on".equals(jEdit.props
			.getProperty("view.autoindent")));
		layout.setConstraints(autoindent,constraints);
		content.add(autoindent);
		return content;
	}
	
	private JPanel createPrintingPanel()
	{
		JPanel content = new JPanel();
		content.setBorder(new TitledBorder(jEdit.props.getProperty(
			"options.printing")));
		content.setLayout(new GridLayout(2,4));
		content.add(new JLabel(jEdit.props.getProperty(
			"options.printing.top"),SwingConstants.RIGHT));
		top = new JTextField(jEdit.props.getProperty("buffer.margin."
			+ "top"),5);
		content.add(top);
		content.add(new JLabel(jEdit.props.getProperty(
			"options.printing.left"),SwingConstants.RIGHT));
		left = new JTextField(jEdit.props.getProperty("buffer.margin."
			+ "left"),5);
		content.add(left);
		content.add(new JLabel(jEdit.props.getProperty(
			"options.printing.bottom"),SwingConstants.RIGHT));
		bottom = new JTextField(jEdit.props.getProperty(
			"buffer.margin.bottom"),5);
		content.add(bottom);
		content.add(new JLabel(jEdit.props.getProperty(
			"options.printing.right"),SwingConstants.RIGHT));
		right = new JTextField(jEdit.props.getProperty("buffer.margin"
			+ ".right"),5);
		content.add(right);
		return content;
	}
	
	private JPanel createButtonsPanel()
	{
		JPanel content = new JPanel();
		ok = new JButton(jEdit.props.getProperty("options.ok"));
		ok.addActionListener(this);
		content.add(ok);
		cancel = new JButton(jEdit.props
			.getProperty("options.cancel"));
		cancel.addActionListener(this);
		content.add(cancel);
		return content;
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		if(source == cancel)
			dispose();
		else if(source == ok)
		{
			String lf;
			if(motif.getModel().isSelected())
				lf = MOTIF;
			else if(windows.getModel().isSelected())
				lf = WINDOWS;
			else
				lf = METAL;
			jEdit.props.put("lf",lf);
			jEdit.props.put("daemon.server.toggle",server
				.getModel().isSelected() ? "on" : "off");
			jEdit.props.put("buffermgr.recent.count",maxrecent
				.getText());
			jEdit.props.put("daemon.autosave.interval",autosave
				.getText());
			jEdit.props.put("buffer.backup.count",backups
				.getText());
			String newline;
			if(newlineWindows.getModel().isSelected())
				newline = CRLF;
			else if(newlineMac.getModel().isSelected())
				newline = CR;
			else
				newline = LF;
			jEdit.props.put("buffer.line.separator",newline);
			jEdit.props.put("view.font",font.getSelectedItem());
			jEdit.props.put("view.fontsize",fontSize
				.getSelectedItem());
			String wrap;
			if(charWrap.getModel().isSelected())
				wrap = "char";
			else if(wordWrap.getModel().isSelected())
				wrap = "word";
			else
				wrap = "off";
			jEdit.props.put("view.linewrap",wrap);
			jEdit.props.put("view.tabsize",tabSize.getText());
			jEdit.props.put("view.autoindent",autoindent
				.getModel().isSelected() ? "on" : "off");
			jEdit.props.put("buffer.margin.top",top.getText());
			jEdit.props.put("buffer.margin.left",left.getText());
			jEdit.props.put("buffer.margin.bottom",bottom
				.getText());
			jEdit.props.put("buffer.margin.right",right.getText());
			jEdit.propertiesChanged();
			Enumeration enum = jEdit.buffers.getViews();
			while(enum.hasMoreElements())
				((View)enum.nextElement()).propertiesChanged();
			dispose();
		}
	}
}
