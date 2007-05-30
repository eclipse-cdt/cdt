/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IProject;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.ui.CUIPlugin;

/*
 * The page to configure the task tags
 */
public class TodoTaskPreferencePage extends PropertyAndPreferencePage {

	public static final String PREF_ID = "org.eclipse.cdt.ui.preferences.TodoTaskPreferencePage"; //$NON-NLS-1$
	public static final String PROP_ID = "org.eclipse.cdt.ui.propertyPages.TodoTaskPreferencePage"; //$NON-NLS-1$
	
	private TodoTaskConfigurationBlock fConfigurationBlock;

	public TodoTaskPreferencePage() {
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		setDescription(PreferencesMessages.TodoTaskPreferencePage_description); 
		
		// only used when page is shown programatically
		setTitle(PreferencesMessages.TodoTaskPreferencePage_title); 
	}
			
	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		IWorkbenchPreferenceContainer container = (IWorkbenchPreferenceContainer) getContainer();
		fConfigurationBlock = new TodoTaskConfigurationBlock(getNewStatusChangedListener(), getProject(), container);
		
		super.createControl(parent);
		
		if (isProjectPreferencePage()) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.TODO_TASK_PROPERTY_PAGE);
		} else {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.TODO_TASK_PREFERENCE_PAGE);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#createPreferenceContent(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createPreferenceContent(Composite composite) {
		return fConfigurationBlock.createContents(composite);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#hasProjectSpecificOptions(org.eclipse.core.resources.IProject)
	 */
	protected boolean hasProjectSpecificOptions(IProject project) {
		return fConfigurationBlock.hasProjectSpecificOptions(project);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageID()
	 */
	protected String getPreferencePageID() {
		return PREF_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageID()
	 */
	protected String getPropertyPageID() {
		return PROP_ID;
	}

//  TODO: Project specific settings are not supported yet.
//	/* (non-Javadoc)
//	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#enableProjectSpecificSettings(boolean)
//	 */
//	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
//		super.enableProjectSpecificSettings(useProjectSpecificSettings);
//		if (fConfigurationBlock != null) {
//			fConfigurationBlock.useProjectSpecificSettings(useProjectSpecificSettings);
//		}
//	}	
			
	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performDefaults();
		}
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (fConfigurationBlock != null && !fConfigurationBlock.performOk()) {
			return false;
		}	
		return super.performOk();
	}
	
	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performApply()
	 */
	public void performApply() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performApply();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.dispose();
		}
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		super.setElement(element);
		setDescription(null); // no description for property page
	}
}
