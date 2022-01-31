/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.remote.proxy.protocol.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StreamChannel {
	public static final int CAPACITY = 8192;

	private class ChannelInputStream extends InputStream {
		private final Lock lock = new ReentrantLock();
		private final Condition cond = lock.newCondition();
		private final StreamChannel channel;

		private int currentPos;
		private int currentSize;
		private boolean connected = true;
		private int inputRequestCount;
		private byte[] buffer = new byte[CAPACITY];

		public ChannelInputStream(StreamChannel channel) {
			this.channel = channel;
		}

		@Override
		public synchronized int read() throws IOException {
			byte[] b = new byte[1];
			if (read(b, 0, 1) != 1) {
				return -1;
			}
			return b[0] & 0xff;
		}

		@Override
		public int available() throws IOException {
			lock.lock();
			try {
				return currentSize - currentPos;
			} finally {
				lock.unlock();
			}
		}

		public synchronized int read(byte b[], int off, int len) throws IOException {
			if (len <= 0) {
				return 0;
			}
			int moreSpace;
			lock.lock();
			try {
				if (currentPos >= currentSize) {
					currentPos = currentSize = 0;
				} else if (currentPos >= CAPACITY / 2) {
					System.arraycopy(buffer, currentPos, buffer, 0, currentSize - currentPos);
					currentSize -= currentPos;
					currentPos = 0;
				}
				int freeSpace = CAPACITY - currentSize;
				moreSpace = Math.max(freeSpace - inputRequestCount, 0);
			} finally {
				lock.unlock();
			}
			if (moreSpace > 0) {
				mux.sendRequestCmd(StreamChannel.this, moreSpace);
			}
			lock.lock();
			try {
				inputRequestCount += moreSpace;
				while (currentPos >= currentSize && connected) {
					try {
						cond.await();
					} catch (InterruptedException e) {
					}
				}
				if (!connected && currentPos >= currentSize) {
					return -1;
				}

				int available = currentSize - currentPos;
				if (len < available) {
					System.arraycopy(buffer, currentPos, b, off, len);
					currentPos += len;
					return len;
				} else {
					System.arraycopy(buffer, currentPos, b, off, available);
					currentPos = currentSize = 0;
					return available;
				}
			} finally {
				lock.unlock();
			}
		}

		@Override
		public void close() throws IOException {
			channel.closeOutput();
			disconnect();
		}

		void receive(byte[] buf, int len) throws IOException {
			lock.lock();
			try {
				if (currentPos > 0 && (CAPACITY - currentSize) < len) {
					System.arraycopy(buffer, currentPos, buffer, 0, currentSize - currentPos);
					currentSize -= currentPos;
					currentPos = 0;
				}
				if (CAPACITY - currentSize < len) {
					throw new IOException("Receive buffer overflow");
				}
				System.arraycopy(buf, 0, buffer, currentSize, len);
				currentSize += len;
				inputRequestCount -= len;
				cond.signalAll();
			} finally {
				lock.unlock();
			}
		}

		void disconnect() {
			lock.lock();
			try {
				connected = false;
				cond.signalAll();
			} finally {
				lock.unlock();
			}
		}

		boolean isConnected() {
			lock.lock();
			try {
				return connected;
			} finally {
				lock.unlock();
			}
		}
	}

	private class ChannelOutputStream extends OutputStream {
		private final Lock lock = new ReentrantLock();
		private final Condition cond = lock.newCondition();
		private final StreamChannel channel;

		private int currentPos;
		private byte[] buffer = new byte[CAPACITY];
		private boolean connected = true;
		private int outputRequestCount;

		public ChannelOutputStream(StreamChannel channel) {
			this.channel = channel;
		}

		@Override
		public synchronized void write(int b) throws IOException {
			while (currentPos >= CAPACITY) {
				send();
			}
			buffer[currentPos++] = (byte) b;
		}

		@Override
		public synchronized void flush() throws IOException {
			while (currentPos > 0) {
				send();
			}
		}

		@Override
		public synchronized void write(byte[] b, int off, int len) throws IOException {
			if (len <= 0) {
				return;
			}
			if (len <= CAPACITY - currentPos) {
				System.arraycopy(b, off, buffer, currentPos, len);
				currentPos += len;
				return;
			}
			flush();
			int canSend;
			while (true) {
				lock.lock();
				try {
					while ((canSend = outputRequestCount) == 0 && connected) {
						try {
							cond.await();
						} catch (InterruptedException e) {
						}
					}
					if (!connected) {
						throw new IOException("channel closed");
					}
				} finally {
					lock.unlock();
				}

				if (canSend < len) {
					mux.sendTransmitCmd(StreamChannel.this, b, off, canSend);
					off += canSend;
					len -= canSend;
					lock.lock();
					outputRequestCount -= canSend;
					lock.unlock();
				} else {
					mux.sendTransmitCmd(StreamChannel.this, b, off, len);
					lock.lock();
					outputRequestCount -= len;
					lock.unlock();
					break;
				}
			}
		}

		void send() throws IOException {
			int canSend;
			lock.lock();
			try {
				while ((canSend = outputRequestCount) == 0 && connected) {
					try {
						cond.await();
					} catch (InterruptedException e) {
					}
				}
				if (!connected) {
					throw new IOException("channel closed");
				}
			} finally {
				lock.unlock();
			}
			if (canSend < currentPos) {
				mux.sendTransmitCmd(StreamChannel.this, buffer, 0, canSend);
				currentPos -= canSend;
				System.arraycopy(buffer, canSend, buffer, 0, currentPos);
				lock.lock();
				outputRequestCount -= canSend;
				lock.unlock();
			} else {
				mux.sendTransmitCmd(StreamChannel.this, buffer, 0, currentPos);
				lock.lock();
				outputRequestCount -= currentPos;
				lock.unlock();
				currentPos = 0;
			}
		}

		@Override
		public void close() throws IOException {
			flush();
			channel.closeInput();
			disconnect();
		}

		void request(int len) {
			lock.lock();
			outputRequestCount += len;
			cond.signalAll();
			lock.unlock();
		}

		void disconnect() {
			lock.lock();
			try {
				connected = false;
				cond.signalAll();
			} finally {
				lock.unlock();
			}
		}

		boolean isConnected() {
			lock.lock();
			try {
				return connected;
			} finally {
				lock.unlock();
			}

		}
	}

	private final StreamChannelManager mux;
	private final int channelId;
	private final ChannelInputStream min = new ChannelInputStream(this);
	private final ChannelOutputStream mout = new ChannelOutputStream(this);

	private boolean open;

	public StreamChannel(StreamChannelManager mux, int id) {
		this.mux = mux;
		channelId = id;
		open = true;
	}

	public int getId() {
		return channelId;
	}

	public InputStream getInputStream() {
		return min;
	}

	public OutputStream getOutputStream() {
		return mout;
	}

	public boolean isOpen() {
		return open;
	}

	public void close() throws IOException {
		mux.sendCloseCmd(this);
	}

	void receive(byte[] buf, int len) throws IOException {
		min.receive(buf, len);
	}

	void request(int len) {
		mout.request(len);
	}

	void disconnect() {
		min.disconnect();
		mout.disconnect();
	}

	void setClosed() {
		open = false;
	}

	void disconnectInput() {
		min.disconnect();
	}

	void disconnectOutput() {
		mout.disconnect();
	}

	void closeInput() throws IOException {
		mux.sendCloseInputCmd(this);
	}

	void closeOutput() throws IOException {
		mux.sendCloseOutputCmd(this);
	}

	boolean isInputConnected() {
		return min.isConnected();
	}

	boolean isOutputConnected() {
		return mout.isConnected();
	}
}
