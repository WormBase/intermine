-- Shows gene->trnascript->cds->protein paths
SELECT count(*) FROM 
(
	SELECT 
		protein.primaryIdentifier, count(*)
	FROM
		gene 
		INNER JOIN transcript ON gene.id = transcript.geneid
		INNER JOIN cdsstranscripts 
			ON transcript.id = cdsstranscripts.transcripts
		INNER JOIN cds ON cdsstranscripts.cdss = cds.id
		INNER JOIN protein ON cds.proteinid = protein.id
	GROUP BY protein.primaryIdentifier
) AS t1
;

