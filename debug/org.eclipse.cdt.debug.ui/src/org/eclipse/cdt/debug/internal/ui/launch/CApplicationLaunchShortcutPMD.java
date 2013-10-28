/*******************************************************************************
 * Copyright (c) 2008, 2010 BlackBerry Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Raymond Qiu (BlackBerry Ltd) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.launch;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * This is the launch shortcut for postmortem debugging
 * 
 */
public class CApplicationLaunchShortcutPMD extends CApplicationLaunchShortcut {

	@Override
	protected void searchAndLaunch(final Object[] elements, String mode) {
		if (elements[0] instanceof IResource) {
			// for postmortem debugging, we always want to convert the selection
			// to the IBinary
			ICElement cElement = CoreModel.getDefault().create(
					(IResource) elements[0]);
			if (cElement != null) {
				super.searchAndLaunch(new Object[] { cElement }, mode);
				return;
			}
		}
		super.searchAndLaunch(elements, mode);
	}

	@Override
	protected ILaunchConfigurationType getCLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				ICDTLaunchConfigurationConstants.ID_LAUNCH_C_POST_MORTEM);
	}

	@Override
	protected ILaunchConfiguration findLaunchConfiguration(IBinary bin,
			String mode) {
		ILaunchConfiguration lc = super.findLaunchConfiguration(bin, mode);
		IResource resource = (IResource) ((IAdaptable) bin)
				.getAdapter(IResource.class);
		if (resource instanceof IFile) {
			try {
				ILaunchConfigurationWorkingCopy wc = lc.getWorkingCopy();
				// set the core file path
				wc.setAttribute(
						ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH,
						resource.getLocation().toOSString());
				wc.doSave();
			} catch (CoreException e) {
				CDebugUIPlugin.log(e);
			}
		}
		return lc;
	}
}
