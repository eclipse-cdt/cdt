<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output indent="yes"/>

    <xsl:param name="version"/>

	<xsl:template match="/site">
		<site>
			<xsl:copy-of select="*"/>
			<category-def>
				<xsl:attribute name="name">
					<xsl:text>cdt_</xsl:text>
					<xsl:value-of select="$version"/>
				</xsl:attribute>
				<xsl:attribute name="label">
					<xsl:text>CDT Build </xsl:text>
					<xsl:value-of select="$version"/>
				</xsl:attribute>
			</category-def>
			<feature id="org.eclipse.cdt">
				<xsl:attribute name="url">
					<xsl:text>features/org.eclipse.cdt_</xsl:text>
					<xsl:value-of select="$version"/>
					<xsl:text>.jar</xsl:text>
				</xsl:attribute>
				<xsl:attribute name="version">
					<xsl:value-of select="$version"/>
				</xsl:attribute>
				<category>
					<xsl:attribute name="name">
						<xsl:text>cdt_</xsl:text>
						<xsl:value-of select="$version"/>
					</xsl:attribute>
				</category>
			</feature>
		</site>
	</xsl:template>
	
</xsl:transform>
