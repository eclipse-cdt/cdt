/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences;


import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IProject;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.preference.IPreferencePageContainer;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.eclipse.ui.preferences.WorkingCopyManager;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.formatter.CodeFormatterConfigurationBlock;

/*
 * The page to configure the code formatter options.
 */
public class CodeFormatterPreferencePage extends PropertyAndPreferencePage {

	public static final String PREF_ID= "org.eclipse.cdt.ui.preferences.CodeFormatterPreferencePage"; //$NON-NLS-1$
	public static final String PROP_ID= "org.eclipse.cdt.ui.propertyPages.CodeFormatterPreferencePage"; //$NON-NLS-1$
	
	private CodeFormatterConfigurationBlock fConfigurationBlock;

	public CodeFormatterPreferencePage() {
		setDescription(PreferencesMessages.CodeFormatterPreferencePage_description); 
		
		// only used when page is shown programatically
		setTitle(PreferencesMessages.CodeFormatterPreferencePage_title);		 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		IPreferencePageContainer container= getContainer();
		IWorkingCopyManager workingCopyManager;
		if (container instanceof IWorkbenchPreferenceContainer) {
			workingCopyManager= ((IWorkbenchPreferenceContainer) container).getWorkingCopyManager();
		} else {
			workingCopyManager= new WorkingCopyManager(); // non shared 
		}
		PreferencesAccess access= PreferencesAccess.getWorkingCopyPreferences(workingCopyManager);
		fConfigurationBlock= new CodeFormatterConfigurationBlock(getProject(), access);
		
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.CODEFORMATTER_PREFERENCE_PAGE);
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
	
//	/* (non-Javadoc)
//	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#enableProjectSpecificSettings(boolean)
//	 */
//	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
//		super.enableProjectSpecificSettings(useProjectSpecificSettings);
//		if (fConfigurationBlock != null) {
//			fConfigurationBlock.enableProjectSpecificSettings(useProjectSpecificSettings);
//		}
//	}
	
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
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performDefaults();
		}
		super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (fConfigurationBlock != null && !fConfigurationBlock.performOk()) {
			return false;
		}	
		return super.performOk();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public void performApply() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performApply();
		}	
		super.performApply();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		super.setElement(element);
		setDescription(null); // no description for property page
	}
}
