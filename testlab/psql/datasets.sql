SELECT 
	b.*, 
	d.name 
FROM 
	bioentity b
	JOIN bioentitiesdatasets bd ON b.id = bd.bioentities
	JOIN dataset d ON bd.datasets = d.id
WHERE
	b.class = 'org.intermine.model.bio.CDS' 
	--b.primaryidentifier = 'CDS:B0025.1a'
	--d.id = 4000000
;