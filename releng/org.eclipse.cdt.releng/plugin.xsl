<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output indent="yes"/>

    <xsl:param name="version"/>

    <xsl:template match="/plugin">
        <plugin>
	    <xsl:attribute name="version">
	        <xsl:value-of select="$version"/>
	    </xsl:attribute>
	    <xsl:copy-of select="*|@*[not(name()='version')]"/>
	</plugin>
    </xsl:template>

    <xsl:template match="/fragment">
        <fragment>
	    <xsl:attribute name="version">
	        <xsl:value-of select="$version"/>
	    </xsl:attribute>
	    <xsl:attribute name="plugin-version">
	        <xsl:value-of select="$version"/>
	    </xsl:attribute>
	    <xsl:copy-of select="*|@*[not(name()='version' or name()='plugin-version')]"/>
	</fragment>
    </xsl:template>

    <xsl:template match="/feature">
        <xsl:copy>
	   <xsl:attribute name="version">
	       <xsl:value-of select="$version"/>
	   </xsl:attribute>
	   <xsl:apply-templates mode="feature" select="@*[not(name()='version')]|*|text()"/>
	</xsl:copy>
    </xsl:template>

    <xsl:template mode="feature" match="includes|plugin">
        <xsl:copy>
	    <xsl:attribute name="version">
	        <xsl:value-of select="$version"/>
	    </xsl:attribute>
	    <xsl:copy-of select="*|@*[not(name()='version')]"/>
	</xsl:copy>	
    </xsl:template>

    <xsl:template mode="feature" match="import[starts-with(@plugin,'org.eclipse.cdt')]">
        <xsl:copy>
	    <xsl:attribute name="version">
	        <xsl:value-of select="$version"/>
	    </xsl:attribute>
	    <xsl:copy-of select="*|@*[not(name()='version')]"/>
	</xsl:copy>	
    </xsl:template>

    <xsl:template mode="feature" match="url/update">
        <xsl:copy>
	    <xsl:attribute name="url">
	        <xsl:text>http://update.eclipse.org/tools/cdt/updates/builds/1.2</xsl:text>
	    </xsl:attribute>
	    <xsl:copy-of select="*|@*[not(name()='url')]"/>
	</xsl:copy>
    </xsl:template>

    <xsl:template match="@*|*|text()">
        <xsl:copy>
	    <xsl:apply-templates select="@*|*|text()"/>
	</xsl:copy>
    </xsl:template>

    <xsl:template mode="feature" match="@*|*|text()">
        <xsl:copy>
	    <xsl:apply-templates mode="feature" select="@*|*|text()"/>
	</xsl:copy>
    </xsl:template>

</xsl:transform>
