/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.ui.internal;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.cdt.build.gcc.core.GCCToolChain;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.jface.wizard.WizardPage;
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

public class GCCToolChainSettingsPage extends WizardPage {

	private final GCCToolChain toolChain;
	private Text compilerText;
	private Text osText;
	private Text archText;

	public GCCToolChainSettingsPage(GCCToolChain toolChain, boolean isClang) {
		super(GCCToolChainSettingsPage.class.getName());
		this.toolChain = toolChain;
		setTitle(isClang ? Messages.GCCToolChainSettingsPage_ClangTitle : Messages.GCCToolChainSettingsPage_Title);
		setDescription(Messages.GCCToolChainSettingsPage_Description);
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.GCCToolChainSettingsPage_Compiler);

		Composite compilerComp = new Composite(comp, SWT.NONE);
		compilerComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		compilerComp.setLayout(layout);

		compilerText = new Text(compilerComp, SWT.BORDER);
		compilerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		compilerText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateComplete();
			}
		});
		compilerText.addModifyListener(e -> updateComplete());
		if (toolChain != null) {
			compilerText.setText(toolChain.getPath().toString());
		}

		Button compilerBrowse = new Button(compilerComp, SWT.PUSH);
		compilerBrowse.setText(Messages.GCCToolChainSettingsPage_Browse);
		compilerBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(getShell());
				String newText = fileDialog.open();
				if (newText != null) {
					compilerText.setText(newText);
				}
			}
		});

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.GCCToolChainSettingsPage_OS);

		osText = new Text(comp, SWT.BORDER);
		osText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (toolChain != null) {
			String os = toolChain.getProperty(IToolChain.ATTR_OS);
			if (os != null) {
				osText.setText(os);
			}
		}

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.GCCToolChainSettingsPage_Arch);

		archText = new Text(comp, SWT.BORDER);
		archText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (toolChain != null) {
			String arch = toolChain.getProperty(IToolChain.ATTR_ARCH);
			if (arch != null) {
				archText.setText(arch);
			}
		}

		updateComplete();

		setControl(comp);
	}

	private void updateComplete() {
		setPageComplete(!compilerText.getText().trim().isEmpty());
	}

	public Path getPath() {
		return Paths.get(compilerText.getText().trim());
	}

	public String getOS() {
		return osText.getText().trim();
	}

	public String getArch() {
		return archText.getText().trim();
	}

}
