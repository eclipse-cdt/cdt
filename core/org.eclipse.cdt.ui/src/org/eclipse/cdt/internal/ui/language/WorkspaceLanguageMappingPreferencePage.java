/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.language.LanguageMappingStore;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.util.Messages;

public class WorkspaceLanguageMappingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	WorkspaceLanguageConfiguration fMappings;
	WorkspaceLanguageMappingWidget fMappingWidget;
	
	public WorkspaceLanguageMappingPreferencePage() {
		fMappingWidget = new WorkspaceLanguageMappingWidget();
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.LANGUAGE_MAPPING_PREFERENCE_PAGE);
	}

	@Override
	protected Control createContents(Composite parent) {
		try {
			fetchMappings();
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return fMappingWidget.createContents(parent, PreferencesMessages.WorkspaceLanguagesPreferencePage_description);
	}
	
	private void fetchMappings() throws CoreException {
		fMappings = LanguageManager.getInstance().getWorkspaceLanguageConfiguration();
		
		Map<String, ILanguage> availableLanguages = LanguageVerifier.computeAvailableLanguages();
		Set<String> missingLanguages = LanguageVerifier.removeMissingLanguages(fMappings, availableLanguages);
		if (missingLanguages.size() > 0) {
			MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			messageBox.setText(PreferencesMessages.LanguageMappings_missingLanguageTitle);
			String affectedLanguages = LanguageVerifier.computeAffectedLanguages(missingLanguages);
			messageBox.setMessage(Messages.format(PreferencesMessages.WorkspaceLanguagesPreferencePage_missingLanguage, affectedLanguages));
			messageBox.open();
		}
		
		fMappingWidget.setMappings(fMappings.getWorkspaceMappings());
	}

	@Override
	public void init(IWorkbench workbench) {
	}
	
	@Override
	public boolean performOk() {
		try {
			if (!fMappingWidget.isChanged()) {
				return true;
			}
			
			IContentType[] affectedContentTypes = fMappingWidget.getAffectedContentTypes();
			LanguageManager manager = LanguageManager.getInstance();
			WorkspaceLanguageConfiguration config = manager.getWorkspaceLanguageConfiguration();
			config.setWorkspaceMappings(fMappingWidget.getContentTypeMappings());
			manager.storeWorkspaceLanguageConfiguration(affectedContentTypes);
			fMappingWidget.setChanged(false);
			return true;
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return false;
		}
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		// set to default
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID);
		node.remove(CCorePreferenceConstants.WORKSPACE_LANGUAGE_MAPPINGS);
		try {
			// remove workspace mappings
			Map<String,String> currentMappings= fMappings.getWorkspaceMappings();
			Set<String> keys = currentMappings.keySet();
			String[] contentTypeIds = keys.toArray(new String[keys.size()]);
			for (String contentTypeId : contentTypeIds) {
				fMappings.removeWorkspaceMapping(contentTypeId);
			}
			// add default mappings
			LanguageMappingStore store = new LanguageMappingStore();
			WorkspaceLanguageConfiguration defaultConfig = store.decodeWorkspaceMappings();
			Map<String,String> defaultMappings= defaultConfig.getWorkspaceMappings();
			for (String contentTypeId : defaultMappings.keySet()) {
				String language= defaultMappings.get(contentTypeId);
				fMappings.addWorkspaceMapping(contentTypeId, language);
			}
			fetchMappings();
			fMappingWidget.refreshMappings();
			fMappingWidget.setChanged(false);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
}
