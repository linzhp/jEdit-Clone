#
# Makefile for org.gjt.sp.jedit.textarea package
#
# It compiles files in the above package.
#

classes=org/gjt/sp/jedit/textarea/JEditTextArea.class \
	org/gjt/sp/jedit/textarea/TextAreaEvents.class \
	org/gjt/sp/jedit/textarea/TextAreaModel.class \
	org/gjt/sp/jedit/textarea/TextAreaPainter.class

others='org/gjt/sp/jedit/textarea/JEditTextArea$$AdjustHandler.class' \
	'org/gjt/sp/jedit/textarea/JEditTextArea$$ScrollLayout.class'

all: $(classes)
	echo $(classes) $(others) >> .resources

include Rules.make
