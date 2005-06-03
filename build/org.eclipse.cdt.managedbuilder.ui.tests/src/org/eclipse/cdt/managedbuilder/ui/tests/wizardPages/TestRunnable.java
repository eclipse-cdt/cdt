/*******************************************************************************
 * Copyright (c) 2005 Texas Instruments Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.ui.tests.wizardPages;

import java.lang.Runnable;
import org.eclipse.cdt.managedbuilder.ui.tests.TestCustomPageManager;

public class TestRunnable implements Runnable
{

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		TestCustomPageManager.testFlag = true;
		
	}

}
