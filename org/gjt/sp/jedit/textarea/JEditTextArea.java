/*
 * JEditTextArea.java - jEdit's text component
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

package org.gjt.sp.jedit.textarea;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import org.gjt.sp.jedit.syntax.*;

/**
 * jEdit's text area component.
 * @author Slava Pestov
 * @version $Id$
 */
public class JEditTextArea extends Container
{
	/**
	 * Creates a new JEditTextArea with the specified default size
	 * @param cols The default number of columns
	 * @param rows The default number of rows
	 */
	public JEditTextArea(int cols, int rows)
	{
		painter = createPainter(cols,rows);
		model = createModel();

		setLayout(new ScrollLayout());
		add(CENTER,painter);
		add(RIGHT,vertical = new JScrollBar(JScrollBar.VERTICAL));
		add(BOTTOM,horizontal = new JScrollBar(JScrollBar.HORIZONTAL));

		vertical.addAdjustmentListener(new AdjustHandler());
		horizontal.addAdjustmentListener(new AdjustHandler());

		painter.addComponentListener(new ComponentHandler());
	}

	/**
	 * Returns the object responsible for painting this text area.
	 */
	public TextAreaPainter getPainter()
	{
		return painter;
	}

	/**
	 * Returns the object responsible for maintaining this text area's
	 * state.
	 */
	public TextAreaModel getModel()
	{
		return model;
	}

	/**
	 * Updates the state of the scroll bars. This should be called
	 * if the number of lines in the document changes, or when the
	 * size of the text are changes.
	 */
	public void updateScrollBars()
	{
		if(vertical != null)
		{
			vertical.setValue(firstLine);
			vertical.setVisibleAmount(visibleLines);
			vertical.setBlockIncrement(visibleLines);
			vertical.setMinimum(0);
			vertical.setMaximum(model.getLineCount());
		}

		if(horizontal != null)
		{
			horizontal.setValue(-horizontalOffset);
			horizontal.setVisibleAmount(getSize().width);
			horizontal.setUnitIncrement(10);
			horizontal.setBlockIncrement(50);
			horizontal.setMinimum(0);
			horizontal.setMaximum(longestLine);
		}
	}

	/**
	 * Returns the line displayed at the text area's origin.
	 */
	public int getFirstLine()
	{
		return firstLine;
	}

	/**
	 * Sets the line displayed at the text area's origin. This can
	 * be used to implement vertical scrolling.
	 * @param firstLine The line to display at the origin
	 */
	public void setFirstLine(int firstLine)
	{
		if(this.firstLine == firstLine)
			return;
		int oldFirstLine = this.firstLine;
		this.firstLine = firstLine;
		if(vertical.getValue() != firstLine)
			updateScrollBars();
		painter.scrollRepaint(oldFirstLine,firstLine);
	}

	/**
	 * Returns the number of lines visible in this text area.
	 */
	public int getVisibleLines()
	{
		return visibleLines;
	}

	/**
	 * Returns the horizontal offset of drawn lines.
	 */
	public int getHorizontalOffset()
	{
		return horizontalOffset;
	}

	/**
	 * Sets the horizontal offset of drawn lines. This can be used to
	 * implement horizontal scrolling.
	 * @param horizontalOffset offset The new horizontal offset
	 */
	public void setHorizontalOffset(int horizontalOffset)
	{
		if(this.horizontalOffset == horizontalOffset)
			return;
		this.horizontalOffset = horizontalOffset;
		horizontal.setValue(-horizontalOffset);
		painter.invalidateLineRange(firstLine,firstLine + visibleLines);
		painter.repaint();
	}

	/**
	 * Returns the width of the longest line (in pixels) in this text
	 * area.
	 */
	public int getLongestLine()
	{
		return longestLine;
	}

	/**
	 * Should be called after painting or inserting a line.
	 * @param lineIndex The line number
	 * @param lineWidth The width of the line
	 */
	public void checkLongestLine(int lineIndex, int lineWidth)
	{
		if(lineWidth > longestLine)
		{
			longestLineIndex = lineIndex;
			longestLine = lineWidth;
			updateScrollBars();
		}
	}

	/**
	 * Should be called after removing a line.
	 * @param lineIndex The line number
	 */
	public void removeLongestLine(int lineIndex)
	{
		if(lineIndex == longestLineIndex)
			calculateLongestLine();
	}

	/**
	 * Calculates the longest line in the document.
	 */
	public void calculateLongestLine()
	{
	}

	// protected members
	protected static String CENTER = "center";
	protected static String RIGHT = "right";
	protected static String BOTTOM = "bottom";
	
	protected TextAreaPainter painter;
	protected TextAreaModel model;

	protected int firstLine;
	protected int visibleLines;

	protected int horizontalOffset;
	protected int longestLine;
	protected int longestLineIndex;
	
	protected JScrollBar vertical;
	protected JScrollBar horizontal;

	protected TextAreaPainter createPainter(int cols, int rows)
	{
		return new TextAreaPainter(this,SyntaxUtilities
			.getDefaultSyntaxStyles(),cols,rows);
	}

	protected TextAreaModel createModel()
	{
		return new TextAreaModel(this,new DefaultSyntaxDocument());
	}

	class ScrollLayout implements LayoutManager
	{
		public void addLayoutComponent(String name, Component comp)
		{
			if(name.equals(CENTER))
				center = comp;
			else if(name.equals(RIGHT))
				right = comp;
			else if(name.equals(BOTTOM))
				bottom = comp;
		}

		public void removeLayoutComponent(Component comp)
		{
			if(center == comp)
				center = null;
			if(right == comp)
				right = null;
			if(bottom == comp)
				bottom = null;
		}

		public Dimension preferredLayoutSize(Container parent)
		{
			Dimension dim = new Dimension();
			Dimension centerPref = center.getPreferredSize();
			dim.height = centerPref.height;
			dim.width = centerPref.width;
			Dimension rightPref = right.getPreferredSize();
			dim.width += rightPref.height;
			Dimension bottomPref = bottom.getPreferredSize();
			dim.height += bottomPref.height;

			return dim;
		}

		public Dimension minimumLayoutSize(Container parent)
		{
			Dimension dim = new Dimension();
			Dimension centerPref = center.getMinimumSize();
			dim.height = centerPref.height;
			dim.width = centerPref.width;
			Dimension rightPref = center.getMinimumSize();
			dim.width += rightPref.height;
			Dimension bottomPref = center.getMinimumSize();
			dim.height += bottomPref.height;

			return dim;
		}

		public void layoutContainer(Container parent)
		{
			Dimension size = parent.getSize();
			int rightWidth = right.getPreferredSize().width;
			int bottomHeight = bottom.getPreferredSize().height;
			center.setBounds(0,0,size.width - rightWidth,
				size.height - bottomHeight);
			right.setBounds(size.width - rightWidth,0,
				rightWidth,size.height - bottomHeight);
			bottom.setBounds(0,size.height - bottomHeight,
				size.width - rightWidth,bottomHeight);
		}

		// private members
		private Component center;
		private Component right;
		private Component bottom;
	}

	class AdjustHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent evt)
		{
			if(evt.getAdjustable() == vertical)
				setFirstLine(vertical.getValue());
			else
				setHorizontalOffset(-horizontal.getValue());
		}
	}

	class ComponentHandler extends ComponentAdapter
	{
		public void componentResized(ComponentEvent evt)
		{
			int height = painter.getSize().height;
			int lineHeight = model.getLineHeight();
			if(height % lineHeight != 0)
				height += lineHeight;
			int newVisibleLines = height / lineHeight;
			painter.invalidateLineRange(firstLine + visibleLines,
				firstLine + newVisibleLines);
			visibleLines = newVisibleLines;
			updateScrollBars();
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.4  1999/06/25 06:54:08  sp
 * Text area updates
 *
 */
