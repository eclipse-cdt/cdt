package org.eclipse.cdt.make.ui.dialogs;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.part.CheckboxTablePart;
import org.eclipse.cdt.make.ui.MakeContentProvider;
import org.eclipse.cdt.make.ui.MakeLabelProvider;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class BuildTargetDialog extends Dialog {

	private IMakeTarget[] selected;
	private CheckboxTableViewer targetListViewer;
	private IContainer fContainer;

	public BuildTargetDialog(Shell shell, IContainer container) {
		super(shell);
		fContainer = container;
	}

	public void setTarget(IMakeTarget[] targets) {
		selected = targets;
	}

	public IMakeTarget getTarget() {
		return null;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Build Targets");
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create Build and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, "Build", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		((GridLayout)composite.getLayout()).numColumns = 2;
		CheckboxTablePart part = new CheckboxTablePart(new String[] { "Select All", "Deselect All", "New" });
		part.createControl(parent, SWT.NULL, 2);
		targetListViewer = part.getTableViewer();
		targetListViewer.setContentProvider(new MakeContentProvider());
		targetListViewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return (element instanceof IMakeTarget);
			}
		});
		targetListViewer.setLabelProvider(new MakeLabelProvider());

		GridData gd = (GridData) part.getControl().getLayoutData();
		gd.heightHint = 100;
		gd.widthHint = 100;

		targetListViewer.setInput(fContainer);
		if (selected != null)
			targetListViewer.setCheckedElements(selected);

		return composite;
	}

}
