<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:mw="http://www.mediawiki.org/xml/export-0.3/">
	<xsl:output  method="text"/>
	
	<xsl:template match="/">
		<xsl:value-of select="//mw:text"/>
	</xsl:template>
</xsl:stylesheet>
