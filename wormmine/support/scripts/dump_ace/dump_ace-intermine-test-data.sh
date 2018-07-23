#!/bin/bash

# Dump data from ACeDB for limited objects in XML format.

CWD="`pwd`"
VERSION="$1"

if [ ! $VERSION ] ; then
  echo ""
  echo "Generate XML dumps to create a test set of data for Intermine."
  echo "You must provide the WSXXX version you wish to build."
  echo ""
  echo "Example: $0 WS246"
  exit 1
fi

ACEDB_BASE=/usr/local/wormbase/acedb
ACEDB_BIN=${ACEDB_BASE}/bin
ACEDB_DATA=${ACEDB_BASE}/wormbase_${VERSION}

DESTINATION=/usr/local/wormbase/intermine/builds/$VERSION/${VERSION}-test-data

# Create the destination directory
if [ ! -e "$DESTINATION" ]; then
    mkdir -p $DESTINATION
fi


#GENES=("unc-26"  "daf-2" "egl-15" "mir-1" "snt-1" "abc-1") 
GENES=("anx-1" "F23H11.5" "C04A11.t1" "C05C9.t1" "C05C9.t2" "cdc-16" "H17B01.2" "dig-2" "dur-91" "F35A5.8" "actl-1" "cest-1" "cup-14" "fath-1" "glb-34" "hpx-2" "impk-1" "lep-2" "natb-1" "ncap-1" "nova-1" "npp-24" "pelo-1" "unc-9" "aak-2" "aakb-1" "aakg-1" "abl-1" "aex-3" "air-2" "aka-1" "anat-1" "apr-1" "ark-1" "atm-1" "bir-1" "bmk-1" "brp-1" "bub-1" "cam-1" "cat-2" "ddx-19" "egl-15" "git-1" "gld-1" "gpd-2" "gsk-3" "jnk-1" "lgl-1" "let-7" "glh-4" "srb-2" "sucl-1" "gur-3" "arrd-4" "athp-2" "unc-26"  "daf-2" "egl-15" "mir-1" "snt-1" "abc-1")

for GENE in ${GENES[@]}; do
    
    echo "Dumping xml for $GENE..."
    $ACEDB_BIN/tace "$ACEDB_DATA" <<EOF > /dev/null
wb
query find Gene_name $GENE ; follow Public_name_for
show -x -f "$DESTINATION/temp-Gene-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow Corresponding_transcript
show -x -f "$DESTINATION/temp-Transcript-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow Corresponding_transcript ; follow Corresponding_CDS
show -x -f "$DESTINATION/temp-CDS-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow Corresponding_transcript ; follow Corresponding_CDS ; follow Corresponding_protein
show -x -f "$DESTINATION/temp-Protein-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow Allele
show -x -f "$DESTINATION/temp-Variation-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow Expr_pattern
show -x -f "$DESTINATION/temp-Expr_pattern-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow Expression_cluster
show -x -f "$DESTINATION/temp-Expression_cluster-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow Expr_pattern ; follow Life_stage
show -x -f "$DESTINATION/temp-Life_stage-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow Expr_pattern ; follow Anatomy_term
show -x -f "$DESTINATION/temp-Anatomy_term-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow Allele ; follow Phenotype       
show -x -f "$DESTINATION/temp-Phenotype-$GENE.xml"
#query find Gene_name $GENE ; follow Public_name_for ; follow Species
#show -x -f "$DESTINATION/temp-Species-$GENE.xml"
query find Gene_name $GENE ; follow Public_name_for ; follow RNAi_result
show -x -f "$DESTINATION/temp-RNAi-$GENE.xml"

EOF

done


#could probably do this with other small xml like species
    echo "Dumping entire Gene_class and species XMLs"
    $ACEDB_BIN/tace "$ACEDB_DATA" <<EOF > /dev/null
wb
query find Gene_class
show -x -f "$DESTINATION/Gene_class.xml"
#KeySet-Read acedb-dev/acedb/species.ace
query find Species
show -x -f "$DESTINATION/Species.xml"
query find Strain ; subset 100 100
show -x -f "$DESTINATION/Strain.xml"
EOF



cd $DESTINATION
echo "Concatenating files..."
cat temp-Gene*       > Gene.xml
cat temp-Transcript* > Transcript.xml
cat temp-CDS*        > CDS.xml
cat temp-Protein*    > Protein.xml
cat temp-Expr_pattern* > Expr_pattern.xml
cat temp-Expression_cluster*        > Expression_cluster.xml
cat temp-Variation*  > Variation.xml
cat temp-Life_stage* > Life_stage.xml
cat temp-Phenotype*  > Phenotype.xml
#cat temp-Species*  > Species.xml
cat temp-RNAi* > RNAi.xml
#cat temp-Gene_class* > Gene_class.xml
rm -f temp*
echo ... done.
#don't tar it up right away so the GFF can be prepared too
#though that step could be added here eventually
#cd ../
#tar czf  ${VERSION}-test-data.tgz ${VERSION}-test-data
cd "$CWD"









