#
# Makefile for org.gjt.sp.jedit.options package
#
# It compiles files in the above package.
#

classes=org/gjt/sp/jedit/options/ColorTableOptionPane.class \
	org/gjt/sp/jedit/options/EditorOptionPane.class \
	org/gjt/sp/jedit/options/GeneralOptionPane.class \
	org/gjt/sp/jedit/options/KeyTableOptionPane.class

others='org/gjt/sp/jedit/options/ColorChoice.class' \
	'org/gjt/sp/jedit/options/ColorChoiceRenderer.class' \
	'org/gjt/sp/jedit/options/ColorChoiceRenderer$$UIResource.class' \
	'org/gjt/sp/jedit/options/ColorTableModel.class' \
	'org/gjt/sp/jedit/options/KeyTableModel.class' \
	'org/gjt/sp/jedit/options/KeyTableModel$$KeyBinding.class'

all: $(classes)
	echo $(classes) $(others) >> .resources

include Rules.make
