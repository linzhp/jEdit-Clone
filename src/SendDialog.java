/*
 * SendDialog.java - Send To Dialog
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

import com.sun.java.swing.*;
import com.sun.java.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class SendDialog extends JDialog
implements ActionListener, WindowListener, Runnable
{
	public static final String CRLF = "\r\n";
	private View view;
	private Thread thread;
	private JTextField smtp;
	private JTextField from;
	private JTextField to;
	private JTextField subject;
	private JPanel buttons;
	private JButton send;
	private JButton cancel;
	
	public SendDialog(View view)
	{
		super(view,jEdit.props.getProperty("send.title"),true);
		this.view = view;
		JPanel panel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = constraints.gridheight = 1;
		constraints.fill = constraints.BOTH;
		constraints.weightx = 1.0f;
		JLabel label = new JLabel(jEdit.props
			.getProperty("send.smtp"),SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		smtp = new JTextField(jEdit.props
			.getProperty("send.smtp.value"),30);
		layout.setConstraints(smtp,constraints);
		panel.add(smtp);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		label = new JLabel(jEdit.props.getProperty("send.from"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		from = new JTextField(jEdit.props
			.getProperty("send.from.value"),30);
		layout.setConstraints(from,constraints);
		panel.add(from);
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		label = new JLabel(jEdit.props.getProperty("send.to"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		to = new JTextField(jEdit.props.getProperty("send.to.value"),30);
		layout.setConstraints(to,constraints);
		panel.add(to);
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 1;
		label = new JLabel(jEdit.props.getProperty("send.subject"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,constraints);
		panel.add(label);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		subject = new JTextField(view.getBuffer().getPath(),30);
		layout.setConstraints(subject,constraints);
		panel.add(subject);
		getContentPane().add("North",panel);
		getContentPane().add("Center",new JSeparator());
		buttons = new JPanel();
		send = new JButton(jEdit.props.getProperty("send.send"));
		buttons.add(send);
		cancel = new JButton(jEdit.props.getProperty("send.cancel"));
		buttons.add(cancel);
		getContentPane().add("South",buttons);
		Dimension screen = getToolkit().getScreenSize();
		pack();
		setLocation((screen.width - getSize().width) / 2,
			(screen.height - getSize().height) / 2);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		send.addActionListener(this);
		cancel.addActionListener(this);
		show();
	}

	public void run()
	{
		String smtp = this.smtp.getText();
		String from = this.from.getText();
		String to = this.to.getText();
		String subject = this.subject.getText();
		if(smtp.length() == 0 || from.length() == 0
			|| to.length() == 0)
		{
			jEdit.error(view,"sendempty",new Object[0]);
			return;
		}
		this.smtp.setEnabled(false);
		this.from.setEnabled(false);
		this.to.setEnabled(false);
		this.subject.setEnabled(false);
		JTextArea transcript = new JTextArea();
		transcript.setRows(10);
		transcript.setEditable(false);
		getContentPane().remove(buttons);
		getContentPane().add("South",new JScrollPane(transcript));
		pack();
		Object[] args = { smtp };
		transcript.append(jEdit.props.getProperty("send.connect",
			args));
		transcript.append(CRLF);
		try
		{
			int index = smtp.indexOf(':');
			int port = 25;
			if(index != -1)
			{
				port = Integer.parseInt(smtp.substring(index
					+ 1));
				smtp = smtp.substring(0,index);
			}
			Socket socket = new Socket(smtp,port);
			BufferedReader in = new BufferedReader(new
				InputStreamReader(socket.getInputStream()));
			Writer out = new OutputStreamWriter(socket
				.getOutputStream());
			String response = in.readLine();
			transcript.append(response);
			transcript.append(CRLF);
			if(!response.startsWith("220"))
				error("serverdown");
			String command = "HELO " + socket.getLocalAddress()
				.getHostName() + CRLF;
			transcript.append(command);
			out.write(command);
			out.flush();
			response = in.readLine();
			transcript.append(response);
			transcript.append(CRLF);
			if(!response.startsWith("250"))
				error("badhost");
			command = "MAIL FROM: \"" + from + "\"" + CRLF;
			transcript.append(command);
			out.write(command);
			out.flush();
			response = in.readLine();
			transcript.append(response);
			transcript.append(CRLF);
			if(!response.startsWith("250"))
				error("badsender");
			command = "RCPT TO: \"" + to + "\"" + CRLF;
			transcript.append(command);
			out.write(command);
			out.flush();
			response = in.readLine();
			transcript.append(response);
			transcript.append(CRLF);
			if(!response.startsWith("250"))
				error("badrecepient");
			command = "DATA" + CRLF;
			transcript.append(command);
			out.write(command);
			out.flush();
			response = in.readLine();
			transcript.append(response);
			transcript.append(CRLF);
			if(!response.startsWith("354"))
				error("badmsg");
			out.write("X-Mailer: jEdit " + jEdit.VERSION
				+ " build " + jEdit.BUILD);
			out.write(CRLF);
			Buffer buffer = view.getBuffer();
			Element map = buffer.getDefaultRootElement();
			int lines = map.getElementCount();
			for(int i = 0; i < lines; i++)
			{
				Element lineElement = map.getElement(i);
				int start = lineElement.getStartOffset();
				String line = buffer.getText(start,
					lineElement.getEndOffset() -
						(start + 1));
				if(".".equals(line))
					out.write(":");
				else
					out.write(line);
				out.write(CRLF);
			}
			out.write("." + CRLF);
			out.flush();
			response = in.readLine();
			transcript.append(response);
			transcript.append(CRLF);
			if(!response.startsWith("250"))
				error("badmsg");
			command = "QUIT" + CRLF;
			transcript.append(command);
			out.write(command);
			out.flush();
			response = in.readLine();
			transcript.append(response);
			transcript.append(CRLF);
			in.close();
			out.close();
			socket.close();
		}
		catch(IOException io)
		{
			args[0] = io.toString();
			jEdit.error(view,"ioerror",args);
		}
		catch(NumberFormatException nf)
		{
			jEdit.error(view,"badport",new Object[0]);
		}
		catch(BadLocationException bl)
		{
		}
		dispose();
	}

	private void error(String msg)
	{
		jEdit.error(view,msg,new Object[0]);
		dispose();
	}
	
	public void dispose()
	{
		jEdit.props.put("send.smtp.value",smtp.getText());
		jEdit.props.put("send.from.value",from.getText());
		jEdit.props.put("send.to.value",to.getText());
		jEdit.props.put("send.subject.value",subject.getText());
		super.dispose();
		if(thread != null)
			thread.stop();
	}
	
	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		if(source == cancel)
			dispose();
		else if(source == send)
			(thread = new Thread(this)).start();
	}

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
}
