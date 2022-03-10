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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StreamChannelManager implements Runnable {
	public interface IChannelListener {
		public void newChannel(StreamChannel chan);

		public void closeChannel(StreamChannel chan);
	}

	private class Sender implements Runnable {
		private OutputStream out;
		private BlockingQueue<ByteArrayOutputStream> queue = new LinkedBlockingQueue<ByteArrayOutputStream>();
		private boolean running = true;

		public Sender(OutputStream out) {
			this.out = out;
		}

		public void sendOpenCmd(int id) throws IOException {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			data.writeByte(CMD_OPEN);
			data.writeByte(id);
			try {
				queue.put(bytes);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void sendTransmitCmd(int id, byte buf[], int off, int len) throws IOException {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			data.writeByte(CMD_TRANSMIT);
			data.writeByte(id);
			data.writeInt(len);
			data.write(buf, off, len);
			try {
				queue.put(bytes);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void sendCloseCmd(int id) throws IOException {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			data.writeByte(CMD_CLOSE);
			data.writeByte(id);
			try {
				queue.put(bytes);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void sendCloseAckCmd(int id) throws IOException {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			data.writeByte(CMD_CLOSEACK);
			data.writeByte(id);
			try {
				queue.put(bytes);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void sendRequestCmd(int id, int len) throws IOException {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			data.writeByte(CMD_REQUEST);
			data.writeByte(id);
			data.writeInt(len);
			try {
				queue.put(bytes);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void sendCloseInputCmd(int id) throws IOException {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			data.writeByte(CMD_CLOSE_INPUT);
			data.writeByte(id);
			try {
				queue.put(bytes);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void sendCloseOutputCmd(int id) throws IOException {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			data.writeByte(CMD_CLOSE_OUTPUT);
			data.writeByte(id);
			try {
				queue.put(bytes);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void shutdown() {
			running = false;
			try {
				queue.put(new ByteArrayOutputStream());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {
				while (running) {
					ByteArrayOutputStream bytes = queue.take();
					if (bytes != null) {
						bytes.writeTo(out);
						out.flush();
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private boolean isMyChannel(int id) {
		return !(isServer ^ ((id & SERVER_ID_MASK) == SERVER_ID_MASK));
	}

	private class Receiver implements Runnable {
		private DataInputStream dataIn;

		public Receiver(InputStream in) {
			this.dataIn = new DataInputStream(in);
		}

		@Override
		public void run() {
			StreamChannel chan;
			running = true;
			try {
				while (true) {
					debugPrint("start read");
					int cmd = dataIn.readByte() & 0xff;
					int id = dataIn.readByte() & 0xff;

					switch (cmd) {
					case CMD_OPEN:
						debugPrint("received cmd=OPEN id=" + id);
						chan = channels.get(id);
						if (chan != null) {
							throw new IOException("Channel already exists");
						}
						if (!isServer && (id & SERVER_ID_MASK) != SERVER_ID_MASK) {
							throw new IOException("Client received invalid server channel id: " + id);
						}
						if (isServer && (id & SERVER_ID_MASK) == SERVER_ID_MASK) {
							throw new IOException("Server received invalid client channel id: " + id);
						}
						chan = new StreamChannel(StreamChannelManager.this, id);
						channels.put(id, chan);
						newChannelCallback(chan);
						break;

					case CMD_CLOSE:
						/*
						 * Received a command to close the channel.
						 * Clean up channel and free channel ID if we allocated it.
						 */
						debugPrint("received cmd=CLOSE id=" + id);
						chan = channels.get(id);
						if (chan == null) {
							throw new IOException("CLOSE: Invalid channel id: " + id);
						}
						chan.disconnect();
						if (chan.isOpen()) {
							sendCloseAckCmd(chan);
						}
						closeChannelCallback(chan);
						channels.remove(id);
						if (isMyChannel(id)) {
							freeId(id);
						}
						break;

					case CMD_CLOSEACK:
						/*
						 * Received acknowledgement for our close command.
						 * Clean up channel and free channel ID if we allocated it.
						 */
						debugPrint("received cmd=CLOSEACK id=" + id);
						chan = channels.get(id);
						if (chan == null) {
							throw new IOException("CLOSEACK: Invalid channel id");
						}
						if (chan.isOpen()) {
							throw new IOException("Channel is still open");
						}
						chan.disconnect();
						channels.remove(id);
						if (isMyChannel(id)) {
							freeId(id);
						}
						break;

					case CMD_TRANSMIT:
						debugPrint("received cmd=TRANSMIT id=" + id);
						chan = channels.get(id);
						if (chan == null) {
							throw new IOException("TRANSMIT: Invalid channel id: " + id);
						}
						int len = dataIn.readInt();
						byte[] buf = new byte[len];
						dataIn.readFully(buf, 0, len);
						chan.receive(buf, len);
						break;

					case CMD_REQUEST:
						chan = channels.get(id);
						if (chan == null) {
							throw new IOException("REQUEST: Invalid channel id: " + id);
						}
						int req = dataIn.readInt();
						debugPrint("received cmd=REQUEST id=" + id + " len=" + req);
						chan.request(req);
						break;

					case CMD_CLOSE_INPUT:
						/*
						 * Received a command to close the input side of the channel.
						 */
						debugPrint("received cmd=CLOSE_INPUT id=" + id);
						chan = channels.get(id);
						if (chan == null) {
							throw new IOException("CLOSE: Invalid channel id: " + id);
						}
						chan.disconnectInput();
						break;

					case CMD_CLOSE_OUTPUT:
						/*
						 * Received a command to close the output side of the channel.
						 */
						debugPrint("received cmd=CLOSE_OUTPUT id=" + id);
						chan = channels.get(id);
						if (chan == null) {
							throw new IOException("CLOSE: Invalid channel id: " + id);
						}
						chan.disconnectOutput();
						break;

					default:
						synchronized (System.err) {
							System.err.print("invalid command: " + dump_byte((byte) cmd) + dump_byte((byte) id));
						}
						try {
							while (true) {
								byte b = dataIn.readByte();
								System.err.print(dump_byte(b));
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						throw new IOException("Invalid command: " + cmd);
					}
				}
			} catch (EOFException e) {
				// Finish
			} catch (Exception e) {
				e.printStackTrace();
				debugPrint("run got exception:" + e.getMessage());
			} finally {
				debugPrint("shutting down manager");
				StreamChannelManager.this.shutdown();
			}
		}

		public void shutdown() {
			try {
				dataIn.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}

	private final static int CMD_OPEN = 0xA1; // Open a new channel
	private final static int CMD_CLOSE = 0xA2; // Close a channel; acknowledgement required
	private final static int CMD_CLOSEACK = 0xA3; // Channel close acknowledgement
	private final static int CMD_REQUEST = 0xA4; // Request buffer space
	private final static int CMD_TRANSMIT = 0xA5; // Transmit a buffer
	private final static int CMD_CLOSE_INPUT = 0xA6; // Close input side of the channel; no acknowledgement required
	private final static int CMD_CLOSE_OUTPUT = 0xA7; // Close output side of the channel; no acknowledgement required

	private final static int SERVER_ID_MASK = 1 << 15;
	private final static int MAX_CHANNELS = SERVER_ID_MASK >> 1;

	private final Map<Integer, StreamChannel> channels = (Map<Integer, StreamChannel>) Collections
			.synchronizedMap(new HashMap<Integer, StreamChannel>());
	private final List<IChannelListener> listeners = (List<IChannelListener>) Collections
			.synchronizedList(new ArrayList<IChannelListener>());

	private Set<Short> usedIds = new HashSet<>();
	private int nextUnusedChannelId;
	private boolean isServer;

	private volatile boolean running = true;

	private Sender sender;
	private Receiver receiver;

	private boolean debug = false;

	public StreamChannelManager(InputStream in, OutputStream out) {
		sender = new Sender(new BufferedOutputStream(out));
		receiver = new Receiver(new BufferedInputStream(in));
	}

	/**
	 * Clients allocate IDs with leading bit 0
	 * Servers allocate IDs with leading bit 1
	 *
	 * Reuse an ID if it is not longer being used.
	 *
	 * @return new ID
	 */
	synchronized int newId() throws IOException {
		if (!usedIds.isEmpty()) {
			Short id = usedIds.iterator().next();
			usedIds.remove(id);
			debugPrint("recover id=" + id);
			return id;
		}
		int nextId = nextUnusedChannelId;
		if (nextUnusedChannelId++ > (MAX_CHANNELS - 1)) {
			throw new IOException("Maximum number of channels exceeded");
		}
		return nextId | (isServer ? SERVER_ID_MASK : 0);
	}

	synchronized void freeId(int id) {
		debugPrint("free id=" + id);
		usedIds.add((short) id);
	}

	void dump_buf(String pref, byte[] b, int off, int len) {
		System.err.print(pref + ": ");
		for (int i = off; i < len + off; i++) {
			if (b[i] <= 32 || b[i] > 126) {
				System.err.print(String.format(" 0x%02x ", b[i]));
			} else {
				System.err.print((char) b[i]);
			}
		}
		System.err.println();
	}

	public boolean isServer() {
		return isServer;
	}

	public void setServer(boolean server) {
		isServer = server;
	}

	public void addListener(IChannelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(IChannelListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	protected void newChannelCallback(StreamChannel chan) {
		for (IChannelListener listener : listeners.toArray(new IChannelListener[listeners.size()])) {
			listener.newChannel(chan);
		}
	}

	protected void closeChannelCallback(StreamChannel chan) {
		for (IChannelListener listener : listeners.toArray(new IChannelListener[listeners.size()])) {
			listener.closeChannel(chan);
		}
	}

	public String dump_byte(byte b) {
		if (b <= 32 || b > 126) {
			return String.format(" 0x%02x ", b);
		}

		return String.valueOf((char) b);
	}

	public StreamChannel openChannel() throws IOException {
		if (!running) {
			throw new IOException("Multiplexer is not running");
		}

		StreamChannel chan = new StreamChannel(this, newId());
		channels.put(chan.getId(), chan);

		debugPrint("send cmd=OPEN id=" + chan.getId());
		sender.sendOpenCmd(chan.getId());

		return chan;
	}

	synchronized void sendTransmitCmd(StreamChannel chan, byte buf[], int off, int len) throws IOException {
		if (running && chan.isOpen()) {
			debugPrint(
					"send cmd=TRANSMIT id=" + chan.getId() + " len=" + len + " off=" + off + " buflen=" + buf.length);
			sender.sendTransmitCmd(chan.getId(), buf, off, len);
		}
	}

	synchronized void sendCloseCmd(StreamChannel chan) throws IOException {
		if (running && chan.isOpen()) {
			debugPrint("send cmd=CLOSE id=" + chan.getId());
			chan.disconnect();
			sender.sendCloseCmd(chan.getId());
			chan.setClosed();
		}
	}

	synchronized void sendCloseAckCmd(StreamChannel chan) throws IOException {
		if (running && chan.isOpen()) {
			debugPrint("send cmd=CLOSEACK id=" + chan.getId());
			sender.sendCloseAckCmd(chan.getId());
			chan.setClosed();
		}
	}

	synchronized void sendRequestCmd(StreamChannel chan, int len) throws IOException {
		if (running && chan.isOpen()) {
			debugPrint("send cmd=REQUEST id=" + chan.getId() + " len=" + len);
			sender.sendRequestCmd(chan.getId(), len);
		}
	}

	synchronized void sendCloseInputCmd(StreamChannel chan) throws IOException {
		if (running && chan.isOpen()) {
			if (!chan.isOutputConnected()) {
				sendCloseCmd(chan);
			} else {
				debugPrint("send cmd=CLOSE_INPUT id=" + chan.getId());
				sender.sendCloseInputCmd(chan.getId());
			}
		}
	}

	synchronized void sendCloseOutputCmd(StreamChannel chan) throws IOException {
		if (running && chan.isOpen()) {
			if (!chan.isInputConnected()) {
				sendCloseCmd(chan);
			} else {
				debugPrint("send cmd=CLOSE_OUTPUT id=" + chan.getId());
				sender.sendCloseOutputCmd(chan.getId());
			}
		}
	}

	public void debugPrint(String x) {
		if (debug) {
			synchronized (System.err) {
				System.err.println(x);
			}
		}
	}

	public void shutdown() {
		if (!running) {
			return;
		}
		running = false;

		synchronized (channels) {
			for (StreamChannel c : channels.values()) {
				c.disconnect();
			}
		}
		channels.clear();

		sender.shutdown();
		receiver.shutdown();
		debugPrint("chan mpx stopped");
		// Should in and out be closed also?
	}

	private String asString(int v) {
		switch (v) {
		case CMD_OPEN:
			return "OPEN";

		case CMD_CLOSE:
			return "CLOSE";

		case CMD_CLOSEACK:
			return "CLOSEACK";

		case CMD_TRANSMIT:
			return "TRANSMIT";

		case CMD_REQUEST:
			return "REQUEST";
		}
		return "<UNKNOWN>";
	}

	@Override
	public void run() {
		debugPrint("mux starting");
		new Thread(sender, "mux sender").start();
		receiver.run();
	}
}
