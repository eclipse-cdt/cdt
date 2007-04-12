/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.Messages;
import org.eclipse.cdt.ui.wizards.WizardItemData;
import org.eclipse.jface.wizard.IWizard;

public class StdBuildWizard extends AbstractCWizard {
	
	public WizardItemData[] createItems(boolean supportedOnly, IWizard wizard) {
		STDWizardHandler h = new STDWizardHandler(Messages.getString("StdBuildWizard.0"), IMG0, parent, wizard); //$NON-NLS-1$
		h.addTc(null); // add default toolchain
		IToolChain[] tcs = ManagedBuildManager.getRealToolChains();
		for (int i=0; i<tcs.length; i++)
			if (!supportedOnly || isValid(tcs[i])) h.addTc(tcs[i]);
		WizardItemData wd = new WizardItemData(); 
		wd.name = h.getName();
		wd.handler = h;
		wd.image = h.getIcon();
		wd.id = h.getName();
		wd.parentId = null;
		return new WizardItemData[] {wd};
	}
}
