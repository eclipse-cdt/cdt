/*******************************************************************************
 * Copyright (c) 2012 NVIDIA Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Ostroukhov (NVIDIA Corporation) - initial API and implementation (bug 378884)
 ********************************************************************************/
package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.ui.CDebugUIMessages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * This handler start attach session without requiring the user to go through debug configuration UI.
 */
public final class AttachCommandHandler extends AbstractHandler implements IHandler {

    public Object execute(final ExecutionEvent event) throws ExecutionException {
        try {
            final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            final ILaunchConfigurationType type = launchManager
                    .getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_ATTACH);
            if (type != null) {
                final ILaunchConfigurationWorkingCopy lc = type.newInstance(null,
                        launchManager.generateLaunchConfigurationName("attach_launch")); //$NON-NLS-1$
                lc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
                        ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH);
                DebugUITools.launch(lc, ILaunchManager.DEBUG_MODE);

            }
        } catch (final CoreException e) {
            StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID, CDebugUIMessages.getString("AttachCommandHandler.error_cant_start"), e)); //$NON-NLS-1$
        }
        return null;
    }

}
