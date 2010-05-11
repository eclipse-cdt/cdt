/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.xlc.core.scannerconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.make.xlc.core.activator.Activator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author crecoskie
 *
 */
public class XLCPerProjectBuildOutputParser extends
		AbstractXLCBuildOutputParser {
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.xlc.core.scannerconfig.AbstractXLCBuildOutputParser#processCommand(java.lang.String[])
	 */
	@Override
    protected boolean processCommand(String[] tokens) {
        int compilerInvocationIdx= findCompilerInvocation(tokens);
        if (compilerInvocationIdx<0) {
        	return false;
        }
        
        if (compilerInvocationIdx+1 >= tokens.length) {
        	return false;
        }

        // Recognized gcc or g++ compiler invocation
        List<String> includes = new ArrayList<String>();
        List<String> symbols = new ArrayList<String>();
        List<String> targetSpecificOptions = new ArrayList<String>();

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
					if (getUtility() != null) {
						candidate= getUtility().normalizePath(candidate);
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
        			if (candidate.indexOf('=') == -1) {
        				candidate+= '='+ getUtility().getDefaultMacroDefinitionValue();
        			}
        			if (!symbols.contains(candidate)) {
        				symbols.add(candidate);
        			}
        		}
        	}
			
        	else if (fileName == null) {
        		int extIndex = token.lastIndexOf('.');
        		String extension=null;
        		
        		if(extIndex != -1)
        			extension = token.substring(extIndex);
        		
        		List<String> extensions = getFileExtensionsList();
        		if(extension != null && extensions.contains(extension))
        			fileName = token;
        	}
        }

        if (fileName == null) {
        	return false;  // return when no file was given (analogous to GCCPerFileBOPConsoleParser)
        }

        IProject project = getProject();   
        IFile file = null;
        List<String> translatedIncludes = includes;
        if (includes.size() > 0) {
        	if (fileName != null) {
        		if (getUtility() != null) {
        			file = getUtility().findFile(fileName);
        			if (file != null) {
        				project = file.getProject();
        				translatedIncludes = getUtility().translateRelativePaths(file, fileName, includes);
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
        		if (getUtility() != null) {
        			getUtility().generateMarker(getProject(), -1, error + line.toString(), IMarkerGenerator.SEVERITY_WARNING, null);
        		}
        	}
        	if (file == null && getUtility() != null) {	// real world case
        		// remove include paths since there was no chance to translate them
        		translatedIncludes.clear();
        	}
        }
        // Contribute discovered includes and symbols to the ScannerInfoCollector
        if (translatedIncludes.size() > 0 || symbols.size() > 0) {
        	Map<ScannerInfoTypes, List<String>> scannerInfo = new HashMap<ScannerInfoTypes, List<String>>();
        	scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, translatedIncludes);
        	scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
        	scannerInfo.put(ScannerInfoTypes.TARGET_SPECIFIC_OPTION, targetSpecificOptions);
        	getCollector().contributeToScannerConfig(project, scannerInfo);
        	if(fCollector != null && fCollector instanceof IScannerInfoCollector2) {
    			IScannerInfoCollector2 collector = (IScannerInfoCollector2) fCollector;
    			try {
    				collector.updateScannerConfiguration(null);
    			} catch (CoreException e) {
    				// TODO Auto-generated catch block
    				Activator.log(e);
    			}
    		}

        	TraceUtil.outputTrace("Discovered scanner info for file \'" + fileName + '\'',	//$NON-NLS-1$
        			"Include paths", includes, translatedIncludes, "Defined symbols", symbols);	//$NON-NLS-1$ //$NON-NLS-2$
        }
		return true;

}


	
}
