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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.language.ProjectLanguageConfiguration;
import org.eclipse.cdt.core.model.LanguageManager;


import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;

public class ProjectLanguageMappingPropertyPage extends PropertyPage {

	private static final int MINIMUM_COLUMN_WIDTH = 150;
	private ProjectLanguageConfiguration fMappings;
	private Table fTable;
	private HashMap fContentTypeNamesToIDsMap;

	public ProjectLanguageMappingPropertyPage() {
		super();

		// keep a mapping of all registered content types and their names
		fContentTypeNamesToIDsMap = new HashMap();
		String[] contentTypesIDs = LanguageManager.getInstance()
				.getRegisteredContentTypeIds();

		IContentTypeManager contentTypeManager = Platform
				.getContentTypeManager();

		for (int i = 0; i < contentTypesIDs.length; i++) {

			String name = contentTypeManager.getContentType(contentTypesIDs[i])
					.getName();

			// keep track of what ID this name corresponds to so that when we
			// setup the mapping
			// later based upon user selection, we'll know what ID to use
			fContentTypeNamesToIDsMap.put(name, contentTypesIDs[i]);

		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		fetchMappings();

		Composite composite = new Composite(parent, SWT.NONE);
		composite
				.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		composite.setLayout(new GridLayout(2, false));

		Composite tableParent = new Composite(composite, SWT.NONE);
		tableParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fTable = new Table(tableParent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);

		TableColumn contentTypeColumn = new TableColumn(fTable, SWT.LEAD);
		contentTypeColumn
				.setText(PreferencesMessages.ProjectLanguagesPropertyPage_contentTypeColumn);

		TableColumn languageColumn = new TableColumn(fTable, SWT.LEAD);
		languageColumn
				.setText(PreferencesMessages.ProjectLanguagesPropertyPage_languageColumn);

		TableColumnLayout layout = new TableColumnLayout();
		layout.setColumnData(contentTypeColumn, new ColumnWeightData(1,
				MINIMUM_COLUMN_WIDTH, true));
		layout.setColumnData(languageColumn, new ColumnWeightData(1,
				MINIMUM_COLUMN_WIDTH, true));
		tableParent.setLayout(layout);

		Composite buttons = new Composite(composite, SWT.NONE);
		buttons.setLayout(new GridLayout());
		buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false,
				false));

		Button addButton = new Button(buttons, SWT.PUSH);
		addButton
				.setText(PreferencesMessages.ProjectLanguagesPropertyPage_addMappingButton);
		addButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				ContentTypeMappingDialog dialog = new ContentTypeMappingDialog(
						getShell());
				dialog.setContentTypeFilter(fMappings.getContentTypeMappings()
						.keySet());
				dialog.setBlockOnOpen(true);

				if (dialog.open() == Window.OK) {
					String contentType = dialog.getContentTypeID();
					String language = dialog.getLanguageID();
					fMappings.addContentTypeMapping(contentType, language);
					refreshMappings();
				}
			}
		});

		Button removeButton = new Button(buttons, SWT.PUSH);
		removeButton
				.setText(PreferencesMessages.ProjectLanguagesPropertyPage_removeMappingButton);
		removeButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event) {
				TableItem[] selection = fTable.getSelection();
				
				for (int i = 0; i < selection.length; i++) {
					fMappings
							.removeContentTypeMapping((String) fContentTypeNamesToIDsMap
									.get(selection[i].getText(0)));
				}
				
				refreshMappings();
			}
		});

		refreshMappings();
		return composite;
	}

	private void refreshMappings() {
		fTable.removeAll();
		Iterator mappings = fMappings.getContentTypeMappings().entrySet()
				.iterator();

		IContentTypeManager contentTypeManager = Platform
				.getContentTypeManager();

		while (mappings.hasNext()) {
			Entry entry = (Entry) mappings.next();

			TableItem item = new TableItem(fTable, SWT.NONE);

			String contentTypeName = contentTypeManager.getContentType(
					(String) entry.getKey()).getName();
			String languageName = LanguageManager.getInstance().getLanguage(
					(String) entry.getValue()).getName();

			item.setText(0, contentTypeName);
			item.setText(1, languageName);
		}
	}

	private void fetchMappings() {
		try {
			fMappings = LanguageManager.getInstance()
					.getLanguageConfiguration(getProject());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}

	protected void performDefaults() {
		fMappings = new ProjectLanguageConfiguration();
	}

	public boolean performOk() {
		try {
			IContentType[] affectedContentTypes = null;
			LanguageManager.getInstance().storeLanguageMappingConfiguration(
					getProject(), affectedContentTypes);
			return true;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return false;
		}
	}

	private IProject getProject() {
		return (IProject) getElement().getAdapter(IProject.class);
	}

}
