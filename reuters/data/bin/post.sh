#/usr/bin/env

TMP=/tmp/$$.reuters.txt
ALG="formula=tfidf&usePOS=false"
for f in $*
do
sed -n '5,$p' < $f > $TMP
curl -s "http://localhost:8983/solr/analysis/summary?${ALG}&indent=true&echoParams=explicit&file=$f&wt=xml&sentences=3" --data-binary @$TMP -H 'Content-type:application/xml' 
done
rm $TMP
