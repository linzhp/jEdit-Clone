/*
 * JEditTextArea.java - jEdit's text component
 * Copyright (C) 1999 Slava Pestov
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
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
		painter.addMouseListener(new MouseHandler());
		painter.addMouseMotionListener(new MouseMotionHandler());

		addFocusListener(new FocusHandler());

		setCaretVisible(true);
		setCaretBlinks(true);
		setElectricScroll(3);
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
	 * Returns true if the caret is blinking, false otherwise.
	 */
	public boolean getCaretBlinks()
	{
		return caretBlinks;
	}

	/**
	 * Toggles caret blinking.
	 * @param caretBlinks True if the caret should blink, false otherwise
	 */
	public void setCaretBlinks(boolean caretBlinks)
	{
		this.caretBlinks = caretBlinks;
		if(caretBlinks)
		{
			if(caretTimer == null)
				caretTimer = new Timer(500,new CaretBlinker());
			if(!caretTimer.isRunning())
				caretTimer.start();
		}
		else
		{
			caretTimer.stop();
			blink = false;
		}

		painter.invalidateCurrentLine();
	}

	/**
	 * Returns true if the caret is visible, false otherwise.
	 */
	public boolean getCaretVisible()
	{
		return (!caretBlinks || blink) && caretVisible;
	}

	/**
	 * Sets if the caret should be visible.
	 * @param caretVisible True if the caret should be visible, false
	 * otherwise
	 */
	public void setCaretVisible(boolean caretVisible)
	{
		this.caretVisible = caretVisible;
		if(caretBlinks)
		{
			if(caretVisible)
				caretTimer.start();
			else
				caretTimer.stop();
		}

		blink = true;

		painter.invalidateCurrentLine();
	}

	/**
	 * Blinks the caret.
	 */
	public void blinkCaret()
	{
		blink = !blink;
		painter.invalidateCurrentLine();
	}

	/**
	 * Returns the number of lines from the top and button of the
	 * text area that are always visible.
	 */
	public int getElectricScroll()
	{
		return electricScroll;
	}

	/**
	 * Sets the number of lines from the top and bottom of the text
	 * area that are always visible
	 * @param electricScroll The number of lines always visible from
	 * the top or bottom
	 */
	public void setElectricScroll(int electricScroll)
	{
		this.electricScroll = electricScroll;
	}

	/**
	 * Updates the state of the scroll bars. This should be called
	 * if the number of lines in the document changes, or when the
	 * size of the text are changes.
	 */
	public void updateScrollBars()
	{
		if(vertical != null && visibleLines != 0)
		{
			vertical.setValue(firstLine);
			vertical.setBlockIncrement(visibleLines);
			vertical.setMinimum(0);
			vertical.setMaximum(model.getLineCount());
			vertical.setVisibleAmount(visibleLines);
		}

		int width = painter.getSize().width;
		if(horizontal != null && width != 0)
		{
			horizontal.setValue(-horizontalOffset);
			horizontal.setUnitIncrement(100);
			horizontal.setBlockIncrement(width / 2);
			horizontal.setMinimum(0);
			horizontal.setMaximum(width * 5);
			horizontal.setVisibleAmount(width);
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
	 * Sets the line displayed at the text area's origin without
	 * updating the scroll bars.
	 */
	public void setFirstLine(int firstLine)
	{
		if(this.firstLine == firstLine)
			return;
		int oldFirstLine = this.firstLine;
		this.firstLine = firstLine;
		if(firstLine != vertical.getValue())
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
	 * Ensures that the specified line and offset is visible by scrolling
	 * the text area if necessary.
	 * @param line The line to scroll to
	 * @param offset The offset in the line to scroll to
	 * @return True if scrolling was performed, false otherwise
	 */
	public boolean scrollTo(int line, int offset)
	{
		if(visibleLines == 0)
		{
			setFirstLine(line);
			return true;
		}

		if(line - electricScroll < firstLine)
		{
			setFirstLine(Math.max(0,line - electricScroll));
			return true;
		}
		else if(line + electricScroll > firstLine + visibleLines)
		{
			int newline = (line - visibleLines) + electricScroll + 1;
			if(newline < 0)
				newline = 0;
			int lines = model.getLineCount();
			if(newline + visibleLines >= lines)
				newline = lines - visibleLines;
			setFirstLine(newline);
			return true;
		}
		else
			return false;
	}

	// protected members
	protected static String CENTER = "center";
	protected static String RIGHT = "right";
	protected static String BOTTOM = "bottom";
	
	protected TextAreaPainter painter;
	protected TextAreaModel model;

	protected Timer caretTimer;
	protected boolean caretBlinks;
	protected boolean caretVisible;
	protected boolean blink;

	protected int firstLine;
	protected int visibleLines;

	protected int electricScroll;

	protected int horizontalOffset;
	
	protected JScrollBar vertical;
	protected JScrollBar horizontal;

	protected TextAreaPainter createPainter(int cols, int rows)
	{
		return new TextAreaPainter(this,cols,rows);
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
			right.setBounds(size.width - rightWidth,0,
				rightWidth,size.height - bottomHeight);
			bottom.setBounds(0,size.height - bottomHeight,
				size.width - rightWidth,bottomHeight);
			center.setBounds(0,0,size.width - rightWidth,
				size.height - bottomHeight);
		}

		// private members
		private Component center;
		private Component right;
		private Component bottom;
	}

	class CaretBlinker implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			blinkCaret();
		}
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

		public void componentShown(ComponentEvent evt)
		{
			caretTimer.start();
		}

		public void componentHidden(ComponentEvent evt)
		{
			caretTimer.stop();
		}
	}

	class FocusHandler implements FocusListener
	{
		public void focusGained(FocusEvent evt)
		{
			System.out.println("HELLO");
			setCaretVisible(true);
		}

		public void focusLost(FocusEvent evt)
		{
			System.out.println("HI!!!");
			setCaretVisible(false);
		}
	}

	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			switch(evt.getClickCount())
			{
			case 1:
				int offset = model.xyToOffset(evt.getX(),evt.getY());
				if((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0)
					model.setSelectionEnd(offset);
				else
					model.setCaretPosition(offset);
				break;
			case 2:
				break;
			case 3:
				int line = model.yToLine(evt.getY());
				int start = model.getLineStartOffset(line);
				int end = model.getLineEndOffset(line) - 1;
				model.select(start,end);
				break;
			}
		}

		public void mousePressed(MouseEvent evt)
		{
			int offset = model.xyToOffset(evt.getX(),evt.getY());
			if((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0)
				model.setSelectionEnd(offset);
			else
				model.setCaretPosition(offset);
		}
	}

	class MouseMotionHandler extends MouseMotionAdapter
	{
		public void mouseDragged(MouseEvent evt)
		{
			int line = model.yToLine(evt.getY());
			int start = model.getLineStartOffset(line);
			int offset = start + model.xToOffset(line,evt.getX());

			int mark = model.getMarkPosition();

			if(offset < mark)
			{
				model.select(offset,mark);
				model.setLeftBias(true);
			}
			else
			{
				model.select(mark,offset);
				model.setLeftBias(false);
			}
		}
	}
}

/*
 * ChangeLog:
 * $Log$
 * Revision 1.6  1999/06/28 09:17:20  sp
 * Perl mode javac compile fix, text area hacking
 *
 * Revision 1.5  1999/06/27 04:53:16  sp
 * Text selection implemented in text area, assorted bug fixes
 *
 * Revision 1.4  1999/06/25 06:54:08  sp
 * Text area updates
 *
 */
