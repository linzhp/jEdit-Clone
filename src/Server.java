/*
 * Server.java - jEdit server
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread
{
	private File portFile;
	private ServerSocket server;

	public Server(File portFile)
	{
		super("***jEdit server***");
		this.portFile = portFile;
		start();
	}

	public void run()
	{
		if(portFile.exists())
		{
			System.out.println("jEdit server already running");
			return;
		}
		try
		{
			server = new ServerSocket(0);
			int port = server.getLocalPort();
			FileWriter out = new FileWriter(portFile);
			out.write(String.valueOf(port));
			out.close();
			System.out.println("jEdit server started on port "
				+ port);
			for(;;)
			{
				Socket client = server.accept();
				InetAddress addr = client.getInetAddress();
				System.out.println("Connection from " + addr);
				doClient(client);
			}
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
	}

	private void doClient(Socket client)
	{
		View view = jEdit.buffers.newView();
		try
		{
			BufferedReader in =
				new BufferedReader(new InputStreamReader(client
					.getInputStream()));
			String filename;
			while((filename = in.readLine()) != null)
				jEdit.buffers.openBuffer(view,filename);
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
		try
		{
			client.close();
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
	}

	public void stopServer()
	{
		if(server == null)
			return;
		stop();
		try
		{
			server.close();
		}
		catch(IOException io)
		{
			io.printStackTrace();
		}
		portFile.delete();
	}
}
