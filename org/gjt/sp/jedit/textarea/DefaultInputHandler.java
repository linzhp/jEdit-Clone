/*
 * DefaultInputHandler.java - Default implementation of an input handler
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

import javax.swing.text.*;
import javax.swing.KeyStroke;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Handles key events and executes actions bound to keystrokes.
 * @author Slava Pestov
 * @version $Id$
 */
public class DefaultInputHandler implements InputHandler
{
	public static final ActionListener BACKSPACE = new backspace();
	public static final ActionListener DELETE = new delete();
	public static final ActionListener END = new end(false);
	public static final ActionListener SELECT_END = new end(true);
	public static final ActionListener INSERT_BREAK = new insert_break();
	public static final ActionListener INSERT_TAB = new insert_tab();
	public static final ActionListener HOME = new home(false);
	public static final ActionListener SELECT_HOME = new home(true);
	public static final ActionListener NEXT_CHAR = new next_char(false);
	public static final ActionListener NEXT_LINE = new next_line(false);
	public static final ActionListener NEXT_PAGE = new next_page();
	public static final ActionListener NEXT_WORD = new next_word(false);
	public static final ActionListener SELECT_NEXT_CHAR = new next_char(true);
	public static final ActionListener SELECT_NEXT_LINE = new next_line(true);
	public static final ActionListener SELECT_NEXT_WORD = new next_word(true);
	public static final ActionListener OVERWRITE = new overwrite();
	public static final ActionListener PREV_CHAR = new prev_char(false);
	public static final ActionListener PREV_LINE = new prev_line(false);
	public static final ActionListener PREV_PAGE = new prev_page();
	public static final ActionListener PREV_WORD = new prev_word(false);
	public static final ActionListener SELECT_PREV_CHAR = new prev_char(true);
	public static final ActionListener SELECT_PREV_LINE = new prev_line(true);
	public static final ActionListener SELECT_PREV_WORD = new prev_word(true);

	public static final ActionListener[] ACTIONS = {
		BACKSPACE, DELETE, END, SELECT_END, INSERT_BREAK,
		INSERT_TAB, HOME, SELECT_HOME, NEXT_CHAR, NEXT_LINE,
		NEXT_WORD, SELECT_NEXT_CHAR, SELECT_NEXT_LINE,
		SELECT_NEXT_WORD, OVERWRITE, PREV_CHAR, PREV_LINE,
		PREV_PAGE, PREV_WORD, SELECT_PREV_CHAR, SELECT_PREV_LINE,
		SELECT_PREV_WORD };

	public DefaultInputHandler()
	{
		bindings = currentBindings = new Hashtable();

		addDefaultBindings();
	}

	public void addDefaultBindings()
	{
		addKeyBinding("BACK_SPACE",BACKSPACE);
		addKeyBinding("DELETE",DELETE);

		addKeyBinding("ENTER",INSERT_BREAK);
		addKeyBinding("TAB",INSERT_TAB);

		addKeyBinding("INSERT",OVERWRITE);

		addKeyBinding("HOME",HOME);
		addKeyBinding("END",END);
		addKeyBinding("S+HOME",SELECT_HOME);
		addKeyBinding("S+END",SELECT_END);

		addKeyBinding("PAGE_UP",PREV_PAGE);
		addKeyBinding("A+UP",PREV_PAGE);
		addKeyBinding("PAGE_DOWN",NEXT_PAGE);
		addKeyBinding("A+DOWN",NEXT_PAGE);

		addKeyBinding("LEFT",PREV_CHAR);
		addKeyBinding("S+LEFT",SELECT_PREV_CHAR);
		addKeyBinding("C+LEFT",PREV_WORD);
		addKeyBinding("CS+LEFT",SELECT_PREV_WORD);
		addKeyBinding("RIGHT",NEXT_CHAR);
		addKeyBinding("S+RIGHT",SELECT_NEXT_CHAR);
		addKeyBinding("C+RIGHT",NEXT_WORD);
		addKeyBinding("CS+RIGHT",SELECT_NEXT_WORD);
		addKeyBinding("UP",PREV_LINE);
		addKeyBinding("S+UP",SELECT_PREV_LINE);
		addKeyBinding("DOWN",NEXT_LINE);
		addKeyBinding("S+DOWN",SELECT_NEXT_LINE);
	}

	public void install(JEditTextArea textArea)
	{
		if(this.textArea != null)
			throw new IllegalArgumentException("Already installed");

		this.textArea = textArea;
	}

	public void uninstall(JEditTextArea textArea)
	{
		if(this.textArea != textArea)
			throw new IllegalArgumentException("Not installed");

		this.textArea = textArea;
	}

	public void addKeyBinding(String keyBinding, ActionListener action)
	{
	        Hashtable current = bindings;

		StringTokenizer st = new StringTokenizer(keyBinding);
		while(st.hasMoreTokens())
		{
			KeyStroke keyStroke = parseKeyStroke(st.nextToken());
			if(keyStroke == null)
				return;

			if(st.hasMoreTokens())
			{
				Object o = current.get(keyStroke);
				if(o instanceof Hashtable)
					current = (Hashtable)o;
				else
				{
					o = new Hashtable();
					current.put(keyStroke,o);
					current = (Hashtable)o;
				}
			}
			else
				current.put(keyStroke,action);
		}
	}

	public void removeKeyBinding(String keyBinding)
	{
		throw new InternalError("Not yet implemented");
	}

	public void removeAllKeyBindings()
	{
		bindings.clear();
	}

	public void keyPressed(KeyEvent evt)
	{
		int keyCode = evt.getKeyCode();
		int modifiers = evt.getModifiers();
		if((modifiers & ~KeyEvent.SHIFT_MASK) != 0
			|| evt.isActionKey()
			|| keyCode == KeyEvent.VK_BACK_SPACE
			|| keyCode == KeyEvent.VK_DELETE
			|| keyCode == KeyEvent.VK_ENTER
			|| keyCode == KeyEvent.VK_TAB)
		{
			KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode,
				modifiers);
			Object o = currentBindings.get(keyStroke);
			if(o == null)
			{
				// Don't beep if the user presses some
				// key we don't know about unless a
				// prefix is active. Otherwise it will
				// beep when caps lock is pressed, etc.
				if(currentBindings != bindings)
					textArea.getToolkit().beep();
				currentBindings = bindings;
				evt.consume();
				return;
			}
			else if(o instanceof ActionListener)
			{
				((ActionListener)o).actionPerformed(
					new ActionEvent(textArea,
					ActionEvent.ACTION_PERFORMED,
					null,modifiers));
				currentBindings = bindings;
				evt.consume();
				return;
			}
			else if(o instanceof Hashtable)
			{
				currentBindings = (Hashtable)o;
				evt.consume();
				return;
			}
			else if(keyCode != KeyEvent.VK_ALT
				&& keyCode != KeyEvent.VK_CONTROL
				&& keyCode != KeyEvent.VK_SHIFT
				&& keyCode != KeyEvent.VK_META)
			{
				return;
			}
		}
	}

	public void keyReleased(KeyEvent evt)
	{
	}

	public void keyTyped(KeyEvent evt)
	{
		if(!textArea.isEditable())
		{
			textArea.getToolkit().beep();
			return;
		}

		int modifiers = evt.getModifiers();
		char c = evt.getKeyChar();
		if((modifiers & ~KeyEvent.SHIFT_MASK) == 0)
		{
			if(c >= 0x20 && c != 0x7f)
			{
				textArea.overwriteSetSelectedText(String.valueOf(c));
			}
		}
	}

	/**
	 * Converts a string to a keystroke. The string should be of the
	 * form <i>modifiers</i>+<i>shortcut</i> where <i>modifiers</i>
	 * is any combination of A for Alt, C for Control, S for Shift
	 * or M for Meta, and <i>shortcut</i> is either a single character,
	 * or a keycode name from the <code>KeyEvent</code> class, without
	 * the <code>VK_</code> prefix.
	 * @param keyStroke A string description of the key stroke
	 */
	public static KeyStroke parseKeyStroke(String keyStroke)
	{
		if(keyStroke == null)
			return null;
		int modifiers = 0;
		int ch = '\0';
		int index = keyStroke.indexOf('+');
		if(index != -1)
		{
			for(int i = 0; i < index; i++)
			{
				switch(Character.toUpperCase(keyStroke
					.charAt(i)))
				{
				case 'A':
					modifiers |= InputEvent.ALT_MASK;
					break;
				case 'C':
					modifiers |= InputEvent.CTRL_MASK;
					break;
				case 'M':
					modifiers |= InputEvent.META_MASK;
					break;
				case 'S':
					modifiers |= InputEvent.SHIFT_MASK;
					break;
				}
			}
		}
		String key = keyStroke.substring(index + 1);
		if(key.length() == 1)
			ch = Character.toUpperCase(key.charAt(0));
		else if(key.length() == 0)
		{
			System.err.println("Invalid key stroke: " + keyStroke);
			return null;
		}
		else
		{
			try
			{
				ch = KeyEvent.class.getField("VK_".concat(key))
					.getInt(null);
			}
			catch(Exception e)
			{
				System.err.println("Invalid key stroke: "
					+ keyStroke);
				return null;
			}
		}		
		return KeyStroke.getKeyStroke(ch,modifiers);
	}

	public static JEditTextArea getTextArea(ActionEvent evt)
	{
		// XXX
		return (JEditTextArea)evt.getSource();
	}

	// private members
	private Hashtable bindings;
	private Hashtable currentBindings;
	private JEditTextArea textArea;

	public static class backspace implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);

			if(!textArea.isEditable())
			{
				textArea.getToolkit().beep();
				return;
			}

			if(textArea.getSelectionStart()
			   != textArea.getSelectionEnd())
			{
				textArea.setSelectedText("");
			}
			else
			{
				int caret = textArea.getCaretPosition();
				if(caret == 0)
				{
					textArea.getToolkit().beep();
					return;
				}
				try
				{
					textArea.getDocument().remove(caret - 1,1);
				}
				catch(BadLocationException bl)
				{
					bl.printStackTrace();
				}
			}
		}
	}

	public static class delete implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);

			if(!textArea.isEditable())
			{
				textArea.getToolkit().beep();
				return;
			}

			if(textArea.getSelectionStart()
			   != textArea.getSelectionEnd())
			{
				textArea.setSelectedText("");
			}
			else
			{
				int caret = textArea.getCaretPosition();
				if(caret == textArea.getDocumentLength())
				{
					textArea.getToolkit().beep();
					return;
				}
				try
				{
					textArea.getDocument().remove(caret,1);
				}
				catch(BadLocationException bl)
				{
					bl.printStackTrace();
				}
			}
		}
	}

	public static class end implements ActionListener
	{
		private boolean select;

		public end(boolean select)
		{
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);

			int caret = textArea.getCaretPosition();

			int lastLine = textArea.getLineEndOffset(
				textArea.getCaretLine()) - 1;
			int lastVisible = textArea.getLineEndOffset(
				textArea.getFirstLine()
				+ textArea.getVisibleLines()
				- textArea.getElectricScroll() - 1) - 1;
			int lastDocument = textArea.getDocumentLength();

			if(caret == lastDocument)
			{
				textArea.getToolkit().beep();
				return;
			}
			else if(caret == lastVisible)
				caret = lastDocument;
			else if(caret == lastLine)
				caret = lastVisible;
			else
				caret = lastLine;

			if(select)
				textArea.select(textArea.getMarkPosition(),caret);
			else
				textArea.setCaretPosition(caret);
		}
	}

	public static class home implements ActionListener
	{
		private boolean select;

		public home(boolean select)
		{
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);

			int caret = textArea.getCaretPosition();

			int firstLine = textArea.getLineStartOffset(
				textArea.getCaretLine());
			int firstVisible = textArea.getLineStartOffset(
				textArea.getFirstLine()
				+ textArea.getElectricScroll());

			if(caret == 0)
			{
				textArea.getToolkit().beep();
				return;
			}
			else if(caret == firstVisible)
				caret = 0;
			else if(caret == firstLine)
				caret = firstVisible;
			else
				caret = firstLine;

			if(select)
				textArea.select(textArea.getMarkPosition(),caret);
			else
				textArea.setCaretPosition(caret);
		}
	}

	public static class insert_break implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);

			if(!textArea.isEditable())
			{
				textArea.getToolkit().beep();
				return;
			}

			textArea.setSelectedText("\n");
		}
	}

	public static class insert_tab implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);

			if(!textArea.isEditable())
			{
				textArea.getToolkit().beep();
				return;
			}

			textArea.overwriteSetSelectedText("\t");
		}
	}

	public static class next_char implements ActionListener
	{
		private boolean select;

		public next_char(boolean select)
		{
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			if(caret == textArea.getDocumentLength())
			{
				textArea.getToolkit().beep();
				return;
			}

			if(select)
				textArea.select(textArea.getMarkPosition(),
					caret + 1);
			else
				textArea.setCaretPosition(caret + 1);
		}
	}

	public static class next_line implements ActionListener
	{
		private boolean select;

		public next_line(boolean select)
		{
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			int line = textArea.getCaretLine();

			if(line == textArea.getLineCount() - 1)
			{
				textArea.getToolkit().beep();
				return;
			}

			int magic = textArea.getMagicCaretPosition();
			if(magic == -1)
			{
				magic = textArea.offsetToX(line,
					caret - textArea.getLineStartOffset(line));
			}

			caret = textArea.getLineStartOffset(line + 1)
				+ textArea.xToOffset(line + 1,magic);
			if(select)
				textArea.select(textArea.getMarkPosition(),caret);
			else
				textArea.setCaretPosition(caret);
			textArea.setMagicCaretPosition(magic);
		}
	}

	public static class next_page implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);
			int firstLine = textArea.getFirstLine();
			int visibleLines = textArea.getVisibleLines();
			int line = textArea.getCaretLine();

			if(firstLine + visibleLines + 1 >= textArea.getLineCount())
			{
				textArea.setCaretPosition(textArea.getLineStartOffset(
					textArea.getLineCount() - 1));
				return;
			}

			textArea.setFirstLine(firstLine + visibleLines);
			textArea.setCaretPosition(textArea.getLineStartOffset(
				Math.min(textArea.getLineCount() - 1,
				line + visibleLines - textArea
				.getElectricScroll())));
		}
	}

	public static class next_word implements ActionListener
	{
		private boolean select;

		public next_word(boolean select)
		{
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			int line = textArea.getCaretLine();
			int lineStart = textArea.getLineStartOffset(line);
			caret -= lineStart;

			String lineText = textArea.getLineText(textArea
				.getCaretLine());

			if(caret == lineText.length())
			{
				if(lineStart + caret == textArea.getDocumentLength())
				{
					textArea.getToolkit().beep();
					return;
				}
				caret++;
			}
			else
			{

				char ch = lineText.charAt(caret);

				String noWordSep = (String)textArea.getDocument()
					.getProperty("noWordSep");
				boolean selectNoLetter = (!Character
					.isLetterOrDigit(ch)
					&& noWordSep.indexOf(ch) == -1);

				int wordEnd = lineText.length();
				for(int i = caret; i < lineText.length(); i++)
				{
					ch = lineText.charAt(i);
					if(selectNoLetter ^ (!Character
						.isLetterOrDigit(ch) &&
						noWordSep.indexOf(ch) == -1))
					{
						wordEnd = i;
						break;
					}
				}
				caret = wordEnd;
			}

			if(select)
				textArea.select(textArea.getMarkPosition(),
					lineStart + caret);
			else
				textArea.setCaretPosition(lineStart + caret);
		}
	}

	public static class overwrite implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);
			textArea.setOverwriteEnabled(
				!textArea.isOverwriteEnabled());
		}
	}

	public static class prev_char implements ActionListener
	{
		private boolean select;

		public prev_char(boolean select)
		{
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			if(caret == 0)
			{
				textArea.getToolkit().beep();
				return;
			}

			if(select)
				textArea.select(textArea.getMarkPosition(),
					caret - 1);
			else
				textArea.setCaretPosition(caret - 1);
		}
	}

	public static class prev_line implements ActionListener
	{
		private boolean select;

		public prev_line(boolean select)
		{
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			int line = textArea.getCaretLine();

			if(line == 0)
			{
				textArea.getToolkit().beep();
				return;
			}

			int magic = textArea.getMagicCaretPosition();
			if(magic == -1)
			{
				magic = textArea.offsetToX(line,
					caret - textArea.getLineStartOffset(line));
			}

			caret = textArea.getLineStartOffset(line - 1)
				+ textArea.xToOffset(line - 1,magic);
			if(select)
				textArea.select(textArea.getMarkPosition(),caret);
			else
				textArea.setCaretPosition(caret);
			textArea.setMagicCaretPosition(magic);
		}
	}

	public static class prev_page implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);
			int firstLine = textArea.getFirstLine();
			int visibleLines = textArea.getVisibleLines();
			int line = textArea.getCaretLine();

			if(firstLine < visibleLines)
				firstLine = visibleLines;

			textArea.setFirstLine(firstLine - visibleLines);
			textArea.setCaretPosition(textArea.getLineStartOffset(
				Math.max(0,line - visibleLines +
				textArea.getElectricScroll())));
		}
	}

	public static class prev_word implements ActionListener
	{
		private boolean select;

		public prev_word(boolean select)
		{
			this.select = select;
		}

		public void actionPerformed(ActionEvent evt)
		{
			JEditTextArea textArea = getTextArea(evt);
			int caret = textArea.getCaretPosition();
			int line = textArea.getCaretLine();
			int lineStart = textArea.getLineStartOffset(line);
			caret -= lineStart;

			String lineText = textArea.getLineText(textArea
				.getCaretLine());

			if(caret == 0)
			{
				if(lineStart == 0)
				{
					textArea.getToolkit().beep();
					return;
				}
				caret--;
			}
			else
			{
				char ch = lineText.charAt(caret - 1);

				String noWordSep = (String)textArea.getDocument()
					.getProperty("noWordSep");
				boolean selectNoLetter = (!Character
					.isLetterOrDigit(ch)
					&& noWordSep.indexOf(ch) == -1);

				int wordStart = 0;
				for(int i = caret - 1; i >= 0; i--)
				{
					ch = lineText.charAt(i);
					if(selectNoLetter ^ (!Character
						.isLetterOrDigit(ch) &&
						noWordSep.indexOf(ch) == -1))
					{
						wordStart = i + 1;
						break;
					}
				}
				caret = wordStart;
			}

			if(select)
				textArea.select(textArea.getMarkPosition(),
					lineStart + caret);
			else
				textArea.setCaretPosition(lineStart + caret);
		}
	}
}
