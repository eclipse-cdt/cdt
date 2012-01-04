/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.core.model.ITargetProperties;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @since 1.0
 */
public class DsfSteppingModeTarget implements ISteppingModeTarget, ITargetProperties {

	private static final String ID_DISASSEMBLY_VIEW= "org.eclipse.cdt.dsf.debug.ui.disassembly.view"; //$NON-NLS-1$

	private final Preferences fPreferences;

	public DsfSteppingModeTarget() {
		fPreferences= new Preferences();
		fPreferences.setDefault(PREF_INSTRUCTION_STEPPING_MODE, false);
	}

	/*
	 * @see org.eclipse.cdt.debug.core.model.ISteppingModeTarget#enableInstructionStepping(boolean)
	 */
	@Override
	public void enableInstructionStepping(boolean enabled) {
		fPreferences.setValue(PREF_INSTRUCTION_STEPPING_MODE, enabled);
		if (enabled) {
			try {
				final IWorkbenchWindow activeWorkbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
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
		return fPreferences.getBoolean(PREF_INSTRUCTION_STEPPING_MODE);
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
		fPreferences.addPropertyChangeListener(listener);
	}

	/*
	 * @see org.eclipse.cdt.debug.core.model.ITargetProperties#removePropertyChangeListener(org.eclipse.core.runtime.Preferences.IPropertyChangeListener)
	 */
	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fPreferences.removePropertyChangeListener(listener);
	}

}
