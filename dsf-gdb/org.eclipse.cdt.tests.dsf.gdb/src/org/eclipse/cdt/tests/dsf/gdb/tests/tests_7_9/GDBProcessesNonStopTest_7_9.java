/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		  - Initial implementation of Test cases
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_9;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBProcessesNonStopTest_7_9 extends GDBProcessesTest_7_9 {
	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
		// Pause program before termination to give a chance to arm a ServiceEventWaitor
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "1");
	}
}