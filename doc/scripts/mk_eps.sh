for file in images/*.gif
do
	convert -geometry 75%x75% \
		-colors 256 -colorspace GRAY $file images/`basename $file .gif`.eps
done
