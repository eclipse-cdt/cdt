package org.eclipse.cdt.make.ui.dialogs;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.part.ListViewerPart;
import org.eclipse.cdt.make.ui.MakeContentProvider;
import org.eclipse.cdt.make.ui.MakeLabelProvider;
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class BuildTargetDialog extends Dialog {

	private IMakeTarget fSelected;
	private StructuredViewer listViewer;
	private IContainer fContainer;

	public BuildTargetDialog(Shell shell, IContainer container) {
		super(shell);
		fContainer = container;
	}

	public void setTarget(IMakeTarget targets) {
		fSelected = targets;
	}

	public IMakeTarget getTarget() {
		return fSelected;
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
		((GridLayout) composite.getLayout()).numColumns = 2;
		Label title = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		title.setLayoutData(gd);
		title.setText("Make Targets for: " + fContainer.getFullPath().toString().substring(1));
		ListViewerPart part = new ListViewerPart(new String[] { "Add Target...", "Remove Target", "Edit Target..." });
		part.createControl(composite, SWT.NULL, 2);
		listViewer = part.getViewer();
		listViewer.setContentProvider(new MakeContentProvider(true));
		listViewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return (element instanceof IMakeTarget);
			}
		});
		listViewer.setLabelProvider(new MakeLabelProvider(fContainer.getProjectRelativePath()));
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		gd = (GridData) part.getControl().getLayoutData();
		gd.heightHint = convertHeightInCharsToPixels(15);
		gd.widthHint = convertWidthInCharsToPixels(50);
		part.getControl().setLayoutData(gd);

		listViewer.setInput(fContainer);
		if (fSelected != null)
			listViewer.setSelection(new StructuredSelection(fSelected), true);

		return composite;
	}

	protected void okPressed() {
		fSelected = (IMakeTarget) ((IStructuredSelection) listViewer.getSelection()).getFirstElement();
		if (fSelected != null) {
			TargetBuild.runWithProgressDialog(getShell(), new IMakeTarget[] { fSelected });
		}
		super.okPressed();
	}

}
