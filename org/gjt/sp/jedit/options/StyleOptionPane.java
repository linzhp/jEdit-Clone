/*
 * StyleOptionPane.java - Color/style option pane
 * Copyright (C) 1999 Slava Pestov
 * Portions copyright (C) 1999 mike dillon
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

import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import org.gjt.sp.jedit.syntax.SyntaxStyle;
import org.gjt.sp.jedit.*;

/**
 * Style/color option pane.
 * @author Slava Pestov
 * @version $Id$
 */
public class StyleOptionPane extends OptionPane
{
	public static final EmptyBorder noFocusBorder = new EmptyBorder(1,1,1,1);

	public StyleOptionPane()
	{
		super("styles");

		setLayout(new GridLayout(2,1));

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.NORTH,new JLabel(jEdit.getProperty(
			"options.styles.colors")));
		panel.add(BorderLayout.CENTER,createColorTableScroller());
		add(panel);

		setLayout(new GridLayout(2,1));

		panel = new JPanel(new BorderLayout());
		panel.add(BorderLayout.NORTH,new JLabel(jEdit.getProperty(
			"options.styles.styles")));
		panel.add(BorderLayout.CENTER,createStyleTableScroller());
		add(panel);
	}

	public void save()
	{
	}

	// private members
	private ColorTableModel colorModel;
	private JTable colorTable;
	private StyleTableModel styleModel;
	private JTable styleTable;

	private JScrollPane createColorTableScroller()
	{
		colorModel = createColorTableModel();
		colorTable = new JTable(colorModel);
		colorTable.getTableHeader().setReorderingAllowed(false);
		TableColumnModel tcm = colorTable.getColumnModel();
 		TableColumn colorColumn = tcm.getColumn(1);
		colorColumn.setCellRenderer(new ColorTableModel.ColorRenderer());
		Dimension d = colorTable.getPreferredSize();
		d.height = Math.min(d.height,100);
		JScrollPane scroller = new JScrollPane(colorTable);
		scroller.setPreferredSize(d);
		return scroller;
	}

	private ColorTableModel createColorTableModel()
	{
		return new ColorTableModel();
	}

	private JScrollPane createStyleTableScroller()
	{
		styleModel = createStyleTableModel();
		styleTable = new JTable(styleModel);
		styleTable.getTableHeader().setReorderingAllowed(false);
		TableColumnModel tcm = styleTable.getColumnModel();
 		TableColumn styleColumn = tcm.getColumn(1);
		styleColumn.setCellRenderer(new StyleTableModel.StyleRenderer());
		Dimension d = styleTable.getPreferredSize();
		d.height = Math.min(d.height,100);
		JScrollPane scroller = new JScrollPane(styleTable);
		scroller.setPreferredSize(d);
		return scroller;
	}

	private StyleTableModel createStyleTableModel()
	{
		return new StyleTableModel();
	}
}

class ColorTableModel extends AbstractTableModel
{
	private Vector colorChoices;

	ColorTableModel()
	{
		colorChoices = new Vector(6);
		addColorChoice("options.styles.bgColor","view.bgColor");
		addColorChoice("options.styles.fgColor","view.fgColor");
		addColorChoice("options.styles.caretColor","view.caretColor");
		addColorChoice("options.styles.selectionColor",
			"view.selectionColor");
		addColorChoice("options.styles.lineHighlightColor",
			"view.lineHighlightColor");
		addColorChoice("options.styles.bracketHighlightColor",
			"view.bracketHighlightColor");
	}

	public int getColumnCount()
	{
		return 2;
	}

	public int getRowCount()
	{
		return colorChoices.size();
	}

	public Object getValueAt(int row, int col)
	{
		ColorChoice ch = (ColorChoice)colorChoices.elementAt(row);
		switch(col)
		{
		case 0:
			return ch.label;
		case 1:
			return ch.color;
		default:
			return null;
		}
	}

	public String getColumnName(int index)
	{
		switch(index)
		{
		case 0:
			return jEdit.getProperty("options.styles.object");
		case 1:
			return jEdit.getProperty("options.styles.color");
		default:
			return null;
		}
	}

	private void addColorChoice(String label, String property)
	{
		colorChoices.addElement(new ColorChoice(jEdit.getProperty(label),
			property,GUIUtilities.parseColor(jEdit.getProperty(property))));
	}

	static class ColorChoice
	{
		String label;
		String property;
		Color color;

		ColorChoice(String label, String property, Color color)
		{
			this.label = label;
			this.property = property;
			this.color = color;
		}
	}

	static class ColorRenderer extends JLabel
		implements TableCellRenderer
	{
		public ColorRenderer()
		{
			setOpaque(true);
			setBorder(StyleOptionPane.noFocusBorder);
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
			if (isSelected)
			{
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			}
			else
			{
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}
	
			if (value != null)
				setBackground((Color)value);
	
			setBorder((cellHasFocus) ? UIManager.getBorder(
				"Table.focusCellHighlightBorder")
				: StyleOptionPane.noFocusBorder);
			return this;
		}
		// end TableCellRenderer implementation
	}
}

class StyleTableModel extends AbstractTableModel
{
	private Vector styleChoices;

	StyleTableModel()
	{
		styleChoices = new Vector(10);
		addStyleChoice("options.styles.comment1Style","buffer.style.comment1");
		addStyleChoice("options.styles.comment2Style","buffer.style.comment2");
		addStyleChoice("options.styles.literal1Style","buffer.style.literal1");
		addStyleChoice("options.styles.literal2Style","buffer.style.literal2");
		addStyleChoice("options.styles.labelStyle","buffer.style.label");
		addStyleChoice("options.styles.keyword1Style","buffer.style.keyword1");
		addStyleChoice("options.styles.keyword2Style","buffer.style.keyword2");
		addStyleChoice("options.styles.keyword3Style","buffer.style.keyword3");
		addStyleChoice("options.styles.operatorStyle","buffer.style.operator");
		addStyleChoice("options.styles.invalidStyle","buffer.style.invalid");
	}

	public int getColumnCount()
	{
		return 2;
	}

	public int getRowCount()
	{
		return styleChoices.size();
	}

	public Object getValueAt(int row, int col)
	{
		StyleChoice ch = (StyleChoice)styleChoices.elementAt(row);
		switch(col)
		{
		case 0:
			return ch.label;
		case 1:
			return ch.style;
		default:
			return null;
		}
	}

	public String getColumnName(int index)
	{
		switch(index)
		{
		case 0:
			return jEdit.getProperty("options.styles.object");
		case 1:
			return jEdit.getProperty("options.styles.style");
		default:
			return null;
		}
	}

	private void addStyleChoice(String label, String property)
	{
		styleChoices.addElement(new StyleChoice(jEdit.getProperty(label),
			property,
			GUIUtilities.parseStyle(jEdit.getProperty(property))));
	}

	static class StyleChoice
	{
		String label;
		String property;
		SyntaxStyle style;

		StyleChoice(String label, String property, SyntaxStyle style)
		{
			this.label = label;
			this.property = property;
			this.style = style;
		}
	}

	static class StyleRenderer extends JLabel
		implements TableCellRenderer
	{
		public StyleRenderer()
		{
			setOpaque(true);
			setBorder(StyleOptionPane.noFocusBorder);
			setText("Hello World");
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
			if (isSelected)
			{
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			}
			else
			{
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}
	
			if (value != null)
			{
				SyntaxStyle style = (SyntaxStyle)value;
				setForeground(style.getColor());
				setFont(style.getStyledFont(getFont()));
			}
	
			setBorder((cellHasFocus) ? UIManager.getBorder(
				"Table.focusCellHighlightBorder")
				: StyleOptionPane.noFocusBorder);
			return this;
		}
		// end TableCellRenderer implementation
	}
}

/**
 * ChangeLog:
 * $Log$
 * Revision 1.1  1999/06/12 02:30:27  sp
 * Find next can now perform multifile searches, multifile-search command added,
 * new style option pane
 *
 */
