/*******************************************************************************
 * Copyright (c) 2019 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.compilationdatabase.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.compilationdatabase.internal.core.CompilationDatabaseParser;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.ui.newui.StatusMessageLine;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Options page for {@link CompilationDatabaseParser}.
 *
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class CompilationDatabaseParserOptionPage extends AbstractLanguageSettingProviderOptionPage {
	private boolean fEditable;
	private Text compileCommandsPath;

	private StatusMessageLine fStatusLine;

	@Override
	public void createControl(Composite parent) {
		fEditable = parent.isEnabled();
		CompilationDatabaseParser provider = (CompilationDatabaseParser) getProvider();

		Composite composite = createCompositeForPageArea(parent);
		createCompileCommandsPathInputControl(composite, provider);
		createBrowseButton(composite);
		createOutputParserCombo(composite);
		createStatusLine(composite, provider);

		setControl(composite);
	}

	private void createOutputParserCombo(Composite composite) {
		// TODO Auto-generated method stub
		ICConfigurationDescription configurationDescription = getConfigurationDescription();
		List<AbstractBuildCommandParser> buildParsers = new ArrayList<>();
		if (configurationDescription instanceof ILanguageSettingsProvidersKeeper) {
			List<ILanguageSettingsProvider> settingProviders = ((ILanguageSettingsProvidersKeeper) configurationDescription)
					.getLanguageSettingProviders();
			for (ILanguageSettingsProvider languageSettingsProvider : settingProviders) {
				if (languageSettingsProvider instanceof AbstractBuildCommandParser) {
					AbstractBuildCommandParser buildParser = (AbstractBuildCommandParser) languageSettingsProvider;
					buildParsers.add(buildParser);
				}
			}
		}

		Label parserLabel = ControlFactory.createLabel(composite, "Build parser:");
		GridData gd = new GridData(SWT.BEGINNING);
		gd.horizontalSpan = 2;
		parserLabel.setLayoutData(gd);

		Combo combo = new Combo(composite, NONE);
		if (buildParsers.isEmpty()) {
			combo.setText("No build output parser enabled");
			combo.setEnabled(false);
			return;
		}
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		combo.setLayoutData(gd);

		for (AbstractBuildCommandParser buildParser : buildParsers) {
			combo.add(buildParser.getName());
			combo.setData(buildParser.getName(), buildParser);
		}

		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				AbstractBuildCommandParser parser = (AbstractBuildCommandParser) e.data;
				CompilationDatabaseParser selectedProvider = (CompilationDatabaseParser) getProviderWorkingCopy();
				String parserId = ""; //$NON-NLS-1$
				if (parser != null) {
					parserId = parser.getId();
				}
				selectedProvider.setBuildParserId(parserId);
			}
		});

	}

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

	private void createCompileCommandsPathInputControl(Composite composite, CompilationDatabaseParser provider) {
		Label label = ControlFactory.createLabel(composite, "Compile Commands Path");
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setEnabled(fEditable);

		compileCommandsPath = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
		String command = provider.getCompilationDataBasePath().toOSString();
		compileCommandsPath.setText(command != null ? command : ""); //$NON-NLS-1$
		compileCommandsPath.setEnabled(fEditable);
		compileCommandsPath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String text = compileCommandsPath.getText();
				CompilationDatabaseParser provider = (CompilationDatabaseParser) getProvider();
				if (provider.getCompilationDataBasePath() == null
						|| !text.equals(provider.getCompilationDataBasePath().toOSString())) {
					CompilationDatabaseParser selectedProvider = (CompilationDatabaseParser) getProviderWorkingCopy();
					selectedProvider.setCompilationDataBasePath(Path.fromOSString(text));
					refreshItem(selectedProvider);
				}
			}
		});
	}

	/**
	 * Create "Browse" button.
	 */
	private void createBrowseButton(Composite composite) {
		Button button = ControlFactory.createPushButton(composite, "Browse...");
		button.setEnabled(fEditable);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText("Choose File");
				String fileName = compileCommandsPath.getText();
				// taking chance that the first word is a compiler path
				int space = fileName.indexOf(' ');
				if (space > 0) {
					fileName = fileName.substring(0, space);
				}
				IPath folder = new Path(fileName).removeLastSegments(1);
				dialog.setFilterPath(folder.toOSString());
				String chosenFile = dialog.open();
				if (chosenFile != null) {
					compileCommandsPath.insert(chosenFile);
				}
			}
		});
	}

	/**
	 * Create status line to display messages for user.
	 */
	private void createStatusLine(Composite composite, CompilationDatabaseParser provider) {
		fStatusLine = new StatusMessageLine(composite, SWT.LEFT, 2);

		//			if (provider instanceof ToolchainBuiltinSpecsDetector) {
		//				String toolchainId = ((ToolchainBuiltinSpecsDetector) provider).getToolchainId();
		//				IToolChain toolchain = ManagedBuildManager.getExtensionToolChain(toolchainId);
		//				if (toolchain == null) {
		//					fStatusLine.setErrorStatus(new Status(IStatus.ERROR, ManagedBuilderUIPlugin.getUniqueIdentifier(),
		//							IStatus.ERROR,
		//							"Toolchain support for CDT is not installed. Toolchain id=[" + toolchainId + "].", null));
		//				} else if (!toolchain.isSupported()) {
		//					fStatusLine.setErrorStatus(new Status(IStatus.INFO, ManagedBuilderUIPlugin.getUniqueIdentifier(),
		//							IStatus.INFO, "Toolchain " + toolchain.getName() + " is not detected on this system.", null));
		//				}
		//			}
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		ILanguageSettingsProvider provider = providerTab.getProvider(providerId);
		if ((provider instanceof CompilationDatabaseParser)) { // basically check for working copy
			ILanguageSettingsProvider initialProvider = providerTab.getInitialProvider(providerId);
			if (!(initialProvider instanceof CompilationDatabaseParser)
					|| !((CompilationDatabaseParser) initialProvider).getCompilationDataBasePath()
							.equals(((CompilationDatabaseParser) provider).getCompilationDataBasePath())) {
				// clear and reset isExecuted flag
				((CompilationDatabaseParser) provider).clear();
			}
		}

		super.performApply(monitor);
	}

	/**
	 * Shortcut for getting the current configuration description.
	 */
	private ICConfigurationDescription getConfigurationDescription() {
		if (providerTab.page.isForPrefs()) {
			return null;
		}

		return providerTab.getResDesc().getConfiguration().getConfiguration();
	}
}
