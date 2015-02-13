/*******************************************************************************
 * Copyright (c) 2013, 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
	
	public static final String WORKSPACE_NAME = "workspace-cdtdebug"; //$NON-NLS-1$

	private Location fInstanceLoc = null;

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		Display display = PlatformUI.createDisplay();
		try {
			if (!setupWorkspaceLocation(display)) {
				return IApplication.EXIT_OK;
			}
			
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
		
	}
	
	private boolean setupWorkspaceLocation(Display display) throws IOException {
        // fetch the Location that we will be modifying
        fInstanceLoc = Platform.getInstanceLocation();

        // -data @noDefault in <applName>.ini allows us to set the workspace here.
        // If the user wants to change the location then he has to change
        // @noDefault to a specific location or remove -data @noDefault for
        // default location
        if (!fInstanceLoc.allowsDefault() && !fInstanceLoc.isSet()) {
            File workspaceRoot = new Path(System.getProperty("user.home")).toFile(); //$NON-NLS-1$

            if (!workspaceRoot.exists()) {
                MessageDialog.openError(display.getActiveShell(),
                        Messages.Application_WorkspaceCreationError,
                        MessageFormat.format(Messages.Application_WorkspaceRootNotExistError, new Object[] { workspaceRoot }));
                return false;
            }

            if (!workspaceRoot.canWrite()) {
                MessageDialog.openError(display.getActiveShell(),
                        Messages.Application_WorkspaceCreationError,
                        MessageFormat.format(Messages.Application_WorkspaceRootPermissionError, new Object[] { workspaceRoot }));
                return false;
            }

            String workspace = workspaceRoot.getAbsolutePath() + File.separator + WORKSPACE_NAME;
            // set location to workspace
            fInstanceLoc.set(new URL("file", null, workspace), false); //$NON-NLS-1$
        }

        if (!fInstanceLoc.lock()) {
            MessageDialog.openError(display.getActiveShell(),
                    Messages.Application_WorkspaceCreationError,
                    MessageFormat.format(Messages.Application_WorkspaceInUseError, new Object[] { fInstanceLoc.getURL().getPath() }));
            return false;
        }
		
        return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	@Override
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		fInstanceLoc.release();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
