<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <project name="CDT Build Generate Scripts" default="all">
	    <target name="all">
	        <xsl:for-each select="projects/feature">
		    <eclipse.buildScript install="build">
		        <xsl:attribute name="elements">
			    <xsl:text>feature@</xsl:text>
			    <xsl:value-of select="@name"/>
			</xsl:attribute>
		    </eclipse.buildScript>
		</xsl:for-each>
	    </target>
	</project>
    </xsl:template>

</xsl:transform>
