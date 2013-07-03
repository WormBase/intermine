\set imclass allele
\set attr intermine_method

SELECT t1.:attr, counts.count
from :imclass as t1 
join (select :attr,count(*) from :imclass group by :attr having count(*) > 1) as counts
    ON t1.:attr = counts.:attr
;
