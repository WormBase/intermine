# Paulo Nuin March 2018

import pandas as pd
from sqlalchemy import create_engine

db_string = "postgres://postgres:interwormmine@localhost/intermine_dev_2_269_1"
db = create_engine(db_string)
connection = db.connect()


if __name__ == '__main__':

    gene_ids = open('to_remove.txt').read().splitlines()
    print(gene_ids)

    for i in gene_ids:
        print(i)
        connection.execute("DELETE from GENE WHERE primaryidentifier = '%s'" % (i))
