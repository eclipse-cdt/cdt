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

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;

/**
 * Common stuff for all GNU build output parsers
 * 
 * @author vhirsl
 */
public abstract class AbstractGCCBOPConsoleParser implements IScannerInfoConsoleParser {
    private IProject project;
    private IScannerInfoCollector collector;
    
    private boolean bMultiline = false;
    private String sMultiline = ""; //$NON-NLS-1$

    /**
     * @return Returns the project.
     */
    protected IProject getProject() {
        return project;
    }
    /**
     * @return Returns the collector.
     */
    protected IScannerInfoCollector getCollector() {
        return collector;
    }

    public void startup(IProject project, IScannerInfoCollector collector) {
        this.project = project;
        this.collector = collector;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#processLine(java.lang.String)
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
        TraceUtil.outputTrace("AbstractGCCBOPConsoleParser parsing line: [", line, "]");    //$NON-NLS-1$ //$NON-NLS-2$
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
                    if (getUtility() != null) {
                        getUtility().changeMakeDirectory(dir, getDirectoryLevel(line), enter);
                    }
                    return rc;
                }
            }
        }
        // call sublclass to process a single line
        return processSingleLine(line);
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

    protected abstract boolean processSingleLine(String line);
    protected abstract AbstractGCCBOPConsoleParserUtility getUtility();
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
     */
    public void shutdown() {
        if (getUtility() != null) {
            getUtility().reportProblems();
        }
    }
}
