SELECT name, t1.taxonid, counts.count
from organism as t1 
join (select taxonid,count(*) from organism group by taxonid having count(*) > 1) as counts
    ON t1.taxonid = counts.taxonid
;