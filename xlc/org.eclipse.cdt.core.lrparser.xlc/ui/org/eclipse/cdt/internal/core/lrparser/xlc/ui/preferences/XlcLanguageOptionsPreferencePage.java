/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.lrparser.xlc.ui.preferences;

import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcLanguagePreferences;
import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcPref;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * TODO trigger reindex?
 *
 */
public class XlcLanguageOptionsPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {

	private IAdaptable element;
	private PrefCheckbox[] checkboxes;

	private void initializeCheckboxes(Composite group) {
		XlcPref[] prefs = XlcPref.values();
		int n = prefs.length;
		checkboxes = new PrefCheckbox[n];
		IProject project = getProject(); // null for preference page

		for (int i = 0; i < n; i++) {
			String message = PreferenceMessages.getMessage(prefs[i].toString());
			checkboxes[i] = new PrefCheckbox(group, prefs[i], message);
			String preference = XlcLanguagePreferences.get(prefs[i], project);
			checkboxes[i].setSelection(Boolean.valueOf(preference));
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite page = ControlFactory.createComposite(parent, 1);

		if (isPropertyPage()) {
			Link link = new Link(page, SWT.NONE);
			link.setText(PreferenceMessages.XlcLanguageOptionsPreferencePage_link);
			link.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					PreferencesUtil.createPreferenceDialogOn(getShell(), event.text, null, null).open();
				}
			});
		}

		Composite group = ControlFactory.createGroup(page, PreferenceMessages.XlcLanguageOptionsPreferencePage_group,
				1);
		initializeCheckboxes(group);

		return page;
	}

	@Override
	protected void performDefaults() {
		for (PrefCheckbox button : checkboxes) {
			button.setDefault();
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		IProject project = getProject();
		for (PrefCheckbox button : checkboxes) {
			setPreference(button.getKey(), button.getSelection(), project);
		}
		return true;
	}

	private static void setPreference(XlcPref key, boolean val, IProject project) {
		String s = String.valueOf(val);
		if (project != null)
			XlcLanguagePreferences.setProjectPreference(key, s, project);
		else
			XlcLanguagePreferences.setWorkspacePreference(key, s);
	}

	private IProject getProject() {
		return isPropertyPage() ? (IProject) element.getAdapter(IProject.class) : null;
	}

	@Override
	public IAdaptable getElement() {
		return element;
	}

	@Override
	public void setElement(IAdaptable element) {
		this.element = element;
	}

	public boolean isPropertyPage() {
		return element != null;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

}
