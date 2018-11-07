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

#################### FTP ########################
#################### Species #################### 
for spe in "${!species[@]}"
do
  echo species: $spe ${species["$spe"]}


  ##################### get gff annotations ####################
  echo 'Getting gff data'
  mkdir -vp $datadir'/wormbase-gff3/raw'
  mkdir -vp $datadir'/wormbase-gff3/final'
  cd $datadir'/wormbase-gff3'
  if [ ! -f final/"$spe"."${species["$spe"]}"."$wbrel".gff ]; then
    echo 'transferring' "$spe"."${species["$spe"]}"."$wbrel".gff
    wget -q --show-progress -O raw/"$spe"."${species["$spe"]}"."$wbrel".gff.gz  "ftp://ftp.wormbase.org/pub/wormbase/releases/"$wbrel"/species/"$spe"/"${species["$spe"]}"/"$spe"."${species["$spe"]}"."$wbrel".annotations.gff3.gz"
    gunzip -v raw/"$spe"."${species["$spe"]}"."$wbrel".gff.gz
    echo 'Starting GFF3 pre-processing'


    echo "$intermine"/wormmine/support/scripts/gff3/scrape_gff3.sh $datadir/wormbase-gff3/raw/"$spe"."${species["$spe"]}"."$wbrel".gff $datadir/wormbase-gff3/final/"$spe"."${species["$spe"]}"."$wbrel".gff
    bash "$intermine"/wormmine/support/scripts/gff3/scrape_gff3.sh $datadir/wormbase-gff3/raw/"$spe"."${species["$spe"]}"."$wbrel".gff $datadir/wormbase-gff3/final/"$spe"."${species["$spe"]}"."$wbrel".gff

    cd $datadir"/wormbase-gff3/final"
    python $testlab"/gff3/index.py" "$spe"."${species["$spe"]}"."$wbrel".gff
    rm "$spe"."${species["$spe"]}"."$wbrel".gff

    echo 'Done #########################'
  else
    echo  raw/"$spe"."${species["$spe"]}"."$wbrel".gff 'found'
  fi
  echo
done
