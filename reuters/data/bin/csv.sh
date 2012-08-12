#!/usr/bin/env

# usage: rootname xmlfile

#
# Extract sentence lsa results from one article XML output from LSA analyzer
# name,time,formula,pos,sentences,terms,sentence0,sentence1,...,strength1,strength2,nchars1,nchars2,nchars3...
#

if [ $# == 0 ]
then
	echo "name,time,formula,pos,sentences,terms,se1,se2,se3,str1,str2,str3,te1,te2,te3"
    exit 0
fi
(
echo $1
sh ~/bin/xpath.sh "/response/lst[@name='responseHeader']/int[@name='QTime']" $2
sh ~/bin/xpath.sh "/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='formula']" $2
sh ~/bin/xpath.sh "/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='usePOS']" $2
sh ~/bin/xpath.sh "/response/lst[@name='analysis']/lst[@name='summary']/lst[@name='stats']/lst[@name='sentences']/int[@name='count']" $2 
sh ~/bin/xpath.sh "/response/lst[@name='analysis']/lst[@name='summary']/lst[@name='stats']/lst[@name='terms']/int[@name='count']" $2 
sh ~/bin/xpath.sh "/response/lst[@name='analysis']/lst[@name='summary']/lst[@name='sentences']/lst[@name='sentence']/int[@name='index']" $2 
sh ~/bin/xpath.sh "/response/lst[@name='analysis']/lst[@name='summary']/lst[@name='sentences']/lst[@name='sentence']/double[@name='strength']" $2 
sh ~/bin/xpath.sh "/response/lst[@name='analysis']/lst[@name='summary']/lst[@name='sentences']/lst[@name='sentence']/int[@name='terms']" $2 
echo
) | tr '\n' , | sed "s',,''" 
