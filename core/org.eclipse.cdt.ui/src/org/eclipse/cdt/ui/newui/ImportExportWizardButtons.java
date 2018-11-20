/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.ProjectSettingsExportWizard;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.ProjectSettingsImportWizard;
import org.eclipse.cdt.internal.ui.wizards.settingswizards.ProjectSettingsWizard;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class that adds buttons for "Import Settings..." and "Export Settings..."
 * to the bottom of the Includes and Symbols tabs.
 *
 * @since 5.2
 */
public class ImportExportWizardButtons {

	private ImportExportWizardButtons() {
	}

	public static void addWizardLaunchButtons(final Composite parent, final IAdaptable selection) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		layout.marginHeight = layout.marginWidth = 0;
		comp.setLayout(layout);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		comp.setLayoutData(data);

		Button importButton = new Button(comp, SWT.NONE);
		importButton.setText(Messages.IncludeTab_import);
		importButton.setImage(CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_IMPORT_SETTINGS));
		importButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean finishedPressed = launchWizard(parent.getShell(), selection, false);
				// There is no way to get the contents of the property page to update
				// other than to close the whole dialog and then reopen it.
				if (finishedPressed)
					parent.getShell().close();
			}
		});

		Button exportButton = new Button(comp, SWT.NONE);
		exportButton.setText(Messages.IncludeTab_export);
		exportButton.setImage(CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_EXPORT_SETTINGS));
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				launchWizard(parent.getShell(), selection, true);
			}
		});

	}

	private static boolean launchWizard(Shell shell, IAdaptable selection, boolean export) {
		ProjectSettingsWizard wizard;
		if (export)
			wizard = new ProjectSettingsExportWizard();
		else
			wizard = new ProjectSettingsImportWizard();

		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(selection));
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.open();

		return wizard.isFinishedPressed();
	}
}
