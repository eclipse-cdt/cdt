/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Console parser for generated makefile output
 * 
 * @author vhirsl
 */
public class GCCPerFileSIPConsoleParser implements IScannerInfoConsoleParser {
    private final static String INCLUDE_PREAMBLE = "#include <...>"; //$NON-NLS-1$
    private final static String QUOTE_INCLUDE_PREAMBLE = "#include \"...\""; //$NON-NLS-1$
    private final static String DEFINE_PREAMBLE = "#define"; //$NON-NLS-1$
    private final static String COMMAND_ID_BEGIN = "begin generating scanner info for scd_cmd_"; //$NON-NLS-1$
    private final static String COMMAND_ID_END = "end generating scanner info for scd_cmd_"; //$NON-NLS-1$

    private final static int NO_INCLUDES = 0;
    private final static int QUOTE_INCLUDES = 1;
    private final static int INCLUDES = 2;
    
    private IProject fProject = null;
    private IScannerInfoCollector fCollector = null;
    
    private int expectingIncludes = NO_INCLUDES;
    private List symbols;
    private List includes;
    private List quoteIncludes;
    private int commandId = -1;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IPath, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector, org.eclipse.cdt.core.IMarkerGenerator)
     */
    public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector, IMarkerGenerator markerGenerator) {
        this.fProject = project;
        this.fCollector = collector;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParser#processLine(java.lang.String)
     */
    public boolean processLine(String line) {
        boolean rc = false;
        TraceUtil.outputTrace("GCCPerFileSIPConsoleParser parsing line:", TraceUtil.EOL, line);  //$NON-NLS-1$ //$NON-NLS-2$

        if (line.startsWith(COMMAND_ID_BEGIN)) {
            commandId = Integer.parseInt(line.substring(COMMAND_ID_BEGIN.length()));
            symbols = new ArrayList();
            includes = new ArrayList();
            quoteIncludes = new ArrayList();
        }
        else if (line.startsWith(COMMAND_ID_END)) {
            Map scannerInfo = new HashMap();
            scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, includes);
            scannerInfo.put(ScannerInfoTypes.QUOTE_INCLUDE_PATHS, quoteIncludes);
            scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
            fCollector.contributeToScannerConfig(new Integer(commandId), scannerInfo);
            commandId = -1;
            rc = true;
        }
        // contribution of -dD option
        else if (line.startsWith(DEFINE_PREAMBLE)) {
            String[] defineParts = line.split("\\s+", 3); //$NON-NLS-1$
            if (defineParts[0].equals(DEFINE_PREAMBLE)) {
                String symbol = null;
                switch (defineParts.length) {
                    case 2:
                        symbol = defineParts[1];
                        break;
                    case 3:
                        symbol = defineParts[1] + "=" + defineParts[2]; //$NON-NLS-1$
                        break;
                }
                if (symbol != null && !symbols.contains(symbol)) { //$NON-NLS-1$
                    symbols.add(symbol);
                }
            }
        }
        // now get all the includes
        else if (line.startsWith(QUOTE_INCLUDE_PREAMBLE) && line.endsWith("search starts here:")) { //$NON-NLS-1$
            expectingIncludes = QUOTE_INCLUDES;
        }
        else if (line.startsWith(INCLUDE_PREAMBLE) && line.endsWith("search starts here:")) { //$NON-NLS-1$
            expectingIncludes = INCLUDES;
        }
        else if (line.startsWith("End of search list.")) {  //$NON-NLS-1$
            expectingIncludes = NO_INCLUDES;
        }
        else if (expectingIncludes == QUOTE_INCLUDES) {
            if (!quoteIncludes.contains(line))
                quoteIncludes.add(line);
        }
        else if (expectingIncludes == INCLUDES) {
            if (!includes.contains(line))
                includes.add(line);
        }
            
        return rc;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
     */
    public void shutdown() {
//        Map scannerInfo = new HashMap();
//        scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, includes);
//        scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
//        fCollector.contributeToScannerConfig(fProject, scannerInfo);
//        TraceUtil.outputTrace("Scanner info from \'specs\' file",   //$NON-NLS-1$
//                "Include paths", includes, new ArrayList(), "Defined symbols", symbols);    //$NON-NLS-1$ //$NON-NLS-2$);
    }

}
