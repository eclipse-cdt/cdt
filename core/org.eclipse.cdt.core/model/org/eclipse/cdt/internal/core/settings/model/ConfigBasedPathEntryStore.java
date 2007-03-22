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
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator;
import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator.PathEntryCollector;
import org.eclipse.cdt.core.settings.model.util.PathEntryTranslator.ReferenceSettingsInfo;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.PathEntryManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

public class ConfigBasedPathEntryStore implements IPathEntryStore, ICProjectDescriptionListener {
	private List fListeners;
	private IProject fProject;
	static final QualifiedName PATH_ENTRY_COLLECTOR_PROPERTY_NAME = new QualifiedName(CCorePlugin.PLUGIN_ID, "PathEntryStoreCollector");	//$NON-NLS-1$
	
	/**
	 * 
	 */
	public ConfigBasedPathEntryStore(IProject project) {
		fProject = project;
		fListeners = Collections.synchronizedList(new ArrayList());
		
		CProjectDescriptionManager.getInstance().addListener(this, CProjectDescriptionEvent.APPLIED | CProjectDescriptionEvent.LOADDED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#addPathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	public void addPathEntryStoreListener(IPathEntryStoreListener listener) {		
		fListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#removePathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	public void removePathEntryStoreListener(IPathEntryStoreListener listener) {
		fListeners.remove(listener);
	}

	private void fireContentChangedEvent(IProject project) {
		PathEntryStoreChangedEvent evt = new PathEntryStoreChangedEvent(this, project, PathEntryStoreChangedEvent.CONTENT_CHANGED);
		IPathEntryStoreListener[] observers = new IPathEntryStoreListener[fListeners.size()];
		fListeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].pathEntryStoreChanged(evt);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#fireClosedChangedEvent(IProject)
	 */
	public void close() {
		PathEntryStoreChangedEvent evt = new PathEntryStoreChangedEvent(this, fProject, PathEntryStoreChangedEvent.STORE_CLOSED);
		IPathEntryStoreListener[] observers = new IPathEntryStoreListener[fListeners.size()];
		fListeners.toArray(observers);
		for (int i = 0; i < observers.length; i++) {
			observers[i].pathEntryStoreChanged(evt);
		}
		CProjectDescriptionManager.getInstance().removeListener(this);
	}

	public IProject getProject() {
		return fProject;
	}

	public ICExtensionReference getExtensionReference() {
		return null;
	}

	public IPathEntry[] getRawPathEntries() throws CoreException {
		ICConfigurationDescription cfg = getIndexCfg(fProject);
		PathEntryCollector cr = getCollector(fProject, cfg);
		if(cr != null){
			List list = cr.getEntries(null, PathEntryTranslator.INCLUDE_USER, cfg);
			list.add(CoreModel.newContainerEntry(ConfigBasedPathEntryContainer.CONTAINER_PATH));
			return (IPathEntry[])list.toArray(new IPathEntry[list.size()]);
		}
		return new IPathEntry[0];
	}

	public void setRawPathEntries(IPathEntry[] entries) throws CoreException {
		ICConfigurationDescription cfg = getIndexCfg(fProject);
		PathEntryCollector cr = getCollector(fProject, cfg);
		if(cr != null){
			List sysList = cr.getEntries(null, PathEntryTranslator.INCLUDE_BUILT_INS, cfg);
			List usrList = new ArrayList(entries.length);
			for(int i = 0; i < entries.length; i++){
				if(entries[i].getEntryKind() != IPathEntry.CDT_CONTAINER)
					usrList.add(entries[i]);
			}
			
			CProjectDescription des = (CProjectDescription)CoreModel.getDefault().getProjectDescription(fProject, true);
			ICConfigurationDescription cfgDes = des.getIndexConfiguration();
			CConfigurationData data = cfgDes.getConfigurationData();
			PathEntryTranslator tr = new PathEntryTranslator(fProject, data);
			IPathEntry[] usrEntries = (IPathEntry[])usrList.toArray(new IPathEntry[usrList.size()]);
			IPathEntry[] sysEntries = (IPathEntry[])sysList.toArray(new IPathEntry[sysList.size()]);
			ReferenceSettingsInfo rInfo = tr.applyPathEntries(usrEntries, sysEntries, PathEntryTranslator.OP_REPLACE);
			cfgDes.removeExternalSettings();
			ICExternalSetting extSettings[] = rInfo.getExternalSettings();
			for(int i = 0; i < extSettings.length; i++){
				ICExternalSetting setting = extSettings[i];
				cfgDes.createExternalSetting(setting.getCompatibleLanguageIds(), 
						setting.getCompatibleContentTypeIds(),
						setting.getCompatibleExtensions(), 
						setting.getEntries());
			}
			Map refMap = rInfo.getRefProjectsMap();
			cfgDes.setReferenceInfo(refMap);
			
			CoreModel.getDefault().setProjectDescription(fProject, des);
		}
	}

	public void handleEvent(CProjectDescriptionEvent event) {
		IProject project = event.getProject();
		if(!fProject.equals(project))
			return;
		
		switch(event.getEventType()){
			case CProjectDescriptionEvent.APPLIED:{
				CProjectDescription des = (CProjectDescription)event.getNewCProjectDescription();
				CProjectDescription oldDes = (CProjectDescription)event.getOldCProjectDescription();
				IPathEntry oldCrEntries[] = null;
				if(oldDes != null){
					ICConfigurationDescription oldIndexCfg = oldDes.getIndexConfiguration();
					PathEntryCollector oldCr = getCachedCollector(oldIndexCfg);
					if(oldCr != null)
						oldCrEntries = oldCr.getEntries(PathEntryTranslator.INCLUDE_BUILT_INS, oldIndexCfg);
				}
				if(des != null){
					//TODO: smart delta handling
					ICConfigurationDescription[] cfgDess = des.getConfigurations();
					for(int i = 0; i < cfgDess.length; i++){
						setCachedCollector(cfgDess[i], null);
					}
					
					if(oldCrEntries != null){
						ICConfigurationDescription newIndexCfg = des.getIndexConfiguration();
						PathEntryCollector newCr = getCollector(fProject, newIndexCfg);
						IPathEntry[] newCrEntries = newCr.getEntries(PathEntryTranslator.INCLUDE_BUILT_INS, newIndexCfg);
						if(!Arrays.equals(oldCrEntries, newCrEntries)){
							CModelManager manager = CModelManager.getDefault();
							ICProject cproject = manager.create(project);
							

				//			ConfigBasedPathEntryContainer newContainer = createContainer(des);
							try {
								PathEntryManager.getDefault().clearPathEntryContainer(new ICProject[]{cproject}, ConfigBasedPathEntryContainer.CONTAINER_PATH, new NullProgressMonitor());
							} catch (CModelException e) {
								CCorePlugin.log(e);
							}
						}
					}
				}
				fireContentChangedEvent(fProject);
				break;
			}
		}
	}
	
//	private static PathEntryCollector getCollector(ICProjectDescription des){
//		ICConfigurationDescription cfgDes = ((CProjectDescription)des).getIndexConfiguration();
//		if(cfgDes != null){
//			PathEntryCollector cr = getCachedCollector(cfgDes);
//			if(cr == null){
//				cr = PathEntryTranslator.collectEntries(des.getProject(), cfgDes);
//				setCachedCollector(cfgDes, cr);
//			}
//			return cr;
//		}
//		return null;
//	}

	private static PathEntryCollector getCollector(IProject project, ICConfigurationDescription cfgDes){
		if(cfgDes != null){
			PathEntryCollector cr = getCachedCollector(cfgDes);
			if(cr == null){
				cr = PathEntryTranslator.collectEntries(project, cfgDes);
				setCachedCollector(cfgDes, cr);
			}
			return cr;
		}
		return null;
	}

	private static PathEntryCollector getCachedCollector(ICConfigurationDescription cfgDes){
		return (PathEntryCollector)cfgDes.getSessionProperty(PATH_ENTRY_COLLECTOR_PROPERTY_NAME);
	}

	private static void setCachedCollector(ICConfigurationDescription cfgDes, PathEntryCollector cr){
		cfgDes.setSessionProperty(PATH_ENTRY_COLLECTOR_PROPERTY_NAME, cr);
	}

//	public static PathEntryCollector getCollector(IProject project){
//		ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project, false);
//		if(des != null)
//			return getCollector(des);
//		return null;
//	}
	
	private static ICConfigurationDescription getIndexCfg(IProject project){
		CProjectDescription des = (CProjectDescription)CCorePlugin.getDefault().getProjectDescription(project, false);
		return des.getIndexConfiguration();
	}
	
	public static IPathEntry[] getContainerEntries(IProject project){
		ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project, false);
		if(des != null)
			return getContainerEntries(des);
		return new IPathEntry[0];
	}

	public static IPathEntry[] getContainerEntries(ICProjectDescription des){
		ICConfigurationDescription cfg = ((CProjectDescription)des).getIndexConfiguration();
		PathEntryCollector cr = getCollector(des.getProject(), cfg);
		if(cr != null)
			return cr.getEntries(PathEntryTranslator.INCLUDE_BUILT_INS, cfg);
		return new IPathEntry[0];
	}

	public static ConfigBasedPathEntryContainer createContainer(IProject project){
		IPathEntry[] entries = getContainerEntries(project);
		return new ConfigBasedPathEntryContainer(entries);
	}

	public static ConfigBasedPathEntryContainer createContainer(ICProjectDescription des){
		IPathEntry[] entries = getContainerEntries(des);
		return new ConfigBasedPathEntryContainer(entries);
	}
}
