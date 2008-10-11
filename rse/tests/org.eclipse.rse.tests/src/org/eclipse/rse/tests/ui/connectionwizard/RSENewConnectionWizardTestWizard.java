/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.tests.ui.connectionwizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.rse.ui.wizards.newconnection.RSENewConnectionWizardRegistry;

/**
 * Simple test wizard implementation.
 * 
 * @author uwe.stieber@windriver.com
 */
public class RSENewConnectionWizardTestWizard extends Wizard {
	private final RSENewConnectionWizardRegistry fWizardRegisty;
	
	/**
	 * Constructor.
	 * 
	 */
	public RSENewConnectionWizardTestWizard() {
		super();
		
		fWizardRegisty = new RSENewConnectionWizardRegistry();
		
		setNeedsProgressMonitor(false);
		setForcePreviousAndNextButtons(true);
	}

	/**
	 * Returns the RSE new connection wizard registry instance.
	 * 
	 * @return The new connection wizard registry.
	 */
	public RSENewConnectionWizardRegistry getWizardRegistry() {
		return fWizardRegisty;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		addPage(new RSENewConnectionWizardTestSimpleWizardPage(getWizardRegistry(), "Simple Selection Page")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		return true;
	}

	
}
