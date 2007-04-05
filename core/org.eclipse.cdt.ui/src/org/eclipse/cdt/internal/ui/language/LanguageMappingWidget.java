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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.cdt.core.model.LanguageManager;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.cdt.internal.ui.util.Messages;

public class LanguageMappingWidget {

	private static final int MINIMUM_COLUMN_WIDTH = 150;
	private Table fTable;
	private HashMap fContentTypeNamesToIDsMap;
	private Set fAffectedContentTypes;
	private Composite fContents;
	private Map fContentTypeMappings;
	private boolean fIsReadOnly;
	private Set fOverriddenContentTypes;
	private Font fOverriddenFont;
	private LanguageMappingWidget fChild;
	private IAdaptable fElement;
	
	public LanguageMappingWidget() {
		fOverriddenFont = JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
		fOverriddenContentTypes = Collections.EMPTY_SET;
		
		// keep a mapping of all registered content types and their names
		fContentTypeNamesToIDsMap = new HashMap();
		String[] contentTypesIDs = LanguageManager.getInstance().getRegisteredContentTypeIds();

		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();

		for (int i = 0; i < contentTypesIDs.length; i++) {

			String name = contentTypeManager.getContentType(contentTypesIDs[i]).getName();

			// keep track of what ID this name corresponds to so that when we
			// setup the mapping
			// later based upon user selection, we'll know what ID to use
			fContentTypeNamesToIDsMap.put(name, contentTypesIDs[i]);
		}

		fContentTypeMappings = new TreeMap();
		fAffectedContentTypes = new HashSet();
	}

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
			addButton.setText(PreferencesMessages.ProjectLanguagesPropertyPage_addMappingButton);
			addButton.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					ContentTypeMappingDialog dialog = new ContentTypeMappingDialog(fContents.getShell());
					dialog.setContentTypeFilter(fContentTypeMappings.keySet());
					dialog.setBlockOnOpen(true);
	
					if (dialog.open() == Window.OK) {
						String contentType = dialog.getContentTypeID();
						String language = dialog.getLanguageID();
						fContentTypeMappings.put(contentType, language);
						IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
						fAffectedContentTypes.add(contentTypeManager.getContentType(contentType));
						refreshMappings();
					}
				}
			});
	
			Button removeButton = new Button(buttons, SWT.PUSH);
			removeButton.setText(PreferencesMessages.ProjectLanguagesPropertyPage_removeMappingButton);
			removeButton.addListener(SWT.Selection, new Listener() {
	
				public void handleEvent(Event event) {
					TableItem[] selection = fTable.getSelection();
	
					for (int i = 0; i < selection.length; i++) {
						String contentType = (String) fContentTypeNamesToIDsMap.get(selection[i].getText(0));
	
						fContentTypeMappings.remove(contentType);
	
						IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
						fAffectedContentTypes.add(contentTypeManager.getContentType(contentType));
					}
	
					refreshMappings();
				}
			});
		}

		refreshMappings();
		return fContents;
	}

	private void createHeader(Composite parent, String description) {
		Link link = new Link(fContents, SWT.NONE);
		link.setText(description);

		link.addListener(SWT.Selection, new LanguageMappingLinkListener(fContents.getShell(), getElement()) {
			protected void refresh() {
				refreshMappings();
			}
		});

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		link.setLayoutData(gridData);
	}

	public IAdaptable getElement() {
		return fElement;
	}
	
	public void setElement(IAdaptable element) {
		fElement = element;
	}
	
	public void refreshMappings() {
		if (fTable == null) {
			return;
		}
		
		fTable.removeAll();
		Iterator mappings = fContentTypeMappings.entrySet().iterator();

		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();

		while (mappings.hasNext()) {
			Entry entry = (Entry) mappings.next();

			TableItem item = new TableItem(fTable, SWT.NONE);

			String contentType = (String) entry.getKey();
			String contentTypeName = contentTypeManager.getContentType(contentType).getName();
			String languageName = LanguageManager.getInstance().getLanguage((String) entry.getValue()).getName();

			if (fOverriddenContentTypes.contains(contentType)) {
				item.setText(0, Messages.format(PreferencesMessages.ProjectLanguagesPropertyPage_overriddenContentType, contentTypeName));
				item.setFont(fOverriddenFont);
			} else {
				item.setText(0, contentTypeName);
			}
			item.setText(1, languageName);
		}
		
		if (fChild != null) {
			Set overrides = new HashSet(fContentTypeMappings.keySet());
			overrides.addAll(fOverriddenContentTypes);
			fChild.setOverriddenContentTypes(overrides);
			fChild.refreshMappings();
		}
	}

	public void setOverriddenContentTypes(Set contentTypes) {
		fOverriddenContentTypes = contentTypes;
	}
	
	public void setMappings(Map mappings) {
		fContentTypeMappings = new TreeMap(mappings);
	}

	public IContentType[] getAffectedContentTypes() {
		return (IContentType[]) fAffectedContentTypes.toArray(new IContentType[fAffectedContentTypes.size()]);
	}

	public Map getContentTypeMappings() {
		return Collections.unmodifiableMap(fContentTypeMappings);
	}

	public void setReadOnly(boolean isReadOnly) {
		fIsReadOnly = isReadOnly;
	}
	
	public void setChild(LanguageMappingWidget child) {
		fChild = child;
	}
}
