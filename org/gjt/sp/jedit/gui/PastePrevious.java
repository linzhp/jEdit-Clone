/*
 * PastePrevious.java - Paste previous dialog
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

package org.gjt.sp.jedit.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import org.gjt.sp.jedit.*;

public class PastePrevious extends JDialog
implements ActionListener, KeyListener, MouseListener
{
	public PastePrevious(View view)
	{
		super(view,jEdit.getProperty("pasteprev.title"),true);
		this.view = view;
		Container content = getContentPane();
		clipHistory = jEdit.getClipHistory();
		String[] abbrevClipHistory = new String[clipHistory.size()];
		for(int i = 0, j = clipHistory.size() - 1;
			i < clipHistory.size(); i++, j--)
		{
			String clip = (String)clipHistory.elementAt(i);
			clip = clip.replace('\n',' ');
			if(clip.length() > 60)
			{
				clip = clip.substring(0,30) + " ... "
					+ clip.substring(clip.length() - 30);
			}
			abbrevClipHistory[j] = clip;
		}
		clips = new JList(abbrevClipHistory);
		clips.setVisibleRowCount(10);
		clips.setFont(view.getTextArea().getFont());
		clips.addMouseListener(this);
		insert = new JButton(jEdit.getProperty("pasteprev.insert"));
		cancel = new JButton(jEdit.getProperty("pasteprev.cancel"));
		content.setLayout(new BorderLayout());
		content.add(new JLabel(jEdit.getProperty("pasteprev.caption")), BorderLayout.NORTH);
		content.add(new JScrollPane(clips), BorderLayout.CENTER);
		JPanel panel = new JPanel();
		panel.add(insert);
		panel.add(cancel);
		content.add(panel, BorderLayout.SOUTH);
		addKeyListener(this);
		getRootPane().setDefaultButton(insert);
		insert.addActionListener(this);
		cancel.addActionListener(this);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		show();
		clips.requestFocus();
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		if(source == insert)
			doInsert();
		else if(source == cancel)
			dispose();
	}

	public void keyPressed(KeyEvent evt)
	{
		switch(evt.getKeyCode())
		{
		case KeyEvent.VK_ENTER:
			doInsert();
			break;
		case KeyEvent.VK_ESCAPE:
			dispose();
			break;
		}
	}

	public void keyReleased(KeyEvent evt) {}
	public void keyTyped(KeyEvent evt) {}

	public void mouseClicked(MouseEvent evt)
	{
		if (evt.getClickCount() == 2)
		{
			doInsert();
		}
	}

	public void mouseEntered(MouseEvent evt) {}
	public void mouseExited(MouseEvent evt) {}
	public void mousePressed(MouseEvent evt) {}
	public void mouseReleased(MouseEvent evt) {}

	// private members
	private View view;
	private JList clips;
	private Vector clipHistory;
	private JButton insert;
	private JButton cancel;

	private void doInsert()
	{
		int selected = clips.getSelectedIndex();
		if(selected != -1)
		{
			String clip = (String)clipHistory.elementAt(
				clipHistory.size() - selected - 1);
			clipHistory.removeElementAt(selected);
			clipHistory.addElement(clip);
			view.getTextArea().replaceSelection(clip);
		}
		dispose();
	}
}
