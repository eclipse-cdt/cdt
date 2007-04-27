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
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizard;

public class StdBuildWizard extends AbstractCWizard {
	private static final String NAME = Messages.getString("StdBuildWizard.0"); //$NON-NLS-1$
	
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		STDWizardHandler h = new STDWizardHandler(parent, wizard);
		h.addTc(null); // add default toolchain
		IToolChain[] tcs = ManagedBuildManager.getRealToolChains();
		for (int i=0; i<tcs.length; i++)
			if (isValid(tcs[i], supportedOnly, wizard)) 
				h.addTc(tcs[i]);
		EntryDescriptor wd = new EntryDescriptor(NAME, null, NAME, false, h, null); 
		return new EntryDescriptor[] {wd};
		
// test only: creating items like of Templates	
/*		
		EntryDescriptor[] out = new EntryDescriptor[6];
		out[5] = wd;
		for (int i=0; i<5; i++) {
			out[i] = new EntryDescriptor("Template #" + i, 
					"org.eclipse.cdt.build.core.buildArtefactType.exe",
					"Template" + i,	false, null, null);
		}
		return out;
*/		
	}
}
