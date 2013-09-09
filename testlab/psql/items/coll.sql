-- joins items and their collections
SELECT 
	i1.id, 
	i1.identifier, 
	i1.classname, 
	a.intermine_value AS pID,
	rl.name AS reflist, 
	i2.id,
	rl.refids,
	a2.intermine_value AS pID
	
FROM 
	item i1
	JOIN referencelist rl ON i1.id = rl.itemid
	JOIN item i2 ON rl.refids = i2.identifier
	JOIN attribute a ON a.itemid = i1.id
	JOIN attribute a2 ON a2.itemid = i2.id
WHERE 
		rl.name = 'CDSs'
	AND a.name = 'primaryIdentifier'
	AND a2.name = 'primaryIdentifier'
	AND i1.identifier = '11_409042'
	--AND a.intermine_value = 'JA:JA51573'
	--AND a.intermine_value = 'JA:JA65336'
;