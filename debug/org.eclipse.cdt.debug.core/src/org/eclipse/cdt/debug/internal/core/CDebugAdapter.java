/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core;

import java.util.Date;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
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
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.MessageFormat;

public class CDebugAdapter implements ICDIDebugger {
	final ICDebugger fDebugger;

	/**
	 * @param debugger
	 */
	public CDebugAdapter(ICDebugger debugger) {
		fDebugger = debugger;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICDIDebugger#createDebuggerSession(org.eclipse.debug.core.ILaunch,
	 *      org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public ICDISession createDebuggerSession(ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) throws CoreException {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		if (exe == null) {
			abort(InternalDebugCoreMessages.CDebugAdapter_Program_file_not_specified, null,
					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROGRAM);
		}
		IFile[] exeFile = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(exe.getPath());
		if (exeFile.length == 0) {
			abort(InternalDebugCoreMessages.CDebugAdapter_0, null, -1);
		}
		int pid = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1);
		String coreFile = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, (String)null);
		ICDISession session;
		try {
			if (pid == -1 && coreFile == null) {
				session = fDebugger.createLaunchSession(config, exeFile[0]);
			} else if (pid != -1) {
				session = fDebugger.createAttachSession(config, exeFile[0], pid);
			} else {
				session = fDebugger.createCoreSession(config, exeFile[0], new Path(coreFile));
			}
			Process debugger = session.getSessionProcess();
			if (debugger != null) {
				IProcess debuggerProcess = DebugPlugin.newProcess(launch, debugger, renderDebuggerProcessLabel());
				launch.addProcess(debuggerProcess);
			}
			return session;
		} catch (CDIException e) {
			abort(e.getLocalizedMessage(), e, -1);
		}
		throw new IllegalStateException(); // should never happen
	}

	protected String renderDebuggerProcessLabel() {
		String format = "{0} ({1})"; //$NON-NLS-1$
		String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		String message = InternalDebugCoreMessages.CDebugAdapter_1;
		return MessageFormat.format(format, message, timestamp);
	}

	protected void abort(String message, Throwable exception, int code) throws CoreException {
		MultiStatus status = new MultiStatus(CDebugCorePlugin.getUniqueIdentifier(), code, message, exception);
		status.add(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), code,
				exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
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
        String programName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String) null);
        if (programName != null) {
        	programName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(programName);
        }
        return programName;
	}

	public static IPath getProgramPath(ILaunchConfiguration configuration) throws CoreException {
		String path = getProgramName(configuration);
		if (path == null) {
			return null;
		}
		return new Path(path);
	}
}
