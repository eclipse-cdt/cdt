package org.eclipse.cdt.ui.wizards;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.internal.ui.CPlugin;
import org.eclipse.cdt.utils.ui.swt.IValidation;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.cdt.utils.ui.controls.RadioButtonsArea;


public class SettingsBlock implements IWizardTab {

	private static final String PREFIX = "SettingsBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String KEEP_GOING = PREFIX + ".keepOnGoing"; //$NON-NLS-1$
	private static final String STOP_ERROR = PREFIX + ".stopOnError"; //$NON-NLS-1$
	private static final String MAKE_OPTION = PREFIX + ".makeOption.label";
	//$NON-NLS-1$
	private static final String MAKE_USE_DEFAULT =
		PREFIX + ".makeOption.use_default";
	//$NON-NLS-1$
	private static final String MAKE_BUILD_CMD = PREFIX + ".makeOption.build_cmd";
	//$NON-NLS-1$

	private static final String KEEP_ARG = "keep"; //$NON-NLS-1$
	private static final String STOP_ARG = "stop"; //$NON-NLS-1$

	private RadioButtonsArea radioButtons;
	private Button defButton;
	private Text cmdText;

	private boolean stopOnError;
	private boolean useDefaultBuildCmd;
	private boolean defaultSelectionMade = false;
	private String buildCmd;
	IValidation page;

	public SettingsBlock(IValidation valid) {
		this(valid, null);
	}

	public SettingsBlock(IValidation valid, IProject project) {
		if (project != null) {
			try {
				CProjectNature nature =
					(CProjectNature) project.getNature(CProjectNature.C_NATURE_ID);
				if (nature != null) {
					stopOnError = nature.isStopOnError();
					useDefaultBuildCmd = nature.isDefaultBuildCmd();
					if (!useDefaultBuildCmd) {
						buildCmd = nature.getBuildCommand().toOSString();
						buildCmd += " " + nature.getFullBuildArguments();
					}
				}
			}
			catch (CoreException e) {
			}
		}
		else {
			// FIXME: Should use the default settings
			stopOnError = false;
			useDefaultBuildCmd = true;
		}
		page = valid;
	}

	public String getLabel() {
		return CPlugin.getResourceString(LABEL);
	}

	public Image getImage() {
		return null;
	}

	public Composite getControl(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 1);

		String[][] radios =
			new String[][] { { CPlugin.getResourceString(STOP_ERROR), STOP_ARG }, {
				CPlugin.getResourceString(KEEP_GOING), KEEP_ARG }
		};
		radioButtons =
			new RadioButtonsArea(composite, CPlugin.getResourceString(LABEL), 1, radios);
		
		Group mgroup =
			ControlFactory.createGroup(
				composite,
				CPlugin.getResourceString(MAKE_OPTION),
				1);
		defButton =
			ControlFactory.createCheckBox(
				mgroup,
				CPlugin.getResourceString(MAKE_USE_DEFAULT));
		defButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (defButton.getSelection() == true) {
					cmdText.setEnabled(false);
					radioButtons.setEnabled(true);
					if (null != page)
						page.setComplete(isValid());
	
				}
				else {
					cmdText.setEnabled(true);
					radioButtons.setEnabled(false);
					if (null != page)
						page.setComplete(isValid());
				}
			}
		});
		Composite cmdComp = new Composite(mgroup, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		cmdComp.setLayout(layout);
		cmdComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label label =
			ControlFactory.createLabel(cmdComp, CPlugin.getResourceString(MAKE_BUILD_CMD));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		cmdText = ControlFactory.createTextField(cmdComp, SWT.SINGLE | SWT.BORDER);
		((GridData) (cmdText.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (cmdText.getLayoutData())).grabExcessHorizontalSpace = true;
		cmdText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				page.setComplete(isValid());
			}
		});
		if ( buildCmd != null ) 
			cmdText.setText(buildCmd);
		if (useDefaultBuildCmd) {
			cmdText.setEnabled(false);
		} else { 
			radioButtons.setEnabled(false);									
		}
		return composite;
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
	
	public void setVisible(boolean visible) {
		if ( !defaultSelectionMade ) {
			if (stopOnError)
				radioButtons.setSelectValue(STOP_ARG);
			else
				radioButtons.setSelectValue(KEEP_ARG);
			defButton.setSelection(useDefaultBuildCmd);
			defaultSelectionMade = true;
		}
	}

	public void doRun(IProject project, IProgressMonitor monitor) {
		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask("Settings", 1);
			CProjectNature nature =
				(CProjectNature) project.getNature(CProjectNature.C_NATURE_ID);
			if (nature != null) {
				nature.setStopOnError(isStopOnError());
				nature.setBuildCommandOverride(useDefaultBuildCmd());
				if (!useDefaultBuildCmd()) {
					String bldLine = getBuildLine();
					int start = bldLine.indexOf(' ');
					IPath path;
					if ( start == -1 ) {
						path = new Path(bldLine);
					} else {
						path = new Path(bldLine.substring(0, start));
					}
					nature.setBuildCommand(path, new SubProgressMonitor(monitor, 50));
					String args = "";
					if ( start != -1 ) {
						args = bldLine.substring(start + 1);
					}
					nature.setFullBuildArguments(args, new SubProgressMonitor(monitor, 50));
				}
			}
		}
		catch (CoreException e) {
		}
	}

	private boolean isStopOnError() {
		if ( defaultSelectionMade ) {
			if (radioButtons != null)
				return radioButtons.getSelectedValue().equals(STOP_ARG);
		}
		return stopOnError;
	}

	private boolean useDefaultBuildCmd() {
		if ( defaultSelectionMade ) {
			if (defButton != null)
				return defButton.getSelection();
		}
		return useDefaultBuildCmd;
	}

	private String getBuildLine() {
		if (cmdText != null) {
			String cmd = cmdText.getText();
			if (cmd != null)
				return cmd.trim();
		}
		return null;
	}
}
