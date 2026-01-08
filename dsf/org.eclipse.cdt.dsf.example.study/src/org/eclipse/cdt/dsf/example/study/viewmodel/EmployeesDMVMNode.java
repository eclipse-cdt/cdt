package org.eclipse.cdt.dsf.example.study.viewmodel;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.example.study.datamodel.EmployeeDMContext;
import org.eclipse.cdt.dsf.example.study.internal.CdtDsfStudyPluginActivator;
import org.eclipse.cdt.dsf.example.study.services.EmployeeService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelForeground;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelImage;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.swt.graphics.RGB;

/**
 * View model node that defines how Employee DMContexts are displayed in the view.
 */
public class EmployeesDMVMNode extends AbstractDMVMNode implements IElementLabelProvider, IElementPropertiesProvider {
	private static final String PROP_EMPLOYEE_NUMBER = "number";
	private static final String PROP_EMPLOYEE_VALUE = "value";

	// Create and configure the label provider.
	private static final PropertiesBasedLabelProvider fgLabelProvider;
	static {
		fgLabelProvider = new PropertiesBasedLabelProvider();

		LabelColumnInfo idCol = new LabelColumnInfo(
				new LabelAttribute[] { new LabelText("Employee #{0}", new String[] { PROP_EMPLOYEE_NUMBER }),
						new LabelForeground(new RGB(0, 0, 255)), new LabelImage(CdtDsfStudyPluginActivator.getDefault()
								.getImageRegistry().getDescriptor(CdtDsfStudyPluginActivator.IMG_EMPLOYEE)) });
		fgLabelProvider.setColumnInfo(EmployeesViewColumnPresentation.COL_ID, idCol);

		LabelColumnInfo valueCol = new LabelColumnInfo(
				new LabelAttribute[] { new LabelText("{0}", new String[] { PROP_EMPLOYEE_VALUE }) });
		fgLabelProvider.setColumnInfo(EmployeesViewColumnPresentation.COL_VALUE, valueCol);

	}

	public EmployeesDMVMNode(AbstractDMVMProvider provider, DsfSession session,
			Class<? extends IDMContext> dmcClassType) {
		super(provider, session, dmcClassType);
	}

	public EmployeesDMVMNode(EmployeesVMProvider provider, DsfSession session) {
		super(provider, session, EmployeeDMContext.class);
	}

	@Override
	public int getDeltaFlags(Object event) {
		// This node generates delta if the employees have changed, or if the
		// label has changed.
		if (event instanceof EmployeeService.EmployeesDMChangedEvent) {
			return IModelDelta.CONTENT; // elements content has change
		}
		return IModelDelta.NO_CHANGE; // elements content no change
	}

	@Override
	public void buildDelta(Object event, VMDelta parent, int nodeOffset, RequestMonitor requestMonitor) {
		if (event instanceof EmployeeService.EmployeesDMChangedEvent) {
			// The list of employees has changed, which means that the parent
			// node needs to refresh its contents, which in turn will re-fetch the
			// elements from this node.
			parent.setFlags(parent.getFlags() | IModelDelta.CONTENT);
		}
		requestMonitor.done();
	}

	@Override
	protected void updateElementsInSessionThread(IChildrenUpdate update) {
		EmployeeService service = getServicesTracker().getService(EmployeeService.class);
		if (service == null) {
			handleFailedUpdate(update);
		}

		// Retrieve the DMContexts from service, and create corresponding VMCs array and set them as result
		EmployeeDMContext[] employees = service.getTimerDMcontexts();

		fillUpdateWithVMCs(update, employees);

		update.done();
	}

	@Override
	public void update(ILabelUpdate[] updates) {
		fgLabelProvider.update(updates);
	}

	@Override
	public void update(final IPropertiesUpdate[] updates) {
		// Switch to the session thread before processing the updates.
		try {
			getSession().getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					for (IPropertiesUpdate update : updates) {
						updatePropertiesInSessionThread(update);
					}
				}
			});
		} catch (RejectedExecutionException e) {
			for (IViewerUpdate update : updates) {
				handleFailedUpdate(update);
			}
		}
	}

	@ConfinedToDsfExecutor("getSession#getExecutor")
	private void updatePropertiesInSessionThread(final IPropertiesUpdate update) {
		// Find the timer context in the element being updated
		EmployeeDMContext dmc = getDMContext(update);
		EmployeeService employeeservice = getServicesTracker().getService(EmployeeService.class, null);

		// If either update or service are not valid, fail the update and exit.
		if (dmc == null || employeeservice == null) {
			handleFailedUpdate(update);
			return;
		}

		int value = employeeservice.getTimerDMContextValue(dmc);

		if (value == -1) {
			handleFailedUpdate(update);
			return;
		}

		update.setProperty(PROP_EMPLOYEE_NUMBER, dmc.getEmployeeNumberId());
		update.setProperty(PROP_EMPLOYEE_VALUE, value);
		update.done();
	}

	// Retrieves data from data model as a DMContext object
	private EmployeeDMContext getDMContext(IPropertiesUpdate update) {
		EmployeeDMContext ctx = findDmcInPath(update, update.getElementPath(), EmployeeDMContext.class);
		return ctx;
	}
}