/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.MessageLine;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MakeTargetDialog extends Dialog {

	protected MessageLine fStatusLine;
	private static final String TARGET_PREFIX = "TargetBlock"; //$NON-NLS-1$
	private static final String TARGET_NAME_LABEL = TARGET_PREFIX + ".target.label"; //$NON-NLS-1$

	private static final String BUILD_ARGUMENT_PREFIX = "BuildTarget"; //$NON-NLS-1$
	private static final String BUILD_ARGUMENT_GROUP = BUILD_ARGUMENT_PREFIX + ".target.group_label"; //$NON-NLS-1$
	private static final String BUILD_ARGUMENT_LABEL = BUILD_ARGUMENT_PREFIX + ".target.label"; //$NON-NLS-1$

	private static final String SETTING_PREFIX = "SettingsBlock"; //$NON-NLS-1$

	private static final String MAKE_SETTING_GROUP = SETTING_PREFIX + ".makeSetting.group_label"; //$NON-NLS-1$
	private static final String MAKE_SETTING_STOP_ERROR = SETTING_PREFIX + ".makeSetting.stopOnError"; //$NON-NLS-1$

	private static final String MAKE_CMD_GROUP = SETTING_PREFIX + ".makeCmd.group_label"; //$NON-NLS-1$
	private static final String MAKE_CMD_USE_DEFAULT = SETTING_PREFIX + ".makeCmd.use_default"; //$NON-NLS-1$
	private static final String MAKE_CMD_LABEL = SETTING_PREFIX + ".makeCmd.label"; //$NON-NLS-1$

	Text targetNameText;
	Button stopOnErrorButton;
	Button runAllBuildersButton;
	Text commandText;
	Button defButton;
	Text targetText;

	IMakeTargetManager fTargetManager;
	IContainer fContainer;

	private IPath buildCommand;
	private boolean isDefaultCommand;
	private boolean isStopOnError;
	private boolean runAllBuilders = true;
	private String buildArguments;
	private String targetString;
	private String targetName;
	private String targetBuildID;
	protected IMakeTarget fTarget;
	private boolean initializing = true;

	/**
	 * @param parentShell
	 */
	public MakeTargetDialog(Shell parentShell, IMakeTarget target) throws CoreException {
		this(parentShell, target.getContainer());
		fTarget = target;
		isStopOnError = target.isStopOnError();
		isDefaultCommand = target.isDefaultBuildCmd();
		buildCommand = target.getBuildCommand();
		buildArguments = target.getBuildArguments();
		targetName = target.getName();
		targetString = target.getBuildTarget();
		targetBuildID = target.getTargetBuilderID();
		runAllBuilders = target.runAllBuilders();
	}

	/**
	 * @param parentShell
	 */
	public MakeTargetDialog(Shell parentShell, IContainer container) throws CoreException {
		super(parentShell);
		fContainer = container;
		fTargetManager = MakeCorePlugin.getDefault().getTargetManager();
		String[] id = fTargetManager.getTargetBuilders(container.getProject());
		if (id.length == 0) {
			throw new CoreException(new Status(IStatus.ERROR, MakeUIPlugin.getUniqueIdentifier(), -1,
					MakeUIPlugin.getResourceString("MakeTargetDialog.exception.noTargetBuilderOnProject"), null)); //$NON-NLS-1$
		}
		targetBuildID = id[0];
		IMakeBuilderInfo buildInfo = MakeCorePlugin.createBuildInfo(container.getProject(),
				fTargetManager.getBuilderID(targetBuildID));
		isStopOnError = buildInfo.isStopOnError();
		isDefaultCommand = buildInfo.isDefaultBuildCmd();
		buildCommand = buildInfo.getBuildCommand();
		buildArguments = buildInfo.getBuildArguments();
		targetString = buildInfo.getIncrementalBuildTarget();
	}

	protected void configureShell(Shell newShell) {
		newShell.setText(getTitle());
		super.configureShell(newShell);
	}

	private String getTitle() {
		String title;
		if (fTarget == null) {
			title = MakeUIPlugin.getResourceString("MakeTargetDialog.title.createMakeTarget"); //$NON-NLS-1$
		} else {
			title = MakeUIPlugin.getResourceString("MakeTargetDialog.title.modifyMakeTarget"); //$NON-NLS-1$
		}
		return title;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		initializeDialogUnits(composite);

		createNameControl(composite);
		createTargetControl(composite);
		createBuildCmdControls(composite);
		createSettingControls(composite);

		fStatusLine = new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(50);
		fStatusLine.setLayoutData(gd);

		initializing = false;
		return composite;
	}

	protected void createNameControl(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 2);
		((GridLayout)composite.getLayout()).makeColumnsEqualWidth = false;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(50);
		composite.setLayoutData(gd);
		Label label = ControlFactory.createLabel(composite, MakeUIPlugin.getResourceString(TARGET_NAME_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		targetNameText = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
		((GridData) (targetNameText.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetNameText.getLayoutData())).grabExcessHorizontalSpace = true;
		targetNameText.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event e) {
				String newName = targetNameText.getText().trim();
				if (newName.equals("")) { //$NON-NLS-1$
					fStatusLine.setErrorMessage(MakeUIPlugin.getResourceString("MakeTargetDialog.message.mustSpecifyName")); //$NON-NLS-1$
				} else
					try {
						if (fTarget != null && fTarget.getName().equals(newName)
								|| fTargetManager.findTarget(fContainer, newName) == null) {
							fStatusLine.setErrorMessage(null);
						} else {
							fStatusLine.setErrorMessage(MakeUIPlugin.getResourceString("MakeTargetDialog.message.targetWithNameExists")); //$NON-NLS-1$
						}
					} catch (CoreException ex) {
						fStatusLine.setErrorMessage(ex.getLocalizedMessage());
					}
				updateButtons();
			}
		});
	}

	protected void createSettingControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_SETTING_GROUP), 1);
		stopOnErrorButton = new Button(group, SWT.CHECK);
		stopOnErrorButton.setText(MakeUIPlugin.getResourceString(MAKE_SETTING_STOP_ERROR));
		stopOnErrorButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});

		if (isStopOnError) {
			stopOnErrorButton.setSelection(true);
		}
		if (isDefaultCommand) {
			stopOnErrorButton.setEnabled(true);
		} else {
			stopOnErrorButton.setEnabled(false);
		}
		runAllBuildersButton = new Button(group, SWT.CHECK);
		runAllBuildersButton.setText(MakeUIPlugin.getResourceString("SettingsBlock.makeSetting.runAllBuilders")); //$NON-NLS-1$
		runAllBuildersButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		if (runAllBuilders) {
			runAllBuildersButton.setSelection(true);
		}
	}

	protected void createBuildCmdControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_CMD_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(50);
		group.setLayoutData(gd);
		defButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_CMD_USE_DEFAULT));
		defButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if (defButton.getSelection() == true) {
					commandText.setEnabled(false);
					stopOnErrorButton.setEnabled(true);
				} else {
					commandText.setEnabled(true);
					stopOnErrorButton.setEnabled(false);
				}
				updateButtons();
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		defButton.setLayoutData(gd);
		Label label = ControlFactory.createLabel(group, MakeUIPlugin.getResourceString(MAKE_CMD_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		commandText = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		((GridData) (commandText.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (commandText.getLayoutData())).grabExcessHorizontalSpace = true;
		commandText.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event e) {
				if (commandText.getText().equals("")) { //$NON-NLS-1$
					fStatusLine.setErrorMessage(MakeUIPlugin.getResourceString("MakeTargetDialog.message.mustSpecifyBuildCommand")); //$NON-NLS-1$
				}
				updateButtons();
			}
		});
		if (isDefaultCommand) {
			commandText.setEnabled(false);
		} else {
			commandText.setEnabled(true);
		}
		defButton.setSelection(isDefaultCommand);
	}

	private void createTargetControl(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(BUILD_ARGUMENT_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(50);
		group.setLayoutData(gd);
		Label label = ControlFactory.createLabel(group, MakeUIPlugin.getResourceString(BUILD_ARGUMENT_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		targetText = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		((GridData) (targetText.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetText.getLayoutData())).grabExcessHorizontalSpace = true;
		targetText.setText(targetString);
		targetText.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event e) {
				updateButtons();
			}
		});
	}

	protected void createButtonsForButtonBar(Composite parent) {
		if (fTarget != null) {
			createButton(parent, IDialogConstants.OK_ID, MakeUIPlugin.getResourceString("MakeTargetDialog.button.update"), true); //$NON-NLS-1$
		} else {
			createButton(parent, IDialogConstants.OK_ID, MakeUIPlugin.getResourceString("MakeTargetDialog.button.create"), true); //$NON-NLS-1$
		}
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		//do this here because setting the text will set enablement on the ok
		// button
		targetNameText.setFocus();
		if (targetName != null) {
			targetNameText.setText(targetName);
		} else {
			targetNameText.setText(generateUniqueName(targetString));
		}
		targetNameText.selectAll();
		if (buildCommand != null) {
			StringBuffer cmd = new StringBuffer(buildCommand.toOSString());
			if (!isDefaultCommand) {
				String args = buildArguments;
				if (args != null && !args.equals("")) { //$NON-NLS-1$
					cmd.append(" "); //$NON-NLS-1$
					cmd.append(args);
				}
			}
			commandText.setText(cmd.toString());
		}
	}

	protected void updateButtons() {
		if (getButton(IDialogConstants.OK_ID) != null) {
			getButton(IDialogConstants.OK_ID).setEnabled(targetHasChanged() && !fStatusLine.hasErrorMessage());
		}
	}

	protected boolean targetHasChanged() {
		if (initializing || fTarget == null)
			return true;
		if (isStopOnError != isStopOnError())
			return true;
		if (runAllBuilders != runAllBuilders())
			return true;
		if (isDefaultCommand != useDefaultBuildCmd())
			return true;
		if (!targetName.equals(getTargetName()))
			return true;
		if (!targetString.equals(getTarget()))
			return true;
		if (!isDefaultCommand) {
			StringBuffer cmd = new StringBuffer(buildCommand.toOSString()).append(buildArguments);
			if (!getBuildLine().equals(cmd.toString())) {
				return true;
			}
		}
		return false;
	}

	private String generateUniqueName(String targetString) {
		String newName = targetString;
		int i = 0;
		try {
			while (fTargetManager.findTarget(fContainer, newName) != null) {
				i++;
				newName = targetString + " (" + Integer.toString(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (CoreException e) {
		}
		return newName;
	}

	private boolean isStopOnError() {
		return stopOnErrorButton.getSelection();
	}

	private boolean runAllBuilders() {
		return runAllBuildersButton.getSelection();
	}

	private boolean useDefaultBuildCmd() {
		return defButton.getSelection();
	}

	private String getBuildLine() {
		if (commandText != null) {
			String cmd = commandText.getText();
			if (cmd != null)
				return cmd.trim();
		}
		return null;
	}

	private String getTarget() {
		return targetText.getText().trim();
	}

	private String getTargetName() {
		return targetNameText.getText().trim();
	}

	protected void okPressed() {
		IMakeTarget target = fTarget;
		try {
			if (fTarget == null) {
				target = fTargetManager.createTarget(fContainer.getProject(), getTargetName(), targetBuildID);
			}
			target.setStopOnError(isStopOnError());
			target.setRunAllBuilders(runAllBuilders());
			target.setUseDefaultBuildCmd(useDefaultBuildCmd());
			if (!useDefaultBuildCmd()) {
				String bldLine = getBuildLine();
				int start = 0;
				int end = -1;
				if (!bldLine.startsWith("\"")) { //$NON-NLS-1$
					end = bldLine.indexOf(' ');
				} else {
					start = 1;
					end = bldLine.indexOf('"', 1);
				}
				IPath path;
				if (end == -1) {
					path = new Path(bldLine);
				} else {
					path = new Path(bldLine.substring(start, end));
				}
				target.setBuildCommand(path);
				String args = ""; //$NON-NLS-1$
				if (end != -1) {
					args = bldLine.substring(end + 1);
				}
				target.setBuildArguments(args);
			}
			target.setBuildTarget(getTarget());

			if (fTarget == null) {
				fTargetManager.addTarget(fContainer, target);
			} else {
				if (!target.getName().equals(getTargetName())) {
					fTargetManager.renameTarget(target, getTargetName());
				}
			}
		} catch (CoreException e) {
			MakeUIPlugin.errorDialog(
					getShell(),
					MakeUIPlugin.getResourceString("MakeTargetDialog.exception.makeTargetError"), MakeUIPlugin.getResourceString("MakeTargetDialog.exception.errorAddingTarget"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		super.okPressed();
	}
}
