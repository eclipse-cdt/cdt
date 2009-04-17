/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.lrparser.xlc.ui.preferences;



import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcLanguagePreferences;
import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcPreferenceKeys;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
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
public class XlcLanguageOptionsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IWorkbenchPropertyPage {

	private IAdaptable element;
	
	private Button button_vectors;
	

	public IAdaptable getElement() {
		return element;
	}

	public void setElement(IAdaptable element) {
		this.element = element;
	}

	public boolean isPropertyPage() {
		return element != null;
	}
	
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite page = ControlFactory.createComposite(parent, 1);
	
		if(isPropertyPage()) {
			Link link = new Link(page, SWT.NONE);
			link.setText(PreferenceMessages.XlcLanguageOptionsPreferencePage_link);
			link.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					PreferencesUtil.createPreferenceDialogOn(getShell(), event.text, null, null).open();
				}
			});
		}
		
		Composite group = ControlFactory.createGroup(page, PreferenceMessages.XlcLanguageOptionsPreferencePage_group, 1);
		
		button_vectors = ControlFactory.createCheckBox(group, PreferenceMessages.XlcLanguageOptionsPreferencePage_preference_vectors);
		initCheckbox(button_vectors, XlcPreferenceKeys.KEY_SUPPORT_VECTOR_TYPES);
		
		return page;
	}

	
	private void initCheckbox(Button checkbox, String prefKey) {
		String preference = null;
		
		if(isPropertyPage()) {
			IProject project = getProject();
			preference = XlcLanguagePreferences.getProjectPreference(prefKey, project);
		}
		else {
			preference = XlcLanguagePreferences.getWorkspacePreference(prefKey);
		}
		
		if(preference == null) {
			preference = XlcLanguagePreferences.getDefaultPreference(prefKey);
		}
		
		checkbox.setSelection(Boolean.valueOf(preference));
	}

	

	@Override
	protected void performDefaults() {
		button_vectors.setSelection(Boolean.valueOf(XlcLanguagePreferences.getDefaultPreference(XlcPreferenceKeys.KEY_SUPPORT_VECTOR_TYPES)));
		
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		setPreference(XlcPreferenceKeys.KEY_SUPPORT_VECTOR_TYPES, button_vectors.getSelection(), getProject());
		return true;
	}
	
	
	private IProject getProject() {
		return isPropertyPage() ? (IProject)element.getAdapter(IProject.class) : null;
	}
	
	private static void setPreference(String key, boolean val, IProject project) {
		if(project != null)
			XlcLanguagePreferences.setProjectPreference(key, String.valueOf(val), project);
		else
			XlcLanguagePreferences.setWorkspacePreference(key, String.valueOf(val));
	}
	
		
}
