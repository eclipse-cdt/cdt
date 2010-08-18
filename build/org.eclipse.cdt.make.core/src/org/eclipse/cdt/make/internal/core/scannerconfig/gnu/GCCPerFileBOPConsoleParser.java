/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *     Gerhard Schaber (Wind River Systems) - bug 187910
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;


/**
 * GCC per file build output parser
 * 
 * @author vhirsl
 */
public class GCCPerFileBOPConsoleParser extends AbstractGCCBOPConsoleParser {
    private final static String[] FILE_EXTENSIONS = {
        ".c", ".cc", ".cpp", ".cxx", ".C", ".CC", ".CPP", ".CXX" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    };
    private final static List<String> FILE_EXTENSIONS_LIST = Arrays.asList(FILE_EXTENSIONS);
    
    private GCCPerFileBOPConsoleParserUtility fUtil;
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IPath, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector, org.eclipse.cdt.core.IMarkerGenerator)
     */
    public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector, IMarkerGenerator markerGenerator) {
        fUtil = (project != null && workingDirectory != null && markerGenerator != null) ?
                new GCCPerFileBOPConsoleParserUtility(project, workingDirectory, markerGenerator) : null;
        super.startup(project, collector);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.gnu.AbstractGCCBOPConsoleParser#getUtility()
     */
    @Override
	protected AbstractGCCBOPConsoleParserUtility getUtility() {
        return fUtil;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.gnu.AbstractGCCBOPConsoleParser#processSingleLine(java.lang.String)
     */
    @Override
	protected boolean processCommand(String[] tokens) {
        // GCC C/C++ compiler invocation 
        int compilerInvocationIndex= findCompilerInvocation(tokens);
        if (compilerInvocationIndex < 0) {
            return false;
        }

        // find a file name
        int extensionsIndex = -1;
        boolean found = false;
        String filePath = null;
        for (int i = compilerInvocationIndex+1; i < tokens.length; i++) {
        	String token= tokens[i];
            int k = token.lastIndexOf('.');
            if (k != -1 && (token.length() - k < 5)) {
                String fileExtension = token.substring(k);
                extensionsIndex = FILE_EXTENSIONS_LIST.indexOf(fileExtension);
                if (extensionsIndex != -1) {
                    filePath = token;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            TraceUtil.outputTrace("Error identifying file name :1", tokens, TraceUtil.EOL); //$NON-NLS-1$
            return false;
        }
        // sanity check
        if (filePath==null || filePath.indexOf(FILE_EXTENSIONS[extensionsIndex]) == -1) {
            TraceUtil.outputTrace("Error identifying file name :2", tokens, TraceUtil.EOL); //$NON-NLS-1$
            return false;
        }
        if (fUtil != null) {
            IPath pFilePath = fUtil.getAbsolutePath(filePath);
            String shortFileName = pFilePath.removeFileExtension().lastSegment();

            // generalize occurrences of the file name
            for (int i = compilerInvocationIndex+1; i < tokens.length; i++) {
				String token = tokens[i];
				if (token.equals("-include")) { //$NON-NLS-1$
					++i;
				}
				else if (token.equals("-imacros")) { //$NON-NLS-1$
					++i;
				}
				else if (token.equals(filePath)) {
					tokens[i]= "LONG_NAME"; //$NON-NLS-1$
				}
				else if (token.startsWith(shortFileName)) {
					tokens[i]= token.replaceFirst(shortFileName, "SHORT_NAME"); //$NON-NLS-1$
				}
			}
            
            IFile file= null;
            IPath baseDirectory= fUtil.getBaseDirectory();
            if (baseDirectory.isPrefixOf(pFilePath) || baseDirectory.setDevice(null).isPrefixOf(pFilePath)) {
				IPath relPath = pFilePath.removeFirstSegments(baseDirectory.segmentCount());
				//Note: We add the scanner-config even if the resource doesn't actually
				//exist below this project (which may happen when reading existing
				//build logs, because resources can be created as part of the build
				//and may not exist at the time of analyzing the config but re-built
				//later on.
				//if (getProject().exists(relPath)) {
	            file = getProject().getFile(relPath);
            } else {
            	// search linked resources
            	final IProject prj= fUtil.getProject();
            	file= ResourceLookup.selectFileForLocation(pFilePath, prj);
            }
            if (file != null) {
                CCommandDSC cmd = fUtil.getNewCCommandDSC(tokens, compilerInvocationIndex, extensionsIndex > 0);
	            List<CCommandDSC> cmdList = new ArrayList<CCommandDSC>();
	            cmdList.add(cmd);
	            Map<ScannerInfoTypes, List<CCommandDSC>> sc = new HashMap<ScannerInfoTypes, List<CCommandDSC>>(1);
	            sc.put(ScannerInfoTypes.COMPILER_COMMAND, cmdList);
	            getCollector().contributeToScannerConfig(file, sc);
            } else
            	TraceUtil.outputError("Build command for file outside project: "+pFilePath.toString(), tokens); //$NON-NLS-1$
        }
        return true;
    }
}
