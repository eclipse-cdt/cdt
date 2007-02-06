/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.language;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;

public class ContentTypeMappingDialog extends Dialog {

	Combo fContentType;
	Combo fLanguage;
	String fSelectedContentTypeName;
	String fSelectedContentTypeID;
	String fSelectedLanguageName;
	String fSelectedLanguageID;
	private Set fFilteredContentTypes;
	private HashMap fContentTypeNamesToIDsMap;
	private HashMap fLanguageNamesToIDsMap;

	public ContentTypeMappingDialog(Shell parentShell) {
		super(parentShell);
		fFilteredContentTypes = Collections.EMPTY_SET;
		fContentTypeNamesToIDsMap = new HashMap();
		fLanguageNamesToIDsMap = new HashMap();
	}

	public String getSelectedContentTypeName() {
		return fSelectedContentTypeName;
	}

	public String getContentTypeID() {
		return fSelectedContentTypeID;
	}

	public String getSelectedLanguageName() {
		return fSelectedLanguageName;
	}

	public String getLanguageID() {
		return fSelectedLanguageID;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(PreferencesMessages.ContentTypeMappingsDialog_title);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);

		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	private boolean isValidSelection() {
		return fContentType.getSelectionIndex() != -1
				&& fLanguage.getSelectionIndex() != -1;
	}

	protected Control createDialogArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(2, false));

		Label contentTypeLabel = new Label(area, SWT.TRAIL);
		contentTypeLabel
				.setText(PreferencesMessages.ContentTypeMappingsDialog_contentType);

		fContentType = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
		fContentType
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		configureContentTypes(fContentType);
		fContentType.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				fSelectedContentTypeName = fContentType.getText();
				fSelectedContentTypeID = (String) fContentTypeNamesToIDsMap
						.get(fSelectedContentTypeName);
				getButton(IDialogConstants.OK_ID)
						.setEnabled(isValidSelection());
			}
		});

		Label languageLabel = new Label(area, SWT.TRAIL);
		languageLabel
				.setText(PreferencesMessages.ContentTypeMappingsDialog_language);

		fLanguage = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
		fLanguage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fLanguage.setItems(getLanguages());
		fLanguage.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				fSelectedLanguageName = fLanguage.getText();
				fSelectedLanguageID = (String) fLanguageNamesToIDsMap
						.get(fSelectedLanguageName);
				getButton(IDialogConstants.OK_ID)
						.setEnabled(isValidSelection());
			}
		});

		return area;
	}

	private void configureContentTypes(Combo combo) {
		combo.removeAll();
		String[] contentTypesIDs = LanguageManager.getInstance()
				.getRegisteredContentTypeIds();

		IContentTypeManager contentTypeManager = Platform
				.getContentTypeManager();

		for (int i = 0; i < contentTypesIDs.length; i++) {
			if (!fFilteredContentTypes.contains(contentTypesIDs[i])) {

				String name = contentTypeManager.getContentType(
						contentTypesIDs[i]).getName();

				combo.add(name);

				// keep track of what ID this name corresponds to so that when
				// we setup the mapping
				// later based upon user selection, we'll know what ID to use
				fContentTypeNamesToIDsMap.put(name, contentTypesIDs[i]);
			}
		}
	}

	private String[] getLanguages() {
		ILanguage[] languages = LanguageManager.getInstance()
				.getRegisteredLanguages();
		String[] descriptions = new String[languages.length];
		for (int i = 0; i < descriptions.length; i++) {
			descriptions[i] = languages[i].getName();
			fLanguageNamesToIDsMap.put(descriptions[i], languages[i].getId());
		}
		return descriptions;
	}

	public void setContentTypeFilter(Set contentTypes) {
		fFilteredContentTypes = contentTypes;
	}

}
