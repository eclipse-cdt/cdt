package org.eclipse.cdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.PropertyPage;

public class CProjectPropertyPage extends PropertyPage implements ICOptionContainer {

	private CProjectOptionBlock fOptionBlock;

	private static final String MSG_CLOSEDPROJECT = "CProjectPropertyPage.closedproject";

	public CProjectPropertyPage() {
		super();
		fOptionBlock = new CProjectOptionBlock(this);
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		IProject project = getProject();
		if (!project.isOpen()) {
			contentForClosedProject(composite);
		} else {
			contentForCProject(composite);
		}

		return composite;
	}
	private void contentForCProject(Composite parent) {
		fOptionBlock.createContents(parent);
		//		WorkbenchHelp.setHelp(parent, ICMakeHelpContextIds.PROJECT_PROPERTY_PAGE);	
	}

	private void contentForClosedProject(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(CUIPlugin.getResourceString(MSG_CLOSEDPROJECT));
		label.setFont(parent.getFont());

		noDefaultAndApplyButton();
	}
	/**
		 * @see PreferencePage#performOk
		 */
	public boolean performOk() {
		Shell shell = getControl().getShell();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				fOptionBlock.performApply(monitor);
			}
		};
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
		try {
			new ProgressMonitorDialog(shell).run(false, true, op);
		} catch (InvocationTargetException e) {
			return false;
		} catch (InterruptedException e) {
			// cancelled
			return false;
		}
		return true;
	}

	public IProject getProject() {
		Object element = getElement();
		if (element instanceof IProject) {
			return (IProject) element;
		}
		return null;
	}

	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fOptionBlock.setVisible(visible);
	}

	public void updateContainer() {
		boolean ok = true;
		ok = fOptionBlock.isValid();
		if (!ok) {
			setErrorMessage(fOptionBlock.getErrorMessage());
		}
		if (ok) {
			setErrorMessage(null);
		}
		setValid(ok);
	}

	protected void performDefaults() {
		fOptionBlock.performDefaults();
		super.performDefaults();
	}

	public boolean isValid() {
		updateContainer();
		return super.isValid();
	}
}
