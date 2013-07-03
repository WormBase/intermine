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
	JOIN transcript ON gene.id = transcript.geneid
	JOIN cdsstranscripts 
		ON transcript.id = cdsstranscripts.transcripts
	JOIN cds ON cdsstranscripts.cdss = cds.id
	JOIN protein ON cds.proteinid = protein.id
WHERE
	gene.primaryidentifier = 'WBGene00000001'
;



