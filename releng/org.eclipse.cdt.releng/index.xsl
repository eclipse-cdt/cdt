<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" indent="yes"/>

    <xsl:template match="/">
        <html>
		    <head>
	    	    <title>CDT 2.0 Build Page</title>
		    </head>
		    <body>
		        <h2>CDT 2.0 Build Page</h2>
		        <p>
		        	The nightly builds are currently run every weeknight
				    To get download and install the builds, add the following
				    as a site bookmark in Eclipse's Update Manager perspective:
				</p>
				<p>
				    http://update.eclipse.org/tools/cdt/updates/builds/2.0
				</p>
				<p>
				    There is one category for each build. Expanding the
		 			category will reveal the installable features.
		    		Currently the following features are available.
		    		You must install the main CDT feature as well as one
		    		or both of the builder features.
				</p>
				<h3>Builds</h3>
				<p>
				    The reports from the automated build verification test suites
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
		    	<xsl:if test="string-length(@label) &gt; 30">
			        <li>
					    <a>
					        <xsl:attribute name="href">
							    <xsl:text>logs/</xsl:text>
							    <xsl:value-of select="substring-after(@label,'CDT Build ')"/>
							    <xsl:text>/org.eclipse.cdt.core.tests.xml</xsl:text>
							</xsl:attribute>
							<xsl:text>org.eclipse.cdt.core.tests</xsl:text>
					    </a>
					</li>
				</xsl:if>
		    	<xsl:if test="string-length(@label) &lt; 30">
			        <li>
					    <a>
					        <xsl:attribute name="href">
							    <xsl:text>logs/</xsl:text>
							    <xsl:value-of select="substring-after(@label,'CDT Build ')"/>
							    <xsl:text>.html</xsl:text>
							</xsl:attribute>
							<xsl:text>Unit Test Results</xsl:text>
					    </a>
						<table width="75%">
							<tr>
								<td>
					    			<a>
					    				<xsl:attribute name="href">
						    				<xsl:text>zips/I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>/org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>-aix.motif.ppc.zip</xsl:text>
						    			</xsl:attribute>
					    				<font size="1">
							    			<xsl:text>org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
							    			<xsl:text>-aix.motif.ppc.zip</xsl:text>
							    		</font>
					    			</a>
					    		</td>
					    		<td>
					    			<a>
					    				<xsl:attribute name="href">
						    				<xsl:text>zips/I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>/org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>-linux.gtk.x86.zip</xsl:text>
						    			</xsl:attribute>
					    				<font size="1">
							    			<xsl:text>org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
							    			<xsl:text>-linux.gtk.x86.zip</xsl:text>
							    		</font>
					    			</a>
					    		</td>
					    	</tr>
					    	<tr>
					    		<td>
					    			<a>
					    				<xsl:attribute name="href">
						    				<xsl:text>zips/I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>/org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>-linux.motif.x86.zip</xsl:text>
						    			</xsl:attribute>
					    				<font size="1">
							    			<xsl:text>org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
							    			<xsl:text>-linux.motif.x86.zip</xsl:text>
							    		</font>
					    			</a>
					    		</td>
					    		<td>
					    			<a>
					    				<xsl:attribute name="href">
						    				<xsl:text>zips/I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>/org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>-qnx.photon.x86.zip</xsl:text>
						    			</xsl:attribute>
						    			<font size="1">
							    			<xsl:text>org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
							    			<xsl:text>-qnx.photon.x86.zip</xsl:text>
							    		</font>
					    			</a>
					    		</td>
					    	</tr>
					    	<tr>
					    		<td>
					    			<a>
					    				<xsl:attribute name="href">
						    				<xsl:text>zips/I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>/org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>-solaris.motif.sparc.zip</xsl:text>
						    			</xsl:attribute>
						    			<font size="1">
							    			<xsl:text>org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
							    			<xsl:text>-solaris.motif.sparc.zip</xsl:text>
							    		</font>
					    			</a>
					    		</td>
					    		<td>
					    			<a>
					    				<xsl:attribute name="href">
						    				<xsl:text>zips/I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>/org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>-win32.win32.x86.zip</xsl:text>
						    			</xsl:attribute>
						    			<font size="1">
							    			<xsl:text>org.eclipse.cdt-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
							    			<xsl:text>-win32.win32.x86.zip</xsl:text>
							    		</font>
					    			</a>
					    		</td>
					    	</tr>
					    	<tr>
					    		<td>
					    			<a>
					    				<xsl:attribute name="href">
						    				<xsl:text>zips/I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>/org.eclipse.cdt.make-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>.zip</xsl:text>
						    			</xsl:attribute>
						    			<font size="1">
							    			<xsl:text>org.eclipse.cdt.make-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
							    			<xsl:text>.zip</xsl:text>
							    		</font>
					    			</a>
					    		</td>
					    		<td>
					    			<a>
					    				<xsl:attribute name="href">
						    				<xsl:text>zips/I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>/org.eclipse.cdt.managedbuilder-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>.zip</xsl:text>
						    			</xsl:attribute>
						    			<font size="1">
							    			<xsl:text>org.eclipse.cdt.managedbuilder-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
							    			<xsl:text>.zip</xsl:text>
							    		</font>
					    			</a>
					    		</td>
					    	</tr>
					    	<tr>
					    		<td>
					    			<a>
					    				<xsl:attribute name="href">
						    				<xsl:text>zips/I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>/org.eclipse.cdt.testing-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
						    				<xsl:text>.zip</xsl:text>
						    			</xsl:attribute>
						    			<font size="1">
							    			<xsl:text>org.eclipse.cdt.testing-I</xsl:text>
						    				<xsl:value-of select="substring-after(@label,'CDT Build 2.0.0.')"/>
							    			<xsl:text>.zip</xsl:text>
							    		</font>
					    			</a>
					    		</td>
					    	</tr>
					    </table>
		    		</li>
		    	</xsl:if>
		    </ul>
		</li>
    </xsl:template>

</xsl:transform>
