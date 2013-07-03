-- Shows gene->trnascript->cds paths
SELECT 
	cds.primaryIdentifier,
	protein.primaryIdentifier
FROM
	cds 
	INNER JOIN protein ON cds.proteinid = protein.id
;



