#
# Makefile for org.gjt.sp.jedit package
#
# It compiles files in the above package, and runs $(MAKE) for all
# sub-packages.
#

classes=org/gjt/sp/jedit/jEdit.class \
	org/gjt/sp/jedit/Buffer.class \
	org/gjt/sp/jedit/EditAction.class \
	org/gjt/sp/jedit/EditPlugin.class \
	org/gjt/sp/jedit/EditServer.class \
	org/gjt/sp/jedit/GUIUtilities.class \
	org/gjt/sp/jedit/JARClassLoader.class \
	org/gjt/sp/jedit/Marker.class \
	org/gjt/sp/jedit/MiscUtilities.class \
	org/gjt/sp/jedit/Mode.class \
	org/gjt/sp/jedit/OptionPane.class \
	org/gjt/sp/jedit/Plugin.class \
	org/gjt/sp/jedit/REFileFilter.class \
	org/gjt/sp/jedit/Registers.class \
	org/gjt/sp/jedit/View.class

others='org/gjt/sp/jedit/jEdit$$1.class' \
	'org/gjt/sp/jedit/jEdit$$WindowHandler.class' \
	'org/gjt/sp/jedit/EditServer$$1.class' \
	'org/gjt/sp/jedit/EditServer$$2.class' \
	'org/gjt/sp/jedit/EditServer$$3.class' \
	'org/gjt/sp/jedit/EditServer$$4.class' \
	'org/gjt/sp/jedit/Buffer$$BufferProps.class' \
	'org/gjt/sp/jedit/Buffer$$DocumentHandler.class' \
	'org/gjt/sp/jedit/Buffer$$EditorHandler.class' \
	'org/gjt/sp/jedit/Buffer$$UndoHandler.class' \
	'org/gjt/sp/jedit/Plugin$$OldAPI.class' \
	'org/gjt/sp/jedit/Plugin$$Wrapper.class' \
	'org/gjt/sp/jedit/Registers$$CaretRegister.class' \
	'org/gjt/sp/jedit/Registers$$ClipboardRegister.class' \
	'org/gjt/sp/jedit/Registers$$Register.class' \
	'org/gjt/sp/jedit/Registers$$StringRegister.class' \
	'org/gjt/sp/jedit/View$$BufferHandler.class' \
	'org/gjt/sp/jedit/View$$EditorHandler.class' \
	'org/gjt/sp/jedit/View$$CaretHandler.class' \
	'org/gjt/sp/jedit/View$$StatusBar.class' \
	org/gjt/sp/jedit/jedit.props \
	org/gjt/sp/jedit/jedit_gui.props \
	org/gjt/sp/jedit/jedit_keys.props \
	org/gjt/sp/jedit/jedit_logo.gif

all: $(classes)
	echo $(classes) $(others) >> .resources
	$(MAKE) -f org/gjt/sp/jedit/actions/Makefile
	$(MAKE) -f org/gjt/sp/jedit/event/Makefile
	$(MAKE) -f org/gjt/sp/jedit/gui/Makefile
	$(MAKE) -f org/gjt/sp/jedit/mode/Makefile
	$(MAKE) -f org/gjt/sp/jedit/options/Makefile
	$(MAKE) -f org/gjt/sp/jedit/proto/Makefile
	$(MAKE) -f org/gjt/sp/jedit/search/Makefile
	$(MAKE) -f org/gjt/sp/jedit/syntax/Makefile
	$(MAKE) -f org/gjt/sp/jedit/textarea/Makefile
	$(MAKE) -f org/gjt/sp/jedit/toolbar/Makefile

include Rules.make
