/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *  Markus Schorn (Wind River Systems)
 *  Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.PathInfo;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo2;
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
public class PerFileSICollector implements IScannerInfoCollector3, IScannerInfoCollectorCleaner {
	private static final int INCLUDE_PATH 		= 1;
	private static final int QUOTE_INCLUDE_PATH = 2;
	private static final int INCLUDE_FILE		= 3;
	private static final int MACROS_FILE		= 4;
	
    private class ScannerInfoData implements IDiscoveredScannerInfoSerializable {
        private final Map<Integer, Set<IFile>> commandIdToFilesMap; // command id and set of files it applies to
        private final Map<IFile, Integer> fileToCommandIdMap;  // maps each file to the corresponding command id
        private final Map<Integer, CCommandDSC> commandIdCommandMap; // map of all commands

        public ScannerInfoData() {
            commandIdCommandMap = new LinkedHashMap<Integer, CCommandDSC>();  // [commandId, command]
            fileToCommandIdMap = new HashMap<IFile, Integer>();         // [file, commandId]
            commandIdToFilesMap = new HashMap<Integer, Set<IFile>>();        // [commandId, set of files]
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#serialize(org.w3c.dom.Element)
         */
        public void serialize(Element collectorElem) {
        	synchronized (PerFileSICollector.this.fLock) {
	            Document doc = collectorElem.getOwnerDocument();
	            
	            List<Integer> commandIds = new ArrayList<Integer>(commandIdCommandMap.keySet());
	            Collections.sort(commandIds);
	            for (Iterator<Integer> i = commandIds.iterator(); i.hasNext(); ) {
	                Integer commandId = i.next();
	                CCommandDSC command = commandIdCommandMap.get(commandId);
	                
	                Element cmdElem = doc.createElement(CC_ELEM); 
	                collectorElem.appendChild(cmdElem);
	                cmdElem.setAttribute(ID_ATTR, commandId.toString()); 
	                cmdElem.setAttribute(FILE_TYPE_ATTR, command.appliesToCPPFileType() ? "c++" : "c"); //$NON-NLS-1$ //$NON-NLS-2$
	                // write command and scanner info
	                command.serialize(cmdElem);
	                // write files command applies to
	                Element filesElem = doc.createElement(APPLIES_TO_ATTR); 
	                cmdElem.appendChild(filesElem);
	                Set<IFile> files = commandIdToFilesMap.get(commandId);
	                if (files != null) {
	                    for (Iterator<IFile> j = files.iterator(); j.hasNext(); ) {
	                        Element fileElem = doc.createElement(FILE_ELEM); 
	                        IFile file = j.next();
	                        IPath path = file.getProjectRelativePath();
	                        fileElem.setAttribute(PATH_ATTR, path.toString()); 
	                        filesElem.appendChild(fileElem);
	                    }
	                }
	            }
        	}
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#deserialize(org.w3c.dom.Element)
         */
        public void deserialize(Element collectorElem) {
        	synchronized (PerFileSICollector.this.fLock) {
	            for (Node child = collectorElem.getFirstChild(); child != null; child = child.getNextSibling()) {
	                if (child.getNodeName().equals(CC_ELEM)) { 
	                    Element cmdElem = (Element) child;
	                    boolean cppFileType = cmdElem.getAttribute(FILE_TYPE_ATTR).equals("c++"); //$NON-NLS-1$
	                    CCommandDSC command = new CCommandDSC(cppFileType, project);
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
    	Map<String, String> definedSymbols;
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
	
    private IProject project;
    private InfoContext context;
    
    private ScannerInfoData sid; // scanner info data
    private ProjectScannerInfo psi = null;	// sum of all scanner info
    
//    private List siChangedForFileList; 		// list of files for which scanner info has changed
	private final Map<IResource, Integer> siChangedForFileMap;		// (file, comandId) map for deltas
	private final List<Integer> siChangedForCommandIdList;	// list of command ids for which scanner info has changed
    
    private final SortedSet<Integer> freeCommandIdPool;   // sorted set of free command ids
    private int commandIdCounter = 0;
    
    /** monitor for data access */
    private final Object fLock = new Object();

    /**
     * 
     */
    public PerFileSICollector() {
        sid = new ScannerInfoData();
        
//        siChangedForFileList = new ArrayList();
		siChangedForFileMap = new HashMap<IResource, Integer>();
		siChangedForCommandIdList = new ArrayList<Integer>();
		
        freeCommandIdPool = new TreeSet<Integer>();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#setProject(org.eclipse.core.resources.IProject)
     */
    public void setProject(IProject project) {
    	setInfoContext(new InfoContext(project));
    }

    public void setInfoContext(InfoContext context) {
        this.project = context.getProject();
        this.context = context;
        
        try {
            // deserialize from SI store
            DiscoveredScannerInfoStore.getInstance().loadDiscoveredScannerInfoFromState(project, context, sid);
        }
        catch (CoreException e) {
            MakeCorePlugin.log(e);
        }
	}

	protected InfoContext getInfoContext() {
		return context;
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(java.lang.Object, java.util.Map)
     */
    public void contributeToScannerConfig(Object resource, Map scannerInfo) {
        // check the resource
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        }
        else if (resource instanceof Integer) {
        	synchronized (fLock) {
                addScannerInfo(((Integer)resource), scannerInfo);
			}
            return;
        }
        else if (!(resource instanceof IFile)) {
            errorMessage = "resource is not an IFile";//$NON-NLS-1$
        }
        else if (((IFile) resource).getProject() == null) {
            errorMessage = "project is null";//$NON-NLS-1$
        }
        else if (!((IFile) resource).getProject().equals(project)) {
            errorMessage = "wrong project";//$NON-NLS-1$
        }
        if (errorMessage != null) {
            TraceUtil.outputError("PerFileSICollector.contributeToScannerConfig : ", errorMessage); //$NON-NLS-1$
            return;
        }
        
        IFile file = (IFile) resource;
       
        synchronized (fLock) {
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
    }

    private void addScannerInfo(Integer commandId, Map scannerInfo) {
		assert Thread.holdsLock(fLock);
        CCommandDSC cmd = sid.commandIdCommandMap.get(commandId);
        if (cmd != null) {
            List<String> siItem = (List<String>) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
            cmd.setSymbols(siItem);
            siItem = (List<String>) scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
            siItem = CygpathTranslator.translateIncludePaths(project, siItem);
            siItem = CCommandDSC.makeRelative(project, siItem);
            cmd.setIncludes(siItem);
            siItem = (List<String>) scannerInfo.get(ScannerInfoTypes.QUOTE_INCLUDE_PATHS);
            siItem = CygpathTranslator.translateIncludePaths(project, siItem);
            siItem = CCommandDSC.makeRelative(project, siItem);
            cmd.setQuoteIncludes(siItem);
            
            cmd.setDiscovered(true);
        }
    }

    private void addCompilerCommand(IFile file, CCommandDSC cmd) {
		assert Thread.holdsLock(fLock);
        List<CCommandDSC> existingCommands = new ArrayList<CCommandDSC>(sid.commandIdCommandMap.values());
        int index = existingCommands.indexOf(cmd);
        if (index != -1) {
            cmd = existingCommands.get(index);
        }
        else {
            int commandId = -1;
            if (!freeCommandIdPool.isEmpty()) {
                Integer freeCommandId = freeCommandIdPool.first();
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

	private void generateFileDelta(IFile file, CCommandDSC cmd) {
		assert Thread.holdsLock(fLock);
        Integer commandId = cmd.getCommandIdAsInteger();
		Integer oldCommandId = sid.fileToCommandIdMap.get(file);

		if (oldCommandId != null && oldCommandId.equals(commandId)) {
			// already exists; remove form delta
			siChangedForFileMap.remove(file);
		}
		else {
			// new (file, commandId) pair
			siChangedForFileMap.put(file, commandId);
		}
	}

	private void applyFileDeltas() {
		assert Thread.holdsLock(fLock);
		for (Iterator<IResource> i = siChangedForFileMap.keySet().iterator(); i.hasNext(); ) {
			IFile file = (IFile) i.next();
			Integer commandId = siChangedForFileMap.get(file);
			if (commandId != null) {
			
		        // update sid.commandIdToFilesMap
		        Set<IFile> fileSet = sid.commandIdToFilesMap.get(commandId);
		        if (fileSet == null) {
		            fileSet = new HashSet<IFile>();
		            sid.commandIdToFilesMap.put(commandId, fileSet);
		            CCommandDSC cmd = sid.commandIdCommandMap.get(commandId);
		            if (cmd != null) {
		            	cmd.resolveOptions(project);
		            }
		        }
		        if (fileSet.add(file)) {
		            // update fileToCommandIdsMap
		            boolean change = true;
		            Integer oldCommandId = sid.fileToCommandIdMap.get(file);
		            if (oldCommandId != null) {
		                if (oldCommandId.equals(commandId)) {
		                    change = false;
		                }
		                else {
		                    Set oldFileSet = sid.commandIdToFilesMap.get(oldCommandId);
		                    if (oldFileSet != null) {
		                    	oldFileSet.remove(file);
		                    }
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
		assert Thread.holdsLock(fLock);
		psi = new ProjectScannerInfo();
		psi.includePaths = getAllIncludePaths(INCLUDE_PATH);
		psi.quoteIncludePaths = getAllIncludePaths(QUOTE_INCLUDE_PATH);
		psi.includeFiles = getAllIncludePaths(INCLUDE_FILE);
		psi.macrosFiles = getAllIncludePaths(MACROS_FILE);
		psi.definedSymbols = getAllSymbols();
	}

	private void removeUnusedCommands() {
		assert Thread.holdsLock(fLock);
        for (Iterator i = sid.commandIdToFilesMap.entrySet().iterator(); i.hasNext(); ) {
            Entry entry = (Entry) i.next();
            Integer cmdId = (Integer) entry.getKey();
            Set fileSet = (Set) entry.getValue();
            if (fileSet.isEmpty()) {
                // return cmdId to the free command id pool
                freeCommandIdPool.add(cmdId);
            }
        }
        for (Iterator<Integer> i = freeCommandIdPool.iterator(); i.hasNext(); ) {
            Integer cmdId = i.next();
            // the command does not have any files associated; remove
            sid.commandIdCommandMap.remove(cmdId);
            sid.commandIdToFilesMap.remove(cmdId);
        }
        while (!freeCommandIdPool.isEmpty()) { 
            Integer last = freeCommandIdPool.last(); 
            if (last.intValue() == commandIdCounter) {
                freeCommandIdPool.remove(last);
                --commandIdCounter;
            }
            else break;
        }
    }
    
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
        monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Processing")); //$NON-NLS-1$
        ArrayList<IResource> changedResources = null;
        synchronized (fLock) {
        	if (scannerInfoChanged()) {
        		applyFileDeltas();
        		removeUnusedCommands();
        		changedResources = new ArrayList<IResource>(siChangedForFileMap.keySet());
        		siChangedForFileMap.clear();
        	}
        	siChangedForCommandIdList.clear();
        }
	    monitor.worked(50);
        if (changedResources != null) {
	        // update outside monitor scope
	        try {
	        	// update scanner configuration
	        	monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Updating") + project.getName()); //$NON-NLS-1$
	        	IDiscoveredPathInfo pathInfo = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project, context);
	        	if (!(pathInfo instanceof IPerFileDiscoveredPathInfo)) {
	        		pathInfo = createPathInfoObject();
	        	}
	        	MakeCorePlugin.getDefault().getDiscoveryManager().updateDiscoveredInfo(context, pathInfo, context.isDefaultContext(), changedResources);
	        } catch (CoreException e) {
	        	MakeCorePlugin.log(e);
	        }
	    }
	    monitor.worked(50);
		monitor.done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#createPathInfoObject()
     */
    public IDiscoveredPathInfo createPathInfoObject() {
        return new PerFileDiscoveredPathInfo();
    }

	private boolean scannerInfoChanged() {
		assert Thread.holdsLock(fLock);
//		return !siChangedForFileList.isEmpty();
		return !siChangedForFileMap.isEmpty();
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#getCollectedScannerInfo(java.lang.Object, org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes)
     */
    public List<CCommandDSC> getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
        List<CCommandDSC> rv = new ArrayList<CCommandDSC>();
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
            return rv;
        }
        if (project.equals(((IResource)resource).getProject())) {
        	if (type.equals(ScannerInfoTypes.COMPILER_COMMAND)) {
        		synchronized (fLock) {
        			for (Iterator<Integer> i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext(); ) {
        				Integer cmdId = i.next();
        				Set<IFile> fileSet = sid.commandIdToFilesMap.get(cmdId);
        				if (fileSet != null && !fileSet.isEmpty()) {
        					rv.add(sid.commandIdCommandMap.get(cmdId));
        				}
        			}
        		}
        	}
        	else if (type.equals(ScannerInfoTypes.UNDISCOVERED_COMPILER_COMMAND)) {
//      		if (!siChangedForFileList.isEmpty()) {
    			synchronized (fLock) {
    				if (scannerInfoChanged()) {
    					if (siChangedForCommandIdList.isEmpty()) {
//  						for (Iterator i = siChangedForFileList.iterator(); i.hasNext(); ) {
    						for (Iterator<IResource> i = siChangedForFileMap.keySet().iterator(); i.hasNext(); ) {
//  							IPath path = (IPath) i.next();
    							IFile file = (IFile) i.next();
    							Integer cmdId = siChangedForFileMap.get(file);
    							if (cmdId != null) {
    								if (!siChangedForCommandIdList.contains(cmdId)) {
    									siChangedForCommandIdList.add(cmdId);
    								}
    							}
    						}
    					}
    					Collections.sort(siChangedForCommandIdList);
    					for (Iterator<Integer> i = siChangedForCommandIdList.iterator(); i.hasNext(); ) {
    						Integer cmdId = i.next();
    						CCommandDSC command = sid.commandIdCommandMap.get(cmdId);
    						rv.add(command);
    					}
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
        	synchronized (fLock) {
//            	siChangedForFileList = new ArrayList();
	            siChangedForFileMap.clear();
	            Set<IFile> changedFiles = sid.fileToCommandIdMap.keySet();
	            for (Iterator<IFile> i = changedFiles.iterator(); i.hasNext(); ) {
	                IFile file = i.next();
//	                IPath path = file.getFullPath();
//	                siChangedForFileList.add(path);
	                siChangedForFileMap.put(file, null);
	            }
	
	            sid = new ScannerInfoData();
	            psi = null;
	            
	            commandIdCounter = 0;
				freeCommandIdPool.clear();
        	}
        }
    }

    /**
     * Per file DPI object
     * 
     * @author vhirsl
     */
    private class PerFileDiscoveredPathInfo implements IPerFileDiscoveredPathInfo2 {
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
        	final IPath[] includepaths;
        	final IPath[] quotepaths;
        	synchronized (PerFileSICollector.this.fLock) {
//      		return new IPath[0];
	        	includepaths = getAllIncludePaths(INCLUDE_PATH);
	        	quotepaths = getAllIncludePaths(QUOTE_INCLUDE_PATH);
        	}
        	if (quotepaths == null || quotepaths.length == 0) {
        		return includepaths;
        	}
        	if (includepaths == null || includepaths.length == 0) {
        		return quotepaths;
        	}
        	ArrayList<IPath> result = new ArrayList<IPath>(includepaths.length + quotepaths.length);
        	result.addAll(Arrays.asList(includepaths));
        	result.addAll(Arrays.asList(quotepaths));
            return result.toArray(new IPath[result.size()]);
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSymbols()
         */
        public Map<String, String> getSymbols() {
//            return new HashMap();
        	synchronized (PerFileSICollector.this.fLock) {
        		return getAllSymbols();
        	}
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludePaths(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getIncludePaths(IPath path) {
        	synchronized (PerFileSICollector.this.fLock) {
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
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getQuoteIncludePaths(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getQuoteIncludePaths(IPath path) {
        	synchronized (PerFileSICollector.this.fLock) {
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
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSymbols(org.eclipse.core.runtime.IPath)
         */
        public Map<String, String> getSymbols(IPath path) {
        	synchronized (PerFileSICollector.this.fLock) {
	            // get the command
	            CCommandDSC cmd = getCommand(path);
	            if (cmd != null && cmd.isDiscovered()) {
	                List symbols = cmd.getSymbols();
	                Map<String, String> definedSymbols = new HashMap<String, String>(symbols.size());
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
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getIncludeFiles(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getIncludeFiles(IPath path) {
        	synchronized (PerFileSICollector.this.fLock) {
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
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getMacroFiles(org.eclipse.core.runtime.IPath)
         */
        public IPath[] getMacroFiles(IPath path) {
        	synchronized (PerFileSICollector.this.fLock) {
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
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerFileDiscoveredPathInfo#getSerializable()
         */
        public IDiscoveredScannerInfoSerializable getSerializable() {
        	synchronized (PerFileSICollector.this.fLock) {
        		return sid;
        	}
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
		        	synchronized (PerFileSICollector.this.fLock) {
		        		rc = (psi == null || psi.isEmpty());
		        	}
				}
			}
			return rc;
		}

		public Map<IResource, PathInfo> getPathInfoMap() {
        	synchronized (PerFileSICollector.this.fLock) {
				//TODO: do we need to cache this?
				return calculatePathInfoMap();
        	}
		}

    }
    
    private Map<IResource, PathInfo> calculatePathInfoMap(){
		assert Thread.holdsLock(fLock);
    	Map<IResource, PathInfo> map = new HashMap<IResource, PathInfo>(sid.fileToCommandIdMap.size() + 1);
    	Map.Entry entry;
    	IFile file;
    	CCommandDSC cmd;
    	PathInfo fpi;
    	for(Iterator iter = sid.fileToCommandIdMap.entrySet().iterator(); iter.hasNext();){
    		entry = (Map.Entry)iter.next();
    		file = (IFile)entry.getKey();
    		if(file != null){
	    		cmd = sid.commandIdCommandMap.get(entry.getValue());
	    		if(cmd != null){
	    			fpi = createFilePathInfo(cmd);
	    			map.put(file, fpi);
	    		}
    		}
    	}
    	
    	if(project != null){
	    	if(psi == null){
	    		generateProjectScannerInfo();
	    	}
	    	
	    	fpi = new PathInfo(psi.includePaths, psi.quoteIncludePaths, psi.definedSymbols, psi.includeFiles, psi.macrosFiles);
	    	map.put(project, fpi);
    	}
    	
    	return map;
    }
    
    private static PathInfo createFilePathInfo(CCommandDSC cmd){
    	IPath[] includes = stringListToPathArray(cmd.getIncludes());
    	IPath[] quotedIncludes = stringListToPathArray(cmd.getQuoteIncludes());
    	IPath[] incFiles = stringListToPathArray(cmd.getIncludeFile());
    	IPath[] macroFiles = stringListToPathArray(cmd.getImacrosFile());
        List symbols = cmd.getSymbols();
        Map<String, String> definedSymbols = new HashMap<String, String>(symbols.size());
        for (Iterator i = symbols.iterator(); i.hasNext(); ) {
            String symbol = (String) i.next();
            String key = ScannerConfigUtil.getSymbolKey(symbol);
            String value = ScannerConfigUtil.getSymbolValue(symbol);
            definedSymbols.put(key, value);
        }
        
        return new PathInfo(includes, quotedIncludes, definedSymbols, incFiles, macroFiles);
    }

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
            Integer cmdId = sid.fileToCommandIdMap.get(file);
            if (cmdId != null) {
                // get the command
                cmd = sid.commandIdCommandMap.get(cmdId);
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
    	List<String> allIncludes = new ArrayList<String>();
        for (Iterator<Integer> i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext(); ) {
            Integer cmdId = i.next();
            CCommandDSC cmd = sid.commandIdCommandMap.get(cmdId);
            if (cmd.isDiscovered()) {
    			List<String> discovered = null;
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
    			for (Iterator<String> j = discovered.iterator(); j.hasNext(); ) {
    			    String include = j.next();
    			    // the following line degrades perfomance
    			    // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=189127
    			    // it is not necessary for renaming projects anyway
    			    // include = CCommandDSC.makeRelative(project, new Path(include)).toPortableString();
    			    if (!allIncludes.contains(include)) {
    			        allIncludes.add(include);
    			    }
    			}
            }
        }
        return stringListToPathArray(allIncludes);
    }

	private static IPath[] stringListToPathArray(List<String> discovered) {
		List<Path> allIncludes = new ArrayList<Path>(discovered.size());
		for (Iterator<String> j = discovered.iterator(); j.hasNext(); ) {
		    String include = j.next();
		    if (!allIncludes.contains(include)) {
		        allIncludes.add(new Path(include));
		    }
		}
		return allIncludes.toArray(new IPath[allIncludes.size()]);
	}

    private Map<String, String> getAllSymbols() {
		assert Thread.holdsLock(fLock);
        Map<String, String> symbols = new HashMap<String, String>();
        for (Iterator<Integer> i = sid.commandIdCommandMap.keySet().iterator(); i.hasNext(); ) {
            Integer cmdId = i.next();
            CCommandDSC cmd = sid.commandIdCommandMap.get(cmdId);
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
