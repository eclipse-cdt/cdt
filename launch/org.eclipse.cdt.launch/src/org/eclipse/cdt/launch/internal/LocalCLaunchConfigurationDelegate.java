package org.eclipse.cdt.launch.internal;

/*
 * (c) Copyright QNX Software System 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIRuntimeOptions;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Insert the type's description here.
 * @see ILaunchConfigurationDelegate
 */
public class LocalCLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	/*
	  	protected String renderDebugTarget(ICDISession session) {
			String format= "{0} at localhost {1}";
			return MessageFormat.format(format, new String[] { classToRun, String.valueOf(host) });
		}
	*/
	public String renderProcessLabel(String[] commandLine) {
		String format = "{0} ({1})";
		String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[] { commandLine[0], timestamp });
	}

	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask("Launching Local C Application", IProgressMonitor.UNKNOWN);
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		ICProject cproject = getCProject(config);
		IPath projectPath = ((IProject) cproject.getResource()).getFile(getProgramName(config)).getLocation();
		String arguments[] = getProgramArgumentsArray(config);
		ArrayList command = new ArrayList(1 + arguments.length);
		command.add(projectPath.toOSString());
		command.addAll(Arrays.asList(arguments));

		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			ICDebugConfiguration dbgCfg = null;
			ICDebugger cdebugger = null;
			try {
				dbgCfg =
					CDebugCorePlugin.getDefault().getDebugConfiguration(
						config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, ""));
				cdebugger = dbgCfg.getDebugger();
			}
			catch (CoreException e) {
				IStatus status =
					new Status(
						IStatus.ERROR,
						LaunchUIPlugin.getUniqueIdentifier(),
						ICDTLaunchConfigurationConstants.ERR_DEBUGGER_NOT_INSTALLED,
						"CDT Debubger not installed",
						e);
				IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);

				if (handler != null) {
					Object result = handler.handleStatus(status, this);
					if (result instanceof String) {
					}
				}
				throw e;
			}
			IFile exe = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(projectPath);
			ICDISession dsession = null;
			try {
				String debugMode =
					config.getAttribute(
						ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
						ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
				if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
					dsession = cdebugger.createLaunchSession(config, exe);
				}
				else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
					int pid = getProcessID();
					dsession = cdebugger.createAttachSession(config, exe, pid);
				}
				else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
					IPath corefile = getCoreFilePath((IProject)cproject.getResource());
					dsession = cdebugger.createCoreSession(config, exe, corefile);
				}
			}
			catch (CDIException e) {
				IStatus status =
					new Status(
						0,
						LaunchUIPlugin.getUniqueIdentifier(),
						ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR,
						"CDI Error",
						e);
				throw new CoreException(status);
			}
			ICDIRuntimeOptions opt = dsession.getRuntimeOptions();
			opt.setArguments(config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""));
			File wd = getWorkingDir(config);
			if (wd != null) {
				opt.setWorkingDirectory(wd.toString());
			}
			opt.setEnvironment(getEnvironmentProperty(config));
			ICDITarget dtarget = dsession.getTargets()[0];
			Process process = dtarget.getProcess();
			IProcess iprocess =
				DebugPlugin.newProcess(launch, process, renderProcessLabel((String[]) command.toArray(new String[command.size()])));
			boolean stopInMain = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
			CDebugModel.newDebugTarget(
				launch,
				dsession.getTargets()[0],
				dbgCfg.getName(),
				iprocess,
				exe.getProject(),
				true,
				false,
				stopInMain);
		}
		else {
			String[] commandArray = (String[]) command.toArray(new String[command.size()]);
			Process process = exec(commandArray, getEnvironmentArray(config), getWorkingDir(config));
			DebugPlugin.getDefault().newProcess(launch, process, renderProcessLabel(commandArray));
		}
		monitor.done();
	}

	private IPath getCoreFilePath(IProject project) {
		Shell shell = LaunchUIPlugin.getShell();
		if ( shell == null ) 
			return null;
		FileDialog dialog = new FileDialog( shell );
		dialog.setText( "Select Corefile" );
		
		String initPath = null;
		try {
			initPath = project.getPersistentProperty(new QualifiedName(LaunchUIPlugin.getUniqueIdentifier(), "SavePath"));
		}
		catch (CoreException e) {
		}
		if ( initPath == null || initPath.equals("") ) {
			initPath = project.getLocation().toString();
		}
		dialog.setFilterPath( initPath );
		String res = dialog.open();
		if ( res != null )
			return new Path( res );
		return null;		
	}


	private int getProcessID() {
		return -1;
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
			}
			else {
				p = Runtime.getRuntime().exec(cmdLine, envp, workingDirectory);
			}
		}
		catch (IOException e) {
			if (p != null) {
				p.destroy();
			}
			abort("Exception starting process", e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		catch (NoSuchMethodError e) {
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
}
