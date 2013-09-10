#!/usr/bin/perl

use strict;
use warnings;
#use Webservice::InterMine ('http://www.wormbase.org/tools/wormmine');
use Webservice::InterMine ('http://206.108.125.174:8080/wormmine/service');

#my $clazz = 'Gene';

die &usage unless scalar @ARGV >= 3;

my (
    $outfile_path, 
    $ace_map_path,
    @mapping_file_paths,
) = @ARGV;

open( my $ace_map, $ace_map_path ) or die "$!";
my %ace_to_im = ();
<$ace_map>;
while( <$ace_map> ){
    next if /^#|^\s*$/;
    
    my ($key, $value) = /(.*?)\s*=\s*(\S.*)/;
    $ace_to_im{$key} = $value;
}
#print join("\n",@mapped_fields),"\n";
close($ace_map);

#my $mapping_file_path = $mapping_file_dir; # REMOVE 
open( my $outfile, '>'.$outfile_path ) or die "$!";


foreach my $mapping_file_path ( @mapping_file_paths ){

    my ($aceclazz) = $mapping_file_path =~ /(\S+)_mapping.properties/;
    my $clazz = $ace_to_im{$aceclazz} ? $ace_to_im{$aceclazz} : $aceclazz;
    
    printf $outfile ("%s%s - %s\n",
      $aceclazz,
      $ace_to_im{$aceclazz} ? $ace_to_im{$aceclazz} : '',
      $mapping_file_path);

    open( my $infile, $mapping_file_path ) or die "$!";
    my @mapped_fields;
    <$infile>;
    while( <$infile> ){
        next if /^#|^\s*$/;
        
        my ($key, $value) = /(.*?)\s*=\s*(\S.*)/;
        push @mapped_fields, $key;
    }
    #print join("\n",@mapped_fields),"\n";
    close($infile);

    my $service = Webservice::InterMine->get_service;

    #print $_,"\n" foreach $cd->collections;

    foreach my $field ( @mapped_fields ){
        my $query = $service->new_query(class => $clazz);
        $query->select('primaryIdentifier')->where($field => { isnt => undef} );
        printf $outfile ("%-20s - %s\n", $query->count() > 0 ? 'OK' : 'NO RECORDS FOUND', $field);
    }
    print $outfile "\n\n";

}

sub usage{
<<USAGE;
Usage:

    perl $0 <output file> <ace map file> <mapping file path> [... <mapping file path>]

Generates basic "presence" acceptance tests got a given wormbase-acedb mapping file.
Covers attributes and references. 

The ace map file maps ace classes to intermine classes.  Example line:
    Anatomy_term    = AnatomyTerm

Puts results in a single output file.
USAGE
}
