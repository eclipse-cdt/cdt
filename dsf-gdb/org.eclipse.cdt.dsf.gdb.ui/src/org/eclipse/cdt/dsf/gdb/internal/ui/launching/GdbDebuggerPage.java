/*******************************************************************************
 * Copyright (c) 2008, 2017 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson             - Modified for DSF
 *     Sergey Prigogin (Google)
 *     Marc Khouzam (Ericsson) - Support for fast tracepoints (Bug 346320)
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.ui.AbstractCDebuggerPage;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * The dynamic tab for gdb-based debugger implementations.
 */
public class GdbDebuggerPage extends AbstractCDebuggerPage implements Observer {
	protected TabFolder fTabFolder;
	protected Text fGDBCommandText;
	protected Text fGDBInitText;
	protected Button fNonStopCheckBox;
	
	protected Button fReverseCheckBox;
	protected Combo fReverseDebugMode;
	protected static final String HW_REVERSE_MODE = LaunchUIMessages.getString("GDBDebuggerPage.reverse_Debuggingmodehard"); //$NON-NLS-1$
	protected static final String SW_REVERSE_MODE = LaunchUIMessages.getString("GDBDebuggerPage.reverse_Debuggingmodesoft"); //$NON-NLS-1$
	
	protected Button fUpdateThreadlistOnSuspend;
	protected Button fDebugOnFork;
	/**
	 * Checkbox for using GDB's new-console -- only displayed on Windows. Will be null if unsupported.
	 */
	private Button fExternalConsole;
	
	/**
	 * A combo box to let the user choose if fast tracepoints should be used or not.
	 */
	protected Combo fTracepointModeCombo;
	protected static final String TP_FAST_ONLY = LaunchUIMessages.getString("GDBDebuggerPage.tracepoint_mode_fast"); //$NON-NLS-1$
	protected static final String TP_NORMAL_ONLY = LaunchUIMessages.getString("GDBDebuggerPage.tracepoint_mode_normal"); //$NON-NLS-1$
	protected static final String TP_AUTOMATIC = LaunchUIMessages.getString("GDBDebuggerPage.tracepoint_mode_auto"); //$NON-NLS-1$

	private IMILaunchConfigurationComponent fSolibBlock;
	private boolean fIsInitializing = false;

    @Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTabFolder = new TabFolder(comp, SWT.NONE);
		fTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
		createTabs(fTabFolder);
		fTabFolder.setSelection(0);
		setControl(parent);
	}

    @Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IPreferenceStore preferenceStore = GdbUIPlugin.getDefault().getPreferenceStore();
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
				preferenceStore.getString(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND));
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT,
				preferenceStore.getString(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_INIT));
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
				preferenceStore.getBoolean(IGdbDebugPreferenceConstants.PREF_DEFAULT_NON_STOP));
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
				IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE_MODE,
				IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_DEFAULT);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_DEBUG_ON_FORK,
				IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_ON_FORK_DEFAULT);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_EXTERNAL_CONSOLE,
				preferenceStore.getBoolean(IGdbDebugPreferenceConstants.PREF_EXTERNAL_CONSOLE));
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_TRACEPOINT_MODE,
				IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT);
		
		if (fSolibBlock != null)
			fSolibBlock.setDefaults(configuration);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid = fGDBCommandText.getText().length() != 0;
		if (valid) {
			setErrorMessage(null);
			setMessage(null);
		} else {
			setErrorMessage(LaunchUIMessages.getString("GDBDebuggerPage.gdb_executable_not_specified")); //$NON-NLS-1$
			setMessage(null);
		}
		return valid;
	}

	/** utility method to cut down on clutter */
	private String getStringAttr(ILaunchConfiguration config, String attributeName, String defaultValue) {
		try {
			return config.getAttribute(attributeName, defaultValue);
		} catch (CoreException e) {
			return defaultValue;
		}
	}
	/** utility method to cut down on clutter */
	private boolean getBooleanAttr(ILaunchConfiguration config, String attributeName, boolean defaultValue) {
		try {
			return config.getAttribute(attributeName, defaultValue);
		} catch (CoreException e) {
			return defaultValue;
		}
	}

    @Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);
		IPreferenceStore preferenceStore = GdbUIPlugin.getDefault().getPreferenceStore();
		String gdbCommand = getStringAttr(configuration, IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
				preferenceStore.getString(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND));
		String gdbInit = getStringAttr(configuration, IGDBLaunchConfigurationConstants.ATTR_GDB_INIT,
				preferenceStore.getString(IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_INIT));
		boolean nonStopMode = getBooleanAttr(configuration, IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
				preferenceStore.getBoolean(IGdbDebugPreferenceConstants.PREF_DEFAULT_NON_STOP));
		boolean reverseEnabled = getBooleanAttr(configuration, IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
				IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);

		updateReverseDebugModeFromConfig(configuration);

		boolean updateThreadsOnSuspend = getBooleanAttr(configuration, IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
		boolean debugOnFork = getBooleanAttr(configuration, IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_DEBUG_ON_FORK,
				IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_ON_FORK_DEFAULT);
		boolean externalConsole = getBooleanAttr(configuration, IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_EXTERNAL_CONSOLE,
				preferenceStore.getBoolean(IGdbDebugPreferenceConstants.PREF_EXTERNAL_CONSOLE));

		if (fSolibBlock != null)
			fSolibBlock.initializeFrom(configuration);
		fGDBCommandText.setText(gdbCommand);
		fGDBInitText.setText(gdbInit);
		fNonStopCheckBox.setSelection(nonStopMode);
		fReverseCheckBox.setSelection(reverseEnabled);
		fUpdateThreadlistOnSuspend.setSelection(updateThreadsOnSuspend);
		fDebugOnFork.setSelection(debugOnFork);
		if (fExternalConsole != null) {
			fExternalConsole.setSelection(externalConsole);
		}
		
		updateTracepointModeFromConfig(configuration);
		
		setInitializing(false);
	}

	protected void updateTracepointModeFromConfig(ILaunchConfiguration config) {
		if (fTracepointModeCombo != null) {
			String tracepointMode = getStringAttr(config, IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_TRACEPOINT_MODE,
					                              IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT);

			if (tracepointMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_NORMAL_ONLY)) {
				fTracepointModeCombo.setText(TP_NORMAL_ONLY);
			} else if (tracepointMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_FAST_ONLY)) {
				fTracepointModeCombo.setText(TP_FAST_ONLY);
			} else if (tracepointMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_FAST_THEN_NORMAL)) {
				fTracepointModeCombo.setText(TP_AUTOMATIC);
			} else {
				// Comment out assertion in the short term to allow for existing launches
				// that used the old names to migrate to the new names.
				// It can be uncommented after we have released Juno.
				// Bug 375256
				//
				// assert false : "Unknown Tracepoint Mode: " + tracepointMode; //$NON-NLS-1$
			    fTracepointModeCombo.setText(TP_NORMAL_ONLY);
			}
		}
	}

	protected void updateReverseDebugModeFromConfig(ILaunchConfiguration config){
		if (fReverseDebugMode != null) {
			String debugMode = getStringAttr(config, IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE_MODE,
					IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_DEFAULT);

			if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_HARDWARE)) {
				fReverseDebugMode.setText(HW_REVERSE_MODE);
			} else {
				fReverseDebugMode.setText(SW_REVERSE_MODE);
			}
		}
	}

	protected String getSelectedTracepointMode() {
		if (fTracepointModeCombo != null) {
			int selectedIndex = fTracepointModeCombo.getSelectionIndex();
			if (fTracepointModeCombo.getItem(selectedIndex).equals(TP_NORMAL_ONLY)) {
				return IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_NORMAL_ONLY;
			} else if (fTracepointModeCombo.getItem(selectedIndex).equals(TP_FAST_ONLY)) {
				return IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_FAST_ONLY;
			} else if (fTracepointModeCombo.getItem(selectedIndex).equals(TP_AUTOMATIC)) {
				return IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_FAST_THEN_NORMAL;
			} else {
				assert false : "Unknown Tracepoint mode: " + fTracepointModeCombo.getItem(selectedIndex); //$NON-NLS-1$
			}
		}
		return IGDBLaunchConfigurationConstants.DEBUGGER_TRACEPOINT_MODE_DEFAULT;
	}

	protected String getSelectedReverseDebugMode() {
		if (fReverseDebugMode != null) {
			int selectedIndex = fReverseDebugMode.getSelectionIndex();
			if (fReverseDebugMode.getItem(selectedIndex).equals(HW_REVERSE_MODE)) {
				return IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_HARDWARE;
			} else if (fReverseDebugMode.getItem(selectedIndex).equals(SW_REVERSE_MODE)) {
				return IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_SOFTWARE;
			} else {
				assert false : "Unknown Reverse Debug mode: " + fReverseDebugMode.getItem(selectedIndex); //$NON-NLS-1$
			}
		}
		return IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_MODE_DEFAULT;
	}

    @Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
				fGDBCommandText.getText().trim());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT,
				fGDBInitText.getText().trim());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
				fNonStopCheckBox.getSelection());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
                fReverseCheckBox.getSelection());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
                fUpdateThreadlistOnSuspend.getSelection());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_DEBUG_ON_FORK,
				fDebugOnFork.getSelection());
		if (fExternalConsole != null) {
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_EXTERNAL_CONSOLE,
					fExternalConsole.getSelection());
		}
		
		if (fTracepointModeCombo != null) {
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_TRACEPOINT_MODE,
									   getSelectedTracepointMode());
		}

		if (fReverseDebugMode != null) {
			configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE_MODE,
                                       getSelectedReverseDebugMode());
		}

		if (fSolibBlock != null)
			fSolibBlock.performApply(configuration);
	}

    @Override
	public String getName() {
		return LaunchUIMessages.getString("GDBDebuggerPage.tab_name"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getShell()
	 */
	@Override
	protected Shell getShell() {
		return super.getShell();
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
    @Override
	public void update(Observable o, Object arg) {
		if (!isInitializing())
			updateLaunchConfigurationDialog();
	}

	public IMILaunchConfigurationComponent createSolibBlock(Composite parent) {
		IMILaunchConfigurationComponent block = new GDBSolibBlock( new SolibSearchPathBlock(), true, true);
		block.createControl(parent);
		return block;
	}

	public void createTabs(TabFolder tabFolder) {
		createMainTab(tabFolder);
		createSolibTab(tabFolder);
	}

	public void createMainTab(TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(LaunchUIMessages.getString("GDBDebuggerPage.main_tab_name")); //$NON-NLS-1$
		Composite comp = ControlFactory.createCompositeEx(tabFolder, 1, GridData.FILL_BOTH);
		((GridLayout) comp.getLayout()).makeColumnsEqualWidth = false;
		tabItem.setControl(comp);
		
		createGdbContent(comp);
		
		ControlFactory.createLabel(comp, LaunchUIMessages.getString("GDBDebuggerPage.cmdfile_warning"), //$NON-NLS-1$
				200, SWT.DEFAULT, SWT.WRAP);

		// TODO: Ideally, this field should be disabled if the back-end doesn't support non-stop debugging
		// TODO: Find a way to determine if non-stop is supported (i.e. find the GDB version) then grey out the check box if necessary
		fNonStopCheckBox = addCheckbox(comp, LaunchUIMessages.getString("GDBDebuggerPage.nonstop_mode")); //$NON-NLS-1$

		createReverseDebugModeCombo(comp);

		fUpdateThreadlistOnSuspend = addCheckbox(comp, LaunchUIMessages.getString("GDBDebuggerPage.update_thread_list_on_suspend")); //$NON-NLS-1$
		// This checkbox needs an explanation. Attach context help to it.
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fUpdateThreadlistOnSuspend, GdbUIPlugin.PLUGIN_ID + ".update_threadlist_button_context"); //$NON-NLS-1$

		fDebugOnFork = addCheckbox(comp, LaunchUIMessages.getString("GDBDebuggerPage.Automatically_debug_forked_processes")); //$NON-NLS-1$
    	if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
    		fExternalConsole = addCheckbox(comp, LaunchUIMessages.getString("GDBDebuggerPage.use_new_console_for_process")); //$NON-NLS-1$
    	}
		
		createTracepointModeCombo(comp);
	}

	private void createGdbContent(Composite comp) {
		// Create a sub-composite with 3 columns
		Composite subComp = ControlFactory.createCompositeEx(comp, 3, GridData.FILL_HORIZONTAL);
		((GridLayout) subComp.getLayout()).makeColumnsEqualWidth = false;

		ControlFactory.createLabel(subComp, LaunchUIMessages.getString("GDBDebuggerPage.gdb_debugger")); //$NON-NLS-1$
		fGDBCommandText = ControlFactory.createTextField(subComp, SWT.SINGLE | SWT.BORDER);
		fGDBCommandText.addModifyListener(new ModifyListener() {
            @Override
			public void modifyText(ModifyEvent evt) {
				if (!isInitializing())
					updateLaunchConfigurationDialog();
			}
		});
		Button button = createPushButton(subComp, LaunchUIMessages.getString("GDBDebuggerPage.gdb_browse"), null); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleGDBButtonSelected();
				updateLaunchConfigurationDialog();
			}

			private void handleGDBButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(LaunchUIMessages.getString("GDBDebuggerPage.gdb_browse_dlg_title")); //$NON-NLS-1$
				String gdbCommand = fGDBCommandText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					String cmd = gdbCommand.substring(0, lastSeparatorIndex);
					// remove double quotes, since they interfere with 
					// "setFilterPath()" below
					cmd = cmd.replaceAll("\\\"", "");  //$NON-NLS-1$//$NON-NLS-2$
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
				fGDBCommandText.setText(res);
			}
		});
		
		ControlFactory.createLabel(subComp, LaunchUIMessages.getString("GDBDebuggerPage.gdb_command_file")); //$NON-NLS-1$
		fGDBInitText = ControlFactory.createTextField(subComp, SWT.SINGLE | SWT.BORDER);
		fGDBInitText.addModifyListener(new ModifyListener() {
            @Override
			public void modifyText(ModifyEvent evt) {
				if (!isInitializing())
					updateLaunchConfigurationDialog();
			}
		});
		button = createPushButton(subComp, LaunchUIMessages.getString("GDBDebuggerPage.gdb_cmdfile_browse"), null); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleGDBInitButtonSelected();
				updateLaunchConfigurationDialog();
			}

			private void handleGDBInitButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(LaunchUIMessages.getString("GDBDebuggerPage.gdb_cmdfile_dlg_title")); //$NON-NLS-1$
				String gdbCommand = fGDBInitText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					dialog.setFilterPath(gdbCommand.substring(0, lastSeparatorIndex));
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				fGDBInitText.setText(res);
			}
		});
	}

	protected void createTracepointModeCombo(Composite parent) {
		// Create a sub-composite with 2 columns
		Composite subComp = ControlFactory.createCompositeEx(parent, 2, GridData.FILL_HORIZONTAL);
		((GridLayout) subComp.getLayout()).makeColumnsEqualWidth = false;

		// Add a combo to choose the type of tracepoint mode to use
		ControlFactory.createLabel(subComp, LaunchUIMessages.getString("GDBDebuggerPage.tracepoint_mode_label")); //$NON-NLS-1$

		fTracepointModeCombo = new Combo(subComp, SWT.READ_ONLY | SWT.DROP_DOWN);
		fTracepointModeCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		fTracepointModeCombo.add(TP_NORMAL_ONLY);
		fTracepointModeCombo.add(TP_FAST_ONLY);
		fTracepointModeCombo.add(TP_AUTOMATIC);

		fTracepointModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		fTracepointModeCombo.select(0);
	}

	protected void createReverseDebugModeCombo(Composite parent) {
		// Create a sub-composite with 2 columns with no indentation width- or height-wise
		Composite subComp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		subComp.setLayout(layout);
		subComp.setFont(parent.getFont());

		fReverseCheckBox = addCheckbox(subComp, LaunchUIMessages.getString("GDBDebuggerPage.reverse_Debugging")); //$NON-NLS-1$

		fReverseDebugMode = new Combo(subComp, SWT.READ_ONLY | SWT.DROP_DOWN );
		fReverseDebugMode.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		fReverseDebugMode.add(HW_REVERSE_MODE);
		fReverseDebugMode.add(SW_REVERSE_MODE);
		
		fReverseDebugMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		fReverseDebugMode.select(0);
	}

	public void createSolibTab(TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(LaunchUIMessages.getString("GDBDebuggerPage.shared_libraries")); //$NON-NLS-1$
		Composite comp = ControlFactory.createCompositeEx(fTabFolder, 1, GridData.FILL_BOTH);
		comp.setFont(tabFolder.getFont());
		tabItem.setControl(comp);
		fSolibBlock = createSolibBlock(comp);
		if (fSolibBlock instanceof Observable)
			((Observable) fSolibBlock).addObserver(this);
	}

	/** Used to add a checkbox to the tab. Each checkbox has its own line. */
	private Button addCheckbox(Composite parent, String label) {
		Button button = ControlFactory.createCheckBox(parent, label);
		button .addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		return button;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		if (fSolibBlock != null) {
			if (fSolibBlock instanceof Observable)
				((Observable) fSolibBlock).deleteObserver(this);
			fSolibBlock.dispose();
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// Override the default behavior
	}

	protected boolean isInitializing() {
		return fIsInitializing;
	}

	private void setInitializing(boolean isInitializing) {
		fIsInitializing = isInitializing;
	}
}
