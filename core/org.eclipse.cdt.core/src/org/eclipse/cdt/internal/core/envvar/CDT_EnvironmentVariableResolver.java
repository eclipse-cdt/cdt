package org.eclipse.cdt.internal.core.envvar;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

public class CDT_EnvironmentVariableResolver implements IDynamicVariableResolver {

	@Override
	public String resolveValue(IDynamicVariable variable, String varName) throws CoreException {
		ICConfigurationDescription confDesc = getConfigurationDescription();
		return getBuildEnvironmentVariable(confDesc, varName, new String(), true);
	}

	/**
	 * Find the active configuration of the project selected in the project manager
	 * 
	 * @return The configuration description
	 * @throws CoreException
	 *             when the project is not found
	 */
	private static ICConfigurationDescription getConfigurationDescription() throws CoreException {
		IResource resource = null;
		resource = getSelectedResource();
		if (resource != null && resource.exists()) {
			IProject project = resource.getProject();
			if (project != null && project.exists() && project.isOpen()) {
				CCorePlugin cCorePlugin = CCorePlugin.getDefault();
				ICProjectDescription prjCDesc = cCorePlugin.getProjectDescription(project);
				return prjCDesc.getActiveConfiguration();
			}
		}
		throw new CoreException(null);
	}

	static private String getBuildEnvironmentVariable(ICConfigurationDescription configurationDescription,
			String envName, String defaultvalue, boolean expanded) {

		IEnvironmentVariableManager envManager = CCorePlugin.getDefault().getBuildEnvironmentManager();
		try {
			return envManager.getVariable(envName, configurationDescription, expanded).getValue();
		} catch (Exception e) {// ignore all errors and return the default value
		}
		return defaultvalue;
	}

	/**
	 * Returns the selected resource. Uses the ${selected_resource_path} variable to
	 * determine the selected resource. This variable is provided by the debug.ui
	 * plug-in. Selected resource resolution is only available when the debug.ui
	 * plug-in is present.
	 *
	 * @param variable
	 *            variable referencing a resource
	 * @return selected resource
	 * @throws CoreException
	 *             if there is no selection
	 */
	private static IResource getSelectedResource() throws CoreException {
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		try {
			String pathString = manager.performStringSubstitution("${selected_resource_path}"); //$NON-NLS-1$
			return ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(pathString));
		} catch (CoreException coreException) {
			Status iStatus = new Status(Status.ERROR, "org.eclipse.cdt",
					"Please select a resource that belongs to a project");
			throw new CoreException(iStatus);
		}
	}

}
