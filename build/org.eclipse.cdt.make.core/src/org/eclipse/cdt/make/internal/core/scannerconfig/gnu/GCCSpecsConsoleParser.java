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
	private final String INCLUDE = "#include"; //$NON-NLS-1$
	private final String DEFINE = "#define"; //$NON-NLS-1$

	private final int STATE_BEGIN = 0;
	private final int STATE_SPECS_STARTED = 1;
	private final int STATE_INCLUDES_STARTED = 2;
	private final int STATE_ADDITIONAL_DEFINES_STARTED = 3;

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
				break;
			case STATE_SPECS_STARTED: 
				if (line.indexOf("-D") != -1) {	//$NON-NLS-1$
					// line contains -Ds, extract them
					String[] tokens = line.split("\\s+");//$NON-NLS-1$
					for (int i = 0; i < tokens.length; ++i) {
						if (tokens[i].startsWith("-D")) {	//$NON-NLS-1$
							String symbol = tokens[i].substring(2);
							if (!symbols.contains(symbol))
								symbols.add(symbol);
						}
					}
				}
				// now get all the includes
				if (line.startsWith(INCLUDE) && line.endsWith("search starts here:")) { //$NON-NLS-1$
					state = STATE_INCLUDES_STARTED;
				}
				break;
			case STATE_INCLUDES_STARTED:
				if (line.startsWith(INCLUDE) && line.endsWith("search starts here:")) { //$NON-NLS-1$
					state = STATE_INCLUDES_STARTED;
				}
				else if (line.startsWith("End of search list.")) {	//$NON-NLS-1$
					state = STATE_ADDITIONAL_DEFINES_STARTED;
				}
				else {
					if (!includes.contains(line))
						includes.add(line);
				}
				break;
			case STATE_ADDITIONAL_DEFINES_STARTED:
				if (line.startsWith(DEFINE)) {
					String[] defineParts = line.split("\\s+", 3); //$NON-NLS-1$
					if (defineParts[0].equals(DEFINE)) {
						String symbol = null;
						switch (defineParts.length) {
							case 2:
								symbol = defineParts[1];
								break;
							case 3:
								symbol = defineParts[1] + "=" + defineParts[2];
								break;
						}
						if (symbol != null && !symbols.contains(symbol)) { //$NON-NLS-1$
							symbols.add(symbol);
						}
					}
				}
				break;
		}
			
		return rc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
	 */
	public void shutdown() {
		fCollector.contributeToScannerConfig(fProject, includes, symbols, new HashMap());
		TraceUtil.outputTrace("Scanner info from \'specs\' file",	//$NON-NLS-1$
				"Include paths", includes, new ArrayList(), "Defined symbols", symbols);	//$NON-NLS-1$ //$NON-NLS-2$);
		if (fUtil != null) {
			fUtil.reportProblems();
		}
	}
}
