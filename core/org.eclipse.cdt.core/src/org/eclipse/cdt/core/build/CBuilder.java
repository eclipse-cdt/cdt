package org.eclipse.cdt.core.build;

import java.io.IOException;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.build.Messages;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 6.0
 */
public class CBuilder extends IncrementalProjectBuilder {

	private static final String ID = CCorePlugin.PLUGIN_ID + ".cBuilder"; //$NON-NLS-1$

	public static void setupBuilder(ICommand command) {
		command.setBuilderName(CBuilder.ID);
		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, false);
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		try {
			IProject project = getProject();

			// Set up console
			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(project);

			// Get the build configuration
			ICBuildConfiguration config = getBuildConfig().getAdapter(ICBuildConfiguration.class);
			if (config == null) {
				console.getErrorStream().write(Messages.CBuilder_NotConfiguredCorrectly);
				return null;
			}

			return config.build(kind, args, console, monitor);
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, Messages.CBuilder_ExceptionWhileBuilding, e));
		}
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		try {
			IProject project = getProject();

			// Set up console
			IConsole console = CCorePlugin.getDefault().getConsole();
			console.start(project);

			// Get the build configuration
			ICBuildConfiguration config = getBuildConfig().getAdapter(ICBuildConfiguration.class);
			if (config == null) {
				console.getErrorStream().write(Messages.CBuilder_NotConfiguredCorrectly2);
				return;
			}

			config.clean(console, monitor);
		} catch (IOException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, Messages.CBuilder_ExceptionWhileBuilding2, e));
		}
	}

}
