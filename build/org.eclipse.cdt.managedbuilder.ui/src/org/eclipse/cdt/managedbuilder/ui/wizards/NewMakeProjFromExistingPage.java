/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * Page to select existing code location and toolchain.
 *
 * @since 7.0
 */
public class NewMakeProjFromExistingPage extends WizardPage {

	Text projectName;
	Text location;
	Button langc;
	Button langcpp;
	IWorkspaceRoot root;
	List tcList;
	Map<String, IToolChain> tcMap = new HashMap<String, IToolChain>();

	protected NewMakeProjFromExistingPage() {
		super(Messages.NewMakeProjFromExistingPage_0);
		setTitle(Messages.NewMakeProjFromExistingPage_1);
		setDescription(Messages.NewMakeProjFromExistingPage_2);

		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		addProjectNameSelector(comp);
		addSourceSelector(comp);
		addLanguageSelector(comp);
		addToolchainSelector(comp);

		setControl(comp);
	}

	public void addProjectNameSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.NewMakeProjFromExistingPage_3);

		projectName = new Text(group, SWT.BORDER);
		projectName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		projectName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateProjectName();
			}
		});
	}

	public void validateProjectName() {
		String name = projectName.getText();
		IProject project = root.getProject(name);
		if (project.exists())
			setErrorMessage(Messages.NewMakeProjFromExistingPage_4);
		else
			setErrorMessage(null);
	}

	public void addSourceSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.NewMakeProjFromExistingPage_5);

		location = new Text(group, SWT.BORDER);
		location.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		location.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateSource();
			}
		});
		validateSource();

		Button browse = new Button(group, SWT.NONE);
		browse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		browse.setText(Messages.NewMakeProjFromExistingPage_6);
		browse.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(location.getShell());
				dialog.setMessage(Messages.NewMakeProjFromExistingPage_7);
				String dir = dialog.open();
				if (dir != null)
					location.setText(dir);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	void validateSource() {
		File file= new File(location.getText());
		if (file.isDirectory()) {
			setErrorMessage(null);
			projectName.setText(file.getName());
		} else
			setErrorMessage(Messages.NewMakeProjFromExistingPage_8);
	}

	public void addLanguageSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.NewMakeProjFromExistingPage_9);

		// TODO, should be a way to dynamically list these
		langc = new Button(group, SWT.CHECK);
		langc.setText("C"); //$NON-NLS-1$
		langc.setSelection(true);

		langcpp = new Button(group, SWT.CHECK);
		langcpp.setText("C++"); //$NON-NLS-1$
		langcpp.setSelection(true);
	}

	public void addToolchainSelector(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(Messages.NewMakeProjFromExistingPage_10);

		tcList = new List(group, SWT.SINGLE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		tcList.add(Messages.NewMakeProjFromExistingPage_11);

		IToolChain[] toolChains = ManagedBuildManager.getRealToolChains();
		for (IToolChain toolChain : toolChains) {
			if (toolChain.isAbstract() || toolChain.isSystemObject())
				continue;
			tcMap.put(toolChain.getUniqueRealName(), toolChain);
		}

		ArrayList<String> names = new ArrayList<String>(tcMap.keySet());
		Collections.sort(names);
		for (String name : names)
			tcList.add(name);

		tcList.setSelection(0); // select <none>
	}

	public String getProjectName() {
		return projectName.getText();
	}

	public String getLocation() {
		return location.getText();
	}

	public boolean isC() {
		return langc.getSelection();
	}

	public boolean isCPP() {
		return langcpp.getSelection();
	}

	public IToolChain getToolChain() {
		String[] selection = tcList.getSelection();
		return selection.length != 0 ? tcMap.get(selection[0]) : null;
	}

}
