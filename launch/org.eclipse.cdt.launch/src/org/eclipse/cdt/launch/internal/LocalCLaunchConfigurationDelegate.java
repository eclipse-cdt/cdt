package org.eclipse.cdt.launch.internal;

/*
 * (c) Copyright QNX Software System 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Insert the type's description here.
 * @see ILaunchConfigurationDelegate
 */
public class LocalCLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask("Launching Local C Application", IProgressMonitor.UNKNOWN);
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		IPath projectPath = verifyProgramFile(config);
		String arguments[] = getProgramArgumentsArray(config);
		ArrayList command = new ArrayList(1 + arguments.length);
		command.add(projectPath.toOSString());
		command.addAll(Arrays.asList(arguments));
		String[] commandArray = (String[]) command.toArray(new String[command.size()]);

		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			IProcess debuggerProcess = null;
			Process debugger;
			ICDebugConfiguration debugConfig = getDebugConfig(config);
			IFile exe = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(projectPath);
			ICDISession dsession = null;
			try {
				String debugMode =
					config.getAttribute(
						ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
				if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
					dsession = debugConfig.getDebugger().createLaunchSession(config, exe);
					ICDIRuntimeOptions opt = dsession.getRuntimeOptions();
					opt.setArguments(getProgramArgumentsArray(config));
					File wd = getWorkingDir(config);
					if (wd != null) {
						opt.setWorkingDirectory(wd.toString());
					}
					opt.setEnvironment(getEnvironmentProperty(config));
					ICDITarget dtarget = dsession.getTargets()[0];
					Process process = dtarget.getProcess();
					IProcess iprocess = DebugPlugin.newProcess(launch, process, renderProcessLabel(commandArray[0]));
					debugger =  dsession.getSessionProcess();
					if ( debugger != null ) {
						debuggerProcess = DebugPlugin.newProcess(launch, debugger, "Debug Console");
						launch.removeProcess(debuggerProcess);
					}
					boolean stopInMain = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
					CDebugModel.newDebugTarget(
						launch,
						dsession.getCurrentTarget(),
						renderTargetLabel(debugConfig),
						iprocess,
						debuggerProcess,
						exe.getProject(),
						true,
						false,
						stopInMain);

				} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
					int pid = getProcessID();
					if (pid == -1) {
						cancel("No Process ID selected", ICDTLaunchConfigurationConstants.ERR_NO_PROCESSID);
					}
					dsession = debugConfig.getDebugger().createAttachSession(config, exe, pid);
					debugger = dsession.getSessionProcess();
					if ( debugger != null ) {
						debuggerProcess = DebugPlugin.newProcess(launch, debugger, "Debug Console");
						launch.removeProcess(debuggerProcess);
					}
					CDebugModel.newAttachDebugTarget(
						launch,
						dsession.getCurrentTarget(),
						renderTargetLabel(debugConfig),
						debuggerProcess,
						exe.getProject());
				}
			} catch (CDIException e) {
				abort("Failed Launching CDI Debugger", e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
		} else {
			Process process = exec(commandArray, getEnvironmentArray(config), getWorkingDir(config));
			DebugPlugin.getDefault().newProcess(launch, process, renderProcessLabel(commandArray[0]));
		}
		monitor.done();
	}

	private int getProcessID() throws CoreException {
		final Shell shell = LaunchUIPlugin.getShell();
		final int pid[] = { -1 };
		if (shell == null) {
			abort("No Shell availible in Launch", null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		Display display = shell.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider() {
					public String getText(Object element) {
						IProcessInfo info = (IProcessInfo) element;
						return info.getPid() + " " + info.getName();
					}
				});
				dialog.setTitle("Select Process");
				dialog.setMessage("Select a Process to attach debugger to:");
				IProcessList plist = CCorePlugin.getDefault().getProcessList();
				if (plist == null) {
					MessageDialog.openError(shell, "CDT Launch Error", "Current platform does not support listing processes");
					return;
				}
				dialog.setElements(plist.getProcessList());
				if (dialog.open() == dialog.OK) {
					IProcessInfo info = (IProcessInfo) dialog.getFirstResult();
					pid[0] = info.getPid();
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
	protected Process exec(String[] cmdLine, String[] envp, File workingDirectory) throws CoreException {
		Process p = null;
		try {
			if (workingDirectory == null) {
				p = Runtime.getRuntime().exec(cmdLine, envp);
			} else {
				p = Runtime.getRuntime().exec(cmdLine, envp, workingDirectory);
			}
		} catch (IOException e) {
			if (p != null) {
				p.destroy();
			}
			abort("Error starting process", e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working directory

			IStatus status =
				new Status(
					IStatus.ERROR,
					LaunchUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED,
					"Eclipse runtime does not support working directory",
					e);
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);

			if (handler != null) {
				Object result = handler.handleStatus(status, this);
				if (result instanceof Boolean && ((Boolean) result).booleanValue()) {
					p = exec(cmdLine, envp, null);
				}
			}
		}
		return p;
	}

	protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}

}
