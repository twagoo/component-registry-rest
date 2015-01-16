<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0">

    <xsl:template match="/">
        <!-- THIS IS A DUMMY IMPLEMENTATION OF CCR TO DCIF CONVERSION.
            TODO: Implement actual conversion
            -->
        <dcif:dataCategorySelection xmlns:dcif="http://www.isocat.org/ns/dcif" dcif-version="1.3">
            <dcif:globalInformation>Max Planck Institute for Psycholinguistics, Nijmegen, The
                Netherlands</dcif:globalInformation>
            <dcif:dataCategory pid="http://www.isocat.org/datcat/DC-4347" type="container">
                <!--This work by http://www.isocat.org/datcat/DC-4347 is licensed under a Creative Commons Attribution 4.0 International License (http://creativecommons.org/licenses/by/4.0/).-->
                <dcif:administrationInformationSection>
                    <dcif:administrationRecord>
                        <dcif:identifier>ArthurianFiction</dcif:identifier>
                        <dcif:version>1:0</dcif:version>
                        <dcif:registrationStatus>private</dcif:registrationStatus>
                        <dcif:justification>project container</dcif:justification>
                        <dcif:effectiveDate>2012-01-10</dcif:effectiveDate>
                        <dcif:creation>
                            <dcif:creationDate>2012-01-10</dcif:creationDate>
                            <dcif:changeDescription xml:lang="en">initial
                                creation</dcif:changeDescription>
                        </dcif:creation>
                        <dcif:lastChange>
                            <dcif:lastChangeDate>2012-01-18</dcif:lastChangeDate>
                            <dcif:changeDescription xml:lang="en">additions</dcif:changeDescription>
                        </dcif:lastChange>
                    </dcif:administrationRecord>
                </dcif:administrationInformationSection>
                <dcif:descriptionSection>
                    <dcif:profile>undecided</dcif:profile>
                    <dcif:languageSection>
                        <dcif:language>en</dcif:language>
                        <dcif:nameSection>
                            <dcif:name xml:lang="en">Arthurian Fiction database</dcif:name>
                            <dcif:nameStatus>admitted name</dcif:nameStatus>
                        </dcif:nameSection>
                        <dcif:definitionSection>
                            <dcif:definition xml:lang="en">Online database containing descriptions
                                of narratives of Arthurian fiction and manuscripts containing
                                them</dcif:definition>
                            <dcif:source>-</dcif:source>
                        </dcif:definitionSection>
                    </dcif:languageSection>
                </dcif:descriptionSection>
            </dcif:dataCategory>
        </dcif:dataCategorySelection>

    </xsl:template>

</xsl:stylesheet>
