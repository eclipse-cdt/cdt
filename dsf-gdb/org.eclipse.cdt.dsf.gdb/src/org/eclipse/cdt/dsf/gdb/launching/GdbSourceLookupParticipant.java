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
				 * actually a change on the source containers. This means we are
				 * over clearing the cache, but this method is only called when
				 * the source containers change which does not happen normally
				 * during a debug session.
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
