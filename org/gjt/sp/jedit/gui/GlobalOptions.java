/*
 * GlobalOptions.java - Global options dialog
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
import org.gjt.sp.jedit.options.*;
import org.gjt.sp.jedit.*;

/**
 * The global (editor-wide) settings dialog.
 * @author Slava Pestov
 * @version $Id$
 */
public class GlobalOptions extends JDialog
implements ActionListener, KeyListener, WindowListener
{
	public GlobalOptions(View view)
	{
		super(view,jEdit.getProperty("options.title"),true);
		getContentPane().setLayout(new BorderLayout());
		panes = new Vector();
		tabs = new JTabbedPane();
		addOptionPane(new GeneralOptionPane());
		addOptionPane(new EditorOptionPane());
		addOptionPane(new ColorTableOptionPane());
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
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		show();
	}
	
	public void ok()
	{
		Enumeration enum = panes.elements();
		while(enum.hasMoreElements())
			((OptionPane)enum.nextElement()).save();

		jEdit.fireEditorEvent(new EditorEvent(EditorEvent
			.PROPERTIES_CHANGED,null,null));

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

	// private members
	private Vector panes;
	private JTabbedPane tabs;
	private JButton ok;
	private JButton cancel;

	private void addOptionPane(OptionPane pane)
	{
		tabs.addTab(jEdit.getProperty("options." + pane.getName()
			+ ".label"),pane);
		panes.addElement(pane);
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.5  1999/04/02 03:21:09  sp
 * Added manifest file, common strings such as OK, etc are no longer duplicated
 * many times in jedit_gui.props
 *
 * Revision 1.4  1999/03/21 01:07:27  sp
 * Fixed stupid bug in global options
 *
 * Revision 1.3  1999/03/20 05:23:32  sp
 * Code cleanups
 *
 * Revision 1.2  1999/03/20 04:52:55  sp
 * Buffer-specific options panel finished, attempt at fixing OS/2 caret bug, code
 * cleanups
 *
 * Revision 1.1  1999/03/20 02:07:59  sp
 * Starting work on buffer-specific options panel
 *
 */
