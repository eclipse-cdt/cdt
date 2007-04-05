/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.core.model.LanguageManager;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;

public class WorkspaceLanguageMappingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	WorkspaceLanguageConfiguration fMappings;
	LanguageMappingWidget fMappingWidget;
	
	public WorkspaceLanguageMappingPreferencePage() {
		fMappingWidget = new LanguageMappingWidget();
	}
	
	protected Control createContents(Composite parent) {
		try {
			fMappings = LanguageManager.getInstance().getWorkspaceLanguageConfiguration();
			fMappingWidget.setMappings(fMappings.getWorkspaceMappings());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return fMappingWidget.createContents(parent, PreferencesMessages.WorkspaceLanguagesPreferencePage_description);
	}
	
	public void init(IWorkbench workbench) {
	}
	
	public boolean performOk() {
		try {
			IContentType[] affectedContentTypes = fMappingWidget.getAffectedContentTypes();
			LanguageManager manager = LanguageManager.getInstance();
			WorkspaceLanguageConfiguration config = manager.getWorkspaceLanguageConfiguration();
			config.setWorkspaceMappings(fMappingWidget.getContentTypeMappings());
			manager.storeWorkspaceLanguageConfiguration(affectedContentTypes);
			return true;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}
	
	protected void performDefaults() {
		super.performDefaults();
		try {
			LanguageManager manager = LanguageManager.getInstance();
			WorkspaceLanguageConfiguration config = manager.getWorkspaceLanguageConfiguration();
			fMappingWidget.setMappings(config.getWorkspaceMappings());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
}
