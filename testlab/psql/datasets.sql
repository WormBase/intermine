\set sf_pid '\''Transcript:Y110A7A.10.2'\'' -- how to set a quoted variable
SELECT
	b.primaryidentifier, 
	d.name 
FROM 
	bioentity b
	JOIN bioentitiesdatasets bd ON b.id = bd.bioentities
	JOIN dataset d ON bd.datasets = d.id
;