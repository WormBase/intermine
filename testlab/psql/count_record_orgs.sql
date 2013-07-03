select name, orgc.count from organism inner join (select organismid,count(*) from bioentity group by organismid) as orgc on orgc.organismid = organism.id;
