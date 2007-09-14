/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Martin Oberhuber (Wind River Systems) - bug 155096
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Parses gcc and g++ output for -I and -D parameters.
 * 
 * @author vhirsl
 */
public class GCCScannerInfoConsoleParser extends AbstractGCCBOPConsoleParser {

	private ScannerInfoConsoleParserUtility fUtil = null;
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IPath, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector, org.eclipse.cdt.core.IMarkerGenerator)
     */
    public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector, IMarkerGenerator markerGenerator) {
        fUtil = (project != null && workingDirectory != null && markerGenerator != null) ?
                new ScannerInfoConsoleParserUtility(project, workingDirectory, markerGenerator) : null;
        super.startup(project, collector);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.gnu.AbstractGCCBOPConsoleParser#getUtility()
     */
    protected AbstractGCCBOPConsoleParserUtility getUtility() {
        return fUtil;
    }

    protected boolean processCommand(String[] tokens) {
        int compilerInvocationIdx= findCompilerInvocation(tokens);
        if (compilerInvocationIdx<0) {
        	return false;
        }
        
        if (compilerInvocationIdx+1 >= tokens.length) {
        	return false;
        }

        // Recognized gcc or g++ compiler invocation
        List includes = new ArrayList();
        List symbols = new ArrayList();
        List targetSpecificOptions = new ArrayList();

        String fileName = null;
        for (int j= compilerInvocationIdx+1; j < tokens.length; j++) {
			String token = tokens[j];
			if (token.equals(DASHIDASH)) {
			}
        	else if (token.startsWith(DASHI)) {
        		String candidate= null;
				if (token.length() > 2) {
					candidate= token.substring(2).trim();
				}
				else if (j+1 < tokens.length) {
					candidate= tokens[j+1];
					if (candidate.startsWith("-")) { //$NON-NLS-1$
						candidate= null;
					}
					else {
						j++;
					}
				}
				if (candidate != null && candidate.length() > 0) {
					if (fUtil != null) {
						candidate= fUtil.normalizePath(candidate);
					}
					if (!includes.contains(candidate)) {
						includes.add(candidate);
					}
				}
        	}
        	else if (token.startsWith(DASHD)) {
        		String candidate= null;
				if (token.length() > 2) {
					candidate= token.substring(2).trim();
				}
				else if (j+1 < tokens.length) {
					candidate= tokens[j+1];
					if (candidate.startsWith("-")) { //$NON-NLS-1$
						candidate= null;
					}
					else {
						j++;
					}
				}
        		if (candidate != null && candidate.length() > 0) {
        			if (!symbols.contains(candidate)) {
        				symbols.add(candidate);
        			}
        		}
        	}
			else if (token.startsWith("-m") ||		 //$NON-NLS-1$
        			token.equals("-ansi") ||		 //$NON-NLS-1$
        			token.equals("-nostdinc") ||	 //$NON-NLS-1$
        			token.equals("-posix") ||		 //$NON-NLS-1$
        			token.equals("-pthread") ||		 //$NON-NLS-1$
        			token.startsWith("-O") ||		 //$NON-NLS-1$
        			token.equals("-fno-inline") ||	 //$NON-NLS-1$
        			token.startsWith("-finline") ||	 //$NON-NLS-1$
        			token.equals("-fno-exceptions") ||	 //$NON-NLS-1$
        			token.equals("-fexceptions") ||		 //$NON-NLS-1$
        			token.equals("-fshort-wchar") ||	 //$NON-NLS-1$
        			token.equals("-fshort-double") ||	 //$NON-NLS-1$
        			token.equals("-fno-signed-char") ||	 //$NON-NLS-1$
        			token.equals("-fsigned-char") ||	 //$NON-NLS-1$
        			token.startsWith("-fabi-version="))	{  //$NON-NLS-1$
        		if (!targetSpecificOptions.contains(token))
        			targetSpecificOptions.add(token);
        	}
        	else if (fileName == null) {
        		String possibleFileName = token.toLowerCase();
        		if (possibleFileName.endsWith(".c") || 		 //$NON-NLS-1$
        				possibleFileName.endsWith(".cpp") ||	 //$NON-NLS-1$
        				possibleFileName.endsWith(".cc") ||		 //$NON-NLS-1$
        				possibleFileName.endsWith(".cxx") ||	 //$NON-NLS-1$
        				possibleFileName.endsWith(".C") ||		 //$NON-NLS-1$
        				possibleFileName.endsWith(".CPP") ||	 //$NON-NLS-1$
        				possibleFileName.endsWith(".CC") ||		 //$NON-NLS-1$
        				possibleFileName.endsWith(".CXX") ||	 //$NON-NLS-1$
        				possibleFileName.endsWith(".c++")) {	 //$NON-NLS-1$
        			fileName = token;
        		}
        	}
        }

        if (fileName != null && fileName.startsWith("/cygdrive/")) { //$NON-NLS-1$
        	fileName= AbstractGCCBOPConsoleParserUtility.convertCygpath(new Path(fileName)).toOSString();
        }
        if (fileName == null) {
        	return false;  // return when no file was given (analogous to GCCPerFileBOPConsoleParser)
        }

        IProject project = getProject();   
        IFile file = null;
        List translatedIncludes = includes;
        if (includes.size() > 0) {
        	if (fileName != null) {
        		if (fUtil != null) {
        			file = fUtil.findFile(fileName);
        			if (file != null) {
        				project = file.getProject();
        				translatedIncludes = fUtil.translateRelativePaths(file, fileName, includes);
        			}
        		}
        	}
        	else {
        		StringBuffer line= new StringBuffer();
        		for (int j = 0; j < tokens.length; j++) {
					line.append(tokens[j]);
					line.append(' ');
				}
        		final String error = MakeMessages.getString("ConsoleParser.Filename_Missing_Error_Message"); //$NON-NLS-1$ 
        		TraceUtil.outputError(error, line.toString());
        		if (fUtil != null) {
        			fUtil.generateMarker(getProject(), -1, error + line.toString(), IMarkerGenerator.SEVERITY_WARNING, null);
        		}
        	}
        	if (file == null && fUtil != null) {	// real world case
        		// remove include paths since there was no chance to translate them
        		translatedIncludes.clear();
        	}
        }
        // Contribute discovered includes and symbols to the ScannerInfoCollector
        if (translatedIncludes.size() > 0 || symbols.size() > 0) {
        	Map scannerInfo = new HashMap();
        	scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, translatedIncludes);
        	scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
        	scannerInfo.put(ScannerInfoTypes.TARGET_SPECIFIC_OPTION, targetSpecificOptions);
        	getCollector().contributeToScannerConfig(project, scannerInfo);

        	TraceUtil.outputTrace("Discovered scanner info for file \'" + fileName + '\'',	//$NON-NLS-1$
        			"Include paths", includes, translatedIncludes, "Defined symbols", symbols);	//$NON-NLS-1$ //$NON-NLS-2$
        }
		return true;
	}
}
