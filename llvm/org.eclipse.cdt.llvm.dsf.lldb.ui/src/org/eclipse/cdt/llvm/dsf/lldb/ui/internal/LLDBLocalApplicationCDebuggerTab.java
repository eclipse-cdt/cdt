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

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.ui.ICDebuggerPage;
import org.eclipse.cdt.dsf.gdb.internal.ui.launching.LocalApplicationCDebuggerTab;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBDebugPreferenceConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A LLDB-specific debugger tab that allows us to present a different debugger
 * page.
 */
@SuppressWarnings("restriction")
public class LLDBLocalApplicationCDebuggerTab extends LocalApplicationCDebuggerTab {

	private final static String LOCAL_DEBUGGER_ID = "lldb-mi";//$NON-NLS-1$

	@Override
	protected void initDebuggerTypes(String selection) {
		if (fAttachMode) {
			setInitializeDefault(selection.isEmpty());

			if (selection.isEmpty()) {
				selection = LOCAL_DEBUGGER_ID;
			}

			loadDebuggerCombo(new String[] { LOCAL_DEBUGGER_ID }, selection);
		} else {
			setDebuggerId(LOCAL_DEBUGGER_ID);
			updateComboFromSelection();
		}
	}

	/*
	 * This flag it to make sure that setDefaults gets called, even if the configuration is initially created for another delegate (GDB) then switched to LLDB.
	 */
	private final static String DEFAULTS_SET = "org.eclipse.cdt.llvm.dsf.lldb.ui.internal.LLDBLocalApplicationCDebuggerTab.DEFAULTS_SET"; //$NON-NLS-1$

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		try {
			if (config.hasAttribute(DEFAULTS_SET) == false) {
				ILaunchConfigurationWorkingCopy wc;
				wc = config.getWorkingCopy();
				setDefaults(wc);
				wc.doSave();
			}
		} catch (CoreException e) {
		}

		super.initializeFrom(config);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		super.setDefaults(config);
		IPreferenceStore corePreferenceStore = LLDBUIPlugin.getDefault().getCorePreferenceStore();
		config.setAttribute(ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
				corePreferenceStore.getString(ILLDBDebugPreferenceConstants.PREF_DEFAULT_LLDB_COMMAND));
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
				corePreferenceStore.getBoolean(ILLDBDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN));
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
				corePreferenceStore.getString(ILLDBDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL));

		config.setAttribute(DEFAULTS_SET, true);
	}

	@Override
	protected void loadDynamicDebugArea() {
		Composite dynamicTabHolder = getDynamicTabHolder();
		// Dispose of any current child widgets in the tab holder area
		Control[] children = dynamicTabHolder.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}

		String debuggerId = getIdForCurrentDebugger();
		if (debuggerId == null) {
			setDynamicTab(null);
		} else {
			if (debuggerId.equals(LOCAL_DEBUGGER_ID)) {
				setDynamicTab(new LLDBCDebuggerPage());
			} else {
				assert false : "Unknown debugger id"; //$NON-NLS-1$
			}
		}
		setDebuggerId(debuggerId);

		ICDebuggerPage debuggerPage = getDynamicTab();
		if (debuggerPage == null) {
			return;
		}
		// Ask the dynamic UI to create its Control
		debuggerPage.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
		debuggerPage.createControl(dynamicTabHolder);
		debuggerPage.getControl().setVisible(true);
		dynamicTabHolder.layout(true);
		contentsChanged();
	}
}
