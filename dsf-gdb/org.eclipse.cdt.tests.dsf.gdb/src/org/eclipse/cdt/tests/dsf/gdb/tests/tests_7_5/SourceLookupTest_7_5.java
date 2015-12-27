/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_4.SourceLookupTest_7_4;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class SourceLookupTest_7_5 extends SourceLookupTest_7_4 {

	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_5);
	}

	@Override
	protected void setExeNames() {
		EXEC_AC_NAME = "SourceLookupAC.exe"; //$NON-NLS-1$
		EXEC_AN_NAME = "SourceLookupAN.exe"; //$NON-NLS-1$
		EXEC_RC_NAME = "SourceLookupRC.exe"; //$NON-NLS-1$
		EXEC_RN_NAME = "SourceLookupRN.exe"; //$NON-NLS-1$
		EXEC_NAME = "SourceLookup.exe"; //$NON-NLS-1$
	}
}
