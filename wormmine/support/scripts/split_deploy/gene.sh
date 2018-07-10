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
#sourcedir='/mnt/data2/acedb_dumps/WS265/WS265-test-data'
sourcedir='/mnt/data2/acedb_dumps/'$wbrel'' # <---- XML dump location


echo 'Source directory is at' $sourcedir
echo
intermine='/mnt/data2/wormmine'

#datadir=$intermine'/datadir_small'   # for now the datadir is inside the intermine directory
datadir=$intermine'/datadir'$wbrel''   # for now the datadir is inside the intermine directory
acexmldir=$datadir'/wormbase-acedb'
testlab=$intermine'/wormmine/support/scripts/'
compara=$intermine'/wormmine/support/compara'

echo 'WormMine datadir is at ' $intermine
echo 'AceDB directory is at ' $acexmldir
echo 'Perl scripts are at ' $testlab
echo


echo 'gene'
mkdir -vp $datadir/wormbase-acedb/gene/XML
mkdir -vp $datadir/wormbase-acedb/gene/mapping
cp -v $sourcedir/Gene.xml $acexmldir/gene/Gene.xml
cp -v $intermine'/wormmine/support/properties/wormbase-acedb-gene.properties' $datadir'/wormbase-acedb/gene/mapping/'
perl $testlab'/wb-acedb/purify_xace.pl' $datadir'/wormbase-acedb/gene/Gene.xml' $datadir'/wormbase-acedb/gene/purified_gene.xml'
perl $testlab'/wb-acedb/prep_wb-acedb-gene.pl' $datadir'/wormbase-acedb/gene/purified_gene.xml' $datadir'/wormbase-acedb/gene/XML/prepped_gene.xml'
rm $datadir/wormbase-acedb/gene/purified_gene.xml
echo

