package org.eclipse.cdt.make.ui;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.make.core.BuildInfoFactory;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.ui.AbstractCOptionPage;
import org.eclipse.cdt.ui.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.RadioButtonsArea;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class SettingsBlock extends AbstractCOptionPage {

	private static final String PREFIX = "SettingsBlock"; //$NON-NLS-1$
	private static final String MAKE_LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String MAKE_MESSAGE = PREFIX + ".message"; //$NON-NLS-1$

	private static final String MAKE_SETTING_GROUP = PREFIX + ".makeSetting.group_label"; //$NON-NLS-1$
	private static final String MAKE_SETTING_KEEP_GOING = PREFIX + ".makeSetting.keepOnGoing"; //$NON-NLS-1$
	private static final String MAKE_SETTING_STOP_ERROR = PREFIX + ".makeSetting.stopOnError"; //$NON-NLS-1$

	private static final String MAKE_CMD_GROUP = PREFIX + ".makeCmd.group_label"; //$NON-NLS-1$
	private static final String MAKE_CMD_USE_DEFAULT = PREFIX + ".makeCmd.use_default"; //$NON-NLS-1$
	private static final String MAKE_CMD_LABEL = PREFIX + ".makeCmd.label";

	private static final String MAKE_WORKBENCH_BUILD_GROUP = PREFIX + ".makeWorkbench.group_label";
	private static final String MAKE_WORKBENCH_BUILD_TYPE = PREFIX + ".makeWorkbench.type";
	private static final String MAKE_WORKBENCH_BUILD_TARGET = PREFIX + ".makeWorkbench.target";
	private static final String MAKE_WORKBENCH_BUILD_AUTO = PREFIX + ".makeWorkbench.auto";
	private static final String MAKE_WORKBENCH_BUILD_INCR = PREFIX + ".makeWorkbench.incremental";
	private static final String MAKE_WORKBENCH_BUILD_FULL = PREFIX + ".makeWorkbench.full";

	private static final String MAKE_BUILD_DIR_GROUP = PREFIX + ".makeDir.group_label";
	private static final String MAKE_BUILD_DIR_LABEL = PREFIX + ".makeDir.label";
	private static final String MAKE_BUILD_DIR_BROWSE = PREFIX + ".makeDir.browse";

	private static final String KEEP_ARG = "keep"; //$NON-NLS-1$
	private static final String STOP_ARG = "stop"; //$NON-NLS-1$

	protected RadioButtonsArea stopRadioButtons;
	protected Button defButton;
	protected Text cmdText;
	protected Text makeDirText;

	private Text targetFull;
	private Text targetIncr;
	private Text targetAuto;
	private Button fFullButton;
	private Button fIncrButton;
	private Button fAutoButton;

	private IMakeBuilderInfo fBuildInfo;
	private Preferences fPrefs;
	private String fBuilderID;
	
	public SettingsBlock(Preferences prefs, String builderID) {
		super(MakeUIPlugin.getResourceString(MAKE_LABEL));
		setDescription(MakeUIPlugin.getResourceString(MAKE_MESSAGE));
		fPrefs = prefs;
		fBuilderID = builderID;
	}

	protected void createSettingControls(Composite parent) {
		String[][] radios = new String[][] { { MakeUIPlugin.getResourceString(MAKE_SETTING_STOP_ERROR), STOP_ARG }, {
				MakeUIPlugin.getResourceString(MAKE_SETTING_KEEP_GOING), KEEP_ARG }
		};
		stopRadioButtons = new RadioButtonsArea(parent, MakeUIPlugin.getResourceString(MAKE_SETTING_GROUP), 1, radios);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		stopRadioButtons.setLayout(layout);
		if (fBuildInfo.isStopOnError())
			stopRadioButtons.setSelectValue(STOP_ARG);
		else
			stopRadioButtons.setSelectValue(KEEP_ARG);
	}

	protected void createBuildCmdControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_CMD_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.horizontalSpacing = 0;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_CMD_USE_DEFAULT));
		defButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (defButton.getSelection() == true) {
					cmdText.setEnabled(false);
					stopRadioButtons.setEnabled(true);
					getContainer().updateContainer();
				} else {
					cmdText.setEnabled(true);
					stopRadioButtons.setEnabled(false);
					getContainer().updateContainer();
				}
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		defButton.setLayoutData(gd);
		Label label = ControlFactory.createLabel(group, MakeUIPlugin.getResourceString(MAKE_CMD_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		cmdText = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		((GridData) (cmdText.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (cmdText.getLayoutData())).grabExcessHorizontalSpace = true;
		cmdText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				getContainer().updateContainer();
			}
		});
		if (fBuildInfo.getBuildCommand() != null) {
			StringBuffer cmd = new StringBuffer(fBuildInfo.getBuildCommand().toOSString());
			String args = fBuildInfo.getBuildArguments();
			if ( args != null && !args.equals("")) {
				cmd.append(" ");
				cmd.append(args);
			}
			cmdText.setText(cmd.toString());
		}
		if (fBuildInfo.isDefaultBuildCmd()) {
			cmdText.setEnabled(false);
		} else {
			stopRadioButtons.setEnabled(false);
		}
		defButton.setSelection(fBuildInfo.isDefaultBuildCmd());
	}

	protected void createWorkBenchBuildControls(Composite parent) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				targetAuto.setEnabled(fAutoButton.getSelection());
				targetFull.setEnabled(fFullButton.getSelection());
				targetIncr.setEnabled(fIncrButton.getSelection());
				getContainer().updateContainer();
			}

		};
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = new Label(group, SWT.NONE);
		label.setText(MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_TYPE));
		label = new Label(group, SWT.NONE);
		label.setText(MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_TARGET));
		fAutoButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_AUTO));
		fAutoButton.addSelectionListener(selectionAdapter);
		fAutoButton.setSelection(fBuildInfo.isAutoBuildEnable());
		targetAuto = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		targetAuto.setText(fBuildInfo.getAutoBuildTarget());
		((GridData) (targetAuto.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetAuto.getLayoutData())).grabExcessHorizontalSpace = true;
		fIncrButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_INCR));
		fIncrButton.addSelectionListener(selectionAdapter);
		fIncrButton.setSelection(fBuildInfo.isIncrementalBuildEnabled());
		targetIncr = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		targetIncr.setText(fBuildInfo.getIncrementalBuildTarget());
		((GridData) (targetIncr.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetIncr.getLayoutData())).grabExcessHorizontalSpace = true;
		fFullButton = ControlFactory.createCheckBox(group, MakeUIPlugin.getResourceString(MAKE_WORKBENCH_BUILD_FULL));
		fFullButton.addSelectionListener(selectionAdapter);
		fFullButton.setSelection(fBuildInfo.isFullBuildEnabled());
		targetFull = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		targetFull.setText(fBuildInfo.getFullBuildTarget());
		((GridData) (targetFull.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (targetFull.getLayoutData())).grabExcessHorizontalSpace = true;
		selectionAdapter.widgetSelected(null);
	}

	protected void createBuilderWorkingDirControls(Composite parent) {
		Group group = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(MAKE_BUILD_DIR_GROUP), 1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label = ControlFactory.createLabel(group, MakeUIPlugin.getResourceString(MAKE_BUILD_DIR_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		makeDirText = ControlFactory.createTextField(group, SWT.SINGLE | SWT.BORDER);
		((GridData) (makeDirText.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (makeDirText.getLayoutData())).grabExcessHorizontalSpace = true;
		makeDirText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				getContainer().updateContainer();
			}
		});
		Button browse = new Button(group, SWT.NONE);
		browse.setText(MakeUIPlugin.getResourceString(MAKE_BUILD_DIR_BROWSE));
		browse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
			}
		});
		makeDirText.setText(fBuildInfo.getBuildLocation().toOSString());
	}

	public void createControl(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 1);

		createSettingControls(composite);
		createBuildCmdControls(composite);
		createWorkBenchBuildControls(composite);

		if (getContainer().getProject() != null) {
			createBuilderWorkingDirControls(composite);
		}

		setControl(composite);
	}

	public boolean isValid() {
		if (defButton.getSelection() != true) {
			String cmd = getBuildLine();
			if (cmd == null || cmd.length() == 0) {
				return false;
			}
		}
		return true;
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Settings", 1);
		IMakeBuilderInfo info;
		if (getContainer().getProject() != null) {
			info = BuildInfoFactory.create(getContainer().getProject(), fBuilderID);
		} else {
			info = BuildInfoFactory.create(fPrefs, fBuilderID);
		}
		info.setStopOnError(isStopOnError());
		info.setUseDefaultBuildCmd(useDefaultBuildCmd());
		if (!useDefaultBuildCmd()) {
			String bldLine = getBuildLine();
			int start = 0;
			int end = -1;
			if (!bldLine.startsWith("\"")) {
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
			info.setBuildCommand(path);
			String args = "";
			if (end != -1) {
				args = bldLine.substring(end + 1);
			}
			info.setBuildArguments(args);
		}
	}

	public void performDefaults() {
		if (getContainer().getProject() != null) {
		} else {

		}
	}

	private boolean isStopOnError() {
		return stopRadioButtons.getSelectedValue().equals(STOP_ARG);
	}

	private boolean useDefaultBuildCmd() {
		return defButton.getSelection();
	}

	private String getBuildLine() {
		if (cmdText != null) {
			String cmd = cmdText.getText();
			if (cmd != null)
				return cmd.trim();
		}
		return null;
	}

	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);
		if (getContainer().getProject() != null) {
			fBuildInfo = BuildInfoFactory.create(getContainer().getProject(), fBuilderID);
		} else {
			fBuildInfo = BuildInfoFactory.create(fPrefs, fBuilderID);
		}
	}

	public String getErrorMessage() {
		if (defButton.getSelection() != true) {
			String cmd = getBuildLine();
			if (cmd == null || cmd.length() == 0) {
				return "Must enter a build command";
			}
		}
		return null;
	}
}
