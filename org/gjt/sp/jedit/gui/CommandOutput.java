/*
 * CommandOutput.java - Command output dialog
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

public class CommandOutput extends JDialog
implements KeyListener
{
	public CommandOutput(View view, String cmd, Process process)
	{
		super(view,jEdit.getProperty("output.title"),false);

		this.process = process;

		getContentPane().setLayout(new BorderLayout());
		Object[] args = { cmd, new Date().toString() };
		getContentPane().add("North",new JLabel(jEdit.getProperty(
			"output.caption",args)));

		output = new JTextArea(10,60);
		output.setFont(view.getTextArea().getFont());
		output.setEditable(false);
		output.addKeyListener(this);
		getContentPane().add("Center",new JScrollPane(output));

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addKeyListener(this);

		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		show();

		stdout = new StdoutThread();
		stderr = new StderrThread();
	}

	public void dispose()
	{
		stdout.stop();
		stderr.stop();
		process.destroy();
		super.dispose();
	}

	public void keyPressed(KeyEvent evt)
	{
		if(evt.getKeyCode() == KeyEvent.VK_ESCAPE
			|| evt.getKeyCode() == KeyEvent.VK_ENTER)
			dispose();
	}

	public void keyReleased(KeyEvent evt) {}
	public void keyTyped(KeyEvent evt) {}

	// private members
	private JTextArea output;
	private Process process;
	private StdoutThread stdout;
	private StderrThread stderr;

	private void addOutput(final String msg)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				output.append(msg);
				output.append("\n");
			}
		});
	}
	
	private class StdoutThread extends Thread
	{
		StdoutThread()
		{
			super("*** jEdit stdout read thread ***");
			start();
		}

		public void run()
		{
			try
			{
				BufferedReader in = new BufferedReader(
					new InputStreamReader(process
					.getInputStream()));
				
				String line;
				while((line = in.readLine()) != null)
				{
					addOutput(line);
				}
				in.close();
				addOutput(jEdit.getProperty("output.done"));
			}
			catch(IOException io)
			{
				Object[] args = { io.getMessage() };
				jEdit.error((View)getParent(),"ioerror",args);
			}
		}
	}

	private class StderrThread extends Thread
	{
		StderrThread()
		{
			super("*** jEdit stderr read thread ***");
			start();
		}

		public void run()
		{
			try
			{
				BufferedReader in = new BufferedReader(
					new InputStreamReader(process
					.getErrorStream()));
				
				String line;
				while((line = in.readLine()) != null)
				{
					addOutput(line);
				}
				in.close();
			}
			catch(IOException io)
			{
				Object[] args = { io.getMessage() };
				jEdit.error((View)getParent(),"ioerror",args);
			}
		}
	}
}
