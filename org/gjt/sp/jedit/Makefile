#
# Makefile for org.gjt.sp.jedit package
#
# It compiles files in the above package, and runs $(MAKE) for all
# sub-packages.
#

classes=org/gjt/sp/jedit/jEdit.class \
	org/gjt/sp/jedit/Buffer.class \
	org/gjt/sp/jedit/EditAction.class \
	org/gjt/sp/jedit/GUIUtilities.class \
	org/gjt/sp/jedit/JARClassLoader.class \
	org/gjt/sp/jedit/Marker.class \
	org/gjt/sp/jedit/MiscUtilities.class \
	org/gjt/sp/jedit/Mode.class \
	org/gjt/sp/jedit/OptionPane.class \
	org/gjt/sp/jedit/Plugin.class \
	org/gjt/sp/jedit/View.class

others='org/gjt/sp/jedit/jEdit$$Autosave.class' \
	'org/gjt/sp/jedit/jEdit$$EditorHandler.class' \
	'org/gjt/sp/jedit/jEdit$$Server.class' \
	'org/gjt/sp/jedit/jEdit$$ServerClientHandler.class' \
	'org/gjt/sp/jedit/Buffer$$BufferProps.class' \
	'org/gjt/sp/jedit/Buffer$$DocumentHandler.class' \
	'org/gjt/sp/jedit/Buffer$$EditorHandler.class' \
	'org/gjt/sp/jedit/Buffer$$UndoHandler.class' \
	'org/gjt/sp/jedit/View$$BufferHandler.class' \
	'org/gjt/sp/jedit/View$$EditorHandler.class' \
	'org/gjt/sp/jedit/View$$CaretHandler.class' \
	'org/gjt/sp/jedit/View$$KeyHandler.class' \
	'org/gjt/sp/jedit/View$$WindowHandler.class' \
	org/gjt/sp/jedit/jedit.props \
	org/gjt/sp/jedit/jedit_gui.props \
	org/gjt/sp/jedit/jedit_keys.props \
	org/gjt/sp/jedit/jedit_logo.jpg \
	org/gjt/sp/jedit/jedit_tips.props

all: $(classes) subdirs
	echo $(classes) $(others) >> .resources

subdirs:
	$(MAKE) -f org/gjt/sp/jedit/actions/Makefile
	$(MAKE) -f org/gjt/sp/jedit/event/Makefile
	$(MAKE) -f org/gjt/sp/jedit/gui/Makefile
	$(MAKE) -f org/gjt/sp/jedit/mode/Makefile
	$(MAKE) -f org/gjt/sp/jedit/options/Makefile
	$(MAKE) -f org/gjt/sp/jedit/proto/Makefile
	$(MAKE) -f org/gjt/sp/jedit/search/Makefile
	$(MAKE) -f org/gjt/sp/jedit/syntax/Makefile
	$(MAKE) -f org/gjt/sp/jedit/toolbar/Makefile

include Rules.make
