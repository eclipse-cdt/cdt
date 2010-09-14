/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
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

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class StdBuildWizard extends AbstractCWizard {
	private static final String NAME = Messages.getString("StdBuildWizard.0"); //$NON-NLS-1$
	private static final String ID = "org.eclipse.cdt.build.makefile.projectType"; //$NON-NLS-1$
	
	/**
	 * @since 5.1
	 */
	public static final String EMPTY_PROJECT = Messages.getString("AbstractCWizard.0");  //$NON-NLS-1$
	
	@Override
	public EntryDescriptor[] createItems(boolean supportedOnly, IWizard wizard) {
		STDWizardHandler h = new STDWizardHandler(parent, wizard);
		h.addTc(null); // add default toolchain
		IToolChain[] tcs = ManagedBuildManager.getRealToolChains();
		for (int i=0; i<tcs.length; i++)
			if (isValid(tcs[i], supportedOnly, wizard)) 
				h.addTc(tcs[i]);
		EntryDescriptor wd = new EntryDescriptor(ID, null, NAME, true, h, null);
				
		EntryDescriptor wd2 = new EntryDescriptor(ID + ".default", ID, //$NON-NLS-1$
				EMPTY_PROJECT, false, h, null);
				wd2.setDefaultForCategory(true);
						
		return new EntryDescriptor[] {wd, wd2};
		
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
