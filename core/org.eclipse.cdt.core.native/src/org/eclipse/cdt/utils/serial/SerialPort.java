/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.serial;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.core.natives.Messages;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * @since 5.8
 */
public class SerialPort {

	private final String portName;
	private boolean isOpen;
	private BaudRate baudRate = BaudRate.B115200;
	private ByteSize byteSize = ByteSize.B8;
	private Parity parity = Parity.None;
	private StopBits stopBits = StopBits.S1;
	private long handle;

	private static final String SERIAL_KEY = "HARDWARE\\DEVICEMAP\\SERIALCOMM"; //$NON-NLS-1$
	private static final String PORT_OPEN = Messages.SerialPort_PORT_IS_OPEN;

	static {
		System.loadLibrary("serial"); //$NON-NLS-1$
	}

	/**
	 * Create a serial port that connect to the given serial device.
	 * 
	 * @param portName name for the serial device.
	 */
	public SerialPort(String portName) {
		this.portName = portName;
	}

	/**
	 * List the available serial ports.
	 * 
	 * @return serial ports
	 */
	public static String[] list() {
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			File dev = new File("/dev"); //$NON-NLS-1$
			final Pattern pattern = Pattern.compile("tty\\.(usbserial|usbmodem).*"); //$NON-NLS-1$
			File[] files = dev.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return pattern.matcher(name).matches();
				}
			});
			
			if (files == null) {
				return new String[0];
			}
			String[] names = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				names[i] = files[i].getAbsolutePath();
			}
			return names;
		} else if (Platform.getOS().equals(Platform.OS_WIN32)) {
			WindowsRegistry reg = WindowsRegistry.getRegistry();
			if (reg != null) {
				List<String> ports = new ArrayList<>();
				int i = 0;
				String name = reg.getLocalMachineValueName(SERIAL_KEY, i);
				while (name != null) {
					String value = reg.getLocalMachineValue(SERIAL_KEY, name);
					ports.add(value);
					i++;
					name = reg.getLocalMachineValueName(SERIAL_KEY, i);
				}
				return ports.toArray(new String[ports.size()]);
			} else {
				return new String[0];
			}
		} else {
			return new String[0];
		}
	}

	/**
	 * Return the name for this serial port.
	 * 
	 * @return serial port name
	 */
	public String getPortName() {
		return portName;
	}

	public void open() throws IOException {
		handle = open0(portName, baudRate.getRate(), byteSize.getSize(), parity.ordinal(), stopBits.ordinal());
		isOpen = true;
	}

	private native long open0(String portName, int baudRate, int byteSize, int parity, int stopBits) throws IOException;

	public void close() throws IOException {
		close0(handle);
		isOpen = false;
		handle = 0;
	}

	private native void close0(long handle) throws IOException;

	public boolean isOpen() {
		return isOpen;
	}

	public void setBaudRate(BaudRate rate) throws IOException {
		if (isOpen) {
			throw new IOException(PORT_OPEN);
		}
		this.baudRate = rate;
	}

	public BaudRate getBaudRate() {
		return baudRate;
	}

	public void setByteSize(ByteSize size) throws IOException {
		if (isOpen) {
			throw new IOException(PORT_OPEN);
		}
		this.byteSize = size;
	}

	public ByteSize getByteSize() {
		return byteSize;
	}

	public void setParity(Parity parity) throws IOException {
		if (isOpen) {
			throw new IOException(PORT_OPEN);
		}
		this.parity = parity;
	}

	public Parity getParity() {
		return parity;
	}

	public void setStopBits(StopBits stopBits) throws IOException {
		if (isOpen) {
			throw new IOException(PORT_OPEN);
		}
		this.stopBits = stopBits;
	}

	public StopBits getStopBits() {
		return stopBits;
	}

	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				if (isOpen()) {
					return read0(handle);
				} else {
					return -1;
				}
			}

			@Override
			public void close() throws IOException {
				SerialPort.this.close();
			}
		};
	}

	private native int read0(long handle) throws IOException;

	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				if (isOpen()) {
					write0(handle, b);
				}
			}

			@Override
			public void close() throws IOException {
				SerialPort.this.close();
			}
		};
	}

	private native void write0(long handle, int b) throws IOException;

}
