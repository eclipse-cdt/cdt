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
        ".c", ".cc", ".cpp", ".cxx", ".C", ".CC", ".CPP", ".CXX"
    };
    private final static String[] COMPILER_INVOCATION = {
        "gcc", "g++", "cc", "c++"
    };
    private final static List FILE_EXTENSIONS_LIST = Arrays.asList(FILE_EXTENSIONS);
    
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
        for (int cii = 0; cii < COMPILER_INVOCATION.length; ++cii) {
            compilerInvocationIndex = line.indexOf(COMPILER_INVOCATION[cii]);
            if (compilerInvocationIndex != -1)
                break;
        }
        if (compilerInvocationIndex == -1)
            return rc;

        // expecting that compiler invocation is the first token in the line
        String[] split = line.split("\\s+");
        String command = split[0];
        // verify that it is compiler invocation
        int cii2 = -1;
        for (int cii = 0; cii < COMPILER_INVOCATION.length; ++cii) {
            cii2 = command.indexOf(COMPILER_INVOCATION[cii]);
            if (cii2 != -1)
                break;
        }
        if (cii2 == -1) {
            TraceUtil.outputTrace("Error identifying compiler command", line, TraceUtil.EOL);
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
            TraceUtil.outputTrace("Error identifying file name :1", line, TraceUtil.EOL);
            return rc;
        }
        // sanity check
        if (filePath.indexOf(FILE_EXTENSIONS[extensionsIndex]) == -1) {
            TraceUtil.outputTrace("Error identifying file name :2", line, TraceUtil.EOL);
            return rc;
        }
        if (fUtil != null) {
            IPath pFilePath = fUtil.getAbsolutePath(filePath);
            String longFileName = pFilePath.toString();
            String shortFileName = pFilePath.removeFileExtension().lastSegment();
            String genericLine = line.replaceAll(filePath, "LONG_NAME");
            genericLine = genericLine.replaceAll(shortFileName+"\\.", "SHORT_NAME\\.");

            CCommandDSC cmd = fUtil.getNewCCommandDSC(genericLine, extensionsIndex > 0);
            List cmdList = new ArrayList();
            cmdList.add(cmd);
            Map sc = new HashMap(1);
            sc.put(ScannerInfoTypes.COMPILER_COMMAND, cmdList);
            if (getProject().getLocation().isPrefixOf(pFilePath)) {
            	IPath relPath = pFilePath.removeFirstSegments(getProject().getLocation().segmentCount());
                IFile file = getProject().getFile(relPath);
                getCollector().contributeToScannerConfig(file, sc);
            }
            // fUtil.addGenericCommandForFile2(longFileName, genericLine);
        }
        return rc;
    }

}
