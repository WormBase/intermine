#!/bin/bash

# set -x

# TODO: set release version as a script argument
# TODO: not process XML files already processed

#set the version to be accessed
wbrel="WS268"
echo 'Release version' $wbrel

declare -A species=(["c_elegans"]="PRJNA13758"
                   ["b_malayi"]="PRJNA10729"
                   ["c_angaria"]="PRJNA51225"
                   ["c_brenneri"]="PRJNA20035"
                   ["c_briggsae"]="PRJNA10731"
                   ["c_japonica"]="PRJNA12591"
                   ["c_remanei"]="PRJNA53967"
                   ["c_tropicalis"]="PRJNA53597"
                     ["o_volvulus"]="PRJEB513"
                   ["p_pacificus"]="PRJNA12644"
                   ["p_redivivus"]="PRJNA186477"
                   ["s_ratti"]="PRJEB125"
                   ["c_sinica"]="PRJNA194557")





echo 'Deploying ' $species
echo

intermine='/mnt/data2/wormmine'

# datadir=$intermine'/datadir_small'   # for now the datadir is inside the intermine directory
datadir=$intermine'/datadir'$wbrel''   # for now the datadir is inside the intermine directory
acexmldir=$datadir'/wormbase-acedb'
testlab=$intermine'/wormmine/support/scripts/'
compara=$intermine'/wormmine/support/compara'

echo 'WormMine datadir is at ' $intermine
echo 'AceDB directory is at ' $acexmldir
echo 'Perl scripts are at ' $testlab
echo

for spe in "${!species[@]}"
do
  echo species: $spe ${species["$spe"]}


  #################### get the protein data ####################
  echo 'Getting protein data'
  # mkdir -vp $datadir"/fasta/"$spe"/proteins/raw"
  # mkdir -vp $datadir"/fasta/"$spe"/proteins/prepped"
  # cd $datadir"/fasta/"$spe"/proteins/raw"
  if [ ! -f "$spe"."${species["$spe"]}"."$wbrel".protein.fa ]; then
    echo "$spe"."${species["$spe"]}"."$wbrel".protein.fa 'not found'
    echo 'transferring ' "$spe"."${species["$spe"]}"."$wbrel".protein.fa
    wget -q --show-progress -O "$spe"."${species["$spe"]}"."$wbrel".protein.fa.gz "ftp://ftp.wormbase.org/pub/wormbase/releases/"$wbrel"/species/"$spe"/"${species["$spe"]}"/"$spe"."${species["$spe"]}"."$wbrel".protein.fa.gz"
    gunzip -v "$spe"."${species["$spe"]}"."$wbrel".protein.fa.gz
  else
    echo "$spe"."${species["$spe"]}"."$wbrel".protein.fa 'found, not transferring'
  fi
  echo 'Pre-processing protein FASTA file'
  # put prepped dir here
  awk '{ if (NF > 3) {split($2,res,"="); print ">"res[2]} else {print}}' < "$spe"."${species["$spe"]}"."$wbrel".protein.fa > "$spe"."${species["$spe"]}"."$wbrel".protein.final.fa



#   perl $testlab'/fasta/wb-proteins/prep-wb-proteins.pl' "$spe"."${species["$spe"]}"."$wbrel".protein.fa ../prepped/"$spe"."${species["$spe"]}"."$wbrel".protein.fa
#   echo 

done

