/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.properties;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.make.internal.ui.MakeProjectOptionBlock;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.PropertyPage;

public class MakePropertyPage extends PropertyPage implements ICOptionContainer {

	MakeProjectOptionBlock fOptionBlock;

	private static final String MSG_CLOSEDPROJECT = "MakeProjectPropertyPage.closedproject"; //$NON-NLS-1$

	public MakePropertyPage() {
		super();
		fOptionBlock = new MakeProjectOptionBlock(this);
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
		label.setText(MakeUIPlugin.getResourceString(MSG_CLOSEDPROJECT));
		label.setFont(parent.getFont());

		noDefaultAndApplyButton();
	}

	/**
	 * @see PreferencePage#performOk
	 */
	public boolean performOk() {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				fOptionBlock.performApply(monitor);
			}
		};
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
		try {
			new ProgressMonitorDialog(getShell()).run(false, true, op);
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
		fOptionBlock.update();
		setValid(fOptionBlock.isValid());
		setErrorMessage(fOptionBlock.getErrorMessage());
	}

	protected void performDefaults() {
		fOptionBlock.performDefaults();
		super.performDefaults();
	}

	public boolean isValid() {
		updateContainer();
		return super.isValid();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getPreferences()
	 */
	public Preferences getPreferences() {
		return null;
	}

}
