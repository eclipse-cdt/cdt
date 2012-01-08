/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.util.Messages;

public class ProjectLanguageMappingWidget extends LanguageMappingWidget {

	static final String CONTENT_TYPE_KEY_DELIMITER = "::"; //$NON-NLS-1$

	private static final String ALL_CONFIGURATIONS = ""; //$NON-NLS-1$

	private static final int CONFIGURATION_COLUMN = 0;

	private static final int CONTENT_TYPE_COLUMN = 1;

	private static final int LANGUAGE_COLUMN = 2;
	
	private Map<String, Map<String, String>> fConfigurationContentTypeMappings;

	public void setMappings(Map<String, Map<String, String>> contentTypeMappings) {
		fConfigurationContentTypeMappings = contentTypeMappings;
	}

	public Map<String, Map<String, String>> getContentTypeMappings() {
		return fConfigurationContentTypeMappings;
	}

	@Override
	public Composite createContents(Composite parent, String description) {
		fContents = new Composite(parent, SWT.NONE);
		fContents.setLayout(new GridLayout(2, false));

		if (description != null) {
			createHeader(parent, description);
		}

		Composite tableParent = new Composite(fContents, SWT.NONE);
		tableParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fTable = new Table(tableParent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);
		fTable.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = PreferencesMessages.ProjectLanguagesPropertyPage_mappingTableTitle;
			}
		});
		fTable.setToolTipText(PreferencesMessages.ProjectLanguagesPropertyPage_mappingTableTitle);

		TableColumn configurationColumn = new TableColumn(fTable, SWT.LEAD);
		configurationColumn.setText(PreferencesMessages.ProjectLanguagesPropertyPage_configurationColumn);
		
		TableColumn contentTypeColumn = new TableColumn(fTable, SWT.LEAD);
		contentTypeColumn.setText(PreferencesMessages.ProjectLanguagesPropertyPage_contentTypeColumn);

		TableColumn languageColumn = new TableColumn(fTable, SWT.LEAD);
		languageColumn.setText(PreferencesMessages.ProjectLanguagesPropertyPage_languageColumn);

		TableColumnLayout layout = new TableColumnLayout();
		layout.setColumnData(configurationColumn, new ColumnWeightData(1, MINIMUM_COLUMN_WIDTH, true));
		layout.setColumnData(contentTypeColumn, new ColumnWeightData(1, MINIMUM_COLUMN_WIDTH, true));
		layout.setColumnData(languageColumn, new ColumnWeightData(1, MINIMUM_COLUMN_WIDTH, true));
		tableParent.setLayout(layout);

		Composite buttons = new Composite(fContents, SWT.NONE);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

		Button addButton = new Button(buttons, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		addButton.setText(PreferencesMessages.ProjectLanguagesPropertyPage_addMappingButton);
		addButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				IProject project = (IProject) getElement().getAdapter(IProject.class);
				ICProjectDescription description = CoreModel.getDefault().getProjectDescription(project, false);
				ICConfigurationDescription[] configurations = description.getConfigurations();
				ProjectContentTypeMappingDialog dialog = new ProjectContentTypeMappingDialog(fContents.getShell(), configurations);
				
				dialog.setContentTypeFilter(createContentTypeFilter(fConfigurationContentTypeMappings));
				dialog.setBlockOnOpen(true);

				if (dialog.open() == Window.OK) {
					String contentType = dialog.getContentTypeID();
					String language = dialog.getLanguageID();
					String configuration = dialog.getConfigurationID();
					if (configuration == null) {
						configuration = ALL_CONFIGURATIONS;
					}
					Map<String, String> contentTypeMappings = fConfigurationContentTypeMappings.get(configuration);
					if (contentTypeMappings == null) {
						contentTypeMappings = new TreeMap<String, String>();
						fConfigurationContentTypeMappings.put(configuration, contentTypeMappings);
					}
					contentTypeMappings.put(contentType, language);
					setChanged(true);
					
					IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
					fAffectedContentTypes.add(contentTypeManager.getContentType(contentType));
					refreshMappings();
				}
			}
		});

		Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		removeButton.setText(PreferencesMessages.ProjectLanguagesPropertyPage_removeMappingButton);
		removeButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				TableItem[] selection = fTable.getSelection();

				for (int i = 0; i < selection.length; i++) {
					LanguageTableData data = (LanguageTableData) selection[i].getData();
					String contentType = data.contentTypeId;

					String configurationId;
					if (data.configuration == null) {
						configurationId = ALL_CONFIGURATIONS;
					} else {
						configurationId = data.configuration.getId();
					}
					
					Map<String, String> contentTypeMappings = fConfigurationContentTypeMappings.get(configurationId);
					if (contentTypeMappings != null) {
						contentTypeMappings.remove(contentType);
					}

					IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
					fAffectedContentTypes.add(contentTypeManager.getContentType(contentType));
				}
				
				if (selection.length > 0) {
					setChanged(true);
				}

				refreshMappings();
			}
		});

		refreshMappings();
		return fContents;
	}

	private Set<String> createContentTypeFilter(Map<String, Map<String, String>> mappings) {
		Set<String> filter = new HashSet<String>();
		Iterator<Entry<String, Map<String, String>>> configurationContentTypeMappings = mappings.entrySet().iterator();
		while (configurationContentTypeMappings.hasNext()) {
			Entry<String, Map<String, String>> entry = configurationContentTypeMappings.next();
			String configuration = entry.getKey();
			Iterator<Entry<String, String>> contentTypeMappings = entry.getValue().entrySet().iterator();
			while (contentTypeMappings.hasNext()) {
				Entry<String, String> contentTypeEntry = contentTypeMappings.next();
				String contentType = contentTypeEntry.getKey();
				filter.add(createFilterKey(configuration, contentType));
			}
		}
		return filter;
	}
	
	@Override
	public void refreshMappings() {
		if (fTable == null) {
			return;
		}
		
		fTable.removeAll();
		Iterator<Entry<String, Map<String, String>>> mappings = fConfigurationContentTypeMappings.entrySet().iterator();

		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();

		IProject project = (IProject) getElement().getAdapter(IProject.class);
		ICProjectDescription description = CoreModel.getDefault().getProjectDescription(project, false);

		while (mappings.hasNext()) {
			Entry<String, Map<String, String>> configurationEntry = mappings.next();
			String configurationId = configurationEntry.getKey();
			Iterator<Entry<String, String>> contentTypeMappings = configurationEntry.getValue().entrySet().iterator();
			while (contentTypeMappings.hasNext()) {
				Entry<String, String> entry = contentTypeMappings.next();
				TableItem item = new TableItem(fTable, SWT.NONE);
	
				String contentType = entry.getKey();
				String contentTypeName = contentTypeManager.getContentType(contentType).getName();
				String languageId = entry.getValue();
				String languageName = LanguageManager.getInstance().getLanguage(languageId).getName();
				
				ICConfigurationDescription configuration = description.getConfigurationById(configurationId);
				
				item.setData(new LanguageTableData(configuration, contentType, languageId));
				
				if (configuration == null) {
					item.setText(CONFIGURATION_COLUMN, PreferencesMessages.ContentTypeMappingsDialog_allConfigurations);
				} else {
					item.setText(CONFIGURATION_COLUMN, configuration.getName());
				}
				
				if (fOverriddenContentTypes.contains(contentType)) {
					item.setText(CONTENT_TYPE_COLUMN, Messages.format(PreferencesMessages.ProjectLanguagesPropertyPage_overriddenContentType, contentTypeName));
					item.setFont(fOverriddenFont);
				} else {
					item.setText(CONTENT_TYPE_COLUMN, contentTypeName);
				}
				item.setText(LANGUAGE_COLUMN, languageName);
			}
		}
		
		if (fChild != null) {
			Set<String> overrides = new HashSet<String>(createWorkspaceContentTypeFilter(fConfigurationContentTypeMappings));
			fChild.setOverriddenContentTypes(overrides);
			fChild.refreshMappings();
		}
	}
	
	private Set<String> createWorkspaceContentTypeFilter(Map<String, Map<String, String>> configurationContentTypeMappings) {
		Map<String, String> contentTypeMappings = configurationContentTypeMappings.get(ALL_CONFIGURATIONS);
		if (contentTypeMappings == null) {
			return Collections.emptySet();
		}
		return contentTypeMappings.keySet();
	}

	static String createFilterKey(String configurationId, String contentTypeId) {
		return configurationId + CONTENT_TYPE_KEY_DELIMITER + contentTypeId;
	}
	
	private static class LanguageTableData {
		ICConfigurationDescription configuration;
		String contentTypeId;
		
		LanguageTableData(ICConfigurationDescription configuration, String contentTypeId, String languageId) {
			this.configuration = configuration;
			this.contentTypeId = contentTypeId;
		}
	}
}
