# Rules.make
JAVAC=javac
SLAVA_HOME=/opt/slava
SLAVA_BIN_DIR=$(SLAVA_HOME)/bin
SLAVA_SHARE_DIR=$(SLAVA_HOME)/share/jedit-1.0pre1
SLAVA_DOC_DIR=$(SLAVA_SHARE_DIR)/doc
SLAVA_JARS_DIR=$(SLAVA_SHARE_DIR)/jars
.SUFFIXES: .java .class
.java.class:
	@echo $<:
	@$(JAVAC) -classpath $(CLASSPATH):../../src $<
default-install:
	mkdirhier $(directory)
	cp $(files) $(directory)
jar-install:
	mkdirhier $(SLAVA_JARS_DIR)
	cp $(jar) $(SLAVA_JARS_DIR)
$(jar): $(resources)
	jar cf0 $(jar) $(resources)
