/*******************************************************************************
 * Copyright (c) 2004, 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
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
    private final static List FILE_EXTENSIONS_LIST = Arrays.asList(FILE_EXTENSIONS);
    
    private String[] compilerInvocation;
    private GCCPerFileBOPConsoleParserUtility fUtil;
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IPath, org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector, org.eclipse.cdt.core.IMarkerGenerator)
     */
    public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector, IMarkerGenerator markerGenerator) {
        fUtil = (project != null && workingDirectory != null && markerGenerator != null) ?
                new GCCPerFileBOPConsoleParserUtility(project, workingDirectory, markerGenerator) : null;
        super.startup(project, collector);
        
        // check additional compiler commands from extension point manifest
        compilerInvocation = getCompilerCommands();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.gnu.AbstractGCCBOPConsoleParser#getUtility()
     */
    protected AbstractGCCBOPConsoleParserUtility getUtility() {
        return fUtil;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.internal.core.scannerconfig.gnu.AbstractGCCBOPConsoleParser#processSingleLine(java.lang.String)
     */
    protected boolean processSingleLine(String line) {
        boolean rc = false;
        // GCC C/C++ compiler invocation 
        int compilerInvocationIndex = -1;
        for (int cii = 0; cii < compilerInvocation.length; ++cii) {
            compilerInvocationIndex = line.indexOf(compilerInvocation[cii]);
            if (compilerInvocationIndex != -1)
                break;
        }
        if (compilerInvocationIndex == -1)
            return rc;

        // expecting that compiler invocation is the first token in the line
        String[] split = line.split("\\s+"); //$NON-NLS-1$
        String command = split[0];
        // verify that it is compiler invocation
        int cii2 = -1;
        for (int cii = 0; cii < compilerInvocation.length; ++cii) {
            cii2 = command.indexOf(compilerInvocation[cii]);
            if (cii2 != -1)
                break;
        }
        if (cii2 == -1) {
            TraceUtil.outputTrace("Error identifying compiler command", line, TraceUtil.EOL); //$NON-NLS-1$
            return rc;
        }
        // find a file name
        int extensionsIndex = -1;
        boolean found = false;
        String filePath = null;
        for (int i = 1; i < split.length; ++i) {
            int k = split[i].lastIndexOf('.');
            if (k != -1 && (split[i].length() - k < 5)) {
                String fileExtension = split[i].substring(k);
                extensionsIndex = FILE_EXTENSIONS_LIST.indexOf(fileExtension);
                if (extensionsIndex != -1) {
                    filePath = split[i];
                    found = true;
                    break;
                }
            }
        }
//              for (int j = 0; j < FILE_EXTENSIONS.length; ++j) {
//                  if (split[i].endsWith(FILE_EXTENSIONS[j])) {
//                      filePath = split[i];
//                      extensionsIndex = j;
//                      found = true;
//                      break;
//                  }
//              }
//              if (found) break;
        if (!found) {
            TraceUtil.outputTrace("Error identifying file name :1", line, TraceUtil.EOL); //$NON-NLS-1$
            return rc;
        }
        // sanity check
        if (filePath.indexOf(FILE_EXTENSIONS[extensionsIndex]) == -1) {
            TraceUtil.outputTrace("Error identifying file name :2", line, TraceUtil.EOL); //$NON-NLS-1$
            return rc;
        }
        if (fUtil != null) {
            IPath pFilePath = fUtil.getAbsolutePath(filePath);
            String shortFileName = pFilePath.removeFileExtension().lastSegment();

            // generalize occurances of the file name
            StringBuffer genericLine = new StringBuffer();
            for (int i = 0; i < split.length; i++) {
				String token = split[i];
				if (token.equals("-include") || token.equals("-imacros")) { //$NON-NLS-1$ //$NON-NLS-2$
					++i;
					genericLine.append(token);
					genericLine.append(' ');
				}
				else if (token.equals(filePath)) {
					split[i] = "LONG_NAME"; //$NON-NLS-1$
				}
				else if (token.startsWith(shortFileName)) {
					split[i] = token.replaceFirst(shortFileName, "SHORT_NAME"); //$NON-NLS-1$
				}
				genericLine.append(split[i]);
				genericLine.append(' ');
			}
            
            CCommandDSC cmd = fUtil.getNewCCommandDSC(genericLine.toString(), extensionsIndex > 0);
            IPath buildDirectory = fUtil.getWorkingDirectory();
            if (buildDirectory.isPrefixOf(pFilePath)) {
	            List cmdList = new ArrayList();
	            cmdList.add(cmd);
	            Map sc = new HashMap(1);
	            sc.put(ScannerInfoTypes.COMPILER_COMMAND, cmdList);

				IPath relPath = pFilePath.removeFirstSegments(buildDirectory.segmentCount());
                IFile file = getProject().getFile(relPath);
                getCollector().contributeToScannerConfig(file, sc);
            }
            // fUtil.addGenericCommandForFile2(longFileName, genericLine);
        }
        return rc;
    }

}
