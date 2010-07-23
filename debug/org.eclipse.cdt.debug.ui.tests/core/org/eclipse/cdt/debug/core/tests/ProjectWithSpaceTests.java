/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.tests;

import java.io.IOException;

import junit.framework.Test;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.core.runtime.CoreException;

/**
 * Create a project with space and do sanity test for debugger
 */
public class ProjectWithSpaceTests extends AbstractDebugTest {
	public static Test suite() {
		return new DebugTestWrapper(ProjectWithSpaceTests.class) {
		};
	}

	@Override
	protected String getProjectName() {
		return "with space";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createDebugSession();
		assertNotNull(currentTarget);
		currentTarget.deleteAllBreakpoints();
		pause();
	}

	@Override
	protected void tearDown() throws Exception {
		/* clean up the session */
		targets[0].terminate();
		int x = 0;
		while ((!targets[0].isTerminated()) && (x < 30)) {
			Thread.sleep(100);
		}
		if (!targets[0].isTerminated())
			targets[0].terminate();
		super.tearDown();
	}

	/**
	 * Basic sanity test
	 */
	public void testLineBreak() throws CoreException, MIException, IOException, CDIException, InterruptedException {
		ICDITarget cdiTarget = currentTarget;
		ICDILineLocation location;
		boolean caught = false;
		/***********************************************************************
		 * Create a break point in a generic function
		 **********************************************************************/
		location = cdiTarget.createLineLocation("main.c", 7);
		assertNotNull(location);
		cdiTarget.setLineBreakpoint(0, location, null, false);
			
		targets = session.getTargets();
		/*
		 * We better only have one target connected to this session or something
		 * is not right...
		 */
		assertTrue(targets.length == 1);
		/*
		 * Resume the target, this should cause it to run till it hits the
		 * breakpoint
		 */
		ICDITarget target = targets[0];
		target.resume(false);
		/**
		 * Give the process up to 10 seconds to become either terminated or
		 * suspended. It sould hit the breakponint almost immediatly so we
		 * should only sleep for max 100 ms
		 */
		for (int x = 0; x < 100; x++) {
			if (target.isSuspended() || target.isTerminated())
				break;
			Thread.sleep(100);
		}
		assertTrue("Suspended: " + target.isSuspended() + " Terminated: " + target.isTerminated(),
		        target.isSuspended());
		ICDILocator locator = getCurrentLocator();
		assertTrue(locator.getLineNumber() == 7);
		assertTrue(locator.getFunction().equals("func1"));
		assertTrue(locator.getFile().endsWith("main.c"));
	}
}