/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.io.OutputStream;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.ScannerInfoConsoleParserUtility;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * A factory that creates a ConsoleOutputStreamSniffer,
 * ScannerInfoConsoleParser and optionally a ScannerInfoConsoleParserUtility.
 * 
 * @author vhirsl
 */
public class ScannerInfoConsoleParserFactory {

	/**
	 * Creates a ConsoleOutputStreamSniffer, make builder scanner info console parser
	 * and a utility.
	 * 
	 * @param outputStream
	 * @param currentProject
	 * @param markerGenerator
	 * @param scBuildInfo
	 * @param collector	- scanner info collector
	 * @return ConsoleOutputSniffer
	 */
	public static ConsoleOutputSniffer getESIProviderOutputSniffer(
											OutputStream outputStream,
											OutputStream errorStream,
											IProject currentProject,
											IScannerConfigBuilderInfo scBuildInfo,
											IScannerInfoCollector collector) {
		if (scBuildInfo.isESIProviderCommandEnabled()) {
			// get the ESIProvider console parser 
			IScannerInfoConsoleParser clParser = MakeCorePlugin.getDefault().
				getScannerInfoConsoleParser(scBuildInfo.getESIProviderConsoleParserId());
			// initialize it with the utility
			clParser.startup(currentProject, null /*new ScannerInfoConsoleParserUtility(
				currentProject, null, markerGenerator)*/, collector);
			// create an output stream sniffer
			return new ConsoleOutputSniffer(outputStream, errorStream, new 
				IScannerInfoConsoleParser[] {clParser});
		}
		return null;
	}
	
	/**
	 * Creates a ConsoleOutputStreamSniffer, ESI provider scanner info console parser
	 * and a utility.
	 * 
	 * @param outputStream
	 * @param currentProject
	 * @param workingDirectory
	 * @param markerGenerator
	 * @return ConsoleOutputSniffer
	 */
	public static ConsoleOutputSniffer getMakeBuilderOutputSniffer(
											OutputStream outputStream,
											OutputStream errorStream,
											IProject currentProject,
											IPath workingDirectory,
											IMarkerGenerator markerGenerator) {
		try {
			// get the SC builder settings
			if (currentProject.hasNature(ScannerConfigNature.NATURE_ID)) {
				IScannerConfigBuilderInfo scBuildInfo;
				try {
					scBuildInfo = MakeCorePlugin.
						createScannerConfigBuildInfo(currentProject, ScannerConfigBuilder.BUILDER_ID);
				}
				catch (CoreException e) {
					// builder not installed or disabled
					scBuildInfo = null;
				}
				if (scBuildInfo != null && 
						scBuildInfo.isAutoDiscoveryEnabled() &&
						scBuildInfo.isMakeBuilderConsoleParserEnabled()) {
					// get the make builder console parser 
					IScannerInfoConsoleParser clParser = MakeCorePlugin.getDefault().
						getScannerInfoConsoleParser(scBuildInfo.getMakeBuilderConsoleParserId());			
					// initialize it with the utility
					clParser.startup(currentProject, new ScannerInfoConsoleParserUtility(
							currentProject, workingDirectory,
							scBuildInfo.isSIProblemGenerationEnabled() ? markerGenerator : null),
							ScannerInfoCollector.getInstance());
					// create an output stream sniffer
					return new ConsoleOutputSniffer(outputStream, errorStream, new 
						IScannerInfoConsoleParser[] {clParser});
				}
			}
		} 
		catch (CoreException e) {
			MakeCorePlugin.log(e.getStatus());
		}
		return null;
	}
}
