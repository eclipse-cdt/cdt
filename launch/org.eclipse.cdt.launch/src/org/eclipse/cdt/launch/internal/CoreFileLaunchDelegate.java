package org.eclipse.cdt.launch.internal;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.cdi.CDIException;
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


	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor)
		throws CoreException {
		
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

		ICDebugConfiguration debugConfig = getDebugConfig(config);
		IFile exe = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(projectPath);
		ICDISession dsession = null;
	
		IPath corefile = getCoreFilePath((IProject)cproject.getResource());
		if ( corefile == null ) {
			abort("No Corefile selected", null, ICDTLaunchConfigurationConstants.ERR_NO_COREFILE);
		}
		try {
			dsession = debugConfig.getDebugger().createCoreSession(config, exe, corefile);
		}
		catch (CDIException e) {
			abort( "Failed Launching CDI Debugger", e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		}

		ICDITarget dtarget = dsession.getTargets()[0];
		Process process = dtarget.getProcess();

		IProcess iprocess =
			DebugPlugin.newProcess(launch, process, renderProcessLabel(projectPath.toOSString()));
		CDebugModel.newDebugTarget(
			launch,
			dsession.getTargets()[0],
			renderTargetLabel(debugConfig),
			iprocess,
			exe.getProject(),
			true,
			false,
			true);
	}

	private IPath getCoreFilePath(final IProject project) {
		final Shell shell = LaunchUIPlugin.getShell();
		final String res[] = { null };
		if (shell == null)
			return null;
		Display display = shell.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				FileDialog dialog = new FileDialog(shell);
				dialog.setText("Select Corefile");

				String initPath = null;
				try {
					initPath = project.getPersistentProperty(new QualifiedName(LaunchUIPlugin.getUniqueIdentifier(), "SavePath"));
				}
				catch (CoreException e) {
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
