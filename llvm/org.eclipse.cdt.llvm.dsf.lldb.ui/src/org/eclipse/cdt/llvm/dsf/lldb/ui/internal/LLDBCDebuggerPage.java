/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.ui.internal;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.ui.AbstractCDebuggerPage;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBDebugPreferenceConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A LLDB-specific debugger page that only shows the options currently supported
 * by LLDB and its integration with CDT.
 */
public class LLDBCDebuggerPage extends AbstractCDebuggerPage {

	protected Text fLLDBCommandText;

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());

		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Label lbl = new Label(composite, SWT.LEFT);
		lbl.setFont(parent.getFont());
		lbl.setText(Messages.LLDBCDebuggerPage_debugger_command);
		fLLDBCommandText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		fLLDBCommandText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		fLLDBCommandText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		Button button = createPushButton(composite, Messages.LLDBCDebuggerPage_browse, null);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleButtonSelected();
				updateLaunchConfigurationDialog();
			}

			private void handleButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(Messages.LLDBCDebuggerPage_browse_dialog_title);
				String lldbCommand = fLLDBCommandText.getText().trim();
				int lastSeparatorIndex = lldbCommand.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					String cmd = lldbCommand.substring(0, lastSeparatorIndex);
					// remove double quotes, since they interfere with
					// "setFilterPath()" below
					cmd = cmd.replaceAll("\\\"", ""); //$NON-NLS-1$//$NON-NLS-2$
					dialog.setFilterPath(cmd);
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				// path contains space(s)?
				if (res.contains(" ")) { //$NON-NLS-1$
					// surround it in double quotes
					res = '"' + res + '"';
				}
				fLLDBCommandText.setText(res);
			}
		});
		setControl(parent);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// I'm actually not sure this is needed but it seems this will be called
		// if this delegate is used when first initializing defaults (i.e. GDB
		// is not the delegate for this configuration first)
		IPreferenceStore corePreferenceStore = LLDBUIPlugin.getDefault().getCorePreferenceStore();
		configuration.setAttribute(ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
				corePreferenceStore.getString(ILLDBDebugPreferenceConstants.PREF_DEFAULT_LLDB_COMMAND));
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
				corePreferenceStore.getBoolean(ILLDBDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN));
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
				corePreferenceStore.getBoolean(ILLDBDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL));
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		IPreferenceStore preferenceStore = LLDBUIPlugin.getDefault().getCorePreferenceStore();
		String lldbCommand = getStringAttr(configuration, ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
				preferenceStore.getString(ILLDBDebugPreferenceConstants.PREF_DEFAULT_LLDB_COMMAND));
		fLLDBCommandText.setText(lldbCommand);
	}

	private static String getStringAttr(ILaunchConfiguration config, String attributeName, String defaultValue) {
		try {
			return config.getAttribute(attributeName, defaultValue);
		} catch (CoreException e) {
			return defaultValue;
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
				fLLDBCommandText.getText().trim());
	}

	@Override
	public String getName() {
		return Messages.LLDBCDebuggerPage_tab_name;
	}

}
