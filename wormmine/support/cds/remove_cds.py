# Paulo Nuin March 2018

import pandas as pd
from sqlalchemy import create_engine

db_string = "postgres://postgres:interwormmine@localhost/intermine_prod_265_185"
db = create_engine(db_string)
connection = db.connect()


if __name__ == '__main__':

    transcript_ids = open('to_remove_cds.txt').read().splitlines()

    for i in transcript_ids:
        print(i)
        connection.execute("DELETE from cds WHERE primaryidentifier = '%s'" % (i))
