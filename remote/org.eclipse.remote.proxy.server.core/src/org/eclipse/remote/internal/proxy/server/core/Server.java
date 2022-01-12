/*******************************************************************************
 * Copyright (c) 2016 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.remote.internal.proxy.server.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.remote.proxy.protocol.core.StreamChannelManager;
import org.eclipse.remote.proxy.protocol.core.StreamChannelManager.IChannelListener;
import org.eclipse.remote.proxy.protocol.core.StreamChannel;

public class Server {
	private volatile boolean running;
	private Thread serverThread;
	private StreamChannel cmdChannel;
	private Map<Integer, StreamChannel> auxChannel = Collections.synchronizedMap(new HashMap<Integer, StreamChannel>());
	
	
	public void start() {
		final StreamChannelManager mux = new StreamChannelManager(System.in, System.out);
		mux.setServer(true);
		mux.addListener(new IChannelListener() {

			@Override
			public void newChannel(StreamChannel chan) {
				Runnable runnable;
				System.err.println("newChannel: " + chan.getId());
				// First channel opened becomes command channel
				if (cmdChannel == null) {
					cmdChannel = chan;
					runnable = new CommandServer(chan, Server.this);
					new Thread(runnable).start();
				} else {
					auxChannel.put(chan.getId(), chan);
				}
			}

			@Override
			public void closeChannel(StreamChannel chan) {
				System.err.println("closeChannel: " + chan.getId());
				auxChannel.remove(chan.getId());
			}
			
		});
		serverThread = new Thread(mux) {
			@Override
			public void run() {
				running = true;
				mux.run();
				running = false;
			}
		};
		serverThread.start();
	}
	
	public StreamChannel getChannel(int id) {
		System.err.println("getChannel: "+id);
		return auxChannel.get(id);
	}
	
	public void waitFor() {
		if (running && serverThread != null) {
			try {
				serverThread.join();
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}
}
