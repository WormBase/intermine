#!/usr/bin/perl

######################################################################
# This is an automatically generated script to run your query.
# To use it you will require the InterMine Perl client libraries.
# These can be installed from CPAN, using your preferred client, eg:
#
#    sudo cpan Webservice::InterMine
#
# For help using these modules, please see these resources:
#
#  * http://search.cpan.org/perldoc?Webservice::InterMine
#       - API reference
#  * http://search.cpan.org/perldoc?Webservice::InterMine::Cookbook
#       - A How-To manual
#  * http://www.intermine.org/wiki/PerlWebServiceAPI
#       - General Usage
#  * http://www.intermine.org/wiki/WebService
#       - Reference documentation for the underlying REST API
#
######################################################################

use strict;
use warnings;
use Webservice::InterMine ('www.flymine.org/query');

my $field = 'organism';
my $clazz = 'Gene';
my $model_file_path = 'model.xml';

# Print unicode to standard out
binmode(STDOUT, 'utf8');
# Silence warnings when printing null fields
no warnings ('uninitialized');

my $model = InterMine::Model->new(file => $model_file_path);
my $cd = $model->get_classdescriptor_by_name($clazz);

my $service = Webservice::InterMine->get_service;
my $query = $service->new_query(class => $clazz);

#print $_,"\n" foreach $cd->attributes; # DELETE
#die;

foreach my $field ( $field ){
    my $fd = $cd->get_field_by_name($field);
#    print $fd,"\n";
    
    die "ERROR: cannot find ".$field." in model" unless $fd;
    
    if(     $fd->isa('InterMine::Model::Attribute')){
        print "$field is an attribute\n";
        #$query->select($clazz.'.'.$field)->where($field => { isnt => undef});
        
    }elsif( $fd->isa('InterMine::Model::Reference') && 
            !$fd->isa('InterMine::Model::Collection')){
        print "$field is a reference\n";
        #$query->select('primaryIdentifier')->where($field => { isnt => undef} );
    }
    if($fd->isa('InterMine::Model::Collection')){
        print "$field is a collection\n";
    }    
}


#print $query->count(),"\n";