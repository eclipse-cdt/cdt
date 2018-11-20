/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.internal.ui.newui.StatusMessageLine;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.language.settings.providers.ToolchainBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.language.settings.providers.AbstractLanguageSettingProviderOptionPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
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
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class BuiltinSpecsDetectorOptionPage extends AbstractLanguageSettingProviderOptionPage {
	private boolean fEditable;
	private Text inputCommand;
	private Button allocateConsoleCheckBox;

	private StatusMessageLine fStatusLine;

	@Override
	public void createControl(Composite parent) {
		fEditable = parent.isEnabled();
		AbstractBuiltinSpecsDetector provider = (AbstractBuiltinSpecsDetector) getProvider();

		Composite composite = createCompositeForPageArea(parent);
		createCompilerCommandInputControl(composite, provider);
		createBrowseButton(composite);
		createConsoleCheckbox(composite, provider);
		createStatusLine(composite, provider);

		setControl(composite);
	}

	/**
	 * Create composite for the page.
	 */
	private Composite createCompositeForPageArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
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
		return composite;
	}

	/**
	 * Create input control for compiler command.
	 */
	private void createCompilerCommandInputControl(Composite composite, AbstractBuiltinSpecsDetector provider) {
		Label label = ControlFactory.createLabel(composite,
				Messages.BuiltinSpecsDetectorOptionPage_CompilerSpecsCommand);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setEnabled(fEditable);

		inputCommand = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
		String command = provider.getCommand();
		inputCommand.setText(command != null ? command : ""); //$NON-NLS-1$
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

	/**
	 * Create "Browse" button.
	 */
	private void createBrowseButton(Composite composite) {
		Button button = ControlFactory.createPushButton(composite, Messages.BuiltinSpecsDetectorOptionPage_Browse);
		button.setEnabled(fEditable);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(Messages.BuiltinSpecsDetectorOptionPage_ChooseFile);
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

	/**
	 * Create check-box for console.
	 */
	private void createConsoleCheckbox(Composite composite, AbstractBuiltinSpecsDetector provider) {
		allocateConsoleCheckBox = new Button(composite, SWT.CHECK);
		allocateConsoleCheckBox.setText(Messages.BuiltinSpecsDetectorOptionPage_AllocateConsole);
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

	/**
	 * Create status line to display messages for user.
	 */
	private void createStatusLine(Composite composite, AbstractBuiltinSpecsDetector provider) {
		fStatusLine = new StatusMessageLine(composite, SWT.LEFT, 2);

		if (provider instanceof ToolchainBuiltinSpecsDetector) {
			String toolchainId = ((ToolchainBuiltinSpecsDetector) provider).getToolchainId();
			IToolChain toolchain = ManagedBuildManager.getExtensionToolChain(toolchainId);
			if (toolchain == null) {
				fStatusLine.setErrorStatus(new Status(IStatus.ERROR, ManagedBuilderUIPlugin.getUniqueIdentifier(),
						IStatus.ERROR,
						"Toolchain support for CDT is not installed. Toolchain id=[" + toolchainId + "].", null));
			} else if (!toolchain.isSupported()) {
				fStatusLine.setErrorStatus(new Status(IStatus.INFO, ManagedBuilderUIPlugin.getUniqueIdentifier(),
						IStatus.INFO, "Toolchain " + toolchain.getName() + " is not detected on this system.", null));
			}
		}
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		ILanguageSettingsProvider provider = providerTab.getProvider(providerId);
		if ((provider instanceof AbstractBuiltinSpecsDetector)) { // basically check for working copy
			ILanguageSettingsProvider initialProvider = providerTab.getInitialProvider(providerId);
			if (!(initialProvider instanceof AbstractBuiltinSpecsDetector)
					|| !((AbstractBuiltinSpecsDetector) initialProvider).getCommand()
							.equals(((AbstractBuiltinSpecsDetector) provider).getCommand())) {
				// clear and reset isExecuted flag
				((AbstractBuiltinSpecsDetector) provider).clear();
			}
		}

		super.performApply(monitor);
	}

}
