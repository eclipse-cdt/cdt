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
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;


/**
 * A 'provider' that will generate a special makefile to generate scanner config
 * 
 * @author vhirsl
 */
public class SCDMakefileGenerator extends DefaultRunSIProvider {
    private static final String ENDL = System.getProperty("line.separator"); //$NON-NLS-1$
    private static final String DENDL = ENDL+ENDL;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig2.DefaultRunSIProvider#initialize()
     */
    protected boolean initialize() {
        boolean rc = super.initialize();
        
        if (rc) {
            fWorkingDirectory = MakeCorePlugin.getWorkingDirectory();
            // replace string variables in compile arguments
            // TODO Vmir - use string variable replacement
            for (int i = 0; i < fCompileArguments.length; ++i) {
                fCompileArguments[i] = fCompileArguments[i].replaceAll("\\$\\{project_name\\}",    //$NON-NLS-1$ 
                        resource.getProject().getName());
            }
            rc = generateMakefile(resource.getProject().getName());
        }
        return rc;
    }

    /**
     * @param name
     * @return
     */
    private boolean generateMakefile(String projectName) {
        boolean rc = false;
        if (collector instanceof IScannerInfoCollector2) {
            IScannerInfoCollector2 collector2 = (IScannerInfoCollector2) collector;
            List commands = collector2.getCollectedScannerInfo(
                    resource.getProject(), ScannerInfoTypes.COMPILER_COMMAND);
            if (commands != null && commands.size() > 0) {
        
                StringBuffer buffer = new StringBuffer();
                buffer.append("# This is a generated file. Please do not edit."); //$NON-NLS-1$
                buffer.append(DENDL);
                buffer.append(".PHONY: all"); //$NON-NLS-1$
                buffer.append(DENDL);
                buffer.append("COMMANDS := "); //$NON-NLS-1$
                for (Iterator i = commands.iterator(); i.hasNext(); ) {
                    CCommandDSC cmd = (CCommandDSC) i.next();
                    buffer.append("\t\\"+ENDL+"\t    scd_cmd_"); //$NON-NLS-1$ //$NON-NLS-2$
                    buffer.append(cmd.getCommandId());
                }
                buffer.append(DENDL);
                buffer.append("all: $(COMMANDS)"); //$NON-NLS-1$
                buffer.append(DENDL);
                for (Iterator i = commands.iterator(); i.hasNext(); ) {
                    CCommandDSC cmd = (CCommandDSC) i.next();
                    buffer.append("scd_cmd_"); //$NON-NLS-1$
                    buffer.append(cmd.getCommandId());
                    buffer.append(':');
                    buffer.append(ENDL);
                    buffer.append("\t@echo begin generating scanner info for $@"+ENDL+"\t"); //$NON-NLS-1$ //$NON-NLS-2$
                    buffer.append(cmd);
                    buffer.append(" -E -P -v -dD "); //$NON-NLS-1$
                    buffer.append(cmd.appliesToCFileType() ? "specs.c" : "specs.cpp"); //$NON-NLS-1$ //$NON-NLS-2$
                    buffer.append(ENDL);
                    buffer.append("\t@echo end generating scanner info for $@"); //$NON-NLS-1$
                    buffer.append(DENDL);
                }
                
                File makefile = new File(fWorkingDirectory.toFile(), projectName+"_scd.mk"); //$NON-NLS-1$
                try {
                    PrintStream ps = new PrintStream(new FileOutputStream(makefile));
                    ps.println(buffer.toString());
                    ps.close();
                    rc = true;
                }
                catch (FileNotFoundException e) {
                    MakeCorePlugin.log(e);
                }
            }
        }
        
        return rc;
    }
}
