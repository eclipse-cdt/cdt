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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettingsManager.CContainerRef;

class CSettingsRefInfo {
	final static String ELEMENT_REFERENCE_INFO = "referenceInfo";  //$NON-NLS-1$
	private HashMap fESHolderMap = new HashMap();

	CSettingsRefInfo(){
	}

	CSettingsRefInfo(ICStorageElement el){
		
		ICStorageElement children[] = el.getChildren();
		for(int i = 0; i < children.length; i++){
			ICStorageElement child = children[i];
			String name = child.getName();
			if(CRefSettingsHolder.ELEMENT_EXT_SETTINGS_CONTAINER.equals(name)){
				CRefSettingsHolder h = new CRefSettingsHolder(child);
				CContainerRef r = h.getContainerInfo();
				fESHolderMap.put(r, h);
			}
		}
	}

	CSettingsRefInfo(CSettingsRefInfo base){
		fESHolderMap = (HashMap)base.fESHolderMap.clone();
		for(Iterator iter = fESHolderMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			CRefSettingsHolder h = (CRefSettingsHolder)entry.getValue();
			h = new CRefSettingsHolder(h);
			entry.setValue(h);
		}
		
	}
	
//	Map getContainerMapForFactory(String id){
//		Map map = new HashMap();
//		for(Iterator iter = fESHolderMap.entrySet().iterator(); iter.hasNext();){
//			Map.Entry entry = (Map.Entry)iter.next();
//			CContainerRef r = (CContainerRef)entry.getKey();
//			if(r.getFactoryId().equals(id))
//				map.put(r.getContainerId(), r);
//		}
//		return map;
//	}
	
//	Map getContainerMapCopy(){
//		return new HashMap(fESHolderMap);
//	}
	
	CContainerRef[] getReferences(String factoryId){
		List list = new ArrayList();
		for(Iterator iter = fESHolderMap.keySet().iterator(); iter.hasNext();){
			CContainerRef r = (CContainerRef)iter.next();
			if(r.getFactoryId().equals(factoryId))
				list.add(r);
		}
		return (CContainerRef[])list.toArray(new CContainerRef[list.size()]);
	}

	CContainerRef[] getReferences(){
		return (CContainerRef[])fESHolderMap.keySet().toArray(new CContainerRef[fESHolderMap.size()]);
	}

	CRefSettingsHolder get(CContainerRef cRef){
		return (CRefSettingsHolder)fESHolderMap.get(cRef);
	}
	
	void serialize(ICStorageElement element){
		for(Iterator iter = fESHolderMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			CRefSettingsHolder h = (CRefSettingsHolder)entry.getValue();
			ICStorageElement child = element.createChild(CRefSettingsHolder.ELEMENT_EXT_SETTINGS_CONTAINER);
			h.serialize(child);
		}
	}
	
	void put(CRefSettingsHolder holder){
		fESHolderMap.put(holder.getContainerInfo(), holder);
	}
	
	CRefSettingsHolder remove(CContainerRef cRef){
		return (CRefSettingsHolder)fESHolderMap.remove(cRef);
	}
	
	CExternalSetting[] createExternalSettings(){
		if(fESHolderMap.size() == 0)
			return new CExternalSetting[0];
		if(fESHolderMap.size() == 1)
			return ((CRefSettingsHolder)fESHolderMap.values().iterator().next()).getExternalSettings();
		CExternalSettingsHolder holder = new CExternalSettingsHolder();
		for(Iterator iter = fESHolderMap.values().iterator(); iter.hasNext();){
			CExternalSettingsHolder h = (CExternalSettingsHolder)iter.next();
			holder.setExternalSettings(h.getExternalSettings(), true);
		}
		return holder.getExternalSettings();
	}
	
	ICSettingEntry[] getAllEntries(int kind){
		Map map = new LinkedHashMap();
		for(Iterator iter = fESHolderMap.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			CRefSettingsHolder h = (CRefSettingsHolder)entry.getValue();
			CExternalSetting[] settings = h.getExternalSettings();
			for(int i = 0; i < settings.length; i++){
				ICSettingEntry[] entries = settings[i].getEntries(kind);
				CDataUtil.fillEntriesMapByNameKey(map, entries);
			}
		}
		return (ICSettingEntry[])map.values().toArray(new ICSettingEntry[map.size()]);
	}

}
