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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParserUtility;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.ScannerInfoConsoleParserUtility;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parses gcc and g++ output for -I and -D parameters.
 * 
 * @author vhirsl
 */
public class GCCScannerInfoConsoleParser implements IScannerInfoConsoleParser {
	private final static String SINGLE_QUOTE_STRING = "\'"; //$NON-NLS-1$
	private final static String DOUBLE_QUOTE_STRING = "\""; //$NON-NLS-1$
	
	private IProject fProject = null;
	private ScannerInfoConsoleParserUtility fUtil = new ScannerInfoConsoleParserUtility();
	private IScannerInfoCollector fCollector = null;
	
	private boolean bMultiline = false;
	private String sMultiline = ""; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#getUtility()
	 */
	public IScannerInfoConsoleParserUtility getUtility() {
		return fUtil;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject, org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParserUtility, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector)
	 */
	public void startup(IProject project, IScannerInfoCollector collector) {
		fProject = project;
		fCollector = collector;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.ScannerInfoConsoleParserUtility#processLine(java.lang.String)
	 */
	public boolean processLine(String line) {
		boolean rc = false;
		// check for multiline commands (ends with '\')
		if (line.endsWith("\\")) { //$NON-NLS-1$
			sMultiline += line.substring(0, line.length()-1);// + " "; //$NON-NLS-1$
			bMultiline = true;
			return rc;
		}
		if (bMultiline) {
			line = sMultiline + line;
			bMultiline = false;
			sMultiline = ""; //$NON-NLS-1$
		}
		TraceUtil.outputTrace("GCCScannerInfoConsoleParser parsing line:", TraceUtil.EOL, line);	//$NON-NLS-1$ //$NON-NLS-2$
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
					if (fUtil != null) {
						fUtil.changeMakeDirectory(dir, getDirectoryLevel(line), enter);
					}
			    	return rc;
				}
			}
		}
		// Known patterns:
		// (a) gcc|g++ ... -Dxxx -Iyyy ...
		ArrayList allTokens = new ArrayList(Arrays.asList(line.split("\\s")));//$NON-NLS-1$
		if (allTokens.size() <= 1)
			return false;
		Iterator I = allTokens.iterator();
		String token = ((String) I.next()).toLowerCase();
		if (token.indexOf("gcc") != -1 || token.indexOf("g++") != -1 || token.indexOf("gcc") != -1) {//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
				if (token.equals("-mwin32") ||		//$NON-NLS-1$
					token.equals("-mno-win32") ||	//$NON-NLS-1$
					token.equals("-mno-cygwin") ||	//$NON-NLS-1$
					token.equals("-ansi") ||		//$NON-NLS-1$
					token.equals("-nostdinc") ||	//$NON-NLS-1$
					token.equals("-posix") ||		//$NON-NLS-1$
					token.equals("-pthread")) {		//$NON-NLS-1$
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
						possibleFileName.endsWith(".cxx") ||	//$NON-NLS-1$
						possibleFileName.endsWith(".C") ||		//$NON-NLS-1$
						possibleFileName.endsWith(".CC")) {		//$NON-NLS-1$
						
						fileName = token;
					}
				}
			}
			
			IProject project = fProject;   
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
						fUtil.generateMarker(fProject, -1, error + line, IMarkerGenerator.SEVERITY_WARNING, null);
					}
				}
				if (file == null && fUtil != null) {	// real world case
					// remove include paths since there was no chance to translate them
					translatedIncludes.clear();
				}
			}
			// Contribute discovered includes and symbols to the ScannerInfoCollector
			if (translatedIncludes.size() > 0 || symbols.size() > 0) {
				Map extraInfo = new HashMap();
				extraInfo.put(IScannerInfoCollector.TARGET_SPECIFIC_OPTION, targetSpecificOptions);
				fCollector.contributeToScannerConfig(project, translatedIncludes, symbols, extraInfo);
				
				TraceUtil.outputTrace("Discovered scanner info for file \'" + fileName + '\'',	//$NON-NLS-1$
						"Include paths", includes, translatedIncludes, "Defined symbols", symbols);	//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return rc;
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
			String delimiter = "\\s"; //$NON-NLS-1$
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
		for (int index = line.indexOf(fDashD, prevIndex); index != -1; 
			 prevIndex = index+2, index = line.indexOf(fDashD, prevIndex)) {
			String delimiter = "\\s"; //$NON-NLS-1$
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
				String[] tokens = postfix.split(delimiter, 2);
				if (tokens.length > 0 && tokens[0].length() > 0) {
					int sQuoteIndex = tokens[0].indexOf(SINGLE_QUOTE_STRING);
					int dQuoteIndex = tokens[0].indexOf(DOUBLE_QUOTE_STRING);
					if (sQuoteIndex == -1 && dQuoteIndex == -1) {
						// simple case, no quotes
						if (!symbols.contains(tokens[0])) {
							symbols.add(tokens[0]);
						}
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
