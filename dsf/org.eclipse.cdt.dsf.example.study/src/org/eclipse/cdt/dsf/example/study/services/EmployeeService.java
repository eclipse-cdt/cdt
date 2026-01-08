package org.eclipse.cdt.dsf.example.study.services;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.example.study.datamodel.EmployeeDMContext;
import org.eclipse.cdt.dsf.example.study.internal.CdtDsfStudyPluginActivator;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.ILog;
import org.osgi.framework.BundleContext;

/**
 * Employee Service tracks a set of employees, which are created per user request.
 */
public class EmployeeService extends AbstractDsfService {
	@Immutable
	public static class EmployeesDMChangedEvent {
	}

	// Counter for generating employee numbers
	private int fTimerDMCount = 0;

	// k: employee - v: counter
	private Map<EmployeeDMContext, Integer> fEmployees = new LinkedHashMap<>();

	public EmployeeService(DsfSession session) {
		super(session);
	}

	@Override
	protected BundleContext getBundleContext() {
		return CdtDsfStudyPluginActivator.getBundleContext();
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

	/** Retrieves the list of employee contexts. */
	public EmployeeDMContext[] getTimerDMcontexts() {
		return fEmployees.keySet().toArray(new EmployeeDMContext[fEmployees.size()]);
	}

	public int getTimerDMContextValue(EmployeeDMContext ctx) {
		return ThreadLocalRandom.current().nextInt(1, 9999 + 1);
	}

	/**
	 * Creates a new employee and returns its context.
	 * It likes start debug session ?
	 * @return
	 */
	public void createTimer() {
		final EmployeeDMContext employeeCtx = createTimerDMContext(getSession());

		// Notify an event to all clients in the current dsf dession
		dispatchEvent(new EmployeesDMChangedEvent());
	}

	private EmployeeDMContext createTimerDMContext(DsfSession session) {
		final EmployeeDMContext ctx = new EmployeeDMContext(session, fTimerDMCount++);
		fEmployees.put(ctx, 0); // init with counter = 0
		return ctx;
	}

	private void dispatchEvent(Object event) {
		if (event instanceof EmployeesDMChangedEvent) {
			ILog.get().info("Dispatchs event: " + EmployeesDMChangedEvent.class.getName());
			getSession().dispatchEvent(event, getProperties());
			return;
		}
		ILog.get().info("Event not supported");
	}

	// After super-class is finished initializing perform TimerService initialization.
	// Q: Why don't we register service in the supper class ? => We don't know the '*Service.class.getName()' at this time
	private void doInitialize(RequestMonitor rm) {
		register(new String[] { EmployeeService.class.getName() }, new Hashtable<>());
		rm.done();
	}
}