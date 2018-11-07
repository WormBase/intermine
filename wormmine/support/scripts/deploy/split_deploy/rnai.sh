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


#################### RNAi #######################
echo 'RNAi'
mkdir -vp $datadir/wormbase-acedb/RNAi/XML
mkdir -vp $datadir/wormbase-acedb/RNAi/mapping
cp -v $sourcedir/RNAi.xml $acexmldir/RNAi/RNAi.xml
cp -v $intermine'/wormmine/support/properties/RNAi_mapping.properties' $datadir'/wormbase-acedb/RNAi/mapping'
perl $testlab'/wb-acedb/prep_RNAi.pl' $datadir'/wormbase-acedb/RNAi/RNAi.xml' $datadir'/wormbase-acedb/RNAi/prepped_RNAi.xml'
perl $testlab'/wb-acedb/prep_wb-acedb-RNAi.pl' $datadir'/wormbase-acedb/RNAi/prepped_RNAi.xml' $datadir'/wormbase-acedb/RNAi/XML/prepped_clean_RNAi.xml'
echo

