/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.core.model.ITargetProperties;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 *
 * @since 1.0
 */
public class DsfSteppingModeTarget implements ISteppingModeTarget, ITargetProperties {

	private static final String ID_DISASSEMBLY_VIEW = "org.eclipse.cdt.dsf.debug.ui.disassembly.view"; //$NON-NLS-1$

	private IEclipsePreferences fPreferences = InstanceScope.INSTANCE.getNode(DsfUIPlugin.PLUGIN_ID); //$NON-NLS-1$

	public DsfSteppingModeTarget() {
	}

	/*
	 * @see org.eclipse.cdt.debug.core.model.ISteppingModeTarget#enableInstructionStepping(boolean)
	 */
	@Override
	public void enableInstructionStepping(boolean enabled) {
		fPreferences.putBoolean(ITargetProperties.PREF_INSTRUCTION_STEPPING_MODE, enabled);
		if (enabled) {
			try {
				final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow != null && activeWorkbenchWindow.getActivePage() != null) {
					activeWorkbenchWindow.getActivePage().showView(ID_DISASSEMBLY_VIEW);
				}
			} catch (PartInitException exc) {
				DsfUIPlugin.log(exc);
			}
		}
	}

	/*
	 * @see org.eclipse.cdt.debug.core.model.ISteppingModeTarget#isInstructionSteppingEnabled()
	 */
	@Override
	public boolean isInstructionSteppingEnabled() {
		return fPreferences.getBoolean(ITargetProperties.PREF_INSTRUCTION_STEPPING_MODE, false);
	}

	/*
	 * @see org.eclipse.cdt.debug.core.model.ISteppingModeTarget#supportsInstructionStepping()
	 */
	@Override
	public boolean supportsInstructionStepping() {
		return true;
	}

	/*
	 * @see org.eclipse.cdt.debug.core.model.ITargetProperties#addPropertyChangeListener(org.eclipse.core.runtime.Preferences.IPropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		// Left unimplemented because of backward compatibility.
	}

	/*
	 * @see org.eclipse.cdt.debug.core.model.ITargetProperties#removePropertyChangeListener(org.eclipse.core.runtime.Preferences.IPropertyChangeListener)
	 */
	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		// Left unimplemented because of backward compatibility.
	}

}
