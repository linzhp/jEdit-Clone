/*
 * ColorTableOptionPane.java - Color table options panel
 * Copyright (C) 1999 mike dillon
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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import org.gjt.sp.jedit.*;

/**
 * Color table option pane.
 * @author Mike Dillon
 * @version $Id$
 */
public class ColorTableOptionPane extends OptionPane
	implements ListSelectionListener, TableColumnModelListener
{
	public ColorTableOptionPane()
	{
		super("colors");
		setLayout(new BorderLayout());
		add(createColorTableScroller(),BorderLayout.CENTER);
	}

	public void save()
	{
		int i = 0;
		saveColorChoice("view.bgColor",i++);
		saveColorChoice("view.fgColor",i++);
		saveColorChoice("view.caretColor",i++);
		saveColorChoice("view.selectionColor",i++);
		saveColorChoice("view.lineHighlightColor",i++);
		saveColorChoice("view.bracketHighlightColor",i++);
		saveColorChoice("console.infoColor",i++);
		saveColorChoice("console.errorColor",i++);
		saveColorChoice("console.parsedErrorColor",i++);
		saveColorChoice("buffer.colors.comment1",i++);
		saveColorChoice("buffer.colors.comment2",i++);
		saveColorChoice("buffer.colors.literal1",i++);
		saveColorChoice("buffer.colors.literal2",i++);
		saveColorChoice("buffer.colors.label",i++);
		saveColorChoice("buffer.colors.keyword1",i++);
		saveColorChoice("buffer.colors.keyword2",i++);
		saveColorChoice("buffer.colors.keyword3",i++);
		saveColorChoice("buffer.colors.operator",i++);
		saveColorChoice("buffer.colors.invalid",i++);
	}

	// ListSelectionListener implementation
	public void valueChanged(ListSelectionEvent e)
	{
		if(e.getValueIsAdjusting())
			return;
		cellSelectionChanged();
	}
	// end ListSelectionListener implementation

	// TableColumnModelListener implementation
	public void columnAdded(TableColumnModelEvent e)
	{
		
	}

	public void columnMarginChanged(ChangeEvent e)
	{
		
	}

	public void columnMoved(TableColumnModelEvent e)
	{
		
	}

	public void columnRemoved(TableColumnModelEvent e)
	{
		
	}

	public void columnSelectionChanged(ListSelectionEvent e)
	{
		if(e.getValueIsAdjusting())
			return;
		cellSelectionChanged();
	}
	// end TableColumnModelListener implementation

	// private members
	private JTable colorTable;
	private ColorTableModel ctModel;
	private int selectedColumn = -1;
	private int selectedRow = -1;
	private static final int COLOR_SETTINGS = 15;

	private void cellSelectionChanged()
	{
		if (colorTable.getSelectedColumn() == 1)
		{
			if (!(colorTable.getSelectedColumn() == selectedColumn
				&& colorTable.getSelectedRow() == selectedRow))
			{
				selectedColumn = colorTable.getSelectedColumn();
				selectedRow = colorTable.getSelectedRow();
				if (selectedRow >= 0 && selectedColumn >=0)
					showColorSelectionDialog();
			}
		}
	}

	private ColorTableModel createColorTableModel()
	{
		Vector types, colors;
		types = new Vector(COLOR_SETTINGS);
		colors = new Vector(COLOR_SETTINGS);

		// general
		types.addElement(jEdit.getProperty("options.colors.bgColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("view.bgColor")));
		types.addElement(jEdit.getProperty("options.colors.fgColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("view.fgColor")));
		types.addElement(jEdit.getProperty("options.colors.caretColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("view.caretColor")));
		types.addElement(jEdit.getProperty("options.colors.selectionColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("view.selectionColor")));
		types.addElement(jEdit.getProperty("options.colors.lineHighlightColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("view.lineHighlightColor")));
		types.addElement(jEdit.getProperty("options.colors.bracketHighlightColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("view.bracketHighlightColor")));
		// console
		types.addElement(jEdit.getProperty("options.colors.infoColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("console.infoColor")));
		types.addElement(jEdit.getProperty("options.colors.errorColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("console.errorColor")));
		types.addElement(jEdit.getProperty("options.colors.parsedErrorColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("console.parsedErrorColor")));
		// syntax
		types.addElement(jEdit.getProperty("options.colors.comment1Color"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.comment1")));
		types.addElement(jEdit.getProperty("options.colors.comment2Color"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.comment2")));
		types.addElement(jEdit.getProperty("options.colors.literal1Color"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.literal1")));
		types.addElement(jEdit.getProperty("options.colors.literal2Color"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.literal2")));
		types.addElement(jEdit.getProperty("options.colors.labelColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.label")));
		types.addElement(jEdit.getProperty("options.colors.keyword1Color"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.keyword1")));
		types.addElement(jEdit.getProperty("options.colors.keyword2Color"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.keyword2")));
		types.addElement(jEdit.getProperty("options.colors.keyword3Color"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.keyword3")));
		types.addElement(jEdit.getProperty("options.colors.operatorColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.operator")));
		types.addElement(jEdit.getProperty("options.colors.invalidColor"));
		colors.addElement(ColorChoice.choiceForValue(
			jEdit.getProperty("buffer.colors.invalid")));

		return new ColorTableModel(types, colors);
	}

	private JScrollPane createColorTableScroller()
	{
		JScrollPane scroller;
		JViewport myViewport;
		ctModel = createColorTableModel();
		colorTable = new JTable(ctModel);
		colorTable.getTableHeader().setReorderingAllowed(false);
		colorTable.getSelectionModel().addListSelectionListener(this);

		TableColumnModel tcm = colorTable.getColumnModel();
		tcm.addColumnModelListener(this);
 		TableColumn colorColumn = tcm.getColumn(1);
		colorColumn.setCellRenderer(new ColorChoiceRenderer());

		scroller = new JScrollPane();
		myViewport = scroller.getViewport();
		myViewport.setView(colorTable);
		Dimension d = colorTable.getPreferredSize();
		d.height = (int) Math.min(d.height,200);
		myViewport.setPreferredSize(d);
		return scroller;
	}

	private void saveColorChoice(String property, int row)
	{
		ColorChoice choice = (ColorChoice) ctModel.getValueAt(row,1);
		jEdit.setProperty(property,choice.getValue());
	}

	private void showColorSelectionDialog()
	{
 		Color color = JColorChooser.showDialog(this,
			jEdit.getProperty("colorChooser.title"),
			((ColorChoice) ctModel.getValueAt(selectedRow,
			selectedColumn)).getColor());
		if (color != null)
		{
			ColorChoice cc = ColorChoice.choiceForColor(color);
			ctModel.setValueAt(cc, selectedRow, selectedColumn);
		}
	}
}

class ColorTableModel extends AbstractTableModel
{
	// Table Model implementation
	public int getRowCount()
	{
		return colorVector.size();
	}

	public int getColumnCount()
	{
		return 2;
	}

	public Class getColumnClass(int index)
	{
		if (index == 0)
		{
			return String.class;
		}
		else if (index == 1)
		{
			return ColorChoice.class;
		}
		else return null;
	}

	public String getColumnName(int index)
	{
		if (index == 0)
		{
			return jEdit.getProperty("options.colors.table.type");
		}
		else if (index == 1)
		{
			return jEdit.getProperty("options.colors.table.color");
		}
		else return new String();
	}

	public boolean isCellEditable(int row, int col)
	{
		return false;
	}

	public Object getValueAt(int row, int col)
	{
		if (col == 0)
		{
			return typeVector.elementAt(row);
		}
		else if (col == 1)
		{
			return colorVector.elementAt(row);
		}
		else return null;
	}

	public void setValueAt(Object val, int row, int col)
	{
		if (col == 1)
		{
			colorVector.removeElementAt(row);
			colorVector.insertElementAt(val, row);
			fireTableCellUpdated(row, col);
		}
	}
	// Table Model implementation

	// package-private constructor
	ColorTableModel(Vector types, Vector colors)
	{
		typeVector = types;
		colorVector = colors;
		fireTableDataChanged();
	}

	// private members
	private Vector typeVector;
	private Vector colorVector;
}

class ColorChoiceRenderer extends JLabel
	implements TableCellRenderer
{
	public ColorChoiceRenderer()
	{
		setOpaque(true);
		setBorder(noFocusBorder);
	}

	public void setColor(Color c)
	{
		setBackground(c);
	}

	// TableCellRenderer implementation
	public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean cellHasFocus,
		int row,
		int col)
	{
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		}
		else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}

		if (value != null)
		{
			ColorChoice cc = (ColorChoice) value;
			setColor(cc.getColor());
		}

		setEnabled(table.isEnabled());
		setFont(table.getFont());
		setBorder((cellHasFocus) ? UIManager.getBorder(
			"Table.focusCellHighlightBorder") : noFocusBorder);
		return this;
	}
	// end TableCellRenderer implementation

	public static class UIResource extends ColorChoiceRenderer
		implements javax.swing.plaf.UIResource
	{
	}

	// private members
	private static final EmptyBorder noFocusBorder = new EmptyBorder(1,1,1,1);
}

class ColorChoice
{
	public ColorChoice(String t)
	{
		this(t,null);
	}

	public ColorChoice(String t,String v)
	{
		this(t,v,true);
	}

	public ColorChoice(String t, String v, boolean c)
	{
		setTitle(t);
		setValue(v);
		custom = c;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String t)
	{
		title = t;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String v)
	{
		col = GUIUtilities.parseColor(v);
		if (col == Color.black && !("black".equals(v) || "#000000".equalsIgnoreCase(v)))
		{
			value = "#000000";
		}
		else
		{
			value = v;
		}
	}

	public Color getColor()
	{
		return col;
	}

	public boolean isCustom()
	{
		return custom;
	}

	public static ColorChoice choiceForValue(String v)
	{
		ColorChoice c;

		if ("red".equalsIgnoreCase(v))
			c = red;
		else if ("green".equalsIgnoreCase(v))
			c = green;
		else if ("blue".equalsIgnoreCase(v))
			c = blue;
		else if ("yellow".equalsIgnoreCase(v))
			c = yellow;
		else if ("orange".equalsIgnoreCase(v))
			c = orange;
		else if ("white".equalsIgnoreCase(v))
			c = white;
		else if ("lightGray".equalsIgnoreCase(v))
			c = lightGray;
		else if ("gray".equalsIgnoreCase(v))
			c = gray;
		else if ("darkGray".equalsIgnoreCase(v))
			c = darkGray;
		else if ("black".equalsIgnoreCase(v))
			c = black;
		else if ("cyan".equalsIgnoreCase(v))
			c = cyan;
		else if ("magenta".equalsIgnoreCase(v))
			c = magenta;
		else if ("pink".equalsIgnoreCase(v))
			c = pink;
		else
		{
			c = new ColorChoice("Custom",v);
		}
		return c;
	}

	public static ColorChoice choiceForColor(Color c)
	{
		ColorChoice cc;

		if (Color.red.equals(c))
			cc = red;
		else if (Color.green.equals(c))
			cc = green;
		else if (Color.blue.equals(c))
			cc = blue;
		else if (Color.yellow.equals(c))
			cc = yellow;
		else if (Color.orange.equals(c))
			cc = orange;
		else if (Color.white.equals(c))
			cc = white;
		else if (Color.lightGray.equals(c))
			cc = lightGray;
		else if (Color.gray.equals(c))
			cc = gray;
		else if (Color.darkGray.equals(c))
			cc = darkGray;
		else if (Color.black.equals(c))
			cc = black;
		else if (Color.cyan.equals(c))
			cc = cyan;
		else if (Color.magenta.equals(c))
			cc = magenta;
		else if (Color.pink.equals(c))
			cc = pink;
		else
		{
			cc = new ColorChoice("Custom",GUIUtilities.getColorHexString(c));
		}
		return cc;
	}

	public String toString()
	{
		return getTitle() + new String(isCustom() ? ": " + getValue() : "");
	}

	// public constants
	public static ColorChoice red = new ColorChoice("Red","red",false);
	public static ColorChoice green = new ColorChoice("Green","green",false);
	public static ColorChoice blue = new ColorChoice("Blue","blue",false);
	public static ColorChoice yellow = new ColorChoice("Yellow","yellow",false);
	public static ColorChoice orange = new ColorChoice("Orange","orange",false);
	public static ColorChoice white = new ColorChoice("White","white",false);
	public static ColorChoice lightGray = new ColorChoice("Light Gray","lightGray",false);
	public static ColorChoice gray = new ColorChoice("Gray","gray",false);
	public static ColorChoice darkGray = new ColorChoice("Dark Gray","darkGray",false);
	public static ColorChoice black = new ColorChoice("Black","black",false);
	public static ColorChoice cyan = new ColorChoice("Cyan","cyan",false);
	public static ColorChoice magenta = new ColorChoice("Magenta","magenta",false);
	public static ColorChoice pink = new ColorChoice("Pink","pink",false);

	// private members
	private String title;
	private String value;
	private Color col;
	private boolean custom;
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.9  1999/04/25 03:39:37  sp
 * Documentation updates, console updates, history text field updates
 *
 * Revision 1.8  1999/04/22 06:03:26  sp
 * Syntax colorizing change
 *
 * Revision 1.7  1999/04/21 07:39:19  sp
 * FAQ added, plugins can now add panels to the options dialog
 *
 * Revision 1.6  1999/04/01 04:13:00  sp
 * Bug fixing for 1.5final
 *
 * Revision 1.5  1999/03/20 06:24:34  sp
 * Colors option pane commited
 *
 */
