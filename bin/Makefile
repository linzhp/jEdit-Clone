# Makefile
files=jedit jopen
directory=$(SLAVA_BIN_DIR)
all:
install: default-install
	@(echo "Would you like to install the .kdelnks for jEdit? (y/n)";\
	read yesno;\
	case $$yesno in\
	y*) echo "Where is KDE located? [/opt/kde]" ;\
	read $$kdedir;\
	if [ "$$kdedir" = "" ]; then kdedir=/opt/kde; fi;\
	cp ../etc/*.kdelnk $$kdedir/share/applnk/Applications;;\
	esac)
include ../Rules.make
