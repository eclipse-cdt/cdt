package org.eclipse.cdt.dsf.example.study.viewmodel;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.example.study.services.EmployeeService.EmployeesDMChangedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.core.runtime.ILog;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

public class EmployeesVMProvider extends AbstractDMVMProvider {
	private boolean fServiceEventListener = false;

	/** Event indicating that the employees view layout has changed */
	public static class RenesasVMChangedEvent {
	}

	public EmployeesVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext,
			DsfSession session) {
		super(adapter, presentationContext, session);
		registerServiceEventListener();
		createTestNode();
	}

	@Override
	public void dispose() {
		unregisterServiceEventListener();
		super.dispose();
	}

	// Add a handle for the EmployeeChangedEvent
	@DsfServiceEventHandler
	public void eventDispatched(final EmployeesDMChangedEvent event) {
		if (isDisposed())
			return;
		ILog.get().info("Received EmployeesChangedEvent - Thread: " + Thread.currentThread().getName());
		// We should access the event via DSF Executor
		getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				ILog.get().info("Handle event - Thread: " + Thread.currentThread().getName());
				handleEvent(event); // Q:What does it do ?
				// A: Send the event to VMNodes to build delta, see @getDeltaFlags
			}
		});
	}

	// UI
	// Override the interface used to create column presentations.
	@Override
	public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
		return new EmployeesViewColumnPresentation();
	}

	@Override
	public String getColumnPresentationId(IPresentationContext context, Object element) {
		return EmployeesViewColumnPresentation.ID;
	}

	private void createTestNode() {
		clearNodes();
		IRootVMNode root = new RenesasRootVMNode(this);
		IVMNode EmployeesNode = new EmployeesDMVMNode(this, getSession());
		addChildNodes(root, new IVMNode[] { EmployeesNode });
		setRootNode(root);
		handleEvent(new RenesasVMChangedEvent());
	}

	private void registerServiceEventListener() {
		// We are in the main thread
		// Switch to dsf thread via Executor
		// Note: use DsfRunnable or () -> {}
		getSession().getExecutor().execute(new DsfRunnable() {
			@Override
			public void run() {
				// IMPORTANT: must use Class.this to explicit the handler method
				getSession().addServiceEventListener(EmployeesVMProvider.this, null);
				fServiceEventListener = true;
			}
		});
	}

	private void unregisterServiceEventListener() {
		// We are in the main thread
		// Switch to dsf thread via Executor
		// Remove ourselves as listener for DM events events. In practice, we
		// get called after the session has shut down, so we'll end up with a
		// RejectedExecutionException. We put this here all the same for
		// completeness sake.
		if (isDisposed())
			return;
		try {
			getSession().getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					if (fServiceEventListener && DsfSession.isSessionActive(getSession().getId())) {
						// IMPORTANT: must use Class.this
						getSession().removeServiceEventListener(EmployeesVMProvider.this);
						fServiceEventListener = false;
					}
				}
			});
		} catch (RejectedExecutionException e) {
			// Session shut down, not much we can do but wait to be disposed.
			// IMPORTANT Session disposed, ignore.
			ILog.get().warn(e.getMessage());
		}
	}
}
