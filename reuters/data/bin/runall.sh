
ALG=tf
if [ $# -eq 1 ]
then
	ALG=$1
fi

(while read f
do
	sh bin/post.sh $ALG data-lines/$f > run/$f.xml
done) < ref/10sentences.txt
