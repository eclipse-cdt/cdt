/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.launch.internal.ui.WorkingDirectoryBlock;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A launch configuration tab that displays and edits program arguments,
 * and working directory launch configuration attributes.
 * @since 9.3
 */
public abstract class CAbstractArgumentsTab extends CLaunchConfigurationTab {
	// Program arguments UI widgets
	protected Label fPrgmArgumentsLabel;
	protected Text fPrgmArgumentsText;
	protected Button fArgumentVariablesButton;

	/**
	 * Working Directory
	 * @noreference This field is not intended to be referenced by clients.
	 */
	protected WorkingDirectoryBlock fWorkingDirectoryBlock = new WorkingDirectoryBlock();

	@Override
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		comp.setLayout(layout);
		comp.setFont(font);

		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		setControl(comp);

		LaunchUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(),
				ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ARGUMNETS_TAB);

		createArgumentComponent(comp, 1);

		fWorkingDirectoryBlock.createControl(comp);
	}

	protected void createArgumentComponent(Composite comp, int horizontalSpan) {
		Font font = comp.getFont();
		Group group = new Group(comp, SWT.NONE);
		group.setFont(font);
		group.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = horizontalSpan;
		group.setLayoutData(gd);

		group.setText(LaunchMessages.CArgumentsTab_C_Program_Arguments);
		fPrgmArgumentsText = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		fPrgmArgumentsText.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = LaunchMessages.CArgumentsTab_C_Program_Arguments;
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 40;
		gd.widthHint = 100;
		fPrgmArgumentsText.setLayoutData(gd);
		fPrgmArgumentsText.setFont(font);
		fPrgmArgumentsText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		fArgumentVariablesButton = createVariablesButton(group, LaunchMessages.CArgumentsTab_Variables,
				fPrgmArgumentsText);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		fArgumentVariablesButton.setLayoutData(gd);
		addControlAccessibleListener(fArgumentVariablesButton, fArgumentVariablesButton.getText()); // need to strip the mnemonic from buttons
	}

	public void addControlAccessibleListener(Control control, String controlName) {
		// Strip mnemonic (&)
		String[] strs = controlName.split("&"); //$NON-NLS-1$
		StringBuilder stripped = new StringBuilder();
		for (int i = 0; i < strs.length; i++) {
			stripped.append(strs[i]);
		}
		control.getAccessible().addAccessibleListener(new ControlAccessibleListener(stripped.toString()));
	}

	private class ControlAccessibleListener extends AccessibleAdapter {
		private String controlName;

		ControlAccessibleListener(String name) {
			controlName = name;
		}

		@Override
		public void getName(AccessibleEvent e) {
			e.result = controlName;
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		return fWorkingDirectoryBlock.isValid(config);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String) null);
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fPrgmArgumentsText
					.setText(configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$
			fWorkingDirectoryBlock.initializeFrom(configuration);
		} catch (CoreException e) {
			setErrorMessage(NLS.bind(LaunchMessages.Launch_common_Exception_occurred_reading_configuration_EXCEPTION,
					e.getStatus().getMessage()));
			LaunchUIPlugin.log(e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				getAttributeValueFrom(fPrgmArgumentsText));
		fWorkingDirectoryBlock.performApply(configuration);
	}

	/**
	 * Returns the string in the text widget, or <code>null</code> if empty.
	 *
	 * @return text or <code>null</code>
	 */
	protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		// Bug #131513 - eliminate Windows \r line delimiter
		content = content.replaceAll("\r\n", "\n"); //$NON-NLS-1$//$NON-NLS-2$
		if (!content.isEmpty()) {
			return content;
		}
		return null;
	}

	@Override
	abstract public String getId();

	@Override
	public String getName() {
		return LaunchMessages.CArgumentsTab_Arguments;
	}

	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
		fWorkingDirectoryBlock.setLaunchConfigurationDialog(dialog);
	}

	@Override
	public String getErrorMessage() {
		String m = super.getErrorMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getErrorMessage();
		}
		return m;
	}

	@Override
	public String getMessage() {
		String m = super.getMessage();
		if (m == null) {
			return fWorkingDirectoryBlock.getMessage();
		}
		return m;
	}

	@Override
	public Image getImage() {
		return LaunchImages.get(LaunchImages.IMG_VIEW_ARGUMENTS_TAB);
	}

	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}
}
