package services;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.ILog;
import org.osgi.framework.BundleContext;

import internal.PluginActivator;

/**
 * The Contractor Service tracks employees.
 */
public class ContractorService extends AbstractDsfService {

	public ContractorService(DsfSession session) {
		super(session);
	}

	@Override
	protected BundleContext getBundleContext() {
		return PluginActivator.getBundleContext();
	}

	public void updateDebugContext() {
		ILog.get().info("Service executed - " + getClass().getName());
	}

	@Override
	public void initialize(RequestMonitor requestMonitor) {
		super.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			protected void handleCompleted() {
				doInitialize(requestMonitor);
			}

		});
	}

	private void doInitialize(RequestMonitor rm) {
		register(new String[] { ContractorService.class.getName() }, new Hashtable<>());
		rm.done();
	}
}
