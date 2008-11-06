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

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.ui.PlatformUI;

/**
 * Tests the RSE new connection wizard functionality.
 * 
 * This is a manual test! Users need to manually make some selections and close
 * the dialog for the test to complete. Therefore this test cannot be run in
 * unattended nightly builds.
 * 
 * What testers should check:
 * <ul>
 * <li>On 1st wizard page, select a type and press next</li>
 * <li>On 2nd page, selected type should be visible.</li>
 * <li>Select type again on that page -- next should be enabled, press next</li>
 * <li>Press back, select a different type, press next -- correct type should be
 * shown</li> </li> For details, see
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=237816
 * 
 * @author uwe.stieber@windriver.com
 */
public class RSENewConnectionWizardTestCase extends RSECoreTestCase {

	/* Test restrictToSystemType functionality. See bug 237816 */
	public void testRestrictToSystemType() {
		//-test-author-:UweStieber
		if (!RSETestsPlugin.isTestCaseEnabled("RSENewConnectionWizardTestCase.testRestrictToSystemType")) return; //$NON-NLS-1$

		IRSECoreRegistry coreRegistry = RSECorePlugin.getTheCoreRegistry();
		assertNotNull("Failed to fetch RSE core registry instance!", coreRegistry); //$NON-NLS-1$

		// Construct the wizard
		RSENewConnectionWizardTestWizard wizard = new RSENewConnectionWizardTestWizard();

		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wizard);
		dialog.open();
	}
}
