<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output method="xml" indent="yes"/>

	<xsl:param name="mirrorsURL"/>

	<!-- add p2.mirrorsURL and p2.statsURI properties -->
	<xsl:template match="repository/properties">
		<properties size='{@size+2}'>
			<xsl:copy-of select="property"/>
			<property name='p2.statsURI' value='http://download.eclipse.org/stats'/>
			<xsl:element name="property">
				<xsl:attribute name="name">p2.mirrorsURL</xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="$mirrorsURL"/></xsl:attribute>
			</xsl:element>
		</properties>
	</xsl:template>

	<!-- add p2.mirrorsURL property -->
	<xsl:template match="repository/artifacts/artifact/properties[../@classifier='org.eclipse.update.feature']">
		<properties size='{@size+1}'>
			<xsl:copy-of select="property"/>
			<property name='download.stats' value='{../@id}-{../@version}'/>
		</properties>
	</xsl:template>

	<!-- copy everything else -->
	<xsl:template match="* | @*">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
