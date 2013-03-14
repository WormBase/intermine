-- Shows gene->trnascript->cds paths
SELECT 
	gene.primaryIdentifier,
	gene.length,
	gene.symbol,
	transcript.primaryIdentifier,
	cds.primaryIdentifier,
	protein.primaryIdentifier
FROM
	gene 
	INNER JOIN transcript ON gene.id = transcript.geneid
	INNER JOIN cdsstranscripts 
		ON transcript.id = cdsstranscripts.transcripts
	INNER JOIN cds ON cdsstranscripts.cdss = cds.id
	INNER JOIN protein ON cds.proteinid = protein.id
;



