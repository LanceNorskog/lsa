#/usr/bin/env

TMP=/tmp/$$.reuters.txt

#POS was better all along the board
ALG="formula=$1&usePOS=true"
shift 1
if [ $# -eq 0 ]
then
	echo usage: algorithm file
fi

sed -n '5,$p' < $1 > $TMP
curl -s "http://localhost:8983/solr/analysis/summary?${ALG}&indent=true&echoParams=explicit&file=$1&wt=xml&sentences=3" --data-binary @$TMP -H 'Content-type:application/xml' 
rm $TMP
