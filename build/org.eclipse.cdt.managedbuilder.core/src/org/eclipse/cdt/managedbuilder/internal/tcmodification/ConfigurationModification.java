/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.internal.core.IRealBuildObjectAssociation;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ToolChainModificationManager.ConflictMatch;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ToolChainModificationManager.ConflictMatchSet;
import org.eclipse.cdt.managedbuilder.tcmodification.CompatibilityStatus;
import org.eclipse.cdt.managedbuilder.tcmodification.IConfigurationModification;
import org.eclipse.core.runtime.IStatus;

public class ConfigurationModification extends FolderInfoModification implements
		IConfigurationModification {
	private IBuilder fSelectedBuilder;
	private IBuilder fRealBuilder;
	private boolean fCompatibilityInfoInited;
	private Map<IBuilder, BuilderCompatibilityInfoElement> fCompatibleBuilders;
	private Map<IBuilder, BuilderCompatibilityInfoElement> fInCompatibleBuilders;
	private ConflictMatchSet fConflicts;
	private IBuilder[] fAllSysBuilders;
	private BuilderCompatibilityInfoElement fCurrentBuilderCompatibilityInfo;


	public static class BuilderCompatibilityInfoElement {
		private Builder fBuilder;
		private List<ConflictMatch> fErrComflictMatchList;
		private CompatibilityStatus fStatus;
		
		BuilderCompatibilityInfoElement(Builder builder, List<ConflictMatch> errConflictList){
			fBuilder = builder;
			if(errConflictList != null && errConflictList.size() != 0)
				fErrComflictMatchList = errConflictList;
		}
		
		public CompatibilityStatus getCompatibilityStatus(){
			if(fStatus == null){
				int severity;
				String message;
				if(fErrComflictMatchList != null){
					severity = IStatus.ERROR;
					message = Messages.getString("ConfigurationModification.0"); //$NON-NLS-1$
				} else {
					severity = IStatus.OK;
					message = ""; //$NON-NLS-1$
				}
				fStatus = new CompatibilityStatus(severity, message, new ConflictSet(fBuilder, fErrComflictMatchList, null));
			}
			return fStatus;
		}
		
		public boolean isCompatible(){
			return fErrComflictMatchList == null;
		}
	}
	
	public ConfigurationModification(FolderInfo foInfo) {
		super(foInfo);
		
		setBuilder(foInfo.getParent().getBuilder());
	}

	public ConfigurationModification(FolderInfo foInfo, ConfigurationModification base) {
		super(foInfo, base);
		
		fSelectedBuilder = base.fSelectedBuilder;
		if(!fSelectedBuilder.isExtensionElement())
			fSelectedBuilder = ManagedBuildManager.getExtensionBuilder(fSelectedBuilder);
			
		fRealBuilder = base.fRealBuilder;
	}

	public IBuilder getBuilder() {
		return fSelectedBuilder;
	}

	public IBuilder getRealBuilder() {
		return fRealBuilder;
	}

	public CompatibilityStatus getBuilderCompatibilityStatus() {
		return getCurrentBuilderCompatibilityInfo().getCompatibilityStatus();
	}

	private ConflictMatchSet getParentConflictMatchSet(){
		if(fConflicts == null){
			PerTypeMapStorage storage = getCompleteObjectStore();
			Object restore = TcModificationUtil.removeBuilderInfo(storage, fRealBuilder);
			try {
				fConflicts = ToolChainModificationManager.getInstance().getConflictInfo(IRealBuildObjectAssociation.OBJECT_BUILDER, storage);
			} finally {
				if(restore != null)
					TcModificationUtil.restoreBuilderInfo(storage, fRealBuilder, restore);
			}
		}
		return fConflicts;
	}
	
	private IBuilder[] getAllSysBuilders(){
		if(fAllSysBuilders == null)
			fAllSysBuilders = ManagedBuildManager.getRealBuilders();
		return fAllSysBuilders;
	}
	
	private void initCompatibilityInfo(){
		if(fCompatibilityInfoInited)
			return;
		
		fCompatibleBuilders = new HashMap<IBuilder, BuilderCompatibilityInfoElement>();
		fInCompatibleBuilders = new HashMap<IBuilder, BuilderCompatibilityInfoElement>();
		ConflictMatchSet conflicts = getParentConflictMatchSet();
		IBuilder sysBs[] = getAllSysBuilders();
		Map<IBuilder, List<ConflictMatch>> conflictMap = conflicts.fObjToConflictListMap;
		for(int i = 0; i < sysBs.length; i++){
			Builder b = (Builder) sysBs[i];
			List<ConflictMatch> l = conflictMap.get(b);
			BuilderCompatibilityInfoElement info = new BuilderCompatibilityInfoElement(b, l);
			if(info.isCompatible()){
				fCompatibleBuilders.put(b, info);
			} else {
				fInCompatibleBuilders.put(b, info);
			}
		}
		
		fCompatibilityInfoInited = true;
	}
	
	private BuilderCompatibilityInfoElement getCurrentBuilderCompatibilityInfo(){
		if(fCurrentBuilderCompatibilityInfo == null){
			initCompatibilityInfo();
			BuilderCompatibilityInfoElement info = fCompatibleBuilders.get(fRealBuilder);
			if(info == null)
				info = fInCompatibleBuilders.get(fRealBuilder);
			fCurrentBuilderCompatibilityInfo = info;
		}
		return fCurrentBuilderCompatibilityInfo;
	}
	
	public IBuilder[] getCompatibleBuilders() {
		initCompatibilityInfo();
		List<IBuilder> l = new ArrayList<IBuilder>(fCompatibleBuilders.size());
		IConfiguration cfg = getResourceInfo().getParent();
		
		Set<IBuilder> keySet = fCompatibleBuilders.keySet();
		for (IBuilder b : keySet) {
			if(b != fRealBuilder && cfg.isBuilderCompatible(b))
				l.add(b);
		}
		return l.toArray(new IBuilder[l.size()]);
	}

	public boolean isBuilderCompatible() {
		BuilderCompatibilityInfoElement be = getCurrentBuilderCompatibilityInfo(); 
		return be == null ? false : be.isCompatible();
	}

	public void setBuilder(IBuilder builder) {
		if(builder == fSelectedBuilder)
			return;
		
		fSelectedBuilder = builder;
		IBuilder realBuilder = ManagedBuildManager.getRealBuilder(builder);
		if(realBuilder == fRealBuilder)
			return;
		
		fRealBuilder = realBuilder;
		fCompletePathMapStorage = null;
		
		PerTypeMapStorage storage = getCompleteObjectStore();
		TcModificationUtil.applyBuilder(storage, getResourceInfo().getPath(), fSelectedBuilder);
		
		clearBuilderCompatibilityInfo();
		clearToolChainCompatibilityInfo();
		clearToolCompatibilityInfo();
	}
	
	@Override
	public void setToolChain(IToolChain tc, boolean force) {
		setBuilder(tc.getBuilder());
		super.setToolChain(tc, force);
	}

	@Override
	public void changeProjectTools(ITool removeTool, ITool addTool) {
		clearBuilderCompatibilityInfo();
		super.changeProjectTools(removeTool, addTool);
	}

	protected void clearBuilderCompatibilityInfo(){
		fInCompatibleBuilders = null;
		fCompatibleBuilders = null;
		fCompatibilityInfoInited = false;
		fCurrentBuilderCompatibilityInfo = null;
	}

}
