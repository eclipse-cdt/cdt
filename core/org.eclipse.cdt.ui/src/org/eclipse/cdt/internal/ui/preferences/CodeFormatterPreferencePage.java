/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.formatter.CodeFormatterConfigurationBlock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.eclipse.ui.preferences.WorkingCopyManager;

/*
 * The page to configure the code formatter options.
 */
public class CodeFormatterPreferencePage extends PropertyAndPreferencePage {
	public static final String PREF_ID = "org.eclipse.cdt.ui.preferences.CodeFormatterPreferencePage"; //$NON-NLS-1$
	public static final String PROP_ID = "org.eclipse.cdt.ui.propertyPages.CodeFormatterPreferencePage"; //$NON-NLS-1$

	private CodeFormatterConfigurationBlock fConfigurationBlock;

	public CodeFormatterPreferencePage() {
		setDescription(PreferencesMessages.CodeFormatterPreferencePage_description);

		// Only used when page is shown programmatically.
		setTitle(PreferencesMessages.CodeFormatterPreferencePage_title);
	}

	@Override
	public void createControl(Composite parent) {
		IPreferencePageContainer container = getContainer();
		IWorkingCopyManager workingCopyManager;
		if (container instanceof IWorkbenchPreferenceContainer) {
			workingCopyManager = ((IWorkbenchPreferenceContainer) container).getWorkingCopyManager();
		} else {
			workingCopyManager = new WorkingCopyManager(); // non shared
		}
		PreferencesAccess access = PreferencesAccess.getWorkingCopyPreferences(workingCopyManager);
		fConfigurationBlock = new CodeFormatterConfigurationBlock(getProject(), access);

		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.CODEFORMATTER_PREFERENCE_PAGE);
	}

	@Override
	protected Control createPreferenceContent(Composite composite) {
		return fConfigurationBlock.createContents(composite);
	}

	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return fConfigurationBlock.hasProjectSpecificOptions(project);
	}

	@Override
	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		super.enableProjectSpecificSettings(useProjectSpecificSettings);
		if (fConfigurationBlock != null) {
			fConfigurationBlock.enableProjectSpecificSettings(useProjectSpecificSettings);
		}
	}

	@Override
	protected String getPreferencePageId() {
		return PREF_ID;
	}

	@Override
	protected String getPropertyPageId() {
		return PROP_ID;
	}

	@Override
	public void dispose() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.dispose();
		}
		super.dispose();
	}

	@Override
	protected void performDefaults() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performDefaults();
		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if (fConfigurationBlock != null && !fConfigurationBlock.performOk()) {
			return false;
		}
		return super.performOk();
	}

	@Override
	public void performApply() {
		if (fConfigurationBlock != null) {
			fConfigurationBlock.performApply();
		}
		super.performApply();
	}

	@Override
	public void setElement(IAdaptable element) {
		super.setElement(element);
		setDescription(null); // No description for property page
	}
}
