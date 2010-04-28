package org.eclipse.cdt.codan.internal.ui.views;

import java.net.URL;
import java.util.Collection;

import org.eclipse.cdt.codan.ui.AbstractCodanProblemDetailsProvider;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
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
	public static final String ID = "org.eclipse.cdt.codan.internal.ui.views.ProblemDetails";
	private Composite area;
	private Action action1;

	private Label description;
	private Label location;
	private Link helpLabel;

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
		helpLabel = new Link(area, SWT.WRAP);
		helpLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		helpLabel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String link = e.text;
				if (link != null && link.startsWith("http")) {
					org.eclipse.swt.program.Program.launch(e.text);
				}
			}
		});
		// Create the help context id for the area's control
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(area, "org.eclipse.cdt.codan.ui.viewer");
		makeActions();
		hookContextMenu();
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
		if (selection == null || selection.isEmpty())
			return;
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
				location.setText(loc + ":" + line); //$NON-NLS-1$
				queryProviders(marker);
				area.layout();
			}
		}
	}

	private void queryProviders(IMarker marker) {
		cleanProversControl();
		String id = marker.getAttribute(IMarker.PROBLEM, "id"); //$NON-NLS-1$
		Collection<AbstractCodanProblemDetailsProvider> providers = ProblemDetailsExtensions.getProviders(id);
		for (AbstractCodanProblemDetailsProvider provider : providers) {
			synchronized (provider) {
				provider.setMarker(marker);
				if (provider.isApplicable(id)) {
					applyProvider(provider);
					break;
				}
			}
		}
	}

	public void cleanProversControl() {
		helpLabel.setText("");
	}

	private void applyProvider(AbstractCodanProblemDetailsProvider provider) {
		String label = provider.getHelpLabel();
		final URL url = provider.getHelpURL();
		if (label != null) {
			helpLabel.setText(label);
		}
		if (url != null) {
			if (label == null) {
				label = url.toString();
			}
			helpLabel.setText("<a href=\"" + url + "\">" + label + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

	}

	private void fillContextMenu(IMenuManager manager) {		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {

	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

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