/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.tests;

import java.io.IOException;

import junit.framework.Test;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement3;
import org.eclipse.cdt.debug.core.cdi.model.ICDIEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.gdb.eventbkpts.IEventBreakpointConstants;

public class EventBreakpointTests extends AbstractDebugTest {
	public static Test suite() {
		return new DebugTestWrapper(EventBreakpointTests.class){};
	}

	@Override
	protected String getProjectName() {
		return "catchpoints";
	}

	@Override
	protected String getProjectZip() {
		return "resources/debugCxxTest.zip";
	}

	@Override
	protected String getProjectBinary() {
		return "catchpoints.exe";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createDebugSession();
		assertNotNull(currentTarget);
		currentTarget.deleteAllBreakpoints();
		pause();
	}


	public void testCatch() throws CModelException, IOException, MIException, CDIException {
		eventbreakpoints(IEventBreakpointConstants.EVENT_TYPE_CATCH, "");
	}

	public void testThrow() throws CModelException, IOException, MIException, CDIException {
		eventbreakpoints(IEventBreakpointConstants.EVENT_TYPE_THROW, "");
	}
	
	private void eventbreakpoints(String type, String arg) throws CModelException, IOException, MIException, CDIException {
		ICDIBreakpoint[] breakpoints;
		ICDIEventBreakpoint curbreak;

		setBreakOnMain();
		currentTarget.restart();
		waitSuspend(currentTarget);
		ICDILocator locator = getCurrentLocator();
		assertEquals("Debug should be stopped in function 'main' but it is stopped in: " + locator.getFunction(),
				"main", locator.getFunction());

		currentTarget.deleteAllBreakpoints();
		pause();
		assertTrue(currentTarget instanceof ICDIBreakpointManagement3);
		((ICDIBreakpointManagement3) currentTarget).setEventBreakpoint(type, arg, ICBreakpointType.REGULAR, null, false, true);
		pause();
		breakpoints = currentTarget.getBreakpoints();
		assertNotNull(breakpoints);
		assertEquals(1, breakpoints.length);
		if (breakpoints[0] instanceof ICDIEventBreakpoint) {
			curbreak = (ICDIEventBreakpoint) breakpoints[0];
		} else
			curbreak = null;
		assertNotNull("Found breakpoint is not an event breakpoint",curbreak);
		currentTarget.resume(false);
		waitSuspend(currentTarget);
		// it is stopped we are fine, it did hit breakpoint
	}

}
