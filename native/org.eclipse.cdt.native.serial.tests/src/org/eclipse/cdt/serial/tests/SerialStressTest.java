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

import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DEFAULT_PAIRED_PORT;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DEFAULT_PORT;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DEFAULT_RATE;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.separator;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.tests.utils.SerialDataEmitter;
import org.eclipse.cdt.serial.tests.utils.SerialTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

//Disabling by default, as it requires someone to set up serial port
//@Disabled
class SerialStressTest {
	private final static String SAMPLE_DATA_FILE = "/lorem-ipsum.txt";
	private static String SAMPLE_DATA;

	@BeforeAll
	static void init() throws IOException {
		InputStream in = SerialStressTest.class.getResourceAsStream(SAMPLE_DATA_FILE);
		byte b[] = new byte[1024];
		int l;
		StringBuffer buf = new StringBuffer();
		while ((l = in.read(b)) != -1) {
			buf.append(new String(b, 0, l));
		}
		SAMPLE_DATA = buf.toString();
	}

	@Test
	public void stressTest() throws IOException {
		separator("stressTest");

		int emitModes[] = { 0, 1, 2, 3 };
		long delays[] = { 0, 10, 100, 500, 1000 };
		String format = "|%11d|%9d|%10s|%16s|\n";

		System.out.println("---------------------------------------------------");
		System.out.println("| Emit Mode |  Delay  |  Random  |   Time Taken   |");
		System.out.println("---------------------------------------------------");

		for (int i = 0; i < emitModes.length; i++) {
			for (int j = 0; j < delays.length; j++) {
				for (int k = 0; k < 2; k++) {
					boolean random = k == 0;

					SerialPort port = SerialTestUtils.openPort(DEFAULT_PORT, DEFAULT_RATE);
					try {
						// Start emitter
						SerialDataEmitter emitter = new SerialDataEmitter(DEFAULT_PAIRED_PORT, SAMPLE_DATA_FILE,
								delays[j], random);
						emitter.setEmitMode(emitModes[i]);
						emitter.start();

						InputStream input = SerialTestUtils.openInputStream(port, 5000);

						long start = Calendar.getInstance().getTimeInMillis();
						readEntireInput(input);
						long end = Calendar.getInstance().getTimeInMillis();

						System.out.format(format, emitModes[i], delays[j], random ? "yes" : "no",
								secondsSpent(start, end));

						emitter.stopEmitter();
					} finally {
						port.close();
					}
				}
			}
		}

		System.out.println("---------------------------------------------------");
	}

	protected void readEntireInput(InputStream input) throws IOException {
		StringBuffer buffer = new StringBuffer(512);
		try {
			int c;
			while ((c = input.read()) != -1)
				buffer.append((char) c);
		} catch (Exception ioe) {
			// timed out... consider it end of stream
		}

		assertEquals(SAMPLE_DATA, buffer.toString(), "Mismatched sample data during read");
	}

	protected String secondsSpent(long start, long end) {
		// Remove the timeout time which is always used
		return ((end - start - 5000) / 1000f) + "s";
	}
}
