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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.mi.service.command.events.MIRunningEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MISignalEvent;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;

/**
 * Keeps track of the interrupts that a run control service sends to make the
 * target available for an {@code executeWithTargetAvailable()} operation.
 * <p>
 * The stop that our interrupt causes must not be shown to the user, and the
 * target must be resumed when the operation completes. But the target can
 * also stop by itself (for example at a breakpoint) at the same time. Such a
 * stop must be shown as usual, and the target must stay stopped. See
 * https://github.com/eclipse-cdt/cdt/issues/62
 * <p>
 * The run control service must register this object as a DSF service event
 * listener, so that it sees the {@link MIRunningEvent}s. All methods must be
 * called on the DSF executor thread.
 *
 * @param <T> the type of context that is interrupted: a container in all-stop
 *            mode, a thread in non-stop mode
 */
public class TargetAvailableInterrupts<T> {

	/** In all-stop mode, the whole target stops and runs as one. */
	private static final Object WHOLE_TARGET = new Object();

	private final Function<T, Object> fKey;
	private final Predicate<T> fIsSuspended;
	private final Consumer<T> fStoppedByItself;

	/** Contexts that we interrupted and for which we wait for the stop. */
	private final Set<Object> fPendingStops = new HashSet<>();

	/** Request monitors of {@link #interrupt} calls that are not completed yet. */
	private final Map<Object, RequestMonitor> fWaitingRequests = new HashMap<>();

	/** Stops caused by our interrupts, which we did not show to the user. */
	private final Map<Object, MIStoppedEvent> fSilencedStops = new HashMap<>();

	/**
	 * Contexts for which we saw a stop event and no running event since. The
	 * run control service marks a context as suspended only a little later,
	 * when it handles its own suspended event.
	 */
	private final Set<Object> fStopsSeen = new HashSet<>();

	/**
	 * Creates the tracker for all-stop mode, where any stop or resume applies
	 * to the whole target.
	 *
	 * @param isSuspended     tells if a context is currently suspended
	 * @param stoppedByItself called when the target stops by itself while we
	 *                        make it available. The target must then stay
	 *                        stopped when the operation completes.
	 */
	public static <T> TargetAvailableInterrupts<T> allStop(Predicate<T> isSuspended, Consumer<T> stoppedByItself) {
		return new TargetAvailableInterrupts<>(context -> WHOLE_TARGET, isSuspended, stoppedByItself);
	}

	/**
	 * Creates the tracker for non-stop mode, where each context stops and
	 * runs on its own.
	 *
	 * @param isSuspended     tells if a context is currently suspended
	 * @param stoppedByItself called when a context stops by itself while we
	 *                        make it available. The context must then stay
	 *                        stopped when the operation completes.
	 */
	public static <T> TargetAvailableInterrupts<T> nonStop(Predicate<T> isSuspended, Consumer<T> stoppedByItself) {
		return new TargetAvailableInterrupts<>(context -> context, isSuspended, stoppedByItself);
	}

	private TargetAvailableInterrupts(Function<T, Object> key, Predicate<T> isSuspended, Consumer<T> stoppedByItself) {
		fKey = key;
		fIsSuspended = isSuspended;
		fStoppedByItself = stoppedByItself;
	}

	/**
	 * Interrupts the context with {@code suspend}, unless it has stopped by
	 * itself in the meantime. The stop that the interrupt causes is not shown
	 * to the user, see {@link #handleStop}.
	 *
	 * @param context the context to interrupt
	 * @param suspend sends the interrupt and completes the given request
	 *                monitor when the context is stopped
	 * @param rm      completed when the context is stopped
	 */
	public void interrupt(T context, BiConsumer<T, RequestMonitor> suspend, RequestMonitor rm) {
		Object key = fKey.apply(context);
		assert !fPendingStops.contains(key);
		assert !fWaitingRequests.containsKey(key);

		if (fStopsSeen.contains(key) || fIsSuspended.test(context)) {
			fStoppedByItself.accept(context);
			rm.done();
			return;
		}

		fPendingStops.add(key);
		fWaitingRequests.put(key, rm);
		suspend.accept(context, new ImmediateRequestMonitor() {
			@Override
			protected void handleCompleted() {
				if (fWaitingRequests.get(key) != rm) {
					// handleStop() has already completed rm, because the context
					// stopped by itself. Ignore how the interrupt ended.
					return;
				}
				fWaitingRequests.remove(key);
				if (isSuccess()) {
					rm.done();
					return;
				}
				fPendingStops.remove(key);
				if (fStopsSeen.contains(key) || fIsSuspended.test(context)) {
					// The context stopped by itself while we tried to interrupt it,
					// so it is available after all.
					fStoppedByItself.accept(context);
					rm.done();
					return;
				}
				rm.done(getStatus());
			}
		});
	}

	/**
	 * Call for every stop event of the context, before it is shown to the user.
	 *
	 * @return {@code true} if our interrupt caused the stop, so it must not be
	 *         shown to the user
	 */
	public boolean handleStop(T context, MIStoppedEvent e) {
		Object key = fKey.apply(context);
		fStopsSeen.add(key);
		if (!fPendingStops.remove(key)) {
			return false;
		}
		if (e instanceof MISignalEvent) {
			fSilencedStops.put(key, e);
			return true;
		}
		// The context stopped by itself before our interrupt took effect.
		fStoppedByItself.accept(context);
		// Our interrupt can now have no effect at all, so don't wait for it.
		RequestMonitor rm = fWaitingRequests.remove(key);
		if (rm != null) {
			rm.done();
		}
		return false;
	}

	/**
	 * Forgets all stops seen so far, because some context runs again. This is
	 * safe: without a seen stop, {@link #interrupt} uses the state of the run
	 * control service.
	 */
	@DsfServiceEventHandler
	public void eventDispatched(MIRunningEvent e) {
		fStopsSeen.clear();
	}

	/**
	 * Call when the context was resumed after the operation.
	 */
	public void resumed(T context) {
		fSilencedStops.remove(fKey.apply(context));
	}

	/**
	 * Call when the context could not be resumed after the operation. We then
	 * do not hide its stop anymore.
	 *
	 * @return the stop event that we did not show to the user, or {@code null}
	 *         if it did not come yet. The caller must show it now.
	 */
	public MIStoppedEvent resumeFailed(T context) {
		Object key = fKey.apply(context);
		fPendingStops.remove(key);
		return fSilencedStops.remove(key);
	}
}
