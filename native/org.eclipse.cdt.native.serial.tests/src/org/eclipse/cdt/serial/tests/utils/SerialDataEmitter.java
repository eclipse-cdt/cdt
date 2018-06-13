/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Waqas Ilyas (Mentor Graphics) - Initial implementation.
 *******************************************************************************/
package org.eclipse.cdt.serial.tests.utils;

import static org.eclipse.cdt.serial.tests.utils.SerialTestUtils.LOG;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.eclipse.cdt.serial.SerialPort;

/**
 * This emits a data file to a given serial port. This can be used for automated
 * testing by sending sample data to a serial port, (or a virtual serial port)
 * that is connected to another port via null-modem cable. The data emitted is
 * expected to emulate an IO device over one serial port, and an application
 * tries to consume the data while listening over the paired serial port. Useful
 * for automated tests.
 * 
 * You can choose whether data is emitted one character, line or "section" at a
 * time. Sections in files are separated using "&&" all by itself on a line.
 * 
 * You can also choose whether the data is emitted automatically, or after
 * receiving a single char, or a line to simulate an interactive shell.
 */
public class SerialDataEmitter extends Thread {
	protected SerialPort port;
	protected InputStream dataStream;

	protected String dataFile;
	protected long maxDelay;
	protected boolean randomDelay;

	protected boolean interactiveMode = false;
	protected boolean emulateShell = false;
	protected int emitMode = 0;
	protected long timeout = 10000;
	protected boolean repeat = false;
	protected boolean enableLogging = false;

	// Read in small amounts, for causing delays through data stream
	protected int readBufferSize = 64;

	// Line handling
	protected ArrayList<String> availableLines = new ArrayList<>();
	protected StringBuffer currentLine = new StringBuffer();

	public SerialDataEmitter(String port, String dataFile, long maxDelay, boolean randomDelay) throws IOException {
		setName("SerialDataEmitter");
		this.port = SerialTestUtils.openPort(port, SerialTestUtils.DEFAULT_RATE);
		this.dataFile = dataFile;
		this.maxDelay = maxDelay;
		this.randomDelay = randomDelay;
		initializeDataStreams();
	}

	protected void initializeDataStreams() throws IOException {
		if (dataStream != null)
			// In case we are repeating the sample data, close previous streams
			dataStream.close();

		// Read the sample data, with the desired delay settings
		InputStream rawData = getClass().getResourceAsStream(dataFile);
		dataStream = new DelayedInputStream(rawData, maxDelay, randomDelay);
	}

	/** Wait for an input char before emitting data */
	public void setInteractiveMode(boolean interactive) {
		this.interactiveMode = interactive;
	}

	/**
	 * Expect a single line of command before emitting data. Requires interactive
	 * mode
	 */
	public void setEmulateShell(boolean emulateShell) {
		this.emulateShell = emulateShell;
	}

	/**
	 * Sets behavior of emitting data 0 = all at once 1 = line by line 2 = char by
	 * char 3 = section by section
	 */
	public void setEmitMode(int emitMode) {
		this.emitMode = emitMode;
	}

	/** Set timeout for waiting for input in interactive mode */
	public void setInputTimeout(long timeout) {
		this.timeout = timeout;
	}

	/** Set true to keep repeating the data file */
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	/** Set true to print log on sysout */
	public void setLoggingEnabled(boolean enable) {
		this.enableLogging = enable;
	}

	public void stopEmitter() throws IOException {
		port.close();
	}

	public void run() {
		InputStream serialIn = SerialTestUtils.openInputStream(port, timeout);
		OutputStream serialOut = SerialTestUtils.openOutputStream(port);

		boolean eof = false;
		while (port.isOpen()) {
			try {
				if (interactiveMode) {
					// wait for input
					waitForInput(serialIn);
				}

				switch (emitMode) {
				case 0:
					// send data, all at once
					eof = emitAll(serialOut);
					break;

				case 1:
				case 3:
					// send data, line by line, or section by section
					eof = emitLine(serialOut, emitMode);
					break;

				case 2:
					eof = emitChar(serialOut);
					break;
				}

				if (eof) {
					// End of data
					if (!repeat) {
						port.close();
					} else
						initializeDataStreams();
				}
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
				break;
			}
		}

		try {
			if (port.isOpen())
				port.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	protected void waitForInput(InputStream input) throws IOException {
		StringBuffer line = new StringBuffer();
		do {
			try {
				int i = input.read();
				if (i < 0)
					return;

				if (i == '\n' || i == 3)
					// Shells expects a command line or an interrupt signal
					break;

				line.append((char) i);
			} catch (InterruptedIOException ioe) {
				ioe.printStackTrace();
				fail("Timed out waiting for input: " + ioe.getMessage());
			}
		} while (emulateShell);

		if (enableLogging)
			LOG.info("Emitter (received): " + line);
	}

	protected boolean emitAll(OutputStream out) throws IOException {
		byte buffer[] = new byte[readBufferSize];
		int length;
		do {
			length = dataStream.read(buffer);
			if (length != -1) {
				out.write(buffer, 0, length);
			}

		} while (length != -1);

		out.flush();
		return true;
	}

	protected boolean emitChar(OutputStream out) throws IOException {
		int c = dataStream.read();
		if (c == -1)
			return true;

		out.write(c);
		out.flush();
		return false;
	}

	protected boolean emitLine(OutputStream out, int emitMode) throws IOException {
		byte buffer[] = new byte[readBufferSize];
		int length;

		boolean eof = false;
		do {
			// Any lines available from previous iteration
			if (availableLines.size() > 0) {
				String line = availableLines.remove(0);

				if (emitMode == 1) {
					out.write(line.getBytes());

					if (enableLogging)
						LOG.info("Emitter (send):" + line);

					break;
				}

				if (emitMode == 3) {
					if (line.trim().equals("&&")) {
						// '&&' all by itself on an entire line, means end of section
						break;
					} else {
						out.write(line.getBytes());

						if (enableLogging)
							LOG.info("Emitter (send): " + line);
					}
					
					if (availableLines.size() == 0 && eof)
						// all lines sent, and EOF reached
						break;
				}
			} else {
				// Read more data
				length = dataStream.read(buffer);
				for (int i = 0; i < length; i++) {
					currentLine.append((char) buffer[i]);
					if (buffer[i] == '\n') {
						availableLines.add(currentLine.toString());
						currentLine = new StringBuffer();
					}
				}

				if (length == -1) {
					eof = true;
					if (currentLine.length() > 0) {
						availableLines.add(currentLine.toString());
					}
				}
			}
		} while (true);

		out.flush();
		return eof;
	}
}
