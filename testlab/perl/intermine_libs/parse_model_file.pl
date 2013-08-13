#!/usr/bin/perl

use strict;
use InterMine::Model;
use Data::Dumper;

my $model_file = "model.xml";
my $model = InterMine::Model->new(file => $model_file);

my $cd = $model->get_classdescriptor_by_name("Gene");
#my $fd = $cd->get_field_by_name("transcripts");

my @ancestors = $cd->get_ancestors();
print join(",", @ancestors),"\n";

#my @fields = $cd->fields;
#my @fields = ($cd->attributes,$cd->references,$cd->collections);
my @fields = ($cd->collections);

for my $fd ( @fields ){
    print $fd,": ";
    if($fd){
        if($fd->isa('InterMine::Model::Attribute')){
            print "Attribute";
        }
        if($fd->isa('InterMine::Model::Reference') && !$fd->isa('InterMine::Model::Collection')){
            print "Reference - ";
            if($fd->is_one_to_0){
                print "is_one_to_0";
            }
            if($fd->is_many_to_one){
                print "is_many_to_one";
            }
            if($fd->is_many_to_0){
                print "is_many_to_0";
            }

        }
        if($fd->isa('InterMine::Model::Collection')){
            print "Collection - ";
            if($fd->is_one_to_many){
                print "is_one_to_many";
            }
            if($fd->is_one_to_0){
                print "is_one_to_0";
            }
            if($fd->is_many_to_many){
                print "is_many_to_many";
                my $rr =  $fd->reverse_reference->referenced_type_name;
                print "[",&pluralize($rr),"]";
            }
            if($fd->is_many_to_one){
                print "is_many_to_one";
            }
            if($fd->is_many_to_0){
                print "is_many_to_0";
            }
            
        }
    }else{
        print "cannot be found";
    }
    
    print "\n";
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

if(0){
my $rd;
if($rd->is_one_to_many){
    print "is_one_to_many\n";
}
if($rd->is_one_to_0){
    print "is_one_to_0\n";
}
}

