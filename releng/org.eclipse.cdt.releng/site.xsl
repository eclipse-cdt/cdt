<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output indent="yes"/>

    <xsl:param name="version"/>
    <xsl:param name="date"/>

    <xsl:template match="/site">
        <site>
	    <xsl:copy-of select="*|@*"/>
	    <category-def>
		<xsl:attribute name="name">
		    <xsl:text>CDT Build </xsl:text>
		    <xsl:value-of select="$version"/>
		</xsl:attribute>
		<xsl:attribute name="label">
		    <xsl:text>CDT Build </xsl:text>
		    <xsl:value-of select="$version"/>
		    <xsl:text> - </xsl:text>
		    <xsl:value-of select="$date"/>
		</xsl:attribute>
	    </category-def>
	    <xsl:apply-templates mode="features" select="document('manifest.xml')"/>
	</site>
    </xsl:template>

    <xsl:template mode="features" match="/projects/feature[not(@visible='false')]">
        <feature>
	    <xsl:attribute name="url">
	        <xsl:text>features/</xsl:text>
		<xsl:value-of select="@name"/>
		<xsl:text>_</xsl:text>
		<xsl:value-of select="$version"/>
		<xsl:text>.jar</xsl:text>
	    </xsl:attribute>
	    <xsl:attribute name="id">
	        <xsl:value-of select="@name"/>
	    </xsl:attribute>
	    <xsl:attribute name="version">
	        <xsl:value-of select="$version"/>
	    </xsl:attribute>
	    <category>
	        <xsl:attribute name="name">
		    <xsl:text>CDT Build </xsl:text>
		    <xsl:value-of select="$version"/>
		</xsl:attribute>
	    </category>
	</feature>
    </xsl:template>

    <xsl:template mode="features" match="/projects">
        <xsl:apply-templates mode="features" select="*"/>
    </xsl:template>

</xsl:transform>
