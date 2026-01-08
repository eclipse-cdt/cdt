package services;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import internal.PluginActivator;

/**
 * Sequence that stops the services in the employees session.
 */
public class ServicesShutdownSequence extends Sequence {
	private DsfSession fSession;
	private DsfServicesTracker fServices;

	public ServicesShutdownSequence(DsfExecutor executor) {
		super(executor);
	}

	public ServicesShutdownSequence(DsfSession session) {
		super(session.getExecutor());
		fSession = session;
	}

	Step[] fSteps = { new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			fServices = new DsfServicesTracker(PluginActivator.getBundleContext(), fSession.getId());
			requestMonitor.done();
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(EmployeeService.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			fServices.dispose();
			fServices = null;
			requestMonitor.done();
		}
	}, };

	@Override
	public Step[] getSteps() {
		return fSteps;
	}

	// A convenience method that shuts down given service.  Only service class
	// is used to identify the service.
	private <V extends IDsfService> void shutdownService(Class<V> clazz, RequestMonitor requestMonitor) {
		IDsfService service = fServices.getService(clazz);
		if (service != null) {
			service.shutdown(requestMonitor);
		} else {
			requestMonitor.setStatus(new Status(IStatus.ERROR, PluginActivator.PLUGIN_ID,
					IDsfStatusConstants.INTERNAL_ERROR, "Service '" + clazz.getName() + "' not found.", null));
			requestMonitor.done();
		}
	}

}
