#!/bin/bash

# set -x

# TODO: set release version as a script argument
# TODO: not process XML files already processed

#set the version to be accessed
wbrel="WS266"
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

mkdir -p $datadir'/ensembl/compara'


################### compara #####################
echo 'compara - Human'
perl $compara'/compara.pl' $compara'/human.xml' > $datadir'/ensembl/compara/6239_9606'

echo 'compara - Zebra'
perl $compara'/compara.pl' $compara'/zebra.xml' > $datadir'/ensembl/compara/6239_7955'

echo 'compara - Mouse'
perl $compara'/compara.pl' $compara'/mus.xml' > $datadir'/ensembl/compara/6239_10090'

echo 'compara - Drosophila'
perl $compara'/compara.pl' $compara'/drosophila.xml' > $datadir'/ensembl/compara/6239_7227'

echo 'compara - Rat'
perl $compara'/compara.pl' $compara'/rat.xml' > $datadir'/ensembl/compara/6239_10116'

echo 'compara - Yeast'
perl $compara'/compara.pl' $compara'/yeast.xml' > $datadir'/ensembl/compara/6239_4932'
