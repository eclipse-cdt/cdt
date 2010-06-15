/*******************************************************************************
 * Copyright (c) 2005, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Andrew Ferguson (andrew.ferguson@arm.com) - bug 123997
 *     Ken Ryall (Nokia) - bug 178731
 *     Anton Leherbauer (Wind River Systems) - bug 224187
 *******************************************************************************/
package org.eclipse.cdt.launch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.ui.RefreshTab;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.MessageFormat;

/**
 * AbstractCLaunchDelegate is the launch delegate used by most CDI based debuggers.
 * It has been superseded by AbstractCLaunchDelegate2 which is used by most DSF based
 * debuggers. AbstractCLaunchDelegate has been left unmodified because it is commonly
 * used by CDT clients and contains lots of obscure code created long ago to handle
 * issues whose relevance is unclear today.
 *
 */
abstract public class AbstractCLaunchDelegate extends LaunchConfigurationDelegate {

    /**
	 * @since 6.0
	 */
    public class CLaunch extends Launch {

        private final AtomicBoolean fRefreshDone;
        
        public CLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
            super(launchConfiguration, mode, locator);
            fRefreshDone = new AtomicBoolean(false);
        }

        public void refresh() {
            if (fRefreshDone.compareAndSet(false, true)) {
                final ILaunchConfiguration config = getLaunchConfiguration();
                try {
                    if (RefreshTab.getRefreshScope(config) != null) {
                        Job refreshJob = new Job("Refresh"){

                            /* (non-Javadoc)
                             * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
                             */
                            @Override
                            protected IStatus run(IProgressMonitor monitor) {
                                try {
                                    RefreshTab.refreshResources(config, monitor);
                                } catch (CoreException e) {
                                    return new Status(IStatus.ERROR, LaunchUIPlugin.PLUGIN_ID, 1, e.getLocalizedMessage(), e);
                                }
                                return Status.OK_STATUS;
                            }};
                        refreshJob.setSystem(true);
                        refreshJob.schedule();
                    }
                }
                catch(CoreException e) {
                    LaunchUIPlugin.log( e.getStatus() );
                }
            }
        }
    }

    
    public AbstractCLaunchDelegate() {
		super();
	}

	/* (non-Javadoc)
     * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#getLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
     */
    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
        return new CLaunch(configuration, mode, null);
    }

    /**
	 * The project containing the programs file being launched
	 */
	private IProject project;
	/**
	 * A list of prequisite projects ordered by their build order.
	 */
	private List orderedProjects;
	private String preLaunchBuildConfiguration;

	/**
	 * Used in conjunction with build before launch settings in the main tab.
	 */
	private boolean workspaceBuildBeforeLaunch;
	
	abstract public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Returns the working directory specified by the given launch
	 * configuration, or <code>null</code> if none.
	 * 
	 * @deprecated Should use getWorkingDirectory()
	 * @param configuration
	 *            launch configuration
	 * @return the working directory specified by the given launch
	 *         configuration, or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	@Deprecated
	public File getWorkingDir(ILaunchConfiguration configuration) throws CoreException {
		return getWorkingDirectory(configuration);
	}

	/**
	 * Returns the working directory specified by the given launch
	 * configuration, or <code>null</code> if none.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the working directory specified by the given launch
	 *         configuration, or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public File getWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		return verifyWorkingDirectory(configuration);
	}

	/**
	 * Expands and returns the working directory attribute of the given launch
	 * configuration. Returns <code>null</code> if a working directory is not
	 * specified. If specified, the working is verified to point to an existing
	 * directory in the local file system.
	 * 
	 * @param configuration launch configuration
	 * @return an absolute path to a directory in the local file system, or
	 * <code>null</code> if unspecified
	 * @throws CoreException if unable to retrieve the associated launch
	 * configuration attribute, if unable to resolve any variables, or if the
	 * resolved location does not point to an existing directory in the local
	 * file system
	 */
	protected IPath getWorkingDirectoryPath(ILaunchConfiguration config) throws CoreException {
		String location = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
		if (location != null) {
			String expandedLocation = LaunchUtils.getStringVariableManager().performStringSubstitution(location);
			if (expandedLocation.length() > 0) {
				return new Path(expandedLocation);
			}
		}
		return null;
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

	protected void cancel(String message, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.OK, getPluginID(), code, message, null));
	}

	/**
	 * @return the ID of the plugin hosting the launch delegate. It's used to
	 *         create {@link IStatus} objects.
	 */
	abstract protected String getPluginID();

    /**
     * @deprecated Use {@link org.eclipse.cdt.debug.core.CDebugUtils} instead.
     */
	@Deprecated
	public static ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
	    return CDebugUtils.getCProject(configuration);
	}

    /**
     * @deprecated Use {@link org.eclipse.cdt.debug.core.CDebugUtils} instead.
     */
	@Deprecated
	public static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
        return CDebugUtils.getProjectName(configuration);
	}

    /**
     * @deprecated Use {@link org.eclipse.cdt.debug.core.CDebugUtils} instead.
     */
	@Deprecated
	public static String getProgramName(ILaunchConfiguration configuration) throws CoreException {
        return CDebugUtils.getProgramName(configuration);
	}

    /**
     * @deprecated Use {@link org.eclipse.cdt.debug.core.CDebugUtils} instead.
     */
	@Deprecated
	public static IPath getProgramPath(ILaunchConfiguration configuration) throws CoreException {
        return CDebugUtils.getProgramPath(configuration);
	}
	
	/**
	 * @param launch
	 * @param config
	 * @throws CoreException
	 * @deprecated
	 */
	@Deprecated
	protected void setSourceLocator(ILaunch launch, ILaunchConfiguration config) throws CoreException {
		setDefaultSourceLocator(launch, config);
	}

	/**
	 * Assigns a default source locator to the given launch if a source locator
	 * has not yet been assigned to it, and the associated launch configuration
	 * does not specify a source locator.
	 * 
	 * @param launch
	 *            launch object
	 * @param configuration
	 *            configuration being launched
	 * @exception CoreException
	 *                if unable to set the source locator
	 */
	protected void setDefaultSourceLocator(ILaunch launch, ILaunchConfiguration configuration) throws CoreException {
		//  set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			IPersistableSourceLocator sourceLocator;
			String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (id == null) {
				ICProject cProject = CDebugUtils.getCProject(configuration);
				if (cProject == null) {
					abort(LaunchMessages.getString("Launch.common.Project_does_not_exist"), null, //$NON-NLS-1$
							ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
				}
				sourceLocator = CDebugUIPlugin.createDefaultSourceLocator();
				sourceLocator.initializeDefaults(configuration);
			} else {
				sourceLocator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(id);
				String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
				if (memento == null) {
					sourceLocator.initializeDefaults(configuration);
				} else {
					sourceLocator.initializeFromMemento(memento);
				}
			}
			launch.setSourceLocator(sourceLocator);
		}
	}

	/**
	 * Returns the program arguments as a String.
	 * 
	 * @return the program arguments as a String
	 */
	public String getProgramArguments(ILaunchConfiguration config) throws CoreException {
		return LaunchUtils.getProgramArguments(config);
	}

	/**
	 * Returns the program arguments as an array of individual arguments.
	 * 
	 * @return the program arguments as an array of individual arguments
	 */
	public String[] getProgramArgumentsArray(ILaunchConfiguration config) throws CoreException {
		return LaunchUtils.getProgramArgumentsArray(config);
	}

	protected ICDebugConfiguration getDebugConfig(ILaunchConfiguration config) throws CoreException {
		ICDebugConfiguration dbgCfg = null;
		try {
			dbgCfg = CDebugCorePlugin.getDefault().getDebugConfiguration(
																			config.getAttribute(
																								ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID,
																								"")); //$NON-NLS-1$
		} catch (CoreException e) {
			IStatus status = new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_DEBUGGER_NOT_INSTALLED,
					LaunchMessages.getString("AbstractCLaunchDelegate.Debugger_not_installed"), //$NON-NLS-1$
					e);
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);

			if (handler != null) {
				Object result = handler.handleStatus(status, this);
				if (result instanceof String) {
					// this could return the new debugger id to use?
				}
			}
			throw e;
		}
		return dbgCfg;
	}

	protected String renderTargetLabel(ICDebugConfiguration debugConfig) {
		String format = "{0} ({1})"; //$NON-NLS-1$
		String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[]{debugConfig.getName(), timestamp});
	}

	protected String renderProcessLabel(String commandLine) {
		String format = "{0} ({1})"; //$NON-NLS-1$
		String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[]{commandLine, timestamp});
	}

	// temporary fix for #66015
	/**
	 * @deprecated
	 */
	@Deprecated
	protected String renderDebuggerProcessLabel() {
		String format = "{0} ({1})"; //$NON-NLS-1$
		String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[]{
				LaunchMessages.getString("AbstractCLaunchDelegate.Debugger_Process"), timestamp}); //$NON-NLS-1$
	}

	
	/**
	 * @param config
	 * @return
	 * @throws CoreException
	 * @deprecated Use <code>verifyProgramFile</code> instead.
	 */
	@Deprecated
	protected IFile getProgramFile(ILaunchConfiguration config) throws CoreException {
		ICProject cproject = CDebugUtils.verifyCProject(config);
		String fileName = CDebugUtils.getProgramName(config);
		if (fileName == null) {
			abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_not_specified"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROGRAM);
		}

		IFile programPath = ((IProject)cproject.getResource()).getFile(fileName);
		if (programPath == null || !programPath.exists() || !programPath.getLocation().toFile().exists()) {
			abort(
					LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_does_not_exist"), //$NON-NLS-1$
					new FileNotFoundException(
							LaunchMessages.getFormattedString(
																"AbstractCLaunchDelegate.PROGRAM_PATH_not_found", programPath.getLocation().toOSString())), //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
		return programPath;
	}

	/**
	 * @deprecated use {@link CDebugUtils#verifyCProject(ILaunchConfiguration)}
	 */
	protected ICProject verifyCProject(ILaunchConfiguration config) throws CoreException {
		return CDebugUtils.verifyCProject(config);
	}

	/**
	 * @deprecated use {@link CDebugUtils#verifyProgramPath(ILaunchConfiguration)
	 */
	protected IPath verifyProgramPath(ILaunchConfiguration config) throws CoreException {
		return CDebugUtils.verifyProgramPath(config);
	}

	protected IPath verifyProgramFile(ILaunchConfiguration config) throws CoreException {
		return getProgramFile(config).getLocation();
	}

	/**
	 * Verifies the working directory specified by the given launch
	 * configuration exists, and returns the working directory, or
	 * <code>null</code> if none is specified.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return the working directory specified by the given launch
	 *         configuration, or <code>null</code> if none
	 * @exception CoreException
	 *                if unable to retrieve the attribute
	 */
	public File verifyWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		IPath path = getWorkingDirectoryPath(configuration);
		if (path == null) {
			// default working dir is the project if this config has a project
			ICProject cp = CDebugUtils.getCProject(configuration);
			if (cp != null) {
				IProject p = cp.getProject();
				return p.getLocation().toFile();
			}
		} else {
			if (path.isAbsolute()) {
				File dir = new File(path.toOSString());
				if (dir.isDirectory()) {
					return dir;
				}
				abort(
						LaunchMessages.getString("AbstractCLaunchDelegate.Working_directory_does_not_exist"), //$NON-NLS-1$
						new FileNotFoundException(
								LaunchMessages.getFormattedString(
																	"AbstractCLaunchDelegate.WORKINGDIRECTORY_PATH_not_found", path.toOSString())), //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
			} else {
				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
				if (res instanceof IContainer && res.exists()) {
					return res.getLocation().toFile();
				}
				abort(
						LaunchMessages.getString("AbstractCLaunchDelegate.Working_directory_does_not_exist"), //$NON-NLS-1$
						new FileNotFoundException(
								LaunchMessages.getFormattedString(
																	"AbstractCLaunchDelegate.WORKINGDIRECTORY_PATH_not_found", path.toOSString())), //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
			}
		}
		return null;
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
	private void getReferencedProjectSet(IProject proj, HashSet referencedProjSet) throws CoreException {
		IProject[] projects = proj.getReferencedProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject refProject = projects[i];
			if (refProject.exists() && !referencedProjSet.contains(refProject)) {
				referencedProjSet.add(refProject);
				getReferencedProjectSet(refProject, referencedProjSet);
			}
		}

	}

	/**
	 * creates a list of project ordered by their build order from an unordered
	 * list of projects.
	 * 
	 * @param resourceCollection
	 *            The list of projects to sort.
	 * @return A new list of projects, ordered by build order.
	 */
	private List getBuildOrder(List resourceCollection) {
		String[] orderedNames = ResourcesPlugin.getWorkspace().getDescription().getBuildOrder();
		if (orderedNames != null) {
			List orderedProjs = new ArrayList(resourceCollection.size());
			//Projects may not be in the build order but should be built if
			// selected
			List unorderedProjects = new ArrayList(resourceCollection.size());
			unorderedProjects.addAll(resourceCollection);

			for (int i = 0; i < orderedNames.length; i++) {
				String projectName = orderedNames[i];
				for (int j = 0; j < resourceCollection.size(); j++) {
					IProject proj = (IProject)resourceCollection.get(j);
					if (proj.getName().equals(projectName)) {
						orderedProjs.add(proj);
						unorderedProjects.remove(proj);
						break;
					}
				}
			}
			//Add anything not specified before we return
			orderedProjs.addAll(unorderedProjects);
			return orderedProjs;
		}

		// Try the project prerequisite order then
		IProject[] projects = new IProject[resourceCollection.size()];
		projects = (IProject[])resourceCollection.toArray(projects);
		IWorkspace.ProjectOrder po = ResourcesPlugin.getWorkspace().computeProjectOrder(projects);
		ArrayList orderedProjs = new ArrayList();
		orderedProjs.addAll(Arrays.asList(po.projects));
		return orderedProjs;
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
		
		//This matches the old code, but I don't know that it is the right behavior.
		//We should be building the local project as well, not just the ordered projects
		if(orderedProjects == null) {		
			return false;
		}
		
		if(monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		int scale = 1000;
		int totalWork = (orderedProjects.size() + 1) * scale;
		
		try {
			monitor.beginTask(LaunchMessages.getString("AbstractCLaunchDelegate.building_projects"), totalWork); //$NON-NLS-1$

			for (Iterator i = orderedProjects.iterator(); i.hasNext();) {
				IProject proj = (IProject)i.next();
				monitor.subTask(LaunchMessages.getString("AbstractCLaunchDelegate.building") + proj.getName()); //$NON-NLS-1$
				proj.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(monitor, scale));
			}

			monitor.subTask(LaunchMessages.getString("AbstractCLaunchDelegate.building") + project.getName()); //$NON-NLS-1$
			setBuildConfiguration(configuration, project);
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(monitor, scale));
		} finally {
			monitor.done();
		}

		return false; 
	}

	/**
	 * Sets up a project for building by making sure the active configuration is the one used
	 * when the launch was created.
	 * @param configuration
	 * @param buildProject
	 */
	private void setBuildConfiguration(ILaunchConfiguration configuration, IProject buildProject) {
		
		try {
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
			
		} catch (CoreException e) {}
	}

	/**
	 * Searches for compile errors in the current project and any of its
	 * prerequisite projects. If any compile errors, give the user a chance to
	 * abort the launch and correct the errors.
	 * 
	 * @param configuration
	 * @param mode
	 * @param monitor
	 * @return whether the launch should proceed
	 * @throws CoreException
	 *             if an exception occurs while checking for compile errors.
	 */
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

		boolean continueLaunch = true;
		if(orderedProjects == null) {
			return continueLaunch;
		}

		if(monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		int scale = 1000;
		int totalWork = (orderedProjects.size() + 1) * scale;
		try {
			monitor.beginTask(LaunchMessages.getString("AbstractCLaunchDelegate.searching_for_errors"), totalWork); //$NON-NLS-1$
			
			boolean compileErrorsInProjs = false;
			
			//check prerequisite projects for compile errors.
			for (Iterator i = orderedProjects.iterator(); i.hasNext();) {
				IProject proj = (IProject)i.next();
				monitor.subTask(LaunchMessages.getString("AbstractCLaunchDelegate.searching_for_errors_in") + proj.getName()); //$NON-NLS-1$
				monitor.worked(scale);
				compileErrorsInProjs = existsErrors(proj);
				if (compileErrorsInProjs) {
					break;
				}
			}
			
			//check current project, if prerequite projects were ok
			if (!compileErrorsInProjs) {
				monitor.subTask(LaunchMessages.getString("AbstractCLaunchDelegate.searching_for_errors_in") + project.getName()); //$NON-NLS-1$
				monitor.worked(scale);
				compileErrorsInProjs = existsErrors(project);
			}
			
			//if compile errors exist, ask the user before continuing.
			if (compileErrorsInProjs) {
				IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);
				if (prompter != null) {
					continueLaunch = ((Boolean)prompter.handleStatus(complileErrorPromptStatus, null)).booleanValue();
				}
			}
		} finally {
			monitor.done();
		}
		
		if (continueLaunch) // If no problems then restore the previous build configuration. Otherwise leave it so the user can fix the build issues.
			resetBuildConfiguration(project);
		
		return continueLaunch;
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
	 * Searches for compile errors in the specified project
	 * 
	 * @param proj
	 *            The project to search
	 * @return true if compile errors exist, otherwise false
	 * @since 6.0
	 */
	protected boolean existsErrors(IProject proj) throws CoreException {
		IMarker[] markers = proj.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

		if (markers.length > 0) {
			for (int j = 0; j < markers.length; j++) {
				Object severityAttribute = markers[j].getAttribute(IMarker.SEVERITY);
				if (severityAttribute != null && ((Integer) severityAttribute).intValue() == IMarker.SEVERITY_ERROR) {
					return true;
				}
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		if(monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		if(!mode.equals(ILaunchManager.RUN_MODE))
			org.eclipse.cdt.launch.LaunchUtils.enableActivity("org.eclipse.cdt.debug.cdigdbActivity", true); //$NON-NLS-1$


		workspaceBuildBeforeLaunch = false;

		int scale = 1000;
		int totalWork = 2 * scale;
		
		try {
			monitor.beginTask(LaunchMessages.getString("AbstractCLaunchDelegate.20"), totalWork); //$NON-NLS-1$
			
			// build project list
			orderedProjects = null;
			ICProject cProject = CDebugUtils.getCProject(configuration);
			if (cProject != null) {
				project = cProject.getProject();
				HashSet projectSet = new HashSet();
				getReferencedProjectSet(project, projectSet);
				orderedProjects = getBuildOrder(new ArrayList(projectSet));
			}
			monitor.worked(scale);
			
			// do generic launch checks
			return super.preLaunchCheck(configuration, mode, new SubProgressMonitor(monitor, scale));
		} finally {
			monitor.done();
		}
	}

	/**
	 * @param project
	 * @param exePath
	 * @return
	 * @throws CoreException
	 */
	protected IBinaryObject verifyBinary(ICProject proj, IPath exePath) throws CoreException {
		Exception exception;
		try {
			return LaunchUtils.getBinary(proj.getProject(), exePath);
		} catch (ClassCastException e) {
			exception = e;
		}
		Status status = new Status(IStatus.ERROR,getPluginID(), 
				ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_BINARY, 
				LaunchMessages.getString("AbstractCLaunchDelegate.Program_is_not_a_recongnized_executable") + " " + exePath.toOSString(), //$NON-NLS-1$ 
				exception); 
		throw new CoreException(status);
	}

	/**
	 * @param config
	 * @return
	 * @throws CoreException
	 */
	protected Properties getEnvironmentAsProperty(ILaunchConfiguration config) throws CoreException {
		String[] envp = getEnvironment(config);
		Properties p = new Properties( );
		for( int i = 0; i < envp.length; i++ ) {
			int idx = envp[i].indexOf('=');
			if (idx != -1) {
				String key = envp[i].substring(0, idx);
				String value = envp[i].substring(idx + 1);
				p.setProperty(key, value);
			} else {
				p.setProperty(envp[i], ""); //$NON-NLS-1$
			}
		}
		return p;
	}

	/**
	 * Return the save environment variables in the configuration. The array
	 * does not include the default environment of the target. array[n] :
	 * name=value
	 * @throws CoreException
	 */
	protected String[] getEnvironment(ILaunchConfiguration config) throws CoreException {
		try {
			// Migrate old env settings to new.
			Map map = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map)null);
			ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
			if (map != null) {
				wc.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map)null);
				config = wc.doSave();
			}
			boolean append = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_INHERIT, true);
			wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, append);
		} catch (CoreException e) {
		}		
		String[] array = DebugPlugin.getDefault().getLaunchManager().getEnvironment(config);
		if (array == null) {
			return new String[0];
		}
		return array;
	}
	
	/**
	 * Return the save environment variables in the configuration. The array
	 * does not include the default environment of the target. array[n] :
	 * name=value
	 * @deprecated
	 */
	@Deprecated
	protected String[] getEnvironmentArray(ILaunchConfiguration config) {
		Map env = null;
		try {
			env = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map)null);
		} catch (CoreException e) {
		}
		if (env == null) {
			return new String[0];
		}
		String[] array = new String[env.size()];
		Iterator entries = env.entrySet().iterator();
		Entry entry;
		for (int i = 0; entries.hasNext() && i < array.length; i++) {
			entry = (Entry)entries.next();
			array[i] = ((String)entry.getKey()) + "=" + ((String)entry.getValue()); //$NON-NLS-1$
		}
		return array;
	}

	/**
	 * Return the save environment variables of this configuration. The array
	 * does not include the default environment of the target.
	 * @deprecated
	 */
	@Deprecated
	protected Properties getEnvironmentProperty(ILaunchConfiguration config) {
		Properties prop = new Properties();
		Map env = null;
		try {
			env = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map)null);
		} catch (CoreException e) {
		}
		if (env == null)
			return prop;
		Iterator entries = env.entrySet().iterator();
		Entry entry;
		while (entries.hasNext()) {
			entry = (Entry)entries.next();
			prop.setProperty((String)entry.getKey(), (String)entry.getValue());
		}
		return prop;
	}

	/**
	 * Returns the default process attribute map for C/C++ processes.
	 * 
	 * @return default process attribute map for C/C++ processes
	 */
	protected Map getDefaultProcessMap() {
		Map map = new HashMap();
		map.put( IProcess.ATTR_PROCESS_TYPE, ICDTLaunchConfigurationConstants.ID_PROGRAM_PROCESS_TYPE );
		return map;
	}
}
