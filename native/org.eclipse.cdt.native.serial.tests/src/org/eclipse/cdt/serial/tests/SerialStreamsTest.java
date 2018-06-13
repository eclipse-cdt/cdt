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

import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DATA_EMITTER_DELAY;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DATA_EMITTER_DELAY_RANDOM;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DEFAULT_PAIRED_PORT;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DEFAULT_PORT;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.DEFAULT_RATE;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.LOG;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.PORT_CLOSE_DELAY;
import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.separator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.tests.utils.SerialDataEmitter;
import org.eclipse.cdt.serial.tests.utils.SerialTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * Tests the streams of the serial port. Requires a program emulating an IO
 * device by sending a pre-determined sample data on a paired serial port.
 */
// Disabling by default, as it requires someone to set up serial port
//@Disabled
class SerialStreamsTest {
	public static final String SAMPLE_DATA_FILE = "/deosai.txt";

	protected static String SAMPLE_DATA;
	protected SerialDataEmitter emitter;

	@BeforeAll
	static void init() throws IOException {
		InputStream in = SerialStreamsTest.class.getResourceAsStream(SAMPLE_DATA_FILE);
		byte b[] = new byte[1024];
		int l;
		StringBuffer buf = new StringBuffer();
		while ((l = in.read(b)) != -1) {
			buf.append(new String(b, 0, l));
		}
		SAMPLE_DATA = buf.toString();
	}

	@BeforeEach
	void startEmitter() throws Exception {
		// Start a serial port data emulator
		emitter = new SerialDataEmitter(DEFAULT_PAIRED_PORT, SAMPLE_DATA_FILE, DATA_EMITTER_DELAY,
				DATA_EMITTER_DELAY_RANDOM);
		emitter.setEmitMode(0);
		emitter.setInputTimeout(120000);
		emitter.setInteractiveMode(true);
		emitter.start();
	}

	@AfterEach
	void stopEmitter() throws Exception {
		emitter.stopEmitter();
	}

	@Test
	public void timeout() throws IOException {
		separator("testTimeout");

		/*
		 * Just get the port, don't send any data, so we don't get a response For this
		 * test we expect to receive no data
		 */
		SerialPort port = SerialTestUtils.openPort(DEFAULT_PORT, DEFAULT_RATE);

		try {
			long start = Calendar.getInstance().getTimeInMillis();
			long timeout = 10000;
			try {
				int b = SerialTestUtils.openInputStream(port, timeout).read();
				fail("Didn't timeout... got data: " + b);
			} catch (InterruptedIOException e) {
				// timed out
				long end = Calendar.getInstance().getTimeInMillis();
				// Ensure timeout is correct given half a second error
				assertTrue((end - start) > (timeout - 500), "Timed out too early: '" + ((end - start) / 1000f) + "s'");
				assertTrue((end - start) < (timeout + 500), "Timed out too late: '" + ((end - start) / 1000f) + "s'");
			}
		} finally {
			port.close();
		}
	}

	@Test
	void readBytes() throws IOException {
		separator("readByte");

		SerialPort port = wakeUpSerial();
		try {
			InputStream input = SerialTestUtils.openInputStream(port);
			closePortDelayed(port);
			readBytes(input);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			port.close();
		}
	}

	@Test
	void readBytesWithTimeout() throws IOException {
		separator("readByteWithTimeout");

		SerialPort port = wakeUpSerial();
		try {
			InputStream input = SerialTestUtils.openInputStream(port, 5000);
			readBytes(input);
		} finally {
			port.close();
		}
	}

	@Test
	void readArarys() throws IOException {
		separator("readArray");

		SerialPort port = wakeUpSerial();
		try {
			closePortDelayed(port);
			InputStream input = SerialTestUtils.openInputStream(port);
			readArrays(input);
		} finally {
			port.close();
		}
	}

	@Test
	void readArraysWithTimeout() throws IOException {
		separator("readArrayWithTimeout");

		SerialPort port = wakeUpSerial();
		try {
			InputStream input = SerialTestUtils.openInputStream(port, 5000);
			readArrays(input);
		} finally {
			port.close();
		}
	}

	@Test
	void readLines() throws IOException {
		separator("readLines");

		SerialPort port = wakeUpSerial();
		try {
			closePortDelayed(port);
			readLines(SerialTestUtils.openReader(port));
		} finally {
			port.close();
		}
	}

	@Test
	void readLinesWithTimeout() throws IOException {
		separator("readLinesWithTimeout");

		SerialPort port = wakeUpSerial();
		try {
			readLines(SerialTestUtils.openReader(port, 5000));
		} finally {
			port.close();
		}
	}

	void readBytes(InputStream input) throws IOException {
		StringBuffer currentLine = new StringBuffer(200);
		StringBuffer data = new StringBuffer(SAMPLE_DATA.length());
		int lineCount = 0;

		try {
			do {
				int c = input.read();
				if (c < 0)
					throw new InterruptedIOException("End of stream");

				data.append((char) c);

				if (c == '\n') {
					lineCount++;
					LOG.info(currentLine.toString());
					currentLine = new StringBuffer(200);
				} else if (c != '\r')
					currentLine.append((char) c);

			} while (true); // Read only limited chars
		} catch (InterruptedIOException e) {
			// timed out while reading or EOF... add remaining data to lines
			if (currentLine.length() > 0) {
				LOG.info(currentLine.toString());
				lineCount++;
			}
		}

		verifyData(lineCount, data.toString());
	}

	void readArrays(InputStream input) throws IOException {
		StringBuffer data = new StringBuffer(SAMPLE_DATA.length());
		StringBuffer currentLine = new StringBuffer();
		int lineCount = 0;

		try {
			do {
				byte array[] = new byte[10000];
				int n = input.read(array, 0, array.length);
				if (n == 0)
					throw new IOException("Input stream returned zero number of bytes read");

				if (n < 0)
					throw new InterruptedIOException("End of stream");

				for (int i = 0; i < n; i++) {
					char c = (char) array[i];
					data.append(c);

					if (c == '\r')
						continue;

					if (c == '\n') {
						lineCount++;
						LOG.info(currentLine.toString());
						currentLine = new StringBuffer();
					} else
						currentLine.append(c);
				}
			} while (true);
		} catch (InterruptedIOException iioe) {
			// timed out while reading or EOF... add remaining data to lines
			if (currentLine.length() > 0) {
				lineCount++;
				LOG.info(currentLine.toString());
			}
		}

		verifyData(lineCount, data.toString());
	}

	void readLines(BufferedReader reader) throws IOException {
		Vector<String> lines = new Vector<>();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
				LOG.info(line);
			}
		} catch (InterruptedIOException e) {
			// timed out
			LOG.info("timed out...");
		}

		verifyLines(lines.toArray(new String[lines.size()]));
	}

	void verifyData(int lineCount, String data) {
		assertEquals(11, lineCount, "Incorrect line count");
		assertEquals(SAMPLE_DATA, data, "Mismatched data received");
	}

	void verifyLines(String lines[]) {
		assertEquals(11, lines.length, "Incorrect line count");

		int ptr = 0;
		for (String l : lines) {
			assertEquals(SAMPLE_DATA.substring(ptr, ptr + l.length()), l, "Mismatched data received");

			ptr += l.length();
			if (SAMPLE_DATA.charAt(ptr) == '\r')
				ptr++;

			if (SAMPLE_DATA.charAt(ptr) == '\n')
				ptr++;
		}
	}

	static SerialPort wakeUpSerial() throws IOException {
		final SerialPort port = SerialTestUtils.openPort(DEFAULT_PORT, DEFAULT_RATE);
		port.getOutputStream().write(32);
		return port;
	}

	static void closePortDelayed(final SerialPort port) {
		// Since the raw input stream blocks so we need a separate task for closing port
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					if (port.isOpen())
						port.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, PORT_CLOSE_DELAY);
	}
}
