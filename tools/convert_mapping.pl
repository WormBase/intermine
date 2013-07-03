#!/usr/bin/perl

=info
	Creates simple tab delimited file from key mapping csv
=cut

my (
	$infile, 
	$outfile,
	$key_col,
	$val_col
	) = @ARGV;

open( IN, $infile) or die $!;
open( OUT, '>'.$outfile) or die $!;

while(<IN>){
	chomp;
	my @line = split(/,/);
	print OUT join("\t",$line[$key_col-1],$line[$val_col-1])."\n";
}
