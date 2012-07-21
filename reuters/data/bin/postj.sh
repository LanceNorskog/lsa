#/usr/bin/env

TMP=/tmp/$$.reuters.txt
for f in $*
do
sed -n '5,$p' < $f > $TMP
curl -s "http://localhost:8983/solr/analysis/summary?indent=true&echoParams=explicit&file=$f&wt=json" --data-binary @$TMP -H 'Content-type:application/xml' 
done
