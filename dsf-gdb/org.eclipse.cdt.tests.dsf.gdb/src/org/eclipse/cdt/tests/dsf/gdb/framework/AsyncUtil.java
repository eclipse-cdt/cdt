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
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
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
	 * @return			the {@link AsyncCompletionWaitor} to wait for the result
	 * @throws InterruptedException
	 */
	public static AsyncCompletionWaitor readMemory(final IMemoryDMContext dmc,
			final IAddress address, final long offset, final int word_size,
			final int count) throws InterruptedException {
		final AsyncCompletionWaitor waitor = new AsyncCompletionWaitor();

		// Set the Data Request Monitor
		final DataRequestMonitor<MemoryByte[]> drm = new DataRequestMonitor<MemoryByte[]>(
				fSession.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					waitor.setReturnInfo(getData());
				}
				waitor.waitFinished(getStatus());
			}
		};

		// Issue the get memory request
		fSession.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				fMemory.getMemory(dmc, address, offset, word_size, count, drm);
			}
		});

		return waitor;
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
	 * @param waitor	the waitor to use for the request, or null to use a new waitor
	 * @return			the {@link AsyncCompletionWaitor} to wait for the result
	 * @throws InterruptedException
	 */
	public static AsyncCompletionWaitor writeMemory(final IMemoryDMContext dmc,
			final IAddress address, final long offset, final int word_size,
			final int count, final byte[] buffer, final AsyncCompletionWaitor waitor) throws InterruptedException {
		// Set the Data Request Monitor
		final RequestMonitor rm = new RequestMonitor(fSession.getExecutor(),
				null) {
			@Override
			protected void handleCompleted() {
				waitor.waitFinished(getStatus());
			}
		};

		// Issue the get memory request
		fSession.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				fMemory.setMemory(dmc, address, offset, word_size, count,
						buffer, rm);
			}
		});
		return waitor;
	}
	
	/**
	 * See {@link AsyncUtil#writeMemory(IMemoryDMContext, IAddress, long, int, int, byte[], AsyncCompletionWaitor)}.
	 */
	public static AsyncCompletionWaitor writeMemory(final IMemoryDMContext dmc,
			final IAddress address, final long offset, final int word_size,
			final int count, final byte[] buffer) throws InterruptedException {
		AsyncCompletionWaitor waitor = new AsyncCompletionWaitor();

		return writeMemory(dmc, address, offset, word_size, count, buffer,
				waitor);
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
	 * @return			the {@link AsyncCompletionWaitor} to wait for the result
	 * @throws InterruptedException
	 */
	public static AsyncCompletionWaitor fillMemory(final IMemoryDMContext dmc,
			final IAddress address, final long offset, final int word_size,
			final int count, final byte[] pattern) throws InterruptedException {
		final AsyncCompletionWaitor waitor = new AsyncCompletionWaitor();

		// Set the Data Request Monitor
		final RequestMonitor rm = new RequestMonitor(fSession.getExecutor(),
				null) {
			@Override
			protected void handleCompleted() {
				waitor.waitFinished(getStatus());
			}
		};

		// Issue the fill memory request
		fSession.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				fMemory.fillMemory(dmc, address, offset, word_size, count,
						pattern, rm);
			}
		});

		return waitor;
	}

	/**
	 * Issue a memory read request. On completion, set the first byte of buffer
	 * to the value of the first byte of the read result.
	 *
	 * See {@link AsyncUtil#readMemory(IMemoryDMContext, IAddress, long, int, int)}
	 * for the typical usage.
	 *
	 * @param dmc		the data model context
	 * @param address	the memory block address
	 * @param offset	the offset in the buffer
	 * @param word_size	the word size, in octets
	 * @param count		the number of bytes to read
	 * @param result	the expected byte
	 * @param waitor 	the waitor to use for the request, or null to use a new waitor
	 * @return			the {@link AsyncCompletionWaitor} to wait for the result
	 * @throws InterruptedException
	 *
	 */
	public static AsyncCompletionWaitor readMemoryByteAtOffset(
			final IMemoryDMContext dmc, final IAddress address,
			final long offset, final int word_size, final int count,
			final MemoryByte[] result, final AsyncCompletionWaitor waitor)
			throws InterruptedException {
		// Set the Data Request Monitor
		final DataRequestMonitor<MemoryByte[]> drm = new DataRequestMonitor<MemoryByte[]>(
				fSession.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					result[(int) offset] = getData()[0];
				}
				waitor.waitFinished(getStatus());
			}
		};

		// Issue the get memory request
		fSession.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				fMemory.getMemory(dmc, address, offset, word_size, count, drm);
			}
		});

		return waitor;
	}

	/**
	 * See {@link AsyncUtil#readMemoryByteAtOffset(IMemoryDMContext, IAddress, long, int, int, MemoryByte[], AsyncCompletionWaitor)}.
	 */
	public static AsyncCompletionWaitor readMemoryByteAtOffset(
			final IMemoryDMContext dmc, final IAddress address,
			final long offset, final int word_size, final int count,
			final MemoryByte[] result) throws InterruptedException {
		AsyncCompletionWaitor waitor = new AsyncCompletionWaitor();

		return readMemoryByteAtOffset(dmc, address, offset, word_size, count,
				result, waitor);
	}
}
