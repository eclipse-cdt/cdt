<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" indent="yes"/>

    <xsl:template match="/">
        <html>
	    <head>
	        <title>CDT 1.2 Buld Page</title>
	    </head>
	    <body>
	        <h2>CDT 1.2 Build Page</h2>
	        <p>
		    The nightly builds are currently run every weeknight,
		    Monday to Friday at 3:00 a.m. EDT/EST.

		    To get download and install the builds, add the following
		    as a site bookmark in Eclipse's Update Manager perspective:
		</p>
		<p>
		    http://update.eclipse.org/tools/cdt/updates/builds/1.2
		</p>
		<p>
		    There is one category for each build. Expanding the
		    category will reveal the installable features.
		</p>
		<p>
		    <span style="font-style: italic;">
		        Warning: The source feature is currently not providing
		        source as required. If you require debugging the CDT
		        with source, the only mechanism that works for now is
			to get the source from CVS. We are actively working
			to resolve this issue for those	who do not have CVS
			access.
		    </span>
		</p>
		<h3>Builds</h3>
		<p>
		    The builds are currently being run every weeknight. The
		    reports from the automated build verification test suites
		    are linked below.
		</p>
		<ul>
	            <xsl:apply-templates select="site/category-def"/>
		</ul>
	    </body>
	</html>
    </xsl:template>

    <xsl:template match="category-def">
        <li>
	    <xsl:value-of select="@label"/>
	    <ul>
	        <li>
		    <a>
		        <xsl:attribute name="href">
			    <xsl:text>logs/</xsl:text>
			    <xsl:value-of select="substring-after(@name,'CDT Build ')"/>
			    <xsl:text>/org.eclipse.cdt.core.tests.html</xsl:text>
			</xsl:attribute>
			org.eclipse.cdt.core.tests
		    </a>
		</li>
	    </ul>
	</li>
    </xsl:template>

</xsl:transform>
