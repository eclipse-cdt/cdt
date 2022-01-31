package org.eclipse.remote.proxy.tests;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.remote.proxy.protocol.core.StreamChannelManager;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;

import junit.framework.TestCase;

public class MultiplexServerTests extends TestCase {
	private static final int NUM_CHANS = 5;

	private class ChanReader implements Runnable {
		private byte[] buf = new byte[8192];
		private StreamChannel chan;
		private StringBuffer[] recvBufs;
		private String name;

		public ChanReader(StreamChannel chan, StringBuffer[] recvBufs, String name) {
			this.chan = chan;
			this.recvBufs = recvBufs;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public void run() {
			try {
				synchronized (MultiplexServerTests.this) {
					System.out.println(getName() + " started");
				}
				int n;
				while ((n = chan.getInputStream().read(buf)) >= 0) {
					if (n > 0) {
						String s = new String(buf, 0, n);
						recvBufs[chan.getId()].append(s);
					}
				}
				synchronized (MultiplexServerTests.this) {
					System.out.println(getName() + " finished");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ChanWriter implements Runnable {
		private StreamChannel chan;
		private StringBuffer[] sentBufs;
		private Random r = new Random();
		private String name;

		public ChanWriter(StreamChannel chan, StringBuffer[] sentBufs, String name) {
			this.chan = chan;
			this.sentBufs = sentBufs;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public void run() {
			try {
				synchronized (MultiplexServerTests.this) {
					System.out.println(getName() + " started");
				}
				for (int i = 0; i < 100; i++) {
					String s = String.format("%05d\n", i);
					chan.getOutputStream().write(s.getBytes());
					//				chan.getOutputStream().flush();
					sentBufs[chan.getId()].append(s);
					try {
						Thread.sleep(r.nextInt(100));
					} catch (InterruptedException e) {
						fail(e.getMessage());
					}
				}
				chan.getOutputStream().flush();
				synchronized (MultiplexServerTests.this) {
					System.out.println(getName() + " finished");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void testChannels() {
		try {
			final StringBuffer[] clntSentBufs = new StringBuffer[NUM_CHANS];
			final StringBuffer[] clntRecvBufs = new StringBuffer[NUM_CHANS];

			final Thread[] clntReaders = new Thread[NUM_CHANS];
			final Thread[] clntWriters = new Thread[NUM_CHANS];

			for (int i = 0; i < NUM_CHANS; i++) {
				clntSentBufs[i] = new StringBuffer();
				clntRecvBufs[i] = new StringBuffer();
			}

			final Process proc = Runtime.getRuntime().exec("java -jar /Users/gw6/Desktop/Server.jar");
			assertTrue(proc.isAlive());

			new Thread("stderr") {
				private byte[] buf = new byte[1024];

				@Override
				public void run() {
					int n;
					BufferedInputStream err = new BufferedInputStream(proc.getErrorStream());
					try {
						while ((n = err.read(buf)) >= 0) {
							if (n > 0) {
								System.err.println("server: " + new String(buf, 0, n));
							}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}.start();

			StreamChannelManager mpxClnt = startMpxClient(proc.getInputStream(), proc.getOutputStream());

			List<StreamChannel> channels = runChannelTest(mpxClnt, clntReaders, clntWriters, clntSentBufs,
					clntRecvBufs);

			for (int i = 0; i < NUM_CHANS; i++) {
				clntWriters[i].join();
			}

			for (StreamChannel channel : channels) {
				channel.close();
			}

			for (int i = 0; i < NUM_CHANS; i++) {
				if (clntReaders[i] != null) {
					clntReaders[i].join();
				}
			}

			proc.destroy();
			proc.waitFor();
			assertEquals(0, proc.exitValue());
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
		}
	}

	private List<StreamChannel> runChannelTest(StreamChannelManager mpx, Thread[] readers, Thread[] writers,
			final StringBuffer[] sentBufs, final StringBuffer[] recvBufs) throws IOException {
		List<StreamChannel> channels = new ArrayList<StreamChannel>();
		for (int i = 0; i < NUM_CHANS; i++) {
			StreamChannel chan = mpx.openChannel(); // needs to be in same thread as reader
			//			ChanReader reader = new ChanReader(chan, recvBufs, "clnt reader thread " + chan.getId());
			//			readers[chan.getId()] = new Thread(reader, reader.getName());
			ChanWriter writer = new ChanWriter(chan, sentBufs, "clnt writer thread " + chan.getId());
			writers[chan.getId()] = new Thread(writer, writer.getName());
			//			readers[chan.getId()].start();
			writers[chan.getId()].start();
			channels.add(chan);
		}
		return channels;
	}

	private StreamChannelManager startMpxClient(InputStream in, OutputStream out) {
		final StreamChannelManager mpx = new StreamChannelManager(in, out);
		new Thread(mpx, "client multiplexer").start();
		return mpx;
	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}
}
