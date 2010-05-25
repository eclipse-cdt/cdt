/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ken Ryall (Nokia)
 *******************************************************************************/
package org.eclipse.cdt.launch;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

/**
 * AbstractCLaunchDelegate2 is used by most DSF based debuggers. It replaces AbstractCLaunchDelegate
 * which is the launch delegate used by most CDI based debuggers.
 * 
 * While it is technically possible to merge the two, AbstractCLaunchDelegate has been left
 * unmodified because it is commonly used by CDT clients and contains lots of obscure code
 * created long ago to handle issues whose relevance is unclear today.
 *
 */
/**
 * @since 6.1
 *
 */
public abstract class AbstractCLaunchDelegate2 extends LaunchConfigurationDelegate {

	private boolean workspaceBuildBeforeLaunch;
	private boolean requireCProject;
	private IProject project;
	private String preLaunchBuildConfiguration;

	public AbstractCLaunchDelegate2() {
		super();
		this.requireCProject = true;
	}

	public AbstractCLaunchDelegate2(boolean requireCProject) {
		super();
		this.requireCProject = requireCProject;
	}

	/**
	 * Recursively creates a set of projects referenced by the current project
	 * 
	 * @param proj
	 *            The current project
	 * @param referencedProjSet
	 *            A set of referenced projects
	 * @throws CoreException
	 *             if an error occurs while getting referenced projects from the
	 *             current project
	 */
	private HashSet<IProject> getReferencedProjectSet(IProject proj, HashSet<IProject> referencedProjSet) throws CoreException {
		// The top project is a reference too and it must be added at the top to avoid cycles
		referencedProjSet.add(proj);

		IProject[] projects = proj.getReferencedProjects();
		for (IProject refProject : projects) {
			if (refProject.exists() && !referencedProjSet.contains(refProject)) {
				getReferencedProjectSet(refProject, referencedProjSet);
			}
		}
		return referencedProjSet;
	}
	
	/**
	 * Returns the order list of projects to build before launching.
	 *  Used in buildForLaunch() 
	 */
	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) throws CoreException {
		IProject[] orderedProjects = null;
		ArrayList<IProject> orderedProjList = null;

		ICProject cProject = verifyCProject(configuration);
		if (cProject != null) {
			HashSet<IProject> projectSet = getReferencedProjectSet(cProject.getProject(), new HashSet<IProject>());

			String[] orderedNames = ResourcesPlugin.getWorkspace().getDescription().getBuildOrder();
			if (orderedNames != null) {
				//Projects may not be in the build order but should still be built if selected
				ArrayList<IProject> unorderedProjects = new ArrayList<IProject>(projectSet.size());
				unorderedProjects.addAll(projectSet);
				orderedProjList = new ArrayList<IProject>(projectSet.size());

				for (String projectName : orderedNames) {
					for (IProject proj : unorderedProjects) {
						if (proj.getName().equals(projectName)) {
							orderedProjList.add(proj);
							unorderedProjects.remove(proj);
							break;
						}
					}
				}

				// Add any remaining projects to the end of the list
				orderedProjList.addAll(unorderedProjects);

				orderedProjects = orderedProjList.toArray(new IProject[orderedProjList.size()]);
			} else {
				// Try the project prerequisite order then
				IProject[] projects = projectSet.toArray(new IProject[projectSet.size()]);
				orderedProjects = ResourcesPlugin.getWorkspace().computeProjectOrder(projects).projects;
			}
		}
		return orderedProjects;
	}

	/* Used in finalLaunchCheck() */
	@Override
	protected IProject[] getProjectsForProblemSearch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return getBuildOrder(configuration, mode);
	}

	/**
	 * Searches for compile errors in the specified project
	 * Used in finalLaunchCheck() 
	 * @param proj
	 *            The project to search
	 * @return true if compile errors exist, otherwise false
	 */
	@Override
	protected boolean existsProblems(IProject proj) throws CoreException {
		IMarker[] markers = proj.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers.length > 0) {
			for (IMarker marker : markers) {
				Integer severity = (Integer)marker.getAttribute(IMarker.SEVERITY);
				if (severity != null) {
					return severity.intValue() >= IMarker.SEVERITY_ERROR;
				}
			}
		}
		return false;
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		IStatus status;
		if (exception != null) {
			MultiStatus multiStatus = new MultiStatus(getPluginID(), code, message, exception);
			multiStatus.add(new Status(IStatus.ERROR, getPluginID(), code, exception.getLocalizedMessage(), exception));
			status= multiStatus;
		} else {
			status= new Status(IStatus.ERROR, getPluginID(), code, message, null);
		}
		throw new CoreException(status);
	}

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Builds the current project and all of it's prerequisite projects if
	 * necessary. Respects specified build order if any exists.
	 * 
	 * @param configuration
	 *            the configuration being launched
	 * @param mode
	 *            the mode the configuration is being launched in
	 * @param monitor
	 *            progress monitor
	 * @return whether the debug platform should perform an incremental
	 *         workspace build before the launch
	 * @throws CoreException
	 *             if an exception occurs while building
	 */
	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {

		workspaceBuildBeforeLaunch = true;
		
		// check the build before launch setting and honor it
		int buildBeforeLaunchValue = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH,
				ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING);

		// we shouldn't be getting called if the workspace setting is disabled, so assume we need to
		// build unless the user explicitly disabled it in the main tab of the launch.
		if (buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_DISABLED) {
			return false;
		}

		setBuildConfiguration(configuration, project);

		return super.buildForLaunch(configuration, mode, monitor);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		ICProject cProject = CDebugUtils.getCProject(configuration);
		if (cProject != null) {
			project = cProject.getProject();
		}
		return super.preLaunchCheck(configuration, mode, monitor);
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		if (!workspaceBuildBeforeLaunch) {
			// buildForLaunch was not called which means that the workspace pref is disabled.  see if the user enabled the
			// launch specific setting in the main tab.  if so, we do call buildBeforeLaunch here.
			if (ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_ENABLED == configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH,
					ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING)) {
				
				IProgressMonitor buildMonitor = new SubProgressMonitor(monitor, 10, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				buildMonitor.beginTask(LaunchMessages.getString("AbstractCLaunchDelegate.BuildBeforeLaunch"), 10); //$NON-NLS-1$	
				buildMonitor.subTask(LaunchMessages.getString("AbstractCLaunchDelegate.PerformingBuild")); //$NON-NLS-1$
				if (buildForLaunch(configuration, mode, new SubProgressMonitor(buildMonitor, 7))) {
					buildMonitor.subTask(LaunchMessages.getString("AbstractCLaunchDelegate.PerformingIncrementalBuild")); //$NON-NLS-1$
					ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(buildMonitor, 3));				
				}
				else {
					buildMonitor.worked(3); /* No incremental build required */
				}
			}
		}
		boolean continueLaunch = super.finalLaunchCheck(configuration, mode, monitor);

		if (continueLaunch) // If no problems then restore the previous build configuration. Otherwise leave it so the user can fix the build issues.
			resetBuildConfiguration(project);
		
		return continueLaunch;
}

	protected ICProject verifyCProject(ILaunchConfiguration config) throws CoreException {
		String name = CDebugUtils.getProjectName(config);
		if (name == null && requireCProject) {
			abort(LaunchMessages.getString("AbstractCLaunchDelegate.C_Project_not_specified"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
		}
		ICProject cproject = CDebugUtils.getCProject(config);
		if (cproject == null && requireCProject) {
			IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (!proj.exists()) {
				abort(
						LaunchMessages.getFormattedString("AbstractCLaunchDelegate.Project_NAME_does_not_exist", name), null, //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
			} else if (!proj.isOpen()) {
				abort(LaunchMessages.getFormattedString("AbstractCLaunchDelegate.Project_NAME_is_closed", name), null, //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
			}
			abort(LaunchMessages.getString("AbstractCLaunchDelegate.Not_a_C_CPP_project"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
		}
		return cproject;
	}

	/**
	 * Sets up a project for building by making sure the active configuration is the one used
	 * when the launch was created.
	 * @param configuration
	 * @param buildProject
	 */
	private void setBuildConfiguration(ILaunchConfiguration configuration, IProject buildProject) {
		
		try {
			if (buildProject != null)
			{
				String buildConfigID = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, ""); //$NON-NLS-1$
				ICProjectDescription projDes = CDTPropertyManager.getProjectDescription(buildProject);
				
				if (buildConfigID.length() > 0 && projDes != null)
				{
					ICConfigurationDescription buildConfiguration = projDes.getConfigurationById(buildConfigID);
					if (buildConfiguration != null) {
						preLaunchBuildConfiguration = projDes.getActiveConfiguration().getId();
						buildConfiguration.setActive();
						CDTPropertyManager.performOk(null);
					}
				}
			}
			
		} catch (CoreException e) {}
	}

	private void resetBuildConfiguration(IProject buildProject) {
		// Restore the active configuration if it was changed for the launch
		if (preLaunchBuildConfiguration != null) {
			ICProjectDescription projDes = CDTPropertyManager.getProjectDescription(buildProject);
			
			if (preLaunchBuildConfiguration.length() > 0 && projDes != null)
			{
				ICConfigurationDescription buildConfiguration = projDes.getConfigurationById(preLaunchBuildConfiguration);
				if (buildConfiguration != null) {
					buildConfiguration.setActive();
					CDTPropertyManager.performOk(null);
				}
			}

		}
		preLaunchBuildConfiguration = null;
	}

	/**
	 * @return the ID of the plugin hosting the launch delegate. It's used to
	 *         create {@link IStatus} objects.
	 */
	abstract protected String getPluginID();
}
