#
# Makefile for org.gjt.sp.jedit.remote.impl package
#
# It compiles files in the above package.
#

classes=org/gjt/sp/jedit/remote/impl/RemoteEditorImpl.class \
	org/gjt/sp/jedit/remote/impl/RemoteBufferImpl.class \
	org/gjt/sp/jedit/remote/impl/RemoteViewImpl.class

others='org/gjt/sp/jedit/remote/impl/RemoteBufferImpl$$1.class' \
	org/gjt/sp/jedit/remote/impl/RemoteBufferImpl_Skel.class \
	org/gjt/sp/jedit/remote/impl/RemoteBufferImpl_Stub.class \
	org/gjt/sp/jedit/remote/impl/RemoteEditorImpl_Skel.class \
	org/gjt/sp/jedit/remote/impl/RemoteEditorImpl_Stub.class \
	'org/gjt/sp/jedit/remote/impl/RemoteEditorImpl$$1.class' \
	'org/gjt/sp/jedit/remote/impl/RemoteEditorImpl$$2.class' \
	'org/gjt/sp/jedit/remote/impl/RemoteEditorImpl$$3.class' \
	'org/gjt/sp/jedit/remote/impl/RemoteEditorImpl$$4.class' \
	'org/gjt/sp/jedit/remote/impl/RemoteEditorImpl$$5.class' \
	'org/gjt/sp/jedit/remote/impl/RemoteEditorImpl$$6.class' \
	'org/gjt/sp/jedit/remote/impl/RemoteEditorImpl$$EditorHandler.class' \
	org/gjt/sp/jedit/remote/impl/RemoteViewImpl_Skel.class \
	org/gjt/sp/jedit/remote/impl/RemoteViewImpl_Stub.class \
	'org/gjt/sp/jedit/remote/impl/RemoteViewImpl$$1.class' \
	'org/gjt/sp/jedit/remote/impl/RemoteViewImpl$$2.class' \
	'org/gjt/sp/jedit/remote/impl/RemoteViewImpl$$3.class'
all: $(classes)
	$(RMIC) -d . \
		org.gjt.sp.jedit.remote.impl.RemoteEditorImpl \
		org.gjt.sp.jedit.remote.impl.RemoteBufferImpl \
		org.gjt.sp.jedit.remote.impl.RemoteViewImpl
	echo $(classes) $(others) >> .resources

include Rules.make
