# Paulo Nuin February 2018

import pandas as pd
from sqlalchemy import create_engine

db_string = "postgres://postgres:interwormmine@localhost/intermine_dev_18_269_4"
db = create_engine(db_string)
connection = db.connect()

def check_mrna_table():

    all = []
    result = connection.execute('select * from mrna')
    for row in result:
        all.append(row)

    return(all)

if __name__ == '__main__':

    print(len(check_mrna_table()))

    print('Reading mRNA list to remove')
    to_remove = open('to_remove.txt').read().splitlines()

    for i in to_remove:
        print(i)
        connection.execute("DELETE FROM mrna WHERE  primaryidentifier = '%s'" % (i))
