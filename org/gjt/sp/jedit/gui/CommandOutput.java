/*
 * CommandOutput.java - Command output window
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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import org.gjt.sp.jedit.*;

public class CommandOutput extends JFrame
implements KeyListener
{
	public CommandOutput(View view, String cmd, Process process)
	{
		super(jEdit.getProperty("output.title"));

		this.view = view;
		this.process = process;

		jEdit.clearErrors();

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
		output.addKeyListener(this);

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
		{
			evt.consume();
			dispose();
		}
	}

	public void keyReleased(KeyEvent evt) {}
	public void keyTyped(KeyEvent evt) {}

	// private members
	private View view;
	private JTextArea output;
	private Process process;
	private StdoutThread stdout;
	private StderrThread stderr;

	// Error parsing state
	private static final int GENERIC = 0;
	private static final int TEX = 1;
	private static final int EMACS = 2;
	private int errorMode;

	// Intermediate data for TeX and Emacs mode
	private String file;
	private int lineNo;
	private String error;

	private synchronized void addOutput(final String msg)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				output.append(msg);
				output.append("\n");
			}
		});

		// Empty errors are of no use to us or the user
		if(msg.length() == 0)
			return;

		// Go the funky error state machine (and hope that the
		// other thread doesn't sent irrelevant crap our way)
		switch(errorMode)
		{
		case GENERIC:
			// Check for TeX-style error
			if(msg.charAt(0) == '!')
			{
				errorMode = TEX;
				error = msg.substring(1);
				break;
			}

			// Otherwise, check for standard filename:lineno:error
			int fileIndex = msg.indexOf(':');
			if(fileIndex == -1)
				break; /* Oops */
			file = msg.substring(0,fileIndex);

			int lineNoIndex = msg.indexOf(':',fileIndex + 1);
			if(lineNoIndex == -1)
				break; /* Oops */
			try
			{
				lineNo = Integer.parseInt(msg.substring(
					fileIndex + 1,lineNoIndex));
			}
			catch(NumberFormatException nf)
			{
				break; /* Oops */
			}

			// Emacs style errors have
			// filename:line:column:endline:endcolumn:
			int emacsIndex = msg.indexOf(':',lineNoIndex + 1);
			if(emacsIndex != -1)
			{
				if(msg.charAt(msg.length() - 1) == ':' &&
					msg.length() - emacsIndex != 1)
				{
					errorMode = EMACS;
					break;
				}
			}
			
			// If not, set the error message variable
			error = msg.substring(lineNoIndex + 1);

			jEdit.addError(file,lineNo,error);
			break;
		case TEX:
			// Check for l.<line number>
			if(msg.startsWith("l."))
			{
				lineNoIndex = msg.indexOf(' ');
				if(lineNoIndex == -1)
				{
					errorMode = GENERIC;
					break; /* Oops */
				}

				try
				{
					lineNo = Integer.parseInt(msg
						.substring(2,lineNoIndex));
				}
				catch(NumberFormatException nf)
				{
					errorMode = GENERIC;
					break; /* Oops */
				}

				error = error.concat(msg.substring(lineNoIndex));
				errorMode = GENERIC;

				jEdit.addError(view.getBuffer().getPath(),
					lineNo,error);
			}
			break;
		case EMACS:
			// Easy peasy
			error = msg.trim();
			errorMode = GENERIC;

			jEdit.addError(file,lineNo,error);
			break;
		}
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
					
			}
			catch(IOException io)
			{
				Object[] args = { io.getMessage() };
				GUIUtilities.error((View)getParent(),"ioerror",
					args);
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
				GUIUtilities.error((View)getParent(),"ioerror",args);
			}

			// Update error list menus
			Enumeration views = jEdit.getViews();
			while(views.hasMoreElements())
			{
				((View)views.nextElement()).updateErrorListMenu();
			}
		}
	}
}
