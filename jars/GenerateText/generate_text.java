/*
 * generate_text.java
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

import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.util.*;
import org.gjt.sp.jedit.*;

public class generate_text extends EditAction
{
	public generate_text()
	{
		super("generate-text");
	}

	public void actionPerformed(ActionEvent evt)
	{
		/* Settings are hard coded right now - ideally, we would
		 * have a nice GUI settings dialog, but this particular
		 * command isn't high on my list right now :) */
		Buffer buffer = jEdit.newFile(getView(evt));
		StringBuffer buf = new StringBuffer();

		int title = Math.abs(random.nextInt()) % 5;
		while(title-- >= 0)
		{
			generateWord(buf,false,true);
			buf.append(' ');
		}
		buf.append("\n\n");

		for(int i = 0; i < 10; i++)
		{
			generateParagraph(buf);
			buf.append("\n\n");
		}

		try
		{
			buffer.insertString(0,buf.toString(),null);
		}
		catch(BadLocationException bl)
		{
		}
		
		/* Pretty print hack */
		jEdit.getAction("format").actionPerformed(evt);
	}

	// private members	
	private Random random = new Random();

	private void generateParagraph(StringBuffer buf)
	{
		int sentences = (Math.abs(random.nextInt()) % 5) + 1;
		while(sentences-- >= 0)
		{
			generateSentence(buf);
			String punct = "....!?";
			buf.append(punct.charAt((Math.abs(random.nextInt())
				% punct.length())));
			buf.append(' ');
		}
	}

	private void generateSentence(StringBuffer buf)
	{
		int words = (Math.abs(random.nextInt()) % 15) + 1;

		for(int i = 0; i < words; i++)
		{
			
			if(Math.abs(random.nextInt()) % 15 == 0)
				generateNumber(buf);
			else
			{
				generateWord(buf,i == 0,Math.abs(random
					.nextInt()) % 15 == 0);
			}
			if(i != words - 1)
			{
				if(i != 0)
				{
					int comma = Math.abs(random.nextInt()) % 10;
					if(comma == 0)
						buf.append(" -");
					else if(comma == 1)
						buf.append(";");
					else if(comma == 2 || comma == 3)
						buf.append(",");
				}
				buf.append(' ');
			}
		}
	}

	private void generateNumber(StringBuffer buf)
	{
		int chars = (Math.abs(random.nextInt()) % 3) + 1;
		if(Math.abs(random.nextInt()) % 15 == 0)
			buf.append('-');
		while(chars-- >= 0)
			buf.append(Math.abs(random.nextInt()) % 10);
	}

	private void generateWord(StringBuffer buf, boolean tcaps, boolean acaps)
	{
		String cons = "bbccddffgghhjjkkllmmnnppqrrssttvvwx";
		String vowels = "aaeeiioouuy";
		int chars = (Math.abs(random.nextInt()) % 10) + 1;
		int consCount = 0;
		for(int i = 0; i < chars; i++)
		{
			char c;
			if(consCount < 3 && Math.abs(random.nextInt()) % 3 != 0)
			{
				consCount++;
				c = cons.charAt(Math.abs(random.nextInt())
					% cons.length());
				
			}
			else
			{
				consCount = 0;
				c = vowels.charAt(Math.abs(random.nextInt())
					% vowels.length());
			}
			if((i == 0 && tcaps) || acaps)
				c = Character.toUpperCase(c);
			buf.append(c);
		}
	}
}
