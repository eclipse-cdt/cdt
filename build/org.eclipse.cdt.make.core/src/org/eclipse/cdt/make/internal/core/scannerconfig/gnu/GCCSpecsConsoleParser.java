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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParserUtility;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;

/**
 * Parses output of gcc -c -v specs.c or
 *                  g++ -c -v specs.cpp
 * command
 * 
 * @author vhirsl
 */
public class GCCSpecsConsoleParser implements IScannerInfoConsoleParser {
	private final int STATE_BEGIN = 0;
	private final int STATE_SPECS_STARTED = 1;
	private final int STATE_INCLUDES_STARTED = 2;

	private IProject fProject = null;
	private IScannerInfoConsoleParserUtility fUtil = null;
	private IScannerInfoCollector fCollector = null;
	
	private int state = STATE_BEGIN;
	private List symbols = new ArrayList();
	private List includes = new ArrayList();

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject, org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParserUtility, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector)
	 */
	public void startup(IProject project, IScannerInfoConsoleParserUtility util, IScannerInfoCollector collector) {
		this.fProject = project;
		this.fUtil = util;
		this.fCollector = collector;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParser#processLine(java.lang.String)
	 */
	public boolean processLine(String line) {
		boolean rc = false;
		TraceUtil.outputTrace("GCCSpecsConsoleParser parsing line:", TraceUtil.EOL, line);	//$NON-NLS-1$ //$NON-NLS-2$
		// Known patterns:
		// (a) gcc|g++ ... -Dxxx -Iyyy ...
		switch (state) {
			case STATE_BEGIN:
				if (line.startsWith("Reading specs from")) {	//$NON-NLS-1$
					state = STATE_SPECS_STARTED;
				}
				return rc;
			case STATE_SPECS_STARTED: 
				if (line.indexOf("-D") != -1) {	//$NON-NLS-1$
					// line contains -Ds, extract them
					StringTokenizer scanner = new StringTokenizer(line);
					if (scanner.countTokens() <= 1)
						return rc;
					for (String token = scanner.nextToken(); scanner.hasMoreTokens(); token = scanner.nextToken()) {
						if (token.startsWith("-D")) {	//$NON-NLS-1$
							String symbol = token.substring(2);
							if (!symbols.contains(symbol))
								symbols.add(symbol);
						}
					}
				}
				// now get all the includes
				if (line.startsWith("#include") && line.endsWith("search starts here:")) { //$NON-NLS-1$ //$NON-NLS-2$
					state = STATE_INCLUDES_STARTED;
				}
				return rc;
			case STATE_INCLUDES_STARTED:
				if (line.startsWith("#include") && line.endsWith("search starts here:")) { //$NON-NLS-1$ //$NON-NLS-2$
					state = STATE_INCLUDES_STARTED;
				}
				else if (line.startsWith("End of search list.")) {	//$NON-NLS-1$
					state = STATE_BEGIN;
					break;
				}
				else {
					if (!includes.contains(line))
						includes.add(line);
				}
				return rc;
		}
			
		fCollector.contributeToScannerConfig(fProject, includes, symbols, new HashMap());
		TraceUtil.outputTrace("Scanner info from \'specs\' file",	//$NON-NLS-1$
				"Include paths", includes, new ArrayList(), "Defined symbols", symbols);	//$NON-NLS-1$ //$NON-NLS-2$);
		
		return rc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
	 */
	public void shutdown() {
		if (fUtil != null) {
			fUtil.reportProblems();
		}
	}
}
