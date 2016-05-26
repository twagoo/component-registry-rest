<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0"
    xmlns:sch="http://purl.oclc.org/dsdl/schematron">

    <xsl:template match="/ | @* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/xs:schema/xs:annotation/xs:appinfo">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
            <!-- Phase to be inserted for upgrade task with some checks excluded -->
            <sch:phase id="cmdi11upgrade">
                <sch:active pattern="h_id"/>
                <sch:active pattern="h_succ"/>
                <sch:active pattern="r_card"/>
                <!--<sch:active pattern="c_empty"/>-->
                <sch:active pattern="c_sibs"/>
                <sch:active pattern="c_csibs"/>
                <sch:active pattern="d_dsibs"/>
                <sch:active pattern="e_vs"/>
                <sch:active pattern="v_uri"/>
                <sch:active pattern="a_vs"/>
                <sch:active pattern="a_res"/>
                <sch:active pattern="a_sibs"/>
                <sch:active pattern="c_atts"/>
                <sch:active pattern="c_card"/>
                <sch:active pattern="item"/>
            </sch:phase>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
