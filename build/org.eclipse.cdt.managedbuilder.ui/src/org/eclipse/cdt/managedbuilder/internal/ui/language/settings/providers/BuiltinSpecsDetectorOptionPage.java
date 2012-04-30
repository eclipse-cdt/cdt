/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers;

import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.ui.language.settings.providers.AbstractLanguageSettingProviderOptionPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Options page for {@link AbstractBuiltinSpecsDetector}.
 */
public final class BuiltinSpecsDetectorOptionPage extends AbstractLanguageSettingProviderOptionPage {
	private boolean fEditable;

	private Text inputCommand;

	private Button allocateConsoleCheckBox;

	@Override
	public void createControl(Composite parent) {
		fEditable = parent.isEnabled();

		Composite composite = new Composite(parent, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = 1;
			layout.marginHeight = 1;
			layout.marginRight = 1;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			Dialog.applyDialogFont(composite);

			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			composite.setLayoutData(gd);
		}

		AbstractBuiltinSpecsDetector provider = (AbstractBuiltinSpecsDetector) getProvider();

		// Compiler specs command
		{
			Label label = ControlFactory.createLabel(composite, "Command to get compiler specs:");
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			label.setLayoutData(gd);
			label.setEnabled(fEditable);
		}

		{
			inputCommand = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
			String command = provider.getCommand();
			inputCommand.setText(command!=null ? command : "");
			inputCommand.setEnabled(fEditable);
			inputCommand.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					String text = inputCommand.getText();
					AbstractBuiltinSpecsDetector provider = (AbstractBuiltinSpecsDetector) getProvider();
					if (!text.equals(provider.getCommand())) {
						AbstractBuiltinSpecsDetector selectedProvider = (AbstractBuiltinSpecsDetector) getProviderWorkingCopy();
						selectedProvider.setCommand(text);
						refreshItem(selectedProvider);
					}
				}
			});
		}

		{
			Button button = ControlFactory.createPushButton(composite, "Browse...");
			button.setEnabled(fEditable);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
					dialog.setText(/*PreferencesMessages.BuildLogPreferencePage_ChooseLogFile*/"Choose file");
					String fileName = inputCommand.getText();
					// taking chance that the first word is a compiler path
					int space = fileName.indexOf(' ');
					if (space > 0) {
						fileName = fileName.substring(0, space);
					}
					IPath folder = new Path(fileName).removeLastSegments(1);
					dialog.setFilterPath(folder.toOSString());
					String chosenFile = dialog.open();
					if (chosenFile != null) {
						inputCommand.insert(chosenFile);
					}
				}
			});
		}

		{
			allocateConsoleCheckBox = new Button(composite, SWT.CHECK);
			allocateConsoleCheckBox.setText("Allocate console in the Console View");
			allocateConsoleCheckBox.setSelection(provider.isConsoleEnabled());
			allocateConsoleCheckBox.setEnabled(fEditable);
			allocateConsoleCheckBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean enabled = allocateConsoleCheckBox.getSelection();
					AbstractBuiltinSpecsDetector provider = (AbstractBuiltinSpecsDetector) getProvider();
					if (enabled != provider.isConsoleEnabled()) {
						AbstractBuiltinSpecsDetector selectedProvider = (AbstractBuiltinSpecsDetector) getProviderWorkingCopy();
						selectedProvider.setConsoleEnabled(enabled);
						refreshItem(selectedProvider);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

			});

		}

		setControl(composite);
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		// handled by LanguageSettingsProviderTab
	}

	@Override
	public void performDefaults() {
		// handled by LanguageSettingsProviderTab
	}

}
