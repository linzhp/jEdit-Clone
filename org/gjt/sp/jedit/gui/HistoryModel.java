/*
 * HistoryModel.java - History list model
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
import java.io.*;
import java.util.*;
import org.gjt.sp.jedit.jEdit;

public class HistoryModel extends DefaultComboBoxModel
{
	public HistoryModel(String name)
	{
		this.name = name;

		try
		{
			max = Integer.parseInt(jEdit.getProperty("history"));
		}
		catch(NumberFormatException nf)
		{
			max = 25;
		}
	}

	public void addItem(String text)
	{
		if(text == null || text.length() == 0)
			text = "";

		int index = getIndexOf(text);
		if(index != -1)
			removeElementAt(index);

		insertElementAt(text,0);

		if(getSize() > max)
			removeElementAt(getSize() - 1);
	}

	public String getName()
	{
		return name;
	}

	public static HistoryModel getModel(String name)
	{
		if(models == null)
			models = new Hashtable();

		HistoryModel model = (HistoryModel)models.get(name);
		if(model == null)
		{
			model = new HistoryModel(name);
			models.put(name,model);
		}

		return model;
	}

	public static void loadHistory(String path)
	{
		if(models == null)
			models = new Hashtable();

		try
		{
			BufferedReader in = new BufferedReader(new FileReader(path));
	
			HistoryModel currentModel = null;
			String line;
	
			while((line = in.readLine()) != null)
			{
				if(line.startsWith("[") && line.endsWith("]"))
				{
					if(currentModel != null)
					{
						models.put(currentModel.getName(),
							currentModel);
					}
					currentModel = new HistoryModel(line
						.substring(1,line.length() - 1));
				}
				else if(currentModel == null)
				{
					throw new IOException("History data starts"
						+ " before model name");
				}
				else
				{
					currentModel.addElement(
						escapesToChars(line));
				}
			}
	
			if(currentModel != null)
			{
				models.put(currentModel.getName(),currentModel);
			}

			in.close();
		}
		catch(FileNotFoundException fnf)
		{
		}
		catch(IOException io)
		{
			System.err.println("Error loading history file:");
			io.printStackTrace();
		}
	}

	public static void saveHistory(String path)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(
				new FileWriter(path));

			if(models == null)
			{
				out.close();
				return;
			}

			Enumeration modelEnum = models.elements();
			while(modelEnum.hasMoreElements())
			{
				HistoryModel model = (HistoryModel)modelEnum
					.nextElement();

				out.write('[');
				out.write(model.getName());
				out.write("\n");

				for(int i = 0; i < model.getSize(); i++)
				{
					out.write(charsToEscapes((String)model
						.getElementAt(i)));
					out.write('\n');
				}
			}

			out.close();
		}
		catch(IOException io)
		{
			System.err.println("Error saving history file:");
			io.printStackTrace();
		}
	}

	// private members
	private String name;
	private int max;
	private static Hashtable models;

	private static String escapesToChars(String str)
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			switch(c)
			{
			case '\\':
				if(i == str.length() - 1)
				{
					buf.append('\\');
					break;
				}
				c = str.charAt(++i);
				switch(c)
				{
				case 'n':
					buf.append('\n');
					break;
				case 'r':
					buf.append('\r');
					break;
				case 't':
					buf.append('\t');
					break;
				default:
					buf.append(c);
					break;
				}
				break;
			default:
				buf.append(c);
			}
		}
		return buf.toString();
	}

	private static String charsToEscapes(String str)
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			switch(c)
			{
			case '\n':
				buf.append("\\n");
				break;
			case '\r':
				buf.append("\\r");
				break;
			case '\t':
				buf.append("\\t");
				break;
			case '[':
				buf.append("\\[");
				break;
			case ']':
				buf.append("\\]");
				break;
			default:
				buf.append(c);
				break;
			}
		}
		return buf.toString();
	}
}
