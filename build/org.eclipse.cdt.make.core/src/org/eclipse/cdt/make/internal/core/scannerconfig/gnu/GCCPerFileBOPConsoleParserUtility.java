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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.KVPair;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SCDOptionsEnum;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * TODO Provide description
 * 
 * @author vhirsl
 */
public class GCCPerFileBOPConsoleParserUtility extends AbstractGCCBOPConsoleParserUtility {
    private Map directoryCommandListMap;
    private List compiledFileList;
    
    private List commandsList2;
    
    private int workingDirsN = 0;
    private int commandsN = 0;
    private int filesN = 0;


    /**
     * @param markerGenerator 
     * @param workingDirectory 
     * @param project 
     */
    public GCCPerFileBOPConsoleParserUtility(IProject project, IPath workingDirectory, IMarkerGenerator markerGenerator) {
        super(project, workingDirectory, markerGenerator);
    }

    /**
     * Adds a mapping filename, generic_command
     * @param longFileName
     * @param genericLine
     */
    void addGenericCommandForFile(String longFileName, String genericCommand) {
        // if a file name has already been added once, return
        if (compiledFileList.contains(longFileName))
            return;
        compiledFileList.add(longFileName);
        
        String workingDir = getWorkingDirectory().toString();
        List directoryCommandList = (List) directoryCommandListMap.get(workingDir);
        if (directoryCommandList == null) {
            directoryCommandList = new ArrayList();
            directoryCommandListMap.put(workingDir, directoryCommandList);
            ++workingDirsN;
        }
        Map command21FileListMap = null;
        for (Iterator i = directoryCommandList.iterator(); i.hasNext(); ) {
            command21FileListMap = (Map) i.next();
            List fileList = (List) command21FileListMap.get(genericCommand);
            if (fileList != null) {
                if (!fileList.contains(longFileName)) {
                    fileList.add(longFileName);
                    ++filesN;
                }
                return;
            }
        }
        command21FileListMap = new HashMap(1);
        directoryCommandList.add(command21FileListMap);
        ++commandsN;
        List fileList = new ArrayList();
        command21FileListMap.put(genericCommand, fileList);
        fileList.add(longFileName);
        ++filesN;
    }

    /**
     * 
     */
    void generateReport() {
        TraceUtil.metricsTrace("Stats for directory ",
                   "Generic command: '", "' applicable for:", 
                   directoryCommandListMap);
        TraceUtil.summaryTrace("Discovery summary", workingDirsN, commandsN, filesN);
    }

    /**
     * Adds a mapping command line -> file, this time without a dir
     * @param longFileName
     * @param genericLine
     */
    void addGenericCommandForFile2(String longFileName, String genericLine) {
        // if a file name has already been added once, return
        if (compiledFileList.contains(longFileName))
            return;
        compiledFileList.add(longFileName);

        CCommandDSC command = getNewCCommandDSC(genericLine);
        int index = commandsList2.indexOf(command);
        if (index == -1) {
            commandsList2.add(command);
            ++commandsN;
        }
        else {
            command = (CCommandDSC) commandsList2.get(index);
        }
//        // add a file
//        command.addFile(longFileName);
//        ++filesN;
    }

    /**
     * @param genericLine
     */
    public CCommandDSC getNewCCommandDSC(String genericLine) {
        CCommandDSC command = new CCommandDSC();
        String[] tokens = genericLine.split("\\s+");
        command.addSCOption(new KVPair(SCDOptionsEnum.COMMAND, tokens[0]));
        for (int i = 1; i < tokens.length; ++i) {
            for (int j = SCDOptionsEnum.MIN; j <= SCDOptionsEnum.MAX; ++j) {
                if (tokens[i].startsWith(SCDOptionsEnum.getSCDOptionsEnum(j).toString())) {
                    String option = tokens[i].substring(
                            SCDOptionsEnum.getSCDOptionsEnum(j).toString().length()).trim();
                    if (option.length() > 0) {
                        // ex. -I/dir
                    }
                    else {
                        // ex. -I /dir
                        // take a next token
                        ++i;
                        if (i < tokens.length && !tokens[i].startsWith("-")) {
                            option = tokens[i];
                        }
                        else break;
                    }
                    if (SCDOptionsEnum.getSCDOptionsEnum(j).equals(SCDOptionsEnum.INCLUDE) ||
                            SCDOptionsEnum.getSCDOptionsEnum(j).equals(SCDOptionsEnum.INCLUDE_FILE) ||
                            SCDOptionsEnum.getSCDOptionsEnum(j).equals(SCDOptionsEnum.IMACROS_FILE) ||
                            SCDOptionsEnum.getSCDOptionsEnum(j).equals(SCDOptionsEnum.IDIRAFTER) ||
                            SCDOptionsEnum.getSCDOptionsEnum(j).equals(SCDOptionsEnum.ISYSTEM)) {
                        option = (getAbsolutePath(option)).toString();
                    }
                    // add the pair
                    command.addSCOption(new KVPair(SCDOptionsEnum.getSCDOptionsEnum(j), option));
                    break;
                }
            }
        }
        return command;
    }

    /**
     * @param filePath : String
     * @return filePath : IPath - not <code>null</code>
     */
    IPath getAbsolutePath(String filePath) {
        IPath pFilePath;
        if (filePath.startsWith("/") || filePath.startsWith("\\") ||
            (!filePath.startsWith(".") &&
             filePath.length() > 2 && filePath.charAt(1) == ':' && 
             (filePath.charAt(2) == '\\' || filePath.charAt(2) == '/'))) {
            // absolute path
            pFilePath = new Path(filePath);
        }
        else {
            // relative path
            pFilePath = getWorkingDirectory().append(filePath);
        }
        return pFilePath;
    }

    /**
     * 
     */
//    void generateReport2() {
//        StringWriter buffer = new StringWriter();
//        PrintWriter writer = new PrintWriter(buffer);
//        for (Iterator i = commandsList2.iterator(); i.hasNext(); ) {
//            CCommandDSC cmd = (CCommandDSC)i.next();
//            writer.println("Stats for generic command: '" + cmd.getCommandAsString() + "' applicable for " + 
//                    Integer.toString(cmd.getNumberOfFiles()) + " files: ");
//            List filesList = cmd.getFilesList();
//            if (filesList != null) {
//                for (Iterator j = filesList.iterator(); j.hasNext(); ) {
//                    writer.println("    " + (String)j.next());
//                }
//            }
//        }
//        writer.close();
//            
//        TraceUtil.metricsTrace(buffer.toString());
//        TraceUtil.summaryTrace("Discovery summary", workingDirsN, commandsN, filesN);
//    }

    /**
     * Returns all CCommandDSC collected so far
     * @return
     */
    public List getCCommandDSCList() {
        return new ArrayList(commandsList2);
    }

}
