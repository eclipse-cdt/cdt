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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICDescriptionDelta;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettingsManager.CContainerRef;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettingsManager.NullContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class CfgExportSettingContainerFactory extends
		CExternalSettingContainerFactoryWithListener implements ICProjectDescriptionListener {
	static final String FACTORY_ID = CCorePlugin.PLUGIN_ID + ".cfg.export.settings.sipplier"; //$NON-NLS-1$
	private static final char DELIMITER = ';';
//	private ListenerList fListenerList;
	
	private static CfgExportSettingContainerFactory fInstance;

	private CfgExportSettingContainerFactory(){
	}
	
	public static CfgExportSettingContainerFactory getInstance(){
		if(fInstance == null)
			fInstance = new CfgExportSettingContainerFactory();
		return fInstance;
	}
	
	public void startup(){
		CProjectDescriptionManager.getInstance().addCProjectDescriptionListener(this, 
				CProjectDescriptionEvent.APPLIED
				| CProjectDescriptionEvent.LOADDED);
	}
	
	public void shutdown(){
		CProjectDescriptionManager.getInstance().removeCProjectDescriptionListener(this);
	}
	
	private class CfgRefContainer extends CExternalSettingsContainer {
		private String fProjName, fCfgId;
		
		CfgRefContainer(String projName, String cfgId){
			fProjName = projName;
			fCfgId = cfgId;
		}

		public CExternalSetting[] getExternalSettings() {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(fProjName);
			if(project.exists() && project.isOpen()){
				ICProjectDescription des = CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
				if(des != null){
					ICConfigurationDescription cfg = fCfgId.length() != 0 ? 
							des.getConfigurationById(fCfgId) : des.getActiveConfiguration();
					
					if(cfg != null){
						ICExternalSetting[] ies = cfg.getExternalSettings();
						if(ies instanceof CExternalSetting[])
							return (CExternalSetting[])ies;
						CExternalSetting[] es = new CExternalSetting[ies.length];
						System.arraycopy(ies, 0, es, 0, es.length);
						return es;
					}
				}
			}
			return new CExternalSetting[0];
		}
		
	}
	
	public CExternalSettingsContainer createContainer(String id,
			IProject project, ICConfigurationDescription cfgDes) {
		try {
			String[] r = parseId(id);
			return new CfgRefContainer(r[0], r[1]);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return new NullContainer();
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
	
	public static Map getReferenceMap(ICConfigurationDescription cfg){
		CContainerRef[] refs = CExternalSettingsManager.getInstance().getReferences(cfg, FACTORY_ID);
		Map map = new HashMap();
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

	public static void setReferenceMap(ICConfigurationDescription cfg, Map map){
		Map cur = getReferenceMap(cfg);
		Map newCopy = new HashMap(map);
		
		for(Iterator iter = cur.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			Object projName = entry.getKey();
			if(newCopy.containsKey(projName) && entry.getValue().equals(newCopy.get(projName))){
				iter.remove();
				newCopy.remove(projName);
			}
		}
		for(Iterator iter = cur.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			removeReference(cfg, (String)entry.getKey(), (String)entry.getValue());
		}
		for(Iterator iter = newCopy.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			createReference(cfg, (String)entry.getKey(), (String)entry.getValue());
		}
	}

	private static String createId(String projName, String cfgId){
		return new StringBuffer().append(projName).append(DELIMITER).append(cfgId).toString();
	}
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
			cfgId = ""; //$NON-NLS-1$
		}
		
		if((projName = projName.trim()).length() == 0)
			throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CfgExportSettingContainerFactory.2")); //$NON-NLS-1$
		
		return new String[]{projName, cfgId};
	}

//	public void addListener(ICExternalSettingsListener listener){
//		if(fListenerList == null)
//			fListenerList = new ListenerList();
//		
//		fListenerList.add(listener);
//	}
//	
//	public void removeListener(ICExternalSettingsListener listener){
//		if(fListenerList == null)
//			return;
//		
//		fListenerList.remove(listener);
//	}
	
	public void handleEvent(CProjectDescriptionEvent event) {
		switch(event.getEventType()){
			case CProjectDescriptionEvent.LOADDED:
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
		// TODO Auto-generated method stub
		
	}
	
	private String[] getContainerIds(ICDescriptionDelta delta){
		if(delta == null)
			return new String[0];
		int deltaKind = delta.getDeltaKind();
		
		List cfgIds = new ArrayList();
		switch(deltaKind){
		case ICDescriptionDelta.ADDED:
		case ICDescriptionDelta.REMOVED:
			ICProjectDescription des = (ICProjectDescription)delta.getSetting();
			ICConfigurationDescription[] cfgs = des.getConfigurations();
			if(cfgs.length != 0){
				for(int i = 0; i < cfgs.length; i++){
					cfgIds.add(cfgs[i].getId());
				}
				cfgIds.add(""); //$NON-NLS-1$
			}
			
		case ICDescriptionDelta.CHANGED:
			ICDescriptionDelta[] children = delta.getChildren();
			collectCfgIds(children, cfgIds);
			if((delta.getChangeFlags() & ICDescriptionDelta.ACTIVE_CFG) != 0)
				cfgIds.add(""); //$NON-NLS-1$
		}


		String[] ids = new String[cfgIds.size()];
		if(ids.length != 0){
			String projName = ((ICProjectDescription)delta.getSetting()).getProject().getName();
			for(int i = 0; i < ids.length; i++){
				ids[i] = createId(projName, (String)cfgIds.get(i));
			}
		}
		
		return ids;
	}
	
	public Collection collectCfgIds(ICDescriptionDelta[] deltas, Collection c){
		if(c == null)
			c = new ArrayList();
		for(int i = 0; i < deltas.length; i++){
			ICDescriptionDelta delta = deltas[i];
			int deltaKind = delta.getDeltaKind();
			
			switch(deltaKind){
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
				}
			}
		}
		
		return c;
	}

//	protected void notifySettingsChange(CExternalSettingsContainerChangeInfo[] infos){
//		if(fListenerList == null)
//			return;
//		
//		if(infos.length == 0)
//			return;
//		
//		CExternalSettingChangeEvent event = new CExternalSettingChangeEvent(infos);
//		
//		Object[] listeners = fListenerList.getListeners();
//		for(int i = 0; i < listeners.length; i++){
//			((ICExternalSettingsListener)listeners[i]).settingsChanged(null, null, event);
//		}
//	}
}
