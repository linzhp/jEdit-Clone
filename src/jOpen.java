/*
 * jOpen.java - jEdit client
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;

public class jOpen
{
	public static final String VERSION = "1.0";
	public static final String BUILD = "19980929";
	
	public static void usage()
	{
		System.err.println("Usage: jopen [<options>] [<files>]");
		System.err.println("Valid options:");
		System.err.println("    --: End of options");
		System.err.println("    -version: Print jOpen version and"
			+ " exit");
		System.err.println("    -usage: Print this message and exit");
		System.err.println("    -server=<server>: Server to connect"
			+ " to");
		System.err.println("    -portfile=<file>: File with server"
			+ " port");
		System.exit(1);
	}

	public static void version()
	{
		System.err.println("jOpen " + VERSION + " build " + BUILD);
		System.exit(1);
	}

	public static void main(String[] args)
	{
		String server = "localhost";
		String portFilename = System.getProperty("user.home") +
			File.separator + ".jedit-port";
		boolean endOpts = false;
		for(int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			if(arg.startsWith("-") && !endOpts)
			{
				if(arg.equals("--"))
					endOpts = true;
				else if(arg.equals("-usage"))
					usage();
				else if(arg.equals("-version"))
					version();
				else if(arg.startsWith("-server="))
					server = arg.substring(8);
				else if(arg.startsWith("-portfile="))
					portFilename = arg.substring(10);
				else
				{
					System.err.println("Unknown option: "
						+ arg);
					usage();
				}

				args[i] = null;
			}
		}
		File portFile = new File(portFilename);
		if(!portFile.exists())
		{
			System.out.println("jEdit server not running");
			return;
		}
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(
				portFile));
			int port = Integer.parseInt(in.readLine());
			in.close();
			System.out.println("Connecting to " + server
				+ " on port " + port);
			Socket socket = new Socket(server,port);
			DataOutputStream out = new DataOutputStream(socket
				.getOutputStream());
			for(int i = 0; i < args.length; i++)
			{
				if(args[i] != null)
				{
					out.writeBytes(args[i]);
					out.write('\n');
				}
			}
			out.close();
			socket.close();
		}
		catch(IOException io)
		{
			io.printStackTrace();
			System.exit(1);
		}
	}
}
