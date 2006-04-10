<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:xalan="http://xml.apache.org/xslt">
    
<xsl:template match="/Benchmark">
 <html>
 <body>
   <h2><xsl:value-of select="@BenchmarkID"/></h2>
   <p>
   <xsl:for-each select="System">
   Operating system: <xsl:value-of select="@OSName"/><br />
   </xsl:for-each>
   <xsl:for-each select="_NORMALIZATION_VALUES">
   Normalization value: <xsl:value-of select="@ElapsedTime"/>
   </xsl:for-each>
   </p>
    <table border="1">
      <tr>
        <th>CallerID</th>
        <th>ElapsedTime</th>
        <th>NormalizedFactor</th>
      </tr>
      <xsl:for-each select="Task">
      <tr>
        <td><xsl:value-of select="@CallerID"/></td>
        <td><xsl:value-of select="@ElapsedTime"/></td>
        <td><xsl:value-of select="@NormalizedFactor"/></td>
      </tr>
      </xsl:for-each>
    </table>
 </body>
 </html>
</xsl:template>

</xsl:stylesheet>
