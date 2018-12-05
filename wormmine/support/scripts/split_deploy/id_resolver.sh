#!/bin/bash

# set -x

# TODO: set release version as a script argument
# TODO: not process XML files already processed

#set the version to be accessed
wbrel="WS265"
echo 'Release version' $wbrel


declare -A species=(["c_elegans"]="PRJNA13758")
echo 'Deploying ' $species
echo
sourcedir='/mnt/data2/acedb_dumps/WS265/WS265-test-data'
#sourcedir='/mnt/data2/acedb_dumps/'$wbrel'' # <---- XML dump location


echo 'Source directory is at' $sourcedir
echo
intermine='/mnt/data2/wormmine'

datadir=$intermine'/datadir_small'   # for now the datadir is inside the intermine directory
#datadir=$intermine'/datadir'$wbrel''   # for now the datadir is inside the intermine directory
acexmldir=$datadir'/wormbase-acedb'
testlab=$intermine'/wormmine/support/scripts/'
compara=$intermine'/wormmine/support/compara'

echo 'WormMine datadir is at ' $intermine
echo 'AceDB directory is at ' $acexmldir
echo 'Perl scripts are at ' $testlab
echo

echo 'ncbi'
if [ ! -f $datadir'/ncbi/gene_info' ];then
  mkdir -p $datadir'/ncbi'
  wget  -q --show-progress -O $datadir'/ncbi/gene_info.gz' "ftp://ftp.ncbi.nih.gov/gene/DATA/gene_info.gz"
  gunzip -v $datadir'/ncbi/gene_info.gz'
else
  echo 'NCBI gene_info already deployed'
fi
echo

echo 'wormid'
mkdir -p $datadir'/worm'
cp -v $intermine'/wormmine/support/panther/wormid' $datadir'/worm'
echo

echo 'idresolver'
mkdir -p $datadir'/idresolver'
ln -s $datadir'/ncbi/gene_info' $datadir'/idresolver/entrez'
ln -s $datadir'/worm/wormid' $datadir'/idresolver/wormid'
echo
