package org.gjt.sp.jedit.syntax;

import javax.swing.text.Segment;

public class TSQLTokenMarker extends TokenMarker
{
	// public members
	public static final String OPERATOR = "operator";
	public static final String PUNCTUATION = "punctuation";

	public TSQLTokenMarker(KeywordMap keywords)
	{
		this.keywords = keywords;
	}

	public Token markTokens(Segment line, int lineIndex)
	{
		ensureCapacity(lineIndex);
		lastToken = null;
		String token = lineIndex == 0 ? null : lineInfo[lineIndex - 1];
		int offset = line.offset;
		int lastOffset = offset;
		int lastKeyword = offset;
		int length = line.count + offset;

loop:
		for(int i = offset; i < length; i++)
		{
			switch(line.array[i])
			{
			case '*':
				if(token == Token.COMMENT1 && length - i >= 1 && line.array[i+1] == '/')
				{
					token = null;
					i++;
					addToken((i + 1) - lastOffset,Token.COMMENT1);
					lastOffset = i + 1;
				}
				else if (token == null)
				{
					lastOffset = searchBack(line, i, lastOffset);
					addToken(1,OPERATOR);
					lastOffset = i + 1;
				}
				break;
			case '[':
				if(token == null)
				{
					lastOffset = searchBack(line, i, lastOffset);
					token = Token.LITERAL1;
					literalChar = '[';
					lastOffset = i;
				}
				break;
			case ']':
				if(token == Token.LITERAL1 && literalChar == '[')
				{
					token = null;
					literalChar = 0;
					addToken((i + 1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
				}
				break;
			case '.': case ',': case '(': case ')':
				if (token == null) {
					lastOffset = searchBack(line, i, lastOffset);
					addToken(1,PUNCTUATION);
					lastOffset = i + 1;
				}
				break;
			case '+': case '%': case '&': case '|': case '^':
			case '~': case '<': case '>': case '=':
				if (token == null) {
					lastOffset = searchBack(line, i, lastOffset);
					addToken(1,OPERATOR);
					lastOffset = i + 1;
				}
				break;
			case ' ': case '\t':
				if (token == null) {
					lastOffset = searchBack(line, i, lastOffset, false);
				}
				break;
			case ':':
				if(token == null)
				{
					addToken((i+1) - lastOffset,Token.LABEL);
					lastOffset = i + 1;
				}
				break;
			case '/':
				if(token == null)
				{
					if (length - i >= 2 && line.array[i + 1] == '*')
					{
						lastOffset = searchBack(line, i, lastOffset);
						token = Token.COMMENT1;
						lastOffset = i;
						i++;
					}
					else
					{
						lastOffset = searchBack(line, i, lastOffset);
						addToken(1,OPERATOR);
						lastOffset = i + 1;
					}
				}
				break;
			case '-':
				if(token == null)
				{
					if (length - i >= 2 && line.array[i+1] == '-')
					{
						lastOffset = searchBack(line, i, lastOffset);
						addToken(length - i,Token.COMMENT1);
						lastOffset = length;
						break loop;
					}
					else
					{
						lastOffset = searchBack(line, i, lastOffset);
						addToken(1,OPERATOR);
						lastOffset = i + 1;
					}
				}
				break;
			case '!':
				if(token == null && length - i >= 2 &&
				(line.array[i+1] == '=' || line.array[i+1] == '<' || line.array[i+1] == '>'))
				{
					lastOffset = searchBack(line, i, lastOffset);
					addToken(1,OPERATOR);
					lastOffset = i + 1;
				}
				break;
			case '"':
				if(token == null)
				{
					token = Token.LITERAL1;
					literalChar = '"';
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == Token.LITERAL1 && literalChar == '"')
				{
					token = null;
					literalChar = 0;
					addToken((i + 1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
				}
				break;
			case '\'':
				if(token == null)
				{
					token = Token.LITERAL1;
					literalChar = '\'';
					addToken(i - lastOffset,null);
					lastOffset = i;
				}
				else if(token == Token.LITERAL1 && literalChar == '\'')
				{
					token = null;
					literalChar = 0;
					addToken((i + 1) - lastOffset,Token.LITERAL1);
					lastOffset = i + 1;
				}
				break;
			default:
				break;
			}
		}
		if(token == null)
			lastOffset = searchBack(line, length, lastOffset, false);
		if(lastOffset != length)
			addToken(length - lastOffset,token);
		lineInfo[lineIndex] = token;
		if(lastToken != null)
		{
			lastToken.nextValid = false;
			return firstToken;
		}
		else
			return null;
	}

	// private members
	private KeywordMap keywords;
	private char literalChar = 0;

	private static boolean isKeywordCharacter(char c)
	{
		return Character.isLetter(c) || c == '_' || c == '@';
	}

	private int searchBack(Segment line, int pos, int lastOffset)
	{
		return searchBack(line, pos, lastOffset, true);
	}

	private int searchBack(Segment line, int pos, int lastOffset, boolean padNull)
	{
		int off = pos;
		while(--off >= lastOffset)
		{
			if(!isKeywordCharacter(line.array[off]))
				break;
		}
		off++;
		int len = pos - off;
		String id = keywords.lookup(line,off,len);
		if(id != null)
		{
			if(off != lastOffset)
				addToken(off - lastOffset,null);
			addToken(len,id);
			lastOffset = pos;
		}
		if (padNull && lastOffset < pos)
			addToken(pos - lastOffset, null);
		return lastOffset;
	}
}
