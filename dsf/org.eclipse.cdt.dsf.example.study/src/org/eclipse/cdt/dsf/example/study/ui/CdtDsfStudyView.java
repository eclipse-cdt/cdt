package org.eclipse.cdt.dsf.example.study.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.example.study.internal.CdtDsfStudyPluginActivator;
import org.eclipse.cdt.dsf.example.study.services.EmployeeService;
import org.eclipse.cdt.dsf.example.study.services.ServicesShutdownSequence;
import org.eclipse.cdt.dsf.example.study.services.ServicesStartupSequence;
import org.eclipse.cdt.dsf.example.study.viewmodel.ViewVMAdapter;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ILog;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

public class CdtDsfStudyView extends ViewPart {
	public static final String ID_STUDY_VIEW_EMPLOYEES = "cdt.dsf.EmployeesView"; //$NON-NLS-1$
	public static final String ID_STUDY_VIEW_DEFAULT = "cdt.dsf.DefaultView"; //$NON-NLS-1$

	private static final String ACTION_NAME_ADD_NEW_EMPLOYEE = "Add New Employee"; //$NON-NLS-1$

	// DSF Stuffs
	private DsfExecutor fExecutor;
	private DsfSession fSession;

	/**
	 * DSF services tracker canbe used immediately after being constructor Use in to
	 * track all the services within a give DSF session (got the references to the
	 * osgi-services)
	 *
	 * User-Services started inside the DsfSession using a Sequence, so fServices
	 * can access them
	 */
	private DsfServicesTracker fServices;

	// View Model Stuffs
	private ViewVMAdapter fVMAdapter;

	// UI Stuffs
	private PresentationContext fPresentationContext;
	private TreeModelViewer fTreeViewer;

	public CdtDsfStudyView() {
		// Do nothing
	}

	private List<Action> fActions = new ArrayList<>();

	@Override
	public void createPartControl(Composite parent) {
		// Create a flexible hierarchy view
		fPresentationContext = new PresentationContext(ID_STUDY_VIEW_EMPLOYEES);
		fTreeViewer = new TreeModelViewer(parent, SWT.VIRTUAL | SWT.FULL_SELECTION, fPresentationContext);

		// Create the executor for this debug context view
		fExecutor = new DefaultDsfExecutor(); // 1 threads
		fSession = DsfSession.startSession(fExecutor, ID_STUDY_VIEW_EMPLOYEES);
		fServices = new DsfServicesTracker(CdtDsfStudyPluginActivator.getBundleContext(), fSession.getId());

		initializeServices(fSession);

		// Create a view data model adapter
		// All the content updates from the viewers are handled by a single instance of the VM Adapter.
		// then register to the current session
		fVMAdapter = new ViewVMAdapter(fSession);
		registerAdapter();

		// Create a view data model adapter
		// All the content updates from the viewers are handled by a single instance of
		// the VM Adapter.
		// then register to the current session
		// Create a input for the tree view,it methods invokes inputChanged on the
		// content provider and then the inputChanged hook method.
		final IAdaptable inputTreeViewer = new IAdaptable() {

			// If the input object is an RenesasPlatformVMAdapter
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				if (adapter.isInstance(fVMAdapter)) {
					return adapter.cast(fVMAdapter);
				}
				return null;
			}

			@Override
			public String toString() {
				return "Timers View Root"; //$NON-NLS-1$
			}
		};
		fTreeViewer.setInput(inputTreeViewer);

		makeActions();
		contributeToActionBars();
	}

	/**
	 * Start the services using a sequence. The sequence runs in the session
	 * executor thread, therefore the thread calling this method has to block using
	 * Future.get() until the sequence it completes. The Future.get() will throw an
	 * exception if the sequence fails.
	 *
	 * @param fSession
	 */
	private void initializeServices(DsfSession session) {
		ServicesStartupSequence sequence = new ServicesStartupSequence(fSession);
		session.getExecutor().execute(sequence);
		try {
			sequence.get();
		} catch (InterruptedException | ExecutionException e) {
			ILog.get().error(e.getMessage());
		}
	}

	/**
	 * Shutdown the services using a sequence.
	 *
	 * @param fSession
	 */
	private void shutdownServices(DsfSession session) {
		ServicesShutdownSequence shutdownSeq = new ServicesShutdownSequence(fSession);
		fSession.getExecutor().execute(shutdownSeq);
		try {
			shutdownSeq.get();
		} catch (InterruptedException | ExecutionException e) {
			ILog.get().error(e.getMessage());
		}
	}

	/**
	 * This will navigate model these element model to our Adapter -> Provider The
	 * same mechanism org.eclipse.core.runtime.adapters, but runtime instead
	 *
	 * @see org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider
	 */
	private void registerAdapter() {
		fSession.registerModelAdapter(IElementContentProvider.class, fVMAdapter);
		fSession.registerModelAdapter(IModelProxyFactory.class, fVMAdapter);
		fSession.registerModelAdapter(IColumnPresentationFactory.class, fVMAdapter);
	}

	private void unregisterAdapter() {
		// First dispose the view model, which is the client of services.
		// This operation needs to be performed in the session executor
		// thread. Block using Future.get() until this call completes.
		try {
			fSession.getExecutor().submit(() -> {
				fSession.unregisterModelAdapter(IElementContentProvider.class);
				fSession.unregisterModelAdapter(IModelProxyFactory.class);
				fSession.unregisterModelAdapter(IColumnPresentationFactory.class);
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			ILog.get().error(e.getMessage());
		}
	}

	@Override
	public void setFocus() {
		fTreeViewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		// First dispose the view model, which is the client of services.
		// This operation needs to be performed in the session executor
		// thread. Block using Future.get() until this call completes.
		unregisterAdapter();

		// Dispose the VM adapter.
		fVMAdapter.dispose();
		fVMAdapter = null;

		// Next invoke the shutdown sequence for the services. Sequence
		// class also implements Future.get()...
		shutdownServices(fSession);

		// End the DSF session/executor
		try {
			fSession.getExecutor().submit(() -> {
				DsfSession.endSession(fSession);
				fSession = null;
				fExecutor.shutdown();
				fExecutor = null;
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			ILog.get().error(e.getMessage());
		}

		// SWT View dispose
		super.dispose();
	}

	private void contributeToActionBars() {
		IActionBars actionBars = getViewSite().getActionBars();
		Iterator<Action> iterator = fActions.iterator();
		while (iterator.hasNext()) {
			actionBars.getToolBarManager().add(iterator.next());
		}
	}

	private void makeActions() {
		Action addTimersAct = new Action(ACTION_NAME_ADD_NEW_EMPLOYEE) {
			@Override
			public void run() {
				// Must use the service caller via DSF executor
				fExecutor.execute(new DsfRunnable() {
					@Override
					public void run() {
						EmployeeService service = fServices.getService(EmployeeService.class);
						if (service != null) {
							service.createTimer();
						} else {
							ILog.get().error("Service is not exist"); //$NON-NLS-1$
						}
					}
				});
			}
		};
		fActions.add(addTimersAct);

	}

}