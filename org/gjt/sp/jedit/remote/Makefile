#
# Makefile for org.gjt.sp.jedit.remote package
#
# It compiles files in the above package.
#

classes=org/gjt/sp/jedit/remote/RemoteBuffer.class \
	org/gjt/sp/jedit/remote/RemoteEditor.class \
	org/gjt/sp/jedit/remote/RemoteView.class

all: $(classes)
	$(MAKE) -f org/gjt/sp/jedit/remote/impl/Makefile
	echo $(classes) $(others) >> .resources

include Rules.make
