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

import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerConfigUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Runs a command to retrieve compiler intrinsic scanner info from 'specs' file.
 * 
 * @author vhirsl
 */
public class GCCSpecsRunSIProvider extends DefaultRunSIProvider {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig2.DefaultRunSIProvider#initialize()
     */
    protected boolean initialize() {
        boolean rc = super.initialize();
        
        if (rc) {
            String targetFile = "dummy";    //$NON-NLS-1$
            IProject project = resource.getProject();
            try {
                if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
                    targetFile = GCCScannerConfigUtil.CPP_SPECS_FILE;
                }
                else if (project.hasNature(CProjectNature.C_NATURE_ID)) {
                    targetFile = GCCScannerConfigUtil.C_SPECS_FILE;
                }
                IPath path2File = MakeCorePlugin.getWorkingDirectory().append(targetFile);
                if (!path2File.toFile().exists()) {
                    GCCScannerConfigUtil.createSpecs();
                }
                // replace string variables in compile arguments
                // TODO Vmir - use string variable replacement
                for (int i = 0; i < fCompileArguments.length; ++i) {
                    fCompileArguments[i] = fCompileArguments[i].replaceAll("\\$\\{plugin_state_location\\}",    //$NON-NLS-1$ 
                            MakeCorePlugin.getWorkingDirectory().toString());
                    fCompileArguments[i] = fCompileArguments[i].replaceAll("\\$\\{specs_file\\}", targetFile);  //$NON-NLS-1$
                }
            } catch (CoreException e) {
                //TODO VMIR better error handling
                MakeCorePlugin.log(e.getStatus());
                rc = false;
            }
        }
        return rc;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig2.DefaultRunSIProvider#prepareArguments(boolean)
     */
    protected String[] prepareArguments(boolean isDefaultCommand) {
        List tso = collector.getCollectedScannerInfo(resource.getProject(), ScannerInfoTypes.TARGET_SPECIFIC_OPTION);
        if (tso == null || tso.size() == 0) {
            return fCompileArguments;
        }
        
        String[] rv = null;
        // commandArguments may have multiple arguments; tokenizing
        int nTokens = 0;
        if (fCompileArguments != null && fCompileArguments.length > 0) {
            nTokens = fCompileArguments.length;
            rv = new String[nTokens + tso.size()];
            System.arraycopy(fCompileArguments, 0, rv, 0, nTokens);
        }
        else {
            rv = new String[tso.size()];
        }
        for (int i = 0; i < tso.size(); ++i) {
            rv[nTokens + i] = (String) tso.get(i);
        }
        return rv;
    }

}
