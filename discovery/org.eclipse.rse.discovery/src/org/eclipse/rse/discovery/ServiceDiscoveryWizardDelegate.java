/********************************************************************************
 * Copyright (c) 2006 Symbian Software Ltd. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Javier Montalvo Orús (Symbian) - initial API and implementation
 ********************************************************************************/

package org.eclipse.rse.discovery;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.ui.wizards.RSENewConnectionWizard;
import org.eclipse.rse.ui.wizards.RSENewConnectionWizardDelegate;

/**
 * RSE Wizard extension for Service Discovery
 */

public class ServiceDiscoveryWizardDelegate extends RSENewConnectionWizardDelegate {

	private ServiceDiscoveryWizard subWizard;

	private boolean isSubWizardCreated;

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.RSENewConnectionWizardDelegate#init(org.eclipse.rse.ui.wizards.RSENewConnectionWizard, org.eclipse.rse.core.IRSESystemType)
	 */
	public void init(RSENewConnectionWizard wizard, IRSESystemType systemType) {
		super.init(wizard, systemType);
		subWizard = new ServiceDiscoveryWizard();
		isSubWizardCreated = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#getDummyHost()
	 */
	public IHost getDummyHost() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#getMainPage()
	 */
	public IWizardPage getMainPage() {
		if (!isSubWizardCreated) {
			subWizard.addPages();
			isSubWizardCreated = true;
		}
		IWizardPage firstSubPage = subWizard.getStartingPage();
		return firstSubPage;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.IRSENewConnectionWizardDelegate#performFinish()
	 */
	public boolean performFinish() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.RSENewConnectionWizardDelegate#canFinish()
	 */
	public boolean canFinish() {
		return subWizard.canFinish();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.RSENewConnectionWizardDelegate#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		return subWizard.getNextPage(page);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.ui.wizards.RSENewConnectionWizardDelegate#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (page == getMainPage()) {
			return getWizard().getPreviousPage(page);
		} else {
			return subWizard.getPreviousPage(page);
		}
	}

}
