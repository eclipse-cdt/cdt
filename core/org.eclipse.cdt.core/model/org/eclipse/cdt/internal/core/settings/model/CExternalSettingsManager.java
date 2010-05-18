/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettinsDeltaCalculator.ExtSettingsDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

public class CExternalSettingsManager implements ICExternalSettingsListener, ICProjectDescriptionListener{
	private static final int OP_CHANGED = 1;
	private static final int OP_ADDED = 2;
	private static final int OP_REMOVED = 3;
	
	private static final QualifiedName EXTERNAL_SETTING_PROPERTY = new QualifiedName(CCorePlugin.PLUGIN_ID, "externalSettings"); //$NON-NLS-1$
	private static final String EXTERNAL_SETTING_STORAGE_ID = CCorePlugin.PLUGIN_ID + ".externalSettings"; //$NON-NLS-1$
	
	private Map<String, FactoryDescriptor> fFactoryMap = new HashMap<String, FactoryDescriptor>();
	private static CExternalSettingsManager fInstance;
	
	private CExternalSettingsManager(){
	}

	public void startup(){
		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(this, CProjectDescriptionEvent.DATA_APPLIED
				| CProjectDescriptionEvent.LOADED);
	}

	public void shutdown(){
		for(Iterator<FactoryDescriptor> iter = fFactoryMap.values().iterator(); iter.hasNext();){
			FactoryDescriptor dr = iter.next();
			dr.shutdown();
		}
		fFactoryMap.clear();

		CProjectDescriptionManager.getInstance().removeCProjectDescriptionListener(this);
	}

	
	public static CExternalSettingsManager getInstance(){
		if(fInstance == null){
			fInstance = new CExternalSettingsManager();
		}
		return fInstance;
	}
	
	/**
	 * A simple class representing an external settings container.
	 * These are uniquely identifiable by the factoryId + factory
	 * specific container id
	 */
	public final static class CContainerRef {
		private final String fFactoryId;
		private final String fContainerId;
		
		public CContainerRef(String factoryId, String containerId){
			fFactoryId = factoryId;
			fContainerId = containerId;
		}

		public String getFactoryId() {
			return fFactoryId;
		}

		public String getContainerId() {
			return fContainerId;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			
			if(obj == null)
				return false;
			
			if(!(obj instanceof CContainerRef))
				return false;
			
			CContainerRef other = (CContainerRef)obj;
			
			if(!fContainerId.equals(other.fContainerId))
				return false;

			return fFactoryId.equals(other.fFactoryId);
		}

		@Override
		public int hashCode() {
			return fFactoryId.hashCode() + fContainerId.hashCode();
		}

		@Override
		public String toString() {
			return fFactoryId.toString() + " : " + fContainerId.toString(); //$NON-NLS-1$
		}
	}
	private static class ContainerDescriptor {
		private FactoryDescriptor fFactoryDr;
		private CExternalSettingsHolder fHolder;
		
		private CExternalSettingsContainer fContainer;

		private ContainerDescriptor(FactoryDescriptor factoryDr,
				String containerId, 
				IProject project,
				ICConfigurationDescription cfgDes,
				CExternalSetting[] previousSettings){
			fFactoryDr = factoryDr;
			try {
				fContainer = fFactoryDr.getFactory().createContainer(containerId, project, cfgDes, previousSettings);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
			if(fContainer == null)
				fContainer = NullContainer.INSTANCE;
		}
		
		public CExternalSetting[] getExternalSettings(){
			if(fHolder == null){
				fHolder = new CExternalSettingsHolder();
				fHolder.setExternalSettings(fContainer.getExternalSettings(), false);
			}
			return fHolder.getExternalSettings();
		}
		
	}

	/**
	 * A dummy SettingsContainer with 0 CExternalSettings
	 */
	static class NullContainer extends CExternalSettingsContainer {
		static final NullContainer INSTANCE = new NullContainer();
		
		@Override
		public CExternalSetting[] getExternalSettings() {
			return new CExternalSetting[0];
		}
	}
	
	private static class NullFactory extends CExternalSettingContainerFactory {
		static NullFactory INSTANCE = new NullFactory();

		@Override
		public CExternalSettingsContainer createContainer(String id,
				IProject project, ICConfigurationDescription cfgDes, CExternalSetting[] previousSettings) throws CoreException {
			return NullContainer.INSTANCE;
		}
	}
	
	private class FactoryDescriptor {
		private CExternalSettingContainerFactory fFactory;
		private String fId;
		
		private FactoryDescriptor(String id){
			fId = id;
		}
		
		private CExternalSettingContainerFactory getFactory(){
			if(fFactory == null){
				fFactory = createFactory(fId);
				fFactory.startup();
				fFactory.addListener(CExternalSettingsManager.this);
			}
			return fFactory;
		}
		
		private CExternalSettingContainerFactory createFactory(String id) {
			if(id.equals(CfgExportSettingContainerFactory.FACTORY_ID))
				return CfgExportSettingContainerFactory.getInstance();
			else if(id.equals(ExtensionContainerFactory.FACTORY_ID))
				return ExtensionContainerFactory.getInstance();
			return NullFactory.INSTANCE;
		}
		
		public void shutdown(){
			if(fFactory != null){
				fFactory.removeListener(CExternalSettingsManager.this);
				fFactory.shutdown();
				fFactory = null;
			}
		}
	}
	
	private interface ICfgContainer {
		ICConfigurationDescription getConfguration(boolean write);
	}
	
	private static class CfgContainer implements ICfgContainer {
		private ICConfigurationDescription fCfgDes;
		
		CfgContainer(ICConfigurationDescription cfgDes){
			fCfgDes = cfgDes;
		}
		
		public ICConfigurationDescription getConfguration(boolean write) {
			return fCfgDes;
		}
		
	}
	
	private class CfgContainerRefInfoContainer {
		private ICfgContainer fCfgContainer;
		private CSettingsRefInfo fRefInfo;
		private boolean fWriteWasRequested;
		
		CfgContainerRefInfoContainer(ICfgContainer container){
			fCfgContainer = container;
		}
		
		public CSettingsRefInfo getRefInfo(boolean write) {
			if(fRefInfo == null 
					|| (write && !fWriteWasRequested)){
				ICConfigurationDescription cfg = fCfgContainer.getConfguration(write);
				fRefInfo = CExternalSettingsManager.this.getRefInfo(cfg, write);
				fWriteWasRequested |= write;
			}
			return fRefInfo;
		}
	}
	
	private static class HolderContainer {
		private CfgContainerRefInfoContainer fRIContainer;
		private CRefSettingsHolder fHolder;
		private boolean fWriteWasRequested;
		private CContainerRef fCRef;
		
		HolderContainer(CfgContainerRefInfoContainer cr, CContainerRef cref){
			fRIContainer = cr;
			fCRef = cref;
		}
		
		CRefSettingsHolder getHolder(boolean write){
			if(fHolder == null 
					|| (write && !fWriteWasRequested)){
				CSettingsRefInfo ri = fRIContainer.getRefInfo(write);
				fHolder = ri.get(fCRef);
				fWriteWasRequested |= write;
			}
			return fHolder;
		}
		
		void setHolder(CRefSettingsHolder holder){
			fRIContainer.getRefInfo(true).put(holder);
			fWriteWasRequested = true;
			fHolder = holder;
		}
		
		void removeHolder(){
			fWriteWasRequested = true;
			fHolder = null;
			fRIContainer.getRefInfo(true).remove(fCRef);
		}
	}
	
	private static class CfgListCfgContainer implements ICfgContainer{
		private ProjDesCfgList fList;
		private int fNum;
		
		CfgListCfgContainer(ProjDesCfgList list, int num){
			fList = list;
			fNum = num;
		}
		
		public ICConfigurationDescription getConfguration(boolean write) {
			return fList.get(fNum, write);
		}
	}
	
	/**
	 * A simple container type that contains a Project Description & and associated list 
	 * of configuration descriptions.
	 */
	private static class ProjDesCfgList {
		private ICProjectDescription fProjDes;
		private List<ICConfigurationDescription> fCfgList = new ArrayList<ICConfigurationDescription>();
		
		public ProjDesCfgList(ICProjectDescription des, Set<String> idSet){
			fProjDes = des;
			ICConfigurationDescription[] cfgs = des.getConfigurations();
			for(int i = 0; i < cfgs.length; i++){
				if(idSet == null || idSet.contains(cfgs[i].getId()))
					fCfgList.add(cfgs[i]);
			}
		}

		public boolean isWritable(){
			return !fProjDes.isReadOnly();
		}
		
		public ICConfigurationDescription get(int num, boolean write) {
			if(write && fProjDes.isReadOnly()){
				makeWritable();
			}
			return fCfgList.get(num);
		}
		
		private void makeWritable(){
			ICProjectDescription writeDes = CProjectDescriptionManager.getInstance().getProjectDescription(fProjDes.getProject());
			fProjDes = writeDes;
			for(int i = 0; i < fCfgList.size(); i++){
				ICConfigurationDescription cfg = fCfgList.get(i);
				cfg = writeDes.getConfigurationById(cfg.getId());
				if(cfg != null)
					fCfgList.set(i, cfg);
				else
					fCfgList.remove(i);
			}
		}
		
		public int size() {
			return fCfgList.size();
		}
	}
	
	private FactoryDescriptor getFactoryDescriptor(String id){
		FactoryDescriptor dr = fFactoryMap.get(id);
		if(dr == null){
			dr = new FactoryDescriptor(id);
			fFactoryMap.put(id, dr);
		}
		return dr;
	}
	
	CExternalSettingContainerFactory getFactory(String id){
		FactoryDescriptor dr = getFactoryDescriptor(id);
		return dr.getFactory();
	}

	/**
	 * External settings call-back from the setting container factories
	 * to notify that settings have changed in a container.
	 * 
	 * Schedules a runnable to update any referencing projects
	 */
	public void settingsChanged(final IProject project, final String cfgId,	final CExternalSettingChangeEvent event) {
		// Modifying the project description in an asynchronous runnable is likely bad...
		// Unfortunately there's nothing else we can do as it's not safe to modify the referencing configurations in place
		IWorkspaceRunnable r = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				ProjDesCfgList[] lists = null;
				for (CExternalSettingsContainerChangeInfo info : event.getChangeInfos()) {
					switch(info.getEventType()){
					case CExternalSettingsContainerChangeInfo.CHANGED:
						int flags = info.getChangeFlags();
						if((flags & CExternalSettingsContainerChangeInfo.CONTAINER_CONTENTS) != 0){
							if(lists == null)
								// Potentially all configuration in all projects need to be considered for be
								lists = createCfgListsForEvent(project, cfgId);
							for (ProjDesCfgList list : lists) {
								for(int i = 0; i < list.size(); i++){
									CfgListCfgContainer cr = new CfgListCfgContainer(list, i);
									processContainerChange(OP_CHANGED, cr, new CfgContainerRefInfoContainer(cr), info.getContainerInfo());
								}
							}
						}
						break;
					}
				}
				if (lists != null) {
					final List<ICProjectDescription> list = getModifiedProjDesList(lists);
					if(list.size() != 0) {
						for(int i = 0; i < list.size(); i++) {
							ICProjectDescription des = list.get(i);
							CProjectDescriptionManager.getInstance().setProjectDescription(des.getProject(), des, false, monitor);
						}
					}
				}
			}
		};
		CProjectDescriptionManager.runWspModification(r, new NullProgressMonitor());
	}
	
	private List<ICProjectDescription> getModifiedProjDesList(ProjDesCfgList[] lists){
		List<ICProjectDescription> list = new ArrayList<ICProjectDescription>();
		for(int i = 0; i < lists.length; i++){
			if(lists[i].isWritable())
				list.add(lists[i].fProjDes);
		}
		return list;
	}

	/**
	 * Returns an array of ProjDescCfgList corresponding to the passed in project + cfgId
	 * @param project project, or null
	 * @param cfgId configuration ID, or null
	 * @return ProjDescCfgList[]
	 */
	private ProjDesCfgList[] createCfgListsForEvent(IProject project, String cfgId){
		ProjDesCfgList lists[];
		Set<String> set = null;
		if(project != null) {
			if(cfgId != null){
				set = new HashSet<String>();
				set.add(cfgId);
			}
			ProjDesCfgList l = createCfgList(project, set);
			if(l != null)
				lists = new ProjDesCfgList[] { l };
			else
				lists = new ProjDesCfgList[0];
		} else {
			// Project is null -- add all CDT projects & configs in the workspace
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			List<ProjDesCfgList> list = new ArrayList<ProjDesCfgList>();
			for (IProject p : projects){
				ProjDesCfgList l = createCfgList(p, set);
				if(l != null)
					list.add(l);
			}
			lists = list.toArray(new ProjDesCfgList[list.size()]);
		}
		return lists;
	}

	private ProjDesCfgList createCfgList(IProject project, Set<String> cfgIdSet){
		ICProjectDescription des = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		if(des == null)
			return null;

		return new ProjDesCfgList(des, cfgIdSet);
	}
	
	private boolean processContainerChange(int op, 
		ICfgContainer cr, 
		CfgContainerRefInfoContainer riContainer, 
		CContainerRef crInfo){

		ICConfigurationDescription cfg = cr.getConfguration(false);

		ExtSettingsDelta[] deltas = checkExternalSettingsChange(op, 
				cfg.getProjectDescription().getProject(), cfg, riContainer, crInfo);
		
		if(deltas != null)
			return CExternalSettingsDeltaProcessor.applyDelta(cr.getConfguration(true), deltas);
		return false;
	}
	
	private static class RefInfoContainer{
		CSettingsRefInfo fRefInfo;
		int fInstanceId;
		
		RefInfoContainer(CSettingsRefInfo ri, int id){
			fRefInfo = ri;
			fInstanceId = id;
		}
	}
	
	private CSettingsRefInfo getRefInfo(ICConfigurationDescription cfg, boolean write){
		if(write && cfg.isReadOnly())
			throw new IllegalArgumentException(SettingsModelMessages.getString("CExternalSettingsManager.3")); //$NON-NLS-1$
		
		RefInfoContainer cr = (RefInfoContainer)cfg.getSessionProperty(EXTERNAL_SETTING_PROPERTY);
		CSettingsRefInfo ri;
		boolean setCr = false;
		if(cr == null){
			ri = load(cfg);
			if(ri == null)
				ri = new CSettingsRefInfo();
			setCr = true;
		} else if (write && cr.fInstanceId != cfg.hashCode()){
			ri = new CSettingsRefInfo(cr.fRefInfo);
			setCr = true;
		} else {
			ri = cr.fRefInfo;
			setCr = false;
		}
		
		if(setCr){
			cr = new RefInfoContainer(ri, cfg.hashCode());
			cfg.setSessionProperty(EXTERNAL_SETTING_PROPERTY, cr);
		}
		
		return ri;
	}
	
	private CSettingsRefInfo load(ICConfigurationDescription cfg){
		try {
			ICStorageElement el = cfg.getStorage(EXTERNAL_SETTING_STORAGE_ID, false);
			if(el != null){
				CSettingsRefInfo ri = new CSettingsRefInfo(el);
				return ri;
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Respond to Project Description events.
	 *  - DATA_APPLIED: Data has been applied, and the description is still
	 *                  writable, store cached external settings into the configuration
	 *  - LOADED: Check whether a reconcile is needed and update the settings atomically
	 */
	public void handleEvent(CProjectDescriptionEvent event) {
		switch(event.getEventType()){
		case CProjectDescriptionEvent.DATA_APPLIED: {
			ICProjectDescription des = event.getNewCProjectDescription();
			if(des == null)
				return;
			
			ICConfigurationDescription[] cfgs = des.getConfigurations();
			for(int i = 0; i < cfgs.length; i++){
				ICConfigurationDescription cfg = cfgs[i];
				RefInfoContainer cr = (RefInfoContainer)cfg.getSessionProperty(EXTERNAL_SETTING_PROPERTY);
				if(cr != null/* && cr.fInstanceId != cfg.hashCode()*/){
					store(cfg, cr.fRefInfo);
				}
			}
			break;
		}
		case CProjectDescriptionEvent.LOADED:
			// Note using an asynchronous get / set here is bad.
			// Unfortunately there's no other way to make this work without re-writing the project model to allow
			// us to reconcile / update the cached configuration during load
			final IProject project = event.getProject();
			IWorkspaceRunnable r = new IWorkspaceRunnable(){
				public void run(IProgressMonitor monitor) throws CoreException {
					if (!project.isAccessible())
						return;
					ProjDesCfgList list = new ProjDesCfgList(CoreModel.getDefault().getProjectDescription(project), null);
					boolean changed = false;
					for(int i = 0; i < list.size(); i++){
						CfgListCfgContainer cfgCr = new CfgListCfgContainer(list, i);
						CfgContainerRefInfoContainer ric = new CfgContainerRefInfoContainer(cfgCr);
						CContainerRef[] refs = ric.getRefInfo(false).getReferences();
						for(int k = 0; k < refs.length; k++) {
							if(processContainerChange(OP_CHANGED, cfgCr, new CfgContainerRefInfoContainer(cfgCr), refs[k]))
								changed = true;
						}
					}
					if (changed)
						CProjectDescriptionManager.getInstance().setProjectDescription(project, list.fProjDes);
				}
			};
			CProjectDescriptionManager.runWspModification(r, null);
			break;
		}
	}

	private void store(ICConfigurationDescription cfg, CSettingsRefInfo ri){
		try {
			ICStorageElement el = cfg.getStorage(EXTERNAL_SETTING_STORAGE_ID, true);
			el.clear();
			ri.serialize(el);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
	public void containerContentsChanged(ICConfigurationDescription cfg, CContainerRef cr){
		CfgContainer ccr = new CfgContainer(cfg);
		processContainerChange(OP_CHANGED, ccr, new CfgContainerRefInfoContainer(ccr), cr);
	}
	
	public void addContainer(ICConfigurationDescription cfg, CContainerRef cr){
		CfgContainer ccr = new CfgContainer(cfg);
		processContainerChange(OP_ADDED, ccr, new CfgContainerRefInfoContainer(ccr), cr);
	}

	public void removeContainer(ICConfigurationDescription cfg, CContainerRef cr){
		CfgContainer ccr = new CfgContainer(cfg);
		processContainerChange(OP_REMOVED, ccr, new CfgContainerRefInfoContainer(ccr), cr);
	}
	
	public CContainerRef[] getReferences(ICConfigurationDescription cfg, String factoryId){
		CSettingsRefInfo info = getRefInfo(cfg, false);
		return info.getReferences(factoryId);
	}
	
	private ExtSettingsDelta[] checkExternalSettingsChange(int op, 
			IProject proj, 
			ICConfigurationDescription cfgDes, 
			CfgContainerRefInfoContainer riContainer, 
			CContainerRef cr){
		HolderContainer hCr = new HolderContainer(riContainer, cr);
		CRefSettingsHolder holder = hCr.getHolder(false);
		if(holder == null && op == OP_ADDED){
			holder = new CRefSettingsHolder(cr);
			hCr.setHolder(holder);
		}

		if(holder == null)
			return null;

		CExternalSetting[] newSettings = null;
		CExternalSetting[] oldSettings = hCr.getHolder(false).getExternalSettings();
		if (op != OP_REMOVED) {
			FactoryDescriptor dr = getFactoryDescriptor(cr.getFactoryId());
			ContainerDescriptor cdr = new ContainerDescriptor(dr, cr.getContainerId(), proj, cfgDes, oldSettings);
			newSettings = cdr.getExternalSettings();
		}

		ExtSettingsDelta[] deltas = CExternalSettinsDeltaCalculator.getInstance().getSettingChange(newSettings, oldSettings);
		if(deltas != null) {
			CRefSettingsHolder holder1 = hCr.getHolder(true);
			holder1.setExternalSettings(newSettings, false);
			holder1.setReconsiled(true);
		}

		if(op == OP_REMOVED)
			hCr.removeHolder();
		return deltas;
	}
	
	public void restoreSourceEntryDefaults(ICConfigurationDescription cfg){
		CfgContainer cr = new CfgContainer(cfg);
		CfgContainerRefInfoContainer ric = new CfgContainerRefInfoContainer(cr);
		CExternalSetting[] settings = ric.getRefInfo(false).createExternalSettings();
		ExtSettingsDelta[] deltas = CExternalSettinsDeltaCalculator.getInstance().getSettingChange(settings, null);
		if(deltas != null){
			CExternalSettingsDeltaProcessor.applySourceEntriesChange(cfg, deltas);
		}
	}

	public void restoreOutputEntryDefaults(ICConfigurationDescription cfg){
		CfgContainer cr = new CfgContainer(cfg);
		CfgContainerRefInfoContainer ric = new CfgContainerRefInfoContainer(cr);
		CExternalSetting[] settings = ric.getRefInfo(false).createExternalSettings();
		ExtSettingsDelta[] deltas = CExternalSettinsDeltaCalculator.getInstance().getSettingChange(settings, null);
		if(deltas != null){
			CExternalSettingsDeltaProcessor.applyOutputEntriesChange(cfg, deltas);
		}
	}

	public void restoreDefaults(ICLanguageSetting ls, int entryKinds){
		ICConfigurationDescription cfg = ls.getConfiguration();
		CfgContainer cr = new CfgContainer(cfg);
		CfgContainerRefInfoContainer ric = new CfgContainerRefInfoContainer(cr);
		CExternalSetting[] settings = ric.getRefInfo(false).createExternalSettings();
		ExtSettingsDelta[] deltas = CExternalSettinsDeltaCalculator.getInstance().getSettingChange(settings, null);
		if(deltas != null){
			CExternalSettingsDeltaProcessor.applyDelta(ls, deltas, entryKinds);
		}
	}
}
