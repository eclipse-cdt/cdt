<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/testRun">
		<html>
			<head><title>Test Results for <xsl:value-of select="@name"/></title></head>
			<body>
				<h2>Summary Table for <xsl:value-of select="@name"/></h2>
				<xsl:variable name="pass" select="count(//test[@result='pass'])"/>
				<xsl:variable name="failed" select="count(//test[@result='failed'])"/>
				<xsl:variable name="error" select="count(//test[@result='error'])"/>
				<xsl:variable name="total" select="count(//test)"/>
				<xsl:variable name="time" select="sum(//test/@time)"/>
				<xsl:variable name="mins" select="floor($time div 60)"/>
				<xsl:variable name="secs" select="floor($time - $mins * 60)"/>
				<xsl:variable name="fsecs" select="$time - $mins * 60 - $secs"/>
				<table border="2" cellspacing="0" cellpadding="2">
					<tr>
						<th>Total</th>
						<th>Pass</th>
						<th>Fail</th>
						<th>Error</th>
						<th>Pass Rate</th>
						<th>Time</th>
					</tr>
					<tr>
						<td><xsl:value-of select="$total"/></td>
						<td><xsl:value-of select="$pass"/></td>
						<td><xsl:value-of select="$failed"/></td>
						<td><xsl:value-of select="$error"/></td>
						<td><xsl:value-of select="format-number($pass div $total, '#.0%')"/></td>
						<td><xsl:value-of select="$mins"/>:<xsl:value-of select="format-number($secs, '00')"/><xsl:value-of select="format-number($fsecs, '.0')"/></td>
					</tr>
				</table>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="testSuite">
		<h2>Test Suite: <xsl:value-of select="@name"/></h2>
		<table border="2" cellspacing="0" cellpadding="2" width="100%">
			<tr><th>Result</th><th>Time (s)</th><th>Test</th></tr>
			<xsl:apply-templates/>
		</table>
	</xsl:template>

	<xsl:template match="test">
		<tr>
			<td><xsl:value-of select="@result"/></td>
			<td><xsl:value-of select="format-number(@time, '0.000')"/></td>
			<td>
				<xsl:value-of select="../@name"/><br></br>::<xsl:value-of select="@name"/>
			</td>
		</tr>
	</xsl:template>
	
</xsl:transform>
