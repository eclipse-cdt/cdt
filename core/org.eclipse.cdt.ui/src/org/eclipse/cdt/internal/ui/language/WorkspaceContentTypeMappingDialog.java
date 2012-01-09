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

import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;

public class WorkspaceContentTypeMappingDialog extends ContentTypeMappingDialog {
	private Set<String> fFilteredContentTypes;

	public WorkspaceContentTypeMappingDialog(Shell parentShell) {
		super(parentShell);
		fFilteredContentTypes = Collections.emptySet();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(2, false));

		Label contentTypeLabel = new Label(area, SWT.TRAIL);
		contentTypeLabel.setText(PreferencesMessages.ContentTypeMappingsDialog_contentType);

		fContentType = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
		fContentType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		configureContentTypes(fContentType);
		fContentType.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				fSelectedContentTypeName = fContentType.getText();
				fSelectedContentTypeID = fContentTypeNamesToIDsMap.get(fSelectedContentTypeName);
				getButton(IDialogConstants.OK_ID).setEnabled(isValidSelection());
			}
		});

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

	public void setContentTypeFilter(Set<String> contentTypes) {
		fFilteredContentTypes = contentTypes;
	}

	@Override
	protected boolean isValidSelection() {
		return fContentType.getSelectionIndex() != -1 && fLanguage.getSelectionIndex() != -1;
	}

}
