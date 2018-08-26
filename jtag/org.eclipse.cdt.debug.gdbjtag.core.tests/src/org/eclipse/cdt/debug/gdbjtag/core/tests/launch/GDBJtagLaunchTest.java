/*******************************************************************************
 * Copyright (c) 2016, 2018 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - base API and implementation
 *    John Dallaway - GDB JTAG implementation (bug 538282)
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.tests.launch;

import static org.junit.Assert.assertFalse;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@SuppressWarnings("restriction")
public class GDBJtagLaunchTest extends BaseParametrizedTestCase {

	private static final String TEST_LAUNCH_CONFIGURATION_TYPE_ID =
			"org.eclipse.cdt.debug.gdbjtag.launchConfigurationType"; //$NON-NLS-1$
	private static final String TEST_JTAG_DEVICE_ID =
			"org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.genericDevice"; //$NON-NLS-1$
	private static final String TEST_PROGRAM_NAME = EXEC_PATH + "Minimal.exe"; //$NON-NLS-1$

	@Test
	public void testGdbJtagLaunch() {
		assertFalse("Launch should be running", getGDBLaunch().isTerminated());
	}

	@Override
	protected String getLaunchConfigurationTypeId() {
		return TEST_LAUNCH_CONFIGURATION_TYPE_ID;
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, TEST_PROGRAM_NAME);
		setLaunchAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET, remote);
		setLaunchAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID, TEST_JTAG_DEVICE_ID);
		setLaunchAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE, false);
		setLaunchAttribute(IGDBJtagConstants.ATTR_LOAD_SYMBOLS, remote);
		setLaunchAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, true);
		setLaunchAttribute(IGDBJtagConstants.ATTR_STOP_AT, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
		setLaunchAttribute(IGDBJtagConstants.ATTR_SET_RESUME, remote);
		if (remote) {
			setLaunchAttribute(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_SYMBOLS, true);
		} else {
			setLaunchAttribute(IGDBJtagConstants.ATTR_INIT_COMMANDS, "file " + TEST_PROGRAM_NAME); //$NON-NLS-1$
			setLaunchAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, "run"); //$NON-NLS-1$
		}
	}

	@Override
	protected GdbLaunch doLaunchInner() throws Exception {
		if (remote) {
			final ILaunchConfigurationWorkingCopy wc = getLaunchConfiguration().getWorkingCopy();
			// copy host from IGDBLaunchConfigurationConstants.ATTR_HOST to IGDBJtagConstants.ATTR_IP_ADDRESS
			final Object host = getLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST);
			wc.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, host);
			// copy port from IGDBLaunchConfigurationConstants.ATTR_PORT to IGDBJtagConstants.ATTR_PORT_NUMBER
			final Object port = getLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT);
			if (port instanceof String) {
				wc.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER, Integer.valueOf((String) port));
			}
			wc.doSave();
		}
		return super.doLaunchInner();
	}

}
