/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.preferences;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.cdt.internal.qt.core.QtInstall;
import org.eclipse.cdt.internal.qt.ui.Messages;
import org.eclipse.cdt.qt.core.IQtInstall;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewQtInstallWizardPage extends WizardPage {

	private Text locationText;
	private Text specText;

	private final Map<Path, IQtInstall> existing;

	public NewQtInstallWizardPage(Map<Path, IQtInstall> existing) {
		super(Messages.NewQtInstallWizardPage_0, Messages.NewQtInstallWizardPage_1, null);
		this.existing = existing;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));

		Label locationLabel = new Label(comp, SWT.NONE);
		locationLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		locationLabel.setText(Messages.NewQtInstallWizardPage_3);

		Composite locationComp = new Composite(comp, SWT.NONE);
		locationComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		locationComp.setLayout(layout);

		locationText = new Text(locationComp, SWT.BORDER);
		locationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		locationText.addModifyListener(e -> validate());

		Button locationButton = new Button(locationComp, SWT.PUSH);
		locationButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		locationButton.setText(Messages.NewQtInstallWizardPage_4);
		locationButton.addListener(SWT.Selection, e -> {
			FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
			dialog.setText(Messages.NewQtInstallWizardPage_5);
			dialog.setFilterExtensions(
					new String[] { Platform.getOS().equals(Platform.OS_WIN32) ? "qmake.exe" : "qmake" }); //$NON-NLS-1$ //$NON-NLS-2$
			String selected = dialog.open();
			if (selected != null) {
				locationText.setText(selected);
				new Job(Messages.NewQtInstallWizardPage_8) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							String spec = QtInstall.getSpec(Paths.get(selected));
							getControl().getDisplay().asyncExec(() -> {
								specText.setText(spec);
							});
							return Status.OK_STATUS;
						} catch (IOException e) {
							return Status.CANCEL_STATUS;
						}
					}
				}.schedule();
			}
		});

		Label specLabel = new Label(comp, SWT.NONE);
		specLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		specLabel.setText(Messages.NewQtInstallWizardPage_9);

		specText = new Text(comp, SWT.READ_ONLY | SWT.BORDER);
		specText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(comp);
		validate();
	}

	private void validate() {
		setPageComplete(false);
		if (existing.containsKey(Paths.get(locationText.getText()))) {
			setErrorMessage(Messages.NewQtInstallWizardPage_11);
			return;
		}

		setPageComplete(true);
		setErrorMessage(null);
	}

	IQtInstall getInstall() {
		return new QtInstall(Paths.get(locationText.getText()));
	}

}
