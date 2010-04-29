/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


/**
 * A 'provider' that will generate a special makefile to generate scanner config
 * 
 * @author vhirsl
 */
public class SCDMakefileGenerator extends DefaultRunSIProvider {
    private static final String ENDL = System.getProperty("line.separator"); //$NON-NLS-1$
    private static final String DENDL = ENDL+ENDL;
    private String fMakeCommand = "-f ${project_name}_scd.mk "; //$NON-NLS-1$
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig2.DefaultRunSIProvider#initialize()
     */
    @Override
	protected boolean initialize() {
    	String args = buildInfo.getProviderRunArguments(providerId);
    	if (null == args)
    		args = " -E -P -v -dD "; //$NON-NLS-1$
    	else {
	        int nPos = args.indexOf('|');
	        if(nPos > 0) {
	        	fMakeCommand = args.substring(0, nPos);
	        	args = args.substring(nPos + 1);
	        }
    	}
        fCompileCommand = new Path(buildInfo.getProviderRunCommand(providerId));
    	args = substituteDynamicVariables(args);
        fCompileArguments = ScannerConfigUtil.tokenizeStringWithQuotes(args, "\"");//$NON-NLS-1$
        fWorkingDirectory = MakeCorePlugin.getWorkingDirectory();
        fMakeCommand = substituteDynamicVariables(fMakeCommand); 
        
        return generateMakefile(resource.getProject().getName());
    }

    private boolean generateMakefile(String projectName) {
        boolean rc = false;
        if (collector instanceof IScannerInfoCollector2) {
            IScannerInfoCollector2 collector2 = (IScannerInfoCollector2) collector;
            List<CCommandDSC> commands = collector2.getCollectedScannerInfo(
                    resource.getProject(), ScannerInfoTypes.UNDISCOVERED_COMPILER_COMMAND);
            if (commands != null && commands.size() > 0) {
        
                StringBuffer buffer = new StringBuffer();
                buffer.append("# This is a generated file. Please do not edit."); //$NON-NLS-1$
                buffer.append(DENDL);
                buffer.append(".PHONY: all"); //$NON-NLS-1$
                buffer.append(DENDL);
                buffer.append("COMMANDS := "); //$NON-NLS-1$
                for (CCommandDSC cmd : commands) {
                    buffer.append("\t\\"+ENDL+"\t    scd_cmd_"); //$NON-NLS-1$ //$NON-NLS-2$
                    buffer.append(cmd.getCommandId());
                }
                buffer.append(DENDL);
                buffer.append("all: $(COMMANDS)"); //$NON-NLS-1$
                buffer.append(DENDL);
                for (CCommandDSC cmd : commands) {
                    buffer.append("scd_cmd_"); //$NON-NLS-1$
                    buffer.append(cmd.getCommandId());
                    buffer.append(':');
                    buffer.append(ENDL);
                    buffer.append("\t@echo begin generating scanner info for $@"+ENDL+"\t"); //$NON-NLS-1$ //$NON-NLS-2$
                    buffer.append(cmd.getSCDRunnableCommand(true, true)); // quote includes and defines
                    for (String arg : prepareArguments(buildInfo.isUseDefaultProviderCommand(providerId))) { 
                    	buffer.append(' ');
                    	buffer.append(arg);
                    }
                    buffer.append(' ');
                    buffer.append(cmd.appliesToCPPFileType() ? "specs.cpp" : "specs.c"); //$NON-NLS-1$ //$NON-NLS-2$
                    buffer.append(ENDL);
                    buffer.append("\t@echo end generating scanner info for $@"); //$NON-NLS-1$
                    buffer.append(DENDL);
                }
                
                File makefile = new File(fWorkingDirectory.toFile(), getMakeFileName(projectName));
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
    
    private String getMakeFileName(String projectName) {
        String[] makeArgs = ScannerConfigUtil.tokenizeStringWithQuotes(fMakeCommand, "\"");//$NON-NLS-1$
        boolean found = false;
        for(String arg : makeArgs) { 
        	if(found)
        		return arg;
        	if(arg.equals("-f")) //$NON-NLS-1$
        		found = true;
        }
        return projectName+"_scd.mk"; //$NON-NLS-1$
    }
    

    protected String substituteDynamicVariables(String in) {
    	String string = in;
// 		TODO: replace it with Eclipse Dynamic Variable Resolver
//      string = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(string, false);
        string = string.replaceAll("\\$\\{project_name\\}",    //$NON-NLS-1$ 
                resource.getProject().getName());
        string = string.replaceAll("\\$\\{plugin_state_location\\}",    //$NON-NLS-1$ 
                MakeCorePlugin.getWorkingDirectory().toString());
        string = string.replaceAll("\\$\\{specs_file\\}",  //$NON-NLS-1$
        		GCCScannerConfigUtil.C_SPECS_FILE );
        return string;
    }

	@Override
	protected String[] getCommandLineOptions() {
		return ScannerConfigUtil.tokenizeStringWithQuotes(fMakeCommand, "\"");		 //$NON-NLS-1$
	}

	@Override
	protected IPath getCommandToLaunch() {
		return new Path("make");	 //$NON-NLS-1$
	}
}
