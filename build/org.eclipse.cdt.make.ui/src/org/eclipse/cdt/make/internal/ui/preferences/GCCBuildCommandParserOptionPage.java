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

package org.eclipse.cdt.make.internal.ui.preferences;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.internal.ui.language.settings.providers.AbstractLanguageSettingProviderOptionPage;
import org.eclipse.cdt.internal.ui.newui.StatusMessageLine;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuildCommandParser;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Options page for TODO
 *
 */
public final class GCCBuildCommandParserOptionPage extends AbstractLanguageSettingProviderOptionPage {
	private boolean fEditable;

	private Text inputCommand;
	
	private StatusMessageLine fStatusLine;
	private Button runOnceRadioButton;
	private Button runEveryBuildRadioButton;
	private Button expandRelativePathCheckBox;
	private Button applyToProjectCheckBox;


	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
//		Composite optionsPageComposite = new Composite(composite, SWT.NULL);
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
			String customParameter = provider.getCustomParameter();
			inputCommand.setText(customParameter!=null ? customParameter : "");
			
			GridData gd = new GridData();
			gd.horizontalSpan = 1;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			inputCommand.setLayoutData(gd);
			inputCommand.setEnabled(fEditable);
			
			inputCommand.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String text = inputCommand.getText();
					AbstractBuildCommandParser provider = getRawProvider();
					if (!text.equals(provider.getCustomParameter())) {
						AbstractBuildCommandParser selectedProvider = getWorkingCopy(providerId);
						selectedProvider.setCustomParameter(text);
						providerTab.refreshItem(selectedProvider);
					}
				}
			});
		}

//		{
//			Button button = ControlFactory.createPushButton(composite, "Browse...");
//			button.setEnabled(fEditable);
//			button.addSelectionListener(new SelectionAdapter() {
//
//				@Override
//				public void widgetSelected(SelectionEvent evt) {
////					handleAddr2LineButtonSelected();
//					//updateLaunchConfigurationDialog();
//				}
//
//			});
//
//		}

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
		
		{
			applyToProjectCheckBox = new Button(composite, SWT.CHECK);
			applyToProjectCheckBox.setText("Apply discovered settings on project level");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			applyToProjectCheckBox.setLayoutData(gd);

//			applyToProjectCheckBox.setSelection(provider.isExpandRelativePaths());
//			applyToProjectCheckBox.setEnabled(fEditable);
			applyToProjectCheckBox.setSelection(false);
			applyToProjectCheckBox.setEnabled(false);
			applyToProjectCheckBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean enabled = applyToProjectCheckBox.getSelection();
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
		
//		// Status line
//		if (fEditable) {
//			fStatusLine = new StatusMessageLine(composite, SWT.LEFT, 2);
//			IStatus status = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, "Note that currently not all options are persisted (FIXME)");
//			fStatusLine.setErrorStatus(status);
//		}
		
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
