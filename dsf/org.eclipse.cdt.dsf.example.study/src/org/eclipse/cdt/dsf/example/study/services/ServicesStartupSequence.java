package org.eclipse.cdt.dsf.example.study.services;

import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Startup sequence for the timers session (new session). Will create 2 services instance for now
 */
public class ServicesStartupSequence extends Sequence {
	private DsfSession fSession;

	// The reference to the services are saved to use in the last step.
	// The services are in the same session
	private EmployeeService fEmployeeService;
	private ContractorService fContractorService;

	private Step[] fSteps = { new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			// Step 1: add Employee Service
			fEmployeeService = new EmployeeService(fSession);
			fEmployeeService.initialize(requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			// TODO Step 2: add Contractor Service

			requestMonitor.done();
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			// Step 3: start services
			fEmployeeService.createTimer();
			requestMonitor.done();
		}
	}, };

	public ServicesStartupSequence(DsfExecutor executor) {
		super(executor);
	}

	public ServicesStartupSequence(DsfSession session) {
		super(session.getExecutor());
		fSession = session;
	}

	@Override
	public Step[] getSteps() {
		return fSteps;
	}

}
