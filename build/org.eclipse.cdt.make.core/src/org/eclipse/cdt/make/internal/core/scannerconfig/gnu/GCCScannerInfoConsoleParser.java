/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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

/**
 * Parses gcc and g++ output for -I and -D parameters.
 * 
 * @author vhirsl
 */
public class GCCScannerInfoConsoleParser extends AbstractGCCBOPConsoleParser {
	private final static String SINGLE_QUOTE_STRING = "\'"; //$NON-NLS-1$
	private final static String DOUBLE_QUOTE_STRING = "\""; //$NON-NLS-1$
	private final static char[] matchingChars = {'`', '\'', '\"'};
	
    private String[] compilerInvocation;

    private ScannerInfoConsoleParserUtility fUtil = null;
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IPath, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector, org.eclipse.cdt.core.IMarkerGenerator)
     */
    public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector, IMarkerGenerator markerGenerator) {
        fUtil = (project != null && workingDirectory != null && markerGenerator != null) ?
                new ScannerInfoConsoleParserUtility(project, workingDirectory, markerGenerator) : null;
        super.startup(project, collector);

        // check additional compiler commands from extension point manifest
        compilerInvocation = getCompilerCommands();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.gnu.AbstractGCCBOPConsoleParser#getUtility()
     */
    protected AbstractGCCBOPConsoleParserUtility getUtility() {
        return fUtil;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.gnu.AbstractGCCBOPConsoleParser#processSingleLine(java.lang.String)
     */
    protected boolean processSingleLine(String line) {
		boolean rc = false;
		// Known patterns:
		// (a) gcc|g++ ... -Dxxx -Iyyy ...
		List allTokens = tokenize(line);
//		ArrayList allTokens = new ArrayList(Arrays.asList(line.split("\\s+")));//$NON-NLS-1$
		if (allTokens.size() <= 1)
			return false;
		Iterator I = allTokens.iterator();
		String token = ((String) I.next()).toLowerCase();
        
        boolean found = false;
        for (int i = 0; i < compilerInvocation.length; i++) {
            if (token.indexOf(compilerInvocation[i]) != -1) {
                found = true;
                break;
            }
        }
		if (found) {
			// Recognized gcc or g++ compiler invocation
			List includes = new ArrayList();
			List symbols = new ArrayList();
			List targetSpecificOptions = new ArrayList();

			rc = true;
			String fileName = null;
			// discover all -I options
			parseLineForIncludePaths(line, includes);
			// discover all -D options
			parseLineForSymbolDefinitions(line, symbols);
			
			while (I.hasNext()) {
				token = (String) I.next();
				if (token.startsWith("-m") ||		//$NON-NLS-1$
					token.equals("-ansi") ||		//$NON-NLS-1$
					token.equals("-nostdinc") ||	//$NON-NLS-1$
					token.equals("-posix") ||		//$NON-NLS-1$
					token.equals("-pthread")) {		//$NON-NLS-1$
					if (!targetSpecificOptions.contains(token))
						targetSpecificOptions.add(token);
				}
				else if (fileName == null) {
					String possibleFileName = token.toLowerCase();
					if ((possibleFileName.startsWith(DOUBLE_QUOTE_STRING) && 
						 possibleFileName.endsWith(DOUBLE_QUOTE_STRING)) ||
						(possibleFileName.startsWith(SINGLE_QUOTE_STRING) && 
						 possibleFileName.endsWith(SINGLE_QUOTE_STRING))) {
						possibleFileName = possibleFileName.substring(1, possibleFileName.length()-1).trim();
					}
					if (possibleFileName.endsWith(".c") || 		//$NON-NLS-1$
						possibleFileName.endsWith(".cpp") ||	//$NON-NLS-1$
						possibleFileName.endsWith(".cc") ||		//$NON-NLS-1$
						possibleFileName.endsWith(".cxx") ||	//$NON-NLS-1$
						possibleFileName.endsWith(".C") ||		//$NON-NLS-1$
						possibleFileName.endsWith(".CPP") ||	//$NON-NLS-1$
						possibleFileName.endsWith(".CC") ||		//$NON-NLS-1$
						possibleFileName.endsWith(".CXX") ||	//$NON-NLS-1$
						possibleFileName.endsWith(".c++")) {	//$NON-NLS-1$
						
						fileName = token;
					}
				}
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
					final String error = MakeMessages.getString("ConsoleParser.Filename_Missing_Error_Message"); //$NON-NLS-1$ 
					TraceUtil.outputError(error, line);
					if (fUtil != null) {
						fUtil.generateMarker(getProject(), -1, error + line, IMarkerGenerator.SEVERITY_WARNING, null);
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
		}
		return rc;
	}

	/**
	 * @param line
	 * @return list of tokens
	 */
	private List tokenize(String line) {
		List rv = new ArrayList(2);
		// find special characters that need to be matched: `, ' and "
		// First Matching Chararcter
		int prevFmc = line.length();
		int emc = -1;
		char matchingChar = 0;
		for (int i = 0; i < matchingChars.length; ++i) {
			char ch = matchingChars[i];
			int fmc = line.indexOf(ch);
			if (fmc > -1 && fmc < prevFmc) {
				emc = line.indexOf(ch, fmc+1);
				if (emc > fmc) {
					matchingChar = ch;
					prevFmc = fmc;
				}
			}
		}
		if (matchingChar != 0) { // found matched chars
			String prefix = line.substring(0, prevFmc).trim();
			rv.addAll(Arrays.asList(prefix.split("\\s+")));//$NON-NLS-1$
			
			rv.add(line.substring(prevFmc, emc+1));
			
			// recursion
			rv.addAll(tokenize(line.substring(emc+1).trim()));
		}
		else {
			rv.addAll(Arrays.asList(line.split("\\s+")));//$NON-NLS-1$
		}
		return rv;
	}

	/**
	 * @param line
	 * @param includes
	 */
	private void parseLineForIncludePaths(String line, List includes) {
		final String fDashI = "-I"; //$NON-NLS-1$
		int prevIndex = 0;
		for (int index = line.indexOf(fDashI, prevIndex); index != -1; 
			 prevIndex = index+2, index = line.indexOf(fDashI, prevIndex)) {
			String delimiter = "\\s+"; //$NON-NLS-1$
			if (line.charAt(index-1) == '\'' || line.charAt(index-1) == '\"') {
				// look for only one more ' or "
				delimiter = String.valueOf(line.charAt(index-1));
			}
			String postfix = line.substring(index+2).trim();
			if (postfix.charAt(0) == '-') {	// empty -I
				continue;
			}
			if (postfix.startsWith(SINGLE_QUOTE_STRING) || postfix.startsWith(DOUBLE_QUOTE_STRING)) { //$NON-NLS-1$ //$NON-NLS-2$
				delimiter = postfix.substring(0, 1);
			}
			String[] tokens = postfix.split(delimiter);
			int tokIndex = (tokens.length > 1 && tokens[0].length() == 0) ? 1 : 0;
			String iPath = tokens[tokIndex];
			String temp = iPath;
			// check for '\ '
			for (++tokIndex; (temp.endsWith("\\") && tokIndex < tokens.length &&  //$NON-NLS-1$
							 tokens[tokIndex].length() > 0 && !tokens[tokIndex].startsWith("-")); //$NON-NLS-1$
				 ++tokIndex) { 
				int beg = postfix.indexOf(temp)+temp.length();
				int end = postfix.indexOf(tokens[tokIndex])+tokens[tokIndex].length();
				iPath = iPath.substring(0, iPath.length()-1) + postfix.substring(beg, end);
				temp += postfix.substring(beg, end);
			}
			String nPath = iPath;
			if (fUtil != null) {
				nPath = fUtil.normalizePath(iPath);
			}
			if (!includes.contains(nPath)) {
				includes.add(nPath);
			}
		}
	}

	/**
	 * @param line
	 * @param symbols
	 */
	private void parseLineForSymbolDefinitions(String line, List symbols) {
		final String fDashD = "-D"; //$NON-NLS-1$
		int prevIndex = 0;
		String delimiter = null;
		String splitRegex = "\\s+"; //$NON-NLS-1$
		for (int index = line.indexOf(fDashD, prevIndex); index != -1; 
			 prevIndex = index+2, index = line.indexOf(fDashD, prevIndex)) {
			int nDelimiterSymbols = 2;
			String postfix = line.substring(index+2).trim();
			if (postfix.charAt(0) == '-') {	// empty -D
				continue;
			}
			if (line.charAt(index-1) == '\'' || line.charAt(index-1) == '\"') {
				// look for only one more ' or "
				delimiter = String.valueOf(line.charAt(index-1)); 
				nDelimiterSymbols = 1;
			}
			else {
				String[] tokens = postfix.split(splitRegex, 2);
				if (tokens.length > 0 && tokens[0].length() > 0) {
					int sQuoteIndex = tokens[0].indexOf(SINGLE_QUOTE_STRING);
					int dQuoteIndex = tokens[0].indexOf(DOUBLE_QUOTE_STRING);
					if (sQuoteIndex == -1 && dQuoteIndex == -1) {
						// simple case, no quotes
						if (!symbols.contains(tokens[0])) {
							symbols.add(tokens[0]);
						}
						continue;
					}
					else {
						delimiter = (sQuoteIndex != -1 && (dQuoteIndex == -1 || sQuoteIndex < dQuoteIndex)) ? SINGLE_QUOTE_STRING : DOUBLE_QUOTE_STRING;
					}
				}
				else 
					continue;
			}
			
			// find next matching delimiter
			int nextDelimiterIndex = -1;
			int prevDelimiterIndex = -1;
			do {
				nextDelimiterIndex = postfix.indexOf(delimiter, nextDelimiterIndex+1);
				if (nextDelimiterIndex == 0 || (nextDelimiterIndex > 0 && postfix.charAt(nextDelimiterIndex-1) != '\\')) {
					--nDelimiterSymbols;
					if (nDelimiterSymbols > 0) {
						prevDelimiterIndex = nextDelimiterIndex;
					}
				}
			}
			while (nDelimiterSymbols > 0 && nextDelimiterIndex != -1);
			if (nDelimiterSymbols > 0) 
				continue; // non matching delimiter

			// take everything up to the last delimiter
			boolean bStartsWithDelimiter = postfix.startsWith(delimiter);
			String symbol = postfix.substring(bStartsWithDelimiter ? 1 : 0, nextDelimiterIndex);
			if (!bStartsWithDelimiter) {
				// there is still a delimiter to be removed
				if (prevDelimiterIndex != -1) {
					symbol = symbol.substring(0, prevDelimiterIndex) + symbol.substring(prevDelimiterIndex+1);
				}
			}
			// transform '\"' into '"'
			if (delimiter.equals(DOUBLE_QUOTE_STRING)) {
				symbol = symbol.replaceAll("\\\\\"", DOUBLE_QUOTE_STRING); //$NON-NLS-1$
			}
			if (!symbols.contains(symbol)) {
				symbols.add(symbol);
			}
		}
	}

}
