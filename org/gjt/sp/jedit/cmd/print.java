/*
 * print.java - Command
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

package org.gjt.sp.jedit.cmd;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.util.Hashtable;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.syntax.*;

public class print implements Command
{
	public void exec(Buffer buffer, View view, String arg, Hashtable args)
	{
		// Sometime in the future, this will be rewritten to do
		// syntax colorizing and word wrap . . .
		PrintJob job = view.getToolkit().getPrintJob(view,buffer
			.getName(),null);
		if(job == null)
			return;
		int topMargin;
		int leftMargin;
		int bottomMargin;
		int rightMargin;
		int ppi = job.getPageResolution();
		try
		{
			topMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("buffer.margin.top")).floatValue()
				* ppi);
		}
		catch(NumberFormatException nf)
		{
			topMargin = ppi / 2;
		}
		try
		{
			leftMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("buffer.margin.left"))
				.floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			leftMargin = ppi / 2;
		}
		try
		{
			bottomMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("buffer.margin.bottom"))
				.floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			bottomMargin = topMargin;
		}
		try
		{
			rightMargin = (int)(Float.valueOf(jEdit.props
				.getProperty("buffer.margin.right"))
				.floatValue() * ppi);
		}
		catch(NumberFormatException nf)
		{
			rightMargin = leftMargin;
		}
		String header = view.getTitle();
		Element map = buffer.getDefaultRootElement();
		SyntaxView syntaxView = new SyntaxView(map);
		SyntaxTextArea textArea = view.getTextArea();
		Graphics gfx = null;
		Font font = textArea.getFont();
		int fontHeight = font.getSize();
		int tabSize = buffer.getTabSize();
		Dimension pageDimension = job.getPageDimension();
		int pageWidth = pageDimension.width;
		int pageHeight = pageDimension.height;
		int y = 0;
		for(int i = 0; i < map.getElementCount(); i++)
		{
			if(gfx == null)
			{
				gfx = job.getGraphics();
				gfx.setFont(font);
				FontMetrics fm = gfx.getFontMetrics();
				gfx.setColor(Color.lightGray);
				gfx.fillRect(leftMargin,topMargin,pageWidth
					- leftMargin - rightMargin,
					  fm.getMaxAscent()
					  + fm.getMaxDescent()
					  + fm.getLeading());
				gfx.setColor(Color.black);
				y = topMargin + fontHeight;
				gfx.drawString(header,leftMargin,y);
				y += fontHeight;
			}
			syntaxView.drawLine(i,gfx,leftMargin,y += fontHeight);
			if((y > pageHeight - bottomMargin) ||
				(i == map.getElementCount() - 1))
			{
				gfx.dispose();
				gfx = null;
			}
		}
		job.end();
	}
}
