/*
 * ErrorList.java - Error list
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

package org.gjt.sp.jedit.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Date;
import org.gjt.sp.jedit.*;

public class ErrorList extends JDialog
implements ListSelectionListener, WindowListener
{
	public ErrorList(View view, String cmd, Process process)
	{
		super(view,jEdit.getProperty("errorlist.title"),false);

		this.process = process;

		getContentPane().setLayout(new BorderLayout());
		Object[] args = { cmd, new Date().toString() };
		getContentPane().add("North",new JLabel(jEdit.getProperty(
			"errorlist.caption",args)));

		errors = new DefaultListModel();
		errorList = new JList(errors);
		errorList.setVisibleRowCount(10);
		errorList.addListSelectionListener(this);
		getContentPane().add("Center",new JScrollPane(errorList));

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);

		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		show();

		thread = new ErrorListThread();
	}

	public void valueChanged(ListSelectionEvent evt) {}

	public void windowOpened(WindowEvent evt) {}
	
	public void windowClosing(WindowEvent evt)
	{
		thread.stop();
		process.destroy();
		dispose();
	}
	
	public void windowClosed(WindowEvent evt) {}
	public void windowIconified(WindowEvent evt) {}
	public void windowDeiconified(WindowEvent evt) {}
	public void windowActivated(WindowEvent evt) {}
	public void windowDeactivated(WindowEvent evt) {}

	// private members
	private JList errorList;
	private DefaultListModel errors;
	private Process process;
	/*private*/ ErrorListThread thread;

	private class ErrorListThread extends Thread
	{
		ErrorListThread()
		{
			super("*** jEdit error list thread ***");
			start();
		}

		public void run()
		{
			try
			{
				BufferedReader in = new BufferedReader(
					new InputStreamReader(process
					.getInputStream()));
				BufferedReader err = new BufferedReader(
					new InputStreamReader(process
					.getErrorStream()));
				// read errors first
				String line;
				while((line = err.readLine()) != null)
				{
					errors.addElement(line);
				}

				// now read the output
				while((line = in.readLine()) != null)
				{
					errors.addElement(line);
				}
				in.close();
				err.close();
			}
			catch(IOException io)
			{
				Object[] args = { io.getMessage() };
				jEdit.error((View)getParent(),"ioerror",args);
			}
		}
	}
}
