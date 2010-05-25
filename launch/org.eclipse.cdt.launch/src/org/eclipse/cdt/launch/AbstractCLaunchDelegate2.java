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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.ui.BuildErrPrompter;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;

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
	 * Even though we override the base behavior and only build the single
	 * project referenced in the launch configuration (and not any of the
	 * projects it references), we still want to implement this as the base will
	 * also use the list to determine what files need be saved, and there it's
	 * not much of a burden to include any referenced projects
	 * 
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#getBuildOrder(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String)
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
	 * Builds the project referenced in the launch configuration
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
		try {
			SubMonitor submon = SubMonitor.convert(monitor, "", 1); //$NON-NLS-1$

			workspaceBuildBeforeLaunch = true;

			IProject project = null;
			ICProject cProject = CDebugUtils.getCProject(configuration);
			if (cProject != null) {
				project = cProject.getProject();
			}
			
			if (project == null) {
				return false;
			}
			
			// check the build before launch setting and honor it
			int buildBeforeLaunchValue = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH,
					ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING);
	
			// we shouldn't be getting called if the workspace setting is disabled, so assume we need to
			// build unless the user explicitly disabled it in the main tab of the launch.
			if (buildBeforeLaunchValue == ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_DISABLED) {
				return false;
			}
	
			// The attribute value will be "" if 'Use Active' is selected
			String buildConfigID = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, ""); //$NON-NLS-1$
			if (buildConfigID.length() == 0) {
				buildConfigID = null;
			}
	
			buildProject(project, buildConfigID, submon.newChild(1));
			return false;
		}
		finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * This is an specialization of the platform method
	 * LaunchConfigurationDelegate#buildProjects(IProject[], IProgressMonitor).
	 * It builds only one project and it builds a particular CDT build
	 * configuration of it. It was added to address bug 309126 and 312709 
	 * 
	 * @param project
	 *            the project to build
	 * @param buildConfigID
	 *            the specific build configuration to build, or null to build
	 *            the active one
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	protected void buildProject(final IProject project, final String buildConfigID, IProgressMonitor monitor) throws CoreException {

		// Some day, this will hopefully be a simple pass-thru to a cdt.core
		// utility. See bug 313927
		
		IWorkspaceRunnable build = new IWorkspaceRunnable(){
			public void run(IProgressMonitor pm) throws CoreException {
				final int TOTAL_TICKS = 1000;
				SubMonitor localmonitor = SubMonitor.convert(pm, "", TOTAL_TICKS); //$NON-NLS-1$

				try {
					// Number of times we'll end up calling IProject.build()
					final int buildCount = (buildConfigID == null) ? 1 : project.getDescription().getBuildSpec().length;
					final int subtaskTicks = TOTAL_TICKS / buildCount;
					
					if (buildConfigID != null) {
						// Build a specific configuration
						
						// To pass args, we have to specify the builder name.
						// There can be multiple so this can require multiple
						// builds. Note that this happens under the covers in
						// the 'else' (args-less) case below
						Map<String,String> cfgIdArgs = AbstractCLaunchDelegate2.cfgIdsToMap(new String[] {buildConfigID}, new HashMap<String,String>());
						cfgIdArgs.put(CONTENTS, CONTENTS_CONFIGURATION_IDS);						
						ICommand[] commands = project.getDescription().getBuildSpec();
						assert buildCount == commands.length;
						for (ICommand command : commands) {
							@SuppressWarnings("unchecked")
							Map<String, String> args = command.getArguments();
							if (args == null) {
								args = new HashMap<String, String>(cfgIdArgs);
							}
							else {
								args.putAll(cfgIdArgs);
							}
							
							if (localmonitor.isCanceled()) {
								throw new OperationCanceledException();
							}
							project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, command.getBuilderName(), args, localmonitor.newChild(subtaskTicks));
						}
					}
					else {
						// Build the active configuration
						project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, localmonitor.newChild(subtaskTicks));
					}
				} finally {
					if (pm != null) {
						pm.done();
					}
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(build, monitor);
	}

	/** TODO: Temporarily duplicated from BuilderFactory. Remove when 313927 is addressed */
	static final String CONFIGURATION_IDS = "org.eclipse.cdt.make.core.configurationIds"; //$NON-NLS-1$
	
	/** TODO: Temporarily duplicated from BuilderFactory. Remove when 313927 is addressed */
	static final String CONTENTS = "org.eclipse.cdt.make.core.contents"; //$NON-NLS-1$
	
	/** TODO: Temporarily duplicated from BuilderFactory. Remove when 313927 is addressed */
	static final String CONTENTS_CONFIGURATION_IDS = "org.eclipse.cdt.make.core.configurationIds"; //$NON-NLS-1$

	/** TODO: Temporarily duplicated from BuilderFactory. Remove when 313927 is addressed */
	private static Map<String, String> cfgIdsToMap(String ids[], Map<String, String> map){
		map.put(CONFIGURATION_IDS, encodeList(Arrays.asList(ids)));
		return map;
	}

	/** TODO: Temporarily duplicated from BuilderFactory. Remove when 313927 is addressed */
	private static String encodeList(List<String> values) {
		StringBuffer str = new StringBuffer();
		Iterator<String> entries = values.iterator();
		while (entries.hasNext()) {
			String entry = entries.next();
			str.append(escapeChars(entry, "|\\", '\\')); //$NON-NLS-1$
			str.append("|"); //$NON-NLS-1$
		}
		return str.toString();
	}

	/** TODO: Temporarily duplicated from BuilderFactory. Remove when 313927 is addressed */
	private static String escapeChars(String string, String escapeChars, char escapeChar) {
		StringBuffer str = new StringBuffer(string);
		for (int i = 0; i < str.length(); i++) {
			if (escapeChars.indexOf(str.charAt(i)) != -1) {
				str.insert(i, escapeChar);
				i++;
			}
		}
		return str.toString();
	}


	/**
	 * The platform has a generic prompter object that redirects to an
	 * appropriate prompter based on the status object. The value-add it
	 * provides is that it can be invoked from a non-GUI thread.
	 */
	private static final IStatus uiPromptStatus = new Status(IStatus.ERROR, "org.eclipse.debug.ui", 200, IInternalDebugCoreConstants.EMPTY_STRING, null); //$NON-NLS-1$

	/** Status object used to fish out our BuildErrPrompter */ 
	private static final IStatus promptStatusMainProj = new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), BuildErrPrompter.STATUS_CODE_ERR_IN_MAIN_PROJ, IInternalDebugCoreConstants.EMPTY_STRING, null);
	
	/** Status object used to fish out our BuildErrPrompter */
	private static final IStatus promptStatusReferencedProjs = new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), BuildErrPrompter.STATUS_CODE_ERR_IN_REFERENCED_PROJS, IInternalDebugCoreConstants.EMPTY_STRING, null);
	
	private Object[] createPrompterArgs(ILaunchConfiguration launchConfig) throws CoreException {
		
		IProject project = CDebugUtils.getCProject(launchConfig).getProject();
		
		Object[] args = new Object[3];
		
		// The launch configuration
		args[0] = launchConfig;
		
		// The name of the project
		args[1] = project.getName();
		
		// The name of the build configuration. Empty string if the
		// setting is "Active" or the selected configuration is the
		// active one, otherwise the name of the configuration.
		args[2] = ""; //$NON-NLS-1$
		String buildConfigId = launchConfig.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, ""); //$NON-NLS-1$
		if (buildConfigId.length() > 0) {
			ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(project, false);
			if (desc != null) {
				ICConfigurationDescription cfgDescActive = desc.getActiveConfiguration();
				ICConfigurationDescription cfgDesc = desc.getConfigurationById(buildConfigId);
				if (cfgDesc != cfgDescActive) {
					if (cfgDesc != null) {
						args[2] = cfgDesc.getName();
					}
					else {
						// TODO: not sure if and when this could ever happen, but just in case...
						args[2] = "???";	 //$NON-NLS-1$
					}
				}
			}
		}
		
		return args;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#finalLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		try {
			SubMonitor localMonitor = SubMonitor.convert(monitor, LaunchMessages.getString("AbstractCLaunchDelegate.BuildBeforeLaunch"), 10); //$NON-NLS-1$
	
			if (!workspaceBuildBeforeLaunch) {
				// buildForLaunch was not called which means that the workspace pref is disabled.  see if the user enabled the
				// launch specific setting in the main tab.  if so, we do call buildBeforeLaunch here.
				if (ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_ENABLED == configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_BUILD_BEFORE_LAUNCH,
						ICDTLaunchConfigurationConstants.BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING)) {
					
					localMonitor.subTask(LaunchMessages.getString("AbstractCLaunchDelegate.PerformingBuild")); //$NON-NLS-1$
					if (buildForLaunch(configuration, mode, localMonitor.newChild(7))) {
						localMonitor.subTask(LaunchMessages.getString("AbstractCLaunchDelegate.PerformingIncrementalBuild")); //$NON-NLS-1$
						ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, localMonitor.newChild(3));				
					}
				}
			}

			// We can just call our super's implementation to have it check for
			// build errors, but it is too generic. It doesn't know the concept
			// of a CDT build configuration, and the fact that we requested the
			// build of a particular one (which, btw, may not be the active one
			// for the project). We want to put up a more informative error
			// dialog if there are errors.
			boolean continueLaunch = true;
			ICProject cproject = CDebugUtils.getCProject(configuration);
			if (cproject != null) {
				IProject project = cproject.getProject();
				localMonitor.subTask(DebugCoreMessages.LaunchConfigurationDelegate_6);
				if (existsProblems(project)) {
					// There's a build error in the main project
					
					// Put up the error dialog. 
					IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(uiPromptStatus);
					if (prompter != null) {
						continueLaunch = ((Boolean) prompter.handleStatus(promptStatusMainProj, createPrompterArgs(configuration))).booleanValue();
					}
					else {
						assert false;					
					}
				}
				else {
					// No build error in the main project but see if there's one
					// in any of its referenced projects
					IProject[] projects = getBuildOrder(configuration, mode);
					for (IProject proj : projects) {
						// The array will contain the top level project.
						// Ignore it since we handled it above
						if (proj == project) {
							continue;
						}
						
						if (existsProblems(proj)) {
							// Put up the error dialog. 
							IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(uiPromptStatus);
							prompter = DebugPlugin.getDefault().getStatusHandler(uiPromptStatus);
							if (prompter != null) {
								continueLaunch = ((Boolean) prompter.handleStatus(promptStatusReferencedProjs, createPrompterArgs(configuration))).booleanValue();
							}
							else {
								assert false;					
							}

							// The error message says "one or more" and doesn't mention names.
							break;
						}
					}
				}
			}
			
			// Note that we do not call our super implementation (platform).
			// That's because it'll just re-do everything we've done here in a
			// non-customized way. However, we need to keep an eye out for any
			// future additions to the platform's logic.
			
			return continueLaunch;
		}
		finally {
			workspaceBuildBeforeLaunch = false;	// reset for future run
			if (monitor != null) {
				monitor.done();
			}
		}
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
	 * @return the ID of the plugin hosting the launch delegate. It's used to
	 *         create {@link IStatus} objects.
	 */
	abstract protected String getPluginID();
}
