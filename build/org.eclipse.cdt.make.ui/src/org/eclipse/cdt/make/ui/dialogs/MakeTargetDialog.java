/*
 * Created on 22-Aug-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.*;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.RadioButtonsArea;
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
	private static final String TARGET_NAME_LABEL = TARGET_PREFIX + ".target.label";

	private static final String BUILD_ARGUMENT_PREFIX = "BuildTarget"; //$NON-NLS-1$
	private static final String BUILD_ARGUMENT_GROUP = BUILD_ARGUMENT_PREFIX + ".target.group_label";
	private static final String BUILD_ARGUMENT_LABEL = BUILD_ARGUMENT_PREFIX + ".target.label";

	private static final String SETTING_PREFIX = "SettingsBlock"; //$NON-NLS-1$

	private static final String MAKE_SETTING_GROUP = SETTING_PREFIX + ".makeSetting.group_label"; //$NON-NLS-1$
	private static final String MAKE_SETTING_KEEP_GOING = SETTING_PREFIX + ".makeSetting.keepOnGoing"; //$NON-NLS-1$
	private static final String MAKE_SETTING_STOP_ERROR = SETTING_PREFIX + ".makeSetting.stopOnError"; //$NON-NLS-1$

	private static final String MAKE_CMD_GROUP = SETTING_PREFIX + ".makeCmd.group_label"; //$NON-NLS-1$
	private static final String MAKE_CMD_USE_DEFAULT = SETTING_PREFIX + ".makeCmd.use_default"; //$NON-NLS-1$
	private static final String MAKE_CMD_LABEL = SETTING_PREFIX + ".makeCmd.label"; //$NON-NLS-1$

	private static final String KEEP_ARG = "keep"; //$NON-NLS-1$
	private static final String STOP_ARG = "stop"; //$NON-NLS-1$

	Text targetNameText;
	RadioButtonsArea stopRadioButtons;
	Text commandText;
	Button defButton;
	Text targetText;

	IMakeTargetManager fTargetManager;
	IContainer fContainer;

	private IPath buildCommand;
	private boolean isDefaultCommand;
	private boolean isStopOnError;
	private String buildArguments;
	private String targetString;
	private String targetName;
	private String targetBuildID;
	protected IMakeTarget fTarget;

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
			throw new CoreException(
				new Status(IStatus.ERROR, MakeUIPlugin.getUniqueIdentifier(), -1, "Not target builders on the project", null));
		}
		targetBuildID = id[0];
		IMakeBuilderInfo buildInfo =
			MakeCorePlugin.createBuildInfo(container.getProject(), fTargetManager.getBuilderID(targetBuildID));
		isStopOnError = buildInfo.isStopOnError();
		isDefaultCommand = buildInfo.isDefaultBuildCmd();
		buildCommand = buildInfo.getBuildCommand();
		buildArguments = buildInfo.getBuildArguments();
		targetString = buildInfo.getIncrementalBuildTarget();
		targetName = "";
	}

	protected void configureShell(Shell newShell) {
		String title;
		if (fTarget == null) {
			title = "Create Make target.";
		} else {
			title = "Modify Make target,";
		}
		newShell.setText(title);
		super.configureShell(newShell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		initializeDialogUnits(composite);

		String title;
		if (fTarget == null) {
			title = "Create a new Make target.";
		} else {
			title = "Modify a Make target,";
		}

		fStatusLine = new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(50);
		fStatusLine.setLayoutData(gd);
		fStatusLine.setMessage(title);

		createNameControl(composite);
		createSettingControls(composite);
		createBuildCmdControls(composite);
		createTargetControl(composite);

		return composite;
	}

	protected void createNameControl(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 2);
		((GridLayout) composite.getLayout()).makeColumnsEqualWidth = false;
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = ControlFactory.createLabel(composite, MakeUIPlugin.getResourceString(TARGET_NAME_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		targetNameText = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
		((GridData) (targetNameText.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetNameText.getLayoutData())).grabExcessHorizontalSpace = true;
		targetNameText.setText(targetName);
		targetNameText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				String newName = targetNameText.getText().trim();
				if (newName.equals("")) {
					fStatusLine.setErrorMessage("Must specify a target name.");
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				} else if (
					fTarget != null
						&& fTarget.getName().equals(newName)
						|| fTargetManager.findTarget(fContainer, newName) == null) {
					fStatusLine.setErrorMessage(null);
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				} else {
					fStatusLine.setErrorMessage("Target with that name already exits");
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
			}

		});
	}

	protected void createSettingControls(Composite parent) {
		String[][] radios = new String[][] { { MakeUIPlugin.getResourceString(MAKE_SETTING_STOP_ERROR), STOP_ARG }, {
				MakeUIPlugin.getResourceString(MAKE_SETTING_KEEP_GOING), KEEP_ARG }
		};
		stopRadioButtons = new RadioButtonsArea(parent, MakeUIPlugin.getResourceString(MAKE_SETTING_GROUP), 1, radios);
		if (isStopOnError)
			stopRadioButtons.setSelectValue(STOP_ARG);
		else
			stopRadioButtons.setSelectValue(KEEP_ARG);
	}

	protected void createBuildCmdControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_CMD_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_CMD_USE_DEFAULT));
		defButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (defButton.getSelection() == true) {
					commandText.setEnabled(false);
					stopRadioButtons.setEnabled(true);
				} else {
					commandText.setEnabled(true);
					stopRadioButtons.setEnabled(false);
				}
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
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
				if (commandText.getText().equals("")) {
					fStatusLine.setErrorMessage("Must specify a build command");
				}
			}
		});
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
		if (isDefaultCommand) {
			commandText.setEnabled(false);
		} else {
			stopRadioButtons.setEnabled(false);
		}
		defButton.setSelection(isDefaultCommand);
	}

	private void createTargetControl(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(BUILD_ARGUMENT_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = ControlFactory.createLabel(group, MakeUIPlugin.getResourceString(BUILD_ARGUMENT_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		targetText = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		((GridData) (targetText.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetText.getLayoutData())).grabExcessHorizontalSpace = true;
		targetText.setText(targetString);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		if (fTarget != null) {
			createButton(parent, IDialogConstants.OK_ID, "Update", true);
		} else {
			createButton(parent, IDialogConstants.OK_ID, "Create", true);
		}
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	private boolean isStopOnError() {
		return stopRadioButtons.getSelectedValue().equals(STOP_ARG);
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

	protected void okPressed() {
		IMakeTarget target = fTarget;
		try {
			if (fTarget == null) {
				target = fTargetManager.createTarget(fContainer.getProject(), targetNameText.getText().trim(), targetBuildID);
			}

			target.setStopOnError(isStopOnError());
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
			target.setBuildTarget(targetText.getText().trim());

			if (fTarget == null) {
				fTargetManager.addTarget(fContainer, target);
			} else {
				if (!target.getName().equals(targetNameText.getText().trim())) {
					fTargetManager.renameTarget(target, targetNameText.getText().trim());
				}
			}
		} catch (CoreException e) {
			MakeUIPlugin.errorDialog(getShell(), "Make Target Error", "Error adding target", e);
		}
		super.okPressed();
	}
}
