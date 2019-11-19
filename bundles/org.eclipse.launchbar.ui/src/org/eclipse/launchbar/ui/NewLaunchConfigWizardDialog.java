/*******************************************************************************
 * Copyright (c) 2014, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * Wizard Dialog for Launch Bar's new Launch Configuration Wizard.
 * 
 * @since 2.3
 */
public class NewLaunchConfigWizardDialog extends WizardDialog {

	/**
	 * Size of this dialog if there is no preference specifying a size.
	 */
	protected static final Point DEFAULT_INITIAL_DIALOG_SIZE = new Point(800, 720);

	public NewLaunchConfigWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	/**
	 * Returns the name of the section that this dialog stores its settings in
	 *
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return Activator.PLUGIN_ID + ".LAUNCH_CONFIGURATIONS_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
		if (section == null) {
			section = settings.addNewSection(getDialogSettingsSectionName());
		}
		return section;
	}
	
	@Override
	protected Point getInitialSize() {
		try {
			// Check if we've saved the height before
			getDialogBoundsSettings().getInt("DIALOG_HEIGHT"); //$NON-NLS-1$
			return super.getInitialSize();
		} catch(NumberFormatException nfe) {
			// Nope, return the default size
			return DEFAULT_INITIAL_DIALOG_SIZE;
		}
	}

}
