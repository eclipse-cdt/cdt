package org.eclipse.cdt.dsf.gdb.launching;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBSourceLookup;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;

/**
 * Source Lookup Participant that notifies the {@link IGDBSourceLookup} service
 * of changes to the lookup path to allow the source lookup service to update
 * GDB's substituted paths.
 * 
 * @since 5.0
 */
@ThreadSafe
public class GdbSourceLookupParticipant implements ISourceLookupParticipant {
	protected static final Object[] EMPTY = new Object[0];

	private DsfExecutor fExecutor;
	private String fSessionId;
	private DsfServicesTracker fServicesTracker;
	private ISourceLookupDirector fDirector;

	public GdbSourceLookupParticipant(DsfSession session) {
		fSessionId = session.getId();
		fExecutor = session.getExecutor();
		fServicesTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSessionId);
	}

	@Override
	public void init(ISourceLookupDirector director) {
		fDirector = director;
	}

	@Override
	public Object[] findSourceElements(Object object) throws CoreException {
		return EMPTY;
	}

	@Override
	public String getSourceName(Object object) throws CoreException {
		return null;
	}

	@Override
	public void dispose() {
		fServicesTracker.dispose();
		fDirector = null;
	}

	@Override
	public void sourceContainersChanged(final ISourceLookupDirector director) {
		fExecutor.execute(new DsfRunnable() {

			@Override
			public void run() {
				IGDBSourceLookup lookup = fServicesTracker.getService(IGDBSourceLookup.class);
				if (lookup != null) {
					lookup.sourceContainersChanged(director, new RequestMonitor(fExecutor, null));
				}
			}
		});

	}

}
