/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationTabGroupViewer;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings("restriction")
public class NewLaunchConfigEditPage extends WizardPage implements IPageChangingListener {
	private ILaunchConfigurationWorkingCopy workingCopy;
	private LaunchConfigurationDialogExt launchConfigurationDialog = new LaunchConfigurationDialogExt();
	private LaunchConfigurationTabGroupViewerExt tabViewer;

	private ILaunchGroup launchGroup;
	private ILaunchConfigurationType launchConfigType;

	public NewLaunchConfigEditPage() {
		super(Messages.NewLaunchConfigEditPage_0);
		setTitle(Messages.NewLaunchConfigEditPage_1);
		setDescription(Messages.NewLaunchConfigEditPage_2);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		setControl(comp);
		// create tab viewer
		LaunchConfigurationsDialog.setCurrentlyVisibleLaunchConfigurationDialog(launchConfigurationDialog);
		tabViewer = new LaunchConfigurationTabGroupViewerExt(comp, launchConfigurationDialog);
		launchConfigurationDialog.setTabViewer(tabViewer);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		data.heightHint = 500;
		tabViewer.getControl().setLayoutData(data);
		parent.layout(true, true);
		validateFields();
	}

	public void setLaunchGroup(ILaunchGroup launchGroup) {
		this.launchGroup = launchGroup;
	}

	public void setLaunchConfigType(ILaunchConfigurationType type) {
		this.launchConfigType = type;
	}

	/**
	 * @return the workingCopy
	 */
	public ILaunchConfigurationWorkingCopy getWorkingCopy() {
		return workingCopy;
	}

	@Override
	public void handlePageChanging(PageChangingEvent event) {
		if (launchConfigType == null || event.getTargetPage() != this) {
			if (tabViewer != null)
				tabViewer.setInput(null);
			return;
		}
		LaunchConfigurationsDialog.setCurrentlyVisibleLaunchConfigurationDialog(launchConfigurationDialog);
		if (tabViewer != null) {
			try {
				String name = launchConfigurationDialog.generateName("launchConfiguration"); //$NON-NLS-1$
				workingCopy = launchConfigType.newInstance(null, name);
				launchConfigurationDialog.doSetDefaults(workingCopy);
				tabViewer.setInput(workingCopy);
				setTitle(String.format(Messages.NewLaunchConfigEditPage_7, launchConfigType.getName()));
			} catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	public boolean performFinish() {
		if (workingCopy == null)
			return false;
		workingCopy.rename(tabViewer.getWorkingCopy().getName());
		tabViewer.getTabGroup().performApply(workingCopy);
		LaunchConfigurationsDialog.setCurrentlyVisibleLaunchConfigurationDialog(null);
		return true;
	}

	@Override
	public void dispose() {
		LaunchConfigurationsDialog.setCurrentlyVisibleLaunchConfigurationDialog(null);
	}

	public void validateFields() {
		// page is not complete unless we finish validation successfully
		setPageComplete(false);
		if (workingCopy == null)
			return;
		String message = tabViewer.getErrorMesssage();
		setErrorMessage(message);
		if (getErrorMessage() != null) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
	}

	private class LaunchConfigurationDialogExt extends LaunchConfigurationDialog {
		public LaunchConfigurationDialogExt() {
			super(NewLaunchConfigEditPage.this.getShell(), null, null);
		}

		@Override
		protected ILaunchConfiguration getLaunchConfiguration() {
			return workingCopy;
		}

		@Override
		public void launchConfigurationAdded(ILaunchConfiguration configuration) {
			if (getLaunchConfiguration() == null)
				return;
			super.launchConfigurationAdded(configuration);
		}

		@Override
		public LaunchGroupExtension getLaunchGroup() {
			return NewLaunchConfigEditPage.this.getLaunchGroup();
		}

		@Override
		public void updateMessage() {
			validateFields();
		}

		@Override
		public void updateButtons() {
			// Launch button
			getTabViewer().refresh();
			// getButton(ID_LAUNCH_BUTTON).setEnabled(getTabViewer().canLaunch()
			// & getTabViewer().canLaunchWithModes() &
			// !getTabViewer().hasDuplicateDelegates());
		}

		@Override
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
				throws InvocationTargetException, InterruptedException {
			// ignore
		}

		@Override
		public String generateName(String name) {
			if (name == null)
				return ""; //$NON-NLS-1$
			return DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(name);
		}

		@Override
		public void doSetDefaults(ILaunchConfigurationWorkingCopy wc) {
			super.doSetDefaults(wc);
		}

		@Override
		public void setTabViewer(LaunchConfigurationTabGroupViewer viewer) {
			super.setTabViewer(viewer);
		}

		@Override
		public boolean isTreeSelectionEmpty() {
			return false;
		}
	}

	private class LaunchConfigurationTabGroupViewerExt extends LaunchConfigurationTabGroupViewer {
		public LaunchConfigurationTabGroupViewerExt(Composite parent, ILaunchConfigurationDialog dialog) {
			super(parent, dialog);
		}

		@Override
		public ILaunchConfigurationWorkingCopy getWorkingCopy() {
			return super.getWorkingCopy();
		}
	};

	public LaunchGroupExtension getLaunchGroup() {
		if (workingCopy == null)
			return null;
		if (launchGroup == null) {
			return null;
		}
		LaunchGroupExtension groupExt = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
				.getLaunchGroup(launchGroup.getIdentifier());
		return groupExt;
	}

}
