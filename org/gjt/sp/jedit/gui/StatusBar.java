/*
 * StatusBar.java - The status bar displayed at the bottom of views
 * Copyright (C) 2001 Slava Pestov
 * Portions copyright (C) 2001 mike dillon
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

import javax.swing.border.*;
import javax.swing.text.Segment;
import javax.swing.*;
import java.awt.*;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.*;

/**
 * The status bar, used for the following:
 * <ul>
 * <li>Displaying caret position information
 * <li>Displaying readNextChar() prompts
 * <li>Displaying the 'macro recording' message
 * <li>Displaying the status of the overwrite, multi select flags
 * <li>I/O progress
 * <li>And so on
 * </ul>
 *
 * @version $Id$
 * @author Slava Pestov
 * @since jEdit 3.2pre2
 */
public class StatusBar extends JPanel
{
	public StatusBar(View view)
	{
		super(new BorderLayout(3,3));
		setBorder(new EmptyBorder(3,0,0,0));

		this.view = view;

		Border border = new BevelBorder(BevelBorder.LOWERED);

		caretStatus = new VICaretStatus();
		caretStatus.setFont(UIManager.getFont("Label.font"));
		caretStatus.setBorder(border);
		add(BorderLayout.WEST,caretStatus);

		message = new JLabel();
		message.setForeground(UIManager.getColor("Button.foreground"));
		message.setBorder(border);
		add(BorderLayout.CENTER,message);

		Box box = new Box(BoxLayout.X_AXIS);
		mode = new JLabel();
		mode.setForeground(UIManager.getColor("Button.foreground"));
		mode.setBorder(border);

		// make the mode label as wide as the widest edit mode name,
		// so that the status bar does not resize while switching buffers
		/* Mode[] modes = jEdit.getModes();
		FontMetrics fm = getToolkit().getFontMetrics(mode.getFont());
		int width = 0;
		for(int i = 0; i < modes.length; i++)
		{
			width = Math.max(width,fm.stringWidth(modes[i].getName()));
		}
		mode.setPreferredSize(new Dimension(width,
			mode.getPreferredSize().height)); */

		box.add(mode);
		box.add(Box.createHorizontalStrut(3));
		multiSelect = new JLabel("multi");
		multiSelect.setBorder(border);
		box.add(multiSelect);
		box.add(Box.createHorizontalStrut(3));
		overwrite = new JLabel("over");
		overwrite.setBorder(border);
		box.add(overwrite);
		box.add(Box.createHorizontalStrut(3));
		narrow = new JLabel("narrow");
		narrow.setBorder(border);
		box.add(narrow);

		updateMode();
		updateMiscStatus();

		box.add(Box.createHorizontalStrut(3));
		ioProgress = new MiniIOProgress();
		ioProgress.setBorder(border);
		box.add(ioProgress);


		// UI hack because BoxLayout does not give all components the
		// same height
		Dimension dim = multiSelect.getPreferredSize();
		dim.width = 40;
		// dim.height = <same as all other components>
		ioProgress.setPreferredSize(dim);

		add(BorderLayout.EAST,box);
	}

	public void setMessage(String message)
	{
		if(message == null)
		{
			InputHandler inputHandler = view.getInputHandler();
			if(inputHandler.isRepeatEnabled())
			{
				int repeatCount = inputHandler.getRepeatCount();

				this.message.setText(jEdit.getProperty("view.status.repeat",
					new Object[] { repeatCount == 1 ? "" : String.valueOf(repeatCount) }));
			}
			else if(view.getMacroRecorder() != null)
				this.message.setText(jEdit.getProperty("view.status.recording"));
			else
				this.message.setText(null);
		}
		else
			this.message.setText(message);
	}

	public void repaintCaretStatus()
	{
		caretStatus.repaint();
	}

	public void updateMode()
	{
		mode.setText(view.getBuffer().getMode().getName());
	}

	public void updateMiscStatus()
	{
		JEditTextArea textArea = view.getTextArea();

		if(textArea.isMultipleSelectionEnabled())
			multiSelect.setForeground(Color.black);
		else
		{
			if(textArea.getSelectionCount() > 1)
			{
				multiSelect.setForeground(UIManager.getColor(
					"Label.foreground"));
			}
			else
				multiSelect.setForeground(gray);
		}

		if(textArea.isOverwriteEnabled())
			overwrite.setForeground(Color.black);
		else
			overwrite.setForeground(gray);

		narrow.setForeground(gray);
	}

	// private members
	private View view;
	private VICaretStatus caretStatus;
	private JLabel message;
	private JLabel mode;
	private JLabel multiSelect;
	private JLabel overwrite;
	private JLabel narrow;
	private MiniIOProgress ioProgress;
	private Color gray = new Color(142,142,142);
	/* package-private for speed */ StringBuffer buf = new StringBuffer();

	public class VICaretStatus extends JComponent
	{
		public VICaretStatus()
		{
			VICaretStatus.this.setForeground(UIManager.getColor("Button.foreground"));
			VICaretStatus.this.setBackground(UIManager.getColor("Label.background"));
		}

		public void paintComponent(Graphics g)
		{
			Buffer buffer = view.getBuffer();

			if(!buffer.isLoaded())
				return;

			FontMetrics fm = g.getFontMetrics();

			JEditTextArea textArea = view.getTextArea();

			int currLine = textArea.getCaretLine();
			int dot = textArea.getCaretPosition()
				- textArea.getLineStartOffset(currLine);
			int virtualPosition = getVirtualPosition(dot,buffer,textArea);

			buf.setLength(0);
			buf.append(Integer.toString(currLine + 1));
			buf.append(',');
			buf.append(Integer.toString(dot + 1));

			if (virtualPosition != dot)
			{
				buf.append('-');
				buf.append(Integer.toString(virtualPosition + 1));
			}

			buf.append(' ');

			int firstLine = textArea.getFirstLine();
			int visible = textArea.getVisibleLines();
			int lineCount = textArea.getVirtualLineCount();

			if (visible >= lineCount)
			{
				buf.append("All");
			}
			else if (firstLine == 0)
			{
				buf.append("Top");
			}
			else if (firstLine + visible >= lineCount)
			{
				buf.append("Bot");
			}
			else
			{
				float percent = (float)firstLine / (float)lineCount
					* 100.0f;
				buf.append(Integer.toString((int)percent));
				buf.append('%');
			}

			g.drawString(buf.toString(),
				VICaretStatus.this.getBorder().getBorderInsets(this).left + 1,
				(VICaretStatus.this.getHeight() + fm.getAscent()) / 2 - 1);
		}

		public Dimension getPreferredSize()
		{
			FontMetrics fm = VICaretStatus.this.getToolkit()
				.getFontMetrics(VICaretStatus.this.getFont());
			size = new Dimension(fm.stringWidth(testStr) + 6,
				fm.getHeight());

			return size;
		}

		// private members
		private static final String testStr = "9999,999-999 99%";

		private Dimension size;
		private Segment seg = new Segment();

		private int getVirtualPosition(int dot, Buffer buffer, JEditTextArea textArea)
		{
			int line = textArea.getCaretLine();

			textArea.getLineText(line, seg);

			int virtualPosition = 0;
			int tabSize = buffer.getTabSize();

			for (int i = 0; i < seg.count && i < dot; ++i)
			{
				char ch = seg.array[seg.offset + i];

				if (ch == '\t')
				{
					virtualPosition += tabSize
						- (virtualPosition % tabSize);
				}
				else
				{
					++virtualPosition;
				}
			}

			return virtualPosition;
		}
	}

	class MiniIOProgress extends JComponent
		implements WorkThreadProgressListener
	{
		public MiniIOProgress()
		{
			MiniIOProgress.this.setDoubleBuffered(true);
			MiniIOProgress.this.setForeground(UIManager.getColor("Button.foreground"));
			MiniIOProgress.this.setBackground(UIManager.getColor("Button.background"));

			icon = GUIUtilities.loadIcon("io.gif");
		}

		public void addNotify()
		{
			super.addNotify();
			VFSManager.getIOThreadPool().addProgressListener(this);
		}

		public void removeNotify()
		{
			super.removeNotify();
			VFSManager.getIOThreadPool().removeProgressListener(this);
		}

		public void progressUpdate(WorkThreadPool threadPool, int threadIndex)
		{
			MiniIOProgress.this.repaint();
		}

		public void paintComponent(Graphics g)
		{
			WorkThreadPool ioThreadPool = VFSManager.getIOThreadPool();
			if(ioThreadPool.getThreadCount() == 0)
				return;

			FontMetrics fm = g.getFontMetrics();

			if(ioThreadPool.getRequestCount() == 0)
				return;
			else
			{
				icon.paintIcon(this,g,MiniIOProgress.this.getWidth()
					- icon.getIconWidth() - 3,
					(MiniIOProgress.this.getHeight()
					- icon.getIconHeight()) / 2);
			}

			Insets insets = MiniIOProgress.this.getBorder().getBorderInsets(this);

			int progressHeight = (MiniIOProgress.this.getHeight() - insets.top - insets.bottom)
				/ ioThreadPool.getThreadCount();
			int progressWidth = MiniIOProgress.this.getWidth()
				- icon.getIconWidth() - insets.left - insets.right - 2;

			for(int i = 0; i < ioThreadPool.getThreadCount(); i++)
			{
				WorkThread thread = ioThreadPool.getThread(i);
				int max = thread.getProgressMaximum();
				if(!thread.isRequestRunning() || max == 0)
					continue;

				int value = thread.getProgressValue();
				double progressRatio = ((double)value / max);

				// when loading gzip files, for example,
				// progressValue (data read) can be larger
				// than progressMaximum (file size)
				progressRatio = Math.min(progressRatio,1.0);

				g.fillRect(insets.left,insets.top + i * progressHeight,
					(int)(progressRatio * progressWidth),progressHeight);
			}
		}

		public Dimension getPreferredSize()
		{
			return new Dimension(40,icon.getIconHeight());
		}

		// private members
		private Icon icon;
	}
}
