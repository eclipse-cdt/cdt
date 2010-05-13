/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
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
	
	public static class SettingsUpdateStatus {
		ICProjectDescription fDes;
		boolean fIsChanged;
		
		SettingsUpdateStatus(ICProjectDescription des, boolean isChanged){
			fDes = des;
			fIsChanged = isChanged;
		}

		public ICProjectDescription getCProjectDescription(){
			return fDes;
		}
		
		public boolean isChanged(){
			return fIsChanged;
		}
	}
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
	
	public final static class CContainerRef {
		private String fFactoryId;
		private String fContainerId;
		
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
//		private String fContainerId;
//		private String fProjectName;
//		private String fCfgId;
		private CExternalSettingsHolder fHolder;
		
		private CExternalSettingsContainer fContainer;

		private ContainerDescriptor(FactoryDescriptor factoryDr,
				String containerId, 
				IProject project,
				ICConfigurationDescription cfgDes,
				CExternalSetting[] previousSettings){
			fFactoryDr = factoryDr;
//			fContainerId = containerId;
//			fProjectName = project.getName();
//			fCfgId = cfgDes.getId();
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
				fHolder.setExternallSettings(fContainer.getExternalSettings());
			}
			return fHolder.getExternalSettings();
		}
		
//		public CExternalSettingsContainer getContainer(){
//			if(fContainer == null){
//				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(fProjectName);
//				try {
//					fContainer = fFactoryDr.getFactory().createContainer(fContainerId, project, fCfgId);
//				} catch (CoreException e) {
//					CCorePlugin.log(e);
//				}
//				if(fContainer == null)
//					fContainer = NullContainer.INSTANCE;
//			}
//			return fContainer;
//		}
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
//		private Map fContainerMap;
		
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
		
//		public String getId(){
//			return fId;
//		}
		
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
		
//		boolean isWritable();
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
	
	private interface ICRefInfoContainer {
		CSettingsRefInfo getRefInfo(boolean write);
	}
	
	private class CfgContainerRefInfoContainer implements ICRefInfoContainer{
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
	
//	private class CfgRefInfoContainer implements ICRefInfoContainer{
//		private CSettingsRefInfo fRefInfo;
//		private ICConfigurationDescription fCfgDes;
//		private boolean fWriteWasRequested;
//		
//		CfgRefInfoContainer(ICConfigurationDescription cfg){
//			fCfgDes = cfg;
//		}
//
//		public CSettingsRefInfo getRefInfo(boolean write) {
//			if(fRefInfo == null 
//					|| (write && !fWriteWasRequested)){
//				ICConfigurationDescription cfg = fCfgDes;
//				fRefInfo = CExternalSettingsManager.this.getRefInfo(cfg, write);
//				fWriteWasRequested |= write;
//			}
//			return fRefInfo;
//		}
//	}
	
	private class HolderContainer {
		private ICRefInfoContainer fRIContainer;
		private CRefSettingsHolder fHolder;
		private boolean fWriteWasRequested;
		private CContainerRef fCRef;
		
		HolderContainer(ICRefInfoContainer cr, CContainerRef cref){
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
		private ICfgList fList;
		private int fNum;
		
		CfgListCfgContainer(ICfgList list, int num){
			fList = list;
			fNum = num;
		}
		
		public ICConfigurationDescription getConfguration(boolean write) {
			return fList.get(fNum, write);
		}

//		public boolean isWritable() {
//			return !getConfguration(false).isReadOnly();
//		}
	}
	
	private interface ICfgList {
		ICConfigurationDescription get(int num, boolean write);

		int size();
	}
	
	private static class ProjDesCfgList implements ICfgList{
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
		
		public int getNumForId(String id){
			for(int i = 0; i < fCfgList.size(); i++){
				ICConfigurationDescription cfg = fCfgList.get(i);
				if(id.equals(cfg.getId()))
					return i;
			}
			return -1;
		}

//		public ICConfigurationDescription getConfigurationById(String id, boolean write){
//			ICConfigurationDescription cfg = fProjDes.getConfigurationById(id);
//			if(cfg == null)
//				return null;
//			if(write && fProjDes.isReadOnly()){
//				makeWritable();
//				cfg = fProjDes.getConfigurationById(id);
//			}
//			return cfg;
//		}

		public int size() {
			return fCfgList.size();
		}
	}
	
	private static class DeltaInfo{
//		private boolean fCalculated;
//		private ExtSettingsDelta[] fDeltas;
		
//		void setDelta(ExtSettingsDelta[] deltas){
//			fDeltas = deltas;
////			fCalculated = true;
//		}
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
	
	private ContainerDescriptor createDescriptor(String factoryId,
			String containerId,
			IProject project,
			ICConfigurationDescription cfgDes,
			CExternalSetting[] previousSettings) {
		FactoryDescriptor dr = getFactoryDescriptor(factoryId);
		return new ContainerDescriptor(dr, containerId, project, cfgDes, previousSettings);
	}

	public void settingsChanged(IProject project, String cfgId,
			CExternalSettingChangeEvent event) {
		ProjDesCfgList[] lists = null;
		CExternalSettingsContainerChangeInfo[] infos = event.getChangeInfos();
		for(int i = 0; i < infos.length; i++){
			CExternalSettingsContainerChangeInfo info = infos[i];
			switch(info.getEventType()){
			case CExternalSettingsContainerChangeInfo.CHANGED:
				int flags = info.getChangeFlags();
				if((flags & CExternalSettingsContainerChangeInfo.CONTAINER_CONTENTS) != 0){
					if(lists == null)
						lists = createCfgListsForEvent(project, cfgId);
					containerContentsChanged(lists, info.getContainerInfo(), null);
				}
				break;
			}
			
		}
		
		if(lists != null)
			applyLists(lists);
	}
	
	private void applyLists(ProjDesCfgList[] lists){
		final List<ICProjectDescription> list = getModifiedProjDesList(lists);
		if(list.size() != 0){
			IWorkspaceRunnable r = new IWorkspaceRunnable(){

				public void run(IProgressMonitor monitor) throws CoreException {
					for(int i = 0; i < list.size(); i++){
						ICProjectDescription des = list.get(i);
						CProjectDescriptionManager.getInstance().setProjectDescription(des.getProject(), des, false, monitor);
					}
				}
				
			};

			CProjectDescriptionManager.runWspModification(r, new NullProgressMonitor());
		}
	}
	
	private List<ICProjectDescription> getModifiedProjDesList(ProjDesCfgList[] lists){
		List<ICProjectDescription> list = new ArrayList<ICProjectDescription>();
		for(int i = 0; i < lists.length; i++){
			if(lists[i].isWritable())
				list.add(lists[i].fProjDes);
		}
		return list;
	}
	
	private void containerContentsChanged(ProjDesCfgList[] lists, CContainerRef ref, DeltaInfo deltaInfo){
		for(int i = 0; i < lists.length; i++){
			containerContentsChanged(lists[i], null, ref, deltaInfo);
		}
	}

	private ProjDesCfgList[] createCfgListsForEvent(IProject project, String cfgId){
		ProjDesCfgList lists[];
		if(project != null){
			ProjDesCfgList l = createCfgList(project, cfgId);
			if(l != null){
				lists = new ProjDesCfgList[1];
				lists[0] = l;
			} else {
				lists = new ProjDesCfgList[0];
			}
		} else {
			lists = createCfgLists();
		}
		return lists;
	}

	private ProjDesCfgList[] createCfgLists(){
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<ProjDesCfgList> list = new ArrayList<ProjDesCfgList>();
		for(int i = 0; i < projects.length; i++){
			ProjDesCfgList l = createCfgList(projects[i], (Set<String>)null);
			if(l != null)
				list.add(l);
		}
		return list.toArray(new ProjDesCfgList[list.size()]);
	}

	private ProjDesCfgList createCfgList(IProject project, String cfgId){
		Set<String> set = null;
		if(cfgId != null){
			set = new HashSet<String>();
			set.add(cfgId);
		}
		
		return createCfgList(project, set);
	}

	private ProjDesCfgList createCfgList(IProject project, Set<String> cfgIdSet){
		ICProjectDescription des = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		if(des == null)
			return null;

		return new ProjDesCfgList(des, cfgIdSet);
	}
	
	private void containerContentsChanged(ProjDesCfgList list, String[]cfgIds, CContainerRef ref, DeltaInfo deltaInfo){
		if(cfgIds != null && cfgIds.length != 0){
			for(int i = 0; i < cfgIds.length; i++){
				int num = list.getNumForId(cfgIds[i]);
				if(num >= 0){
					CfgListCfgContainer cr = new CfgListCfgContainer(list, num);
					containerContentsChanged(cr, ref, deltaInfo);
				}
			}
		} else {
			for(int i = 0; i < list.size(); i++){
				CfgListCfgContainer cr = new CfgListCfgContainer(list, i);
				containerContentsChanged(cr, ref, deltaInfo);
			}
		}
	}
	
	
	private boolean containerContentsChanged(ICfgContainer cr, CContainerRef ref, DeltaInfo deltaInfo){
		return processContainerChange(OP_CHANGED, cr, ref, deltaInfo);
	}

	private boolean processContainerChange(int op, 
			ICfgContainer cr, 
			CContainerRef crInfo,
			DeltaInfo deltaInfo){
		return processContainerChange(op, cr, new CfgContainerRefInfoContainer(cr), crInfo, deltaInfo);
	}

	private boolean processContainerChange(int op, 
		ICfgContainer cr, 
		ICRefInfoContainer riContainer, 
		CContainerRef crInfo,
		DeltaInfo deltaInfo){

		ICConfigurationDescription cfg = cr.getConfguration(false);

		ExtSettingsDelta[] deltas = checkExternalSettingsChange(op, 
				cfg.getProjectDescription().getProject(), cfg, riContainer, crInfo);
		
		if(deltas != null){
			return applyDeltas(cr, deltas);
		}
		return false;
	}
	
	private boolean applyDeltas(ICfgContainer cr, ExtSettingsDelta[] deltas){
		return CExternalSettingsDeltaProcessor.applyDelta(cr.getConfguration(true), deltas);
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

	public void handleEvent(CProjectDescriptionEvent event) {
		switch(event.getEventType()){
		case CProjectDescriptionEvent.DATA_APPLIED:
			checkStore(event.getNewCProjectDescription());
			break;
		case CProjectDescriptionEvent.LOADED:
			final SettingsUpdateStatus status = update(event.getNewCProjectDescription());
			if(status.isChanged()){
				IWorkspaceRunnable r = new IWorkspaceRunnable(){

					public void run(IProgressMonitor monitor) throws CoreException {
						ICProjectDescription des = status.getCProjectDescription();
						CProjectDescriptionManager.getInstance().setProjectDescription(des.getProject(), des);
					}
					
				};
				CProjectDescriptionManager.runWspModification(r, null);
			}
			break;
		}
	}

	private void checkStore(ICProjectDescription des){
		if(des == null)
			return;
		
		ICConfigurationDescription[] cfgs = des.getConfigurations();
		for(int i = 0; i < cfgs.length; i++){
			checkStore(cfgs[i]);
		}
	}

	private void checkStore(ICConfigurationDescription cfg){
		RefInfoContainer cr = (RefInfoContainer)cfg.getSessionProperty(EXTERNAL_SETTING_PROPERTY);
		if(cr != null/* && cr.fInstanceId != cfg.hashCode()*/){
			store(cfg, cr.fRefInfo);
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
		containerContentsChanged(ccr, cr, null);
	}
	
	public void addContainer(ICConfigurationDescription cfg, CContainerRef cr){
		CfgContainer ccr = new CfgContainer(cfg);
		processContainerChange(OP_ADDED, ccr, cr, null);
	}

	public void removeContainer(ICConfigurationDescription cfg, CContainerRef cr){
		CfgContainer ccr = new CfgContainer(cfg);
		processContainerChange(OP_REMOVED, ccr, cr, null);
	}
	
	public CContainerRef[] getReferences(ICConfigurationDescription cfg, String factoryId){
		CSettingsRefInfo info = getRefInfo(cfg, false);
		return info.getReferences(factoryId);
	}
	
	public SettingsUpdateStatus update(ICProjectDescription des){
		ProjDesCfgList list = new ProjDesCfgList(des, null);
		boolean changed = false;
		for(int i = 0; i < list.size(); i++){
			CfgListCfgContainer cfgCr = new CfgListCfgContainer(list, i);
			CfgContainerRefInfoContainer ric = new CfgContainerRefInfoContainer(cfgCr);
			CContainerRef[] refs = ric.getRefInfo(false).getReferences();
			for(int k = 0; k < refs.length; k++) {
				if(containerContentsChanged(cfgCr, refs[k], null))
					changed = true;
			}
		}
		return new SettingsUpdateStatus(list.fProjDes, changed);
	}

	private ExtSettingsDelta[] checkExternalSettingsChange(int op, 
			IProject proj, 
			ICConfigurationDescription cfgDes, 
			ICRefInfoContainer riContainer, 
			CContainerRef cr){
		HolderContainer hCr = new HolderContainer(riContainer, cr);
		CRefSettingsHolder holder = hCr.getHolder(false);
		if(holder == null && op == OP_ADDED){
			holder = new CRefSettingsHolder(cr);
			hCr.setHolder(holder);
		}
		
		if(holder == null)
			return null;
		
		ExtSettingsDelta[] deltas = reconsile(proj, cfgDes, op != OP_REMOVED, hCr, cr);
		
		if(op == OP_REMOVED)
			hCr.removeHolder();
		return deltas;
	}
	
	private ExtSettingsDelta[] reconsile(IProject proj, ICConfigurationDescription cfgDes, boolean addOrChange, HolderContainer hCr, CContainerRef cr){
//		if(holder.isReconsiled())
//			return;
		CExternalSetting[] newSettings = null;
		CExternalSetting[] oldSettings = hCr.getHolder(false).getExternalSettings();
		if (addOrChange) {
			ContainerDescriptor cdr = createDescriptor(cr.getFactoryId(), cr.getContainerId(), proj, cfgDes, oldSettings);
			newSettings = cdr.getExternalSettings();
		}
		
		ExtSettingsDelta[] deltas = getDeltaCalculator().getSettingChange(newSettings, oldSettings);
		if(deltas != null){
			CRefSettingsHolder holder = hCr.getHolder(true);
			holder.setExternallSettings(newSettings);
			holder.setReconsiled(true);
		}
		return deltas;
	}
	
	private CExternalSettinsDeltaCalculator getDeltaCalculator(){
		return CExternalSettinsDeltaCalculator.getInstance();
	}

	public void restoreSourceEntryDefaults(ICConfigurationDescription cfg){
		CfgContainer cr = new CfgContainer(cfg);
		CfgContainerRefInfoContainer ric = new CfgContainerRefInfoContainer(cr);
		CExternalSetting[] settings = ric.getRefInfo(false).createExternalSettings();
		ExtSettingsDelta[] deltas = getDeltaCalculator().getSettingChange(settings, null);
		if(deltas != null){
			CExternalSettingsDeltaProcessor.applySourceEntriesChange(cfg, deltas);
		}
	}

	public void restoreOutputEntryDefaults(ICConfigurationDescription cfg){
		CfgContainer cr = new CfgContainer(cfg);
		CfgContainerRefInfoContainer ric = new CfgContainerRefInfoContainer(cr);
		CExternalSetting[] settings = ric.getRefInfo(false).createExternalSettings();
		ExtSettingsDelta[] deltas = getDeltaCalculator().getSettingChange(settings, null);
		if(deltas != null){
			CExternalSettingsDeltaProcessor.applyOutputEntriesChange(cfg, deltas);
		}
	}

	public void restoreDefaults(ICLanguageSetting ls, int entryKinds){
		ICConfigurationDescription cfg = ls.getConfiguration();
		CfgContainer cr = new CfgContainer(cfg);
		CfgContainerRefInfoContainer ric = new CfgContainerRefInfoContainer(cr);
		CExternalSetting[] settings = ric.getRefInfo(false).createExternalSettings();
		ExtSettingsDelta[] deltas = getDeltaCalculator().getSettingChange(settings, null);
		if(deltas != null){
			CExternalSettingsDeltaProcessor.applyDelta(ls, deltas, entryKinds);
		}
	}
}
