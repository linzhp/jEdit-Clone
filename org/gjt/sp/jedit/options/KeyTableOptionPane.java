/*
 * KeyTableOptionPane.java - Key table options panel
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

package org.gjt.sp.jedit.options;

import org.gjt.sp.jedit.*;
import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * Key binding editor option pane.
 * @author Slava Pestov
 * @version $Id$
 */
public class KeyTableOptionPane extends OptionPane
{
	public KeyTableOptionPane()
	{
		super("keys");
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER,createKeyTableScroller());
	}

	public void save()
	{
		keyModel.save();
	}

	// private members
	private JTable keyTable;
	private KeyTableModel keyModel;

	private JScrollPane createKeyTableScroller()
	{
		keyModel = createKeyTableModel();
		keyTable = new JTable(keyModel);
		keyTable.getTableHeader().setReorderingAllowed(false);
		Dimension d = keyTable.getPreferredSize();
		d.height = Math.min(d.height,200);
		JScrollPane scroller = new JScrollPane(keyTable);
		scroller.setPreferredSize(d);
		return scroller;
	}

	private KeyTableModel createKeyTableModel()
	{
		return new KeyTableModel();
	}
}

class KeyTableModel extends AbstractTableModel
{
	private Vector bindings;

	KeyTableModel()
	{
		EditAction[] actions = jEdit.getActions();
		bindings = new Vector(actions.length);

		for(int i = 0; i < actions.length; i++)
		{
			String name = actions[i].getName();
			String label = jEdit.getProperty(name + ".label");
			if(label == null)
				continue;
			label = GUIUtilities.prettifyMenuLabel(label);
			String shortcut = jEdit.getProperty(name + ".shortcut");
			bindings.addElement(new KeyBinding(name,label,shortcut));
		}
	}

	public int getColumnCount()
	{
		return 2;
	}

	public int getRowCount()
	{
		return bindings.size();
	}

	public Object getValueAt(int row, int col)
	{
		KeyBinding binding = (KeyBinding)bindings.elementAt(row);
		switch(col)
		{
		case 0:
			return binding.label;
		case 1:
			return binding.shortcut;
		default:
			return null;
		}
	}

	public boolean isCellEditable(int row, int col)
	{
		return (col == 1);
	}

	public void setValueAt(Object value, int row, int col)
	{
		if(col != 1)
			return;
		((KeyBinding)bindings.elementAt(row)).shortcut = (String)value;
	}

	public String getColumnName(int index)
	{
		switch(index)
		{
		case 0:
			return jEdit.getProperty("options.keys.name");
		case 1:
			return jEdit.getProperty("options.keys.binding");
		default:
			return null;
		}
	}

	public void save()
	{
		for(int i = 0; i < bindings.size(); i++)
		{
			KeyBinding binding = (KeyBinding)bindings.elementAt(i);
			String name = binding.name;
			String shortcut = binding.shortcut;
			if(shortcut == null)
			{
				if(jEdit.getProperty(name + ".shortcut") != null)
					jEdit.setProperty(name + ".shortcut","");
			}
			else
			{
				if(!shortcut.equals(jEdit.getProperty(
					name + ".shortcut")))
				{
					jEdit.setProperty(name + ".shortcut",
						shortcut);
				}
			}
		}
	}

	class KeyBinding
	{
		KeyBinding(String name, String label, String shortcut)
		{
			this.name = name;
			this.label = label;
			this.shortcut = shortcut;
		}

		String name;
		String label;
		String shortcut;
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/05/03 08:28:14  sp
 * Documentation updates, key binding editor, syntax text area bug fix
 *
 */
