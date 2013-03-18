-- Shows gene->trnascript->cds paths
SELECT 
	cds.id AS cds_id,
	cds.primaryIdentifier,
	protein.primaryIdentifier AS prot_id,
	protein.primaryIdentifier
FROM
	cds 
	INNER JOIN protein ON cds.proteinid = protein.id
;



