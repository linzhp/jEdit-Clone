package gnu.regexp.util;

import gnu.regexp.*;
import gnu.getopt.*;
import java.io.*;

public class Grep {
  private static final int BYTE_OFFSET = 0;
  private static final int COUNT = 1;
  private static final int LINE_NUMBER = 2;
  private static final int QUIET = 3;
  private static final int SILENT = 4;
  private static final int NO_FILENAME = 5;
  private static final int REVERT_MATCH = 6;
  private static final int FILES_WITH_MATCHES = 7;
  private static final int LINE_REGEXP = 8;
  private static final int FILES_WITHOUT_MATCH = 9;

  private static final String PROGNAME = "gnu.regexp.util.Grep";
  private static final String PROGVERSION = "1.00";

  public static void main(String[] argv) {
    System.exit(grep(argv,RESyntax.RE_SYNTAX_GREP));
  }

  public static int grep(String[] argv, RESyntax syntax) {
    // use gnu.getopt to read arguments
    int cflags = 0;
    
    boolean[] options = new boolean [10];
    
    LongOpt[] longOptions = { 
        new LongOpt("byte-offset",         LongOpt.NO_ARGUMENT, null, 'b'),
	new LongOpt("count",               LongOpt.NO_ARGUMENT, null, 'c'),
	new LongOpt("no-filename",         LongOpt.NO_ARGUMENT, null, 'h'),
	new LongOpt("ignore-case",         LongOpt.NO_ARGUMENT, null, 'i'),
	new LongOpt("files-with-matches",  LongOpt.NO_ARGUMENT, null, 'l'),
	new LongOpt("help",                LongOpt.NO_ARGUMENT, null, '!'),
	new LongOpt("line-number",         LongOpt.NO_ARGUMENT, null, 'n'),
	new LongOpt("quiet",               LongOpt.NO_ARGUMENT, null, 'q'),
	new LongOpt("silent",              LongOpt.NO_ARGUMENT, null, 'q'),
	new LongOpt("no-messages",         LongOpt.NO_ARGUMENT, null, 's'),
	new LongOpt("revert-match",        LongOpt.NO_ARGUMENT, null, 'v'),
	new LongOpt("line-regexp",         LongOpt.NO_ARGUMENT, null, 'x'),
	new LongOpt("extended-regexp",     LongOpt.NO_ARGUMENT, null, 'E'),
	new LongOpt("fixed-strings",       LongOpt.NO_ARGUMENT, null, 'F'), // TODO
	new LongOpt("basic-regexp",        LongOpt.NO_ARGUMENT, null, 'G'),
	new LongOpt("files-without-match", LongOpt.NO_ARGUMENT, null, 'L'),
	new LongOpt("version",             LongOpt.NO_ARGUMENT, null, 'V')
	  };

    Getopt g = new Getopt(PROGNAME, argv, "bchilnqsvxyEFGLV", longOptions);
    int c;
    String arg;
    while ((c = g.getopt()) != -1) {
      switch (c) {
      case 'b':
	options[BYTE_OFFSET] = true;
	break;
      case 'c':
	options[COUNT] = true;
	break;
      case 'h':
	options[NO_FILENAME] = true;
	break;
      case 'i':
      case 'y':
	cflags |= RE.REG_ICASE;
	break;
      case 'l':
	options[FILES_WITH_MATCHES] = true;
	break;
      case 'n':
	options[LINE_NUMBER] = true;
	break;
      case 'q':
	options[QUIET] = true;
	break;
      case 's':
	options[SILENT] = true;
	break;
      case 'v':
	options[REVERT_MATCH] = true;
	break;
      case 'x':
	options[LINE_REGEXP] = true;
	break;
      case 'E':  // TODO: check compatibility with grep
	syntax = RESyntax.RE_SYNTAX_EGREP;
	break;
      case 'F':  // TODO: fixed strings
	break;
      case 'G':
	syntax = RESyntax.RE_SYNTAX_GREP;
	break;
      case 'L':
	options[FILES_WITHOUT_MATCH] = true;
	break;
      case 'V':
	System.err.println(PROGNAME+' '+PROGVERSION);
	return 0;
      case '!': // help
	BufferedReader br = new BufferedReader(new InputStreamReader((Grep.class).getResourceAsStream("GrepUsage.txt")));
	String line;
	try {
	  while ((line = br.readLine()) != null)
	    System.out.println(line);
	} catch (IOException ie) { }
	return 0;
      }
    }	      
    
    
    InputStream is = null;
    RE pattern = null;
    int optind = g.getOptind();
    if (optind >= argv.length) {
      System.err.println("Usage: java " + PROGNAME + " [OPTION]... PATTERN [FILE]...");
      System.err.println("Try `java " + PROGNAME + " --help' for more information.");
      return 2;
    }
    try {
      pattern = new RE(argv[g.getOptind()],cflags,syntax);
    } catch (REException e) {
      System.err.println("Error in expression: "+e);
      return 2;
    }
    int retval = 1;
    if (argv.length >= g.getOptind()+2) {
      for (int i = g.getOptind() + 1; i < argv.length; i++) {
	if (argv[i].equals("-")) {
	  if (processStream(pattern,System.in,options,(argv.length == g.getOptind()+2) || options[NO_FILENAME] ? null : "(standard input)")) {
	    retval = 0;
	  }
	} else {
	  try {
	    is = new FileInputStream(argv[i]);
	    if (processStream(pattern,is,options,(argv.length == g.getOptind()+2) || options[NO_FILENAME] ? null : argv[i]))
	      retval = 0;
	  } catch (FileNotFoundException e) {
	    if (!options[SILENT])
	      System.err.println(PROGNAME+": "+e);
	  }
	}
      }
    } else {
      if (processStream(pattern,System.in,options,null))
	retval = 1;
    }
    return retval;
  }

  private static boolean processStream(RE pattern, InputStream is, boolean[] options, String filename) {
    int newlineLen = System.getProperty("line.separator").length();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    int count = 0;
    long atByte = 0;
    int atLine = 1;
    String line;
    REMatch match;
    
    try {
      while ((line = br.readLine()) != null) {
	match = pattern.getMatch(line);
	if (((options[LINE_REGEXP] && pattern.isMatch(line))
	     || (!options[LINE_REGEXP] && (match != null))) 
	    ^ options[REVERT_MATCH]) {
	  count++;
	  if (!options[COUNT]) {
	    if (options[QUIET]) {
	      return true;
	    }
	    if (options[FILES_WITH_MATCHES]) {
	      if (filename != null)
		System.out.println(filename);
	      return true;
	    }
	    if (options[FILES_WITHOUT_MATCH]) {
	      return false;
	    }
	    if (filename != null) {
	      System.out.print(filename);
	      System.out.print(':');
	    }
	    if (options[LINE_NUMBER]) {
	      System.out.print(atLine);
	      System.out.print(':');
	    }
	    if (options[BYTE_OFFSET]) {
	      System.out.print(atByte + match.getStartIndex() );
	      System.out.print(':');
	    }
	    System.out.println(line);
	  }
	} // a match
	atByte += line.length() + newlineLen; // could be troublesome...
	atLine++;
      } // a valid line
      br.close();

      if (options[COUNT]) {
	if (filename != null)
	  System.out.println(filename+':');
	System.out.println(count);
      }
      if (options[FILES_WITHOUT_MATCH] && count==0) {
	if (filename != null)
	  System.out.println(filename);
      }
    } catch (IOException e) {
      System.err.println(PROGNAME+": "+e);
    }
    return ((count > 0) ^ options[REVERT_MATCH]);
  }
}
