package org.intermine.bio.dataconversion;

import org.biojava.bio.seq.Sequence;
import org.intermine.model.bio.BioEntity;
import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.Organism;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.model.bio.Protein;


public class WormBaseProteinFastaLoaderTask extends FastaLoaderTask {
	
	String proteinPrefix = "Protein:";
	
    /**
     * Do any extra processing needed for this record (extra attributes, objects, references etc.)
     * This method is called before the new objects are stored
     * @param bioJavaSequence the BioJava Sequence
     * @param flymineSequence the FlyMine Sequence
     * @param bioEntity the object that references the flymineSequence
     * @param organism the Organism object for the new InterMineObject
     * @param dataSet the DataSet object
     * @throws ObjectStoreException if a store() fails during processing
     */
	@Override
	protected void  extraProcessing(Sequence bioJavaSequence, org.intermine.model.bio.Sequence
            flymineSequence, BioEntity bioEntity, Organism organism, DataSet dataSet)
        throws ObjectStoreException {
        String name = bioJavaSequence.getName();
        String[] ids = name.split("\\|");
        
       	
       	bioEntity.setFieldValue("secondaryIdentifier", ids[1]);
        
       	if(bioEntity instanceof Protein){
       		bioEntity.setFieldValue("primaryIdentifier", proteinPrefix + this.PIDPrefix + ids[0]);
       		bioEntity.setFieldValue("primaryAccession", this.PIDPrefix + ids[0]);
       	}else{
       		bioEntity.setFieldValue("primaryIdentifier", this.PIDPrefix + ids[0]);
       	}

    }

	
}