\set sf_pid '\''Transcript:Y110A7A.10.2'\''
SELECT
	sf.primaryidentifier, location.* 
FROM 
	sequencefeature sf
	JOIN location ON sf.chromosomelocationid = location.id
WHERE 
	primaryidentifier = :sf_pid
;