/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class InferiorExitCodeTest extends BaseTestCase {
	private static final String EXEC_NAME = "LaunchConfigurationAndRestartTestApp.exe";
	// The exit code returned by the test program
	private static final int TEST_EXIT_CODE = 36;

    @Override
    protected void setLaunchAttributes() {
    	super.setLaunchAttributes();
    	    	
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
    }

    /**
     * Test that the exit code is available for the console to use.
     */
    @Test
    public void testExitCodeSet() throws Throwable {
    	// The target is currently stopped.  We resume to get it running
    	// and wait for a shutdown event to say execution has completed
    	SyncUtil.resume();
    	
        ServiceEventWaitor<ICommandControlShutdownDMEvent> shutdownEventWaitor = new ServiceEventWaitor<ICommandControlShutdownDMEvent>(
        		getGDBLaunch().getSession(),
        		ICommandControlShutdownDMEvent.class);
        shutdownEventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));

        
		IProcess[] launchProcesses = getGDBLaunch().getProcesses();;
		for (IProcess proc : launchProcesses) {
			if (proc instanceof InferiorRuntimeProcess) {
				assertThat(proc.getAttribute(IGdbDebugConstants.INFERIOR_EXITED_ATTR), is(notNullValue()));

				// Wait for the process terminate so we can obtain its exit code
				int count = 0;
				while (count++ < 100 && !proc.isTerminated()) {
					try {
						synchronized (proc) {
							proc.wait(10);							
						}
					} catch (InterruptedException ie) {
					}
				}

				int exitValue = proc.getExitValue();
				assertThat(exitValue, is(TEST_EXIT_CODE));
				return;
			}
		}
		assert false;
    }
}
