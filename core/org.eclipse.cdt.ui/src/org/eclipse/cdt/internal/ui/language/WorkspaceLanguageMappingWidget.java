/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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

import org.eclipse.cdt.core.model.LanguageManager;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.util.Messages;

public class WorkspaceLanguageMappingWidget extends LanguageMappingWidget {
	private Map<String, String> fContentTypeMappings;
	
	public WorkspaceLanguageMappingWidget() {
		super();
		fContentTypeMappings = new TreeMap<String, String>();
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
				e.result = PreferencesMessages.WorkspaceLanguagesPreferencePage_mappingTableTitle;
			}
		});
		fTable.setToolTipText(PreferencesMessages.WorkspaceLanguagesPreferencePage_mappingTableTitle);
		

		TableColumn contentTypeColumn = new TableColumn(fTable, SWT.LEAD);
		contentTypeColumn.setText(PreferencesMessages.ProjectLanguagesPropertyPage_contentTypeColumn);

		TableColumn languageColumn = new TableColumn(fTable, SWT.LEAD);
		languageColumn.setText(PreferencesMessages.ProjectLanguagesPropertyPage_languageColumn);

		TableColumnLayout layout = new TableColumnLayout();
		layout.setColumnData(contentTypeColumn, new ColumnWeightData(1, MINIMUM_COLUMN_WIDTH, true));
		layout.setColumnData(languageColumn, new ColumnWeightData(1, MINIMUM_COLUMN_WIDTH, true));
		tableParent.setLayout(layout);

		if (!fIsReadOnly) {
			Composite buttons = new Composite(fContents, SWT.NONE);
			buttons.setLayout(new GridLayout());
			buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
	
			Button addButton = new Button(buttons, SWT.PUSH);
			addButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			addButton.setText(PreferencesMessages.ProjectLanguagesPropertyPage_addMappingButton);
			addButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					WorkspaceContentTypeMappingDialog dialog = new WorkspaceContentTypeMappingDialog(fContents.getShell());
					dialog.setContentTypeFilter(fContentTypeMappings.keySet());
					dialog.setBlockOnOpen(true);
	
					if (dialog.open() == Window.OK) {
						String contentType = dialog.getContentTypeID();
						String language = dialog.getLanguageID();
						fContentTypeMappings.put(contentType, language);
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
						String contentType = fContentTypeNamesToIDsMap.get(selection[i].getText(0));
	
						fContentTypeMappings.remove(contentType);
	
						IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
						fAffectedContentTypes.add(contentTypeManager.getContentType(contentType));
					}
					
					if (selection.length > 0) {
						setChanged(true);
					}
	
					refreshMappings();
				}
			});
		}

		refreshMappings();
		return fContents;
	}

	@Override
	public void refreshMappings() {
		if (fTable == null) {
			return;
		}
		
		fTable.removeAll();
		Iterator<Entry<String, String>> mappings = fContentTypeMappings.entrySet().iterator();

		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();

		while (mappings.hasNext()) {
			Entry<String, String> entry = mappings.next();

			TableItem item = new TableItem(fTable, SWT.NONE);

			String contentType = entry.getKey();
			String contentTypeName = contentTypeManager.getContentType(contentType).getName();
			String languageName = LanguageManager.getInstance().getLanguage(entry.getValue()).getName();

			if (fOverriddenContentTypes.contains(contentType)) {
				item.setText(0, Messages.format(PreferencesMessages.ProjectLanguagesPropertyPage_overriddenContentType, contentTypeName));
				item.setFont(fOverriddenFont);
			} else {
				item.setText(0, contentTypeName);
			}
			item.setText(1, languageName);
		}
		
		if (fChild != null) {
			Set<String> overrides = new HashSet<String>(fContentTypeMappings.keySet());
			overrides.addAll(fOverriddenContentTypes);
			fChild.setOverriddenContentTypes(overrides);
			fChild.refreshMappings();
		}
	}
	
	public void setMappings(Map<String, String> mappings) {
		fContentTypeMappings = new TreeMap<String, String>(mappings);
	}

	public Map<String, String> getContentTypeMappings() {
		return Collections.unmodifiableMap(fContentTypeMappings);
	}
}
