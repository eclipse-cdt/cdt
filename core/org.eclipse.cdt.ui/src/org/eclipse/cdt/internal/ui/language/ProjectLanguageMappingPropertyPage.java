/*******************************************************************************
 *  Copyright (c) 2007, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.language.WorkspaceLanguageConfiguration;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ILanguageMappingChangeEvent;
import org.eclipse.cdt.core.model.ILanguageMappingChangeListener;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.util.Messages;

public class ProjectLanguageMappingPropertyPage extends PropertyPage {

	private ProjectLanguageMappingWidget fMappingWidget;
	private WorkspaceLanguageMappingWidget fInheritedMappingWidget;
	private ProjectLanguageConfiguration fMappings;
	private ILanguageMappingChangeListener fInheritedMappingsChangeListener;
	
	public ProjectLanguageMappingPropertyPage() {
		super();
		fMappingWidget = new ProjectLanguageMappingWidget();
		
		fInheritedMappingWidget = new WorkspaceLanguageMappingWidget();
		fInheritedMappingWidget.setReadOnly(true);
		
		fMappingWidget.setChild(fInheritedMappingWidget);
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		fMappingWidget.setElement(getProject());
		
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayout(new GridLayout(1, false));
		
		fetchMappings(getProject());
		Composite contentTypeMappings = fMappingWidget.createContents(contents, PreferencesMessages.ProjectLanguagesPropertyPage_description);
		contentTypeMappings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group group = new Group(contents, SWT.SHADOW_IN);
		group.setText(PreferencesMessages.ProjectLanguagesPropertyPage_inheritedWorkspaceMappingsGroup);
		group.setLayout(new FillLayout());
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fetchWorkspaceMappings();
		fInheritedMappingWidget.createContents(group, null);
		fInheritedMappingsChangeListener = new ILanguageMappingChangeListener() {
			@Override
			public void handleLanguageMappingChangeEvent(final ILanguageMappingChangeEvent event) {
				if (event.getType() == ILanguageMappingChangeEvent.TYPE_WORKSPACE) {
					if (ProjectLanguageMappingPropertyPage.this.isControlCreated()) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (!ProjectLanguageMappingPropertyPage.this.getControl().isDisposed()) {
									fetchWorkspaceMappings();
									fInheritedMappingWidget.refreshMappings();
								}
							}
						});
					}
				}
				else if (event.getType() == ILanguageMappingChangeEvent.TYPE_PROJECT) {
					if (ProjectLanguageMappingPropertyPage.this.isControlCreated()) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (!ProjectLanguageMappingPropertyPage.this.getControl().isDisposed()) {
									fetchMappings(event.getProject());
									fMappingWidget.refreshMappings();
								}
							}
						});
					}
				}
			}
		};
		LanguageManager.getInstance().registerLanguageChangeListener(fInheritedMappingsChangeListener);
		
		return contents;
	}

	private void fetchMappings(IProject project) {
		try {
			LanguageManager manager = LanguageManager.getInstance();
			fMappings = manager.getLanguageConfiguration(project);
			
			ICProjectDescription description = CoreModel.getDefault().getProjectDescription(project);
			Map<String, ILanguage> availableLanguages = LanguageVerifier.computeAvailableLanguages();
			Set<String> missingLanguages = LanguageVerifier.removeMissingLanguages(fMappings, description, availableLanguages);
			if (missingLanguages.size() > 0) {
				MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
				messageBox.setText(PreferencesMessages.LanguageMappings_missingLanguageTitle);
				String affectedLanguages = LanguageVerifier.computeAffectedLanguages(missingLanguages);
				messageBox.setMessage(Messages.format(PreferencesMessages.ProjectLanguagesPropertyPage_missingLanguage, affectedLanguages));
				messageBox.open();
			}

			fMappingWidget.setMappings(fMappings.getContentTypeMappings());
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
	
	private void fetchWorkspaceMappings() {
		try {
			LanguageManager manager = LanguageManager.getInstance();
			WorkspaceLanguageConfiguration workspaceMappings = manager.getWorkspaceLanguageConfiguration();

			Map<String, ILanguage> availableLanguages = LanguageVerifier.computeAvailableLanguages();
			Set<String> missingLanguages = LanguageVerifier.removeMissingLanguages(workspaceMappings, availableLanguages);
			if (missingLanguages.size() > 0) {
				MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
				messageBox.setText(PreferencesMessages.LanguageMappings_missingLanguageTitle);
				String affectedLanguages = LanguageVerifier.computeAffectedLanguages(missingLanguages);
				messageBox.setMessage(Messages.format(PreferencesMessages.WorkspaceLanguagesPreferencePage_missingLanguage, affectedLanguages));
				messageBox.open();
			}

			fInheritedMappingWidget.setMappings(workspaceMappings.getWorkspaceMappings());
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		fetchMappings(getProject());
		fMappingWidget.refreshMappings();
	}

	@Override
	public boolean performOk() {
		try {
			if (!fMappingWidget.isChanged()) {
				return true;
			}
			
			fMappings.setContentTypeMappings(fMappingWidget.getContentTypeMappings());
			IContentType[] affectedContentTypes = fMappingWidget.getAffectedContentTypes();
			LanguageManager.getInstance().storeLanguageMappingConfiguration(getProject(), affectedContentTypes);
			fMappingWidget.setChanged(false);
			return true;
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return false;
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		LanguageManager.getInstance().unregisterLanguageChangeListener(fInheritedMappingsChangeListener);
	}

	private IProject getProject() {
		return (IProject) getElement().getAdapter(IProject.class);
	}
}
