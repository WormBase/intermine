# Paulo Nuin Nov 2018

import sys
import requests


def get_data(xml_query):

    url = 'http://www.ensembl.org/biomart/martservice?query='

    req = requests.get(url + xml_query, stream=True)
    for line in req.iter_lines():
        print(line.decode('ascii'))

if __name__ == '__main__':

    xml_query = open(sys.argv[1]).read().replace('\n', '').replace('\t', '')

    get_data(xml_query)
