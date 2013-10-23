#!/usr/bin/perl

use strict;
use LWP::Simple;
use JSON qw( decode_json );

# Property prefix
my $prefix = 'bpid-';

die &usage unless scalar @ARGV == 2;

my ($url, $outfile_path) = @ARGV;

open( my $outfile, '>'.$outfile_path) or die "$!";

my $json = get( $url ) or die "<ERROR>: Could not get ".$url;
print "JSON retrieved from ".$url."\n";

my $decoded_json = decode_json( $json );
foreach my $species (keys %$decoded_json){
    # Gets first assembly listed
    my $bpid = $decoded_json->{$species}->{assemblies}->[0]->{bioproject};
    printf $outfile ("%s%-15s = %s\n",$prefix,$species,$bpid);
}
print $outfile_path." written\n";



sub usage{
<<USAGE;
$0
Parses WormBase assemblies json file into properties file mapping species to bio project id.

Sample assemblies file: 
ftp://ftp.wormbase.org/pub/wormbase/releases/current-production-release/species/ASSEMBLIES.WS238.json

Usage:
$0 <url> <outfile_path>
USAGE
}