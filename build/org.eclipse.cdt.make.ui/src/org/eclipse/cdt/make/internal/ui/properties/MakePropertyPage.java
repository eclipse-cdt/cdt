/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.properties;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeProjectOptionBlock;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @deprecated as of CDT 4.0. This property page was used to set properties
 * "C/C++ Make Project" for 3.X style projects.
 * This page lives dormant as of writing (CDT 7.0) but may get activated for
 * {@code org.eclipse.cdt.make.core.makeNature} project (3.X style).
 */
@Deprecated
public class MakePropertyPage extends PropertyPage implements ICOptionContainer {

	MakeProjectOptionBlock fOptionBlock;

	private static final String MSG_CLOSEDPROJECT = "MakeProjectPropertyPage.closedproject"; //$NON-NLS-1$

	public MakePropertyPage() {
		super();
	    fOptionBlock = new MakeProjectOptionBlock();
	}

	@Override
	public void setContainer(IPreferencePageContainer preferencePageContainer) {
	    super.setContainer(preferencePageContainer);
	    fOptionBlock.setOptionContainer(this);
	}

	@Override
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

	@Override
	public boolean performOk() {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				fOptionBlock.performApply(monitor);
			}
		};
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(PlatformUI.getWorkbench().getProgressService() ,runnable, MakeUIPlugin.getWorkspace().getRoot());
		} catch (InvocationTargetException e) {
			Throwable e1 = e.getTargetException();
			MakeUIPlugin.errorDialog(getShell(), MakeUIPlugin.getResourceString("MakeProjectPropertyPage.internalError"),e1.toString(), e1); //$NON-NLS-1$
			return false;
		} catch (InterruptedException e) {
			// cancelled
			return false;
		}
		return true;
	}

	@Override
	public IProject getProject() {
		Object element = getElement();
		if (element instanceof IProject) {
			return (IProject) element;
		}
		return null;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fOptionBlock.setVisible(visible);
	}

	@Override
	public void updateContainer() {
		fOptionBlock.update();
		setValid(fOptionBlock.isValid());
		setErrorMessage(fOptionBlock.getErrorMessage());
	}

	@Override
	protected void performDefaults() {
		fOptionBlock.performDefaults();
		super.performDefaults();
	}

	@Override
	public boolean isValid() {
		updateContainer();
		return super.isValid();
	}

	@Override
	public Preferences getPreferences() {
		return MakeCorePlugin.getDefault().getPluginPreferences();
	}

}
