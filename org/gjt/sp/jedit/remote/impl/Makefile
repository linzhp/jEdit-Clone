#
# Makefile for org.gjt.sp.jedit.remote.impl package
#
# It compiles files in the above package.
#

classes=org/gjt/sp/jedit/remote/impl/RemoteEditorImpl.class \
	org/gjt/sp/jedit/remote/impl/RemoteBufferImpl.class \
	org/gjt/sp/jedit/remote/impl/RemoteViewImpl.class

others=org/gjt/sp/jedit/remote/impl/RemoteBufferImpl_Skel.class \
	org/gjt/sp/jedit/remote/impl/RemoteBufferImpl_Stub.class \
	org/gjt/sp/jedit/remote/impl/RemoteEditorImpl_Skel.class \
	org/gjt/sp/jedit/remote/impl/RemoteEditorImpl_Stub.class \
	org/gjt/sp/jedit/remote/impl/RemoteViewImpl_Skel.class \
	org/gjt/sp/jedit/remote/impl/RemoteViewImpl_Stub.class \
	'org/gjt/sp/jedit/remote/impl/RemoteEditorImpl$$EditorHandler.class'
all: $(classes)
	$(RMIC) -d . \
		org.gjt.sp.jedit.remote.impl.RemoteEditorImpl \
		org.gjt.sp.jedit.remote.impl.RemoteBufferImpl \
		org.gjt.sp.jedit.remote.impl.RemoteViewImpl
	echo $(classes) $(others) >> .resources

include Rules.make
