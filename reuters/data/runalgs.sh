
LOCAL="tf binary log augNorm"
GLOBAL="gfidf entropy normal inverse"
GLOBAL="idf gfidf entropy normal inverse"

for l in $LOCAL
do
	sh bin/runall.sh $l
	sh bin/indexall.sh > csv/$l.csv
done

set -x
for l in $LOCAL
do
for g in $GLOBAL
do
	time sh bin/runall.sh ${l}_${g}
	time sh bin/indexall.sh > csv/${l}_${g}.csv
done
done
rm all.csv
sh bin/csv.sh > all.csv
cat csv/*.csv >> all.csv
mv all.csv csv/all.csv

