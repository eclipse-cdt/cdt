/*******************************************************************************
 * Copyright (c) 2009, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers;

import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
import org.eclipse.cdt.ui.language.settings.providers.AbstractLanguageSettingProviderOptionPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
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
 * Options page for {@link AbstractBuildCommandParser}.
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
		AbstractBuildCommandParser provider = (AbstractBuildCommandParser) getProvider();

		Composite composite = new Composite(parent, SWT.NONE);
		createCompositeForPageArea(composite);
		createCompilerPatternInputControl(provider, composite);
		createResourceScopeGroup(provider, composite);
		createResolvePathsCheckbox(composite, provider);

		setControl(composite);
	}

	/**
	 * Create composite for the page.
	 */
	private void createCompositeForPageArea(final Composite composite) {
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
	}

	/**
	 * Create input control for compiler pattern.
	 */
	private void createCompilerPatternInputControl(AbstractBuildCommandParser provider, Composite composite) {
		Label label = ControlFactory.createLabel(composite, Messages.GCCBuildCommandParserOptionPage_CompilerPattern);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		label.setEnabled(fEditable);

		inputCommand = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
		String compilerPattern = provider.getCompilerPattern();
		inputCommand.setText(compilerPattern!=null ? compilerPattern : ""); //$NON-NLS-1$

		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		inputCommand.setLayoutData(gd);
		inputCommand.setEnabled(fEditable);

		inputCommand.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				String text = inputCommand.getText();
				AbstractBuildCommandParser provider = (AbstractBuildCommandParser) getProvider();
				if (!text.equals(provider.getCompilerPattern())) {
					AbstractBuildCommandParser selectedProvider = (AbstractBuildCommandParser) getProviderWorkingCopy();
					selectedProvider.setCompilerPattern(text);
					refreshItem(selectedProvider);
				}
			}
		});
	}

	/**
	 * Create check-box for resolving paths.
	 */
	private void createResolvePathsCheckbox(Composite composite, AbstractBuildCommandParser provider) {
		expandRelativePathCheckBox = new Button(composite, SWT.CHECK);
		expandRelativePathCheckBox.setText(Messages.GCCBuildCommandParserOptionPage_ResolvePaths);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		expandRelativePathCheckBox.setLayoutData(gd);

		expandRelativePathCheckBox.setSelection(provider.isResolvingPaths());
		expandRelativePathCheckBox.setEnabled(fEditable);
		expandRelativePathCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = expandRelativePathCheckBox.getSelection();
				AbstractBuildCommandParser provider = (AbstractBuildCommandParser) getProvider();
				if (enabled != provider.isResolvingPaths()) {
					AbstractBuildCommandParser selectedProvider = (AbstractBuildCommandParser) getProviderWorkingCopy();
					selectedProvider.setResolvingPaths(enabled);
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
	 * Create group and radio buttons for container to keep discovered entries.
	 */
	private void createResourceScopeGroup(AbstractBuildCommandParser provider, Composite composite) {
		Group resourceScopeGroup = new Group(composite, SWT.NONE);
		resourceScopeGroup.setText(Messages.GCCBuildCommandParserOptionPage_ContainerForDiscoveredEntries);
		resourceScopeGroup.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		resourceScopeGroup.setLayoutData(gd);

		scopeFileRadioButton = new Button(resourceScopeGroup, SWT.RADIO);
		scopeFileRadioButton.setText(Messages.GCCBuildCommandParserOptionPage_File);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		scopeFileRadioButton.setLayoutData(gd);

		scopeFileRadioButton.setSelection(provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.FILE);
		scopeFileRadioButton.setEnabled(fEditable);
		scopeFileRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = scopeFileRadioButton.getSelection();
				AbstractBuildCommandParser provider = (AbstractBuildCommandParser) getProvider();
				if (enabled != (provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.FILE)) {
					AbstractBuildCommandParser selectedProvider = (AbstractBuildCommandParser) getProviderWorkingCopy();
					selectedProvider.setResourceScope(AbstractBuildCommandParser.ResourceScope.FILE);
					refreshItem(selectedProvider);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		scopeFolderRadioButton = new Button(resourceScopeGroup, SWT.RADIO);
		scopeFolderRadioButton.setText(Messages.GCCBuildCommandParserOptionPage_Folder);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		scopeFolderRadioButton.setLayoutData(gd);

		scopeFolderRadioButton.setSelection(provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.FOLDER);
		scopeFolderRadioButton.setEnabled(fEditable);
		scopeFolderRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = scopeFolderRadioButton.getSelection();
				AbstractBuildCommandParser provider = (AbstractBuildCommandParser) getProvider();
				if (enabled != (provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.FOLDER)) {
					AbstractBuildCommandParser selectedProvider = (AbstractBuildCommandParser) getProviderWorkingCopy();
					selectedProvider.setResourceScope(AbstractBuildCommandParser.ResourceScope.FOLDER);
					refreshItem(selectedProvider);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		scopeProjectRadioButton = new Button(resourceScopeGroup, SWT.RADIO);
		scopeProjectRadioButton.setText(Messages.GCCBuildCommandParserOptionPage_Project);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		scopeProjectRadioButton.setLayoutData(gd);

		scopeProjectRadioButton.setSelection(provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.PROJECT);
		scopeProjectRadioButton.setEnabled(fEditable);
		scopeProjectRadioButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = scopeProjectRadioButton.getSelection();
				AbstractBuildCommandParser provider = (AbstractBuildCommandParser) getProvider();
				if (enabled != (provider.getResourceScope() == AbstractBuildCommandParser.ResourceScope.PROJECT)) {
					AbstractBuildCommandParser selectedProvider = (AbstractBuildCommandParser) getProviderWorkingCopy();
					selectedProvider.setResourceScope(AbstractBuildCommandParser.ResourceScope.PROJECT);
					refreshItem(selectedProvider);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	}

}
