/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
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

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#createPreferenceContent(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createPreferenceContent(Composite composite) {
		return fConfigurationBlock.createContents(composite);
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#hasProjectSpecificOptions(org.eclipse.core.resources.IProject)
	 */
	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return fConfigurationBlock.hasProjectSpecificOptions(project);
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#enableProjectSpecificSettings(boolean)
	 */
	@Override
	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		super.enableProjectSpecificSettings(useProjectSpecificSettings);
		if (fConfigurationBlock != null) {
			fConfigurationBlock.enableProjectSpecificSettings(useProjectSpecificSettings);
		}
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageID()
	 */
	@Override
	protected String getPreferencePageId() {
		return PREF_ID;
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageID()
	 */
	@Override
	protected String getPropertyPageId() {
		return PROP_ID;
	}
	
	/*
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.dispose();
		}
		super.dispose();
	}
	
	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performDefaults();
		}
		super.performDefaults();
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (fConfigurationBlock != null && !fConfigurationBlock.performOk()) {
			return false;
		}	
		return super.performOk();
	}
	
	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public void performApply() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performApply();
		}	
		super.performApply();
	}
	
	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public void setElement(IAdaptable element) {
		super.setElement(element);
		setDescription(null); // no description for property page
	}
}
