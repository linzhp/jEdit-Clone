/*
 * Console.java - Command output window
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
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.gjt.sp.jedit.*;

public class Console extends JFrame
implements ActionListener, KeyListener, ListSelectionListener, WindowListener
{
	public Console(View view, String defaultCmd)
	{
		super(jEdit.getProperty("console.title"));

		this.view = view;

		JPanel panel = new JPanel(new BorderLayout());
		panel.add("West",new JLabel(jEdit.getProperty("console.cmd")));
		panel.add("Center",cmd = new HistoryTextField("console",40));
		cmd.setText(defaultCmd);
		cmd.addKeyListener(this);
		getContentPane().add("North",panel);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(jEdit.getProperty("console.output"),
			new JScrollPane(output = new JTextArea(15,40)));
		output.setEditable(false);
		tabs.addTab(jEdit.getProperty("console.errors"),
			new JScrollPane(errors = new JList(jEdit.getErrorList())));
		errors.setVisibleRowCount(15);
		errors.addListSelectionListener(this);
		getContentPane().add("Center",tabs);

		panel = new JPanel();
		panel.add(run = new JButton(jEdit.getProperty("console.run")));
		getRootPane().setDefaultButton(run);
		panel.add(stop = new JButton(jEdit.getProperty("console.stop")));
		panel.add(close = new JButton(jEdit.getProperty("console.close")));
		run.addActionListener(this);
		close.addActionListener(this);
		getContentPane().add("South",panel);

		addKeyListener(this);
		addWindowListener(this);

		pack();
		GUIUtilities.loadGeometry(this,"console");
		show();

		cmd.requestFocus();
	}

	public void run(String command)
	{
		stop();
		output.append("> ");
		output.append(command);
		output.append("\n");
		
		jEdit.clearErrors();

		try
		{
			process = Runtime.getRuntime().exec(command);
			process.getOutputStream().close();
		}
		catch(IOException io)
		{
			String[] args = { io.getMessage() };
			output.append(jEdit.getProperty("console.ioerror",args));
			output.append("\n");
			return;
		}
		stdout = new StdoutThread();
		stderr = new StderrThread();
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		if(source == run)
			run();
		else if(source == stop)
			stop();
		else if(source == close)
			close();
	}

	public void keyPressed(KeyEvent evt)
	{
		switch(evt.getKeyCode())
		{
		case KeyEvent.VK_ENTER:
			run();
			evt.consume();
			break;
		case KeyEvent.VK_ESCAPE:
			close();
			evt.consume();
			break;
		}
	}

	public void keyReleased(KeyEvent evt) {}
	public void keyTyped(KeyEvent evt) {}

	public void valueChanged(ListSelectionEvent evt)
	{
		if(errors.isSelectionEmpty())
			return;

		if(!view.isVisible())
		{
			view = jEdit.newView(view,(Buffer)jEdit.getBuffers()
				.nextElement());
		}

		int errorNo = errors.getSelectedIndex();
		jEdit.setCurrentError(errorNo);
		CompilerError error = jEdit.getError(errorNo);
		Buffer buffer = error.openFile();
		int lineNo = error.getLineNo();
		Element lineElement = buffer.getDefaultRootElement()
			.getElement(lineNo);
		int start = (lineElement == null ? 0 : lineElement
			.getStartOffset());
		if(view.getBuffer() == buffer)
			view.getTextArea().setCaretPosition(start);
		else
		{
			buffer.setCaretInfo(start,start);
			view.setBuffer(buffer);
			view.updateBuffersMenu();
		}
	}
	
	public void windowOpened(WindowEvent evt) {}
	
	public void windowClosing(WindowEvent evt)
	{
		close();
	}
	
	public void windowClosed(WindowEvent evt) {}
	public void windowIconified(WindowEvent evt) {}
	public void windowDeiconified(WindowEvent evt) {}
	public void windowActivated(WindowEvent evt) {}
	public void windowDeactivated(WindowEvent evt) {}

	// private members
	private HistoryTextField cmd;
	private JTextArea output;
	private JList errors;
	private JButton run;
	private JButton stop;
	private JButton close;

	private View view;

	private Process process;
	private StdoutThread stdout;
	private StderrThread stderr;

	private static final int GENERIC = 0;
	private static final int TEX = 1;
	private static final int EMACS = 2;
	private int errorMode;

	private String file;
	private int lineNo;
	private String error;

	private void run()
	{
		String s = cmd.getText();
		if(s != null && s.length() != 0)
		{
			cmd.addCurrentToHistory();
			cmd.setText(null);
			run(s);
		}
	}

	private void stop()
	{
		if(process != null)
		{
			stdout.stop();
			stderr.stop();
			process.destroy();
			process = null;
		}
	}

	private void close()
	{
		GUIUtilities.saveGeometry(this,"console");
		cmd.save();
		dispose();
	}

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
		{
			errorMode = GENERIC;
			return;
		}

		// Do the funky error state machine (and hope that the
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
			// We have to start at offset 3, otherwise DOS drive
			// letters will be caught (E:\file:12:error)
			int fileIndex = msg.indexOf(':',2);
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
		}
	}
}
