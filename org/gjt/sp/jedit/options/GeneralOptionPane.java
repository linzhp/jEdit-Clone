/*
 * GeneralOptionPane.java - General options panel
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

public class GeneralOptionPane extends OptionPane
{
	public static final String METAL = "javax.swing.plaf.metal"
		+ ".MetalLookAndFeel";
	public static final String MOTIF = "com.sun.java.swing.plaf.motif"
		+ ".MotifLookAndFeel";
	public static final String WINDOWS = "com.sun.java.swing.plaf.windows"
		+ ".WindowsLookAndFeel";
	public static final String MAC = "com.sun.java.swing.plaf.mac"
		+ ".MacLookAndFeel";

	public GeneralOptionPane()
	{
		super("general");
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints cons = new GridBagConstraints();
		cons.gridx = cons.gridy = 0;
		cons.gridwidth = 4;
		cons.gridheight = 1;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1.0f;
		JLabel label = new JLabel(jEdit.getProperty("options.general.lf.note"),
			SwingConstants.CENTER);
		layout.setConstraints(label,cons);
		add(label);

		cons.gridy = 1;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.lf"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		String lf = UIManager.getLookAndFeel().getClass().getName();
		String[] lfs = { "Java", "Mac", "Motif", "Windows" };
		lookAndFeel = new JComboBox(lfs);
		if(METAL.equals(lf))
			lookAndFeel.setSelectedIndex(0);
		else if(MAC.equals(lf))
			lookAndFeel.setSelectedIndex(1);
		else if(MOTIF.equals(lf))
			lookAndFeel.setSelectedIndex(2);
		else if(WINDOWS.equals(lf))
			lookAndFeel.setSelectedIndex(3);
		layout.setConstraints(lookAndFeel,cons);
		add(lookAndFeel);
		
		cons.gridx = 0;
		cons.gridy = 2;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.recent"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		recent = new JTextField(jEdit.getProperty("recent"));
		layout.setConstraints(recent,cons);
		add(recent);

		cons.gridx = 0;
		cons.gridy = 3;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.history"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		history = new JTextField(jEdit.getProperty("history"));
		layout.setConstraints(history,cons);
		add(history);

		cons.gridx = 0;
		cons.gridy = 4;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.autosave"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		autosave = new JTextField(jEdit.getProperty("autosave"));
		layout.setConstraints(autosave,cons);
		add(autosave);

		cons.gridx = 0;
		cons.gridy = 5;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.backups"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		backups = new JTextField(jEdit.getProperty("backups"));
		layout.setConstraints(backups,cons);
		add(backups);

		cons.gridx = 0;
		cons.gridy = 6;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.backupDirectory"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		backupDirectory = new JTextField(jEdit
			.getProperty("backup.directory"));
		layout.setConstraints(backupDirectory,cons);
		add(backupDirectory);

		cons.gridx = 0;
		cons.gridy = 7;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.backupPrefix"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		backupPrefix = new JTextField(jEdit
			.getProperty("backup.prefix"));
		layout.setConstraints(backupPrefix,cons);
		add(backupPrefix);

		cons.gridx = 0;
		cons.gridy = 8;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.backupSuffix"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		backupSuffix = new JTextField(jEdit
			.getProperty("backup.suffix"));
		layout.setConstraints(backupSuffix,cons);
		add(backupSuffix);

		cons.gridx = 0;
		cons.gridy = 9;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.lineSeparator"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		String[] lineSeps = { jEdit.getProperty("lineSep.unix"),
			jEdit.getProperty("lineSep.windows"),
			jEdit.getProperty("lineSep.mac") };
		lineSeparator = new JComboBox(lineSeps);
		String lineSep = jEdit.getProperty("buffer.lineSeparator",
			System.getProperty("line.separator"));
		if("\n".equals(lineSep))
			lineSeparator.setSelectedIndex(0);
		else if("\r\n".equals(lineSep))
			lineSeparator.setSelectedIndex(1);
		else if("\r".equals(lineSep))
			lineSeparator.setSelectedIndex(2);
		layout.setConstraints(lineSeparator,cons);
		add(lineSeparator);

		cons.gridx = 0;
		cons.gridy = 10;
		cons.gridwidth = 3;
		label = new JLabel(jEdit.getProperty("options.general.browser"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx = 3;
		cons.gridwidth = 1;
		String[] browsers = { "jedit_moz_remote", "iexplore.exe",
			"netscape.exe" };
		browser = new JComboBox(browsers);
		browser.setEditable(true);
		browser.setSelectedItem(jEdit.getProperty("browser"));
		layout.setConstraints(browser,cons);
		add(browser);

		cons.gridx = 0;
		cons.gridy = 11;
		cons.gridwidth = cons.REMAINDER;
		cons.fill = GridBagConstraints.NONE;
		cons.anchor = GridBagConstraints.WEST;
		saveDesktop = new JCheckBox(jEdit.getProperty(
			"options.general.saveDesktop"));
		saveDesktop.getModel().setSelected("on".equals(jEdit.getProperty(
			"saveDesktop")));
		layout.setConstraints(saveDesktop,cons);
		add(saveDesktop);

		cons.gridy = 12;
		server = new JCheckBox(jEdit.getProperty(
			"options.general.server"));
		server.getModel().setSelected("on".equals(jEdit.getProperty(
			"server")));
		layout.setConstraints(server,cons);
		add(server);

		cons.gridy = 13;
		showTips = new JCheckBox(jEdit.getProperty(
			"options.general.showTips"));
		showTips.getModel().setSelected("on".equals(jEdit.getProperty(
			"view.showTips")));
		layout.setConstraints(showTips,cons);
		add(showTips);

		cons.gridy = 14;
		showToolbar = new JCheckBox(jEdit.getProperty(
			"options.general.showToolbar"));
		showToolbar.getModel().setSelected("on".equals(jEdit.getProperty(
			"view.showToolbar")));
		layout.setConstraints(showToolbar,cons);
		add(showToolbar);
	}

	public void save()
	{
		String lf = (String)lookAndFeel.getSelectedItem();
		if("Java".equals(lf))
			lf = METAL;
		else if("Mac".equals(lf))
			lf = MAC;
		else if("Motif".equals(lf))
			lf = MOTIF;
		else if("Windows".equals(lf))
			lf = WINDOWS;
		jEdit.setProperty("lf",lf);
		jEdit.setProperty("saveDesktop",saveDesktop.getModel()
			.isSelected() ? "on" : "off");
		jEdit.setProperty("server",server.getModel().isSelected()
			? "on" : "off");
		jEdit.setProperty("autosave",autosave.getText());
		jEdit.setProperty("recent",recent.getText());
		jEdit.setProperty("history",history.getText());
		jEdit.setProperty("backups",backups.getText());
		jEdit.setProperty("backup.directory",backupDirectory.getText());
		jEdit.setProperty("backup.prefix",backupPrefix.getText());
		jEdit.setProperty("backup.suffix",backupSuffix.getText());
		String lineSep = null;
		switch(lineSeparator.getSelectedIndex())
		{
		case 0:
			lineSep = "\n";
			break;
		case 1:
			lineSep = "\r\n";
			break;
		case 2:
			lineSep = "\r";
			break;
		}
		jEdit.setProperty("buffer.lineSeparator",lineSep);
		jEdit.setProperty("browser",(String)browser.getSelectedItem());
		jEdit.setProperty("view.showTips",showTips.getModel()
			.isSelected() ? "on" : "off");
		jEdit.setProperty("view.showToolbar",showToolbar.getModel()
			.isSelected() ? "on" : "off");
	}

	// private members
	private JComboBox lookAndFeel;
	private JTextField autosave;
	private JTextField recent;
	private JTextField history;
	private JTextField backups;
	private JTextField backupDirectory;
	private JTextField backupPrefix;
	private JTextField backupSuffix;
	private JComboBox lineSeparator;
	private JComboBox browser;
	private JCheckBox saveDesktop;
	private JCheckBox server;
	private JCheckBox showTips;
	private JCheckBox showToolbar;
}
