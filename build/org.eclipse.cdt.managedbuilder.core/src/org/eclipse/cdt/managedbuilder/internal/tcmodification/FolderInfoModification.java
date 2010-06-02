/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceInfo;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.internal.dataprovider.ConfigurationDataProvider;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ToolChainModificationManager.ConflictMatchSet;
import org.eclipse.cdt.managedbuilder.tcmodification.CompatibilityStatus;
import org.eclipse.cdt.managedbuilder.tcmodification.IFolderInfoModification;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

public class FolderInfoModification extends ToolListModification implements IFolderInfoModification {
	private ToolChain fRealToolChain;
	private ToolChainCompatibilityInfoElement fCurrentCompatibilityInfo;
	private ToolChain fSelectedToolChain;
	private IToolChain[] fAllSysToolChains;
	private Map fCompatibleToolChains;
	private Map fInCompatibleToolChains;
	private PerTypeMapStorage fParentObjectStorage;
	private ConflictMatchSet fParentConflicts;
//	private PerTypeMapStorage fChildObjectStorage;
//	private ConflictMatchSet fChildConflicts;
	private boolean fCompatibilityInfoInited;
	private ToolChainApplicabilityPaths fTcApplicabilityPaths;

	public FolderInfoModification(FolderInfo foInfo) {
		super(foInfo, foInfo.getTools());
		fSelectedToolChain = (ToolChain)foInfo.getToolChain();
		fRealToolChain = (ToolChain)ManagedBuildManager.getRealToolChain(fSelectedToolChain);
	}

	public FolderInfoModification(FolderInfo foInfo, FolderInfoModification base) {
		super(foInfo, base);
		fSelectedToolChain = base.fSelectedToolChain;
		if(!fSelectedToolChain.isExtensionElement())
			fSelectedToolChain = (ToolChain)fSelectedToolChain.getExtensionObject();
		
		fRealToolChain = base.fRealToolChain;
	}
	
	private ConflictMatchSet getParentConflictMatchSet(){
		if(fParentConflicts == null){
			PerTypeMapStorage storage = getParentObjectStorage();
			fParentConflicts = ToolChainModificationManager.getInstance().getConflictInfo(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, storage);
		}
		return fParentConflicts;
	}
	
	private PerTypeMapStorage getParentObjectStorage(){
		if(fParentObjectStorage == null){
			fParentObjectStorage = TcModificationUtil.createParentObjectsRealToolToPathSet((FolderInfo)getResourceInfo());
		}
		return fParentObjectStorage;
	}
	
	private IToolChain[] getAllSysToolChains(){
		if(fAllSysToolChains == null)
			fAllSysToolChains = ManagedBuildManager.getRealToolChains();
		return fAllSysToolChains;
	}
	
	private static class ToolChainApplicabilityPaths {
		private Set fFileInfoPaths = new HashSet();
		private Set fFolderInfoPaths = new HashSet();
		private Map fToolPathMap = new HashMap(); 
	}
	
	public static class ToolChainCompatibilityInfoElement {
		private ToolChain fTc;
		private List fErrComflictMatchList;
		private List fWarningConflictMatchList;
		private CompatibilityStatus fStatus;
		
		ToolChainCompatibilityInfoElement(ToolChain tc, List errConflictList){
			fTc = tc;
			if(errConflictList != null && errConflictList.size() != 0)
				fErrComflictMatchList = errConflictList;
		}
		
		public CompatibilityStatus getCompatibilityStatus(){
			if(fStatus == null){
				int severity;
				String message;
				if(fErrComflictMatchList != null){
					severity = IStatus.ERROR;
					message = Messages.getString("FolderInfoModification.0"); //$NON-NLS-1$
				} else {
					severity = IStatus.OK;
					message = ""; //$NON-NLS-1$
				}
				fStatus = new CompatibilityStatus(severity, message, new ConflictSet(fTc, fErrComflictMatchList, null));
			}
			return fStatus;
		}
		
		public boolean isCompatible(){
			return fErrComflictMatchList == null;
		}
	}
	
	public IToolChain[] getCompatibleToolChains(){
		initCompatibilityInfo();
		FolderInfo foInfo = (FolderInfo)getResourceInfo();

		List l = new ArrayList(fCompatibleToolChains.size());
		for(Iterator iter = fCompatibleToolChains.keySet().iterator(); iter.hasNext(); ){
			ToolChain tc = (ToolChain)iter.next();

			if(tc != fRealToolChain && foInfo.isToolChainCompatible(fRealToolChain, tc))
				l.add(tc);
		}
		return (ToolChain[])l.toArray(new ToolChain[l.size()]);
	}
	
	public CompatibilityStatus getToolChainCompatibilityStatus(){
		return getCurrentCompatibilityInfo().getCompatibilityStatus();
	}
	
	private ToolChainCompatibilityInfoElement getCurrentCompatibilityInfo(){
		if(fCurrentCompatibilityInfo == null){
			initCompatibilityInfo();
			ToolChainCompatibilityInfoElement info = (ToolChainCompatibilityInfoElement)fCompatibleToolChains.get(fRealToolChain);
			if(info == null)
				info = (ToolChainCompatibilityInfoElement)fInCompatibleToolChains.get(fRealToolChain);
			fCurrentCompatibilityInfo = info;
		}
		return fCurrentCompatibilityInfo;
	}
	
	public boolean isToolChainCompatible(){
		return getCurrentCompatibilityInfo().isCompatible();
	}
	
	private void initCompatibilityInfo(){
		if(fCompatibilityInfoInited)
			return;
		
		fCompatibleToolChains = new HashMap();
		fInCompatibleToolChains = new HashMap();
		ConflictMatchSet parentConflicts = getParentConflictMatchSet();
		ToolChain sysTCs[] = (ToolChain[])getAllSysToolChains();
		
		Map conflictMap = parentConflicts.fObjToConflictListMap;
		for(int i = 0; i < sysTCs.length; i++){
			ToolChain tc = sysTCs[i];
			List l = (List)conflictMap.get(tc);
			ToolChainCompatibilityInfoElement info = new ToolChainCompatibilityInfoElement(tc, l);
			if(info.isCompatible()){
				fCompatibleToolChains.put(tc, info);
			} else {
				fInCompatibleToolChains.put(tc, info);
			}
		}
		
		fCompatibilityInfoInited = true;
	}
	
	public IToolChain getToolChain(){
		return fSelectedToolChain;
	}

	public final void setToolChain(IToolChain tc){
		setToolChain(tc, false);
	}

	public void setToolChain(IToolChain tc, boolean force){
		if(tc == fSelectedToolChain && !force)
			return;
	
		applyToolChain((ToolChain)tc);

		fSelectedToolChain = (ToolChain)tc;
		IToolChain newReal = ManagedBuildManager.getRealToolChain(tc);
		if(newReal == fRealToolChain && !force)
			return;

		fRealToolChain = (ToolChain)newReal;

//		setProjectTools(tc.getTools());
//		applyToolChain(fSelectedToolChain);
		clearToolInfo(tc.getTools());
		fCurrentCompatibilityInfo = null;
		
	}

	protected void clearToolChainCompatibilityInfo(){
		fCompatibilityInfoInited = false;
		fCompatibleToolChains = null;
		fInCompatibleToolChains = null;
		fCurrentCompatibilityInfo = null;
	}
	
	protected boolean canRemove(ITool realTool) {
		IToolChain extTc = ManagedBuildManager.getExtensionToolChain(fSelectedToolChain);
		ITool[] tools = extTc.getTools();
		for(int i = 0; i < tools.length; i++){
			if(realTool == ManagedBuildManager.getRealTool(tools[i]))
				return false;
		}
		
		return true;
	}

	protected boolean canAdd(Tool tool) {
		return !TcModificationUtil.containCommonEntries(getInputExtsSet(), tool.getPrimaryInputExtensions());
	}

	protected boolean canReplace(Tool fromTool, Tool toTool) {
		String[] exts = toTool.getPrimaryInputExtensions();
		Set curInputExts = null;
		Set inputExts = getInputExtsSet();
		for(int k = 0; k < exts.length; k++){
			if(inputExts.contains(exts[k])){
				if(curInputExts == null)
					curInputExts = new HashSet(Arrays.asList(fromTool.getPrimaryInputExtensions()));
				
				if(curInputExts.contains(exts[k])){
					return true;
				}
			}
		}
		return false;
	}

	protected Set getExtensionConflictToolSet(Tool tool, Tool[] tools) {
		String exts[] = tool.getPrimaryInputExtensions();
		Set extsSet = new HashSet(Arrays.asList(exts));
		Set conflictsSet = null;
		for(int i = 0; i < tools.length; i++){
			Tool t = tools[i];
			if(t == tool)
				continue;
			if(TcModificationUtil.containCommonEntries(extsSet, t.getPrimaryInputExtensions())){
				if(conflictsSet == null)
					conflictsSet = new HashSet();
				
				conflictsSet.add(t);
			}
					
		}
		
		if(conflictsSet == null)
			conflictsSet = Collections.EMPTY_SET;
		return conflictsSet;
	}

	protected Set getToolApplicabilityPathSet(Tool realTool, boolean isProject) {
		if(isProject)
			return (Set)getToolChainApplicabilityPaths().fToolPathMap.get(realTool);
		return getToolChainApplicabilityPaths().fFolderInfoPaths;
	}

	protected Tool[] filterTools(Tool[] tools) {
		IResourceInfo rcInfo = getResourceInfo();
		return (Tool[])((FolderInfo)rcInfo).filterTools(tools, rcInfo.getParent().getManagedProject());
	}

	private ToolChainApplicabilityPaths getToolChainApplicabilityPaths(){
		initToolChainApplicabilityPaths();
		return fTcApplicabilityPaths;
	}
	
	private void initToolChainApplicabilityPaths(){
		if(fTcApplicabilityPaths != null)
			return;
		
		ToolChainApplicabilityPaths tcApplicabilityPaths = new ToolChainApplicabilityPaths();
		IPath path = getResourceInfo().getPath();
		
		TreeMap pathMap = getCompletePathMapStorage();
		
		PerTypeSetStorage oSet = (PerTypeSetStorage)pathMap.get(path);
		Set toolSet = oSet.getSet(IRealBuildObjectAssociation.OBJECT_TOOL, false);
		Set tcSet = oSet.getSet(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, false);
		
		ToolChain curTc = (ToolChain)tcSet.iterator().next();
		
		Set foInfoPaths = tcApplicabilityPaths.fFolderInfoPaths;
		Set fileInfoPaths = tcApplicabilityPaths.fFileInfoPaths;

		foInfoPaths.add(path);
		
		Map toolPathsMap = tcApplicabilityPaths.fToolPathMap;
		if(toolSet != null){
			for(Iterator iter = toolSet.iterator(); iter.hasNext(); ){
				Set set = new HashSet();
				toolPathsMap.put(iter.next(), set);
				set.add(path);
			}
		}
		
		calculateChildPaths(pathMap, path, curTc, foInfoPaths, toolPathsMap, fileInfoPaths);
		
		fTcApplicabilityPaths = tcApplicabilityPaths;
	}
	
	protected void clearToolInfo(ITool[] tools){
		super.clearToolInfo(tools);
		fTcApplicabilityPaths = null;
	}
	
	private static void putToolInfo(Set ct, Map toolPathsMap, Set fileInfoPaths, Object p){
		if(ct != null && ct.size() != 0){
			for(Iterator toolIter = ct.iterator(); toolIter.hasNext(); ){
				Object t = toolIter.next();
				Set set = (Set)toolPathsMap.get(t);
				if(set != null){
					if(fileInfoPaths != null)
						fileInfoPaths.add(p);
					set.add(p);
				}
			}
		}
	}

	private static void calculateChildPaths(TreeMap pathMap, IPath path, ToolChain tc, Set tcPaths, Map toolPathsMap, Set fileInfoPaths){
		SortedMap directCMap = PathComparator.getDirectChildPathMap(pathMap, path);
		for(Iterator iter = directCMap.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry)iter.next();
			PerTypeSetStorage cst = (PerTypeSetStorage)entry.getValue();
			
			Set ctc = cst.getSet(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, false);
			Set ct = cst.getSet(IRealBuildObjectAssociation.OBJECT_TOOL, false);


			if(ctc == null || ctc.size() == 0){
				//fileInfo, check for tools
				putToolInfo(ct, toolPathsMap, fileInfoPaths, entry.getKey());
			} else {
				if(ctc.contains(tc)){
					IPath cp = (IPath)entry.getKey();
					tcPaths.add(cp);
					putToolInfo(ct, toolPathsMap, null, entry.getKey());
					//recurse
					calculateChildPaths(pathMap, cp, tc, tcPaths, toolPathsMap, fileInfoPaths);
				}
			}
		}
	}
	
	private void applyToolChain(ToolChain newNonRealTc){
		ToolChain newRealTc = (ToolChain)ManagedBuildManager.getRealToolChain(newNonRealTc);
		
		ToolChainApplicabilityPaths tcApplicability = getToolChainApplicabilityPaths();
		PerTypeMapStorage storage = getCompleteObjectStore();
		
		Map tcMap = storage.getMap(IRealBuildObjectAssociation.OBJECT_TOOLCHAIN, false);
		Map toolMap = storage.getMap(IRealBuildObjectAssociation.OBJECT_TOOL, false);
		
		
		TcModificationUtil.removePaths(tcMap, fRealToolChain, tcApplicability.fFolderInfoPaths);
		TcModificationUtil.addPaths(tcMap, newRealTc, tcApplicability.fFolderInfoPaths);
		Tool[] newTools = (Tool[])newNonRealTc.getTools();
		
		for(int i = 0; i < newTools.length; i++){
			newTools[i] = (Tool)ManagedBuildManager.getRealTool(newTools[i]);
		}
		
		for(Iterator iter = tcApplicability.fToolPathMap.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry)iter.next();
			Tool tool = (Tool)entry.getKey();
			Set pathSet = (Set)entry.getValue();
			
			TcModificationUtil.removePaths(toolMap, tool, pathSet);
		}
		
		for(int i = 0; i < newTools.length; i++){
			TcModificationUtil.addPaths(toolMap, newTools[i], tcApplicability.fFolderInfoPaths);
		}

		if(tcApplicability.fFileInfoPaths.size() != 0){
			FolderInfo foInfo = (FolderInfo)getResourceInfo();
			IManagedProject mProj = foInfo.getParent().getManagedProject();
			IProject project = mProj.getOwner().getProject();
			Tool[] filtered = (Tool[])foInfo.filterTools(newTools, mProj);
			if(filtered.length != 0){
				for(Iterator iter = tcApplicability.fFileInfoPaths.iterator(); iter.hasNext(); ){
					IPath p = (IPath)iter.next();
					boolean found = false;
					String ext = p.getFileExtension();
					if(ext == null)
						ext = ""; //$NON-NLS-1$
					for(int i = 0; i < filtered.length; i++){
						if(filtered[i].buildsFileType(ext, project)){
							TcModificationUtil.addPath(toolMap, filtered[i], p);
							found = true;
							break;
						}
					}
					
					if(!found){
						if (DbgTcmUtil.DEBUG){
							DbgTcmUtil.println("no tools found for path " + p); //$NON-NLS-1$
						}
					}
				}
				
			} else if (DbgTcmUtil.DEBUG){
				DbgTcmUtil.println("no filtered tools"); //$NON-NLS-1$
			}
		}
	}

	private IToolChain getDefaultToolChain(){
		IResourceInfo rcInfo = getResourceInfo();
		IToolChain defaultTc = null;
		if (rcInfo.getPath().segmentCount() == 0) {
//			1.Per-project : change to the "default" tool-chain defined in the extension
//			super-class of the project configuration. NOTE: the makefile project case might
//			need a special handling in this case.
			
			IConfiguration cfg = rcInfo.getParent();
			IConfiguration extCfg = cfg.getParent();
			defaultTc = extCfg.getToolChain(); 
			if (defaultTc == null) {
				if (cfg.getToolChain() != null) {
					defaultTc = cfg.getToolChain().getSuperClass();
				}
			}
		} else {
//			2.per-folder : change to the same tool-chain as the one used by the parent
//			folder.
			IFolderInfo parentFo = ((ResourceInfo)rcInfo).getParentFolderInfo();
			IToolChain tc = parentFo.getToolChain();
			defaultTc = ManagedBuildManager.getExtensionToolChain(tc);
		}
		
		if(defaultTc != null && defaultTc.getId().equals(ConfigurationDataProvider.PREF_TC_ID))
			defaultTc = null;

		return defaultTc;
	}
	
	public final void restoreDefaults() {
		IToolChain tc = getDefaultToolChain();
		if(tc != null){
			setToolChain(tc, true);
		}
	}
}
