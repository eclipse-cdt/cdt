package com.ashling.riscfree.globalvariable.actions;

import java.util.ArrayList; //CUSTOMIZATION XKORAT
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.internal.ui.actions.AbstractDebugActionDelegate;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.ashling.riscfree.debug.multicore.model.IMulticoreLaunch;
import com.ashling.riscfree.globalvariable.view.Activator;
import com.ashling.riscfree.globalvariable.view.datamodel.IGlobalVariableDescriptor;
import com.ashling.riscfree.globalvariable.view.dsf.IGlobalVariableService;
import com.ashling.riscfree.globalvariable.view.dsf.Messages;
import com.ashling.riscfree.globalvariable.view.utils.GlobalVariableServiceUtil;

/**
 * A delegate for the "Add Globals" action.
 */
/**
 * @implNote Copied and modified from  AddGlobalsActionDelegate.java in CDT
 * @author vinod.appu
 *
 */
public class AddGlobalsActionDelegate extends ActionDelegate
		implements IViewActionDelegate, ISelectionListener, IPartListener {

	class SortedListSelectionDialog extends ListSelectionDialog {

		public SortedListSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider,
				ILabelProvider labelProvider, String message) {
			super(parentShell, input, contentProvider, labelProvider, message);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Control da = super.createDialogArea(parent);
			getViewer().setSorter(new ViewerSorter());
			return da;
		}
	}

	private IGlobalVariableDescriptor[] fGlobals;

	private IViewPart fView = null;

	private IAction fAction;

	private IStructuredSelection fSelection;

	private IStatus fStatus = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		fView = view;
		view.getSite().getPage().addPartListener(this);
		view.getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart,
	 * ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part != null && part.getSite().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW)) {
			if (selection instanceof IStructuredSelection) {
				setSelection((IStructuredSelection) selection);
			} else {
				setSelection(null);
			}
			update(getAction());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		final IStructuredSelection selection = getSelection();
		if (selection == null || selection.size() != 1)
			return;
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

			@Override
			public void run() {
				try {
					doAction(selection.getFirstElement());
					setStatus(null);
				} catch (DebugException e) {
					setStatus(e.getStatus());
				}
			}
		});
		IStatus status = getStatus();
		if (status != null && !status.isOK()) {
			if (status.isMultiStatus()) {
				status = new MultiStatus(status.getPlugin(), status.getCode(), status.getChildren(),
						Messages.getString("AddGlobalsActionDelegate.Error(s)_occured_adding_globals_1"), //$NON-NLS-1$
						status.getException());
			}
			IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				CDebugUIPlugin.errorDialog(getErrorDialogMessage(), status);
			} else {
				CDebugUIPlugin.log(status);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		setAction(action);
		if (getView() != null) {
			update(action);
		}
	}

	protected void update(IAction action) {
		if (action != null) {
			action.setEnabled(getEnableStateForSelection(getSelection()));
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
	 */
	@Override
	public void partActivated(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IPartListener#partClosed(IWorkbenchPart)
	 */
	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part.equals(getView())) {
			dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IPartListener#partDeactivated(IWorkbenchPart)
	 */
	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IPartListener#partOpened(IWorkbenchPart)
	 */
	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	protected IViewPart getView() {
		return fView;
	}

	protected void setView(IViewPart viewPart) {
		fView = viewPart;
	}

	protected void setAction(IAction action) {
		fAction = action;
	}

	protected IAction getAction() {
		return fAction;
	}

	private void setSelection(IStructuredSelection selection) {
		fSelection = selection;
	}

	private IStructuredSelection getSelection() {
		return fSelection;
	}

	@Override
	public void dispose() {
		if (getView() != null) {
			getView().getViewSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
			getView().getViewSite().getPage().removePartListener(this);
		}
	}

	protected boolean getEnableStateForSelection(IStructuredSelection selection) {
		if (selection == null || selection.size() != 1) {
			return false;
		}
		Object element = selection.getFirstElement();
		if (element instanceof IAdaptable) {
			ILaunch launch = ((IAdaptable) element).getAdapter(ILaunch.class);
			if (launch != null) {
				return !launch.isTerminated();
			}
		}
		return false;
	}

	private SortedListSelectionDialog createDialog() {
		return new SortedListSelectionDialog(getView().getSite().getShell(), fGlobals,
				new IStructuredContentProvider() {

					@Override
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
					}

					@Override
					public void dispose() {
					}

					@Override
					public Object[] getElements(Object parent) {
						return getGlobals();
					}
				}, new LabelProvider() {

					@Override
					public String getText(Object element) {
						if (element instanceof IGlobalVariableDescriptor) {
							String path = ((IGlobalVariableDescriptor) element).getFileName();
							return (path.length() > 0 ? (((IGlobalVariableDescriptor) element).getName() + " ("+ path+")") : ""); //$NON-NLS-1$//$NON-NLS-2$
						}
						return null;
					}
				}, Messages.getString("AddGlobalsActionDelegate.0")); //$NON-NLS-1$
	}

	protected IGlobalVariableDescriptor[] getGlobals() {
		return fGlobals;
	}

	protected void doAction(Object element) throws DebugException {
		if (getView() == null)
			return;
		if (element instanceof IAdaptable) {
			ILaunch launch = ((IAdaptable) element).getAdapter(ILaunch.class);
			if (launch != null && !launch.isTerminated()) {
					DsfSession dsfSession = null;
					if (launch instanceof GdbLaunch) {
						dsfSession = ((GdbLaunch) launch).getSession();
					} else if (launch instanceof IMulticoreLaunch) {
						dsfSession = ((IMulticoreLaunch) launch).getSession();
					}
					if (dsfSession != null) {
						DsfServicesTracker dsfTracker = new DsfServicesTracker(Activator.getContext(),
								dsfSession.getId());
						IGlobalVariableService globalVariableService = GlobalVariableServiceUtil.INSTANCE
								.getGlobalVariablesService(dsfSession,
										((IAdaptable) element).getAdapter(IDMContext.class), launch);
						dsfTracker.dispose();
						globalVariableService.getInitialGlobals(((IAdaptable) element).getAdapter(IDMContext.class), new ImmediateDataRequestMonitor<IGlobalVariableDescriptor[]>() {
							@Override
							protected void handleCompleted() {
								if(isSuccess())
								{
									fGlobals = getData();
									showDialog(element, globalVariableService);
								}
							}
						});


					}

			}
		}
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage() {
		return Messages.getString("AddGlobalsActionDelegate.1"); //$NON-NLS-1$
	}

	protected void setStatus(IStatus status) {
		fStatus = status;
	}

	protected IStatus getStatus() {
		return fStatus;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
		super.init(action);
		Object element = DebugUITools.getDebugContext();
		setSelection((element != null) ? new StructuredSelection(element) : new StructuredSelection());
		update(action);
	}

	private void showDialog(Object element, IGlobalVariableService globalVariableService) {
		ListSelectionDialog dlg = createDialog();
		dlg.setTitle(Messages.getString("AddGlobalsActionDelegate.title")); //$NON-NLS-1$
		try {
			final IGlobalVariableDescriptor[] descriptors = globalVariableService.getGlobals()
					.toArray(new IGlobalVariableDescriptor[0]);
			dlg.setInitialSelections(descriptors);
			// Need to change to UI thread here.
			Display.getDefault().syncExec(() -> {
				dlg.open();
			});
//			if (dlg.open() == Window.OK) {
				List<Object> list = Arrays.asList(dlg.getResult());
				IGlobalVariableDescriptor[] selections = list.toArray(new IGlobalVariableDescriptor[list.size()]);
				// <CUSTOMIZATION>
				List toBeRemoved = new ArrayList();
				for (int i = 0; i < descriptors.length; i++) {
					if (!list.contains(descriptors[i])) {
						toBeRemoved.add(descriptors[i]);
					}
				}
				if (toBeRemoved.size() > 0) {
					globalVariableService.removeGlobals(((IAdaptable) element).getAdapter(IDMContext.class),
							(IGlobalVariableDescriptor[]) toBeRemoved
									.toArray(new IGlobalVariableDescriptor[toBeRemoved.size()]));
				}
				globalVariableService.addGlobals(((IAdaptable) element).getAdapter(IDMContext.class), selections);

//			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
