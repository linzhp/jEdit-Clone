/*
 * PastePredefined.java - Paste predefined dialog
 * Copyright (C) 1999 Slava Pestov
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
import java.util.*;
import org.gjt.sp.jedit.*;

public class PastePredefined extends JDialog
implements ActionListener, KeyListener, MouseListener
{
	public PastePredefined(View view)
	{
		super(view,jEdit.getProperty("pastepredef.title"),true);
		this.view = view;
		Container content = getContentPane();
		content.setLayout(new BorderLayout());

		content.add(new JLabel(jEdit.getProperty(
			"pastepredef.caption")), BorderLayout.NORTH);

		clipModel = new DefaultListModel();
		clipVector = new Vector();
		int i = 0;
		for(;;)
		{
			String clip = jEdit.getProperty("clipPredefined." + i);
			if(clip == null)
				break;
			clipVector.addElement(clip);
			clipModel.addElement(truncString(clip));
			i++;
		}
		clips = new JList(clipModel);
		clips.setVisibleRowCount(10);
		clips.setFont(view.getTextArea().getPainter().getFont());
		clips.addMouseListener(this);
		content.add(new JScrollPane(clips), BorderLayout.CENTER);

		JPanel buttons = new JPanel();
		insert = new JButton(jEdit.getProperty("common.insert"));
		insert.addActionListener(this);
		buttons.add(insert);
		add = new JButton(jEdit.getProperty("pastepredef.add"));
		add.addActionListener(this);
		buttons.add(add);
		delete = new JButton(jEdit.getProperty("pastepredef.delete"));
		delete.addActionListener(this);
		buttons.add(delete);
		edit = new JButton(jEdit.getProperty("pastepredef.edit"));
		edit.addActionListener(this);
		buttons.add(edit);
		cancel = new JButton(jEdit.getProperty("common.cancel"));
		cancel.addActionListener(this);
		buttons.add(cancel);
		content.add(buttons, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(insert);
		addKeyListener(this);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		show();
		clips.requestFocus();
	}

	public void dispose()
	{
		Enumeration enum = clipVector.elements();
		int i = 0;
		while(enum.hasMoreElements())
		{
			jEdit.setProperty("clipPredefined." + i,
				(String)enum.nextElement());
			i++;
		}
		jEdit.unsetProperty("clipPredefined." + i);
		super.dispose();
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		
		if(source == insert)
		{
			doInsert();
		}
		else if(source == add)
		{
			String clip = new ClippingEditor(view,null).getText();
			if(clip != null)
			{
				clipVector.addElement(clip);
				clipModel.addElement(truncString(clip));
			}
		}
		else if(source == delete)
		{
			int index = clips.getSelectedIndex();
			if(index != -1)
			{
				clipVector.removeElementAt(index);
				clipModel.removeElementAt(index);
			}
		}
		else if(source == edit)
		{
			int index = clips.getSelectedIndex();
			if(index != -1)
			{
				String clip = new ClippingEditor(view,(String)clipVector
					.elementAt(index)).getText();
				if(clip != null)
				{
					clipVector.setElementAt(clip,index);
					clipModel.setElementAt(truncString(clip),index);
				}
			}
		}
		else if(source == cancel)
			dispose();
	}

	public void keyPressed(KeyEvent evt)
	{
		switch(evt.getKeyCode())
		{
		case KeyEvent.VK_ENTER:
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
	// clipModel contains blah...blah versions, clipVector contains
	// full versions
	private DefaultListModel clipModel;
	private Vector clipVector;
	private JButton insert;
	private JButton add;
	private JButton delete;
	private JButton edit;
	private JButton cancel;

	private String truncString(String str)
	{
		str = str.replace('\n',' ');
		if(str.length() > 60)
		{
			str = str.substring(0,30) + " ... " + str.substring(
				str.length() - 30);
		}
		return str;
	}

	private void doInsert()
	{
		int index = clips.getSelectedIndex();
		if(index != -1)
			view.getTextArea().setSelectedText((String)
				clipVector.elementAt(index));
		dispose();
	}
}
