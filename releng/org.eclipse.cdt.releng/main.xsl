<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output indent="yes"/>

	<xsl:template match="/">
		<project name="CDT Build Main" default="all">

			<target name="build">

				<!-- Set up build environment -->
				<ftp server="${{build.server}}"
					userid="${{build.userid}}" password="${{build.password}}"
					remotedir="${{build.remotedir}}" action="get" passive="no">
					<fileset dir=".">
						<include name="site.xml"/>
						<include name="build.number"/>
					</fileset>
				</ftp>
				<propertyfile file="build.number">
					<entry key="build.number" type="int" default="0" operation="+" pattern="00"/>
				</propertyfile>
				<property file="build.number"/>
				<echo message="Build number: ${{build.version}}.${{build.number}}"/>
				<delete dir="build"/>
				<unzip src="zips/eclipse-SDK-2.1.1-linux-gtk.zip" dest="."/>
				<move todir="build">
					<fileset dir="eclipse"/>
				</move>
	
				<!-- Download source from CVS -->
				<xsl:for-each select="projects/plugin">
					<cvs cvsroot=":pserver:anonymous@dev.eclipse.org:/home/tools" dest="build/plugins" quiet="true" tag="${{build.tag}}">
						<xsl:attribute name="package">
							<xsl:value-of select="@name"/>
						</xsl:attribute>
					</cvs>
				</xsl:for-each>
	
				<xsl:for-each select="projects/feature">
					<cvs cvsroot=":pserver:anonymous@dev.eclipse.org:/home/tools" dest="build/features" quiet="true" tag="${{build.tag}}">
						<xsl:attribute name="package">
							<xsl:value-of select="@name"/>
							<xsl:text>-feature</xsl:text>
						</xsl:attribute>
					</cvs>
					<move>
						<xsl:attribute name="todir">
							<xsl:text>build/features/</xsl:text>
							<xsl:value-of select="@name"/>
						</xsl:attribute>
						<fileset defaultexcludes="no">
							<xsl:attribute name="dir">
								<xsl:text>build/features/</xsl:text>
								<xsl:value-of select="@name"/>
								<xsl:text>-feature</xsl:text>
							</xsl:attribute>
						</fileset>
					</move>
				</xsl:for-each>
	
				<!-- Fix up the versions to match build number -->
				<xsl:for-each select="projects/plugin|projects/feature">
					<xslt out="x" style="plugin.xsl">
						<xsl:attribute name="in">
							<xsl:text>build/</xsl:text>
							<xsl:value-of select="name()"/>
							<xsl:text>s/</xsl:text>
							<xsl:value-of select="@name"/>
							<xsl:text>/</xsl:text>
							<xsl:value-of select="@type"/>
							<xsl:text>.xml</xsl:text>
						</xsl:attribute>
						<param name="version" expression="${{build.version}}.${{build.number}}"/>
					</xslt>
					<move file="x">
						<xsl:attribute name="tofile">
							<xsl:text>build/</xsl:text>
							<xsl:value-of select="name()"/>
							<xsl:text>s/</xsl:text>
							<xsl:value-of select="@name"/>
							<xsl:text>/</xsl:text>
							<xsl:value-of select="@type"/>
							<xsl:text>.xml</xsl:text>
						</xsl:attribute>
					</move>
				</xsl:for-each>
	
				<!-- Prepare the source plugin -->
				<property name="source.plugin" value="org.eclipse.cdt.source"/>
				<replace
					file="build/plugins/${{source.plugin}}/build.properties"
					token="plugin.properties"
					value="plugin.properties,src/"/>
				
				<!-- Generate build.xml files for projects -->
				<xslt in="manifest.xml" out="build/genscripts.xml" style="genscripts.xsl"/>
				<chmod perm="+x" file="build/eclipse"/>
				<exec executable="${{basedir}}/build/eclipse">
					<arg line="-nosplash -data build/workspace"/>
					<arg line="-application org.eclipse.ant.core.antRunner"/>
					<arg line="-buildfile build/genscripts.xml"/>
				</exec>
		
				<!-- Run the build.xml scripts -->
				<xsl:for-each select="projects/plugin">
					<!-- Build the source jars -->
					<ant target="build.sources">
						<xsl:attribute name="dir">
							<xsl:text>build/plugins/</xsl:text>
							<xsl:value-of select="@name"/>
						</xsl:attribute>
						<property name="javacFailOnError" value="true"/>
						<property name="ws" value="gtk"/>
						<property name="os" value="linux"/>
					</ant>
					<ant target="gather.sources">
						<xsl:attribute name="dir">
							<xsl:text>build/plugins/</xsl:text>
							<xsl:value-of select="@name"/>
						</xsl:attribute>
						<property name="javacFailOnError" value="true"/>
						<property name="ws" value="gtk"/>
						<property name="os" value="linux"/>
						<property name="destination.temp.folder" value="../${{source.plugin}}/src"/>
					</ant>
				</xsl:for-each>
		
				<xsl:for-each select="projects/feature">
					<!-- The default to build the update jars -->
					<ant>
						<xsl:attribute name="dir">
							<xsl:text>build/features/</xsl:text>
							<xsl:value-of select="@name"/>
						</xsl:attribute>
						<property name="javacFailOnError" value="true"/>
						<property name="ws" value="gtk"/>
						<property name="os" value="linux"/>
					</ant>
					<!-- The old style zips -->
					<ant target="zip.distribution">
						<xsl:attribute name="dir">
							<xsl:text>build/features/</xsl:text>
							<xsl:value-of select="@name"/>
						</xsl:attribute>
						<property name="javacFailOnError" value="true"/>
						<property name="ws" value="gtk"/>
						<property name="os" value="linux"/>
					</ant>
				</xsl:for-each>
		
				<!-- Move jars and zips to the update site format -->
				<delete dir="plugins"/>
				<copy todir="plugins" flatten="true">
					<fileset dir="build/plugins">
						<xsl:for-each select="projects/plugin">
							<include>
								<xsl:attribute name="name">
									<xsl:value-of select="@name"/>
									<xsl:text>/</xsl:text>
									<xsl:value-of select="@name"/>
									<xsl:text>_${build.version}.${build.number}.jar</xsl:text>
								</xsl:attribute>
							</include>
						</xsl:for-each>
					</fileset>
				</copy>
				<delete dir="features"/>
				<copy todir="features" flatten="true">
					<fileset dir="build/features">
						<xsl:for-each select="projects/feature">
							<include>
								<xsl:attribute name="name">
									<xsl:value-of select="@name"/>
									<xsl:text>/</xsl:text>
									<xsl:value-of select="@name"/>
									<xsl:text>_${build.version}.${build.number}.jar</xsl:text>
								</xsl:attribute>
							</include>
						</xsl:for-each>
					</fileset>
				</copy>
				<delete dir="dist"/>
				<copy todir="dist" flatten="true">
					<fileset dir="build/features">
						<xsl:for-each select="projects/feature">
							<include>
								<xsl:attribute name="name">
									<xsl:value-of select="@name"/>
									<xsl:text>/</xsl:text>
									<xsl:value-of select="@name"/>
									<xsl:text>_${build.version}.${build.number}.bin.dist.zip</xsl:text>
								</xsl:attribute>
							</include>
						</xsl:for-each>
					</fileset>
				</copy>
		
				<!-- Set up the test environment -->
				<delete dir="test"/>
				<unzip src="zips/eclipse-SDK-2.1.1-linux-gtk.zip" dest="."/>
				<move todir="test">
					<fileset dir="eclipse"/>
				</move>
				<unzip src="zips/org.eclipse.test_2.1.0.zip" dest="test/plugins"/>
				<unzip src="zips/org.eclipse.ant.optional.junit_2.1.0.zip" dest="test/plugins"/>
		
				<xsl:for-each select="projects/plugin|projects/feature">
					<unjar>
						<xsl:attribute name="src">
							<xsl:value-of select="name()"/>
							<xsl:text>s/</xsl:text>
							<xsl:value-of select="@name"/>
							<xsl:text>_${build.version}.${build.number}.jar</xsl:text>
						</xsl:attribute>
						<xsl:attribute name="dest">
							<xsl:text>test/</xsl:text>
							<xsl:value-of select="name()"/>
							<xsl:text>s/</xsl:text>
							<xsl:value-of select="@name"/>
							<xsl:text>_${build.version}.${build.number}</xsl:text>
						</xsl:attribute>
					</unjar>
				</xsl:for-each>
		
				<!-- Run the tests -->
				<chmod perm="+x" file="test/eclipse"/>
				<exec executable="${{basedir}}/test/eclipse" dir="test">
					<arg line="-nosplash"/>
					<arg line="-application org.eclipse.ant.core.antRunner"/>
					<arg line="-buildfile plugins/org.eclipse.cdt.core.tests_${{build.version}}.${{build.number}}/test.xml"/>
					<arg line="-Dorg.eclipse.test=org.eclipse.test_2.1.0"/>
					<arg line="-Declipse-home=${{basedir}}/test"/>
					<arg line="-Dos=linux -Dws=gtk -Darch=x86"/>
				</exec>
		
				<!-- Create the reports -->
				<delete dir="logs"/>
					<xslt in="test/org.eclipse.cdt.core.tests.xml"
						out="logs/${{build.version}}.${{build.number}}/org.eclipse.cdt.core.tests.html"
						style="junit.xsl"/>
		
				<!-- Add our version to site.xml -->
				<tstamp>
					<format property="build.date" pattern="EEE MMM d HH:mm:ss z yyyy"/>
				</tstamp>
				<xslt in="site.xml" out="s" style="site.xsl">
					<param name="version" expression="${{build.version}}.${{build.number}}"/>
					<param name="date" expression="${{build.date}}"/>
				</xslt>
				<move file="s" tofile="site.xml"/>
		
				<!-- Generate the site home page -->
				<xslt in="site.xml" out="index.html" style="index.xsl">
					<param name="branch" expression="${{build.branch}}"/>
				</xslt>
		
			</target>
	
			<target name="upload">
				<!-- Upload the update site -->
				<ftp server="${{build.server}}"
					userid="${{build.userid}}" password="${{build.password}}"
					remotedir="${{build.remotedir}}" action="put" passive="no">
					<fileset dir=".">
						<include name="plugins/*.jar"/>
						<include name="features/*.jar"/>
						<include name="dist/*.zip"/>
						<include name="logs/**/*.html"/>
						<include name="build.number"/>
						<include name="index.html"/>
						<include name="site.xml"/>
					</fileset>
				</ftp>
			</target>
	
			<target name="mail">
				<mail from="dschaefe@ca.ibm.com"
					tolist="cdt-test-dev@eclipse.org"
					subject="CDT Build ${{build.version}}.${{build.number}} completed">
					<message src="message.txt"/>
				</mail>
			</target>
	
			<target name="all" depends="build,upload,mail"/>
			
		</project>
		
	</xsl:template>
	
</xsl:transform>
