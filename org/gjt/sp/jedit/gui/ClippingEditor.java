/*
 * ClippingEditor.java - Paste predefined clipping editor dialog
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
import java.util.Vector;
import org.gjt.sp.jedit.*;

public class ClippingEditor extends JDialog
implements ActionListener, KeyListener
{
	public ClippingEditor(View view, String text)
	{
		super(view,jEdit.getProperty("clipedit.title"),true);
			
		getContentPane().setLayout(new BorderLayout());

		textArea = new JTextArea(text,8,50);
		textArea.setFont(view.getTextArea().getFont());
		getContentPane().add(BorderLayout.CENTER,
			new JScrollPane(textArea));
			
		JPanel buttons = new JPanel();
		ok = new JButton(jEdit.getProperty("clipedit.ok"));
		ok.addActionListener(this);
		buttons.add(ok);
		getRootPane().setDefaultButton(ok);
		cancel = new JButton(jEdit.getProperty("clipedit.cancel"));
		cancel.addActionListener(this);
		buttons.add(cancel);
		getContentPane().add(BorderLayout.SOUTH,buttons);
		
		addKeyListener(this);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		show();
	}

	public String getText()
	{
		if(isOk)
			return textArea.getText();
		else
			return null;
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();

		if(source == ok)
		{
			isOk = true;
			dispose();
		}
		else if(source == cancel)
			dispose();
	}

	public void keyPressed(KeyEvent evt)
	{
		switch(evt.getKeyCode())
		{
		case KeyEvent.VK_ENTER:
			isOk = true;
			dispose();
			break;
		case KeyEvent.VK_ESCAPE:
			dispose();
			break;
		}
	}

	public void keyReleased(KeyEvent evt) {}
	public void keyTyped(KeyEvent evt) {}

	// private members
	private JTextArea textArea;
	private JButton ok;
	private JButton cancel;
	private boolean isOk;
}
