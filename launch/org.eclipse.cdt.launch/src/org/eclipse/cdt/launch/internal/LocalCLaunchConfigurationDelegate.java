/**********************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchImages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

public class LocalCLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.Launching_Local_C_Application"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		IFile exeFile = getProgramFile(config);
		String arguments[] = getProgramArgumentsArray(config);
		ArrayList command = new ArrayList(1 + arguments.length);
		command.add(exeFile.getLocation().toOSString());
		command.addAll(Arrays.asList(arguments));
		String[] commandArray = (String[]) command.toArray(new String[command.size()]);

		// set the default source locator if required
		setSourceLocator(launch, config);

		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			IProcess debuggerProcess = null;
			Process debugger;
			ICDebugConfiguration debugConfig = getDebugConfig(config);
			ICDISession dsession = null;
			try {
				String debugMode =
					config.getAttribute(
						ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
				if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
					dsession = debugConfig.getDebugger().createLaunchSession(config, exeFile);
					ICDIRuntimeOptions opt = dsession.getRuntimeOptions();
					opt.setArguments(getProgramArgumentsArray(config));
					File wd = getWorkingDirectory(config);
					if (wd != null) {
						opt.setWorkingDirectory(wd.getAbsolutePath());
					}
					opt.setEnvironment(expandEnvironment(config));
					debugger =  dsession.getSessionProcess();
					if ( debugger != null ) {
						debuggerProcess = DebugPlugin.newProcess(launch, debugger, renderDebuggerProcessLabel());
					}
					ICDITarget[] dtargets = dsession.getTargets();
					for (int i = 0; i < dtargets.length; ++i) {
						ICDITarget dtarget = dtargets[i];
						Process process = dtarget.getProcess();
						IProcess iprocess = DebugPlugin.newProcess(launch, process, renderProcessLabel(commandArray[0]));
						boolean stopInMain = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
						CDIDebugModel.newDebugTarget(
								launch,
								dtarget,
								renderTargetLabel(debugConfig),
								iprocess,
								debuggerProcess,
								exeFile,
								true,
								false,
								stopInMain);
					}

				} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
					int pid = getProcessID();
					if (pid == -1) {
						cancel(LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.No_Process_ID_selected"), ICDTLaunchConfigurationConstants.ERR_NO_PROCESSID); //$NON-NLS-1$
					}
					dsession = debugConfig.getDebugger().createAttachSession(config, exeFile, pid);
					debugger = dsession.getSessionProcess();
					if ( debugger != null ) {
						debuggerProcess = DebugPlugin.newProcess(launch, debugger, renderDebuggerProcessLabel());
						launch.removeProcess(debuggerProcess);
					}
					ICDITarget[] dTargets = dsession.getTargets();
					for (int i = 0; i < dTargets.length; ++i) {
						CDIDebugModel.newAttachDebugTarget(
								launch,
								dTargets[i],
								renderTargetLabel(debugConfig),
								debuggerProcess,
								exeFile);
					}
				}
			} catch (CDIException e) {
				if (dsession != null) {
					try {
						dsession.terminate();
					} catch (CDIException ex) {
						// ignore
					}
				}
				abort(LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.Failed_Launching_CDI_Debugger"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
			}
		} else {
			File wd = getWorkingDirectory(config);
			if (wd == null) {
				wd = new File(System.getProperty("user.home", ".")); //NON-NLS-1;  //$NON-NLS-1$//$NON-NLS-2$
			}
			Process process = exec(commandArray, getEnvironmentProperty(config), wd);
			DebugPlugin.newProcess(launch, process, renderProcessLabel(commandArray[0]));
		}
		
		monitor.done();
		
	}

	private int getProcessID() throws CoreException {
		final Shell shell = LaunchUIPlugin.getShell();
		final int pid[] = { -1 };
		if (shell == null) {
			abort(LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.No_Shell_available_in_Launch"), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		}
		Display display = shell.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				ILabelProvider provider = new LabelProvider() {
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
					 */
					public String getText(Object element) {
						IProcessInfo info = (IProcessInfo)element;
						IPath path = new Path(info.getName());
						return path.lastSegment() + " - " + info.getPid(); //$NON-NLS-1$
					}
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
					 */
					public Image getImage(Object element) {
						return LaunchImages.get(LaunchImages.IMG_OBJS_EXEC);
					}
				};
				ILabelProvider qprovider = new LabelProvider() {
					public String getText(Object element) {
						IProcessInfo info = (IProcessInfo) element;
						return info.getName();
					}
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
					 */
					public Image getImage(Object element) {
						return LaunchImages.get(LaunchImages.IMG_OBJS_EXEC);
					}
				};
				TwoPaneElementSelector dialog = new TwoPaneElementSelector(shell, provider, qprovider);
				dialog.setTitle(LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.Select_Process")); //$NON-NLS-1$
				dialog.setMessage(LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.Select_Process_to_attach_debugger_to")); //$NON-NLS-1$
				IProcessList plist = null;
				try {
					plist = CCorePlugin.getDefault().getProcessList();
				} catch (CoreException e) {
					LaunchUIPlugin.errorDialog(LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.CDT_Launch_Error"), e.getStatus()); //$NON-NLS-1$
				}
				if (plist == null) {
					MessageDialog.openError(shell, LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.CDT_Launch_Error"), LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.Platform_cannot_list_processes")); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
				dialog.setElements(plist.getProcessList());
				if (dialog.open() == Window.OK) {
					IProcessInfo info = (IProcessInfo) dialog.getFirstResult();
					if ( info != null ) {
						pid[0] = info.getPid();
					}
				}
			}
		});
		return pid[0];
	}

	/**
	 * Performs a runtime exec on the given command line in the context
	 * of the specified working directory, and returns
	 * the resulting process. If the current runtime does not support the
	 * specification of a working directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if the
	 * exec should be re-executed without specifying a working directory.
	 * 
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *  cancelled
	 * @see Runtime
	 */
	protected Process exec(String[] cmdLine, Properties environ, File workingDirectory) throws CoreException {
		Process p = null;
		Properties props = getDefaultEnvironment();
		props.putAll(expandEnvironment(environ));
		String[] envp = null;
		ArrayList envList = new ArrayList();
		Enumeration names = props.propertyNames();
		if (names != null) {
			while (names.hasMoreElements()) {
				String key = (String) names.nextElement();
				envList.add(key + "=" + props.getProperty(key)); //$NON-NLS-1$
			}
			envp = (String[]) envList.toArray(new String[envList.size()]);
		}
		try {

			if (workingDirectory == null) {
				p = ProcessFactory.getFactory().exec(cmdLine, envp);
			} else {
				p = ProcessFactory.getFactory().exec(cmdLine, envp, workingDirectory);
			}
		} catch (IOException e) {
			if (p != null) {
				p.destroy();
			}
			abort(LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.Error_starting_process"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); //$NON-NLS-1$
		} catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working directory

			IStatus status =
				new Status(
					IStatus.ERROR,
					LaunchUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED,
					LaunchUIPlugin.getResourceString("LocalCLaunchConfigurationDelegate.Does_not_support_working_dir"), //$NON-NLS-1$
					e);
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);

			if (handler != null) {
				Object result = handler.handleStatus(status, this);
				if (result instanceof Boolean && ((Boolean) result).booleanValue()) {
					p = exec(cmdLine, environ, null);
				}
			}
		}
		return p;
	}

	protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}

}
