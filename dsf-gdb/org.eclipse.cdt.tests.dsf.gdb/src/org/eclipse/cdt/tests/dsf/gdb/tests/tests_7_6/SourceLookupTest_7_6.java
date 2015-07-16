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
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_6;

import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_5.SourceLookupTest_7_5;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class SourceLookupTest_7_6 extends SourceLookupTest_7_5 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_6);
	}

	/**
	 * Supported starting in GDB >= 7.6 because DSF is using the full path name
	 * to pass to the {@link ISourceContainer#findSourceElements(String)}
	 */
	@Test
	@Override
	public void sourceMapping() throws Throwable {
		super.sourceMapping();
	}

	/**
	 * Not supported in GDB >= 7.6 because DSF is using the full path name to
	 * pass to the {@link ISourceContainer#findSourceElements(String)}
	 */
	@Ignore
	@Test
	@Override
	public void directorySource() throws Throwable {
		super.directorySource();
	}
}
