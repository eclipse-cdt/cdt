/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.launch;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

public class CBinarylessDebugLaunchShortcut implements ILaunchShortcut2 {
	
	final String BINARYLESS_DEBUG_LAUNCH_NAME = "Binary-less Launch"; //$NON-NLS-1$
	
	@Override
	public void launch(IEditorPart editor, String mode) {
		DebugUITools.launch(createConfiguration(mode), mode);
	}

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			DebugUITools.launch(createConfiguration(mode), mode);
		}
	}

	/**
	 * Method createConfiguration.
	 * @param bin
	 * @return ILaunchConfiguration
	 */
	private ILaunchConfiguration createConfiguration(String mode) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getCLaunchConfigType();
			
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, BINARYLESS_DEBUG_LAUNCH_NAME);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_FLUID);
//					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
			
			// ATM, the saved launch configuration will not be seen as valid, since it contains no project and
			// no executable. We could create a new launch configuration type, that handles this gracefully. 
			// for now, we get around this by not saving the configuration and instead getting a "working copy" 
//			config = wc.doSave();
			config = wc.getWorkingCopy();
		} catch (CoreException ce) {
			CDebugUIPlugin.log(ce);
		}
		return config;
	}

	/**
	 * Method getCLaunchConfigType.
	 * @return ILaunchConfigurationType
	 */
	protected ILaunchConfigurationType getCLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		return null;
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		return null;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		return null;
	}
}

