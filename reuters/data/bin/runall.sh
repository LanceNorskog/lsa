
(while read f
do
	sh bin/post.sh data-lines/$f > run/$f.xml
done) < ref/10sentences.txt
