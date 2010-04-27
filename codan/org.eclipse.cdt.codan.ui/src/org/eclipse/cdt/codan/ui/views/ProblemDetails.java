package org.eclipse.cdt.codan.ui.views;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ProblemDetails extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.eclipse.cdt.codan.ui.views.ProblemDetails";

	private Composite area;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	private Label description;

	private Label location;

	/**
	 * The constructor.
	 */
	public ProblemDetails() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the area and initialize it.
	 */
	public void createPartControl(Composite parent) {
		final String processViewId = "org.eclipse.ui.views.ProblemView";
		area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout());
		description = new Label(area, SWT.WRAP);
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		location = new Label(area, SWT.WRAP);
		

		// Create the help context id for the area's control
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(area, "org.eclipse.cdt.codan.ui.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		ISelectionService ser = (ISelectionService) getSite().getService(ISelectionService.class);
		ser.addSelectionListener(new ISelectionListener() {

			public void selectionChanged(IWorkbenchPart part, ISelection selection) {

				if (part.getSite().getId().equals(processViewId)) {
					processSelection(selection);
				}

			}
		});
		ISelection selection = ser.getSelection(processViewId);
		processSelection(selection);
	}

	protected void processSelection(ISelection selection) {
		if (selection == null || selection.isEmpty()) return;
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection).getFirstElement();
			IMarker marker = null;
			if (firstElement instanceof IAdaptable) {
				marker = (IMarker) ((IAdaptable) firstElement).getAdapter(IMarker.class);
			} else if (firstElement instanceof IMarker) {
				marker = (IMarker) firstElement;
			}
			if (marker != null) {
				String message = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
				description.setText(message);
				String loc = marker.getResource().getFullPath().toOSString(); //$NON-NLS-1$
				int line = marker.getAttribute(IMarker.LINE_NUMBER, 0);
				location.setText(loc+":"+line); //$NON-NLS-1$
				area.layout();
			}

		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ProblemDetails.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(area);
		area.setMenu(menu);
		//getSite().registerContextMenu(menuMgr, area);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
//		manager.add(action1);
//		manager.add(new Separator());
//		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
//		manager.add(action1);
//		manager.add(action2);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {

				showMessage("Double-click detected");
			}
		};
	}

	private void hookDoubleClickAction() {
		// todo
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(area.getShell(), "Problem Details", message);
	}

	/**
	 * Passing the focus request to the area's control.
	 */
	public void setFocus() {
		area.setFocus();
	}
}