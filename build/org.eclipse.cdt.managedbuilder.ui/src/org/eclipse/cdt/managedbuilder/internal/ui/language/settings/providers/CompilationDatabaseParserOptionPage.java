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
package org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.ui.newui.StatusMessageLine;
import org.eclipse.cdt.managedbuilder.internal.language.settings.providers.CompilationDatabaseParser;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
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
import org.eclipse.swt.events.SelectionListener;
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
	private Text fCompileCommandsPath;

	@SuppressWarnings("restriction")
	private StatusMessageLine fStatusLine;
	private Combo fBuildOutputParserCombo;

	@Override
	public void createControl(Composite parent) {
		fEditable = parent.isEnabled();
		CompilationDatabaseParser provider = (CompilationDatabaseParser) getProvider();

		Composite composite = createCompositeForPageArea(parent);
		createCompileCommandsPathInputControl(composite, provider);
		createBrowseButton(composite);
		createOutputParserCombo(composite);
		createExclusionOptions(composite);
		createStatusLine(composite, provider);

		setControl(composite);
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
		Label label = ControlFactory.createLabel(composite,
				Messages.CompilationDatabaseParserOptionPage_CompileCommandsPath);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setEnabled(fEditable);

		fCompileCommandsPath = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
		String command = provider.getCompilationDataBasePath().toOSString();
		fCompileCommandsPath.setText(command != null ? command : ""); //$NON-NLS-1$
		fCompileCommandsPath.setEnabled(fEditable);
		fCompileCommandsPath.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String text = fCompileCommandsPath.getText();
				CompilationDatabaseParser provider = (CompilationDatabaseParser) getProvider();
				if (provider.getCompilationDataBasePath() == null
						|| !text.equals(provider.getCompilationDataBasePath().toOSString())) {
					CompilationDatabaseParser selectedProvider = (CompilationDatabaseParser) getProviderWorkingCopy();
					selectedProvider.setCompilationDataBasePath(Path.fromOSString(text));
					refreshItem(selectedProvider);
					validate();
				}
			}
		});
	}

	private void createBrowseButton(Composite composite) {
		Button button = ControlFactory.createPushButton(composite, Messages.CompilationDatabaseParserOptionPage_Browse);
		button.setEnabled(fEditable);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(Messages.CompilationDatabaseParserOptionPage_ChooseFile);
				String fileName = fCompileCommandsPath.getText();
				IPath folder = new Path(fileName).removeLastSegments(1);
				dialog.setFilterPath(folder.toOSString());
				String chosenFile = dialog.open();
				if (chosenFile != null) {
					fCompileCommandsPath.insert(chosenFile);
				}
			}
		});
	}

	private void createOutputParserCombo(Composite composite) {
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

		Label parserLabel = ControlFactory.createLabel(composite,
				Messages.CompilationDatabaseParserOptionPage_BuildParser);
		GridData gd = new GridData(SWT.BEGINNING);
		gd.horizontalSpan = 2;
		parserLabel.setLayoutData(gd);

		fBuildOutputParserCombo = new Combo(composite, SWT.READ_ONLY);
		fBuildOutputParserCombo.setEnabled(fEditable);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fBuildOutputParserCombo.setLayoutData(gd);
		if (buildParsers.isEmpty()) {
			fBuildOutputParserCombo.add(Messages.CompilationDatabaseParserOptionPage_NoBuildOutputParserError);
			fBuildOutputParserCombo.select(0);
			fBuildOutputParserCombo.setEnabled(false);
			// Can't call getProviderWorkingCopy().setBuildParserId() while creating the page since
			// it will try to replace the selected provider in the table which
			// doesn't have a proper selection index until one of them is clicked.
			// Use combo.setData to encode invalid/valid data then set it on the working copy on setVisible(true)/validate.
			fBuildOutputParserCombo.setData(null);
			return;
		}

		for (int i = 0; i < buildParsers.size(); i++) {
			AbstractBuildCommandParser buildParser = buildParsers.get(i);
			fBuildOutputParserCombo.add(buildParser.getName());
			fBuildOutputParserCombo.setData(buildParser.getName(), buildParser);
			if (buildParser.getId().equals(((CompilationDatabaseParser) getProvider()).getBuildParserId())) {
				fBuildOutputParserCombo.select(i);
				fBuildOutputParserCombo.setData(buildParser.getId());
			}
		}

		fBuildOutputParserCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				AbstractBuildCommandParser parser = (AbstractBuildCommandParser) fBuildOutputParserCombo
						.getData(fBuildOutputParserCombo.getText());
				CompilationDatabaseParser selectedProvider = (CompilationDatabaseParser) getProviderWorkingCopy();
				String parserId = ""; //$NON-NLS-1$
				if (parser != null) {
					parserId = parser.getId();
				}
				selectedProvider.setBuildParserId(parserId);
				fBuildOutputParserCombo.setData(parserId);
				validate();
			}
		});

	}

	private void createExclusionOptions(Composite parent) {
		Button keepExclusion = new Button(parent, SWT.CHECK);
		keepExclusion.setText(Messages.CompilationDatabaseParserOptionPage_ExcludeFiles);
		GridData gd = new GridData(SWT.BEGINNING);
		gd.horizontalSpan = 2;
		keepExclusion.setLayoutData(gd);

		keepExclusion.setSelection(((CompilationDatabaseParser) getProvider()).getExcludeFiles());
		keepExclusion.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				CompilationDatabaseParser selectedProvider = (CompilationDatabaseParser) getProviderWorkingCopy();
				selectedProvider.setExcludeFiles(keepExclusion.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	@SuppressWarnings("restriction")
	private void createStatusLine(Composite composite, CompilationDatabaseParser provider) {
		fStatusLine = new StatusMessageLine(composite, SWT.LEFT, 2);
	}

	@SuppressWarnings("restriction")
	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		ILanguageSettingsProvider provider = providerTab.getProvider(providerId);
		if ((provider instanceof CompilationDatabaseParser)) { // basically check for working copy
			CompilationDatabaseParser compilationDatabaseParser = (CompilationDatabaseParser) provider;
			ILanguageSettingsProvider initialProvider = providerTab.getInitialProvider(providerId);
			if (!(initialProvider instanceof CompilationDatabaseParser)
					|| !((CompilationDatabaseParser) initialProvider).getCompilationDataBasePath()
							.equals(compilationDatabaseParser.getCompilationDataBasePath())
					|| !((CompilationDatabaseParser) initialProvider).getBuildParserId()
							.equals(compilationDatabaseParser.getBuildParserId())
					|| ((CompilationDatabaseParser) initialProvider).getExcludeFiles() != compilationDatabaseParser
							.getExcludeFiles()) {
				compilationDatabaseParser.clear();
			}
			if (compilationDatabaseParser.isEmpty()) {
				compilationDatabaseParser.processCompileCommandsFile(monitor, getConfigurationDescription());
			}
		}

		super.performApply(monitor);
	}

	@SuppressWarnings("restriction")
	private void validate() {
		if (fBuildOutputParserCombo.getData() == null) {
			((CompilationDatabaseParser) getProviderWorkingCopy()).setBuildParserId(null);
		}

		CompilationDatabaseParser provider = (CompilationDatabaseParser) getProvider();
		if (provider.getCompilationDataBasePath() == null || provider.getCompilationDataBasePath().isEmpty()
				|| !Files.exists(Paths.get(provider.getCompilationDataBasePath().toOSString()))) {
			fStatusLine.setErrorStatus(new Status(IStatus.ERROR, ManagedBuilderUIPlugin.getUniqueIdentifier(),
					Messages.CompilationDatabaseParserOptionPage_CompileCommandsPathError));
			return;
		}

		if (provider.getBuildParserId() == null || provider.getBuildParserId().isEmpty()) {
			fStatusLine.setErrorStatus(new Status(IStatus.ERROR, ManagedBuilderUIPlugin.getUniqueIdentifier(),
					Messages.CompilationDatabaseParserOptionPage_BuildOutputParserError));
			return;
		}

		fStatusLine.setErrorStatus(Status.OK_STATUS);
	}

	private ICConfigurationDescription getConfigurationDescription() {
		if (providerTab.page.isForPrefs()) {
			return null;
		}

		return providerTab.getResDesc().getConfiguration().getConfiguration();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			validate();
		}
	}
}
