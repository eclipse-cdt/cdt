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
package org.eclipse.cdt.serial.tests.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.eclipse.cdt.serial.BaudRate;
import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.ecf.provider.filetransfer.util.TimeoutInputStream;

public class SerialTestUtils {
	public static final String DEFAULT_PORT = System.getProperty("os.name").startsWith("Windows") ? "COM6"
			: "/dev/pts/17";
	public static final BaudRate DEFAULT_RATE = BaudRate.B115200;
	public static final long PORT_CLOSE_DELAY = 10000;

	public static final String DEFAULT_PAIRED_PORT = System.getProperty("os.name").startsWith("Windows") ? "COM7"
			: "/dev/pts/18";

	public static final long DATA_EMITTER_DELAY = 300;
	public static final boolean DATA_EMITTER_DELAY_RANDOM = true;

	public static final Logger LOG = Logger.getLogger("SerialPortTests");

	static {
		StreamHandler sh = new StreamHandler(System.out, new Formatter() {
			@Override
			public String format(LogRecord record) {
				StringBuffer buff = new StringBuffer();
				String s = record.getSourceClassName();
				if (s != null)
					buff.append(s.substring(s.lastIndexOf('.') + 1, s.length()));

				s = record.getSourceMethodName();
				if (s != null) {
					buff.append("(");
					buff.append(s);
					buff.append("): ");
				}

				buff.append(record.getMessage());
				buff.append('\n');
				return buff.toString();
			}
		}) {
			@Override
			public synchronized void publish(LogRecord record) {
				super.publish(record);
				flush();
			}
		};
		LOG.setUseParentHandlers(false);
		LOG.addHandler(sh);

		// To kill all logging, uncomment this
		// LOG.setLevel(Level.OFF);
	}

	public static SerialPort openPort(String portName, BaudRate rate) throws IOException {
		SerialPort port = new SerialPort(portName);
		port.setBaudRate(rate);
		port.setByteSize(ByteSize.B8);
		port.setStopBits(StopBits.S1);
		port.setParity(Parity.None);

		port.open();

		return port;
	}

	public static InputStream openInputStream(SerialPort port, long timeout) {
		return new TimeoutInputStream(port.getInputStream(), 10000, timeout, -1);
	}

	public static InputStream openInputStream(SerialPort port) {
		return port.getInputStream();
	}

	public static OutputStream openOutputStream(SerialPort port) {
		return port.getOutputStream();
	}

	public static BufferedReader openReader(SerialPort port) {
		// Small buffer size to read in small chunks
		return new BufferedReader(new InputStreamReader(openInputStream(port)), 32);
	}

	public static BufferedReader openReader(SerialPort port, long timeout) {
		// Small buffer size to read in small chunks
		TimeoutInputStream timeoutInput = new TimeoutInputStream(port.getInputStream(), 10000, timeout, -1);
		return new BufferedReader(new InputStreamReader(timeoutInput), 32);
	}

	public static void separator(String title) {
		LOG.info("================  " + title + "  ================");
	}
}