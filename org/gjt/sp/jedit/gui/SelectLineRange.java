/*
 * SelectLineRange.java - Selects a range of lines
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.text.Element;
import javax.swing.*;
import org.gjt.sp.jedit.*;

/**
 * Select line range dialog.
 * @author Slava Pestov
 * @version $Id$
 */
public class SelectLineRange extends JDialog
implements ActionListener, KeyListener
{
	public SelectLineRange(View view)
	{
		super(view,jEdit.getProperty("selectlinerange.title"),true);

		buffer = view.getBuffer();
		
		getContentPane().add(BorderLayout.NORTH,new JLabel(
			jEdit.getProperty("selectlinerange.caption")));

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2,2));		
		panel.add(new JLabel(jEdit.getProperty("selectlinerange.start"),
			SwingConstants.RIGHT));
		panel.add(start = new HistoryTextField("line"));
		start.getEditor().getEditorComponent().addKeyListener(this);

		panel.add(new JLabel(jEdit.getProperty("selectlinerange.end"),
			SwingConstants.RIGHT));
		panel.add(end = new HistoryTextField("line"));
		end.getEditor().getEditorComponent().addKeyListener(this);
		getContentPane().add(BorderLayout.CENTER,panel);

		panel = new JPanel();
		panel.add(ok = new JButton(jEdit.getProperty("common.ok")));
		ok.addActionListener(this);
		panel.add(cancel = new JButton(jEdit.getProperty(
			"common.cancel")));
		cancel.addActionListener(this);
		getContentPane().add(BorderLayout.SOUTH,panel);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getRootPane().setDefaultButton(ok);
		addKeyListener(this);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		show();
		start.requestFocus();
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();

		if(source == ok || source == start || source == end)
			doSelectLineRange();
		else if(source == cancel)
			dispose();
	}

	public void keyPressed(KeyEvent evt)
	{
		switch(evt.getKeyCode())
		{
		case KeyEvent.VK_ESCAPE:
			dispose();
			break;
		}
	}

	public void keyReleased(KeyEvent evt) {}
	public void keyTyped(KeyEvent evt) {}

	// private members
	private Buffer buffer;
	private HistoryTextField start;
	private HistoryTextField end;
	private JButton ok;
	private JButton cancel;

	private void doSelectLineRange()
	{
		int startLine;
		int endLine;

		try
		{
			startLine = Integer.parseInt((String)start.getSelectedItem())
				- 1;
			endLine = Integer.parseInt((String)end.getSelectedItem())
				- 1;
		}
		catch(NumberFormatException nf)
		{
			getToolkit().beep();
			return;
		}

		Element startElement = buffer.getDefaultRootElement()
			.getElement(startLine);
		Element endElement = buffer.getDefaultRootElement()
			.getElement(endLine);
		
		if(startElement == null || endElement == null)
		{
			getToolkit().beep();
			return;
		}

		int startOffset = startElement.getStartOffset();
		int endOffset = endElement.getEndOffset();
		if(endOffset > buffer.getLength())
			endOffset = buffer.getLength();

		((View)getParent()).getTextArea().select(startOffset,endOffset);

		start.save();
		end.save();

		dispose();
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.5  1999/04/19 05:44:34  sp
 * GUI updates
 *
 * Revision 1.4  1999/04/02 03:21:09  sp
 * Added manifest file, common strings such as OK, etc are no longer duplicated
 * many times in jedit_gui.props
 *
 * Revision 1.3  1999/03/20 05:23:32  sp
 * Code cleanups
 *
 * Revision 1.2  1999/03/19 08:32:22  sp
 * Added a status bar to views, Escape key now works in dialog boxes
 *
 */
