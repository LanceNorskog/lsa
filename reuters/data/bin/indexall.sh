

(while read f
do
	sh bin/csv.sh $f run/$f.xml 
done) < ref/10sentences.txt
