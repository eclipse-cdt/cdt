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
package org.eclipse.cdt.internal.ui.build;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.jface.wizard.Wizard;

public class NewToolChainWizard extends Wizard {

	public NewToolChainWizard() {
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new NewToolChainWizardSelectionPage());
		setWindowTitle(CUIMessages.NewToolChainWizard_Title);
	}

	@Override
	public boolean performFinish() {
		// Downstream wizards do finish
		return false;
	}

}
