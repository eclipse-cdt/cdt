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
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParserUtility;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoCollector;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses gcc and g++ output for -I and -D parameters.
 * 
 * @author vhirsl
 */
public class GCCScannerInfoConsoleParser implements IScannerInfoConsoleParser {

	private IProject fProject = null;
	private IScannerInfoConsoleParserUtility fUtil = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject)
	 */
	public void startup(IProject project, IScannerInfoConsoleParserUtility util) {
		fProject = project;
		fUtil = util;
		
		IPath workingDirectory = MakeCorePlugin.getWorkingDirectory();
		String targetFile = "dummy";	//$NON-NLS-1$
		try {
			if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
				targetFile = GCCScannerConfigUtil.CPP_SPECS_FILE;
			}
			else if (project.hasNature(CProjectNature.C_NATURE_ID)) {
				targetFile = GCCScannerConfigUtil.C_SPECS_FILE;
			}
		} catch (CoreException e) {
			//TODO VMIR better error handling
			MakeCorePlugin.log(e.getStatus());
		}
		IPath path2File = workingDirectory.append(targetFile);
		if (!path2File.toFile().exists()) {
			GCCScannerConfigUtil.createSpecs();
		}
		List targetSpecificOptions = new ArrayList();
		targetSpecificOptions.add(targetFile);
		
		ScannerInfoCollector.getInstance().
			contributeToScannerConfig(project, null, null, targetSpecificOptions);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.ScannerInfoConsoleParserUtility#processLine(java.lang.String)
	 */
	public boolean processLine(String line) {
		boolean rc = false;
		// make\[[0-9]*\]:  error_desc
		int firstColon= line.indexOf(':');
		String make = line.substring(0, firstColon + 1);
		if (firstColon != -1 && make.indexOf("make") != -1) { //$NON-NLS-1$
			boolean enter = false;
			String msg = line.substring(firstColon + 1).trim();		
			if ((enter = msg.startsWith("Entering directory")) || //$NON-NLS-1$
			    (msg.startsWith("Leaving directory"))) { //$NON-NLS-1$
			    int s = msg.indexOf('`');
			    int e = msg.indexOf('\'');
			    if (s != -1 && e != -1) {
			    	String dir = msg.substring(s+1, e);
			    	fUtil.changeMakeDirectory(dir, getDirectoryLevel(line), enter);
			    	return rc;
				}
			}
		}
		// Known patterns:
		// (a) gcc|g++ ... -Dxxx -Iyyy ...
		StringTokenizer scanner = new StringTokenizer(line);
		if (scanner.countTokens() <= 1)
			return false;
		String token = scanner.nextToken().toLowerCase();
		if (token.endsWith("gcc") || token.endsWith("g++")) {//$NON-NLS-1$ //$NON-NLS-2$
			// Recognized gcc or g++ compiler invocation
			List includes = new ArrayList();
			List symbols = new ArrayList();
			List targetSpecificOptions = new ArrayList();

			rc = true;
			String fileName = null;
			while (scanner.hasMoreTokens()) {
				token = scanner.nextToken();
				if (token.startsWith("-D")) {//$NON-NLS-1$
					String symbol = token.substring(2);
					if (symbol.length() == 0) {
						if (scanner.hasMoreTokens()) {
							symbol = scanner.nextToken();
						}
						else {
							continue;
						}
					}
					if (!symbols.contains(symbol))
						symbols.add(symbol);
				}
				else if (token.startsWith("-I")) {//$NON-NLS-1$
					String iPath = token.substring(2);
					if (iPath.length() == 0) {
						if (scanner.hasMoreTokens()) {
							iPath = scanner.nextToken();
						}
						else {
							continue;
						}
					}
					String nPath = fUtil.normalizePath(iPath);
					if (!includes.contains(nPath))
						includes.add(nPath);
				}
				else if (token.equals("-mwin32") ||		//$NON-NLS-1$
						 token.equals("-mno-win32") ||	//$NON-NLS-1$
						 token.equals("-mno-cygwin") ||	//$NON-NLS-1$
						 token.equals("-ansi") ||		//$NON-NLS-1$
						 token.equals("-nostdinc") ||	//$NON-NLS-1$
						 token.equals("-posix") ||		//$NON-NLS-1$
						 token.equals("-pthread")) {	//$NON-NLS-1$
					if (!targetSpecificOptions.contains(token))
						targetSpecificOptions.add(token);
				}
				else {
					String possibleFileName = token.toLowerCase();
					if (possibleFileName.startsWith("..") ||	//$NON-NLS-1$
						possibleFileName.startsWith(".") ||		//$NON-NLS-1$
						possibleFileName.startsWith("/") ||		//$NON-NLS-1$
						possibleFileName.endsWith(".c") || 		//$NON-NLS-1$
						possibleFileName.endsWith(".cpp") ||	//$NON-NLS-1$
						possibleFileName.endsWith(".cc") ||		//$NON-NLS-1$
						possibleFileName.endsWith(".cxx")) {	//$NON-NLS-1$
						
						fileName = token;
					}
				}
			}
			
			IProject project = fProject;   
			IFile file = null;
			List translatedIncludes = includes;
			if (includes.size() > 0) {
				if (fileName != null) {
					file = fUtil.findFile(fileName);
					if (file != null) {
						project = file.getProject();
						translatedIncludes = fUtil.translateRelativePaths(file, fileName, includes);
					}
				}
				else {
					TraceUtil.outputError("Unable to find file name: ", line);	//$NON-NLS-1$
//					fUtil.generateMarker(fProject, -1, "Unable to find file name: " + line, //$NON-NLS-1$
//							IMarkerGenerator.SEVERITY_ERROR_RESOURCE, null);
				}
			}
			// Contribute discovered includes and symbols to the ScannerInfoCollector
			if (translatedIncludes.size() > 0 || symbols.size() > 0) {
				ScannerInfoCollector.getInstance().
					contributeToScannerConfig(project, translatedIncludes, symbols, targetSpecificOptions);
				
				TraceUtil.outputTrace("Discovered scanner info for file \'" + fileName + '\'',	//$NON-NLS-1$
						"Include paths", includes, translatedIncludes, "Defined symbols", symbols);	//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return rc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
	 */
	public void shutdown() {
		if (fUtil != null) {
			fUtil.reportProblems();
		}
	}

	private int getDirectoryLevel(String line) {
		int s = line.indexOf('[');
		int num = 0;
		if (s != -1) {
			int e = line.indexOf(']');
			String number = line.substring(s + 1, e).trim();		
			try {
				num = Integer.parseInt(number);
			} catch (NumberFormatException exc) {
			}
		}
		return num;
	}
}
