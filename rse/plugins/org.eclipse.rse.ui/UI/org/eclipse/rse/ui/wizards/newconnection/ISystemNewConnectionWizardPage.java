/*********************************************************************************
 * Copyright (c) 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 * David Dykstal (IBM) - [168976][api] move ISystemNewConnectionWizardPage from core to UI
 *********************************************************************************/

package org.eclipse.rse.ui.wizards.newconnection;

import org.eclipse.rse.core.model.ISubSystemConfigurator;


/**
 * Interface that all subsystem configuration supplied pages contributed to the
 * New Connection wizard must implement. Moved from Core to UI in RSE 3.0
 *
 * @see org.eclipse.rse.ui.wizards.AbstractSystemNewConnectionWizardPage
 * @see org.eclipse.rse.ui.view.SubSystemConfigurationAdapter#
 *      getNewConnectionWizardPages
 *      (org.eclipse.rse.core.subsystems.ISubSystemConfiguration,
 *      org.eclipse.jface.wizard.IWizard)
 * @since 3.0 moved from Core to UI and extends ISubSystemConfigurator
 */
public interface ISystemNewConnectionWizardPage extends ISubSystemConfigurator {

	/**
	 * This is called when the users presses Finish. All that should be done here is validation
	 * of the input.
	 * @return true if all is ok and the finish can proceed.
	 */
	public boolean performFinish();

	/**
	 * This is called frequently by the framework to decide whether to enable the Finish and Next buttons.
	 * <p>
	 * @return true if the page is complete and has no errors.
	 */
	public boolean isPageComplete();

}
