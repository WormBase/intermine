SELECT 
	d.name 
FROM 
	bioentity b
	JOIN bioentitiesdatasets bd ON b.id = bd.bioentities
	JOIN dataset d ON bd.datasets = d.id
WHERE
	b.class = 'org.intermine.model.bio.CDS' AND
	--b.primaryidentifier = 'CDS:B0025.1a'
	b.id = 5000002
;