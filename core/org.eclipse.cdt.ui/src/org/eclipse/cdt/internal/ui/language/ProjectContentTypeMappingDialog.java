/*******************************************************************************
 *  Copyright (c) 2007, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *   IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;

public class ProjectContentTypeMappingDialog extends ContentTypeMappingDialog {
	
	private Combo fConfiguration;
	private ICConfigurationDescription[] fConfigurations;
	private String[] fContentTypesIDs;
	private Set<String> fFilteredContentTypes;

	public ProjectContentTypeMappingDialog(Shell parentShell, ICConfigurationDescription[] configurations) {
		super(parentShell);
		fConfigurations = configurations;
		
		fContentTypesIDs = LanguageManager.getInstance().getRegisteredContentTypeIds();
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();

		for (int i = 0; i < fContentTypesIDs.length; i++) {
			String name = contentTypeManager.getContentType(fContentTypesIDs[i]).getName();
			
			// keep track of what ID this name corresponds to so that when
			// we setup the mapping
			// later based upon user selection, we'll know what ID to use
			fContentTypeNamesToIDsMap.put(name, fContentTypesIDs[i]);
		}
	}

	private void configureConfigurations(Combo combo) {
		combo.add(PreferencesMessages.ContentTypeMappingsDialog_allConfigurations);
		for (int i = 0; i < fConfigurations.length; i++) {
			combo.add(fConfigurations[i].getName());
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(2, false));

		Label configurationLabel = new Label(area, SWT.TRAIL);
		configurationLabel.setText(PreferencesMessages.ContentTypeMappingsDialog_configuration);
		fConfiguration = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
		fConfiguration.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		configureConfigurations(fConfiguration);
		fConfiguration.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int index = fConfiguration.getSelectionIndex();
				if (index <= 0) {
					fSelectedConfigurationName = null;
					fSelectedConfigurationID = null;
					configureContentTypes(fContentType, null);
					getButton(IDialogConstants.OK_ID).setEnabled(false);
					return;
				}
				
				// Shift index by one because of "All configurations" entry. 
				int configurationIndex = index - 1;
				ICConfigurationDescription configuration = fConfigurations[configurationIndex];
				
				fSelectedConfigurationName = configuration.getName();
				fSelectedConfigurationID = configuration.getId();
				configureContentTypes(fContentType, configuration);

				getButton(IDialogConstants.OK_ID).setEnabled(isValidSelection());
			}
		});
		fConfiguration.select(0);
		
		Label contentTypeLabel = new Label(area, SWT.TRAIL);
		contentTypeLabel.setText(PreferencesMessages.ContentTypeMappingsDialog_contentType);

		fContentType = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
		fContentType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fContentType.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				fSelectedContentTypeName = fContentType.getText();
				fSelectedContentTypeID = fContentTypeNamesToIDsMap.get(fSelectedContentTypeName);
				getButton(IDialogConstants.OK_ID).setEnabled(isValidSelection());
			}
		});
		configureContentTypes(fContentType, null);

		Label languageLabel = new Label(area, SWT.TRAIL);
		languageLabel.setText(PreferencesMessages.ContentTypeMappingsDialog_language);

		fLanguage = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
		fLanguage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fLanguage.setItems(getLanguages());
		fLanguage.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				fSelectedLanguageName = fLanguage.getText();
				fSelectedLanguageID = fLanguageNamesToIDsMap.get(fSelectedLanguageName);
				getButton(IDialogConstants.OK_ID).setEnabled(isValidSelection());
			}
		});

		return area;
	}
	
	private void configureContentTypes(Combo combo, ICConfigurationDescription configuration) {
		combo.removeAll();
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
		List<String> names = new LinkedList<String>();
		
		for (int i = 0; i < fContentTypesIDs.length; i++) {
			String contentTypeId = fContentTypesIDs[i];
			String name = contentTypeManager.getContentType(contentTypeId).getName();
			
			if (configuration != null) {
				String key = ProjectLanguageMappingWidget.createFilterKey(configuration.getId(), contentTypeId);
				if (!fFilteredContentTypes.contains(key)) {
					names.add(name);
				}
			} else {
				names.add(name);
			}
		}
		
		Collections.sort(names);
		for(String name : names) {
			combo.add(name);
		}
	}

	@Override
	protected boolean isValidSelection() {
		return fContentType.getSelectionIndex() != -1 && fLanguage.getSelectionIndex() != -1 && fConfiguration.getSelectionIndex() != -1;
	}

	public void setContentTypeFilter(Set<String> contentTypeFilter) {
		fFilteredContentTypes = contentTypeFilter;
	}

	public String getConfigurationID() {
		return fSelectedConfigurationID;
	}
}
