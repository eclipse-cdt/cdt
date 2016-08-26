/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.internal;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.cdt.cmake.core.ICMakeToolChainFile;
import org.eclipse.cdt.cmake.core.ICMakeToolChainManager;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewCMakeToolChainFilePage extends WizardPage {

	private final Map<Path, ICMakeToolChainFile> existing;
	private Text pathText;
	private Text osText;
	private Text archText;

	public NewCMakeToolChainFilePage(Map<Path, ICMakeToolChainFile> existing) {
		super("NewCMakeToolChainFilePage", "New CMake ToolChain File", null); //$NON-NLS-1$
		this.existing = existing;
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));

		Label pathLabel = new Label(comp, SWT.NONE);
		pathLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		pathLabel.setText("Path:");

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
		pathButton.setText("Browse...");
		pathButton.addListener(SWT.Selection, e -> {
			FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
			dialog.setText("Select location for CMake toolchain file");
			String path = dialog.open();
			if (path != null) {
				pathText.setText(path);
			}
		});

		Label osLabel = new Label(comp, SWT.NONE);
		osLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		osLabel.setText("Target OS:");

		osText = new Text(comp, SWT.BORDER);
		osText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		osText.addModifyListener(e -> validate());

		Label archLabel = new Label(comp, SWT.NONE);
		archLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		archLabel.setText("Target CPU:");

		archText = new Text(comp, SWT.BORDER);
		archText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		archText.addModifyListener(e -> validate());

		setControl(comp);
		validate();
	}

	private void validate() {
		setPageComplete(false);

		String path = pathText.getText();
		if (path.isEmpty()) {
			setErrorMessage("Please set the path to the CMake toolchain file.");
			return;
		}

		if (existing.containsKey(Paths.get(path))) {
			setErrorMessage("CMake toolchain file entry already exists.");
			return;
		}

		if (osText.getText().isEmpty()) {
			setErrorMessage("Please set the target operating system.");
			return;
		}

		if (archText.getText().isEmpty()) {
			setErrorMessage("Please set the target CPU architecture.");
			return;
		}

		setPageComplete(true);
		setErrorMessage(null);
	}

	public ICMakeToolChainFile getNewFile() {
		ICMakeToolChainManager manager = Activator.getService(ICMakeToolChainManager.class);
		ICMakeToolChainFile file = manager.newToolChainFile(Paths.get(pathText.getText()));

		String os = osText.getText();
		if (!os.isEmpty()) {
			file.setProperty(IToolChain.ATTR_OS, os);
		}

		String arch = archText.getText();
		if (!arch.isEmpty()) {
			file.setProperty(IToolChain.ATTR_ARCH, arch);
		}

		return file;
	}

}
