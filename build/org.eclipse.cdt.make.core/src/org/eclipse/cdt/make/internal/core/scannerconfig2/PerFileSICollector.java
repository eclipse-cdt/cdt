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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigScope;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CygpathTranslator;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Per file scanner info collector
 * 
 * @author vhirsl
 */
public class PerFileSICollector implements IScannerInfoCollector2, IScannerInfoCollectorCleaner {
    public class ScannerInfoData implements IDiscoveredScannerInfoSerializable {
        private Map commandIdToFilesMap; // command id and set of files it applies to
        private Map fileToCommandIdMap;  // maps each file to the corresponding command id
        private Map commandIdCommandMap; // map of all commands

        public ScannerInfoData() {
            commandIdCommandMap = new LinkedHashMap();  // [commandId, command]
            fileToCommandIdMap = new HashMap();         // [file, commandId]
            commandIdToFilesMap = new HashMap();        // [commandId, set of files]
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#serialize(org.w3c.dom.Element)
         */
        public void serialize(Element collectorElem) {
            Document doc = collectorElem.getOwnerDocument();
            
            List commandIds = new ArrayList(commandIdCommandMap.keySet());
            Collections.sort(commandIds);
            for (Iterator i = commandIds.iterator(); i.hasNext(); ) {
                Integer commandId = (Integer) i.next();
                CCommandDSC command = (CCommandDSC) commandIdCommandMap.get(commandId);
                
                Element cmdElem = doc.createElement(CC_ELEM); //$NON-NLS-1$
                collectorElem.appendChild(cmdElem);
                cmdElem.setAttribute(ID_ATTR, commandId.toString()); //$NON-NLS-1$
                cmdElem.setAttribute(FILE_TYPE_ATTR, command.appliesToCPPFileType() ? "c++" : "c"); //$NON-NLS-1$ //$NON-NLS-2$
                // write command and scanner info
                command.serialize(cmdElem);
                // write files command applies to
                Element filesElem = doc.createElement(APPLIES_TO_ATTR); //$NON-NLS-1$
                cmdElem.appendChild(filesElem);
                Set files = (Set) commandIdToFilesMap.get(commandId);
                if (files != null) {
                    for (Iterator j = files.iterator(); j.hasNext(); ) {
                        Element fileElem = doc.createElement(FILE_ELEM); //$NON-NLS-1$
                        IFile file = (IFile) j.next();
                        IPath path = file.getProjectRelativePath();
                        fileElem.setAttribute(PATH_ATTR, path.toString()); //$NON-NLS-1$
                        filesElem.appendChild(fileElem);
                    }
                }
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#deserialize(org.w3c.dom.Element)
         */
        public void deserialize(Element collectorElem) {
            for (Node child = collectorElem.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getNodeName().equals(CC_ELEM)) { //$NON-NLS-1$
                    Element cmdElem = (Element) child;
                    boolean cppFileType = cmdElem.getAttribute(FILE_TYPE_ATTR).equals("c++"); //$NON-NLS-1$
                    CCommandDSC command = new CCommandDSC(cppFileType);
                    command.setCommandId(Integer.parseInt(cmdElem.getAttribute(ID_ATTR)));
                    // deserialize command
                    command.deserialize(cmdElem);
                    // get set of files the command applies to
                    NodeList appliesList = cmdElem.getElementsByTagName(APPLIES_TO_ATTR);
                    if (appliesList.getLength() > 0) {
                        Element appliesElem = (Element) appliesList.item(0);
                        NodeList fileList = appliesElem.getElementsByTagName(FILE_ELEM);
                        for (int i = 0; i < fileList.getLength(); ++i) {
                            Element fileElem = (Element) fileList.item(i);
                            String fileName = fileElem.getAttribute(PATH_ATTR);
                            IFile file = project.getFile(fileName);
                            addCompilerCommand(file, command);
                        }
                    }
                }
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#getCollectorId()
         */
        public String getCollectorId() {
            return COLLECTOR_ID;
        }

    };
    
    public static final String COLLECTOR_ID = MakeCorePlugin.getUniqueIdentifier() + ".PerFileSICollector"; //$NON-NLS-1$
	private static final String CC_ELEM = "compilerCommand"; //$NON-NLS-1$
	private static final String ID_ATTR = "id"; //$NON-NLS-1$
	private static final String FILE_TYPE_ATTR = "fileType"; //$NON-NLS-1$
	private static final String APPLIES_TO_ATTR = "appliesToFiles"; //$NON-NLS-1$
	private static final String FILE_ELEM = "file"; //$NON-NLS-1$
	private static final String PATH_ATTR = "path"; //$NON-NLS-1$
	
    private static final LinkedHashMap EMPTY_LHM = new LinkedHashMap(0); 

    private IProject project;
    
    private ScannerInfoData sid; // scanner info data
    
    private List siChangedForFileList; // list of files for which scanner info has changed
    
    private SortedSet freeCommandIdPool;   // sorted set of free command ids
    private int commandIdCounter = 0;
    
    private boolean siAvailable;    // is there any scanner info discovered
    
    /**
     * 
     */
    public PerFileSICollector() {
        sid = new ScannerInfoData();
        
        siChangedForFileList = new ArrayList();
        
        freeCommandIdPool = new TreeSet();
        siAvailable = false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#setProject(org.eclipse.core.resources.IProject)
     */
    public void setProject(IProject project) {
        this.project = project;

        try {
            // deserialize from SI store
            DiscoveredScannerInfoStore.getInstance().loadDiscoveredScannerInfoFromState(project, sid);
        }
        catch (CoreException e) {
            MakeCorePlugin.log(e);
            siAvailable = false;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(java.lang.Object, java.util.Map)
     */
    public synchronized void contributeToScannerConfig(Object resource, Map scannerInfo) {
        // check the resource
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        }
        else if (resource instanceof Integer) {
            addScannerInfo(((Integer)resource), scannerInfo);
            return;
        }
        else if (!(resource instanceof IFile)) {
            errorMessage = "resource is not an IFile";//$NON-NLS-1$
        }
        else if (((IFile) resource).getProject() == null) {
            errorMessage = "project is null";//$NON-NLS-1$
        }
        else if (((IFile) resource).getProject() != project) {
            errorMessage = "wrong project";//$NON-NLS-1$
        }
        if (errorMessage != null) {
            TraceUtil.outputError("PerFileSICollector.contributeToScannerConfig : ", errorMessage); //$NON-NLS-1$
            return;
        }
        IFile file = (IFile) resource;
       
        for (Iterator i = scannerInfo.keySet().iterator(); i.hasNext(); ) {
            ScannerInfoTypes type = (ScannerInfoTypes) i.next();
            if (type.equals(ScannerInfoTypes.COMPILER_COMMAND)) {
                List commands = (List) scannerInfo.get(type);
                for (Iterator j = commands.iterator(); j.hasNext(); ) {
                    addCompilerCommand(file, (CCommandDSC) j.next());
                }
            }
            else {
                addScannerInfo(type, (List) scannerInfo.get(type));
            }
        }
    }

    /**
     * @param commandId
     * @param scannerInfo
     */
    private void addScannerInfo(Integer commandId, Map scannerInfo) {
        CCommandDSC cmd = (CCommandDSC) sid.commandIdCommandMap.get(commandId);
        if (cmd != null) {
            List symbols = (List) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
            List includes = (List) scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
            cmd.setSymbols(symbols);
            cmd.setIncludes(CygpathTranslator.translateIncludePaths(includes));
            cmd.setDiscovered(true);
        }
    }

    /**
     * @param file 
     * @param object
     */
    private void addCompilerCommand(IFile file, CCommandDSC cmd) {
        List existingCommands = new ArrayList(sid.commandIdCommandMap.values());
        int index = existingCommands.indexOf(cmd);
        if (index != -1) {
            cmd = (CCommandDSC) existingCommands.get(index);
        }
        else {
            int commandId = -1;
            if (!freeCommandIdPool.isEmpty()) {
                Integer freeCommandId = (Integer) freeCommandIdPool.first();
                freeCommandIdPool.remove(freeCommandId);
                commandId = freeCommandId.intValue();
            }
            else {
                commandId = ++commandIdCounter;
            }
            cmd.setCommandId(commandId);
            sid.commandIdCommandMap.put(cmd.getCommandIdAsInteger(), cmd);
        }
        Integer commandId = cmd.getCommandIdAsInteger();
        // update sid.commandIdToFilesMap
        Set fileSet = (Set) sid.commandIdToFilesMap.get(commandId);
        if (fileSet == null) {
            fileSet = new HashSet();
            sid.commandIdToFilesMap.put(commandId, fileSet);
        }
        if (fileSet.add(file)) {
            // update fileToCommandIdsMap
            boolean change = true;
            Integer oldCommandId = (Integer) sid.fileToCommandIdMap.get(file);
            if (oldCommandId != null) {
                if (oldCommandId.equals(commandId)) {
                    change = false;
                }
                else {
                    Set oldFileSet = (Set) sid.commandIdToFilesMap.get(oldCommandId);
                    oldFileSet.remove(file);
                }
            }
            if (change) {
                sid.fileToCommandIdMap.put(file, commandId);
                // TODO generate change event for this resource
                IPath path = file.getFullPath();
                if (!siChangedForFileList.contains(path)) {
                    siChangedForFileList.add(path);
                }
            }
        }
    }

    private void removeUnusedCommands() {
        for (Iterator i = sid.commandIdToFilesMap.entrySet().iterator(); i.hasNext(); ) {
            Entry entry = (Entry) i.next();
            Integer cmdId = (Integer) entry.getKey();
            Set fileSet = (Set) entry.getValue();
            if (fileSet.isEmpty()) {
                // return cmdId to the free command id pool
                freeCommandIdPool.add(cmdId);
            }
        }
        for (Iterator i = freeCommandIdPool.iterator(); i.hasNext(); ) {
            Integer cmdId = (Integer) i.next();
            // the command does not have any files associated; remove
            sid.commandIdCommandMap.remove(cmdId);
            sid.commandIdToFilesMap.remove(cmdId);
        }
        while (!freeCommandIdPool.isEmpty()) { 
            Integer last = (Integer) freeCommandIdPool.last(); 
            if (last.intValue() == commandIdCounter) {
                freeCommandIdPool.remove(last);
                --commandIdCounter;
            }
            else break;
        }
    }
    
    /**
     * @param type
     * @param object
     */
    private void addScannerInfo(ScannerInfoTypes type, List delta) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#updateScannerConfiguration(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask(MakeMessages.getString("ScannerInfoCollector.Processing"), 100); //$NON-NLS-1$
        removeUnusedCommands();
        monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Processing")); //$NON-NLS-1$
        if (!siChangedForFileList.isEmpty()) {
//        MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project);
//        DiscoveredScannerInfoStore.getInstance().loadDiscoveredScannerInfoFromState(project, this);
            monitor.worked(50);
            monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Updating") + project.getName()); //$NON-NLS-1$
            try {
                // update scanner configuration
                MakeCorePlugin.getDefault().getDiscoveryManager().
                        updateDiscoveredInfo(createPathInfoObject(), siChangedForFileList);
    //            DiscoveredScannerInfoStore.getInstance().saveDiscoveredScannerInfoToState(project, this);
                monitor.worked(50);
            } catch (CoreException e) {
                MakeCorePlugin.log(e);
            }
            siChangedForFileList.clear();
        }
        monitor.done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#createPathInfoObject()
     */
    public IDiscoveredPathInfo createPathInfoObject() {
        return new PerFileDiscoveredPathInfo();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#getCollectedScannerInfo(java.lang.Object, org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes)
     */
    public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
        List rv = null;
        // check the resource
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        } 
        else if (!(resource instanceof IResource)) {
            errorMessage = "resource is not an IResource";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() == null) {
            errorMessage = "project is null";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() != project) {
            errorMessage = "wrong project";//$NON-NLS-1$
        }
        
        if (errorMessage != null) {
            TraceUtil.outputError("PerProjectSICollector.getCollectedScannerInfo : ", errorMessage); //$NON-NLS-1$
        }
        else if (project.equals(((IResource)resource).getProject())) {
            if (type.equals(ScannerInfoTypes.COMPILER_COMMAND)) {
                rv = new ArrayList();
                for (Iterator i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext(); ) {
                    Integer cmdId = (Integer) i.next();
                    Set fileSet = (Set) sid.commandIdToFilesMap.get(cmdId);
                    if (!fileSet.isEmpty()) {
                        rv.add(sid.commandIdCommandMap.get(cmdId));
                    }
                }
            }
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#deleteAllPaths(org.eclipse.core.resources.IResource)
     */
    public void deleteAllPaths(IResource resource) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#deleteAllSymbols(org.eclipse.core.resources.IResource)
     */
    public void deleteAllSymbols(IResource resource) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#deletePath(org.eclipse.core.resources.IResource, java.lang.String)
     */
    public void deletePath(IResource resource, String path) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#deleteSymbol(org.eclipse.core.resources.IResource, java.lang.String)
     */
    public void deleteSymbol(IResource resource, String symbol) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Per file DPI object
     * 
     * @author vhirsl
     */
    public class PerFileDiscoveredPathInfo implements IDiscoveredPathInfo {
        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getProject()
         */
        public IProject getProject() {
            return project;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludePaths()
         */
        public IPath[] getIncludePaths() {
//            return new IPath[0];
            List includes = getAllIncludePaths();
            List finalIncludePaths = new ArrayList(includes.size());
            for (Iterator i = includes.iterator(); i.hasNext(); ) {
                finalIncludePaths.add(new Path((String) i.next()));
            }
            return (IPath[])finalIncludePaths.toArray(new IPath[finalIncludePaths.size()]);
        }

        /**
         * @return list of IPath(s).
         */
        private List getAllIncludePaths() {
            List allIncludes = new ArrayList();
            for (Iterator i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext(); ) {
                Integer cmdId = (Integer) i.next();
                CCommandDSC cmd = (CCommandDSC) sid.commandIdCommandMap.get(cmdId);
                if (cmd.isDiscovered()) {
                    List discovered = cmd.getIncludes();
                    for (Iterator j = discovered.iterator(); j.hasNext(); ) {
                        String include = (String) j.next();
                        if (!allIncludes.contains(include)) {
                            allIncludes.add(include);
                        }
                    }
                }
            }
            return allIncludes;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSymbols()
         */
        public Map getSymbols() {
//            return new HashMap();
            return getAllSymbols();
        }

        /**
         * @return
         */
        private Map getAllSymbols() {
            Map symbols = new HashMap();
            for (Iterator i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext(); ) {
                Integer cmdId = (Integer) i.next();
                CCommandDSC cmd = (CCommandDSC) sid.commandIdCommandMap.get(cmdId);
                if (cmd.isDiscovered()) {
                    List discovered = cmd.getSymbols();
                    for (Iterator j = discovered.iterator(); j.hasNext(); ) {
                        String symbol = (String) j.next();
                        String key = ScannerConfigUtil.getSymbolKey(symbol);
                        String value = ScannerConfigUtil.getSymbolValue(symbol);
                        symbols.put(key, value);
                    }
                }
            }
            
            return symbols;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#setIncludeMap(java.util.LinkedHashMap)
         */
        public void setIncludeMap(LinkedHashMap map) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#setSymbolMap(java.util.LinkedHashMap)
         */
        public void setSymbolMap(LinkedHashMap map) {
            // TODO Auto-generated method stub

        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludeMap()
         */
        public LinkedHashMap getIncludeMap() {
            return EMPTY_LHM;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSymbolMap()
         */
        public LinkedHashMap getSymbolMap() {
            return EMPTY_LHM;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludePaths(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getIncludePaths(IPath path) {
            // get the command
            CCommandDSC cmd = getCommand(path);
            if (cmd != null && cmd.isDiscovered()) {
                List includes = cmd.getIncludes();
                List includePaths = new ArrayList(includes.size());
                for (Iterator i = includes.iterator(); i.hasNext(); ) {
                    includePaths.add(new Path((String) i.next()));
                }
                return (IPath[])includePaths.toArray(new IPath[includePaths.size()]);
            }
            return new IPath[0];
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSymbols(org.eclipse.core.runtime.IPath)
         */
        public Map getSymbols(IPath path) {
            // get the command
            CCommandDSC cmd = getCommand(path);
            if (cmd != null && cmd.isDiscovered()) {
                List symbols = cmd.getSymbols();
                Map definedSymbols = new HashMap(symbols.size());
                for (Iterator i = symbols.iterator(); i.hasNext(); ) {
                    String symbol = (String) i.next();
                    String key = ScannerConfigUtil.getSymbolKey(symbol);
                    String value = ScannerConfigUtil.getSymbolValue(symbol);
                    definedSymbols.put(key, value);
                }
                return definedSymbols;
            }
            return new HashMap(0);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludeFiles(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getIncludeFiles(IPath path) {
            // get the command
            CCommandDSC cmd = getCommand(path);
            if (cmd != null) {
                return cmd.getIncludeFile();
            }
            return new IPath[0];
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getMacroFiles(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getMacroFiles(IPath path) {
            // get the command
            CCommandDSC cmd = getCommand(path);
            if (cmd != null) {
                return cmd.getImacrosFile();
            }
            return new IPath[0];
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSerializable()
         */
        public IDiscoveredScannerInfoSerializable getSerializable() {
            return sid;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getScope()
         */
        public ScannerConfigScope getScope() {
            return ScannerConfigScope.FILE_SCOPE;
        }

        /**
         * @param path
         * @return
         */
        private CCommandDSC getCommand(IPath path) {
            CCommandDSC cmd = null;
            IFile file = project.getWorkspace().getRoot().getFile(path);
            if (file != null) {
                Integer cmdId = (Integer) sid.fileToCommandIdMap.get(file);
                if (cmdId != null) {
                    // get the command
                    cmd = (CCommandDSC) sid.commandIdCommandMap.get(cmdId);
                }
            }
            return cmd;
        }

    }

}
