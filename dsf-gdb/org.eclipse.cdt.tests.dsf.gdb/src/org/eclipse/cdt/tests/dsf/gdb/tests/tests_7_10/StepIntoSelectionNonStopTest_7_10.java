/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_11;

import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class StepIntoSelectionNonStopTest_7_11 extends StepIntoSelectionTest_7_11 {

	@BeforeClass
	public static void beforeClass() {
		Assume.assumeTrue(supportsNonStop());
	}

    @Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		
    	setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, true);
	}
}
