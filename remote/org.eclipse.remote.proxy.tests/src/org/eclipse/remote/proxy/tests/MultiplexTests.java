package org.eclipse.remote.proxy.tests;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.remote.proxy.protocol.core.StreamChannelManager;
import org.eclipse.remote.proxy.protocol.core.StreamChannelManager.IChannelListener;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;

import junit.framework.TestCase;

public class MultiplexTests extends TestCase {
	private static final int NUM_CHANS_PER_THREAD = 5;
	private static final int NUM_THREADS = 5;
	private static final int FINISH = -1;

	private class ChanReader implements Runnable {
		private DataInputStream in;
		private List<Integer> recvBufs;
		private String name;

		public ChanReader(StreamChannel chan, List<Integer> recvBufs, String name) {
			this.in = new DataInputStream(chan.getInputStream());
			this.recvBufs = recvBufs;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public void run() {
			try {
				synchronized (MultiplexTests.this) {
					System.out.println(getName() + " started");
				}
				try {
					while (true) {
						int val = in.readInt();
						if (val == FINISH) {
							break;
						}
						recvBufs.add(val);
					}
				} catch (EOFException e) {
					// Finish
				}
				synchronized (MultiplexTests.this) {
					System.out.println(getName() + " finished");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ChanWriter implements Runnable {
		private DataOutputStream out;
		private List<Integer> sentBufs;
		private Random r = new Random();
		private String name;

		public ChanWriter(StreamChannel chan, List<Integer> sentBufs, String name) {
			this.out = new DataOutputStream(chan.getOutputStream());
			this.sentBufs = sentBufs;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public void run() {
			try {
				synchronized (MultiplexTests.this) {
					System.out.println(getName() + " started");
				}
				for (int i = 0; i < 100; i++) {
					int val = r.nextInt(1024);
					out.writeInt(val);
					out.flush();
					sentBufs.add(val);
					try {
						Thread.sleep(r.nextInt(100));
					} catch (InterruptedException e) {
						fail(e.getMessage());
					}
				}
				out.writeInt(FINISH);
				out.flush();
				synchronized (MultiplexTests.this) {
					System.out.println(getName() + " finished");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ChanReaderWriter implements Runnable {
		private DataInputStream in;
		private DataOutputStream out;
		private String name;

		public ChanReaderWriter(StreamChannel chan, String name) {
			this.in = new DataInputStream(chan.getInputStream());
			this.out = new DataOutputStream(chan.getOutputStream());
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public void run() {
			try {
				synchronized (MultiplexTests.this) {
					System.out.println(getName() + " started");
				}
				try {
					while (true) {
						int val = in.readInt();
						out.writeInt(val);
						out.flush();
					}
				} catch (EOFException e) {
					// Finish
				}
				synchronized (MultiplexTests.this) {
					System.out.println(getName() + " finished");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void testChannels() {
		try {
			final PipedInputStream inClnt = new PipedInputStream();
			final PipedInputStream inSvr = new PipedInputStream();
			final PipedOutputStream outClnt = new PipedOutputStream(inSvr);
			final PipedOutputStream outSvr = new PipedOutputStream(inClnt);

			final List<List<Integer>> clntSentBufs = new ArrayList<List<Integer>>();
			final List<List<Integer>> clntRecvBufs = new ArrayList<List<Integer>>();

			final Thread[][] clntReaders = new Thread[NUM_THREADS][NUM_CHANS_PER_THREAD];
			final Thread[][] clntWriters = new Thread[NUM_THREADS][NUM_CHANS_PER_THREAD];
			final Thread[] svrRW = new Thread[NUM_THREADS * NUM_CHANS_PER_THREAD];
			final Thread[] testers = new Thread[NUM_THREADS];

			for (int i = 0; i < NUM_CHANS_PER_THREAD * NUM_THREADS; i++) {
				clntSentBufs.add(new ArrayList<Integer>());
				clntRecvBufs.add(new ArrayList<Integer>());
			}

			// Must start server first or it will miss the new channel message
			StreamChannelManager mpxSvr = startMpxServer(inSvr, outSvr, svrRW);

			StreamChannelManager mpxClnt = startMpxClient(inClnt, outClnt);

			List<StreamChannel> channels = runChannelTest(mpxClnt, testers, clntReaders, clntWriters, clntSentBufs,
					clntRecvBufs);

			// Make sure all the testers have finished
			for (int i = 0; i < NUM_THREADS; i++) {
				testers[i].join();
			}

			// Wait for the readers and writers to complete
			for (int i = 0; i < NUM_THREADS; i++) {
				for (int j = 0; j < NUM_CHANS_PER_THREAD; j++) {
					clntWriters[i][j].join();
					clntReaders[i][j].join();
				}
			}

			for (StreamChannel channel : channels) {
				channel.close();
			}

			for (int i = 0; i < NUM_THREADS * NUM_CHANS_PER_THREAD; i++) {
				svrRW[i].join();
			}

			for (int i = 0; i < NUM_CHANS_PER_THREAD * NUM_THREADS; i++) {
				assertEquals(clntSentBufs.get(i), clntRecvBufs.get(i));
			}
		} catch (IOException | InterruptedException e) {
			fail(e.getMessage());
		}
	}

	private List<StreamChannel> runChannelTest(final StreamChannelManager mpx, final Thread[] testers,
			final Thread[][] readers, final Thread[][] writers, final List<List<Integer>> sentBufs,
			final List<List<Integer>> recvBufs) throws IOException {
		final List<StreamChannel> channels = new ArrayList<StreamChannel>();
		for (int i = 0; i < NUM_THREADS; i++) {
			final int thread = i;
			testers[i] = new Thread("client test thread " + thread) {
				@Override
				public void run() {
					try {
						for (int j = 0; j < NUM_CHANS_PER_THREAD; j++) {
							StreamChannel chan = mpx.openChannel();
							ChanReader reader = new ChanReader(chan, recvBufs.get(thread * NUM_CHANS_PER_THREAD + j),
									"clnt reader thread=" + thread + " chan=" + chan.getId());
							readers[thread][j] = new Thread(reader, reader.getName());
							ChanWriter writer = new ChanWriter(chan, sentBufs.get(thread * NUM_CHANS_PER_THREAD + j),
									"clnt writer thread=" + thread + " chan=" + chan.getId());
							writers[thread][j] = new Thread(writer, writer.getName());
							readers[thread][j].start();
							writers[thread][j].start();
							channels.add(chan);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			testers[i].start();
		}
		return channels;
	}

	private StreamChannelManager startMpxClient(InputStream in, OutputStream out) {
		final StreamChannelManager mpx = new StreamChannelManager(in, out);
		new Thread(mpx, "client multiplexer").start();
		return mpx;
	}

	private StreamChannelManager startMpxServer(InputStream in, OutputStream out, final Thread[] rws)
			throws IOException {
		final StreamChannelManager mpx = new StreamChannelManager(in, out);
		mpx.setServer(true);
		mpx.addListener(new IChannelListener() {
			private int numThreadChans;

			@Override
			public void newChannel(final StreamChannel chan) {
				synchronized (MultiplexTests.this) {
					System.out.println("newChannel " + chan.getId());
				}

				ChanReaderWriter rw = new ChanReaderWriter(chan, "svr rw thread " + numThreadChans);
				rws[numThreadChans] = new Thread(rw, rw.getName());
				rws[numThreadChans++].start();
			}

			@Override
			public void closeChannel(StreamChannel chan) {
				//					readers[idx].interrupt();
				//					writers[idx].interrupt();
				synchronized (MultiplexTests.this) {
					System.out.println("closeChannel " + chan.getId());
				}
			}
		});
		new Thread(mpx, "server multiplexer").start();
		return mpx;
	}

	@Override
	protected void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
	}
}
