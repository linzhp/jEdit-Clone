#
# Makefile for doc directory
#
# It creates javadocs for jEdit's core classes, and generates the SGML docs
#

all:
	@echo
	@echo "The following targets are supported:"
	@echo "    apidocs: rebuild the jEdit API documentation"
	@echo "    htmldocs: generate nice HTML docs from the SGML source"
	@echo "    dvidocs: generate TeX DVI docs from the SGML source"
	@echo "    psdocs: generate PostScript docs from the SGML source"
	@echo
	@echo "Rerun 'make' with one of the above parameters."
	@echo
	@exit 1

apidocs:
	-javadoc -J-mx24m -classpath $(CLASSPATH):.. -noindex -notree -d api \
		org.gjt.sp.jedit \
		org.gjt.sp.jedit.event \
		org.gjt.sp.jedit.remote \
		org.gjt.sp.jedit.syntax \
		org.gjt.sp.jedit.textarea

htmldocs: create-index
	sgmltools --dsssl-spec=dsssl/jedithtml.dsl --backend=html jeditdocs.sgml

dvidocs: create-index
	sh scripts/mk_eps.sh
	sgmltools --dsssl-spec=dsssl/jeditprint.dsl --backend=dvi jeditdocs.sgml

psdocs: dvidocs
	dvips -o jeditdocs.ps jeditdocs.dvi

create-index:
	touch index.sgml
	jade -t sgml -d dsssl/jedithtml.dsl -V html-index jeditdocs.sgml
	perl scripts/collateindex.pl -i word-index -g -p -f \
		-o index.sgml HTML.index
	rm *.html -f
