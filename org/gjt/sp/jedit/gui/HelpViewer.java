/*
 * HelpViewer.java - HTML Help viewer
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
import javax.swing.event.*;
import javax.swing.text.html.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import org.gjt.sp.jedit.*;

public class HelpViewer extends JDialog
implements ActionListener, HyperlinkListener
{
	public HelpViewer(View view, URL url)
	{
		super(view,jEdit.getProperty("helpviewer.title"),false);
		
		history = new URL[25];

		getContentPane().setLayout(new BorderLayout());

		JPanel buttons = new JPanel();
		back = new JButton(jEdit.getProperty("helpviewer.back"));
		back.addActionListener(this);
		buttons.add(back);
		forward = new JButton(jEdit.getProperty("helpviewer.forward"));
		forward.addActionListener(this);
		buttons.add(forward);
		home = new JButton(jEdit.getProperty("helpviewer.home"));
		home.addActionListener(this);
		buttons.add(home);
		getContentPane().add("North",buttons);

		viewer = new JEditorPane();
		viewer.setEditable(false);
		viewer.addHyperlinkListener(this);
		getContentPane().add("Center",new JScrollPane(viewer));
		gotoURL(url,true);
		
		Dimension screen = getToolkit().getScreenSize();
		int width, height;
		try
		{
			width = Integer.parseInt(jEdit.getProperty("helpviewer.width"));
		}
		catch(NumberFormatException nf)
		{
			width = 600;
		}
		try
		{
			height = Integer.parseInt(jEdit.getProperty("helpviewer.height"));
		}
		catch(NumberFormatException nf)
		{
			height = 400;
		}
		setSize(width,height);
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		show();
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		if(source == back)
		{
			if(historyPos <= 1)
				getToolkit().beep();
			else
			{
				URL url = history[--historyPos - 1];
				gotoURL(url,false);
			}
		}
		else if(source == forward)
		{
			if(history.length - historyPos <= 1)
				getToolkit().beep();
			else
			{
				URL url = history[historyPos];
				if(url == null)
					getToolkit().beep();
				else
				{
					historyPos++;
					gotoURL(url,false);
				}
			}
		}
		else if(source == home)
			gotoURL(getClass().getResource("/doc/index.html"),true);
	}

	public void hyperlinkUpdate(HyperlinkEvent evt)
	{
		if(evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		{
			if(evt instanceof HTMLFrameHyperlinkEvent)
			{
				((HTMLDocument)viewer.getDocument())
					.processHTMLFrameHyperlinkEvent(
					(HTMLFrameHyperlinkEvent)evt);
			}
			else
			{
				gotoURL(evt.getURL(),true);
			}
		}
	}

	// private members
	private JButton back;
	private JButton forward;
	private JButton home;
	private JEditorPane viewer;
	private URL[] history;
	private int historyPos;

	private void gotoURL(URL url, boolean addToHistory)
	{
		try
		{
			viewer.setPage(url);
			if(addToHistory)
			{
				history[historyPos++] = url;
				if(history.length == historyPos)
					System.arraycopy(history,1,history,
						0,history.length);
				history[historyPos] = null;
			}
		}
		catch(IOException io)
		{
			Object[] args = { io.getMessage() };
			jEdit.error((View)getParent(),"ioerror",args);
		}
	}
}
