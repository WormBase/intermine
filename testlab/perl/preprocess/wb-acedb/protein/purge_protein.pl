#!/usr/bin/perl

use strict;
use XML::Simple qw(:strict);
use Data::Dumper;

my ($infile_path,$outfile_path, $rootname) = @ARGV;

die &usage if (scalar @ARGV) != 2;

open( my $infile, $infile_path) or die $infile_path.": $!";
open( my $outfile, '>'.$outfile_path) or die $outfile_path.": $!";

my $xs = XML::Simple->new( 
	ForceArray 	=> 1, 
	KeyAttr 	=> {},
	KeepRoot 	=> 1,
	NoSort		=> 1
);
my $xml_chunk = "";
while(<$infile>){
	if( /^\s*$/ ){
		&process($xml_chunk) if length($xml_chunk) > 0;
		$xml_chunk = "";
		next;
	}
	$xml_chunk .= $_;
}
&process($xml_chunk) if length($xml_chunk) > 0;


sub process{
	my $xml_chunk = shift;
	print "[[processing item]]\n";
	my $ref = $xs->XMLin($xml_chunk);
	print Dumper $ref;
	print $xs->XMLout($ref);
}

sub usage{
<<"USAGE";
Usage: perl $0 <infile> <outfile> \n
USAGE
}