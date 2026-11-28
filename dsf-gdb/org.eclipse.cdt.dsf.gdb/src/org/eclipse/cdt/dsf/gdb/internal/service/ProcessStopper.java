/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.service;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IMultiRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;

/**
 * Stops the running threads of processes before GDB exits, kills a process or detaches from it.
 * GDB can hang in these operations while a running program starts threads, see
 * <a href="https://github.com/eclipse-cdt/cdt/issues/1065">#1065</a>.
 */
public final class ProcessStopper {
	/** How long to wait for the threads to stop */
	public static final long STOP_WAIT_TIME_MS = 1000;
	private static final long POLL_INTERVAL_MS = 50;
	private static final long REPEAT_INTERVAL_MS = 200;

	private ProcessStopper() {
	}

	/**
	 * Returns a request monitor that completes rm once: when the returned monitor completes, or
	 * after timeoutMs, whichever comes first. So rm completes also if GDB does not reply.
	 * rm always completes with an OK status.
	 */
	public static RequestMonitor withTimeout(DsfExecutor executor, long timeoutMs, RequestMonitor rm) {
		boolean[] done = new boolean[1];
		Runnable finish = () -> {
			if (!done[0]) {
				done[0] = true;
				rm.done();
			}
		};
		Future<?> timer;
		try {
			timer = executor.schedule(new DsfRunnable() {
				@Override
				public void run() {
					finish.run();
				}

				@Override
				protected boolean isExecutionRequired() {
					return false;
				}
			}, timeoutMs, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) {
			timer = null;
		}
		Future<?> timeoutTask = timer;
		return new RequestMonitor(executor, null) {
			@Override
			protected void handleCompleted() {
				if (timeoutTask != null) {
					timeoutTask.cancel(false);
				}
				finish.run();
			}
		};
	}

	/**
	 * Interrupts the running threads of the containers and waits until none of them runs, or
	 * until the deadline. Does not wait for the replies to the interrupts: a suspend can complete
	 * only on *stopped, which a thread that exits at the same time never sends.
	 *
	 * @param interrupt sends the interrupt for the containers, and ignores the reply
	 * @param repeat whether to send the interrupt again while a thread still runs. In non-stop
	 *            mode, an interrupt can miss threads that start at the same time.
	 * @param deadline {@link System#nanoTime()} after which to stop waiting
	 */
	public static void stopThreads(DsfExecutor executor, IMIRunControl runControl,
			List<? extends IContainerDMContext> containers, Consumer<List<? extends IContainerDMContext>> interrupt,
			boolean repeat, long deadline, RequestMonitor rm) {
		canSuspendAny(executor, runControl, containers, new DataRequestMonitor<Boolean>(executor, rm) {
			@Override
			protected void handleSuccess() {
				if (!getData()) {
					rm.done();
					return;
				}
				interrupt.accept(containers);
				scheduleCheck(executor, runControl, containers, repeat ? interrupt : null, deadline, System.nanoTime(),
						rm);
			}
		});
	}

	private static void scheduleCheck(DsfExecutor executor, IMIRunControl runControl,
			List<? extends IContainerDMContext> containers, Consumer<List<? extends IContainerDMContext>> interrupt,
			long deadline, long lastInterrupt, RequestMonitor rm) {
		try {
			executor.schedule(
					() -> waitUntilStopped(executor, runControl, containers, interrupt, deadline, lastInterrupt, rm),
					POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) {
			rm.done();
		}
	}

	private static void waitUntilStopped(DsfExecutor executor, IMIRunControl runControl,
			List<? extends IContainerDMContext> containers, Consumer<List<? extends IContainerDMContext>> interrupt,
			long deadline, long lastInterrupt, RequestMonitor rm) {
		canSuspendAny(executor, runControl, containers, new DataRequestMonitor<Boolean>(executor, rm) {
			@Override
			protected void handleSuccess() {
				long now = System.nanoTime();
				if (!getData() || now - deadline >= 0) {
					rm.done();
					return;
				}
				long interruptTime = lastInterrupt;
				if (interrupt != null && now - lastInterrupt >= TimeUnit.MILLISECONDS.toNanos(REPEAT_INTERVAL_MS)) {
					interrupt.accept(containers);
					interruptTime = now;
				}
				scheduleCheck(executor, runControl, containers, interrupt, deadline, interruptTime, rm);
			}
		});
	}

	/**
	 * Returns in rm whether a thread of the containers runs.
	 */
	private static void canSuspendAny(DsfExecutor executor, IMIRunControl runControl,
			List<? extends IContainerDMContext> containers, DataRequestMonitor<Boolean> rm) {
		if (runControl instanceof IMultiRunControl multiRunControl) {
			multiRunControl.canSuspendSome(containers.toArray(new IExecutionDMContext[containers.size()]), rm);
			return;
		}
		boolean[] any = new boolean[1];
		CountingRequestMonitor crm = new CountingRequestMonitor(executor, rm) {
			@Override
			protected void handleSuccess() {
				rm.done(any[0]);
			}
		};
		for (IContainerDMContext container : containers) {
			runControl.canSuspend(container, new DataRequestMonitor<Boolean>(executor, crm) {
				@Override
				protected void handleSuccess() {
					any[0] |= getData();
					crm.done();
				}
			});
		}
		crm.setDoneCount(containers.size());
	}
}
