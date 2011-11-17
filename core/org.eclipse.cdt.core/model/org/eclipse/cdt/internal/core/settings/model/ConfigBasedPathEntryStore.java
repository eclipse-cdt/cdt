/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
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
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
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
	private List<IPathEntryStoreListener> fListeners;
	private IProject fProject;
	static final QualifiedName PATH_ENTRY_COLLECTOR_PROPERTY_NAME = new QualifiedName(CCorePlugin.PLUGIN_ID, "PathEntryStoreCollector");	//$NON-NLS-1$

	/**
	 *
	 */
	public ConfigBasedPathEntryStore(IProject project) {
		fProject = project;
		fListeners = Collections.synchronizedList(new ArrayList<IPathEntryStoreListener>());

		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(this, CProjectDescriptionEvent.APPLIED | CProjectDescriptionEvent.LOADED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#addPathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	@Override
	public void addPathEntryStoreListener(IPathEntryStoreListener listener) {
		fListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#removePathEntryStoreListener(org.eclipse.cdt.core.resources.IPathEntryStoreListener)
	 */
	@Override
	public void removePathEntryStoreListener(IPathEntryStoreListener listener) {
		fListeners.remove(listener);
	}

	private void fireContentChangedEvent(IProject project) {
		PathEntryStoreChangedEvent evt = new PathEntryStoreChangedEvent(this, project, PathEntryStoreChangedEvent.CONTENT_CHANGED);
		IPathEntryStoreListener[] observers = new IPathEntryStoreListener[fListeners.size()];
		fListeners.toArray(observers);
		for (IPathEntryStoreListener observer : observers) {
			observer.pathEntryStoreChanged(evt);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStore#fireClosedChangedEvent(IProject)
	 */
	@Override
	public void close() {
		PathEntryStoreChangedEvent evt = new PathEntryStoreChangedEvent(this, fProject, PathEntryStoreChangedEvent.STORE_CLOSED);
		IPathEntryStoreListener[] observers = new IPathEntryStoreListener[fListeners.size()];
		fListeners.toArray(observers);
		for (IPathEntryStoreListener observer : observers) {
			observer.pathEntryStoreChanged(evt);
		}
		CProjectDescriptionManager.getInstance().removeCProjectDescriptionListener(this);
	}

	@Override
	public IProject getProject() {
		return fProject;
	}

	@Override
	@Deprecated
	public ICExtensionReference getExtensionReference() {
		return null;
	}

	@Override
	public ICConfigExtensionReference getConfigExtensionReference() {
		return null;
	}

	@Override
	public IPathEntry[] getRawPathEntries() throws CoreException {
		ICConfigurationDescription cfg = getIndexCfg(fProject);
		List<IPathEntry>[] es = getEntries(fProject, cfg);
		if(es != null){
			List<IPathEntry> list = new ArrayList<IPathEntry>(es[0].size() + 1);
			list.addAll(es[0]);
			list.add(CoreModel.newContainerEntry(ConfigBasedPathEntryContainer.CONTAINER_PATH));
			return list.toArray(new IPathEntry[list.size()]);
		}
		return new IPathEntry[0];
	}

	@Override
	public void setRawPathEntries(IPathEntry[] entries) throws CoreException {
		ICConfigurationDescription cfg = getIndexCfg(fProject);
		List<IPathEntry> es[] = getEntries(fProject, cfg);
		if(es != null){
			List<IPathEntry> sysList = es[1];
			List<IPathEntry> usrList = es[0];
			List<IPathEntry> newUsrList = new ArrayList<IPathEntry>(entries.length);
			for (IPathEntry entry : entries) {
				if(entry.getEntryKind() != IPathEntry.CDT_CONTAINER)
					newUsrList.add(entry);
			}

			if(!newUsrList.equals(usrList)){
				usrList = newUsrList;
				ICProjectDescription des = CoreModel.getDefault().getProjectDescription(fProject, true);
				ICConfigurationDescription cfgDes = des.getDefaultSettingConfiguration();
				CConfigurationData data = cfgDes.getConfigurationData();
				PathEntryTranslator tr = new PathEntryTranslator(fProject, data);
				IPathEntry[] usrEntries = usrList.toArray(new IPathEntry[usrList.size()]);
				IPathEntry[] sysEntries = sysList.toArray(new IPathEntry[sysList.size()]);
				ReferenceSettingsInfo rInfo = tr.applyPathEntries(usrEntries, sysEntries, PathEntryTranslator.OP_REPLACE);
				cfgDes.removeExternalSettings();
				ICExternalSetting extSettings[] = rInfo.getExternalSettings();
				for (ICExternalSetting setting : extSettings) {
					cfgDes.createExternalSetting(setting.getCompatibleLanguageIds(),
							setting.getCompatibleContentTypeIds(),
							setting.getCompatibleExtensions(),
							setting.getEntries());
				}
				Map<String, String> refMap = rInfo.getRefProjectsMap();
				cfgDes.setReferenceInfo(refMap);

				CoreModel.getDefault().setProjectDescription(fProject, des);
			}
		}
	}

	private static void clearCachedEntries(ICProjectDescription des){
		ICConfigurationDescription[] cfgDess = des.getConfigurations();
		for (ICConfigurationDescription cfgDes : cfgDess) {
			setCachedEntries(cfgDes, null);
		}
	}

	@Override
	public void handleEvent(CProjectDescriptionEvent event) {
		IProject project = event.getProject();
		if(!fProject.equals(project))
			return;

		switch(event.getEventType()){
			case CProjectDescriptionEvent.APPLIED:{
				ICProjectDescription des = event.getNewCProjectDescription();
				ICProjectDescription oldDes = event.getOldCProjectDescription();
				List<IPathEntry> oldCrEntries = null;
				if(oldDes != null){
					ICConfigurationDescription oldIndexCfg = oldDes.getDefaultSettingConfiguration();
					List<IPathEntry>[] oldEs = getCachedEntries(oldIndexCfg);
					if(oldEs != null)
						oldCrEntries = oldEs[1];

					clearCachedEntries(oldDes);
				}
				if(des != null){
					//TODO: smart delta handling
					clearCachedEntries(des);

					if(oldCrEntries != null){
						ICConfigurationDescription newIndexCfg = des.getDefaultSettingConfiguration();
						List<IPathEntry>[] newEs = getEntries(fProject, newIndexCfg);
						List<IPathEntry> newCrEntries = newEs[1];
						if(!Arrays.equals(oldCrEntries.toArray(), newCrEntries.toArray())){
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

	private static List<IPathEntry>[] getEntries(IProject project, ICConfigurationDescription cfgDes){
		if(cfgDes != null){
			List<IPathEntry>[] es = getCachedEntries(cfgDes);
			if(es == null){
				PathEntryCollector cr = PathEntryTranslator.collectEntries(project, cfgDes);
				es = createEntriesList(cfgDes, cr);
				setCachedEntries(cfgDes, es);
			}
			return es;
		}
		return null;
	}

	private static List<IPathEntry>[] createEntriesList(ICConfigurationDescription cfgDes, PathEntryCollector cr){
		@SuppressWarnings("unchecked")
		ArrayList<IPathEntry>[] es = new ArrayList[2];
		es[0] = new ArrayList<IPathEntry>();
		cr.getEntries(es[0], PathEntryTranslator.INCLUDE_USER, cfgDes);
		es[0].trimToSize();
		es[1] = new ArrayList<IPathEntry>();
		cr.getEntries(es[1], PathEntryTranslator.INCLUDE_BUILT_INS, cfgDes);
		es[1].trimToSize();

		return es;
	}

	@SuppressWarnings("unchecked")
	private static List<IPathEntry>[] getCachedEntries(ICConfigurationDescription cfgDes){
		return (List<IPathEntry>[])cfgDes.getSessionProperty(PATH_ENTRY_COLLECTOR_PROPERTY_NAME);
	}

	private static void setCachedEntries(ICConfigurationDescription cfgDes, List<IPathEntry>[] es){
		cfgDes.setSessionProperty(PATH_ENTRY_COLLECTOR_PROPERTY_NAME, es);
	}

//	public static PathEntryCollector getCollector(IProject project){
//		ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project, false);
//		if(des != null)
//			return getCollector(des);
//		return null;
//	}

	private static ICConfigurationDescription getIndexCfg(IProject project){
		ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project, false);
		return des != null ? des.getDefaultSettingConfiguration() : null;
	}

	private static List<IPathEntry> getContainerEntries(IProject project){
		ICProjectDescription des = CCorePlugin.getDefault().getProjectDescription(project, false);
		if(des != null)
			return getContainerEntries(des);
		return new ArrayList<IPathEntry>(0);
	}

	private static List<IPathEntry> getContainerEntries(ICProjectDescription des){
		ICConfigurationDescription cfg = des.getDefaultSettingConfiguration();
		List<IPathEntry> es[] = getEntries(des.getProject(), cfg);
		if(es != null)
			return es[1];
		return new ArrayList<IPathEntry>(0);
	}

	public static ConfigBasedPathEntryContainer createContainer(IProject project){
		List<IPathEntry> list = getContainerEntries(project);
		return new ConfigBasedPathEntryContainer(list);
	}

	public static ConfigBasedPathEntryContainer createContainer(ICProjectDescription des){
		List<IPathEntry> list = getContainerEntries(des);
		return new ConfigBasedPathEntryContainer(list);
	}
}
