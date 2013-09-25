#!/usr/bin/perl

use strict;
use Switch;
use Template;

my $datadir = '../../../datadir';

# Used by fasta and gff3 sources
my $speciesraw = << "SPECIES";
<!ENTITY          a_suum_taxon_id "6253">
<!ENTITY        b_malayi_taxon_id "6279">
<!ENTITY    b_xylophilus_taxon_id "6326">
<!ENTITY       c_angaria_taxon_id "96668">
<!ENTITY      c_brenneri_taxon_id "135651">
<!ENTITY      c_briggsae_taxon_id "6238">
<!ENTITY       c_elegans_taxon_id "6239">
<!ENTITY      c_japonica_taxon_id "281687">
<!ENTITY       c_remanei_taxon_id "31234">
<!ENTITY           c_sp5_taxon_id "-5">
<!ENTITY          c_sp11_taxon_id "-11">
<!ENTITY h_bacteriophora_taxon_id "37862">
<!ENTITY     h_contortus_taxon_id "6289">
<!ENTITY         m_hapla_taxon_id "6305">
<!ENTITY     m_incognita_taxon_id "6306">
<!ENTITY     p_pacificus_taxon_id "54126">
<!ENTITY         s_ratti_taxon_id "34506">
<!ENTITY      t_spiralis_taxon_id "6334">
SPECIES
my @species = $speciesraw =~ /\s(\w+)_taxon_id/mg;

my @ace_species = (
{ Anatomy_term => 'AnatomyTerm' },
{ CDS => 'CDS' },
{ Expression_cluster => 'ExpressionCluster' },
{ Expr_pattern => 'EpressionPattern' },
{ Gene => 'Gene' },
{ Life_stage => 'LifeStage' },
{ Phenotype => 'Phenotype' },
{ Protein => 'Protein' },
{ RNAi => 'RNAi' },
{ Species => 'Organism' },
{ Transcript => 'Transcript' },
{ Variation => 'Allele' },
);

my $tt = Template->new({
    FILTERS => {
        'beautify_species' => \&beautify_species_filter
    }
});

#print $_."-protein-fasta,\\\n" foreach @species;


foreach my $type (qw/acexml/){

    if( $type eq 'acexml' ){
        my $template = &gen_ace_template;
        my $result;
        foreach my $map (@ace_species){
            $tt->process(\$template, {
                acetype => [keys %$map]->[0],
                imtype => $map->{ [keys %$map]->[0] }
                }, \$result) || die $tt->error;
        }
        print $result;
    }else{

        foreach my $species (@species){
            my $result;
            my $template;
            
            switch( $type ){
                case 'genomic' {
                    $template = &gen_genomic_template;
                    $tt->process(\$template, {
                        species => $species, 
                        typeMapping => -d "$datadir/wormbase-gff3/$species/mapping"
                        }, \$result) || die $tt->error; 
                    print $result;
                }
                case 'protein' {
                    $template = &gen_protein_template;
                    $tt->process(\$template, {
                        species => $species,
                        typeMapping => -d "$datadir/wormbase-gff3/$species/mapping"
                        }, \$result) || die $tt->error; 
                    print $result;
                }
                case 'gff3' {
                    $template = &gen_gff3_template;
                    foreach my $subtype (qw/gene mrna cds/){
                        $tt->process(\$template, {
                            species => $species,
                            type => $subtype,
                            typeMapping => -d "$datadir/wormbase-gff3/$species/mapping"
                            }, \$result) || die $tt->error; 
                    
                    }
                    print $result;
                }
            }
        }

    }

}

sub beautify_species_filter{
    my $species = shift;
    $species =~ s/([^_])_([^_])([^_]+)/uc($1).'. '.uc($2).$3/e;
    return $species;
}

# species
sub gen_genomic_template{
<<TEMPLATE;
    <source name="[% species %]-genomic-fasta" type="fasta">
        <property name="fasta.taxonId"         value="&[% species %]_taxon_id;"/>
        <property name="fasta.className"       value="org.intermine.model.bio.Chromosome"/>
        <property name="fasta.classAttribute"  value="primaryIdentifier"/>
        <property name="fasta.dataSourceName"  value="WormBase"/>
        <property name="fasta.dataSetTitle"    value="WormBase [% species %] chromosome sequence (Fasta)"/> 
        <property name="fasta.sequenceType"    value="dna"/>
        <property name="fasta.includes"        value="*.fa*"/>
        <property name="src.data.dir"          location="&datadir;/fasta/[% species %]/genomic/" />
    </source> 

TEMPLATE
}


sub gen_protein_template{
<<TEMPLATE;
    <source name="[% species %]-protein-fasta" type="fasta">
        <property name="fasta.taxonId"          value="&[% species %]_taxon_id;"/>
        <property name="fasta.className"        value="org.intermine.model.bio.Protein"/>
        <property name="fasta.classAttribute"   value="primaryIdentifier"/>
        <property name="fasta.dataSourceName"   value="WormBase"/>
        <property name="fasta.dataSetTitle"     value="WormBase [% FILTER beautify_species %] [% species %] [% END %] protein sequence (Fasta)"/>
        <property name="fasta.sequenceType"     value="protein"/>
        <property name="src.data.dir"           location="&datadir;/fasta/[% species %]/proteins/prepped" />
        <property name="fasta.includes"         value="*.fa*"/> 
        <property name="fasta.loaderClassName"
            value="org.intermine.bio.dataconversion.WormBaseProteinFastaLoaderTask"/>
        <property name="fasta.PIDPrefix"        value="&[% species %]_protein_prefix;"/>
    </source>

TEMPLATE
}

# species, type, typeMapping = 1 or 0
sub gen_gff3_template{
<<TEMPLATE;
    <source name="[% species %]-gff3-[% type %]" type="wormbase-gff3-core" dump="false">
        <property name="gff3.taxonId"           value="&[% species %]_taxon_id;"/>
        <property name="gff3.seqDataSourceName" value="WormBase"/>
        <property name="gff3.dataSourceName"    value="WormBase"/>
        <property name="gff3.seqClsName"        value="Chromosome"/>
        <property name="gff3.dataSetTitle"      value="[% FILTER beautify_species %] [% species %] [% END %] genomic annotations (GFF3 [% type %])"/>
        <property name="src.data.dir"           location="&datadir;/wormbase-gff3/[% species %]/final"/> 
        <property name="gff3.allowedClasses"    value="[% type %]"/>
        <property name="gff3.typeMappingFile"   value="&build_config;/wormbase-gff3/typeMapping.tab"/>[% IF IDMapping == 1 %]
        <property name="gff3.IDMappingFile"     value="&datadir;/wormbase-gff3/[% species %]/mapping/id_mapping.tab"/>[% END %]
    </source>

TEMPLATE
}

# acetype, imtype
sub gen_ace_template{
<<TEMPLATE;
    <!-- [% imtype %] -->
    <source name="wb-acedb-[% acetype %]" type="wormbase-acedb" dump="false">
        <property name="source.class" value="[% imtype %]"/>
        <property name="src.data.dir" location="&datadir;/wormbase-acedb/[% acetype %]/final" />
        <property name="mapping.file" 
            value="&build_config;/wormbase-acedb/[% acetype %]_mapping.properties" />
        <property name="key.file"
            value="../../bio/sources/wormbase-acedb/resources/wormbase-acedb_keys.properties"/>
        <property name="rejects.file" 
            value="&build_output;/wormbase-acedb-[% acetype %]-rejects.xml"/>
        <property name="data.set" value="AceDB XML ([% acetype %])"/>
    </source>

TEMPLATE
}

