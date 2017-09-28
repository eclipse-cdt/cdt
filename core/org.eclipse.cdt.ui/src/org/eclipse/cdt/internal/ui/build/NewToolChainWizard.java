/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.build;

import org.eclipse.jface.wizard.Wizard;

public class NewToolChainWizard extends Wizard {

	public NewToolChainWizard() {
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new NewToolChainWizardSelectionPage());
		setWindowTitle("New ToolChain");
	}

	@Override
	public boolean performFinish() {
		// Downstream wizards do finish
		return false;
	}

}
