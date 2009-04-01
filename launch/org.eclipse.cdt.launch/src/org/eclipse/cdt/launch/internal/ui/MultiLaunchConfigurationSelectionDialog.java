package org.eclipse.cdt.launch.internal.ui;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.launch.internal.MultiLaunchConfigurationDelegate;
import org.eclipse.cdt.launch.ui.ComboControlledStackComposite;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationFilteredTree;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupFilter;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PatternFilter;



/**
 * Dialog to select launch configuration(s)
 */
public class MultiLaunchConfigurationSelectionDialog extends TitleAreaDialog implements ISelectionChangedListener {
	private ViewerFilter[] fFilters = null;
	private ISelection fSelection;
	private ILaunchGroup[] launchGroups;
	private String mode;
	private boolean isDefaultMode;
	private ViewerFilter emptyTypeFilter;
	private IStructuredSelection fInitialSelection;

	public MultiLaunchConfigurationSelectionDialog(Shell shell, String title, String initMode) {
		super(shell);
		LaunchConfigurationManager manager = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		launchGroups = manager.getLaunchGroups();
		mode = initMode;
		fFilters = null;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		emptyTypeFilter = new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof ILaunchConfigurationType) {
					try {
						ILaunchConfigurationType type = (ILaunchConfigurationType) element;
						return getLaunchManager().getLaunchConfigurations(type).length > 0;
					} catch (CoreException e) {
						return false;
					}
				}
				return true;
			}
		};
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected Control createContents(Composite parent) {
		Control x = super.createContents(parent);
		validate();
		setErrorMessage(null);
		return x;
	}

	protected Control createDialogArea(Composite parent2) {
		getShell().setText(LaunchMessages.getString("MultiLaunchConfigurationSelectionDialog.0")); //$NON-NLS-1$
		setTitle(LaunchMessages.getString("MultiLaunchConfigurationSelectionDialog.1")); //$NON-NLS-1$
		//setMessage("Select a Launch Configuration and a Launch Mode");
		Composite comp = (Composite) super.createDialogArea(parent2);
		ComboControlledStackComposite scomp = new ComboControlledStackComposite(comp, SWT.NONE);
		HashMap modes = new HashMap();
		for (int i = 0; i < launchGroups.length; i++) {
			ILaunchGroup g = launchGroups[i];
			if (!modes.containsKey(g.getMode())) {
				modes.put(g.getMode(), g);
			}
		}
		if (this.mode.equals(MultiLaunchConfigurationDelegate.DEFAULT_MODE)) { //$NON-NLS-1$
			try {
				this.mode = "run"; //$NON-NLS-1$
				ILaunchConfiguration sel = getSelectedLaunchConfiguration();
				if (sel != null)
					for (Iterator iterator = modes.keySet().iterator(); iterator.hasNext();) {
						String mode = (String) iterator.next();
						if (sel.supportsMode(mode)) {
							this.mode = mode;
							break;
						}
					}
			} catch (Exception e) {
			}
		} 
		for (Iterator iterator = modes.keySet().iterator(); iterator.hasNext();) {
			String mode = (String) iterator.next();
			ILaunchGroup g = (ILaunchGroup) modes.get(mode);
			LaunchConfigurationFilteredTree fTree = new LaunchConfigurationFilteredTree(scomp.getStackParent(), SWT.MULTI
			        | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), g, fFilters);
			String label = mode;
			scomp.addItem(label, fTree);
			fTree.createViewControl();
			ViewerFilter[] filters = fTree.getViewer().getFilters();
			for (int i = 0; i < filters.length; i++) {
				ViewerFilter viewerFilter = filters[i];
				if (viewerFilter instanceof LaunchGroupFilter) {
					fTree.getViewer().removeFilter(viewerFilter);
				}
			}
			fTree.getViewer().addFilter(emptyTypeFilter);
			fTree.getViewer().addSelectionChangedListener(this);
			if (g.getMode().equals(this.mode)) {
				scomp.setSelection(label);
			}
			if (fInitialSelection!=null) {
				
				fTree.getViewer().setSelection(fInitialSelection, true);
			}
		}
		scomp.setLabelText(LaunchMessages.getString("MultiLaunchConfigurationSelectionDialog.4")); //$NON-NLS-1$
		scomp.pack();
		Rectangle bounds = scomp.getBounds();
		// adjust size
		GridData data = ((GridData) scomp.getLayoutData());
		if (data == null) {
			data = new GridData(GridData.FILL_BOTH);
			scomp.setLayoutData(data);
		}
		data.heightHint = Math.max(convertHeightInCharsToPixels(15), bounds.height);
		data.widthHint = Math.max(convertWidthInCharsToPixels(40), bounds.width);
		scomp.getCombo().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				mode = ((Combo) e.widget).getText();
			}
		});
		// Use default checkbox
		Button checkBox = new Button(comp, SWT.CHECK);
		checkBox.setText(LaunchMessages.getString("MultiLaunchConfigurationSelectionDialog.5")); //$NON-NLS-1$
		checkBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				isDefaultMode = ((Button) e.widget).getSelection();
			}
		});
		return comp;
	}

	public ILaunchConfiguration getSelectedLaunchConfiguration() {
		if (fSelection != null && !fSelection.isEmpty()) {
			Object firstElement = ((IStructuredSelection) fSelection).getFirstElement();
			if (firstElement instanceof ILaunchConfiguration)
				return (ILaunchConfiguration) firstElement;
		}
		return null;
	}

	public String getMode() {
		if (isDefaultMode)
			return MultiLaunchConfigurationDelegate.DEFAULT_MODE; //$NON-NLS-1$
		else
			return mode;
	}

	public static MultiLaunchConfigurationSelectionDialog createDialog(Shell shell, String title, String groupId) {
		return new MultiLaunchConfigurationSelectionDialog(shell, title, groupId);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		fSelection = event.getSelection();
		
		validate();
	}

	protected void validate() {
		Button ok_button = getButton(IDialogConstants.OK_ID);
		if (getSelectedLaunchConfiguration() == null) {
			setErrorMessage(LaunchMessages.getString("MultiLaunchConfigurationSelectionDialog.7")); //$NON-NLS-1$
			if (ok_button!=null) ok_button.setEnabled(false);
		} else {
			setErrorMessage(null);
			if (ok_button!=null) ok_button.setEnabled(true);
		}
	}

	public void setInitialSelection(ILaunchConfiguration data) {
	    fInitialSelection = new StructuredSelection(data);   
	    fSelection = fInitialSelection;
    }
}
