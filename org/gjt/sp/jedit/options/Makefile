#
# Makefile for org.gjt.sp.jedit.options package
#
# It compiles files in the above package.
#

classes=org/gjt/sp/jedit/options/EditorOptionPane.class \
	org/gjt/sp/jedit/options/GeneralOptionPane.class \
	org/gjt/sp/jedit/options/FileFilterOptionPane.class \
	org/gjt/sp/jedit/options/KeyTableOptionPane.class \
	org/gjt/sp/jedit/options/StyleOptionPane.class

others='org/gjt/sp/jedit/options/StyleEditor.class' \
	'org/gjt/sp/jedit/options/StyleOptionPane$$ListHandler.class' \
	'org/gjt/sp/jedit/options/ColorTableModel.class' \
	'org/gjt/sp/jedit/options/ColorTableModel$$ColorChoice.class' \
	'org/gjt/sp/jedit/options/ColorTableModel$$ColorRenderer.class' \
	'org/gjt/sp/jedit/options/KeyTableModel.class' \
	'org/gjt/sp/jedit/options/KeyTableModel$$KeyBinding.class' \
	'org/gjt/sp/jedit/options/FileFilterTableModel.class' \
	'org/gjt/sp/jedit/options/FileFilterTableModel$$FilterEntry.class' \
	'org/gjt/sp/jedit/options/StyleTableModel.class' \
	'org/gjt/sp/jedit/options/StyleTableModel$$StyleChoice.class' \
	'org/gjt/sp/jedit/options/StyleTableModel$$StyleRenderer.class'

all: $(classes)
	echo $(classes) $(others) >> .resources

include Rules.make
