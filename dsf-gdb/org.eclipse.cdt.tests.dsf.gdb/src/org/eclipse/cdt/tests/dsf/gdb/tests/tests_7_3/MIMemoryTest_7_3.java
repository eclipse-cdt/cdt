/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson	AB		- Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_3;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.tests.ITestConstants;
import org.eclipse.cdt.tests.dsf.gdb.tests.tests_7_2.MIMemoryTest_7_2;
import org.eclipse.cdt.utils.Addr64;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIMemoryTest_7_3 extends MIMemoryTest_7_2 {
	@Override
	protected void setGdbVersion() {
		setGdbProgramNamesLaunchAttributes(ITestConstants.SUFFIX_GDB_7_3);
	}
	
	// Error message is different from GDB 7.3 to GDB 7.5
	@Override
	@Test
	public void writeWithInvalidAddress() throws Throwable {

		// Run to the point where the variable is initialized
		SyncUtil.addBreakpoint("MemoryTestApp.cc:zeroBlocks", true);
		SyncUtil.resumeUntilStopped();
		SyncUtil.step(StepType.STEP_RETURN);

		// Setup call parameters
		long offset = 0;
		int word_size = 1;
		int count = 1;
		byte[] buffer = new byte[count];
		fBaseAddress = new Addr64("0");

		// Perform the test
		fWait.waitReset();
		writeMemory(fMemoryDmc, fBaseAddress, offset, word_size, count, buffer);
		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertFalse(fWait.getMessage(), fWait.isOK());
		String expected = "Could not write memory";	// Error msg returned by gdb
		assertTrue("Wrong error message: expected '" + expected + "', received '" + fWait.getMessage() + "'",
				fWait.getMessage().contains(expected));

		// Ensure no MemoryChangedEvent event was received
		assertTrue("MemoryChangedEvent problem: expected " + 0 + ", received " + getEventCount(), getEventCount() == 0);
	}
}
