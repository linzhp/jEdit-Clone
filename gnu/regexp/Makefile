#
# Makefile for gnu.regexp package
#
# It compiles files in the above package.
#

classes=gnu/regexp/RESyntax.class \
	gnu/regexp/CharIndexed.class \
	gnu/regexp/REMatch.class \
	gnu/regexp/REToken.class \
	gnu/regexp/REException.class \
	gnu/regexp/RETokenChar.class \
	gnu/regexp/RETokenRange.class \
	gnu/regexp/RETokenPOSIX.class \
	gnu/regexp/RETokenOneOf.class \
	gnu/regexp/RETokenAny.class \
	gnu/regexp/RETokenRepeated.class \
	gnu/regexp/RETokenBackRef.class \
	gnu/regexp/CharIndexedString.class \
	gnu/regexp/CharIndexedCharArray.class \
	gnu/regexp/CharIndexedStringBuffer.class \
	gnu/regexp/CharIndexedInputStream.class \
	gnu/regexp/RE.class \
	gnu/regexp/RETokenStart.class \
	gnu/regexp/RETokenEnd.class \
	gnu/regexp/REMatchEnumeration.class

others=gnu/regexp/IntPair.class \
	gnu/regexp/CharUnit.class

all: $(classes)
	echo $(classes) $(others) >> .resources

include Rules.make
