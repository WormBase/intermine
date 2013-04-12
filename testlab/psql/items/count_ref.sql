SELECT 
	DISTINCT
	count(a2.intermine_value) 
	
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
	--AND i2.identifier = '4_192689'
	--AND a.intermine_value = 'JA:JA51573'
	--AND a.intermine_value = 'JA:JA65336'
GROUP BY i1.identifier


;