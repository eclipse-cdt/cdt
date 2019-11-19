/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.launchbar.ui.internal.target;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.launchbar.ui.internal.Messages;

/**
 * The new wizard is responsible for allowing the user to choose which new
 * (nested) wizard to run. The set of available new wizards comes from the new
 * extension point.
 */
public class NewLaunchTargetWizard extends Wizard {

	public NewLaunchTargetWizard() {
		setForcePreviousAndNextButtons(true);
	}

	/**
	 * Create the wizard pages
	 */
	@Override
	public void addPages() {
		addPage(new NewLaunchTargetWizardSelectionPage());
		setWindowTitle(Messages.NewLaunchTargetWizard_Title);
	}

	@Override
	public boolean performFinish() {
		// Downstream wizards do finish
		return false;
	}

}
