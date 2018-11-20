/*******************************************************************************
 * Copyright (c) 2015, 2016 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupParticipant;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBSourceLookup;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

/**
 * Source Lookup Participant that notifies the {@link IGDBSourceLookup} service
 * of changes to the lookup path to allow the source lookup service to update
 * GDB's substituted paths.
 *
 * @since 5.0
 */
@ThreadSafe
public class GdbSourceLookupParticipant extends DsfSourceLookupParticipant {

	private DsfExecutor fExecutor;
	private String fSessionId;
	private DsfServicesTracker fServicesTracker;

	public GdbSourceLookupParticipant(DsfSession session) {
		super(session);
		fSessionId = session.getId();
		fExecutor = session.getExecutor();
		fServicesTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSessionId);
	}

	@Override
	public void init(ISourceLookupDirector director) {
		super.init(director);
	}

	@Override
	public void dispose() {
		fServicesTracker.dispose();
		super.dispose();
	}

	@Override
	public void sourceContainersChanged(final ISourceLookupDirector director) {
		super.sourceContainersChanged(director);

		/*
		 * The change can be issued directly on the executor thread, or on other
		 * threads, so we need to continue on the correct thread.
		 */
		if (fExecutor.isInExecutorThread()) {
			sourceContainersChangedOnDispatchThread(director, new RequestMonitor(fExecutor, null));
		} else {
			/*
			 * Don't use a Query here, this method must be non-blocking on the
			 * calling thread as there is an interlock possible when two
			 * executors get the same update for the same launch configuration
			 * at the same time. See Bug 494650 for more info.
			 */
			fExecutor.execute(new DsfRunnable() {
				@Override
				public void run() {
					sourceContainersChangedOnDispatchThread(director, new RequestMonitor(fExecutor, null));
				}
			});
		}
	}

	@ConfinedToDsfExecutor("fExecutor")
	protected void sourceContainersChangedOnDispatchThread(final ISourceLookupDirector director,
			final RequestMonitor rm) {
		IGDBSourceLookup lookup = fServicesTracker.getService(IGDBSourceLookup.class);
		if (lookup != null) {
			IStack stackService = fServicesTracker.getService(IStack.class);
			if (stackService instanceof ICachingService) {
				/*
				 * To preserve the atomicity of this method, we need to clear
				 * the cache without waiting for
				 * lookup.sourceContainersChanged() to report if there was
				 * actually a change on the source containers. The cache needs
				 * to be cleared so that any further requests to GDB (e.g.
				 * getting stack frames) sees the new values from GDB after the
				 * source lookup changes. This means we are over clearing the
				 * cache, but this method is only called when the source
				 * containers change which does not happen normally during a
				 * debug session.
				 *
				 * XXX: Adding an event once we finished the update would allow
				 * other interested parties (such as the Debug View) from being
				 * notified that the frame data has changed. See Bug 489607.
				 */
				ICachingService cachingStackService = (ICachingService) stackService;
				cachingStackService.flushCache(null);
			}

			ICommandControlService command = fServicesTracker.getService(ICommandControlService.class);
			ISourceLookupDMContext context = (ISourceLookupDMContext) command.getContext();
			lookup.sourceContainersChanged(context, new DataRequestMonitor<Boolean>(fExecutor, rm));
		} else {
			rm.done();
		}
	}
}
