Sample gff3 source:

     <source name="celegans-gff3-gene" type="wormbase-gff3-core" dump="true">
         <property name="gff3.taxonId"           value="&c_elegans_taxon_id;"/>
         <property name="gff3.seqDataSourceName" value="WormBase"/>
         <property name="gff3.dataSourceName"    value="WormBase"/>
         <property name="gff3.seqClsName"        value="Chromosome"/>
         <property name="gff3.dataSetTitle"      value="WormBase C. elegans genomic annotations"/>
         <!--<property name="src.data.dir"           location="&datadir;/c_elegans_gff3"/>-->
	 <property name="src.data.dir"           location="&datadir;/test_gff3"/> 
        
	 <!-- 
	 Only GFF3 records with a third column matching this value 
	 are processed.  Case insensitive.
	 gene, mrna, cds, exon
	 -->
	 <property name="gff3.allowedClasses" 	 value="gene"/>
	 
	 <!-- 
	 This file maps IDs from GFF3 into a type compatible with other 
	 data sources
	 Format: key \t value. All other columns ignored 
	 -->
	 <property name="gff3.IDMappingFile"	 value="&datadir;/test_gff3/c_elegans.WS235.geneIDs.wb-gff3.tab"/> 
	 
	 <!-- 
	 Maps type names from GFF3 into recognized InterMine class names
	 Format: key \t value. All other columns ignored 
	 -->
	 <property name="gff3.typeMappingFile"	 value="&datadir;/test_gff3/typeMapping.tab"/>

	<!-- 
	Maps type names from GFF3 into recognized InterMine class names
	Format: key \t value. All other columns ignored 
	-->
	<property name="gff3.typeMappingFile"    value="&datadir;/wormbase-gff3/mapping/typeMapping.tab"/>
    </source>


Sample wormbase-acedb source:
	<source name="wb-acedb-gene" type="wormbase-acedb" dump="false">
		<property name="src.data.dir" location="&datadir;/wormbase-acedb/gene/XML" />
		<!--
			This file maps intermine data classes to an XPath query.
		-->
		<property name="mapping.file"	
			value="&datadir;/wormbase-acedb/gene/mapping/wormbase-acedb-gene.properties"/> 
		<!-- 
		File specifying the primary keys to use for this source
		Usually this source's keys.properties file.  Keys must end in ".key"
		-->
		<property name="key.file" 
			value="../../bio/sources/wormbase-acedb/resources/wormbase-acedb_keys.properties"/>
		
		<!-- 
		This property specifies the intermine class type this source loads.
		Must use proper CamelCase
		-->
		<property name="source.class" value="Gene"/>
		
		<!--
		Optional.
		This specifies where the XML rejects file should go.  This file stores all XML records
		which could not be parsed
		-->
		<property name="rejects.file" 
			value="&datadir;/wormbase-acedb/gene/wormbase-acedb-gene-rejects.xml"/>
			
		<!-- Debug mode, prints everything.  Optional -->
		<property name="debug" value="true" />
		
		<property name="data.set" value="AceDB XML (Gene)"/>
	</source> 


