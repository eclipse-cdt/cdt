/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Intel Corp - use in Managed Make system 
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedProjectOptionBlock;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.managedbuilder.internal.ui.ErrorParserBlock;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.PropertyPage;

public class ManagedBuilderPropertyPage extends PropertyPage implements ICOptionContainer {

	protected ManagedProjectOptionBlock fOptionBlock;
	protected ITarget displayedTarget;

	private static final String MSG_CLOSEDPROJECT = "MngMakeProjectPropertyPage.closedproject"; //$NON-NLS-1$

	public ManagedBuilderPropertyPage() {
		super();
	}

	public void setContainer(IPreferencePageContainer preferencePageContainer) {
	    super.setContainer(preferencePageContainer);
	    if (fOptionBlock == null) {
	    	fOptionBlock = new ManagedProjectOptionBlock(this);
	    }
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
		label.setText(ManagedBuilderUIMessages.getResourceString(MSG_CLOSEDPROJECT));
		label.setFont(parent.getFont());

		noDefaultAndApplyButton();
	}

	/**
	 * @see PreferencePage#performOk
	 */
	public boolean performOk() {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				fOptionBlock.performApply(monitor);
			}
		};

		//  If the user did not come to this page when the current selected target
		//  was the selected target, then there is nothing to do.  The page was either
		//  never visited, or was visited for another target.
		ITarget target = getSelectedTarget();
		if (target != displayedTarget) return true;
		
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
		try {
			new ProgressMonitorDialog(getShell()).run(false, true, op);
		} catch (InvocationTargetException e) {
			Throwable e1 = e.getTargetException();
			ManagedBuilderUIPlugin.errorDialog(getShell(), ManagedBuilderUIMessages.getResourceString("ManagedProjectPropertyPage.internalError"),e1.toString(), e1); //$NON-NLS-1$
			return false;
		} catch (InterruptedException e) {
			// cancelled
			return false;
		}
		
		// Write out the build model info
		IProject project = getProject();
		ManagedBuildManager.saveBuildInfo(project, false);
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
		if (visible) {
			ErrorParserBlock errorParsers = fOptionBlock.getErrorParserBlock();
			errorParsers.updateValues();
			displayedTarget = getSelectedTarget();
		}
	}

	protected ITarget getSelectedTarget() {
		//  If the selected target is not yet set, set it to the default target
		//  The selected target is needed for saving error parser information
		IProject project = getProject();
		ITarget target = ManagedBuildManager.getSelectedTarget(project);
		if (target == null) {
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			target = info.getDefaultTarget();
			ManagedBuildManager.setSelectedTarget(project, target);
		}
		return target;
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
