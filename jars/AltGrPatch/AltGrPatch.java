import java.awt.event.*;
import javax.swing.text.*;

/**
 * Patch to work around the problem where currently
 * the AltGr doesn't work with swing text because
 * of the way it filters key typed events. At some
 * point the native AWT support will be fixed and the
 * filtering behavior can be removed, but until then
 * this patch should help (it's a slightly improved
 * filter). This will only work on win32 which is 
 * currently setting modifiers on the key typed 
 * events.
 * <p>
 * This can be installed by calling 
 * <pre><code>
 * &nbsp; new AltGrPatch();
 * </code></pre>
 * prior to using a swing text component. The creation
 * of an instance of this class does not itself do anything
 * but causes the class to be loaded and the static 
 * initialization to take place. The static
 * initializer will change the default keymaps handling
 * of the key typed events when this class is loaded.
 * This patch assumes that the the handling of key
 * typed events hasn't been changed by the application
 * (i.e. by calling Keymap.setDefaultAction on a keymap).
 * If the application is already modifying the keymap, 
 * the applications action for this should be modified
 * to include the behavior contained in the nested class
 * AltGrPatch.DefaultKeyTypedAction.
 *
 */
public class AltGrPatch {

	/**
	 * The action that is executed by default if 
	 * a <em>key typed event</em> is received and there
	 * is no keymap entry. There is a variation across
	 * different VM's in what gets sent as a <em>key typed</em>
	 * event, and this action tries to filter out the undesired
	 * events. This filters the control characters and those
	 * with the ALT modifier.
	 * <p>
	 * If the event doesn't get filtered, it will try to insert
	 * content into the text editor. The content is fetched
	 * from the command string of the ActionEvent. The text
	 * entry is done through the <code>replaceSelection</code>
	 * method on the target text component. This is the
	 * action that will be fired for most text entry tasks.
	 *
	 * @see DefaultEditorKit#defaultKeyTypedAction
	 * @see DefaultEditorKit#getActions
	 * @see Keymap#setDefaultAction
	 * @see Keymap#getDefaultAction
	 */
	 static class DefaultKeyTypedAction extends TextAction {

		/**
		 * Creates this object with the appropriate identifier.
		 */
		public DefaultKeyTypedAction() {
			super(DefaultEditorKit.defaultKeyTypedAction);
        }

        /**
         * The operation to perform when this action is triggered.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
			 JTextComponent target = getTextComponent(e);
			 if ((target != null) && (e != null)) {
				 String content = e.getActionCommand();
				 int mod = e.getModifiers();
				 if ((content != null) && (content.length() > 0) &&
				 	(((mod & ActionEvent.ALT_MASK) == 0) ||
					((mod & ActionEvent.CTRL_MASK) != 0))) {

						char c = content.charAt(0);
						if ((c >= 0x20) && (c != 0x7F)) {
							target.replaceSelection(content);
						}
				}
			}
        }
    }

	static {
		Keymap map =
			JTextComponent.getKeymap(JTextComponent.DEFAULT_KEYMAP);
	 
	 	if (map != null) {
			map.setDefaultAction(new DefaultKeyTypedAction());
			System.err.println("AltGr patch applied");
		} else {
			System.err.println("Default keymap doesn't exist, patch not applied");
		}
	 }

}
