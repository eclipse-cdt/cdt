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
package org.eclipse.cdt.core.settings.model.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.SettingsSet.SettingLevel;

public abstract class UserAndDiscoveredEntryStorage extends AbstractEntryStorage {
	private static final int USER_ENTRIES_LEVEL = 0;
	private static final int DISCOVERY_ENTRIES_LEVEL = 1;

	public UserAndDiscoveredEntryStorage(int kind) {
		super(kind);
	}

	@Override
	protected SettingsSet createEmptySettings(){
		SettingsSet settings = new SettingsSet(2);
		SettingsSet.SettingLevel levels[] = settings.getLevels();
		
		levels[USER_ENTRIES_LEVEL].setFlagsToClear(ICSettingEntry.READONLY | ICSettingEntry.BUILTIN);
		levels[USER_ENTRIES_LEVEL].setFlagsToSet(0);
		levels[USER_ENTRIES_LEVEL].setReadOnly(false);
		levels[USER_ENTRIES_LEVEL].setOverrideSupported(false);

		boolean override = canDisableDiscoveredEntries(); 
		int readOnlyFlag = override ? 0 : ICSettingEntry.READONLY;
		levels[DISCOVERY_ENTRIES_LEVEL].setFlagsToClear(0);
		levels[DISCOVERY_ENTRIES_LEVEL].setFlagsToSet(readOnlyFlag | ICSettingEntry.BUILTIN | ICSettingEntry.RESOLVED);
		levels[DISCOVERY_ENTRIES_LEVEL].setReadOnly(true);
		levels[DISCOVERY_ENTRIES_LEVEL].setOverrideSupported(override);

		return settings;
	}
	
	@Override
	protected void obtainEntriesFromLevel(int levelNum, SettingLevel level) {
		switch(levelNum){
		case USER_ENTRIES_LEVEL:
			setUserEntries(level != null ? level.getEntries() : null);
			break;
		case DISCOVERY_ENTRIES_LEVEL:
			if(level != null){
				Set<String> set = level.getOverrideSet();
				setDisabledDiscoveredNames(set);
			} else {
				setDisabledDiscoveredNames(null);
			}
		}
	}

	@Override
	protected void putEntriesToLevel(int levelNum, SettingLevel level) {
		switch(levelNum){
		case USER_ENTRIES_LEVEL:
			level.addEntries(getUserEntries());
			break;
		case DISCOVERY_ENTRIES_LEVEL:
			HashSet<String> set = new HashSet<String>();
			ICLanguageSettingEntry[] entries = getDiscoveredEntries(set);
			level.addEntries(entries);
			if(set.size() != 0)
				level.fOverrideSet = set;
			break;
		}
	}

	protected boolean canDisableDiscoveredEntries(){
		return false;
	}

	protected abstract void setUserEntries(ICLanguageSettingEntry[] entries);
	
	protected abstract ICLanguageSettingEntry[] getUserEntries();

	protected abstract void setDisabledDiscoveredNames(Set<String> disabledNameSet);

	protected abstract ICLanguageSettingEntry[] getDiscoveredEntries(Set<String> disabledNameSet);
}
