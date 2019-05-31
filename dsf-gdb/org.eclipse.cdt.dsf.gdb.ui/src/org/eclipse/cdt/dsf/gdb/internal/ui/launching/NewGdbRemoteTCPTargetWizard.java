/*******************************************************************************
 * Copyright (c) 2019 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GDBRemoteTCPLaunchTargetProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.eclipse.launchbar.ui.target.LaunchTargetWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewGdbRemoteTCPTargetWizard extends LaunchTargetWizard {

	private Button sameAsHostname;
	private Text nameText;
	private Text hostText;
	private Text portText;

	private class TCPPage extends WizardPage {
		public TCPPage() {
			super(NewGdbRemoteTCPTargetWizard.class.getName());
			setTitle(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.Title")); //$NON-NLS-1$
			setDescription(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.Desc")); //$NON-NLS-1$
		}

		@Override
		public void createControl(Composite parent) {
			Composite control = new Composite(parent, SWT.NONE);
			control.setLayout(new GridLayout());

			// Target name

			Group nameGroup = new Group(control, SWT.NONE);
			nameGroup.setText(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.NameGroup")); //$NON-NLS-1$
			nameGroup.setLayout(new GridLayout(2, false));
			nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			sameAsHostname = new Button(nameGroup, SWT.CHECK);
			sameAsHostname.setText(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.SameAsHost")); //$NON-NLS-1$
			GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			sameAsHostname.setLayoutData(gridData);
			sameAsHostname.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean same = sameAsHostname.getSelection();
					if (same) {
						nameText.setText(hostText.getText());
					}
					nameText.setEnabled(!same);
				}
			});
			sameAsHostname.setSelection(true);

			Label nameLabel = new Label(nameGroup, SWT.NONE);
			nameLabel.setText(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.TargetName")); //$NON-NLS-1$

			nameText = new Text(nameGroup, SWT.BORDER);
			nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			nameText.setEnabled(false);
			nameText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validatePage();
				}
			});

			// host and port

			Group connGroup = new Group(control, SWT.NONE);
			connGroup.setText(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.ConnectionGroup")); //$NON-NLS-1$
			connGroup.setLayout(new GridLayout(4, false));
			connGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label hostLabel = new Label(connGroup, SWT.NONE);
			hostLabel.setText(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.HostName")); //$NON-NLS-1$

			hostText = new Text(connGroup, SWT.BORDER);
			hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			hostText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (sameAsHostname.getSelection()) {
						nameText.setText(hostText.getText());
					}
					validatePage();
				}
			});

			Label portLabel = new Label(connGroup, SWT.NONE);
			portLabel.setText(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.Port")); //$NON-NLS-1$

			portText = new Text(connGroup, SWT.BORDER);
			portText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validatePage();
				}
			});

			setControl(control);
			validatePage();
		}

		private void validatePage() {
			setPageComplete(false);

			if (hostText.getText().isEmpty()) {
				setErrorMessage(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.NoHost")); //$NON-NLS-1$
				return;
			}

			String port = portText.getText();
			if (port.isEmpty()) {
				setErrorMessage(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.NoPort")); //$NON-NLS-1$
				return;
			}

			try {
				Integer.parseInt(port);
			} catch (NumberFormatException e) {
				setErrorMessage(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.PortNotANumber")); //$NON-NLS-1$
				return;
			}

			if (nameText.getText().isEmpty()) {
				setErrorMessage(LaunchUIMessages.getString("NewGdbRemoteTCPTargetWizard.NoName")); //$NON-NLS-1$
				return;
			}

			setErrorMessage(null);
			setPageComplete(true);
		}
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new TCPPage());
	}

	@Override
	public boolean performFinish() {
		ILaunchTargetManager manager = GdbUIPlugin.getService(ILaunchTargetManager.class);
		String id = nameText.getText();

		ILaunchTarget target = getLaunchTarget();
		if (target == null) {
			target = manager.addLaunchTarget(GDBRemoteTCPLaunchTargetProvider.TYPE_ID, id);
		}

		ILaunchTargetWorkingCopy wc = target.getWorkingCopy();
		wc.setId(id);
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, hostText.getText());
		wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, portText.getText());
		wc.save();

		return true;
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public void performDelete() {
		ILaunchTargetManager manager = GdbUIPlugin.getService(ILaunchTargetManager.class);
		ILaunchTarget target = getLaunchTarget();
		if (target != null) {
			manager.removeLaunchTarget(target);
		}
	}

}
