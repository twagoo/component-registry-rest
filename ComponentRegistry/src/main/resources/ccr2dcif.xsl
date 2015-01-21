<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="xs rdf openskos skos"
	version="2.0"
	xmlns:dcif="http://www.isocat.org/ns/dcif"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:openskos="http://openskos.org/xmlns#"
	xmlns:skos="http://www.w3.org/2004/02/skos/core#"
>
	
	<xsl:output method="xml" encoding="UTF-8"/>
	
	<xsl:template match="rdf:RDF">
		<dcif:dataCategorySelection dcif-version="1.3" name="search">
			<dcif:globalInformation>CLARIN Concept Registry, Meertens Institute, Amsterdam, The Netherlands</dcif:globalInformation>
			<xsl:apply-templates/>
		</dcif:dataCategorySelection>
	</xsl:template>
	
	<xsl:template match="skos:Concept">
		<dcif:dataCategory
			definition="{(skos:definition[@xml:lang='en'])[1]}"
			identifier="{(skos:notation)[1]}"
			name="{(skos:prefLabel[@xml:lang='en'])[1]}"
			owner="CLARIN"
			pid="{@rdf:resource}"
			type="concept"
			version="1:0">
		</dcif:dataCategory>
	</xsl:template>
	
</xsl:stylesheet>