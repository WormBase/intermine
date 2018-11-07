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

#################### protein #######################
echo 'protein'
mkdir -vp $datadir/wormbase-acedb/protein/XML
mkdir -vp $datadir/wormbase-acedb/protein/mapping
cp -v $sourcedir/Protein.xml $acexmldir/protein/Protein.xml
cp -v $intermine'/wormmine/support/properties/protein_mapping.properties' $datadir'/wormbase-acedb/protein/mapping'
perl $testlab'/wb-acedb/prep_wb-acedb-protein.pl' $datadir'/wormbase-acedb/protein/Protein.xml' $datadir'/wormbase-acedb/protein/prepped_protein.xml'
perl $testlab'/wb-acedb/purge_protein.pl' $datadir'/wormbase-acedb/protein/prepped_protein.xml' $datadir/'wormbase-acedb/protein/XML/purged_prepped_protein.xml' $testlab'/wb-acedb/species_whitelist.txt' $datadir'/wormbase-acedb/protein/rejected_by_purge.xml'
# rm $datadir/wormbase-acedb/protein/prepped_protein.xml
echo


