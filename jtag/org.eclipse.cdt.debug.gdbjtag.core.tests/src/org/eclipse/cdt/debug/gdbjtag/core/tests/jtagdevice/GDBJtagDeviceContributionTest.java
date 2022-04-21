/*******************************************************************************
 * Copyright (c) 2022 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    John Dallaway - Initial implementation (Bug 535143)
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.tests.jtagdevice;

import static org.junit.Assert.assertArrayEquals;

import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContribution;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContributionFactory;
import org.junit.Test;

import junit.framework.TestCase;

public class GDBJtagDeviceContributionTest extends TestCase {

	private static final String TEST_JTAG_DEVICE_ID = "org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.genericDevice"; //$NON-NLS-1$
	private static final String EXPECTED_PROTOCOLS = "remote,extended-remote"; //$NON-NLS-1$
	private static final String EXPECTED_DEFAULT_CONNECTION = "localhost:1234"; //$NON-NLS-1$

	@Test
	public void testGdbJtagDeviceContribution() {
		final GDBJtagDeviceContribution contribution = GDBJtagDeviceContributionFactory.getInstance()
				.findByDeviceId(TEST_JTAG_DEVICE_ID);
		assertNotNull(contribution);
		final IGDBJtagConnection device = (IGDBJtagConnection) contribution.getDevice();
		assertArrayEquals(EXPECTED_PROTOCOLS.split(","), device.getDeviceProtocols());
		assertEquals(EXPECTED_DEFAULT_CONNECTION, device.getDefaultDeviceConnection());
	}

}
