package org.eclipse.cdt.launch.internal;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Insert the type's description here.
 * @see ILaunchConfigurationDelegate
 */
public class CoreFileLaunchDelegate extends AbstractCLaunchDelegate {

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

		ICDebugConfiguration debugConfig = getDebugConfig(config);
		IFile exe = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(projectPath);
		ICDISession dsession = null;

		ICProject cproject = getCProject(config);

		IPath corefile = getCoreFilePath((IProject) cproject.getResource());
		if (corefile == null) {
			cancel("No Corefile selected", ICDTLaunchConfigurationConstants.ERR_NO_COREFILE);
		}
		Process debugger = null;
		IProcess debuggerProcess = null;
		try {
			dsession = debugConfig.getDebugger().createCoreSession(config, exe, corefile);
			debugger = dsession.getSessionProcess();
		} catch (CDIException e) {
			abort("Failed Launching CDI Debugger", e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		if ( debugger != null ) {
			debuggerProcess = DebugPlugin.newProcess(launch, debugger, "Debug Console");
			launch.removeProcess(debuggerProcess);
		}
		CDebugModel.newCoreFileDebugTarget(
			launch,
			dsession.getCurrentTarget(),
			renderTargetLabel(debugConfig),
			debuggerProcess,
			exe);
	}

	protected IPath getCoreFilePath(final IProject project) throws CoreException {
		final Shell shell = LaunchUIPlugin.getShell();
		final String res[] = { null };
		if (shell == null) {
			abort("No Shell availible in Launch", null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}
		Display display = shell.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				FileDialog dialog = new FileDialog(shell);
				dialog.setText("Select Corefile");

				String initPath = null;
				try {
					initPath = project.getPersistentProperty(new QualifiedName(LaunchUIPlugin.getUniqueIdentifier(), "SavePath"));
				} catch (CoreException e) {
				}
				if (initPath == null || initPath.equals("")) {
					initPath = project.getLocation().toString();
				}
				dialog.setFilterPath(initPath);
				res[0] = dialog.open();
			}
		});
		if (res[0] != null) {
			return new Path(res[0]);
		}
		return null;
	}

	public String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}
}
