-- joins items and their references
SELECT 
	i1.id, 
	i1.identifier, 
	i1.classname, 
	a.intermine_value AS pID,
	r.name AS reference, 
	i2.id,
	r.refid,
	a2.intermine_value AS pID
	
FROM 
	item i1
	JOIN reference r ON i1.id = r.itemid
	JOIN item i2 ON r.refid = i2.identifier
	JOIN attribute a ON a.itemid = i1.id
	JOIN attribute a2 ON a2.itemid = i2.id
WHERE 
        a.name = 'primaryIdentifier'
	AND a2.name = 'primaryIdentifier'
	AND i1.identifier = '11_409042'
	--AND a.intermine_value = 'JA:JA51573'
	--AND a.intermine_value = 'JA:JA65336'
;