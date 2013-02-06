--SELECT gene.primaryidentifier, transcript.primaryidentifier
--FROM gene INNER JOIN transcript ON transcript.geneid = gene.id
--WHERE transcript.primaryidentifier = 'Y12A6A.2'
--;

--SELECT transcript.primaryidentifier AS transcript, cds.primaryidentifier AS CDS
--FROM transcript INNER JOIN cds ON cds.transcriptid = transcript.id
--WHERE transcript.primaryidentifier = 'Y12A6A.2'
--;

--SELECT 
--	gene.primaryidentifier AS gene,
--	transcript.primaryidentifier AS transcript, 
--	cds.primaryidentifier AS CDS
--FROM gene 
--	INNER JOIN transcript ON transcript.geneid = gene.id 
--	INNER JOIN cds ON cds.transcriptid = transcript.id
--;

--  Shows all duplicate :imclass records
\set imclass cds
SELECT 
	:imclass.id,
	:imclass.primaryidentifier,
	location.intermine_start,
	location.intermine_end,
	:imclass.length,
	:imclass.chromosomeid,
	:imclass.class 
FROM
	(SELECT 
		primaryidentifier,count(*) 
	FROM :imclass 
	GROUP BY primaryidentifier 
	HAVING count(*) > 1) t1,
	:imclass INNER JOIN location 
		ON :imclass.chromosomelocationid = location.id
WHERE :imclass.primaryidentifier = t1.primaryidentifier
;



