package org.eclipse.cdt.dsf.gdb.launching;

import java.util.concurrent.ExecutionException;

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
				query.get();
			} catch (InterruptedException | ExecutionException e) {
				// We have failed to update in some way, we don't really have a
				// path
				// to expose the failure so at least log it.
				GdbPlugin.log(e);
			}
		}
	}

	/**
	 * Update the substitution paths in GDB, we run this as a query to block the
	 * source lookup from attempting to do a lookup until GDB is fully updated.
	 * 
	 * Once GDB is updated, we need to flush the IStack's cache, so that
	 * #getSourceName get the new names.
	 */
	@ConfinedToDsfExecutor("fExecutor")
	protected void sourceContainersChangedOnDispatchThread(final ISourceLookupDirector director,
			final RequestMonitor rm) {
		IGDBSourceLookup lookup = fServicesTracker.getService(IGDBSourceLookup.class);
		if (lookup != null) {
			ICommandControlService command = fServicesTracker.getService(ICommandControlService.class);
			ISourceLookupDMContext context = (ISourceLookupDMContext)command.getContext();
			lookup.sourceContainersChanged(context, new DataRequestMonitor<Boolean>(fExecutor, rm) {
				@Override
				protected void handleCompleted() {
					if (isSuccess() && getData()) {
						IStack stackService = fServicesTracker.getService(IStack.class);
						if (stackService instanceof ICachingService) {
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
