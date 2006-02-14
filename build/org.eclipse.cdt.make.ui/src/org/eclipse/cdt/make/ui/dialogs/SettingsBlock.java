/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class SettingsBlock extends AbstractCOptionPage {

	private static final String PREFIX = "SettingsBlock"; //$NON-NLS-1$
	private static final String MAKE_LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String MAKE_MESSAGE = PREFIX + ".message"; //$NON-NLS-1$

	private static final String MAKE_SETTING_GROUP = PREFIX + ".makeSetting.group_label"; //$NON-NLS-1$
	private static final String MAKE_SETTING_STOP_ERROR = PREFIX + ".makeSetting.stopOnError"; //$NON-NLS-1$

	private static final String MAKE_CMD_GROUP = PREFIX + ".makeCmd.group_label"; //$NON-NLS-1$
	private static final String MAKE_CMD_USE_DEFAULT = PREFIX + ".makeCmd.use_default"; //$NON-NLS-1$
	private static final String MAKE_CMD_LABEL = PREFIX + ".makeCmd.label"; //$NON-NLS-1$

	private static final String MAKE_WORKBENCH_BUILD_GROUP = PREFIX + ".makeWorkbench.group_label"; //$NON-NLS-1$
	private static final String MAKE_WORKBENCH_BUILD_TYPE = PREFIX + ".makeWorkbench.type"; //$NON-NLS-1$
	private static final String MAKE_WORKBENCH_BUILD_TARGET = PREFIX + ".makeWorkbench.target"; //$NON-NLS-1$
	private static final String MAKE_WORKBENCH_BUILD_AUTO = PREFIX + ".makeWorkbench.auto"; //$NON-NLS-1$
	private static final String MAKE_WORKBENCH_BUILD_INCR = PREFIX + ".makeWorkbench.incremental"; //$NON-NLS-1$
	private static final String MAKE_WORKBENCH_BUILD_FULL = PREFIX + ".makeWorkbench.full"; //$NON-NLS-1$
	private static final String MAKE_WORKBENCH_BUILD_CLEAN = PREFIX + ".makeWorkbench.clean"; //$NON-NLS-1$

	private static final String MAKE_BUILD_DIR_GROUP = PREFIX + ".makeDir.group_label"; //$NON-NLS-1$
	private static final String MAKE_BUILD_DIR_LABEL = PREFIX + ".makeDir.label"; //$NON-NLS-1$
	private static final String MAKE_BUILD_DIR_BROWSE = PREFIX + ".makeDir.browse"; //$NON-NLS-1$

	private static final String MAKE_BUILD_AUTO_TARGET = PREFIX + ".makeWorkbench.autoBuildTarget"; //$NON-NLS-1$
	private static final String MAKE_BUILD_INCREMENTAL_TARGET = PREFIX + ".makeWorkbench.incrementalBuildTarget"; //$NON-NLS-1$
	private static final String MAKE_BUILD_FULL_TARGET = PREFIX + ".makeWorkbench.fullBuildTarget"; //$NON-NLS-1$
	private static final String MAKE_BUILD_CLEAN_TARGET = PREFIX + ".makeWorkbench.cleanTarget"; //$NON-NLS-1$

	Button stopOnErrorButton;
	Button defButton;
	Text buildCommand;
	Button argumentVariablesButton;

	Text buildLocation;
	Button locationVariablesButton;

	Text targetIncr;
	Text targetAuto;
	Text targetClean;

	Button incrButton;
	Button autoButton;
	Button cleanButton;

	Button incrVariableButton;
	Button autoVariableButton;
	Button cleanVariableButton;
	
	IMakeBuilderInfo fBuildInfo;
	Preferences fPrefs;
	String fBuilderID;

	public SettingsBlock(Preferences prefs, String builderID) {
		super(MakeUIPlugin.getResourceString(MAKE_LABEL));
		setDescription(MakeUIPlugin.getResourceString(MAKE_MESSAGE));
		fPrefs = prefs;
		fBuilderID = builderID;
	}

	protected void createSettingControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_SETTING_GROUP), 1);
		stopOnErrorButton = new Button(group, SWT.CHECK);
		stopOnErrorButton.setText(MakeUIPlugin.getResourceString(MAKE_SETTING_STOP_ERROR));
		if (fBuildInfo.isStopOnError()) {
			stopOnErrorButton.setSelection(true);
		}
		stopOnErrorButton.setEnabled(fBuildInfo.isDefaultBuildCmd());
	}

	protected void createBuildCmdControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_CMD_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_CMD_USE_DEFAULT));
		defButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if (defButton.getSelection() == true) {
					buildCommand.setEnabled(false);
					argumentVariablesButton.setEnabled(false);
					stopOnErrorButton.setEnabled(true);
					getContainer().updateContainer();
				} else {
					buildCommand.setEnabled(true);
					argumentVariablesButton.setEnabled(true);
					stopOnErrorButton.setEnabled(false);
					getContainer().updateContainer();
				}
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		defButton.setLayoutData(gd);
		Label label = ControlFactory.createLabel(group, MakeUIPlugin.getResourceString(MAKE_CMD_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		buildCommand = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);

		((GridData) (buildCommand.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (buildCommand.getLayoutData())).grabExcessHorizontalSpace = true;
		buildCommand.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event e) {
				getContainer().updateContainer();
			}
		});
		if (fBuildInfo.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, null) != null) {
			StringBuffer cmd = new StringBuffer(fBuildInfo.getBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, "")); //$NON-NLS-1$
			if (!fBuildInfo.isDefaultBuildCmd()) {
				String args = fBuildInfo.getBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS, ""); //$NON-NLS-1$ 
				if (args != null && !args.equals("")) { //$NON-NLS-1$
					cmd.append(" "); //$NON-NLS-1$
					cmd.append(args);
				}
			}
			buildCommand.setText(cmd.toString());
		}
		argumentVariablesButton = addVariablesButton(group, buildCommand); 
		if (fBuildInfo.isDefaultBuildCmd()) {
			buildCommand.setEnabled(false);
			argumentVariablesButton.setEnabled(false);
		}
		defButton.setSelection(fBuildInfo.isDefaultBuildCmd());
	}

	protected void createWorkBenchBuildControls(Composite parent) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				targetAuto.setEnabled(autoButton.getSelection());
				autoVariableButton.setEnabled(autoButton.getSelection());
				targetIncr.setEnabled(incrButton.getSelection());
				incrVariableButton.setEnabled(incrButton.getSelection());
				targetClean.setEnabled(cleanButton.getSelection());
				cleanVariableButton.setEnabled(cleanButton.getSelection());
				getContainer().updateContainer();
			}

		};
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = new Label(group, SWT.NONE);
		label.setText(MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_TYPE));
		label = new Label(group, SWT.NONE);
		label.setText(MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_TARGET));
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		autoButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_AUTO));
		autoButton.addSelectionListener(selectionAdapter);
		autoButton.setSelection(fBuildInfo.isAutoBuildEnable());
		targetAuto = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		targetAuto.setText(fBuildInfo.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, "")); //$NON-NLS-1$
		((GridData) (targetAuto.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetAuto.getLayoutData())).grabExcessHorizontalSpace = true;
		addControlAccessibleListener(targetAuto, MakeUIPlugin.getResourceString(MAKE_BUILD_AUTO_TARGET));
		autoVariableButton = addVariablesButton(group, targetAuto);
		String noteTitle = MakeUIPlugin.getResourceString("SettingsBlock.makeWorkbench.note"); //$NON-NLS-1$
		String noteMessage = MakeUIPlugin.getResourceString("SettingsBlock.makeWorkbench.autobuildMessage"); //$NON-NLS-1$
		Composite noteControl = createNoteComposite(JFaceResources.getDialogFont(), group, noteTitle, noteMessage);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 3;
		noteControl.setLayoutData(gd);
		incrButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_INCR));
		incrButton.addSelectionListener(selectionAdapter);
		incrButton.setSelection(fBuildInfo.isIncrementalBuildEnabled());
		targetIncr = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		targetIncr.setText(fBuildInfo.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, "")); //$NON-NLS-1$
		((GridData) (targetIncr.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetIncr.getLayoutData())).grabExcessHorizontalSpace = true;
		addControlAccessibleListener(targetIncr, MakeUIPlugin.getResourceString(MAKE_BUILD_INCREMENTAL_TARGET));
		incrVariableButton = addVariablesButton(group, targetIncr);
		cleanButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_CLEAN));
		cleanButton.addSelectionListener(selectionAdapter);
		cleanButton.setSelection(fBuildInfo.isCleanBuildEnabled());
		targetClean = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		targetClean.setText(fBuildInfo.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, "")); //$NON-NLS-1$
		((GridData) (targetClean.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetClean.getLayoutData())).grabExcessHorizontalSpace = true;
		addControlAccessibleListener(targetClean, MakeUIPlugin.getResourceString(MAKE_BUILD_CLEAN_TARGET));
		cleanVariableButton = addVariablesButton(group, targetClean);
		selectionAdapter.widgetSelected(null);
	}

	private Button addVariablesButton(Composite parent, final Text control) {
		String variablesTitle = MakeUIPlugin.getResourceString("SettingsBlock.variables"); //$NON-NLS-1$
		Button variablesButton = createPushButton(parent, variablesTitle, null);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		variablesButton.setLayoutData(gd);
		variablesButton.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent arg0) {
				handleVariablesButtonSelected(control);
			}
		});
		return variablesButton;
	}

	protected Composite createNoteComposite(Font font, Composite composite, String title, String message) {
		Composite messageComposite = new Composite(composite, SWT.NONE);
		GridLayout messageLayout = new GridLayout();
		messageLayout.numColumns = 2;
		messageLayout.marginWidth = 0;
		messageLayout.marginHeight = 0;
		messageComposite.setLayout(messageLayout);
		messageComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		messageComposite.setFont(font);

		final Label noteLabel = new Label(messageComposite, SWT.BOLD);
		noteLabel.setText(title);
		noteLabel.setFont(JFaceResources.getBannerFont());
		noteLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		final IPropertyChangeListener fontListener = new IPropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				if (JFaceResources.BANNER_FONT.equals(event.getProperty())) {
					noteLabel.setFont(JFaceResources.getFont(JFaceResources.BANNER_FONT));
				}
			}
		};
		JFaceResources.getFontRegistry().addListener(fontListener);
		noteLabel.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent event) {
				JFaceResources.getFontRegistry().removeListener(fontListener);
			}
		});

		Label messageLabel = new Label(messageComposite, SWT.WRAP);
		messageLabel.setText(message);
		messageLabel.setFont(font);
		return messageComposite;
	}
	public void addControlAccessibleListener(Control control, String controlName) {
		control.getAccessible().addAccessibleListener(new ControlAccessibleListener(controlName));
	}
	private class ControlAccessibleListener extends AccessibleAdapter {

		private String controlName;
		ControlAccessibleListener(String name) {
			controlName = name;
		}
		public void getName(AccessibleEvent e) {
			e.result = controlName;
		}
	}
	protected void createBuilderWorkingDirControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_BUILD_DIR_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = ControlFactory.createLabel(group, MakeUIPlugin.getResourceString(MAKE_BUILD_DIR_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		buildLocation = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		((GridData) (buildLocation.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (buildLocation.getLayoutData())).grabExcessHorizontalSpace = true;
		buildLocation.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event e) {
				getContainer().updateContainer();
			}
		});
		Button browse = new Button(group, SWT.NONE);
		browse.setText(MakeUIPlugin.getResourceString(MAKE_BUILD_DIR_BROWSE));
		browse.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), getContainer().getProject(), true,
						MakeUIPlugin.getResourceString("SettingsBlock.title.selectLocationToBuildFrom")); //$NON-NLS-1$
				if (dialog.open() == Window.OK) {
					Object[] selection = dialog.getResult();
					if (selection.length > 0) {
						buildLocation.setText( ((IPath)selection[0]).toOSString());
					}
				}
			}
		});
		buildLocation.setText(fBuildInfo.getBuildAttribute(IMakeCommonBuildInfo.BUILD_LOCATION, "")); //$NON-NLS-1$
		locationVariablesButton = addVariablesButton(group, buildLocation);
	}

	/**
	 * A variable entry button has been pressed for the given text field. Prompt
	 * the user for a variable and enter the result in the given field.
	 */
	private void handleVariablesButtonSelected(Text textField) {
		String variable = getVariable();
		if (variable != null) {
			textField.append(variable);
		}
	}

	/**
	 * Prompts the user to choose and configure a variable and returns the
	 * resulting string, suitable to be used as an attribute.
	 */
	private String getVariable() {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		return dialog.getVariableExpression();
	}
	public void createControl(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 1);
		setControl(composite);

		MakeUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(), IMakeHelpContextIds.MAKE_BUILDER_SETTINGS);

		if (fBuildInfo == null) {
			ControlFactory.createEmptySpace(composite);
			ControlFactory.createLabel(composite, MakeUIPlugin.getResourceString("SettingsBlock.label.missingBuilderInformation")); //$NON-NLS-1$
			return;
		}

		createBuildCmdControls(composite);
		createSettingControls(composite);
		createWorkBenchBuildControls(composite);

		if (getContainer().getProject() != null) {
			createBuilderWorkingDirControls(composite);
		}
	}

	public boolean isValid() {
		if (defButton != null && defButton.getSelection() != true) {
			String cmd = getBuildLine();
			if (cmd == null || cmd.length() == 0) {
				return false;
			}
		}
		return true;
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
		// Missing builder info
		if (fBuildInfo == null) {
			return;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		IWorkspace workspace = MakeUIPlugin.getWorkspace();
		// To avoid multi-build
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(MakeUIPlugin.getResourceString("SettingsBlock.monitor.applyingSettings"), 1); //$NON-NLS-1$
				IMakeBuilderInfo info = null;
				if (getContainer().getProject() != null) {
					try {
						info = MakeCorePlugin.createBuildInfo(getContainer().getProject(), fBuilderID);
					} catch (CoreException e) {
						// disabled builder... just log it
						MakeCorePlugin.log(e);
						return;
					}
				} else {
					info = MakeCorePlugin.createBuildInfo(fPrefs, fBuilderID, false);
				}
				info.setStopOnError(isStopOnError());
				info.setUseDefaultBuildCmd(useDefaultBuildCmd());
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
					String path;
					if (end == -1) {
						path = bldLine;
					} else {
						path = bldLine.substring(start, end);
					}
					info.setBuildAttribute(IMakeBuilderInfo.BUILD_COMMAND, path);
					String args = ""; //$NON-NLS-1$
					if (end != -1) {
						args = bldLine.substring(end + 1);
					}
					info.setBuildAttribute(IMakeBuilderInfo.BUILD_ARGUMENTS, args);
				}
				info.setAutoBuildEnable(autoButton.getSelection());
				info.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, targetAuto.getText().trim());
				info.setIncrementalBuildEnable(incrButton.getSelection());
				info.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, targetIncr.getText().trim());
				info.setFullBuildEnable(incrButton.getSelection());
				info.setCleanBuildEnable(cleanButton.getSelection());
				info.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, targetClean.getText().trim());
				if (buildLocation != null) {
					info.setBuildAttribute(IMakeBuilderInfo.BUILD_LOCATION, buildLocation.getText().trim());
				}
			}
		};
		if (getContainer().getProject() != null) {
			workspace.run(operation, monitor);
		} else {
			operation.run(monitor);
		}
	}

	public void performDefaults() {
		// Missing builder info
		if (fBuildInfo == null) {
			return;
		}
		IMakeBuilderInfo info;
		if (getContainer().getProject() != null) {
			info = MakeCorePlugin.createBuildInfo(fPrefs, fBuilderID, false);
		} else {
			info = MakeCorePlugin.createBuildInfo(fPrefs, fBuilderID, true);
		}
		if (info.isStopOnError())
			stopOnErrorButton.setSelection(true);
		else
			stopOnErrorButton.setSelection(false);
		if (info.getBuildCommand() != null) {
			StringBuffer cmd = new StringBuffer(info.getBuildCommand().toOSString());
			if (!info.isDefaultBuildCmd()) {
				String args = info.getBuildArguments();
				if (args != null && !args.equals("")) { //$NON-NLS-1$
					cmd.append(" "); //$NON-NLS-1$
					cmd.append(args);
				}
			}
			buildCommand.setText(cmd.toString());
		}
		if (info.isDefaultBuildCmd()) {
			buildCommand.setEnabled(false);
			argumentVariablesButton.setEnabled(false);
			stopOnErrorButton.setEnabled(true);
		} else {
			buildCommand.setEnabled(true);
			argumentVariablesButton.setEnabled(true);
			stopOnErrorButton.setEnabled(false);
		}
		
		defButton.setSelection(info.isDefaultBuildCmd());
		
		autoButton.setSelection(info.isAutoBuildEnable());
		autoVariableButton.setEnabled(info.isAutoBuildEnable());
		targetAuto.setEnabled(info.isAutoBuildEnable());
		targetAuto.setText(info.getAutoBuildTarget());
		incrButton.setSelection(info.isIncrementalBuildEnabled());
		incrVariableButton.setEnabled(info.isIncrementalBuildEnabled());
		targetIncr.setText(info.getIncrementalBuildTarget());
		targetIncr.setEnabled(info.isIncrementalBuildEnabled());
		cleanButton.setSelection(info.isCleanBuildEnabled());
		cleanVariableButton.setEnabled(info.isCleanBuildEnabled());
		targetClean.setText(info.getCleanBuildTarget());
		targetClean.setEnabled(info.isCleanBuildEnabled());
	}

	boolean isStopOnError() {
		return stopOnErrorButton.getSelection();
	}

	boolean useDefaultBuildCmd() {
		return defButton.getSelection();
	}

	String getBuildLine() {
		if (buildCommand != null) {
			String cmd = buildCommand.getText();
			if (cmd != null)
				return cmd.trim();
		}
		return null;
	}

	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);
		if (getContainer().getProject() != null) {
			try {
				fBuildInfo = MakeCorePlugin.createBuildInfo(getContainer().getProject(), fBuilderID);
			} catch (CoreException e) {
			}
		} else {
			fBuildInfo = MakeCorePlugin.createBuildInfo(fPrefs, fBuilderID, false);
		}
	}

	public String getErrorMessage() {
		if (!useDefaultBuildCmd()) {
			String cmd = getBuildLine();
			if (cmd == null || cmd.length() == 0) {
				return MakeUIPlugin.getResourceString("SettingsBlock.message.mustEnterBuildCommand"); //$NON-NLS-1$
			}
		}
		return null;
	}
}
