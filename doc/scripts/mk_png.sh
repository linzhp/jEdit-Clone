for file in images/*.gif
do
	convert $file images/`basename $file .gif`.png
done
