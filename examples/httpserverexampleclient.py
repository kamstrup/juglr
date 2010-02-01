#! /usr/bin/python

import httplib
import urllib2

for i in range(100):	
	#conn = httplib.HTTPConnection("localhost", 4567)
	#conn.request("POST", "/actor/calc/", u'{"isPrime" : %s}' % i)
	#print conn.getresponse().read()
	#conn.close()
	resp = urllib2.urlopen("http://localhost:4567/actor/calc/", '{"isPrime":%s}' % i)
	print resp.read()
