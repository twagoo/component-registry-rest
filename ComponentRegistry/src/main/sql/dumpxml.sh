#!/bin/sh

####################################################################################
# This script dumps all xml content from the component registry
# database into separate files.
#
# THIS WILL INCLUDE PRIVATE AND DELETED COMPONENTS AND PROFILES
#
# Use with care...
#
####################################################################################

DB_NAME=component_registry
DB_USER=compreg
OUTPUTDIR=/tmp/compregxml

mkdir ${OUTPUTDIR}

echo getting xml from database...

psql -U ${DB_USER} ${DB_NAME} << EOF
\copy xml_content (content) TO '/tmp/xmls.tmp'
EOF

echo processing into files

# sed to strip out explicit newline characters
# split to move into separate files
cat /tmp/xmls.tmp | sed -e 's/\\n//g' | split -a 5 -l 1 - ${OUTPUTDIR}/compreg.xml.

