/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.downloads;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.arduino.core.internal.ArduinoPreferences;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class ArduinoDownloadsManager extends WizardDialog {

	public ArduinoDownloadsManager() {
		super(null, new Wizard() {
			{
				setNeedsProgressMonitor(true);
			}

			@Override
			public void addPages() {
				addPage(new WizardPage(ArduinoDownloadsManager.class.getName()) {
					{
						setTitle("Arduino Download Manager");
						setDescription("Manage installed Platforms and Libraries.");
					}

					@Override
					public void createControl(Composite parent) {
						final CTabFolder tabFolder = new CTabFolder(parent, SWT.BORDER);
						tabFolder.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								tabFolder.getSelection().getControl().setFocus();
							}
						});
						tabFolder.addFocusListener(new FocusAdapter() {
							@Override
							public void focusGained(FocusEvent e) {
								tabFolder.getSelection().getControl().setFocus();
							}
						});

						CTabItem platformsTab = new CTabItem(tabFolder, SWT.NONE);
						platformsTab.setText("Platforms");
						platformsTab.setImage(Activator.getDefault().getImageRegistry().get(Activator.IMG_ARDUINO));
						PlatformsTabControl platformsControl = new PlatformsTabControl(tabFolder, SWT.NONE);
						platformsControl.setContainer(getContainer());
						platformsTab.setControl(platformsControl);

						CTabItem librariesTab = new CTabItem(tabFolder, SWT.NONE);
						librariesTab.setText("Libraries");
						librariesTab.setImage(Activator.getDefault().getImageRegistry().get(Activator.IMG_ARDUINO));
						LibrariesTabControl librariesControl = new LibrariesTabControl(tabFolder, SWT.NONE);
						librariesControl.setContainer(getContainer());
						librariesTab.setControl(librariesControl);

						applyDialogFont(tabFolder);
						setControl(tabFolder);
					}
				});
			}

			@Override
			public boolean performFinish() {
				return true;
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.CANCEL_ID).setVisible(false);
		Button finishButton = getButton(IDialogConstants.FINISH_ID);
		finishButton.setText("Done");
		// make sure it's far right
		finishButton.moveBelow(null);
	}

	static boolean checkLicense(Shell shell) {
		File acceptedFile = ArduinoPreferences.getArduinoHome().resolve(".accepted").toFile(); //$NON-NLS-1$
		if (!acceptedFile.exists()) {
			String message = "Do you accept the licenses for the platforms and libraries you are downloading?";
			MessageDialog dialog = new MessageDialog(shell, "Arduino Licensing", null, message, MessageDialog.QUESTION,
					new String[] { "Yes", "No" }, 0);
			int rc = dialog.open();
			if (rc == 0) {
				try {
					acceptedFile.createNewFile();
				} catch (IOException e) {
					Activator.log(e);
				}
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

}
