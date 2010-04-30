/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andrew Gvozdev (Quoin Inc) - bunch of improvements and bug fixes
 *******************************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

/**
 * The class represents Create/Modify Make Target Dialog in Make Targets View.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MakeTargetDialog extends Dialog {

	private MessageLine fStatusLine;
	private static final String TARGET_PREFIX = "TargetBlock"; //$NON-NLS-1$
	private static final String TARGET_NAME_LABEL = TARGET_PREFIX + ".target.label"; //$NON-NLS-1$

	private static final String BUILD_ARGUMENT_PREFIX = "BuildTarget"; //$NON-NLS-1$
	private static final String BUILD_ARGUMENT_GROUP = BUILD_ARGUMENT_PREFIX + ".target.group_label"; //$NON-NLS-1$
	private static final String BUILD_ARGUMENT_LABEL = BUILD_ARGUMENT_PREFIX + ".target.label"; //$NON-NLS-1$

	private static final String SETTING_PREFIX = "SettingsBlock"; //$NON-NLS-1$

	private static final String MAKE_SETTING_GROUP = SETTING_PREFIX + ".makeSetting.group_label"; //$NON-NLS-1$
	private static final String MAKE_SETTING_STOP_ERROR = SETTING_PREFIX + ".makeSetting.stopOnError"; //$NON-NLS-1$

	private static final String MAKE_CMD_GROUP = SETTING_PREFIX + ".makeCmd.group_label"; //$NON-NLS-1$
	private static final String MAKE_CMD_USE_BUILDER_SETTINGS = SETTING_PREFIX + ".makeCmd.useBuilderSettings"; //$NON-NLS-1$
	private static final String MAKE_CMD_LABEL = SETTING_PREFIX + ".makeCmd.label"; //$NON-NLS-1$

	private Text targetNameText;
	private Button sameAsNameCheckBox;
	private Text targetText;
	private Button useBuilderCommandCheckBox;
	private Text commandText;
	private Button stopOnErrorCheckBox;
	private Button runAllBuildersCheckBox;

	private final IMakeTargetManager fTargetManager;
	private final IContainer fContainer;

	private final IPath builderCommand;
	private final String builderArguments;
	private boolean isUsingBuilderCommand = true;
	private boolean isStopOnError;
	private boolean runAllBuilders = true;
	private IPath targetBuildCommand;
	private String targetBuildArguments;
	private String targetString;
	private String targetName;
	private String targetBuildID;
	private IMakeTarget fTarget;
	private boolean initializing = true;

	/**
	 * A Listener class to verify correctness of input and display an error message
	 */
	private class UpdateStatusLineListener implements Listener {

		private void setStatusLine() {
			fStatusLine.setErrorMessage(null);

			String newTargetName = targetNameText.getText().trim();
			if (newTargetName.length()==0) {
				fStatusLine.setErrorMessage(
					MakeUIPlugin.getResourceString("MakeTargetDialog.message.mustSpecifyName")); //$NON-NLS-1$
			} else if (commandText.isEnabled() && commandText.getText().trim().length()==0) {
				fStatusLine.setErrorMessage(
					MakeUIPlugin.getResourceString("MakeTargetDialog.message.mustSpecifyBuildCommand")); //$NON-NLS-1$
			} else {
				try {
					if (!newTargetName.equals(targetName) && fTargetManager.findTarget(fContainer, newTargetName) != null) {
						fStatusLine.setErrorMessage(
								MakeUIPlugin.getResourceString("MakeTargetDialog.message.targetWithNameExists")); //$NON-NLS-1$
					}
				} catch (CoreException e) {
					// ignore exception since no update action was initiated by user yet
				}

			}
		}

		public void handleEvent(Event e) {
			setStatusLine();
			updateButtons();
		}

	}

	/**
	 * This constructor is called on "Edit Make Target" action.
	 *
	 * @param parentShell - shell to display the dialog.
	 * @param target - make target to edit.
	 */
	public MakeTargetDialog(Shell parentShell, IMakeTarget target) throws CoreException {
		this(parentShell, target.getContainer());
		fTarget = target;
		isStopOnError = target.isStopOnError();
		isUsingBuilderCommand = target.isDefaultBuildCmd();
		if (!isUsingBuilderCommand) {
			targetBuildCommand = target.getBuildCommand();
			targetBuildArguments = target.getBuildArguments();
		}
		targetName = target.getName();
		targetString = target.getBuildAttribute(IMakeTarget.BUILD_TARGET, ""); //$NON-NLS-1$
		targetBuildID = target.getTargetBuilderID();
		runAllBuilders = target.runAllBuilders();
	}

	/**
	 * This constructor is called on "Add Make Target" action and from
	 * the other constructor where some initialized values can be overwritten.
	 *
	 * @param parentShell - shell to display the dialog.
	 * @param container - container where to create the target.
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
		isUsingBuilderCommand = true;
		builderCommand = buildInfo.getBuildCommand();
		targetBuildCommand = builderCommand;
		builderArguments = buildInfo.getBuildArguments();
		targetBuildArguments = builderArguments;
		targetString = buildInfo.getIncrementalBuildTarget();

		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(getTitle());
		super.configureShell(newShell);
	}

	private String getTitle() {
		String title;
		if (fTarget == null || !MakeCorePlugin.getDefault().getTargetManager().targetExists(fTarget)) {
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
	@Override
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

		targetNameText.addListener(SWT.Modify, new UpdateStatusLineListener());
	}

	protected void createSettingControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_SETTING_GROUP), 1);
		stopOnErrorCheckBox = new Button(group, SWT.CHECK);
		stopOnErrorCheckBox.setText(MakeUIPlugin.getResourceString(MAKE_SETTING_STOP_ERROR));
		stopOnErrorCheckBox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});

		if (isStopOnError) {
			stopOnErrorCheckBox.setSelection(true);
		}
		if (isUsingBuilderCommand) {
			stopOnErrorCheckBox.setEnabled(true);
		} else {
			stopOnErrorCheckBox.setEnabled(false);
		}
		runAllBuildersCheckBox = new Button(group, SWT.CHECK);
		runAllBuildersCheckBox.setText(MakeUIPlugin.getResourceString("SettingsBlock.makeSetting.runAllBuilders")); //$NON-NLS-1$
		runAllBuildersCheckBox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		if (runAllBuilders) {
			runAllBuildersCheckBox.setSelection(true);
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
		useBuilderCommandCheckBox = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_CMD_USE_BUILDER_SETTINGS));
		useBuilderCommandCheckBox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (useBuilderCommandCheckBox.getSelection() == true) {
					StringBuffer cmd = new StringBuffer(builderCommand.toString());
					if (builderArguments != null && !builderArguments.equals("")) { //$NON-NLS-1$
						cmd.append(" "); //$NON-NLS-1$
						cmd.append(builderArguments);
					}
					commandText.setText(cmd.toString());
					commandText.setEnabled(false);
					stopOnErrorCheckBox.setEnabled(true);
				} else {
					commandText.setEnabled(true);
					stopOnErrorCheckBox.setEnabled(false);
				}
				updateButtons();
			}
		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		useBuilderCommandCheckBox.setLayoutData(gd);
		Label label = ControlFactory.createLabel(group, MakeUIPlugin.getResourceString(MAKE_CMD_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		commandText = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		((GridData) (commandText.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (commandText.getLayoutData())).grabExcessHorizontalSpace = true;

		commandText.addListener(SWT.Modify, new UpdateStatusLineListener());

		if (isUsingBuilderCommand) {
			commandText.setEnabled(false);
		} else {
			commandText.setEnabled(true);
		}
		useBuilderCommandCheckBox.setSelection(isUsingBuilderCommand);
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

		sameAsNameCheckBox = new Button(group, SWT.CHECK);
		gd = new GridData();
		gd.horizontalSpan = 2;
		sameAsNameCheckBox.setLayoutData(gd);
		sameAsNameCheckBox.setText(MakeUIPlugin.getResourceString("SettingsBlock.makeSetting.sameAsTarget")); //$NON-NLS-1$

		/* Add a listener to the target name text to update the targetText */
		targetNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (sameAsNameCheckBox.getSelection()) {
					targetText.setText(targetNameText.getText());
				}
			}
		});

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

		sameAsNameCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sameAsNameSelected();
			}
		});
		/* set sameAsNameCheck if targetName and targetString are equal */
		sameAsNameCheckBox.setSelection(targetString.equals(targetName) || (targetString.length()==0 && targetName==null));
		sameAsNameSelected();
	}

	protected void sameAsNameSelected() {
		targetText.setEnabled(!sameAsNameCheckBox.getSelection());
		if (sameAsNameCheckBox.getSelection()) {
			targetText.setText(targetNameText.getText());
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (fTarget == null || !MakeCorePlugin.getDefault().getTargetManager().targetExists(fTarget)) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		} else {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
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
		if (targetBuildCommand != null) {
			StringBuffer cmd = new StringBuffer(targetBuildCommand.toOSString());
			if (targetBuildArguments != null && !targetBuildArguments.equals("")) { //$NON-NLS-1$
				cmd.append(" "); //$NON-NLS-1$
				cmd.append(targetBuildArguments);
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
		if (initializing || fTarget == null || !MakeCorePlugin.getDefault().getTargetManager().targetExists(fTarget)) {
			return true;
		}
		if (isStopOnError != isStopOnError()) {
			return true;
		}
		if (runAllBuilders != runAllBuilders()) {
			return true;
		}
		if (isUsingBuilderCommand != isUsingBuilderCommand()) {
			return true;
		}
		if (!targetName.equals(getTargetName())) {
			return true;
		}
		if (!targetString.equals(getTarget())) {
			return true;
		}
		if (!isUsingBuilderCommand) {
			StringBuffer cmd = new StringBuffer(targetBuildCommand.toOSString()).append(targetBuildArguments);
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
		return stopOnErrorCheckBox.getSelection();
	}

	private boolean runAllBuilders() {
		return runAllBuildersCheckBox.getSelection();
	}

	private boolean isUsingBuilderCommand() {
		return useBuilderCommandCheckBox.getSelection();
	}

	private String getBuildLine() {
		if (commandText != null) {
			String cmd = commandText.getText();
			if (cmd != null) {
				return cmd.trim();
			}
		}
		return null;
	}

	private String getTarget() {
		return targetText.getText().trim();
	}

	private String getTargetName() {
		return targetNameText.getText().trim();
	}

	@Override
	protected void okPressed() {
		IMakeTarget target = fTarget;
		try {
			String targetName = getTargetName();
			if (target == null) {
				target = fTargetManager.createTarget(fContainer.getProject(), targetName, targetBuildID);
			} else {
				if (!target.getName().equals(targetName)) {
					// if necessary rename last target property, too
					String lastTargetName = null;
					IContainer container = target.getContainer();
					try {
						lastTargetName = (String)container.getSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), "lastTarget")); //$NON-NLS-1$
					} catch (CoreException e) {
					}
					if (lastTargetName != null && lastTargetName.equals(target.getName())) {
						IPath path = container.getProjectRelativePath().removeFirstSegments(
								container.getProjectRelativePath().segmentCount());
						path = path.append(targetName);
						container.setSessionProperty(new QualifiedName(MakeUIPlugin.getUniqueIdentifier(), "lastTarget"), //$NON-NLS-1$
								path.toString());
					}

					fTargetManager.renameTarget(target, targetName);
				}
			}
			target.setStopOnError(isStopOnError());
			target.setRunAllBuilders(runAllBuilders());
			target.setUseDefaultBuildCmd(isUsingBuilderCommand());
			if (isUsingBuilderCommand()) {
				target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, builderCommand.toString());
				target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, builderArguments);
			} else {
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
				target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, path.toString());
				String args = ""; //$NON-NLS-1$
				if (end != -1) {
					args = bldLine.substring(end + 1);
				}
				target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, args);
			}
			target.setBuildAttribute(IMakeTarget.BUILD_TARGET, getTarget());

			if (fTarget == null || !MakeCorePlugin.getDefault().getTargetManager().targetExists(fTarget)) {
				fTargetManager.addTarget(fContainer, target);
			}
		} catch (CoreException e) {
			MakeUIPlugin.errorDialog(
					getShell(),
					MakeUIPlugin.getResourceString("MakeTargetDialog.exception.makeTargetError"), MakeUIPlugin.getResourceString("MakeTargetDialog.exception.errorAddingTarget"), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		super.okPressed();
	}
}
