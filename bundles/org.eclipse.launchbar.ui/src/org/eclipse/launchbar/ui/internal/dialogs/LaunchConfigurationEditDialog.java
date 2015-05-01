/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPropertiesDialog;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.launchbar.ui.internal.Activator;
import org.eclipse.launchbar.ui.internal.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class LaunchConfigurationEditDialog extends LaunchConfigurationPropertiesDialog {

	private static final int DELETE_ID = 64;
	private static final int DUPLICATE_ID = 65;
	private static final int LAUNCH_ID = 66;

	public LaunchConfigurationEditDialog(Shell shell, ILaunchConfiguration launchConfiguration, LaunchGroupExtension group) {
		super(shell, launchConfiguration, group);
	}

	@Override
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		PlatformUI.getWorkbench().getProgressService().run(fork, cancelable, runnable);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		// Clone super's implementation, removes the monitor since we don't run from here
		// And adds in the left button bar.
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(font);

		// create help control if needed
		if (isHelpAvailable()) {
			createHelpControl(composite);
		}

		Composite leftButtonComp = new Composite(composite, SWT.NULL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.numColumns = 0;
		leftButtonComp.setLayout(layout);
		leftButtonComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		leftButtonComp.setFont(parent.getFont());

		createButton(leftButtonComp, DELETE_ID, Messages.LaunchConfigurationEditDialog_0, false);
		createButton(leftButtonComp, DUPLICATE_ID, Messages.LaunchConfigurationEditDialog_1, false);
		createButton(leftButtonComp, LAUNCH_ID, Messages.LaunchConfigurationEditDialog_2, false);

		Composite mainButtonComp = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.numColumns = 0;
		mainButtonComp.setLayout(layout);
		mainButtonComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		mainButtonComp.setFont(parent.getFont());

		createButton(mainButtonComp, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(mainButtonComp, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Do nothing since we now have the buttons created above.
	}

	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		// update the dialog with the new config
		getTabViewer().setInput(configuration);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case DELETE_ID:
			ILaunchConfiguration c = getLaunchConfiguration();
			if (c.isWorkingCopy())
				c = ((ILaunchConfigurationWorkingCopy)c).getOriginal();
			final ILaunchConfiguration config = c;
			if (MessageDialog.openConfirm(getShell(), Messages.LaunchConfigurationEditDialog_3,
					Messages.LaunchConfigurationEditDialog_4 + config.getName())) {
				new Job(Messages.LaunchConfigurationEditDialog_5) {
					protected IStatus run(IProgressMonitor monitor) {
						try {
							config.delete();
							return Status.OK_STATUS;
						} catch (CoreException e) {
							return e.getStatus();
						}
					};
				}.schedule();
				cancelPressed();
			}
			break;
		case DUPLICATE_ID:
			final ILaunchConfiguration original = getLaunchConfiguration();
			final String newName = DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(original.getName());
			new Job(Messages.LaunchConfigurationEditDialog_6) {
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ILaunchConfigurationWorkingCopy newWorkingCopy = original.copy(newName);
						newWorkingCopy.doSave();
						return Status.OK_STATUS;
					} catch (CoreException e) {
						return e.getStatus();
					}
				};
			}.schedule();
			break;
		case LAUNCH_ID:
			okPressed();
			Activator.runCommand(Activator.CMD_LAUNCH);
			break;
		default:
			super.buttonPressed(buttonId);
		}
	}
}
