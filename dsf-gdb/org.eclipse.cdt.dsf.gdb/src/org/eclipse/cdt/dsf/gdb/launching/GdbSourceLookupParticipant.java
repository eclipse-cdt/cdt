/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
		 * Update the substitution paths in GDB. Ideally we would always run
		 * this atomically to block the source lookup from attempting to do a
		 * lookup until GDB is fully updated.
		 * 
		 * However, if we are already on the executor thread there is no way to
		 * make this atomic. In practice this case does not matter as the times
		 * sourceContainersChanged is called when on the executor thread are
		 * when the launch configuration is being saved and in that case the
		 * source containers are not even changing.
		 */
		if (fExecutor.isInExecutorThread()) {
			sourceContainersChangedOnDispatchThread(director, new RequestMonitor(fExecutor, null));
		} else {
			Query<Object> query = new Query<Object>() {
				@Override
				protected void execute(final DataRequestMonitor<Object> rm) {
					sourceContainersChangedOnDispatchThread(director, rm);
				}

			};
			fExecutor.execute(query);
			try {
				/*
				 * This is a workaround to prevent a deadlock due to this
				 * blocking call. The proper solution of handling this code
				 * atomically and being non-blocking will be in the next
				 * release. See Bug 494650 for details.
				 * 
				 * Note, even though the timeout is 5 seconds here, the UI
				 * freeze is actually worst case a 2 x 5 seconds x number of
				 * simultaneously shutdown launches. This is because each launch
				 * tries to save the launch configuration 2 times at shutdown
				 * (once for the Register settings and once for the Memory
				 * settings), and since each launch notifies all the other
				 * launches the total UI freeze depends on the number being
				 * terminated simultaneously.
				 */
				query.get(5, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				/*
				 * Log the error, but with at least a bit of help for the user
				 */
				GdbPlugin.log(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
						"There was a timeout completing the operation. " //$NON-NLS-1$
								+ "This is probably due to terminating two launches at the same time. " //$NON-NLS-1$
								+ "Please consider terminating one launch at a time to avoid the UI lockup. " //$NON-NLS-1$
								+ "See Bug 494650 for details.", //$NON-NLS-1$
						e));
			} catch (InterruptedException | ExecutionException e) {
				// We have failed to update in some way, we don't really have a
				// path to expose the failure so at least log it.
				GdbPlugin.log(e);
			}
		}
	}

	@ConfinedToDsfExecutor("fExecutor")
	protected void sourceContainersChangedOnDispatchThread(final ISourceLookupDirector director,
			final RequestMonitor rm) {
		IGDBSourceLookup lookup = fServicesTracker.getService(IGDBSourceLookup.class);
		if (lookup != null) {
			ICommandControlService command = fServicesTracker.getService(ICommandControlService.class);
			ISourceLookupDMContext context = (ISourceLookupDMContext) command.getContext();
			lookup.sourceContainersChanged(context, new DataRequestMonitor<Boolean>(fExecutor, rm) {
				@Override
				protected void handleCompleted() {
					/*
					 * Once GDB is updated, we need to flush the IStack's cache,
					 * so that #getSourceName get the new names.
					 */
					if (isSuccess() && getData()) {
						IStack stackService = fServicesTracker.getService(IStack.class);
						if (stackService instanceof ICachingService) {
							/*
							 * XXX: Ideally we would issue an event here to
							 * flush the cache. But if we did that we would have
							 * to add some interlocking to prevent the call to
							 * IStack.getFrameData() (Called from
							 * super.getSourceNameOnDispatchThread) from
							 * returning until the event had been fully
							 * propogated. At the moment there is no way to
							 * ensure that happens.
							 * 
							 * XXX: Adding an event would allow other interested
							 * parties (such as the Debug View) from being
							 * notified that the frame data has changed. See Bug
							 * 489607.
							 */
							ICachingService cachingStackService = (ICachingService) stackService;
							cachingStackService.flushCache(null);
						}
					}
					super.handleCompleted();
				}
			});
		} else {
			rm.done();
		}
	}
}
