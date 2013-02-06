-- Shows gene->trnascript->cds paths
SELECT 
	gene.primaryIdentifier,
	gene.length,
	gene.symbol,
	transcript.primaryIdentifier,
	cds.primaryIdentifier
FROM
	gene 
	INNER JOIN transcript ON gene.id = transcript.geneid
--	transcript
	INNER JOIN cds ON transcript.id = cds.transcriptid
;



