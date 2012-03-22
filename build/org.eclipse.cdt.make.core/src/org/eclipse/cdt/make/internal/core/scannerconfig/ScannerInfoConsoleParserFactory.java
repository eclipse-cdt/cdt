/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  IBM - Initial API and implementation
 *  Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.io.OutputStream;
import java.net.URI;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeBuilderUtil;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfile;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

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
	 */
	public static ConsoleOutputSniffer getESIProviderOutputSniffer(OutputStream outputStream,
			OutputStream errorStream,
			IProject project,
			String id,
			IScannerConfigBuilderInfo2 info2,
			IScannerInfoCollector collector,
			IMarkerGenerator markerGenerator) {

		return getESIProviderOutputSniffer(outputStream, errorStream, project, new InfoContext(project), id, info2, collector, markerGenerator);
	}

	/**
	 * Creates a ConsoleOutputStreamSniffer, make builder scanner info console parser
	 * and a utility.
	 */
	public static ConsoleOutputSniffer getESIProviderOutputSniffer(OutputStream outputStream,
			OutputStream errorStream,
			IProject project,
			InfoContext infoContext,
			String id,
			IScannerConfigBuilderInfo2 info2,
			IScannerInfoCollector collector,
			IMarkerGenerator markerGenerator) {

		IScannerInfoConsoleParser parser = getESIConsoleParser(project, infoContext, id, info2, collector, markerGenerator);
		if (parser != null) {
			return new ConsoleOutputSniffer(outputStream, errorStream, new IScannerInfoConsoleParser[] { parser });
		}
		return null;
	}

	/**
	/* Get the ESIProvider console parser.
	 */
	public static IScannerInfoConsoleParser getESIConsoleParser(IProject project,
			InfoContext infoContext,
			String id,
			IScannerConfigBuilderInfo2 info2,
			IScannerInfoCollector collector,
			IMarkerGenerator markerGenerator) {

		if (info2.isProviderOutputParserEnabled(id)) {
			SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
					getSCProfileInstance(project, infoContext, info2.getSelectedProfileId());

			IScannerInfoConsoleParser parser = profileInstance.createExternalScannerInfoParser(id);
			IPath buildDirectory = MakeBuilderUtil.getBuildDirectory(project, MakeBuilder.BUILDER_ID);

			parser.startup(project, buildDirectory, collector, markerGenerator);
			return parser;
		}
		return null;
	}

	/**
	 * Creates a ConsoleOutputStreamSniffer, ESI provider scanner info console parser
	 * and a utility.
	 */
	public static ConsoleOutputSniffer getMakeBuilderOutputSniffer(
			OutputStream outputStream,
			OutputStream errorStream,
			IProject project,
			IPath workingDirectory,
			IScannerConfigBuilderInfo2 info2,
			IMarkerGenerator markerGenerator,
			IScannerInfoCollector collector) {
		
		return getMakeBuilderOutputSniffer(outputStream, errorStream, project, new InfoContext(project), workingDirectory, info2, markerGenerator, collector);
	}

	/**
	 * Creates a ConsoleOutputStreamSniffer, ESI provider scanner info console parser
	 * and a utility.
	 */
	public static ConsoleOutputSniffer getMakeBuilderOutputSniffer(
			OutputStream outputStream,
			OutputStream errorStream,
			IProject project,
			InfoContext infoContext,
			IPath workingDirectory,
			IScannerConfigBuilderInfo2 info2,
			IMarkerGenerator markerGenerator,
			IScannerInfoCollector collector) {

		IScannerInfoConsoleParser parser = getScannerInfoConsoleParserInternal(project, infoContext, workingDirectory, info2, markerGenerator, collector);
		if (parser != null) {
			// create an output stream sniffer
			return new ConsoleOutputSniffer(outputStream, errorStream, new IScannerInfoConsoleParser[] {parser});

		}
		return null;
	}

	private static IScannerInfoConsoleParser getScannerInfoConsoleParserInternal(IProject project,
			InfoContext infoContext,
			IPath workingDirectory,
			IScannerConfigBuilderInfo2 info2,
			IMarkerGenerator markerGenerator,
			IScannerInfoCollector collector) {

		IScannerInfoConsoleParser parser = null;
//		try {
			// get the SC builder settings
			/*if (currentProject.hasNature(ScannerConfigNature.NATURE_ID))*/ {
				if (info2 == null) {
					try {
						IScannerConfigBuilderInfo2Set container = ScannerConfigProfileManager.createScannerConfigBuildInfo2Set(project);
						info2 = container.getInfo(infoContext);
					} catch (CoreException e) {
						// builder not installed or disabled
					}
				}
				if (info2 != null && info2.isAutoDiscoveryEnabled() && info2.isBuildOutputParserEnabled()) {
					String id = info2.getSelectedProfileId();

					// get the make builder console parser
					SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().getSCProfileInstance(project, infoContext, id);
					parser = profileInstance.createBuildOutputParser();
					if (parser != null){
						if (collector == null) {
							collector = profileInstance.getScannerInfoCollector();
						}
						parser.startup(project, workingDirectory, collector, info2.isProblemReportingEnabled() ? markerGenerator : null);
					}
				}
			}
//		}
//		catch (CoreException e) {
//			MakeCorePlugin.log(e.getStatus());
//		}

		return parser;
	}

	public static IScannerInfoConsoleParser getScannerInfoConsoleParser(IProject project, URI workingDirectoryURI, IMarkerGenerator markerGenerator) {
		String pathFromURI = EFSExtensionManager.getDefault().getPathFromURI(workingDirectoryURI);
		if(pathFromURI == null) {
			// fallback to CWD
			pathFromURI = System.getProperty("user.dir"); //$NON-NLS-1$
		}
		return getScannerInfoConsoleParserInternal(project, new InfoContext(project), new Path(pathFromURI), null, markerGenerator, null);
	}

	// TODO - perhaps this be unified with the other one?
	public static IScannerInfoConsoleParser getScannerInfoConsoleParser(IProject project,
			InfoContext infoContext,
			IPath workingDirectory,
			IScannerConfigBuilderInfo2 info2,
			IMarkerGenerator markerGenerator,
			IScannerInfoCollector collector) {

		IScannerInfoConsoleParser parser = null;
		if (info2 != null && info2.isAutoDiscoveryEnabled() && info2.isBuildOutputParserEnabled()) {
			String id = info2.getSelectedProfileId();
			ScannerConfigProfile profile = ScannerConfigProfileManager.getInstance().getSCProfileConfiguration(id);
			if(profile.getBuildOutputProviderElement() != null){
				// get the make builder console parser
				SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().getSCProfileInstance(project, infoContext, id);
				parser = profileInstance.createBuildOutputParser();
				if(parser != null){
					if (collector == null) {
						collector = profileInstance.getScannerInfoCollector();
					}
					parser.startup(project, workingDirectory, collector, info2.isProblemReportingEnabled() ? markerGenerator : null);
				}

			}
		}

		return parser;
	}
}
