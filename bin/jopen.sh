usage()
{
	echo "Usage: jopen [<options>] [<files>]"
	echo "Valid options:"
	echo "    --: End of options"
	echo "    -version: Print jOpen version and exit"
	echo "    -usage: Print this message and exit"
	echo "    -portfile=<file>: File with server port"
	echo "    -readonly: Open files read-only"
	exit 1
}

version()
{
	echo "jOpen-shellscript version $VERSION"
	exit 1
}

do_client()
{
	echo $auth
	echo $readonly
	echo "-cwd=`pwd`"
	echo "--"
	for file in $files
	do
		echo $file
	done
}

for opt in $@
do
	if test ! -z "$endopts"
	then
		files="$files $opt"
	else
		case $opt in
		--) endopts="yes" ;;
		-version) version ;;
		-usage) usage ;;
		-portfile=*) portfile=`echo $opt | sed 's/[-_a-zA-Z0-9]*=//'`
			;;
		-readonly) readonly=-readonly ;;
		*) files="$files $opt" ;;
		esac
	fi
	shift 1
done

if test -z "$portfile"
then
	portfile="$HOME/.jedit-server-`echo $DISPLAY|sed 's/\..*//'`"
fi

if test ! -f "$portfile"
then
	echo "jEdit server not running"
	exit 1
fi

{ read port; read auth } < "$portfile"
echo "Connecting to port $port with authorization key $auth"

do_client|hose localhost $port -slave

if test "$?" -gt "0"
then
	echo "Stale port file deleted"
	rm -f "$portfile"
	exit 1
fi
