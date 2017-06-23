# Paulo Nuin June 2017

import sys
import os
import re
from lxml import etree
import click

@click.command()
@click.option('--wrap', default=True, help='Wrap AceDB XML in a root tag')
@click.option('--unwrap', default=False, help='Unwrap AceDB from root tag')

def wrap_xml(xml_file, xml_class):
	
	xml_contents = open(xml_file).read().splitlines()
	xml_class = xml_class + 's'
	xml_eof = 0
	while xml_eof < len(xml_contents):
		try:
			xml_class = re.search('<' + re.escape(xml_class) + '>', xml_contents[xml_eof], re.IGNORECASE).group(0)[:-1]
			return 'Already wrapped'
		except:
			xml_eof += 1
		if xml_eof > 100:
			break

	xml_file = open(xml_file, 'r+')
	xml_text = xml_file.read()
	xml_file.seek(0, 0)
	xml_file.write('<' + xml_class + '>\n' + xml_text + '</' + xml_class + '>\n')
	xml_file.close()

	return 'done'

def unwrap_xml(xml_file, xml_class):

	xml_contents = open(xml_file).read().splitlines()
	# xml_class = os.path.basename(xml_file).replace('.xml', 's')

	xml_eof = 0
	while xml_eof < len(xml_contents):
		if xml_contents[xml_eof].find('<' + xml_class + 's>') >= 0:
			del xml_contents[xml_eof]
			print 'found', xml_eof
		elif xml_contents[xml_eof].find('</' + xml_class + 's>') >= 0:
			del xml_contents[xml_eof]
			print 'found', xml_eof
		xml_eof += 1

	os.remove(xml_file)

	xml_output = open(xml_file, 'w')
	xml_output.write('\n'.join(xml_contents))
	xml_output.close()


if __name__ == '__main__':

	xml_file = sys.argv[1]
	xml_class = sys.argv[2]

	print wrap_xml(xml_file, xml_class)
	# unwrap_xml(xml_file, xml_class)

