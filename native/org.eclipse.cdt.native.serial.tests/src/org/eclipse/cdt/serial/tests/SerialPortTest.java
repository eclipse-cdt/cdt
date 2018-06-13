/*******************************************************************************
 * Copyright (c) 2018 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Waqas Ilyas (Mentor Graphics) - Initial implementation.
 *******************************************************************************/
package org.eclipse.cdt.serial.tests;

import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DEFAULT_PORT;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DEFAULT_RATE;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.LOG;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.openPort;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.separator;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.eclipse.cdt.serial.SerialPort;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

//@Disabled
class SerialPortTest {
	@Test
	void testOpen() {
		separator("openClose");

		SerialPort port = null;
		try {
			port = openPort(DEFAULT_PORT, DEFAULT_RATE);
		} catch (IOException e) {
			fail("Failed to open port: " + e.getMessage());
		}

		// Shouldn't be able to open the port again
		try {
			port = openPort(DEFAULT_PORT, DEFAULT_RATE);
			fail("This port should not be available");
		} catch (IOException e) {
			// success
		}

		try {
			assertNotNull(port);
			port.close();
		} catch (IOException e) {
			fail("Unable to close port: " + e.getMessage());
		}
	}

	@Test
	void testPortsList() throws IOException {
		separator("printPorts");

		for (String portName : SerialPort.list())
			LOG.info(portName);
	}
}
