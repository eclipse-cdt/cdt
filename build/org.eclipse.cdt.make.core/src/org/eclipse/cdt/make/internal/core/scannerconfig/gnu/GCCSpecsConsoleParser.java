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

	private IProject fProject = null;
	private IScannerInfoConsoleParserUtility fUtil = null;
	private IScannerInfoCollector fCollector = null;
	
	private boolean expectingIncludes = false;
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

		// contribution of -dD option
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
		// now get all the includes
		else if (line.startsWith(INCLUDE) && line.endsWith("search starts here:")) { //$NON-NLS-1$
			expectingIncludes = true;
		}
		else if (line.startsWith("End of search list.")) {	//$NON-NLS-1$
			expectingIncludes = false;
		}
		else if (expectingIncludes) {
			if (!includes.contains(line))
				includes.add(line);
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
