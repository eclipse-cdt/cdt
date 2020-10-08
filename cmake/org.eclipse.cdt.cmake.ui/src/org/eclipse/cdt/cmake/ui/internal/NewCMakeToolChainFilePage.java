/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.internal;

import java.nio.file.Paths;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.cmake.core.internal.CMakeBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.cdt.core.build.IToolChainManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewCMakeToolChainFilePage extends WizardPage {

	private Text pathText;
	private Combo tcCombo;

	private IToolChain[] toolchains;

	public NewCMakeToolChainFilePage() {
		super("NewCMakeToolChainFilePage", Messages.NewCMakeToolChainFilePage_Title, null); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));

		Label pathLabel = new Label(comp, SWT.NONE);
		pathLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		pathLabel.setText(Messages.NewCMakeToolChainFilePage_Path);

		Composite pathComp = new Composite(comp, SWT.NONE);
		pathComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		pathComp.setLayout(layout);

		pathText = new Text(pathComp, SWT.BORDER);
		pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pathText.addModifyListener(e -> validate());

		Button pathButton = new Button(pathComp, SWT.PUSH);
		pathButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		pathButton.setText(Messages.NewCMakeToolChainFilePage_Browse);
		pathButton.addListener(SWT.Selection, e -> {
			FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
			dialog.setText(Messages.NewCMakeToolChainFilePage_Select);
			String path = dialog.open();
			if (path != null) {
				pathText.setText(path);
			}
		});

		Label tcLabel = new Label(comp, SWT.NONE);
		tcLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		tcLabel.setText(Messages.NewCMakeToolChainFilePage_Toolchain);

		tcCombo = new Combo(comp, SWT.READ_ONLY);
		tcCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		try {
			IToolChainManager tcManager = Activator.getService(IToolChainManager.class);
			toolchains = tcManager.getAllToolChains().toArray(new IToolChain[0]);
			for (IToolChain tc : toolchains) {
				tcCombo.add(tc.getName());
			}
			tcCombo.select(0);
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}

		setControl(comp);
		validate();
	}

	private void validate() {
		setPageComplete(false);

		String path = pathText.getText();
		if (path.isEmpty()) {
			setErrorMessage(Messages.NewCMakeToolChainFilePage_NoPath);
			return;
		}

		setPageComplete(true);
		setErrorMessage(null);
	}

	public ICMakeToolChainFile getNewFile() {
		ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
		ICMakeToolChainFile file = manager.newToolChainFile(Paths.get(pathText.getText()));

		IToolChain tc = toolchains[tcCombo.getSelectionIndex()];
		file.setProperty(CMakeBuildConfiguration.TOOLCHAIN_TYPE, tc.getTypeId());
		file.setProperty(CMakeBuildConfiguration.TOOLCHAIN_ID, tc.getId());

		return file;
	}

}
