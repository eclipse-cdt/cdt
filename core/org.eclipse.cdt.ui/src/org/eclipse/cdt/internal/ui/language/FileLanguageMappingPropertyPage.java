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
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.CContentTypes;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.language.LanguageMapping;
import org.eclipse.cdt.internal.core.language.LanguageMappingResolver;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.util.Messages;

public class FileLanguageMappingPropertyPage extends PropertyPage {

	private static final int MINIMUM_COLUMN_WIDTH = 150;
	private static final int LANGUAGE_COLUMN = 1;
	private static final int CONFIGURATION_COLUMN = 0;
	
	private static final int LANGUAGE_ID = 0;
	private static final int LANGUAGE_NAME = 1;
	
	private static final String ALL_CONFIGURATIONS = ""; //$NON-NLS-1$
	
	private IContentType fContentType;
	private Composite fContents;
	private Table fTable;
	private ILanguage[] fLanguages;
	private Map<String, ILanguage> fLanguageIds;
	private boolean fHasChanges;
	
	public FileLanguageMappingPropertyPage() {
		super();
		fLanguages = LanguageManager.getInstance().getRegisteredLanguages();
		fLanguageIds = LanguageVerifier.computeAvailableLanguages();
	}
	
	@Override
	protected Control createContents(Composite parent) {
		IFile file = getFile();
		IProject project = file.getProject();
		fContentType = CContentTypes.getContentType(project, file.getName());
		
		fContents = new Composite(parent, SWT.NONE);
		fContents.setLayout(new GridLayout(2, false));

		Label contentTypeLabel = new Label(fContents, SWT.NONE);
		contentTypeLabel.setText(PreferencesMessages.FileLanguagesPropertyPage_contentTypeLabel);
		contentTypeLabel.setLayoutData(new GridData(SWT.TRAIL, SWT.CENTER, false, false));
		
		Label contentTypeDescriptionLabel = new Label(fContents, SWT.NONE);
		contentTypeDescriptionLabel.setText(fContentType.getName());
		contentTypeDescriptionLabel.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
		
		try {
			createMappingTable(fContents, file);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		
		Link link = new Link(fContents, SWT.NONE);
		link.setText(PreferencesMessages.FileLanguagesPropertyPage_description);
		link.addListener(SWT.Selection, new LanguageMappingLinkListener(parent.getShell(), project) {
			@Override
			protected void refresh() {
				try {
					refreshMappings();
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		});
		
		link.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false, 2, 1));
		
		fContents.pack();
		return fContents;
	}

	private void createMappingTable(Composite contents, final IFile file) throws CoreException {
		Composite tableParent = new Composite(contents, SWT.NONE);
		tableParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		fTable = new Table(tableParent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);
		fTable.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = PreferencesMessages.FileLanguagesPropertyPage_mappingTableTitle;
			}
		});
		fTable.setToolTipText(PreferencesMessages.FileLanguagesPropertyPage_mappingTableTitle);

		TableColumn contentTypeColumn = new TableColumn(fTable, SWT.LEAD);
		contentTypeColumn.setText(PreferencesMessages.FileLanguagesPropertyPage_configurationColumn);

		TableColumn languageColumn = new TableColumn(fTable, SWT.LEAD);
		languageColumn.setText(PreferencesMessages.ProjectLanguagesPropertyPage_languageColumn);

		TableColumnLayout layout = new TableColumnLayout();
		layout.setColumnData(contentTypeColumn, new ColumnWeightData(1, MINIMUM_COLUMN_WIDTH, true));
		layout.setColumnData(languageColumn, new ColumnWeightData(1, MINIMUM_COLUMN_WIDTH, true));
		tableParent.setLayout(layout);
		
		final TableEditor editor = new TableEditor(fTable);
		editor.grabHorizontal = true;
		editor.grabVertical = true;
		editor.setColumn(LANGUAGE_COLUMN);
		
		final IProject project = file.getProject();

		fTable.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Control oldEditor = editor.getEditor();
				if (oldEditor != null) {
					oldEditor.dispose();
				}
				
				TableItem item = (TableItem) event.item;
				if (item == null) {
					return;
				}

				LanguageTableData data = (LanguageTableData) item.getData();
				CCombo newEditor = new CCombo(fTable, SWT.READ_ONLY);
				populateLanguages(project, file, data.configuration, data.languageId, newEditor);
				
				newEditor.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						CCombo combo = (CCombo) editor.getEditor();
						int index = combo.getSelectionIndex();
						if (index != -1) {
							TableItem item = editor.getItem();
							item.setText(LANGUAGE_COLUMN, combo.getText());
							
							String selectedLanguage = ((String[]) combo.getData())[index];
							LanguageTableData data = (LanguageTableData) item.getData();
							data.languageId = selectedLanguage;
							fHasChanges = true;
							
							try {
								refreshMappings();
							} catch (CoreException e) {
								CUIPlugin.log(e);
							}
						}
					}
				});
				
				newEditor.setFocus();
				editor.setEditor(newEditor, item, LANGUAGE_COLUMN);
			}
		});
		
		populateLanguageTable(fTable);
		refreshMappings();
	}

	private void populateLanguageTable(Table table) throws CoreException {
		IFile file = getFile();
		IProject project = file.getProject();
		ICProjectDescription description = CoreModel.getDefault().getProjectDescription(project);
		ICConfigurationDescription[] configurations = description.getConfigurations();
		
		TableItem defaultItem = new TableItem(table, SWT.NONE);
		defaultItem.setText(CONFIGURATION_COLUMN, PreferencesMessages.FileLanguagesPropertyPage_defaultMapping);
		
		ProjectLanguageConfiguration config = LanguageManager.getInstance().getLanguageConfiguration(project);
		
		Set<String> missingLanguages = LanguageVerifier.removeMissingLanguages(config, description, fLanguageIds);
		if (missingLanguages.size() > 0) {
			MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
			messageBox.setText(PreferencesMessages.LanguageMappings_missingLanguageTitle);
			String affectedLanguages = LanguageVerifier.computeAffectedLanguages(missingLanguages);
			messageBox.setMessage(Messages.format(PreferencesMessages.FileLanguagesPropertyPage_missingLanguage, affectedLanguages));
			messageBox.open();
		}
		
		String defaultLanguageId = config.getLanguageForFile(null, file);
		LanguageTableData defaultData = new LanguageTableData(null, defaultLanguageId );
		defaultItem.setData(defaultData);
		
		for (int i = 0; i < configurations.length; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(CONFIGURATION_COLUMN, configurations[i].getName());
			String languageId = config.getLanguageForFile(configurations[i], file);
			
			if (languageId != null) {
				ILanguage language = fLanguageIds.get(languageId);
				String languageName =  language.getName();
				item.setText(LANGUAGE_COLUMN, languageName);
			}
			
			LanguageTableData data = new LanguageTableData(configurations[i], languageId);
			item.setData(data);
		}
	}

	private void populateLanguages(IProject project, IFile file, ICConfigurationDescription configuration, String selectedLanguage, CCombo combo) {
		try {
			String[][] languageInfo = getLanguages(project, file, configuration);
			combo.setItems(languageInfo[LANGUAGE_NAME]);
			combo.setData(languageInfo[LANGUAGE_ID]);
			
			findSelection(configuration, selectedLanguage, combo);
			fContents.layout();
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
	
	private void refreshMappings() throws CoreException {
		IFile file = getFile();
		IProject project = file.getProject();
		
		LanguageManager manager = LanguageManager.getInstance();
		TableItem[] items = fTable.getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			LanguageTableData data = (LanguageTableData) item.getData();
			if (data.languageId == null) {
				LanguageMapping mapping = computeInheritedMapping(project, file, data.configuration);
				item.setText(LANGUAGE_COLUMN, computeInheritedFrom(data.configuration, mapping));
			} else {
				ILanguage language = manager.getLanguage(data.languageId);
				item.setText(LANGUAGE_COLUMN, language.getName());
			}
		}
	}

	private void findSelection(ICConfigurationDescription configuration, String languageId, CCombo combo) throws CoreException {
//		if (languageId == null) {
//			TableItem[] items = fTable.getItems();
//			for (int i = 0; i < items.length; i++) {
//				LanguageTableData data = (LanguageTableData) items[i].getData();
//				if (configuration == null && data.configuration == null) {
//					languageId = data.languageId;
//					break;
//				} else if (configuration != null && data.configuration != null) {
//					languageId = data.languageId;
//					break;
//				}
//			}
//		}
		
		if (languageId == null) {
			// No mapping was defined so we'll choose the default.
			combo.select(0);
			return;
		}
		
		LanguageManager manager = LanguageManager.getInstance();
		ILanguage language = manager.getLanguage(languageId);
		String name = language.getName();
		
		for (int i = 1; i < combo.getItemCount(); i++) {
			if (name.equals(combo.getItem(i))) {
				combo.select(i);
				return;
			}
		}
		
		// Couldn't find the mapping so we'll choose the default.
		combo.select(0);
	}

	@Override
	public boolean performOk() {
		try {
			if (!fHasChanges) {
				return true;
			}
			
			IFile file = getFile();
			IProject project = file.getProject();
			LanguageManager manager = LanguageManager.getInstance();
			ProjectLanguageConfiguration config = manager.getLanguageConfiguration(project);
			
			Map<String, String> mappings = new TreeMap<String, String>();
			TableItem[] items = fTable.getItems();
			for (int i = 0; i < items.length; i++) {
				TableItem item = items[i];
				LanguageTableData data = (LanguageTableData) item.getData();
				if (data.languageId == null) {
					continue;
				}
				String configurationId;
				if (data.configuration == null) {
					configurationId = ALL_CONFIGURATIONS;
				} else {
					configurationId = data.configuration.getId();
				}
				mappings.put(configurationId, data.languageId);
			}
			config.setFileMappings(file, mappings);
			manager.storeLanguageMappingConfiguration(file);
			fHasChanges = false;
			return true;
		} catch (CoreException e) {
			CUIPlugin.log(e);
			return false;
		}
	}
	
	private IFile getFile() {
		return (IFile) getElement().getAdapter(IFile.class);
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}
	
	private String computeInheritedFrom(ICConfigurationDescription configuration, LanguageMapping mapping) throws CoreException {
		String inheritedFrom;
		ILanguage language;
		
		LanguageTableData data = (LanguageTableData) fTable.getItem(0).getData();
		if (configuration != null && data.languageId != null) {
			inheritedFrom = PreferencesMessages.FileLanguagesPropertyPage_inheritedFromFile;
			language = LanguageManager.getInstance().getLanguage(data.languageId);
		} else {
			language = mapping.language;
			switch (mapping.inheritedFrom) {
			case LanguageMappingResolver.DEFAULT_MAPPING:
				inheritedFrom = PreferencesMessages.FileLanguagesPropertyPage_inheritedFromSystem;
				break;
			case LanguageMappingResolver.PROJECT_MAPPING:
				inheritedFrom = PreferencesMessages.FileLanguagesPropertyPage_inheritedFromProject;
				break;
			case LanguageMappingResolver.WORKSPACE_MAPPING:
				inheritedFrom = PreferencesMessages.FileLanguagesPropertyPage_inheritedFromWorkspace;
				break;
			default:
				throw new CoreException(Util.createStatus(new IllegalArgumentException()));
			}
		}
		return Messages.format(inheritedFrom, language.getName());
	}
	
	private LanguageMapping computeInheritedMapping(IProject project, IFile file, ICConfigurationDescription configuration) throws CoreException {
		LanguageMapping mappings[] = LanguageMappingResolver.computeLanguage(project, file.getProjectRelativePath().toPortableString(), configuration, fContentType.getId(), true);
		LanguageMapping inheritedMapping = mappings[0];
		
		// Skip over the file mapping because we want to know what mapping the file
		// mapping overrides.
		if (inheritedMapping.inheritedFrom == LanguageMappingResolver.FILE_MAPPING ) {
			inheritedMapping = mappings[1];
		}
		
		return inheritedMapping;
	}
	
	private String[][] getLanguages(IProject project, IFile file, ICConfigurationDescription configuration) throws CoreException {
		String[][] descriptions = new String[2][fLanguages.length + 1];
		
		LanguageMapping inheritedMapping = computeInheritedMapping(project, file, configuration);
		
		int index = 0;
		descriptions[LANGUAGE_ID][index] = null;
		descriptions[LANGUAGE_NAME][index] = computeInheritedFrom(configuration, inheritedMapping);

		index++;
		for (int i = 0; i < fLanguages.length; i++) {
			descriptions[LANGUAGE_ID][index] = fLanguages[i].getId();
			descriptions[LANGUAGE_NAME][index] = fLanguages[i].getName();
			index++;
		}
		return descriptions;
	}
	
	private static class LanguageTableData {
		ICConfigurationDescription configuration;
		String languageId;
		
		LanguageTableData(ICConfigurationDescription configuration, String languageId) {
			this.configuration = configuration;
			this.languageId = languageId;
		}
	}
}

