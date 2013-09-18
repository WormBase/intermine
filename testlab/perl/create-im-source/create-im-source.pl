#!/usr/bin/perl

use strict;
use Template;

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
<!ENTITY           c_sp5_taxon_id "unknown">
<!ENTITY          c_sp11_taxon_id "unknown">
<!ENTITY h_bacteriophora_taxon_id "37862">
<!ENTITY     h_contortus_taxon_id "6289">
<!ENTITY         m_hapla_taxon_id "6305">
<!ENTITY     m_incognita_taxon_id "6306">
<!ENTITY     p_pacificus_taxon_id "54126">
<!ENTITY         s_ratti_taxon_id "34506">
<!ENTITY      t_spiralis_taxon_id "6334">
SPECIES

my @species = $speciesraw =~ /\s(\w+)_taxon_id/mg;

my $tt = Template->new();

#print $_."-protein-fasta,\\\n" foreach @species;

foreach my $species (@species){
	my $result;

    #my $template = &gen_protein_template;
    my $template = &gen_genomic_template;
	$tt->process(\$template, {species => $species}, \$result) || die $tt->error; 
    print $result;

}

# species
sub gen_genomic_template{
<<TEMPLATE;
	<source name="[% species %]-genomic-fasta" type="fasta">
		<property name="fasta.taxonId"         value="&[% species %]_taxon_id;"/>
		<property name="fasta.className"       value="org.intermine.model.bio.Chromosome"/>
		<property name="fasta.classAttribute"  value="primaryIdentifier"/>
		<property name="fasta.dataSourceName"  value="WormBase FTP [% species %] genomic fasta"/>
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
		<property name="fasta.taxonId"         	value="&[% species %]_taxon_id;"/>
		<property name="fasta.className"       	value="org.intermine.model.bio.Protein"/>
		<property name="fasta.classAttribute"  	value="primaryIdentifier"/>
		<property name="fasta.dataSourceName"  	value="WormBase FTP [% species %] protein fasta"/>
		<property name="fasta.dataSetTitle"    	value="WormBase [% species %] protein sequence (Fasta)"/>
		<property name="fasta.sequenceType"    	value="protein"/>
		<property name="src.data.dir"          	location="&datadir;/fasta/[% species %]/proteins/prepped" />
		<property name="fasta.includes"        	value="*.fa*"/> 
		<property name="fasta.loaderClassName"
			value="org.intermine.bio.dataconversion.WormBaseProteinFastaLoaderTask"/>
		<property name="fasta.PIDPrefix"		value="&[% species %]_protein_prefix;"/>
	</source>

TEMPLATE
}