#!/bin/sh

#----------------------------------------------
# UPDATE GFF3 PATHS, NOW USES FOLDER "FINAL"
# ADD GRABBING MAPPING FILE INTO "MAPPING"
# ftp://ftp.wormbase.org/pub/wormbase/releases/WS238/species/c_elegans/PRJNA13758/annotation/c_elegans.PRJNA13758.WS238.geneIDs.txt.gz
#----------------------------------------------

# Backs up intermine/datadir to a specific name in the datadir_dumps directory
# Grabs all the XML files from the hard coded file source and replaces each one accordingly

genomicfaurl='ftp://ftp.wormbase.org/pub/wormbase/releases/WS238/species/c_elegans/PRJNA13758/c_elegans.PRJNA13758.WS238.genomic.fa.gz'
proteinfaurl='ftp://ftp.wormbase.org/pub/wormbase/releases/WS238/species/c_elegans/PRJNA13758/c_elegans.PRJNA13758.WS238.protein.fa.gz'
gffurl='ftp://ftp.wormbase.org/pub/wormbase/releases/WS238/species/c_elegans/PRJNA13758/c_elegans.PRJNA13758.WS238.annotations.gff3.gz'
obofile='gene_ontology.WS238.ob'
obourl='ftp://ftp.wormbase.org/pub/wormbase/releases/WS238/ONTOLOGY/'$obofile
gaffile='gene_association.WS238.wb'
gafurl='ftp://ftp.wormbase.org/pub/wormbase/releases/WS238/ONTOLOGY/'$gaffile

dumpdir='/nfs/wormbase/wormmine/datadir_dumps'
sourcedir='/nfs/wormbase/wormmine/acedb_dumps/WS238' # WS238 HARD CODED
datadir='/nfs/wormbase/wormmine/website-intermine/acedb-dev/intermine/datadir'
acexmldir=$datadir'/wormbase-acedb'
pp='/home/jdmswong/intermine/testlab/perl/preprocess'
preprocdir=$pp'/wb-acedb'
ppgo=$pp'/go-annotation'

#cp -r ../datadir/ $dumpdir/WS234_first

# ftp files

echo 'replacing genomic fasta'
cd $datadir'/fasta/c_elegans/genomic'
rm *
wget -O genomic.fa.gz $genomicfaurl
gunzip genomic.fa.gz
cd - > ~/delete

echo 'replacing protein fasta'
cd $datadir'/fasta/c_elegans/proteins/raw'
rm *
wget -O protein.fa.gz $genomicfaurl
gunzip protein.fa.gz
$pp'/fasta/wb-proteins/prep-wb-proteins.pl' protein.fa ../prepped/prepped_protein.fa
cd - > ~/delete


#gff3
echo 'replacing gff3'
cd $datadir'/wormbase-gff3'
rm *
rm final/*
rm mapping/*
wget -O 'raw.gz' $gffurl
gunzip raw.gz
$pp'/gff3/scrape_gff3.sh' raw final/cdogma.gff3

#gff3 mapping
# UNZIP AND RUN refmt_mapping_file.pl into mapping/



cd - > ~/delete


#go
echo 'replacing go file'
rm $datadir'/go/*.obo'
wget -O $datadir'/go/'$obofile $obourl


# gaf
echo 'replacing gaf file'
rm $datadir'/go-annotation/raw/*'
wget -O $datadir'/go-annotation/raw/'$gaffile $gafurl
cd $ppgo
./xx
cd - > ~/delete

# get xml and preprocess

echo 'anatomy_term'
rm $acexmldir/anatomy_term/Anatomy_term.xml
cp $sourcedir/Anatomy_term.xml $acexmldir/anatomy_term/Anatomy_term.xml
cd $preprocdir/anatomy_term
./xx
cd - > ~/delete

echo 'cds'
rm $acexmldir/cds/CDS.xml
cp $sourcedir/CDS.xml $acexmldir/cds/CDS.xml
cd $preprocdir/cds
./xx
cd - > ~/delete

echo 'expr_cluster'
rm $acexmldir/expr_cluster/Expression_cluster.xml
cp $sourcedir/Expression_cluster.xml $acexmldir/expr_cluster/Expression_cluster.xml
cd $preprocdir/expr_cluster
./xx
cd - > ~/delete

echo 'expr_pattern'
rm $acexmldir/expr_pattern/Expr_pattern.xml
cp $sourcedir/Expr_pattern.xml $acexmldir/expr_pattern/Expr_pattern.xml
cd $preprocdir/expr_pattern
./xx
cd - > ~/delete

echo 'gene'
rm $acexmldir/gene/Gene.xml
cp $sourcedir/Gene.xml $acexmldir/gene/Gene.xml
cd $preprocdir/gene
./xx
cd - > ~/delete

echo 'life_stage'
rm $acexmldir/life_stage/Life_stage.xml
cp $sourcedir/Life_stage.xml $acexmldir/life_stage/Life_stage.xml
cd $preprocdir/life_stage
./xx
cd - > ~/delete

echo 'phenotype'
rm $acexmldir/phenotype/Phenotype.xml
cp $sourcedir/Phenotype.xml $acexmldir/phenotype/Phenotype.xml
cd $preprocdir/phenotype
./xx
cd - > ~/delete

echo 'protein'
rm $acexmldir/protein/Protein.xml
cp $sourcedir/Protein.xml $acexmldir/protein/Protein.xml
cd $preprocdir/protein
./xx
cd - > ~/delete

echo 'species'
#rm $acexmldir/species/Species.xml
#cp $sourcedir/Species.xml $acexmldir/species/Species.xml
#$preprocdir/species
#./xx
#cd - > ~/delete

echo 'transcript'
rm $acexmldir/transcript/Transcript.xml
cp $sourcedir/Transcript.xml $acexmldir/transcript/Transcript.xml
cd $preprocdir/transcript
./xx
cd - > ~/delete

echo 'variation'
rm $acexmldir/variation/Variation.xml
cp $sourcedir/Variation.xml $acexmldir/variation/Variation.xml
cd $preprocdir/variation
./xx
cd - > ~/delete

rm ~/delete

# now run the build
cd ~/intermine/wormmine
./xx
