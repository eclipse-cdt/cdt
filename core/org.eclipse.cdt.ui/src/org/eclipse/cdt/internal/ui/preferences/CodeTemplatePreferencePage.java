/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;

/*
 * The page to configure the code templates.
 */
public class CodeTemplatePreferencePage extends PropertyAndPreferencePage {
	public static final String PREF_ID= "org.eclipse.cdt.ui.preferences.CodeTemplatePreferencePage"; //$NON-NLS-1$
	public static final String PROP_ID= "org.eclipse.cdt.ui.propertyPages.CodeTemplatePreferencePage"; //$NON-NLS-1$
	
	public static final String DATA_SELECT_TEMPLATE= "CodeTemplatePreferencePage.select_template"; //$NON-NLS-1$
	
	private CodeTemplateBlock fCodeTemplateConfigurationBlock;

	public CodeTemplatePreferencePage() {
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		// Only used when page is shown programmatically
		setTitle(PreferencesMessages.CodeTemplatesPreferencePage_title);		 
	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		IWorkbenchPreferenceContainer container= (IWorkbenchPreferenceContainer) getContainer();
		fCodeTemplateConfigurationBlock= new CodeTemplateBlock(getNewStatusChangedListener(),
				getProject(), container);
		
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				ICHelpContextIds.CODE_TEMPLATES_PREFERENCE_PAGE);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#createPreferenceContent(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createPreferenceContent(Composite composite) {
		return fCodeTemplateConfigurationBlock.createContents(composite);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#enableProjectSpecificSettings(boolean)
	 */
	@Override
	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		super.enableProjectSpecificSettings(useProjectSpecificSettings);
		if (fCodeTemplateConfigurationBlock != null) {
			fCodeTemplateConfigurationBlock.useProjectSpecificSettings(useProjectSpecificSettings);
		}
	}
	
	/*
	 * @see IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (fCodeTemplateConfigurationBlock != null) {
			return fCodeTemplateConfigurationBlock.performOk(useProjectSettings());
		}
		return true;
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
		if (fCodeTemplateConfigurationBlock != null) {
			fCodeTemplateConfigurationBlock.performDefaults();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		if (fCodeTemplateConfigurationBlock != null) {
			fCodeTemplateConfigurationBlock.dispose();
		}
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.wizards.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
	 */
	public void statusChanged(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		if (fCodeTemplateConfigurationBlock != null) {
			fCodeTemplateConfigurationBlock.performCancel();
		}
		return super.performCancel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#hasProjectSpecificOptions(org.eclipse.core.resources.IProject)
	 */
	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return fCodeTemplateConfigurationBlock.hasProjectSpecificOptions(project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageID()
	 */
	@Override
	protected String getPreferencePageId() {
		return PREF_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageID()
	 */
	@Override
	protected String getPropertyPageId() {
		return null;
		// TODO project specific settings
//		return PROP_ID;
	}

	/*
	 * @see org.eclipse.jface.preference.PreferencePage#applyData(java.lang.Object)
	 */
	@Override
	public void applyData(Object data) {
		if (data instanceof Map<?, ?>) {
			Object id= ((Map<?, ?>) data).get(DATA_SELECT_TEMPLATE);
			if (id instanceof String) {
				final TemplatePersistenceData[] templates=
						fCodeTemplateConfigurationBlock.fTemplateStore.getTemplateData();
				for (TemplatePersistenceData template : templates) {
					if (id.equals(template.getId()) || id.equals(template.getTemplate().getName())) {
						fCodeTemplateConfigurationBlock.postSetSelection(template);
						break;
					}
				}
			}
		}
		super.applyData(data);
	}
}
