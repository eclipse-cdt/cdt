/*******************************************************************************
 * Copyright (c) 2009, 2019 Andrew Gvozdev and others.
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
 *     Marc-Andre Laperle - Adapted to MSVC (only supports file scope)
 *******************************************************************************/
package org.eclipse.cdt.msw.build.ui;

import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractLanguageSettingsOutputScanner;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Options page for {@link AbstractBuildCommandParser}.
 *
 */
public final class MSVCCommandParserOptionPage extends AbstractLanguageSettingProviderOptionPage {
	private boolean fEditable;

	private Text inputCommand;
	private Button expandRelativePathCheckBox;

	@Override
	public void createControl(Composite parent) {
		fEditable = parent.isEnabled();
		AbstractBuildCommandParser provider = (AbstractBuildCommandParser) getProvider();

		Composite composite = new Composite(parent, SWT.NONE);
		createCompositeForPageArea(composite);
		createCompilerPatternInputControl(provider, composite);
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
		Label label = ControlFactory.createLabel(composite, Messages.MSVCCommandParserOptionPage_CompilerPattern);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);
		label.setEnabled(fEditable);

		inputCommand = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
		String compilerPattern = provider.getCompilerPattern();
		inputCommand.setText(compilerPattern != null ? compilerPattern : ""); //$NON-NLS-1$

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
	private void createResolvePathsCheckbox(Composite composite, AbstractLanguageSettingsOutputScanner provider) {
		expandRelativePathCheckBox = new Button(composite, SWT.CHECK);
		expandRelativePathCheckBox.setText(Messages.MSVCCommandParserOptionPage_ResolvePaths);
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

}
