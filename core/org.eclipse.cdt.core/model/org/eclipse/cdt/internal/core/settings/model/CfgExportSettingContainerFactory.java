/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Intel Corporation - Initial API and implementation
 *  James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettingsManager.CContainerRef;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * A class responsible for persisting CDT Projects and Configuration IDs as referenced
 * by other configurations in other projects.
 * The user controls this via RefsTab.  This External settings factory listens
 * for CProjectDescription model changes and notifies the {@link CExternalSettingsManager},
 * which is a listener, of changes to the set of external settings.
 * {@link ICConfigurationDescription#setReferenceInfo(Map)} and {@link ICConfigurationDescription#getReferenceInfo()}
 */
public class CfgExportSettingContainerFactory extends
		CExternalSettingContainerFactoryWithListener implements ICProjectDescriptionListener {

	/** ID of this external settings factory */
	static final String FACTORY_ID = CCorePlugin.PLUGIN_ID + ".cfg.export.settings.sipplier"; //$NON-NLS-1$

	/** Empty String is a the magic configuration ID which maps to the Active configuration */
	private static final String ACTIVE_CONFIG_ID = ""; //$NON-NLS-1$
	private static final char DELIMITER = ';';

	/** Cache the external settings exported by project configurations */
	private static final ConcurrentHashMap<String, CExternalSetting[]> cachedSettings = new ConcurrentHashMap<String, CExternalSetting[]>();

	private static CfgExportSettingContainerFactory fInstance;

	private CfgExportSettingContainerFactory(){
	}

	public static CfgExportSettingContainerFactory getInstance(){
		if(fInstance == null)
			fInstance = new CfgExportSettingContainerFactory();
		return fInstance;
	}

	@Override
	public void startup(){
		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(this,
				CProjectDescriptionEvent.APPLIED
				| CProjectDescriptionEvent.LOADED);
	}

	@Override
	public void shutdown(){
		CProjectDescriptionManager.getInstance().removeCProjectDescriptionListener(this);
	}

	/**
	 * An ExternalSettingsContainer which returns the settings as
	 * exported by a referenced configuration in another project.
	 */
	private static class CfgRefContainer extends CExternalSettingsContainer {
		final private String fId;
		final private String fProjName, fCfgId;
		final private CExternalSetting[] prevSettings;

		CfgRefContainer(String containerId, String projName, String cfgId, CExternalSetting[] previousSettings){
			fId = containerId;
			fProjName = projName;
			fCfgId = cfgId;
			prevSettings = previousSettings;
		}

		@Override
		public CExternalSetting[] getExternalSettings() {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(fProjName);
			if (project.isAccessible()) {
				ICProjectDescription des = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
				if(des != null){
					ICConfigurationDescription cfg = fCfgId.length() != 0 ?
							des.getConfigurationById(fCfgId) : des.getActiveConfiguration();

					if(cfg != null){
						CExternalSetting[] es;
						ICExternalSetting[] ies = cfg.getExternalSettings();
						if (ies instanceof CExternalSetting[])
							es = (CExternalSetting[])ies;
						else {
							es = new CExternalSetting[ies.length];
							System.arraycopy(ies, 0, es, 0, es.length);
						}
						// Update the cache with the real settings this configuration is exporting
						cachedSettings.put(fId, es);
						return es;
					}
				}
			}
			// If project not yet accessible, just return the previous settings
			// for the moment. We'll update again when the referenced project reappears
			if (!cachedSettings.containsKey(fId) && prevSettings.length > 0)
				cachedSettings.putIfAbsent(fId, prevSettings);
			if (prevSettings.length == 0 && cachedSettings.containsKey(fId))
				return cachedSettings.get(fId);
			return prevSettings;
		}
	}

	@Override
	public CExternalSettingsContainer createContainer(String id,
			IProject project, ICConfigurationDescription cfgDes, CExternalSetting[] previousSettings) throws CoreException {
		String[] r = parseId(id);
		return new CfgRefContainer(id, r[0], r[1], previousSettings);
	}

	private static void createReference(ICConfigurationDescription cfg, String projName, String cfgId){
		CContainerRef cr = createContainerRef(projName, cfgId);
		CExternalSettingsManager.getInstance().addContainer(cfg, cr);
	}

	private static void removeReference(ICConfigurationDescription cfg, String projName, String cfgId){
		CContainerRef cr = createContainerRef(projName, cfgId);
		CExternalSettingsManager.getInstance().removeContainer(cfg, cr);
	}

	private static CContainerRef createContainerRef(String projName, String cfgId){
		return new CContainerRef(FACTORY_ID, createId(projName, cfgId));
	}

	public static Map<String, String> getReferenceMap(ICConfigurationDescription cfg){
		CContainerRef[] refs = CExternalSettingsManager.getInstance().getReferences(cfg, FACTORY_ID);
		Map<String, String> map = new LinkedHashMap<String, String>();
		for(int i = 0; i < refs.length; i++){
			try {
				String[] r = parseId(refs[i].getContainerId());
				map.put(r[0], r[1]);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
		return map;
	}

	public static void setReferenceMap(ICConfigurationDescription cfg, Map<String, String> map){
		Map<String, String> oldRefs = getReferenceMap(cfg);
		Map<String, String> newRefs = new LinkedHashMap<String, String>(map);

		// We need to preserve order. The API we have with the external settings manager allows us to
		// add and remove individual items.
		// In the future this could be fixed, but for the moment, remove and replace all the referenced items
		// from the first item that doens't match.

		Iterator<Map.Entry<String, String>> oldIter = oldRefs.entrySet().iterator();
		Iterator<Map.Entry<String, String>> newIter = newRefs.entrySet().iterator();

		while (oldIter.hasNext() && newIter.hasNext()) {
			Map.Entry<String, String> oldEntry = oldIter.next();
			Map.Entry<String, String> newEntry = newIter.next();
			if (!oldEntry.equals(newEntry))
				break;
			oldIter.remove();
			newIter.remove();
		}

		// Now remove all the remaining old entries
		for (Map.Entry<String,String> entry : oldRefs.entrySet())
			removeReference(cfg, entry.getKey(), entry.getValue());
		// And add the new entries
		for (Map.Entry<String,String> entry : newRefs.entrySet())
			createReference(cfg, entry.getKey(), entry.getValue());
	}

	/**
	 * Reference ID looks like:
	 *   {projName};{configuration_id}
	 * @param projName
	 * @param cfgId
	 * @return ID
	 */
	private static String createId(String projName, String cfgId){
		return projName + DELIMITER + cfgId;
	}
	/**
	 * Return a 2-element array String[]{projName, cfgId}
	 * @param id id to parse, must not be null
	 * @return String[]{projName, cfgId}
	 * @throws CoreException if prjName not valid
	 */
	private static String[] parseId(String id) throws CoreException {
		if(id == null)
			throw new NullPointerException();

		String projName, cfgId;
		int index = id.indexOf(DELIMITER);
		if(index != -1){
			projName = id.substring(0, index);
			cfgId = id.substring(index + 1);
		} else {
			projName = id;
			cfgId = ACTIVE_CONFIG_ID;
		}

		if((projName = projName.trim()).length() == 0)
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CfgExportSettingContainerFactory.2")); //$NON-NLS-1$

		return new String[]{projName, cfgId};
	}

	/**
	 * Notify the ExternalSettingManager that there's been a change in the configuration which may require referencing configs to update
	 * their cache of the external settings
	 */
	@Override
	public void handleEvent(CProjectDescriptionEvent event) {
		switch(event.getEventType()){
			case CProjectDescriptionEvent.LOADED: {
				// Bug 312575 on Project load, event.getProjectDelta() == null => report all configs as potentially changed
				// Referencing projects should be reconciled and potentially updated.
				String projName = event.getProject().getName();
				ICConfigurationDescription[] descs = event.getNewCProjectDescription().getConfigurations();
				CExternalSettingsContainerChangeInfo[] changeInfos = new CExternalSettingsContainerChangeInfo[descs.length + 1];
				int i = 0;
				for (ICConfigurationDescription desc : event.getNewCProjectDescription().getConfigurations())
					changeInfos[i++] = new CExternalSettingsContainerChangeInfo(
							CExternalSettingsContainerChangeInfo.CONTAINER_CONTENTS,
							new CContainerRef(FACTORY_ID, createId(projName, desc.getId())),
							null);
				// Active configuration too
				changeInfos[i] = new CExternalSettingsContainerChangeInfo(
						CExternalSettingsContainerChangeInfo.CONTAINER_CONTENTS,
						new CContainerRef(FACTORY_ID, createId(projName, ACTIVE_CONFIG_ID)),
						null);
				notifySettingsChange(null, null, changeInfos);
				break;
			}
			case CProjectDescriptionEvent.APPLIED:
				String[] ids = getContainerIds(event.getProjectDelta());
				if(ids.length != 0){
					CExternalSettingsContainerChangeInfo[] changeInfos =
						new CExternalSettingsContainerChangeInfo[ids.length];

					for(int i = 0; i < changeInfos.length; i++){
						changeInfos[i] = new CExternalSettingsContainerChangeInfo(
								CExternalSettingsContainerChangeInfo.CONTAINER_CONTENTS,
								new CContainerRef(FACTORY_ID, ids[i]),
								null);
					}
					notifySettingsChange(null, null, changeInfos);
				}
		}
	}

	/**
	 * Returns the set of containers (Project configurations) (project_name;config_id) for the project descriptions
	 * reported as changed by the ICDescriptionDelta
	 * @param delta
	 * @return String[] of Configuration Reference IDs
	 */
	private String[] getContainerIds(ICDescriptionDelta delta){
		if(delta == null)
			return new String[0];
		int deltaKind = delta.getDeltaKind();

		Set<String> cfgIds = new HashSet<String>();
		switch(deltaKind){
		case ICDescriptionDelta.ADDED:
		case ICDescriptionDelta.REMOVED:
			ICProjectDescription des = (ICProjectDescription)delta.getSetting();
			ICConfigurationDescription[] cfgs = des.getConfigurations();
			if(cfgs.length != 0){
				for(int i = 0; i < cfgs.length; i++){
					cfgIds.add(cfgs[i].getId());
				}
				cfgIds.add(ACTIVE_CONFIG_ID);
			}
			break;
		case ICDescriptionDelta.CHANGED:
			ICDescriptionDelta[] children = delta.getChildren();
			collectCfgIds(children, cfgIds);
			if((delta.getChangeFlags() & ICDescriptionDelta.ACTIVE_CFG) != 0)
				cfgIds.add(ACTIVE_CONFIG_ID);
			break;
		}

		String[] ids = new String[cfgIds.size()];
		if(ids.length != 0){
			String projName = ((ICProjectDescription)delta.getSetting()).getProject().getName();
			int i = 0;
			for (String config : cfgIds)
				ids[i++] = createId(projName, config);
		}
		return ids;
	}

	/**
	 * Return the set of changed {Added, Remove & Changed} configuration IDs as discovered
	 * from an ICDescrptionDelta[]
	 * @param deltas
	 * @param c
	 * @return
	 */
	private Collection<String> collectCfgIds(ICDescriptionDelta[] deltas, Collection<String> c){
		for (ICDescriptionDelta delta : deltas) {
			switch (delta.getDeltaKind()) {
			case ICDescriptionDelta.ADDED:
			case ICDescriptionDelta.REMOVED:
				c.add(delta.getSetting().getId());
				break;
			case ICDescriptionDelta.CHANGED:
				int changeFlags = delta.getChangeFlags();
				if((changeFlags &
						(ICDescriptionDelta.EXTERNAL_SETTINGS_ADDED
								| ICDescriptionDelta.EXTERNAL_SETTINGS_REMOVED)) != 0){
					c.add(delta.getSetting().getId());
					// If this is the Active configuration, then record it as changed too (bug 312575)
					if (delta.getSetting().getConfiguration().isActive())
						c.add(ACTIVE_CONFIG_ID);
				}
				break;
			}
		}
		return c;
	}
}
