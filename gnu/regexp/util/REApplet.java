package gnu.regexp.util;
import java.applet.*;
import java.awt.*;
import gnu.regexp.*;

public class REApplet extends Applet {
  Label l1, l2, l3;
  Button b;
  TextField tf;
  TextArea input, output;
  Checkbox insens;

  public void init() {
    // test run RE stuff so it doesn't appear slow on first search.
    try {
      RE x = new RE("^.*(w[x])\1$");
      REMatchEnumeration xx = x.getMatchEnumeration("wxwx");
      while (xx.hasMoreMatches()) xx.nextMatch().toString();
    } catch (REException arg) { }

    setBackground(Color.lightGray);

    GridBagLayout gbag = new GridBagLayout();
    setLayout(gbag);
    GridBagConstraints c = new GridBagConstraints();

    Panel p = new Panel();
    GridBagLayout gbag2 = new GridBagLayout();
    p.setLayout(gbag2);

    c.weightx = 1.0;
    l1 = new Label("Regular Expression");
    gbag2.setConstraints(l1,c);
    p.add(l1);

    tf = new TextField(getParameter("regexp"),30);
    gbag2.setConstraints(tf,c);
    p.add(tf);
    
    b = new Button("Match");
    c.gridwidth = GridBagConstraints.REMAINDER;
    gbag2.setConstraints(b,c);
    p.add(b);

    int z = c.gridx;
    c.gridx = 1;
    c.anchor = GridBagConstraints.WEST;
    insens = new Checkbox("Ignore case",false);
    gbag2.setConstraints(insens,c);
    p.add(insens);
    c.gridx = z;
    c.anchor = GridBagConstraints.CENTER;

    // Add the panel itself.
    c.gridwidth = GridBagConstraints.REMAINDER;
    gbag.setConstraints(p,c);
    add(p);

    l2 = new Label("Input Text");
    c.gridwidth = 1;
    gbag.setConstraints(l2,c);
    add(l2);
    
    l3 = new Label("Matches Found");
    c.gridwidth = GridBagConstraints.REMAINDER;
    gbag.setConstraints(l3,c);
    add(l3);
    
    input = new TextArea(getParameter("input"),5,30);
    c.gridwidth = 1;
    gbag.setConstraints(input,c);
    add(input);

    output = new TextArea(5,30);
    output.setEditable(false);
    c.gridwidth = GridBagConstraints.REMAINDER;
    gbag.setConstraints(output,c);
    add(output);
  }

  public boolean action(Event e, Object arg) {
    Object target = e.target;

    if (target == b) { // match
      String expr = tf.getText();
      RE reg = null;
      try {
	reg = new RE(expr,insens.getState() ? RE.REG_ICASE : 0);
	REMatchEnumeration en = reg.getMatchEnumeration(input.getText());
	StringBuffer sb = new StringBuffer();
	int matchNum = 0;
	while (en.hasMoreMatches()) {
	  sb.append(String.valueOf(++matchNum));
	  sb.append(". ");
	  sb.append(en.nextMatch().toString());
	  sb.append('\n');
	}
	output.setText(sb.toString());
      } catch (REException err) { 
	output.setText(err.getMessage());
      }
      return true;
    } else return false;
  }
}
