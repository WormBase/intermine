# Paulo Nuin February 2018

import pandas as pd
from sqlalchemy import create_engine

db_string = "postgres://postgres:interwormmine@localhost/intermine_prod_264_185_3"
db = create_engine(db_string)
connection = db.connect()

def check_transcript_table():

    all = []
    result = connection.execute('select * from transcript')
    for row in result:
        all.append(row)

    return(all)

if __name__ == '__main__':

    print(len(check_transcript_table()))

    print('Reading transcript list to remove')
    to_remove = open('to_remove.txt').read().splitlines()

    for i in to_remove:
        print(i)
        connection.execute("DELETE FROM transcript WHERE  primaryidentifier = '%s'" % (i))
