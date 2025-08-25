/*******************************************************************************
 * Copyright (c) 2000, 2025 QNX Software Systems and others.
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
 *     Intel Corporation - Update for Core Build (#1222)
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.util.Observable;

import org.eclipse.cdt.debug.internal.ui.dialogfields.ComboDialogField;
import org.eclipse.cdt.debug.internal.ui.dialogfields.StringDialogField;
import org.eclipse.cdt.dsf.gdb.internal.launching.CoreBuildGdbManualRemoteLaunchConfigProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public abstract class AbstractSettingsBlock extends Observable {

	protected Shell fShell;

	protected Control fControl;

	protected String fErrorMessage = null;

	public AbstractSettingsBlock() {
		super();
	}

	public abstract void createBlock(Composite parent);

	protected Shell getShell() {
		return fShell;
	}

	public void dispose() {
		deleteObservers();
	}

	public abstract void initializeFrom(ILaunchConfiguration configuration);

	public abstract void setDefaults(ILaunchConfigurationWorkingCopy configuration);

	public abstract void performApply(ILaunchConfigurationWorkingCopy configuration);

	/**
	 * Core Build launch configurations can only be launched by the launch bar.
	 * The launch bar target settings have precedence over the launch configuration.
	 * Disable the connection settings so that they cannot be changed.
	 *
	 * @param configuration The launch configuration.
	 * @param field The combo dialog field to initialize.
	 * @param comp The composite to which the text control belongs.
	 */
	@SuppressWarnings("restriction")
	public void initializeField(ILaunchConfiguration configuration, StringDialogField field, Composite comp) {
		if (configuration != null && field != null && comp != null) {
			try {
				if (configuration.getType().getIdentifier()
						.equals(CoreBuildGdbManualRemoteLaunchConfigProvider.TYPE_ID)) {
					field.setEnabled(false);
					// FIXME: The tool tip is not showing.
					field.getTextControl(comp).setToolTipText(LaunchUIMessages.getString("GDBServerDebuggerPage.14")); //$NON-NLS-1$
				}
			} catch (CoreException e) {
				// do nothing
			}
		}
	}

	/**
	 * Core Build launch configurations can only be launched by the launch bar.
	 * The launch bar target settings have precedence over the launch configuration.
	 * Disable the connection settings so that they cannot be changed.
	 *
	 * @param configuration The launch configuration.
	 * @param field The combo dialog field to initialize.
	 * @param comp The composite to which the combo control belongs.
	 */
	@SuppressWarnings("restriction")
	public void initializeField(ILaunchConfiguration configuration, ComboDialogField field, Composite comp) {
		if (configuration != null && field != null && comp != null) {
			try {
				if (configuration.getType().getIdentifier()
						.equals(CoreBuildGdbManualRemoteLaunchConfigProvider.TYPE_ID)) {
					field.setEnabled(false);
					field.getComboControl(comp).setToolTipText(LaunchUIMessages.getString("GDBServerDebuggerPage.14")); //$NON-NLS-1$
				}
			} catch (CoreException e) {
				// do nothing
			}
		}
	}

	public Control getControl() {
		return fControl;
	}

	protected void setControl(Control control) {
		fControl = control;
	}

	public boolean isValid(ILaunchConfiguration configuration) {
		updateErrorMessage();
		return (getErrorMessage() == null);
	}

	protected abstract void updateErrorMessage();

	public String getErrorMessage() {
		return fErrorMessage;
	}

	protected void setErrorMessage(String string) {
		fErrorMessage = string;
	}
}
