/*
 * Console.java - Command output panel
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
import org.gjt.sp.jedit.event.*;
import org.gjt.sp.jedit.*;

public class Console extends JPanel
implements ActionListener, ListSelectionListener
{
	public Console(View view)
	{
		super(new BorderLayout());

		String osName = System.getProperty("os.name");
		appendEXE = (osName.indexOf("Windows") != -1 ||
			osName.indexOf("OS/2") != -1);

		this.view = view;

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.WEST,new JLabel(jEdit
			.getProperty("console.cmd")));
		panel.add(BorderLayout.CENTER,cmd = new HistoryTextField(
			"console"));
		cmd.addActionListener(this);
		add(BorderLayout.NORTH,panel);

		JTabbedPane tabs = new JTabbedPane(SwingConstants.BOTTOM);
		tabs.addTab(jEdit.getProperty("console.output"),
			new JScrollPane(output = new JTextArea()));
		output.setEditable(false);
		tabs.addTab(jEdit.getProperty("console.errors"),
			new JScrollPane(errorList = new JList(getErrorList())));
		errorList.setVisibleRowCount(15);
		errorList.addListSelectionListener(this);
		add(BorderLayout.CENTER,tabs);
	}

	public HistoryTextField getCommandField()
	{
		return cmd;
	}

	public void run(String command)
	{
		stop();

		output.setText("> " + command + "\n");

		if(appendEXE)
		{
			// append .exe to command name on Windows and OS/2
			int dotIndex = command.indexOf('.');
			int spaceIndex = command.indexOf(' ');
			if(dotIndex == -1 || dotIndex > spaceIndex)
			{
				command = command.substring(0,spaceIndex)
					+ ".exe" + command.substring(spaceIndex);
			}
		}
		
		if(errors != null)
		{
			for(int i = 0; i < errors.size(); i++)
			{
				CompilerError error =
					(CompilerError)errors.elementAt(i);
				jEdit.removeEditorListener(error);
			}
			errors.removeAllElements();
		}
		currentError = -1;

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

	public void stop()
	{
		if(process != null)
		{
			stdout.stop();
			stderr.stop();
			//process.destroy(); // Keep running
			process = null;
		}
	}

	/**
	 * Returns the error at the specified index in the error list.
	 * @param index The index in the error list
	 */
	public CompilerError getError(int index)
	{
		if(errors == null || index < 0 || index >= errors.size())
			return null;
		else
			return (CompilerError)errors.elementAt(index);
	}

	/**
	 * Returns the compiler error list as a Swing ListModel.
	 * Guaranteed to return non-null (as a JList requires).
	 */
	public DefaultListModel getErrorList()
	{
		if(errors == null)
			errors = new DefaultListModel();
		return errors;
	}

	/**
	 * Sets the current error number.
	 */
	public void setCurrentError(int error)
	{
		currentError = error;
		// XXX: causes valueChanged() to be called twice
		//errorList.setSelectedIndex(error);

		// Fire event
		view.fireViewEvent(new ViewEvent(ViewEvent.CURRENT_ERROR_CHANGED,
			view,null));
	}

	/**
	 * Gets the current error number.
	 */
	public int getCurrentError()
	{
		return currentError;
	}

	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource() == cmd)
		{
			String s = (String)cmd.getSelectedItem();
			if(s != null && s.length() != 0)
			{
				cmd.addCurrentToHistory();
				cmd.setSelectedItem(null);

				run(s);
			}
		}
	}

	public void valueChanged(ListSelectionEvent evt)
	{
		if(errorList.isSelectionEmpty())
			return;

		int errorNo = errorList.getSelectedIndex();
		setCurrentError(errorNo);
		CompilerError error = getError(errorNo);
		
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

	// private members
	private boolean appendEXE;

	private HistoryTextField cmd;
	private JTextArea output;
	private JList errorList;

	private View view;

	private DefaultListModel errors;
	private int currentError;

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

	private synchronized void addOutput(String msg)
	{
		SwingUtilities.invokeLater(new SafeAppend(msg));

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

			addError(file,lineNo,error);
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

				addError(view.getBuffer().getPath(),
					lineNo,error);
			}
			break;
		case EMACS:
			// Easy peasy
			error = msg.trim();
			errorMode = GENERIC;

			addError(file,lineNo,error);
			break;
		}
	}

	private void addError(String path, int lineNo, String error)
	{
		if(errors == null)
			errors = new DefaultListModel();
		SwingUtilities.invokeLater(new SafeAddError(
			new CompilerError(path,lineNo,error)));
	}

	class StdoutThread extends Thread
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

	class StderrThread extends Thread
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

	class SafeAppend implements Runnable
	{
		private String msg;

		SafeAppend(String msg)
		{
			this.msg = msg;
		}

		public void run()
		{
			output.append(msg);
			output.append("\n");
		}
	}

	class SafeAddError implements Runnable
	{
		private CompilerError error;

		SafeAddError(CompilerError error)
		{
			this.error = error;
		}

		public void run()
		{
			errors.addElement(error);
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.15  1999/03/22 04:20:01  sp
 * Syntax colorizing updates
 *
 * Revision 1.14  1999/03/20 05:23:32  sp
 * Code cleanups
 *
 * Revision 1.13  1999/03/20 00:26:48  sp
 * Console fix, backed out new JOptionPane code, updated tips
 *
 * Revision 1.12  1999/03/17 05:32:52  sp
 * Event system bug fix, history text field updates (but it still doesn't work), code cleanups, lots of banging head against wall
 *
 * Revision 1.11  1999/03/16 04:34:46  sp
 * HistoryTextField updates, moved generate-text to a plugin, fixed spelling mistake in EditAction Javadocs
 *
 * Revision 1.10  1999/03/13 08:50:39  sp
 * Syntax colorizing updates and cleanups, general code reorganizations
 *
 * Revision 1.9  1999/03/12 23:51:00  sp
 * Console updates, uncomment removed cos it's too buggy, cvs log tags added
 *
 */
