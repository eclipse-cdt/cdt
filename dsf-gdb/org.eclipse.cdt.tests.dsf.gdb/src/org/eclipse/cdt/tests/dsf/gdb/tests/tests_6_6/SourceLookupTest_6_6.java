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
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_6_6;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.SourceLookupTest;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_6.SourceLookupTest_7_6;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class SourceLookupTest_6_6 extends SourceLookupTest {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_6_6);
	}

	/**
	 * For details on the ignore, see
	 * {@link SourceLookupTest_7_6#sourceMappingAC()}
	 */
	@Ignore("Only works starting with GDB 7.6")
	@Test
	@Override
	public void sourceMappingAC() throws Throwable {
		super.sourceMappingAC();
	}

	/**
	 * For details on the ignore, see
	 * {@link SourceLookupTest_7_6#sourceMappingAC()}
	 */
	@Ignore("Only works starting with GDB 7.6")
	@Test
	@Override
	public void sourceMappingAN() throws Throwable {
		super.sourceMappingAN();
	}

	/**
	 * For details on the ignore, see
	 * {@link SourceLookupTest_7_6#sourceMappingAC()}
	 */
	@Ignore("Only works starting with GDB 7.6")
	@Test
	@Override
	public void sourceMappingRC() throws Throwable {
		super.sourceMappingRC();
	}

	/**
	 * For details on the ignore, see
	 * {@link SourceLookupTest_7_6#sourceMappingAC()}
	 */
	@Ignore("Only works starting with GDB 7.6")
	@Test
	@Override
	public void sourceMappingRN() throws Throwable {
		super.sourceMappingRN();
	}

	/**
	 * For details on the ignore, see
	 * {@link SourceLookupTest_7_6#sourceMappingBreakpointsAC()}
	 */
	@Ignore("Only works starting with GDB 7.6")
	@Test
	@Override
	public void sourceMappingBreakpointsAC() throws Throwable {
		super.sourceMappingBreakpointsAC();
	}

	/**
	 * For details on the ignore, see
	 * {@link SourceLookupTest_7_6#sourceMappingBreakpointsAC()}
	 */
	@Ignore("Only works starting with GDB 7.6")
	@Test
	@Override
	public void sourceMappingBreakpointsRC() throws Throwable {
		super.sourceMappingBreakpointsRC();
	}

	/**
	 * For details on the ignore, see
	 * {@link SourceLookupTest_7_6#sourceMappingBreakpointsAC()}
	 */
	@Ignore("Only works starting with GDB 7.6")
	@Test
	@Override
	public void sourceMappingChanges() throws Throwable {
		super.sourceMappingChanges();
	}
}
