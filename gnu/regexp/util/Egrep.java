package gnu.regexp.util;
import gnu.regexp.RESyntax;

public class Egrep {
  public static void main(String[] argv) {
    System.exit(Grep.grep(argv,RESyntax.RE_SYNTAX_EGREP));
  }
}

