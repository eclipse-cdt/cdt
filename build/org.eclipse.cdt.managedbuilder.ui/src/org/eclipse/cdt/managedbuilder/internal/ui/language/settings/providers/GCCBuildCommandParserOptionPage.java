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

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.internal.ui.language.settings.providers.AbstractLanguageSettingProviderOptionPage;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Options page for TODO
 *
 */
public final class GCCBuildCommandParserOptionPage extends AbstractLanguageSettingProviderOptionPage {
	private boolean fEditable;

	private Text inputCommand;

	private Button expandRelativePathCheckBox;

	private Button scopeProjectRadioButton;
	private Button scopeFolderRadioButton;
	private Button scopeFileRadioButton;


	@Override
	public void createControl(Composite parent) {
		fEditable = parent.isEnabled();

		final Composite composite = new Composite(parent, SWT.NONE);
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

		AbstractBuildCommandParser provider = getRawProvider();

		// Compiler specs command
		{
			Label label = ControlFactory.createLabel(composite, "Compiler command pattern:");
			GridData gd = new GridData();
			gd.horizontalSpan = 1;
			label.setLayoutData(gd);
			label.setEnabled(fEditable);
		}

		{
			inputCommand = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
			String compilerPattern = provider.getCompilerPattern();
			inputCommand.setText(compilerPattern!=null ? compilerPattern : "");

			GridData gd = new GridData();
			gd.horizontalSpan = 1;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			inputCommand.setLayoutData(gd);
			inputCommand.setEnabled(fEditable);

			inputCommand.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					String text = inputCommand.getText();
					AbstractBuildCommandParser provider = getRawProvider();
					if (!text.equals(provider.getCompilerPattern())) {
						AbstractBuildCommandParser selectedProvider = getWorkingCopy(providerId);
						selectedProvider.setCompilerPattern(text);
						providerTab.refreshItem(selectedProvider);
					}
				}
			});
		}

		{
			expandRelativePathCheckBox = new Button(composite, SWT.CHECK);
			expandRelativePathCheckBox.setText("Use heuristics to resolve paths");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			expandRelativePathCheckBox.setLayoutData(gd);

			expandRelativePathCheckBox.setSelection(provider.isResolvingPaths());
			expandRelativePathCheckBox.setEnabled(fEditable);
			expandRelativePathCheckBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean enabled = expandRelativePathCheckBox.getSelection();
					AbstractBuildCommandParser provider = getRawProvider();
					if (enabled != provider.isResolvingPaths()) {
						AbstractBuildCommandParser selectedProvider = getWorkingCopy(providerId);
						selectedProvider.setResolvingPaths(enabled);
						providerTab.refreshItem(selectedProvider);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

			});

		}

		Group resourceScopeGroup = new Group(composite, SWT.NONE);
		{
//			resourceScopeGroup.setText("Define scope of discovered entries");
//			resourceScopeGroup.setText("Apply discovered entries to");
			resourceScopeGroup.setText("Scope to keep discovered entries");
			resourceScopeGroup.setLayout(new GridLayout(2, false));
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			resourceScopeGroup.setLayoutData(gd);
		}

		{
			scopeFileRadioButton = new Button(resourceScopeGroup, SWT.RADIO);
			scopeFileRadioButton.setText("Per file, use when settings vary for different files");
//			applyToResourceRadioButton.setText("File level, use when settings vary for different files");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			scopeFileRadioButton.setLayoutData(gd);

			scopeFileRadioButton.setSelection(provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.FILE);
			scopeFileRadioButton.setEnabled(fEditable);
			scopeFileRadioButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean enabled = scopeFileRadioButton.getSelection();
					AbstractBuildCommandParser provider = getRawProvider();
					if (enabled != (provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.FILE)) {
						AbstractBuildCommandParser selectedProvider = getWorkingCopy(providerId);
						selectedProvider.setResourceScope(AbstractBuildCommandParser.ResourceScope.FILE);
						providerTab.refreshItem(selectedProvider);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

			});

		}

		{
			scopeFolderRadioButton = new Button(resourceScopeGroup, SWT.RADIO);
			scopeFolderRadioButton.setText("Per folder, use when settings are the same for all files in each folder");
//			applyToEnclosingFolderRadioButton.setText("Enclosing folder, use when settings are the same for all files in a folder");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			scopeFolderRadioButton.setLayoutData(gd);

			scopeFolderRadioButton.setSelection(provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.FOLDER);
			scopeFolderRadioButton.setEnabled(fEditable);
			scopeFolderRadioButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean enabled = scopeFolderRadioButton.getSelection();
					AbstractBuildCommandParser provider = getRawProvider();
					if (enabled != (provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.FOLDER)) {
						AbstractBuildCommandParser selectedProvider = getWorkingCopy(providerId);
						selectedProvider.setResourceScope(AbstractBuildCommandParser.ResourceScope.FOLDER);
						providerTab.refreshItem(selectedProvider);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

			});

		}

		{
			scopeProjectRadioButton = new Button(resourceScopeGroup, SWT.RADIO);
			scopeProjectRadioButton.setText("Per project, use when settings are the same for all files in the project");
//			applyToProjectRadioButton.setText("Project level, use when settings are the same for all files in the project");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			scopeProjectRadioButton.setLayoutData(gd);

			scopeProjectRadioButton.setSelection(provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.PROJECT);
			scopeProjectRadioButton.setEnabled(fEditable);
			scopeProjectRadioButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean enabled = scopeProjectRadioButton.getSelection();
					AbstractBuildCommandParser provider = getRawProvider();
					if (enabled != (provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.PROJECT)) {
						AbstractBuildCommandParser selectedProvider = getWorkingCopy(providerId);
						selectedProvider.setResourceScope(AbstractBuildCommandParser.ResourceScope.PROJECT);
						providerTab.refreshItem(selectedProvider);
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

	private AbstractBuildCommandParser getRawProvider() {
		ILanguageSettingsProvider provider = LanguageSettingsManager.getRawProvider(providerTab.getProvider(providerId));
		Assert.isTrue(provider instanceof AbstractBuildCommandParser);
		return (AbstractBuildCommandParser) provider;
	}

	private AbstractBuildCommandParser getWorkingCopy(String providerId) {
		ILanguageSettingsProvider provider = providerTab.getWorkingCopy(providerId);
		Assert.isTrue(provider instanceof AbstractBuildCommandParser);
		return (AbstractBuildCommandParser) provider;
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
