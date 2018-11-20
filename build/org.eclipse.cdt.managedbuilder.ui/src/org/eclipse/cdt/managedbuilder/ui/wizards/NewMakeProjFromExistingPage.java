/*******************************************************************************
 * Copyright (c) 2010, 2013 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
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
import org.eclipse.ui.progress.WorkbenchJob;

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
	Map<String, IToolChain> tcMap = new HashMap<>();

	/**
	 * True if the user entered a non-empty string in the project name field. In that state, we avoid
	 * automatically filling the project name field with the directory name (last segment of the location) he
	 * has entered.
	 */
	boolean projectNameSetByUser;

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
				validatePage();
				if (getProjectName().isEmpty()) {
					projectNameSetByUser = false;
				}
			}
		});

		// Note that the modify listener gets called not only when the user enters text but also when we
		// programatically set the field. This listener only gets called when the user modifies the field
		projectName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				projectNameSetByUser = true;
			}
		});
	}

	/**
	 * Validates the contents of the page, setting the page error message and Finish button state accordingly
	 *
	 * @since 8.1
	 */
	protected void validatePage() {
		// Don't generate an error if project name or location is empty, but do disable Finish button.
		String msg = null;
		boolean complete = true; // ultimately treated as false if msg != null

		String name = getProjectName();
		if (name.isEmpty()) {
			complete = false;
		} else {
			IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.PROJECT);
			if (!status.isOK()) {
				msg = status.getMessage();
			} else {
				IProject project = root.getProject(name);
				if (project.exists()) {
					msg = Messages.NewMakeProjFromExistingPage_4;

				}
			}
		}
		if (msg == null) {
			String loc = getLocation();
			if (loc.isEmpty()) {
				complete = false;
			} else {
				final File file = new File(loc);
				if (file.isDirectory()) {
					// Ensure we can create files in the directory.
					if (!file.canWrite())
						msg = Messages.NewMakeProjFromExistingPage_DirReadOnlyError;
					// Set the project name to the directory name but not if the user has supplied a name
					// (bugzilla 368987). Use a job to ensure proper sequence of activity, as setting the Text
					// will invoke the listener, which will invoke this method.
					else if (!projectNameSetByUser && !name.equals(file.getName())) {
						WorkbenchJob wjob = new WorkbenchJob("update project name") { //$NON-NLS-1$
							@Override
							public IStatus runInUIThread(IProgressMonitor monitor) {
								if (!projectName.isDisposed()) {
									projectName.setText(file.getName());
								}
								return Status.OK_STATUS;
							}
						};
						wjob.setSystem(true);
						wjob.schedule();
					}
				} else {
					msg = Messages.NewMakeProjFromExistingPage_8;
				}
			}
		}

		setErrorMessage(msg);
		setPageComplete((msg == null) && complete);
	}

	/** @deprecated Replaced by {@link #validatePage()} */
	@Deprecated
	public void validateProjectName() {
		validatePage();
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
				validatePage();
			}
		});
		validatePage();

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

	/** @deprecated Replaced by {@link #validatePage()} */
	@Deprecated
	void validateSource() {
		validatePage();
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
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setText(Messages.NewMakeProjFromExistingPage_10);

		tcList = new List(group, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);

		// Base the List control size on the number of total toolchains, up to 15 entries, but allocate for no
		// less than five (small list boxes look strange). A vertical scrollbar will appear as needed
		updateTcMap(false);
		gd.heightHint = tcList.getItemHeight() * (1 + Math.max(Math.min(tcMap.size(), 15), 5)); // +1 for <none>
		tcList.setLayoutData(gd);
		tcList.add(Messages.NewMakeProjFromExistingPage_11);

		final Button supportedOnly = new Button(group, SWT.CHECK);
		supportedOnly.setSelection(false);
		supportedOnly.setText(Messages.NewMakeProjFromExistingPage_show_only_supported);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		supportedOnly.setLayoutData(gd);
		supportedOnly.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTcWidget(supportedOnly.getSelection());
			}
		});

		supportedOnly.setSelection(true);
		updateTcWidget(true);
	}

	/**
	 * Load our map and with the suitable toolchains and then populate the List control
	 *
	 * @param supportedOnly
	 *            if true, consider only supported toolchains
	 */
	private void updateTcWidget(boolean supportedOnly) {
		updateTcMap(supportedOnly);
		ArrayList<String> names = new ArrayList<>(tcMap.keySet());
		Collections.sort(names);

		tcList.removeAll();
		tcList.add(Messages.NewMakeProjFromExistingPage_11); // <none>
		for (String name : names)
			tcList.add(name);

		tcList.setSelection(0); // select <none>
	}

	/**
	 * Load our map with the suitable toolchains.
	 *
	 * @param supportedOnly
	 *            if true, add only toolchains that are available and which support the host platform
	 */
	private void updateTcMap(boolean supportedOnly) {
		tcMap.clear();
		IToolChain[] toolChains = ManagedBuildManager.getRealToolChains();
		for (IToolChain toolChain : toolChains) {
			if (toolChain.isAbstract() || toolChain.isSystemObject())
				continue;
			if (supportedOnly) {
				if (!toolChain.isSupported() || !ManagedBuildManager.isPlatformOk(toolChain)) {
					continue;
				}
			}
			tcMap.put(toolChain.getUniqueRealName(), toolChain);
		}
	}

	public String getProjectName() {
		return projectName.getText().trim();
	}

	public String getLocation() {
		return location.getText().trim();
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
