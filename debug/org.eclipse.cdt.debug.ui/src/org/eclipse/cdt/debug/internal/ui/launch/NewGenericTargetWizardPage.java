/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.launch;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewGenericTargetWizardPage extends WizardPage {

	private final ILaunchTarget launchTarget;

	private Text nameText;
	private Text osText;
	private Text archText;

	public NewGenericTargetWizardPage(ILaunchTarget launchTarget) {
		super(NewGenericTargetWizardPage.class.getName());
		setTitle(LaunchMessages.getString("NewGenericTargetWizardPage.Title")); //$NON-NLS-1$
		setDescription(LaunchMessages.getString("NewGenericTargetWizardPage.Desc")); //$NON-NLS-1$
		this.launchTarget = launchTarget;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Label label = new Label(comp, SWT.NONE);
		label.setText(LaunchMessages.getString("NewGenericTargetWizardPage.Name")); //$NON-NLS-1$

		nameText = new Text(comp, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (launchTarget != null) {
			nameText.setText(launchTarget.getId());
		}

		label = new Label(comp, SWT.NONE);
		label.setText(LaunchMessages.getString("NewGenericTargetWizardPage.OS")); //$NON-NLS-1$

		osText = new Text(comp, SWT.BORDER);
		osText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (launchTarget != null) {
			String os = launchTarget.getAttribute(ILaunchTarget.ATTR_OS, null);
			if (os != null) {
				osText.setText(os);
			}
		}

		label = new Label(comp, SWT.NONE);
		label.setText(LaunchMessages.getString("NewGenericTargetWizardPage.Arch")); //$NON-NLS-1$

		archText = new Text(comp, SWT.BORDER);
		archText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (launchTarget != null) {
			String arch = launchTarget.getAttribute(ILaunchTarget.ATTR_ARCH, null);
			if (arch != null) {
				archText.setText(arch);
			}
		}

		setControl(comp);
	}

	public String getTargetName() {
		return nameText.getText().trim();
	}

	public String getOS() {
		String os = osText.getText().trim();
		return !os.isEmpty() ? os : null;
	}

	public String getArch() {
		String arch = archText.getText().trim();
		return !arch.isEmpty() ? arch : null;
	}

}
