#
# Makefile for org.gjt.sp.jedit.textarea package
#
# It compiles files in the above package.
#

classes=org/gjt/sp/jedit/textarea/JEditTextArea.class \
	org/gjt/sp/jedit/textarea/TextAreaModel.class \
	org/gjt/sp/jedit/textarea/TextAreaPainter.class \
	org/gjt/sp/jedit/textarea/TextUtilities.class

others='org/gjt/sp/jedit/textarea/JEditTextArea$$AdjustHandler.class' \
	'org/gjt/sp/jedit/textarea/JEditTextArea$$AutoScroll.class' \
	'org/gjt/sp/jedit/textarea/JEditTextArea$$CaretBlinker.class' \
	'org/gjt/sp/jedit/textarea/JEditTextArea$$ComponentHandler.class' \
	'org/gjt/sp/jedit/textarea/JEditTextArea$$FocusHandler.class' \
	'org/gjt/sp/jedit/textarea/JEditTextArea$$MouseHandler.class' \
	'org/gjt/sp/jedit/textarea/JEditTextArea$$ScrollLayout.class' \
	'org/gjt/sp/jedit/textarea/TextAreaModel$$DocumentHandler.class'

all: $(classes)
	echo $(classes) $(others) >> .resources

include Rules.make
