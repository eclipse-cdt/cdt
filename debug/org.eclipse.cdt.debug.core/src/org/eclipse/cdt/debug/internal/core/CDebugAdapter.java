/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

public class CDebugAdapter implements ICDIDebugger {

	final ICDebugger fDebugger;
	/**
	 * @param debugger
	 */
	public CDebugAdapter(ICDebugger debugger) {
		fDebugger = debugger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICDIDebugger#createDebuggerSession(org.eclipse.debug.core.ILaunch,
	 *      org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICDISession createDebuggerSession(ILaunch launch, IBinaryExecutable exe, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		IFile[] exeFile = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(exe.getPath());
		if (exeFile.length == 0) {
			abort(InternalDebugCoreMessages.getString("CDebugAdapter.0"), null, -1); //$NON-NLS-1$
		}
		int pid = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1);
		String coreFile = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, (String)null);
		ICDISession session;
		try {
			if (pid == -1 && coreFile == null) {
				return fDebugger.createLaunchSession(config, exeFile[0]);
			} else if (pid != -1) {
				return fDebugger.createAttachSession(config, exeFile[0], pid);
			}
			return fDebugger.createCoreSession(config, exeFile[0], new Path(coreFile));
		} catch (CDIException e) {
			abort(e.getLocalizedMessage(), e, -1);
		}
		throw new IllegalStateException(); // should never happen
	}

	protected void abort(String message, Throwable exception, int code) throws CoreException {
		MultiStatus status = new MultiStatus(CDebugCorePlugin.getUniqueIdentifier(), code, message, exception);
		status.add(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), code, exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
				exception));
		throw new CoreException(status);
	}

	public static ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
				if (cProject != null && cProject.exists()) {
					return cProject;
				}
			}
		}
		return null;
	}

	public static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}

	public static String getProgramName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
	}

	public static IPath getProgramPath(ILaunchConfiguration configuration) throws CoreException {
		String path = getProgramName(configuration);
		if (path == null) {
			return null;
		}
		return new Path(path);
	}

}