/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.language.settings.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;


// TODO possibly make an interface from that
public class LanguageSettingsDelta {
	// maps need to be ordered by providers
	private LinkedHashMap<String, // providerId
					LanguageSettingsStorage> oldLanguageSettingsState;
	private LinkedHashMap<String, // providerId
					LanguageSettingsStorage> newLanguageSettingsState;
	
	public LanguageSettingsDelta(LinkedHashMap<String, LanguageSettingsStorage> oldState, LinkedHashMap<String, LanguageSettingsStorage> newState) {
		oldLanguageSettingsState = oldState;
		newLanguageSettingsState = newState;
	}

	// FIXME - this API is no good
	public Set<IResource> getChangedResources(IProject project, ICConfigurationDescription cfgDescription) {
		Set<IResource> resources = new HashSet<IResource>();
		
		// Ordered collections
		Collection<LanguageSettingsStorage> oldStateStorages = oldLanguageSettingsState.values();
		Collection<LanguageSettingsStorage> newStateStorages = newLanguageSettingsState.values();

		for (LanguageSettingsStorage lss : oldStateStorages) {
//			Map<String, // languageId
//				Map<String, // resource project path
//					List<ICLanguageSettingEntry>>>
			Map<String, Map<String, List<ICLanguageSettingEntry>>> mapInternal = lss.getMapInternal();
			synchronized (mapInternal) {
				for (Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang : mapInternal.entrySet()) {
					String langId = entryLang.getKey();
					for (Entry<String, List<ICLanguageSettingEntry>> entryRc : entryLang.getValue().entrySet()) {
						String rcName = entryRc.getKey();
						IResource rc = (rcName != null) ? project.findMember(rcName) : project;
						if (resources.contains(rc))
							continue;
						
						List<ICLanguageSettingEntry> lsEntriesOld = entryRc.getValue();
						List<ICLanguageSettingEntry> lsEntriesNew = getSettingEntries(newStateStorages, cfgDescription, rc, langId);
						
						if (!lsEntriesNew.equals(lsEntriesOld) && !(lsEntriesOld==null && lsEntriesNew.size()==0)) {
							resources.add(rc);
						}
					}
				}
			}
		}
		for (LanguageSettingsStorage lss : newStateStorages) {
//			Map<String, // languageId
//				Map<String, // resource project path
//					List<ICLanguageSettingEntry>>>
			Map<String, Map<String, List<ICLanguageSettingEntry>>> mapInternal = lss.getMapInternal();
			synchronized (mapInternal) {
				for (Entry<String, Map<String, List<ICLanguageSettingEntry>>> entryLang : mapInternal.entrySet()) {
					String langId = entryLang.getKey();
					for (Entry<String, List<ICLanguageSettingEntry>> entryRc : entryLang.getValue().entrySet()) {
						String rcName = entryRc.getKey();
						IResource rc = (rcName != null) ? project.findMember(rcName) : project;
						if (resources.contains(rc))
							continue;
						
						List<ICLanguageSettingEntry> lsEntriesNew = entryRc.getValue();
						List<ICLanguageSettingEntry> lsEntriesOld = getSettingEntries(oldStateStorages, cfgDescription, rc, langId);
						
						if (!lsEntriesOld.equals(lsEntriesNew) && !(lsEntriesNew==null && lsEntriesOld.size()==0)) {
							resources.add(rc);
						}
					}
				}
			}
		}
		
		return resources;
	}

	private static boolean checkBit(int flags, int bit) {
		return (flags & bit) == bit;
	}

	private static List<ICLanguageSettingEntry> getSettingEntries(Collection<LanguageSettingsStorage> stores, ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		List<String> alreadyAdded = new ArrayList<String>();
	
		for (LanguageSettingsStorage store: stores) {
			List<ICLanguageSettingEntry> providerEntries = getSettingEntriesUpResourceTree(store, cfgDescription, rc, languageId);
			for (ICLanguageSettingEntry entry : providerEntries) {
				if (entry!=null) {
					String entryName = entry.getName();
					// Only first entry is considered
					// Entry flagged as "UNDEFINED" prevents adding entry with the same name down the line
					if (!alreadyAdded.contains(entryName)) {
						int flags = entry.getFlags();
						if (!checkBit(flags, ICSettingEntry.UNDEFINED)) {
							entries.add(entry);
						}
						alreadyAdded.add(entryName);
					}
				}
			}
		}
	
		return entries;
	}

	private static List<ICLanguageSettingEntry> getSettingEntriesUpResourceTree(LanguageSettingsStorage store, ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		Assert.isTrue( !(rc instanceof IWorkspaceRoot) );
		if (store!=null) {
			List<ICLanguageSettingEntry> entries = store.getSettingEntries(cfgDescription, rc, languageId);
			if (entries!=null) {
				return new ArrayList<ICLanguageSettingEntry>(entries);
			}
			if (rc!=null) {
				IResource parentFolder = (rc instanceof IProject) ? null : rc.getParent();
				if (parentFolder!=null) {
					return getSettingEntriesUpResourceTree(store, cfgDescription, parentFolder, languageId);
				}
				// if out of parent resources - get default entries for the applicable language scope
				entries = store.getSettingEntries(null, null, languageId);
				if (entries!=null) {
					return new ArrayList<ICLanguageSettingEntry>(entries);
				}
			}
		}
	
		return new ArrayList<ICLanguageSettingEntry>(0);
	}
}
