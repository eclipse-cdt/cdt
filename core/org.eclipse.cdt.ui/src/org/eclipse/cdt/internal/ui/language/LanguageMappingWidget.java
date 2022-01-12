/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;

public abstract class LanguageMappingWidget {

	protected static final int MINIMUM_COLUMN_WIDTH = 150;
	protected Composite fContents;
	protected boolean fIsReadOnly;

	protected Table fTable;
	protected HashMap<String, String> fContentTypeNamesToIDsMap;
	protected Set<IContentType> fAffectedContentTypes;
	protected Font fOverriddenFont;
	protected LanguageMappingWidget fChild;
	protected IAdaptable fElement;

	protected Set<String> fOverriddenContentTypes;

	private boolean fIsChanged;

	public LanguageMappingWidget() {
		fOverriddenFont = JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
		fOverriddenContentTypes = Collections.emptySet();

		// keep a mapping of all registered content types and their names
		fContentTypeNamesToIDsMap = new HashMap<>();
		String[] contentTypesIDs = LanguageManager.getInstance().getRegisteredContentTypeIds();

		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();

		for (int i = 0; i < contentTypesIDs.length; i++) {

			String name = contentTypeManager.getContentType(contentTypesIDs[i]).getName();

			// keep track of what ID this name corresponds to so that when we
			// setup the mapping
			// later based upon user selection, we'll know what ID to use
			fContentTypeNamesToIDsMap.put(name, contentTypesIDs[i]);
		}

		fAffectedContentTypes = new HashSet<>();
	}

	public IAdaptable getElement() {
		return fElement;
	}

	public void setElement(IAdaptable element) {
		fElement = element;
	}

	public void setOverriddenContentTypes(Set<String> contentTypes) {
		fOverriddenContentTypes = contentTypes;
	}

	public IContentType[] getAffectedContentTypes() {
		return fAffectedContentTypes.toArray(new IContentType[fAffectedContentTypes.size()]);
	}

	public void setReadOnly(boolean isReadOnly) {
		fIsReadOnly = isReadOnly;
	}

	public void setChild(LanguageMappingWidget child) {
		fChild = child;
	}

	public boolean isChanged() {
		return fIsChanged;
	}

	public void setChanged(boolean changed) {
		fIsChanged = changed;
	}

	protected void createHeader(Composite parent, String description) {
		Link link = new Link(fContents, SWT.NONE);
		link.setText(description);

		link.addListener(SWT.Selection, new LanguageMappingLinkListener(fContents.getShell(), getElement()) {
			@Override
			protected void refresh() {
				refreshMappings();
			}
		});

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.widthHint = MINIMUM_COLUMN_WIDTH * 2;
		gridData.horizontalSpan = 2;
		link.setLayoutData(gridData);
	}

	public abstract Composite createContents(Composite parent, String description);

	public abstract void refreshMappings();
}
