# Paulo Nuin May 2017
# Simple script that 


import sys
import os
from lxml import etree



def extract_xpaths(xml_file):

	root = etree.fromstring(open(xml_file).read())
	tree = etree.ElementTree(root)


	xpaths = []
	for node in tree.iter():
		xpath = tree.getpath(node)
		xpaths.append(tree.getpath(node)) 

	uxpaths = []
	[uxpaths.append(item) for item in xpaths if item not in uxpaths]
	print len(uxpaths)
	return uxpaths


if __name__ == '__main__':

	# xml_file = sys.argv[1]/
	xml_file = '../../../../WS259-test-data/Expr_pattern.xml'
	for i in extract_xpaths(xml_file):
		print i
