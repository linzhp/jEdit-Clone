/*
 * OptionsDialog.java - Abstract tabbed options dialog
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
import java.util.*;
import org.gjt.sp.jedit.event.*;
import org.gjt.sp.jedit.*;

/**
 * An abstract tabbed options dialog box.
 * @author Slava Pestov
 * @version $Id$
 */
public class OptionsDialog extends JDialog
implements ActionListener, KeyListener, WindowListener
{
	public OptionsDialog(View view, String title)
	{
		super(view,title,true);
		getContentPane().setLayout(new BorderLayout());
		panes = new Vector();
		tabs = new JTabbedPane();
		getContentPane().add(BorderLayout.CENTER,tabs);
		JPanel buttons = new JPanel();
		ok = new JButton(jEdit.getProperty("common.ok"));
		ok.addActionListener(this);
		buttons.add(ok);
		getRootPane().setDefaultButton(ok);
		cancel = new JButton(jEdit.getProperty("common.cancel"));
		cancel.addActionListener(this);
		buttons.add(cancel);
		getContentPane().add(BorderLayout.SOUTH,buttons);
		addKeyListener(this);
		addWindowListener(this);
	}
	
	public void ok()
	{
		Enumeration enum = panes.elements();
		while(enum.hasMoreElements())
			((OptionPane)enum.nextElement()).save();

		/* This will fire the PROPERTITES_CHANGED event */
		jEdit.propertiesChanged();
		dispose();
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		if(source == ok)
			ok();
		else if(source == cancel)
			dispose();
	}

	public void keyPressed(KeyEvent evt)
	{
		switch(evt.getKeyCode())
		{
		case KeyEvent.VK_ENTER:
			ok();
			break;
		case KeyEvent.VK_ESCAPE:
			dispose();
			break;
		}
	}

	public void keyReleased(KeyEvent evt) {}

	public void keyTyped(KeyEvent evt) {}
	
	public void windowOpened(WindowEvent evt) {}
	
	public void windowClosing(WindowEvent evt)
	{
		dispose();
	}
	
	public void windowClosed(WindowEvent evt) {}
	public void windowIconified(WindowEvent evt) {}
	public void windowDeiconified(WindowEvent evt) {}
	public void windowActivated(WindowEvent evt) {}
	public void windowDeactivated(WindowEvent evt) {}

	// protected members
	protected Vector panes;
	protected JTabbedPane tabs;

	protected void addOptionPane(OptionPane pane)
	{
		tabs.addTab(jEdit.getProperty("options." + pane.getName()
			+ ".label"),pane);
		panes.addElement(pane);
	}

	// private members
	private JButton ok;
	private JButton cancel;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.2  1999/07/05 04:38:39  sp
 * Massive batch of changes... bug fixes, also new text component is in place.
 * Have fun
 *
 * Revision 1.1  1999/06/07 06:36:32  sp
 * Syntax `styling' (bold/italic tokens) added,
 * plugin options dialog for plugin option panes
 *
 */
