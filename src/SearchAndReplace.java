/*
 * SearchAndReplace.java - Search and replace dialog
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

import com.sun.java.swing.JButton;
import com.sun.java.swing.JCheckBox;
import com.sun.java.swing.JDialog;
import com.sun.java.swing.JLabel;
import com.sun.java.swing.JPanel;
import com.sun.java.swing.JSeparator;
import com.sun.java.swing.JTextField;
import com.sun.java.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class SearchAndReplace extends JDialog
implements ActionListener, WindowListener
{
	private View view;
	private JTextField find;
	private JTextField replace;
	private JCheckBox ignoreCase;
	private JButton findNext;
	private JButton replaceBtn;
	private JButton replaceAll;
	private JButton close;
	
	public SearchAndReplace(View view)
	{
		super(view,jEdit.props.getProperty("search.title"),true);
		this.view = view;
		find = new JTextField(jEdit.props.getProperty("lastfind"),30);
		replace = new JTextField(jEdit.props
			.getProperty("lastreplace"),30);
		ignoreCase = new JCheckBox(jEdit.props.getProperty(
			"search.ignoreCase"),
			"on".equals(jEdit.props.getProperty("ignoreCase")));
		findNext = new JButton(jEdit.props.getProperty("search.next"));
		replaceBtn = new JButton(jEdit.props
			.getProperty("search.replaceBtn"));
		replaceAll = new JButton(jEdit.props
			.getProperty("search.replaceAll"));
		close = new JButton(jEdit.props.getProperty("search.close"));
		getContentPane().setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = constraints.gridheight = 1;
		constraints.fill = constraints.BOTH;
		constraints.weightx = 1.0f;
		JLabel label = new JLabel(jEdit.props
			.getProperty("search.find"),SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		layout.setConstraints(find,constraints);
		panel.add(find);
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		constraints.gridy = 2;
		label = new JLabel(jEdit.props.getProperty("search.replace"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		layout.setConstraints(replace,constraints);
		panel.add(replace);
		getContentPane().add("North",panel);
		getContentPane().add("Center",new JSeparator());
		panel = new JPanel();
		panel.add(ignoreCase);
		panel.add(findNext);
		panel.add(replaceBtn);
		panel.add(replaceAll);
		panel.add(close);
		getContentPane().add("South",panel);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		findNext.addActionListener(this);
		replaceBtn.addActionListener(this);
		replaceAll.addActionListener(this);
		close.addActionListener(this);
		show();
	}
	
	public void save()
	{
		jEdit.props.put("lastfind",find.getText());
		jEdit.props.put("lastreplace",replace.getText());
		jEdit.props.put("ignoreCase",ignoreCase.getModel().isSelected()
			? "on" : "off");
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		save();
		Object source = evt.getSource();
		if(source == close)
			dispose();
		else if(source == findNext)
			view.getBuffer().findNext(view);
		else if(source == replaceBtn)
			view.getBuffer().replace(view);
		else if(source == replaceAll)
			view.getBuffer().replaceAll(view);
	}

	public void windowOpened(WindowEvent evt) {}
	
	public void windowClosing(WindowEvent evt)
	{
		save();
		dispose();
	}
	
	public void windowClosed(WindowEvent evt) {}
	public void windowIconified(WindowEvent evt) {}
	public void windowDeiconified(WindowEvent evt) {}
	public void windowActivated(WindowEvent evt) {}
	public void windowDeactivated(WindowEvent evt) {}
}
