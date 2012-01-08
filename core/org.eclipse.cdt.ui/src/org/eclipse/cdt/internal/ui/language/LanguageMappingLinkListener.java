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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class LanguageMappingLinkListener implements Listener {

	private static final String WORKSPACE_PREFERENCE_PAGE = "org.eclipse.cdt.ui.preferences.LanguageMappings"; //$NON-NLS-1$
	private static final String PROJECT_PROPERTY_PAGE = "org.eclipse.cdt.ui.projectLanguageMappings"; //$NON-NLS-1$
	
	private static final String WORKSPACE_LINK = "workspace"; //$NON-NLS-1$
	private static final String PROJECT_LINK = "project"; //$NON-NLS-1$
	
	private Shell fShell;
	private IAdaptable fElement;

	public LanguageMappingLinkListener(Shell shell, IAdaptable element) {
		fShell = shell;
		fElement = element;
	}

	@Override
	public void handleEvent(Event event) {
		if (WORKSPACE_LINK.equals(event.text)) {
			PreferencesUtil.createPreferenceDialogOn(fShell, WORKSPACE_PREFERENCE_PAGE, null, null).open();
		} else if (PROJECT_LINK.equals(event.text)) {
			PreferencesUtil.createPropertyDialogOn(fShell, fElement, PROJECT_PROPERTY_PAGE, null, null).open();
		}
		refresh();
	}

	protected void refresh() {
	}
}
