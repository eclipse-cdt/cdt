/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Marchi (Ericsson) - Initial Implementation
 *******************************************************************************/

package org.eclipse.cdt.tests.dsf.gdb.framework;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.debug.core.model.MemoryByte;

public class AsyncUtil {
    private static DsfSession fSession;
    private static IMemory fMemory;

    public static void initialize(DsfSession session) throws Exception {
		fSession = session;

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				DsfServicesTracker tracker = new DsfServicesTracker(
						TestsPlugin.getBundleContext(), fSession.getId());

				fMemory = tracker.getService(IMemory.class);

				tracker.dispose();
			}
		};
		fSession.getExecutor().submit(runnable).get();
	}

	/**
	 * Issue a memory read request. The result is stored in fWait.
	 *
	 * Typical usage:
	 *  wait = AsyncUtil.readMemory(dmc, address, offset, word_size, count);
	 *  wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
	 *  assertTrue(wait.getMessage(), wait.isOK());
	 *
	 * @param dmc		the data model context
	 * @param address	the memory block address
	 * @param offset	the offset in the buffer
	 * @param word_size	the size of a word, in octets
	 * @param count		the number of bytes to read
	 * @return			the {@link Query} to wait for the result
	 * @throws InterruptedException
	 */
	public static Query<MemoryByte[]> readMemory(final IMemoryDMContext dmc,
			final IAddress address, final long offset, final int word_size,
			final int count) throws InterruptedException {
		Query<MemoryByte[]> query = new Query<MemoryByte[]>() {
			@Override
			protected void execute(DataRequestMonitor<MemoryByte[]> rm) {
				fMemory.getMemory(dmc, address, offset, word_size, count, rm);
			}
		};

		fMemory.getExecutor().execute(query);

		return query;
	}

	/**
	 * Issue a memory write request.
	 *
	 * See {@link AsyncUtil#readMemory(IMemoryDMContext, IAddress, long, int, int)}
	 * for the typical usage.
	 *
	 * @param dmc		the data model context
	 * @param address	the memory block address (could be an expression)
	 * @param offset	the offset from address
	 * @param word_size	the word size, in octets
	 * @param count		the number of bytes to write
	 * @param buffer	the byte buffer to write from
	 * @return			the {@link Query} to wait for the result
	 * @throws InterruptedException
	 */
	public static Query<Void> writeMemory(final IMemoryDMContext dmc,
			final IAddress address, final long offset, final int word_size,
			final int count, final byte[] buffer) throws InterruptedException {
		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(DataRequestMonitor<Void> rm) {
				fMemory.setMemory(dmc, address, offset, word_size, count,
						buffer, rm);
			}
		};

		fMemory.getExecutor().execute(query);

		return query;
	}

	/**
	 *
	 * Issue a request to fill memory with a pattern.
	 *
	 * See {@link AsyncUtil#readMemory(IMemoryDMContext, IAddress, long, int, int)}
	 * for the typical usage.
	 *
	 * @param dmc		the data model context
	 * @param address	the memory block address (could be an expression)
	 * @param offset	the offset from address
	 * @param word_size	the word size, in octets
	 * @param count		the number of bytes to write
	 * @param pattern	the byte pattern to write
	 * @return			the {@link Query} to wait for the result
	 * @throws InterruptedException
	 */
	public static Query<Void> fillMemory(final IMemoryDMContext dmc,
			final IAddress address, final long offset, final int word_size,
			final int count, final byte[] pattern) throws InterruptedException {
		Query<Void> query = new Query<Void>() {
			@Override
			protected void execute(DataRequestMonitor<Void> rm) {
				fMemory.fillMemory(dmc, address, offset, word_size, count,
						pattern, rm);
			}
		};

		fMemory.getExecutor().execute(query);

		return query;
	}
}
