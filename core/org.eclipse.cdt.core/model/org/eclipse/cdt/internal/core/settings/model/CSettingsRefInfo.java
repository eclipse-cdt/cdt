/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * James Blackburn (Broadcom Corp.)
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
import org.eclipse.cdt.core.settings.model.util.EntryNameKey;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettingsManager.CContainerRef;

class CSettingsRefInfo {

	/** External Settings Holder Map
	 *  From references container -> to concrete held settings */
	private HashMap<CContainerRef, CRefSettingsHolder> fESHolderMap = new LinkedHashMap<>();

	CSettingsRefInfo() {
	}

	CSettingsRefInfo(ICStorageElement el) {
		for (ICStorageElement child : el.getChildren()) {
			String name = child.getName();
			if (CExternalSettingsHolder.ELEMENT_EXT_SETTINGS_CONTAINER.equals(name)) {
				CRefSettingsHolder h = new CRefSettingsHolder(child);
				CContainerRef r = h.getContainerInfo();
				fESHolderMap.put(r, h);
			}
		}
	}

	@SuppressWarnings("unchecked")
	CSettingsRefInfo(CSettingsRefInfo base) {
		fESHolderMap = (LinkedHashMap<CContainerRef, CRefSettingsHolder>) base.fESHolderMap.clone();
		for (Map.Entry<CContainerRef, CRefSettingsHolder> e : fESHolderMap.entrySet()) {
			CRefSettingsHolder h = e.getValue();
			h = new CRefSettingsHolder(h);
			e.setValue(h);
		}
	}

	CContainerRef[] getReferences(String factoryId) {
		List<CContainerRef> list = new ArrayList<>();
		for (CContainerRef r : fESHolderMap.keySet()) {
			if (r.getFactoryId().equals(factoryId))
				list.add(r);
		}
		return list.toArray(new CContainerRef[list.size()]);
	}

	CContainerRef[] getReferences() {
		return fESHolderMap.keySet().toArray(new CContainerRef[fESHolderMap.size()]);
	}

	CRefSettingsHolder get(CContainerRef cRef) {
		return fESHolderMap.get(cRef);
	}

	void serialize(ICStorageElement element) {
		for (CRefSettingsHolder h : fESHolderMap.values()) {
			ICStorageElement child = element.createChild(CExternalSettingsHolder.ELEMENT_EXT_SETTINGS_CONTAINER);
			h.serialize(child);
		}
	}

	void put(CRefSettingsHolder holder) {
		fESHolderMap.put(holder.getContainerInfo(), holder);
	}

	CRefSettingsHolder remove(CContainerRef cRef) {
		return fESHolderMap.remove(cRef);
	}

	CExternalSetting[] createExternalSettings() {
		if (fESHolderMap.size() == 0)
			return new CExternalSetting[0];
		if (fESHolderMap.size() == 1)
			return fESHolderMap.values().iterator().next().getExternalSettings();
		CExternalSettingsHolder holder = new CExternalSettingsHolder();
		for (Iterator<CRefSettingsHolder> iter = fESHolderMap.values().iterator(); iter.hasNext();) {
			CExternalSettingsHolder h = iter.next();
			holder.setExternalSettings(h.getExternalSettings(), true);
		}
		return holder.getExternalSettings();
	}

	ICSettingEntry[] getAllEntries(int kind) {
		Map<EntryNameKey, ICSettingEntry> map = new LinkedHashMap<>();
		for (CRefSettingsHolder h : fESHolderMap.values()) {
			CExternalSetting[] settings = h.getExternalSettings();
			for (int i = 0; i < settings.length; i++) {
				ICSettingEntry[] entries = settings[i].getEntries(kind);
				CDataUtil.fillEntriesMapByNameKey(map, entries);
			}
		}
		return map.values().toArray(new ICSettingEntry[map.size()]);
	}

}
