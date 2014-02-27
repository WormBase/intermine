#!/usr/bin/perl

use strict;
use XML::Simple qw(:strict);
use Data::Dumper;
use feature "state";

my (
	$infile_path,
	$outfile_path, 
	$whitelist_path, 
	$rejects_path
) = @ARGV;

die &usage if (scalar @ARGV) != 4;

open( my $infile, $infile_path) or die $infile_path.": $!";
open( my $outfile, '>'.$outfile_path) or die $outfile_path.": $!";
open( my $whitelist, $whitelist_path) or die $whitelist_path.": $!";
open( my $rejectsfile, '>'.$rejects_path) or die $rejects_path.": $!";

my %species_wl = ();
while(<$whitelist>){
	chomp;
	$species_wl{$_} = 1;
}
my $passing_species = join('|', keys %species_wl);

my $xs = XML::Simple->new( 
	ForceArray 	=> 1, 
	KeyAttr 	=> {},
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
	state $count = 0;
	$count++;
	print $count."\n" if $count % 1000 == 0; 
	
	my $xml_chunk = shift;
	#print "[[processing item]]\n";
	
	return if grep(/^<Protein>MSP:/, $xml_chunk);
	
	return unless grep(/<Species>($passing_species)<\/Species>/, $xml_chunk);

	#print Dumper $species;
	#print $id."\n";
	print $outfile $xml_chunk."\n";
}

sub status_report{
	my $count = shift;
	state $oom = 1; # order of magnitude
	if($count < $oom*10){
		print $count."\n";
	}elsif($count = $oom){
		$oom++;
		print $count."\n";
	}
}

sub usage{
<<"USAGE";
Usage: perl $0 <infile> <outfile> <whitelist> <rejectsfile>
	whitelist: list of allowed species
	rejects: file to dump XML records rejected by the parser
USAGE
}
