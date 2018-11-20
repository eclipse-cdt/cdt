/*******************************************************************************
 * Copyright (c) 2005, 2011 Texas Instruments Incorporated and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.tests.wizardPages;

import org.eclipse.cdt.managedbuilder.ui.tests.TestCustomPageManager;

public class TestRunnable implements Runnable {

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		TestCustomPageManager.testFlag = true;

	}

}
