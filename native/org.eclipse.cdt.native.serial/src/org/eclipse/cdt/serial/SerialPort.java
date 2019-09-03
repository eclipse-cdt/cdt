/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.serial;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.serial.internal.Messages;

/**
 * @since 5.8
 */
public class SerialPort {

	private final String portName;
	private boolean isOpen;
	private boolean isPaused;
	private Object pauseMutex = new Object();
	private BaudRate baudRate = BaudRate.B115200;
	private ByteSize byteSize = ByteSize.B8;
	private Parity parity = Parity.None;
	private StopBits stopBits = StopBits.S1;
	private long handle;

	private static final String PORT_OPEN = Messages.getString("SerialPort.PortIsOpen"); //$NON-NLS-1$

	private static final Map<String, LinkedList<WeakReference<SerialPort>>> openPorts = new HashMap<>();

	static {
		try {
			System.loadLibrary("serial"); //$NON-NLS-1$
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
	}

	private InputStream inputStream = new InputStream() {
		private byte[] rbuff = new byte[256];
		private int rpos = 0;
		private int rlen = 0;

		@Override
		public int read() throws IOException {
			if (isOpen()) {
				if (rpos >= rlen) {
					while (true) {
						rlen = read1(handle, rbuff, 0, rbuff.length);
						if (rlen < 0) {
							if (isPaused) {
								synchronized (pauseMutex) {
									while (isPaused) {
										try {
											pauseMutex.wait();
										} catch (InterruptedException e) {
											return -1;
										}
									}
								}
							} else {
								return -1;
							}
						} else if (rlen > 0) {
							// Reset the pointer, as we have new data
							rpos = 0;
							break;
						}
					}
				}
				return rbuff[rpos++];
			} else {
				return -1;
			}
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (isOpen()) {
				int n = rlen - rpos;
				if (n > 0) {
					if (len < n) {
						n = len;
					}
					System.arraycopy(rbuff, rpos, b, off, n);
					rpos += n;
					return n;
				} else {
					n = read1(handle, b, off, len);
					if (n <= 0 && isPaused) {
						synchronized (pauseMutex) {
							while (isPaused) {
								try {
									pauseMutex.wait();
								} catch (InterruptedException e) {
									return -1;
								}
							}
						}
						return read1(handle, b, off, len);
					} else {
						return n;
					}
				}
			} else {
				return -1;
			}
		}

		@Override
		public void close() throws IOException {
			SerialPort.this.close();
		}

		@Override
		public int available() throws IOException {
			if (isOpen()) {
				return available0(handle);
			} else {
				return 0;
			}
		}
	};

	private OutputStream outputStream = new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			if (isOpen()) {
				try {
					write0(handle, b);
				} catch (IOException e) {
					if (isPaused) {
						synchronized (pauseMutex) {
							while (isPaused) {
								try {
									pauseMutex.wait();
								} catch (InterruptedException e1) {
									throw e;
								}
							}
						}
						write0(handle, b);
					}
				}
			}
		}

		@Override
		public void write(byte[] buff, int off, int len) throws IOException {
			if (isOpen()) {
				try {
					write1(handle, buff, off, len);
				} catch (IOException e) {
					if (isPaused) {
						synchronized (pauseMutex) {
							while (isPaused) {
								try {
									pauseMutex.wait();
								} catch (InterruptedException e1) {
									throw e;
								}
							}
						}
						write1(handle, buff, off, len);
					}
				}
			}
		}

		@Override
		public void close() throws IOException {
			SerialPort.this.close();
		}
	};

	private static String adjustPortName(String portName) {
		if (System.getProperty("os.name").startsWith("Windows") && !portName.startsWith("\\\\.\\")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return "\\\\.\\" + portName; //$NON-NLS-1$
		} else {
			return portName;
		}
	}

	/**
	 * Create a serial port that connect to the given serial device.
	 *
	 * @param portName
	 *            name for the serial device.
	 */
	public SerialPort(String portName) {
		this.portName = adjustPortName(portName);
	}

	private native long open0(String portName, int baudRate, int byteSize, int parity, int stopBits) throws IOException;

	private native void close0(long handle) throws IOException;

	private native int read1(long handle, byte[] b, int off, int len) throws IOException;

	private native int available0(long handle) throws IOException;

	private native void write0(long handle, int b) throws IOException;

	private native void write1(long handle, byte[] b, int off, int len) throws IOException;

	private static native String getPortName(int i) throws IOException;

	private static String[] listDevs(final Pattern pattern) {
		File dev = new File("/dev"); //$NON-NLS-1$
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
	}

	/**
	 * List the available serial ports.
	 *
	 * @return serial ports
	 */
	public static String[] list() throws IOException {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (osName.equals("Mac OS X")) { //$NON-NLS-1$
			return listDevs(Pattern.compile("cu\\..*")); //$NON-NLS-1$
		} else if (osName.equals("Linux")) { //$NON-NLS-1$
			return listDevs(Pattern.compile("(ttyUSB|ttyACM|ttyS).*")); //$NON-NLS-1$
		} else if (osName.startsWith("Windows")) { //$NON-NLS-1$
			List<String> ports = new ArrayList<>();
			int i = 0;
			String name = null;
			do {
				try {
					name = getPortName(i++);
					if (name != null) {
						ports.add(name);
					}
				} catch (IOException e) {
					// TODO log the exception
					e.printStackTrace();
				}
			} while (name != null);
			return ports.toArray(new String[ports.size()]);
		} else {
			return new String[0];
		}
	}

	/**
	 * Return an the SerialPort with the given name or null if it hasn't been allocated yet. This
	 * would be used by components that need to pause and resume a serial port.
	 *
	 * @param portName
	 * @return
	 * @since 1.1
	 */
	public static SerialPort get(String portName) {
		synchronized (openPorts) {
			LinkedList<WeakReference<SerialPort>> list = openPorts.get(adjustPortName(portName));
			if (list == null) {
				return null;
			}

			Iterator<WeakReference<SerialPort>> i = list.iterator();
			while (i.hasNext()) {
				WeakReference<SerialPort> ref = i.next();
				SerialPort port = ref.get();
				if (port == null) {
					i.remove();
				} else {
					return port;
				}
			}

			return null;
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

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public synchronized void open() throws IOException {
		handle = open0(portName, baudRate.getRate(), byteSize.getSize(), parity.ordinal(), stopBits.ordinal());
		isOpen = true;

		synchronized (openPorts) {
			LinkedList<WeakReference<SerialPort>> list = openPorts.get(portName);
			if (list == null) {
				list = new LinkedList<>();
				openPorts.put(portName, list);
			}
			list.addFirst(new WeakReference<>(this));
		}
	}

	public synchronized void close() throws IOException {
		if (isOpen) {
			isOpen = false;
			close0(handle);
			handle = 0;

			synchronized (openPorts) {
				LinkedList<WeakReference<SerialPort>> list = openPorts.get(portName);
				if (list != null) {
					Iterator<WeakReference<SerialPort>> i = list.iterator();
					while (i.hasNext()) {
						WeakReference<SerialPort> ref = i.next();
						SerialPort port = ref.get();
						if (port == null || port.equals(this)) {
							i.remove();
						}
					}
				}
			}

			try {
				// Sleep for a second since some serial ports take a while to actually close
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// nothing to do
			}
		}
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void pause() throws IOException {
		if (!isOpen) {
			return;
		}
		synchronized (pauseMutex) {
			isPaused = true;
			close0(handle);
			try {
				// Sleep for a second since some serial ports take a while to actually close
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// nothing to do
			}
		}
	}

	public void resume() throws IOException {
		synchronized (pauseMutex) {
			if (!isPaused) {
				return;
			}
			isPaused = false;
			handle = open0(portName, baudRate.getRate(), byteSize.getSize(), parity.ordinal(), stopBits.ordinal());
			isOpen = true;
			pauseMutex.notifyAll();
		}
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

}
