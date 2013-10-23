#!/usr/bin/perl

use strict;
use InterMine::Model;
use Template;

die &usage unless scalar @ARGV == 4;

my (
    $mapping_file_path, 
    $outfile_path, 
    $model_file_path, 
    $clazz
) = @ARGV;

open( my $outfile, '>'.$outfile_path ) or die "$!";

# populate @mapped_fields
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

my $model = InterMine::Model->new(file => $model_file_path);

my $cd = $model->get_classdescriptor_by_name($clazz);

my $tt = Template->new();

foreach my $field ( @mapped_fields ){
    my $fd = $cd->get_field_by_name($field);
#    print $fd,"\n";
    
    die "ERROR: cannot find ".$field." in model" unless $fd;
    
    if(     $fd->isa('InterMine::Model::Attribute')){
#        print $outfile &gen_test(&attr_template, {
#            field => $field,
#            class => $clazz});
    }elsif( $fd->isa('InterMine::Model::Reference') && 
            !$fd->isa('InterMine::Model::Collection')){
#        print $outfile &gen_test(&ref_template, {
#            field => $field,
#            class => $clazz});
    }
    if($fd->isa('InterMine::Model::Collection')){
        if($fd->is_one_to_many){
#            print "is_one_to_many";
            print $outfile &gen_test(&ref_template, {
                field => $fd->reverse_reference,
                class => $fd->referenced_type_name,
                localfield => $field,
                localclass => $clazz});
            
        }
        if($fd->is_many_to_many){
            # this case not stored as expected
            my $rrr = $fd->reverse_reference->referenced_type_name;
            my $r = $fd->referenced_type_name;
            my $tablename = join('',
                sort(pluralize($rrr), pluralize($r)));
            #print $outfile &gen_test(&col_nn_template,
            #    $field, $tablename);
        }
        if($fd->is_many_to_0){
#            print "is_many_to_0";
        }
        
    }    
}

sub attr_template{
<<TEMPLATE;
some-results {
    sql: SELECT * FROM [% class %] WHERE [% field %] IS NOT NULL LIMIT 1
    note: [% class %].[% field %]
}

TEMPLATE
}

# params: (template, fieldname, tablename)
sub gen_test{
    my ($template, $vars) = @_;
    my $result;
    $tt->process(\$template, $vars, \$result) || die $tt->error;
    return $result;
}

sub ref_template{
<<TEMPLATE;
some-results {
    sql: SELECT * FROM [% class %] WHERE [% field %]id IS NOT NULL LIMIT 1
    note: [% 
        IF localclass; localclass; 
        ELSE; class; 
        END%].[% 
        IF localfield;
        localfield;
        ELSE;
        field;
        END %]
}

TEMPLATE
}

sub col_nn_template{
<<TEMPLATE;
some-results {
    sql: SELECT * FROM [% tablename %] LIMIT 1
    note: [% class %].[% field %]
}

TEMPLATE
}

sub pluralize{
    my $word = shift @_;
    if( $word =~ /y$/ ){
        $word =~ s/y$/ies/;
        return $word;
    }else{
        return $word.'s';
    }
}


sub usage{
<<USAGE;
Usage:

    perl $0 <mapping file> <output file> <model file> <class>

Generates basic "presence" acceptance tests got a given wormbase-acedb mapping file.
Covers attributes and references. 

Requires an intermine model file, which can be found at <INTERMINE URL ROOT>/service/model

Sample:

some-results {
    sql: SELECT * FROM gene WHERE secondaryidentifier IS NOT NULL LIMIT 1
    note: gene.secondaryidentifier
}
USAGE
}