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
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo;
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
	private static final int INCLUDE_PATH 		= 1;
	private static final int QUOTE_INCLUDE_PATH = 2;
	private static final int INCLUDE_FILE		= 3;
	private static final int MACROS_FILE		= 4;
	
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
						applyFileDeltas();
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

    }
    
    private static class ProjectScannerInfo {
    	IPath[] includePaths;
    	IPath[] quoteIncludePaths;
    	IPath[] includeFiles;
    	IPath[] macrosFiles;
    	Map definedSymbols;
		public boolean isEmpty() {
			return (includePaths.length == 0 &&
					quoteIncludePaths.length == 0 &&
					includeFiles.length == 0 &&
					macrosFiles.length == 0 &&
					definedSymbols.size() == 0);
		}
    }
    
    public static final String COLLECTOR_ID = MakeCorePlugin.getUniqueIdentifier() + ".PerFileSICollector"; //$NON-NLS-1$
	private static final String CC_ELEM = "compilerCommand"; //$NON-NLS-1$
	private static final String ID_ATTR = "id"; //$NON-NLS-1$
	private static final String FILE_TYPE_ATTR = "fileType"; //$NON-NLS-1$
	private static final String APPLIES_TO_ATTR = "appliesToFiles"; //$NON-NLS-1$
	private static final String FILE_ELEM = "file"; //$NON-NLS-1$
	private static final String PATH_ATTR = "path"; //$NON-NLS-1$
	
    IProject project;
    
    private ScannerInfoData sid; // scanner info data
    private ProjectScannerInfo psi = null;	// sum of all scanner info
    
//    private List siChangedForFileList; 		// list of files for which scanner info has changed
	private Map siChangedForFileMap;		// (file, comandId) map for deltas
	private List siChangedForCommandIdList;	// list of command ids for which scanner info has changed
    
    private SortedSet freeCommandIdPool;   // sorted set of free command ids
    private int commandIdCounter = 0;
    
    /**
     * 
     */
    public PerFileSICollector() {
        sid = new ScannerInfoData();
        
//        siChangedForFileList = new ArrayList();
		siChangedForFileMap = new HashMap();
		siChangedForCommandIdList = new ArrayList();
		
        freeCommandIdPool = new TreeSet();
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
            List siItem = (List) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
            cmd.setSymbols(siItem);
            siItem = (List) scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
            cmd.setIncludes(CygpathTranslator.translateIncludePaths(project, siItem));
            siItem = (List) scannerInfo.get(ScannerInfoTypes.QUOTE_INCLUDE_PATHS);
            cmd.setQuoteIncludes(siItem);
            
            cmd.setDiscovered(true);
        }
    }

    /**
     * @param file 
     * @param object
     */
    void addCompilerCommand(IFile file, CCommandDSC cmd) {
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
		
		generateFileDelta(file, cmd);
    }

    /**
	 * @param file
	 * @param cmd
	 */
	private void generateFileDelta(IFile file, CCommandDSC cmd) {
        Integer commandId = cmd.getCommandIdAsInteger();
		Integer oldCommandId = (Integer) sid.fileToCommandIdMap.get(file);

		if (oldCommandId != null && oldCommandId.equals(commandId)) {
			// already exists; remove form delta
			siChangedForFileMap.remove(file);
		}
		else {
			// new (file, commandId) pair
			siChangedForFileMap.put(file, commandId);
		}
	}

	/**
	 * @param file
	 * @param cmd
	 */
	void applyFileDeltas() {
		for (Iterator i = siChangedForFileMap.keySet().iterator(); i.hasNext(); ) {
			IFile file = (IFile) i.next();
			Integer commandId = (Integer) siChangedForFileMap.get(file);
			if (commandId != null) {
			
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
//			                IPath path = file.getFullPath();
//			                if (!siChangedForFileList.contains(path)) {
//			                    siChangedForFileList.add(path);
//			                }
		            }
		        }
			}
		}
		generateProjectScannerInfo();
	}

	private void generateProjectScannerInfo() {
        psi = new ProjectScannerInfo();
		psi.includePaths = getAllIncludePaths(INCLUDE_PATH);
		psi.quoteIncludePaths = getAllIncludePaths(QUOTE_INCLUDE_PATH);
		psi.includeFiles = getAllIncludePaths(INCLUDE_FILE);
		psi.macrosFiles = getAllIncludePaths(MACROS_FILE);
		psi.definedSymbols = getAllSymbols();
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
//        removeUnusedCommands();
        monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Processing")); //$NON-NLS-1$
        if (scannerInfoChanged()) {
			applyFileDeltas();
	        removeUnusedCommands();
            monitor.worked(50);
            monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Updating") + project.getName()); //$NON-NLS-1$
            try {
                // update scanner configuration
//                MakeCorePlugin.getDefault().getDiscoveryManager().
//                        updateDiscoveredInfo(createPathInfoObject(), siChangedForFileList);
                IDiscoveredPathInfo pathInfo = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project);
                if (!(pathInfo instanceof IPerFileDiscoveredPathInfo)) {
                	pathInfo = createPathInfoObject();
                }
                MakeCorePlugin.getDefault().getDiscoveryManager().
                		updateDiscoveredInfo(pathInfo, new ArrayList(siChangedForFileMap.keySet()));
                monitor.worked(50);
            } catch (CoreException e) {
                MakeCorePlugin.log(e);
            }
        }
//        siChangedForFileList.clear();
		siChangedForFileMap.clear();
		siChangedForCommandIdList.clear();

		monitor.done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#createPathInfoObject()
     */
    public IDiscoveredPathInfo createPathInfoObject() {
        return new PerFileDiscoveredPathInfo();
    }

	private boolean scannerInfoChanged() {
//		return !siChangedForFileList.isEmpty();
		return !siChangedForFileMap.isEmpty();
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#getCollectedScannerInfo(java.lang.Object, org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes)
     */
    public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
        List rv = new ArrayList();
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
                for (Iterator i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext(); ) {
                    Integer cmdId = (Integer) i.next();
                    Set fileSet = (Set) sid.commandIdToFilesMap.get(cmdId);
                    if (!fileSet.isEmpty()) {
                        rv.add(sid.commandIdCommandMap.get(cmdId));
                    }
                }
            }
            else if (type.equals(ScannerInfoTypes.UNDISCOVERED_COMPILER_COMMAND)) {
//				if (!siChangedForFileList.isEmpty()) {
				if (scannerInfoChanged()) {
					if (siChangedForCommandIdList.isEmpty()) {
//						for (Iterator i = siChangedForFileList.iterator(); i.hasNext(); ) {
						for (Iterator i = siChangedForFileMap.keySet().iterator(); i.hasNext(); ) {
//							IPath path = (IPath) i.next();
							IFile file = (IFile) i.next();
							Integer cmdId = (Integer) siChangedForFileMap.get(file);
							if (cmdId != null) {
								if (!siChangedForCommandIdList.contains(cmdId)) {
									siChangedForCommandIdList.add(cmdId);
								}
							}
						}
					}
					Collections.sort(siChangedForCommandIdList);
					for (Iterator i = siChangedForCommandIdList.iterator(); i.hasNext(); ) {
						Integer cmdId = (Integer) i.next();
						CCommandDSC command = (CCommandDSC) sid.commandIdCommandMap.get(cmdId);
						rv.add(command);
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner#deleteAll(org.eclipse.core.resources.IResource)
     */
    public void deleteAll(IResource resource) {
        if (resource.equals(project)) {
//            siChangedForFileList = new ArrayList();
            siChangedForFileMap.clear();
            Set changedFiles = sid.fileToCommandIdMap.keySet();
            for (Iterator i = changedFiles.iterator(); i.hasNext(); ) {
                IFile file = (IFile) i.next();
//                IPath path = file.getFullPath();
//                siChangedForFileList.add(path);
                siChangedForFileMap.put(file, null);
            }

            sid = new ScannerInfoData();
            psi = null;
            
            commandIdCounter = 0;
			freeCommandIdPool.clear();
        }
    }

    /**
     * Per file DPI object
     * 
     * @author vhirsl
     */
    public class PerFileDiscoveredPathInfo implements IPerFileDiscoveredPathInfo {
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
            return getAllIncludePaths(INCLUDE_PATH);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSymbols()
         */
        public Map getSymbols() {
//            return new HashMap();
            return getAllSymbols();
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludePaths(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getIncludePaths(IPath path) {
            // get the command
            CCommandDSC cmd = getCommand(path);
            if (cmd != null && cmd.isDiscovered()) {
                return stringListToPathArray(cmd.getIncludes());
            }
            // use project scope scanner info
            if (psi == null) {
            	generateProjectScannerInfo();
            }
            return psi.includePaths;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getQuoteIncludePaths(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getQuoteIncludePaths(IPath path) {
            // get the command
            CCommandDSC cmd = getCommand(path);
            if (cmd != null && cmd.isDiscovered()) {
                return stringListToPathArray(cmd.getQuoteIncludes());
            }
            // use project scope scanner info
            if (psi == null) {
            	generateProjectScannerInfo();
            }
            return psi.quoteIncludePaths;
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
            // use project scope scanner info
            if (psi == null) {
            	generateProjectScannerInfo();
            }
            return psi.definedSymbols;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludeFiles(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getIncludeFiles(IPath path) {
            // get the command
            CCommandDSC cmd = getCommand(path);
            if (cmd != null) {
                return stringListToPathArray(cmd.getIncludeFile());
            }
            // use project scope scanner info
            if (psi == null) {
            	generateProjectScannerInfo();
            }
            return psi.includeFiles;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getMacroFiles(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getMacroFiles(IPath path) {
            // get the command
            CCommandDSC cmd = getCommand(path);
            if (cmd != null) {
                return stringListToPathArray(cmd.getImacrosFile());
            }
            // use project scope scanner info
            if (psi == null) {
            	generateProjectScannerInfo();
            }
            return psi.macrosFiles;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getSerializable()
         */
        public IDiscoveredScannerInfoSerializable getSerializable() {
            return sid;
        }

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#isEmpty(org.eclipse.core.runtime.IPath)
		 */
		public boolean isEmpty(IPath path) {
			boolean rc = true;
			IResource resource = project.getWorkspace().getRoot().findMember(path);
			if (resource != null) {
				if (resource instanceof IFile) {
					rc = (getCommand((IFile)resource) == null);
				}
				else if (resource instanceof IProject) {
					rc = (psi == null || psi.isEmpty()); 
				}
			}
			return rc;
		}

    }

    /**
     * @param path
     * @return
     */
    private CCommandDSC getCommand(IPath path) {
        try {
        	IFile file = project.getWorkspace().getRoot().getFile(path);
    		return getCommand(file);
        }
        catch (Exception e) {
        	return null;
        }
    }

    private CCommandDSC getCommand(IFile file) {
        CCommandDSC cmd = null;
        if (file != null) {
            Integer cmdId = (Integer) sid.fileToCommandIdMap.get(file);
            if (cmdId != null) {
                // get the command
                cmd = (CCommandDSC) sid.commandIdCommandMap.get(cmdId);
            }
        }
        return cmd;
    }

    /**
     * @param type can be one of the following:
     * <li><code>INCLUDE_PATH</code>
     * <li><code>QUOTE_INCLUDE_PATH</code>
     * <li><code>INCLUDE_FILE</code>
     * <li><code>MACROS_FILE</code>
     * 
     * @return list of IPath(s).
     */
    private IPath[] getAllIncludePaths(int type) {
    	List allIncludes = new ArrayList();
        for (Iterator i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext(); ) {
            Integer cmdId = (Integer) i.next();
            CCommandDSC cmd = (CCommandDSC) sid.commandIdCommandMap.get(cmdId);
            if (cmd.isDiscovered()) {
    			List discovered = null;
            	switch (type) {
            		case INCLUDE_PATH:
            			discovered = cmd.getIncludes();
            			break;
            		case QUOTE_INCLUDE_PATH: 
            			discovered = cmd.getQuoteIncludes();
            			break;
            		case INCLUDE_FILE:
            			discovered = cmd.getIncludeFile();
            			break;
            		case MACROS_FILE:
            			discovered = cmd.getImacrosFile();
            			break;
            	}
    			for (Iterator j = discovered.iterator(); j.hasNext(); ) {
    			    String include = (String) j.next();
    			    if (!allIncludes.contains(include)) {
    			        allIncludes.add(include);
    			    }
    			}
            }
        }
        return stringListToPathArray(allIncludes);
    }

	/**
	 * @param discovered
	 * @param allIncludes
	 * @return
	 */
	private IPath[] stringListToPathArray(List discovered) {
		List allIncludes = new ArrayList(discovered.size());
		for (Iterator j = discovered.iterator(); j.hasNext(); ) {
		    String include = (String) j.next();
		    if (!allIncludes.contains(include)) {
		        allIncludes.add(new Path(include));
		    }
		}
		return (IPath[])allIncludes.toArray(new IPath[allIncludes.size()]);
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

}
