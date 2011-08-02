/*******************************************************************************
 * Copyright (c) 2009, 2009 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.scannerconfig;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.AbstractExecutableExtensionBase;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

//public class MBSLanguageSettingsProvider extends AbstractExecutableExtensionBase implements ILanguageSettingsEditableProvider {
public class MBSLanguageSettingsProvider extends AbstractExecutableExtensionBase implements ILanguageSettingsProvider {

	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		
		IPath projectPath = rc.getProjectRelativePath();
		ICResourceDescription rcDescription = cfgDescription.getResourceDescription(projectPath, false);
		
		List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>();
		for (ICLanguageSetting languageSetting : getLanguageSettings(rcDescription)) {
			if (languageSetting!=null) {
				String id = languageSetting.getLanguageId();
				if (id!=null && id.equals(languageId)) {
					int kindsBits = languageSetting.getSupportedEntryKinds();
					for (int kind=1;kind<=kindsBits;kind<<=1) {
						if ((kindsBits & kind) != 0) {
							list.addAll(languageSetting.getSettingEntriesList(kind));
						}
					}
				} else {
//					System.err.println("languageSetting id=null: name=" + languageSetting.getName());
				}
			} else {
				System.err.println("languageSetting=null: rcDescription=" + rcDescription.getName());
			}
		}
		return list;
	}
	
	private ICLanguageSetting[] getLanguageSettings(ICResourceDescription rcDescription) {
		ICLanguageSetting[] array = null;
		switch (rcDescription.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription)rcDescription;
			array = foDes.getLanguageSettings();
			break;
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription)rcDescription;
			ICLanguageSetting ls = fiDes.getLanguageSetting();
			if (ls!=null) {
				array = new ICLanguageSetting[] { ls };
			}
		}
		if (array==null) {
			array = new ICLanguageSetting[0];
		}
		return array;
	}

	public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId,
			List<ICLanguageSettingEntry> entries) {

//		lang.setSettingEntries(kind, entries);
		IPath projectPath = rc.getProjectRelativePath();
		ICResourceDescription rcDescription = cfgDescription.getResourceDescription(projectPath, false);
		
		for (ICLanguageSetting languageSetting : getLanguageSettings(rcDescription)) {
			if (languageSetting!=null) {
				String id = languageSetting.getLanguageId();
				if (id!=null && id.equals(languageId)) {
					int kindsBits = languageSetting.getSupportedEntryKinds();
					for (int kind=1;kind<=kindsBits;kind<<=1) {
						if ((kindsBits & kind) != 0) {
							List<ICLanguageSettingEntry> list = new ArrayList<ICLanguageSettingEntry>(entries.size());
							for (ICLanguageSettingEntry entry : entries) {
								if (entry.getKind()==kind) {
									list.add(entry);
								}
							}
							languageSetting.setSettingEntries(kind, list);
						}
					}
				} else {
//					System.err.println("languageSetting id=null: name=" + languageSetting.getName());
				}
			} else {
				System.err.println("languageSetting=null: rcDescription=" + rcDescription.getName());
			}
		}
	}

}
