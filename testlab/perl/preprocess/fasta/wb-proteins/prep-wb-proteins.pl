#!/usr/bin/perl

use strict;

my ($infile_path,$outfile_path) = @ARGV;

open( my $infile, $infile_path) or die $infile_path.": $!";
open( my $outfile, '>'.$outfile_path) or die $outfile_path.": $!";

while(<$infile>){
	s[>(\S+).*$][>Protein:$1];
	print $outfile $_;
}