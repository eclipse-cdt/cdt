<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/testRun">
		<html>
			<head><title>Test Results for <xsl:value-of select="@name"/></title></head>
			<body>
				<h2>Summary Table for <xsl:value-of select="@name"/></h2>
				<p>Some day...</p>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="testSuite">
		<h2>Test Suite: <xsl:value-of select="@name"/></h2>
		<table border="2" cellspacing="0" width="100%">
			<tr><th>Result</th><th>Time (s)</th><th>Test</th></tr>
			<xsl:apply-templates/>
		</table>
	</xsl:template>

	<xsl:template match="test">
		<tr>
			<td><xsl:value-of select="@result"/></td>
			<td><xsl:value-of select="@time"/></td>
			<td>
				<xsl:value-of select="../@name"/><br></br>::<xsl:value-of select="@name"/>
			</td>
		</tr>
	</xsl:template>
	
</xsl:transform>
