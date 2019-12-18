/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.ui.target;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.launchbar.ui.internal.target.NewLaunchTargetWizard;
import org.eclipse.ui.PlatformUI;

/**
 * Open the new launch target wizard.
 */
public class NewLaunchTargetWizardAction extends Action {

	@Override
	public void run() {
		Wizard wizard = new NewLaunchTargetWizard();
		new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard).open();
	}

}
