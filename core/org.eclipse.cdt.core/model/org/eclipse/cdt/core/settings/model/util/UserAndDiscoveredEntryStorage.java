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
package org.eclipse.cdt.core.settings.model.util;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.SettingsSet.SettingLevel;

public abstract class UserAndDiscoveredEntryStorage extends AbstractEntryStorage {
	public UserAndDiscoveredEntryStorage(int kind) {
		super(kind);
	}

	protected SettingsSet createEmptySettings(){
		SettingsSet settings = new SettingsSet(2);
		SettingLevel levels[] = settings.getLevels();
		
		boolean override = canDisableDiscoveredEntries(); 
		int readOnlyFlag = override ? 0 : ICSettingEntry.READONLY;
		levels[0].setFlagsToClear(ICSettingEntry.READONLY | ICSettingEntry.BUILTIN);
		levels[0].setFlagsToSet(0);
		levels[0].setReadOnly(false);
		levels[0].setOverrideSupported(false);

		levels[1].setFlagsToClear(0);
		levels[1].setFlagsToSet(readOnlyFlag | ICSettingEntry.BUILTIN | ICSettingEntry.RESOLVED);
		levels[1].setReadOnly(true);
		levels[1].setOverrideSupported(override);

		return settings;
	}
	
	protected void obtainEntriesFromLevel(int levelNum, SettingLevel level) {
		switch(levelNum){
		case 0:
			setUserEntries(level != null ? level.getEntries() : null);
			break;
		case 1:
			if(level != null){
				Set set = level.getOverrideSet();
				setDisabledDiscoveredNames(set);
			} else {
				setDisabledDiscoveredNames(null);
			}
		}
	}

	protected void putEntriesToLevel(int levelNum, SettingLevel level) {
		switch(levelNum){
		case 0:
			level.addEntries(getUserEntries());
			break;
		case 1:
			HashSet set = new HashSet();
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

	protected abstract void setDisabledDiscoveredNames(Set disabledNameSet);

	protected abstract ICLanguageSettingEntry[] getDiscoveredEntries(Set disabledNameSet);
}
