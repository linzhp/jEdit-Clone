# Rules.make
# Version: 1.0
# Last Modified: 980927

SLAVA_HOME=/opt/slava
SLAVA_BIN_DIR=$(SLAVA_HOME)/bin
SLAVA_SHARE_DIR=$(SLAVA_HOME)/share/jedit-1.00
SLAVA_DOC_DIR=$(SLAVA_HOME)/doc/jedit-1.00
.SUFFIXES: .java .class
.java.class:
	javac $<
default-install:
	mkdirhier $(directory)
	cp $(files) $(directory)
subdirs:
	@(for i in $(subdirs) ; \
	do \
	make -C $$i; \
	if [ $$? != 0 ]; \
	then \
	exit $$?; \
	fi \
	done)
subdirs-install:
	@(for i in $(subdirs); \
	do \
	make -C $$i install; \
	if [ $$? != 0 ]; \
	then \
	exit $$?; \
	fi \
	done)
$(jar): $(resources)
	jar cf $(jar) $(resources)
