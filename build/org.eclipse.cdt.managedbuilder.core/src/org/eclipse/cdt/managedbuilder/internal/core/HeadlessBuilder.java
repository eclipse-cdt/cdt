/*******************************************************************************
 * Copyright (c) 2009, 2010 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom Corporation - initial API and implementation
 *     Clare Richardson (Motorola) - Bug 281397 building specific configs
 *     Dmitry Kozlov (CodeSourcery) - Bug 309909 Headless build import fails
 *                                    silently with relative pathname
 *                                  - Bug 300554 Build status not propagated
 *                                    to exit code
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * A headless builder for CDT with additional features.
 *
 * IApplication ID: org.eclipse.cdt.managedbuilder.core.headlessbuild
 * Provides:
 *   - Import projects :                       -import     {[uri:/]/path/to/project}
 *   - Import all projects in the tree :       -importAll  {[uri:/]/path/to/projectTreeURI}
 *   - Build projects / the workspace :        -build      {project_name_reg_ex/config_name_reg_ex | all}
 *   - Clean build projects / the workspace :  -cleanBuild {project_name_reg_ex/config_name_reg_ex | all}
 *   - Add Include path to build :             -I          {include_path}
 *   - Add Include file to build :             -include    {include_file}
 *   - Add preprocessor define to build :      -D          {prepoc_define}
 *
 * Build output is automatically sent to stdout.
 * @since 6.0
 */
public class HeadlessBuilder implements IApplication {

	/**
	 * IProgressMonitor to provide printing of task
	 */
	private class PrintingProgressMonitor extends NullProgressMonitor {
		@Override
		public void beginTask(String name, int totalWork) {
			if (name != null && name.length() > 0)
				System.out.println(name);
		}
	}

	/** Error return status */
	public static final Integer ERROR = 1;
	/** OK return status */
	public static final Integer OK = IApplication.EXIT_OK;

	/** Set of project URIs / paths to import */
	private final Set<String> projectsToImport = new HashSet<String>();
	/** Tree of projects to recursively import */
	private final Set<String> projectTreeToImport = new HashSet<String>();
	/** Set of project names to build */
	private final Set<String> projectRegExToBuild = new HashSet<String>();
	/** Set of project names to clean */
	private final Set<String> projectRegExToClean = new HashSet<String>();
	private boolean buildAll = false;
	private boolean cleanAll = false;

	private static final String MATCH_ALL_CONFIGS = ".*"; //$NON-NLS-1$

	/*
	 *  Find all project build configurations that match the regular expression ("project/config")
	 */
	private Map<IProject, Set<ICConfigurationDescription>> matchConfigurations(String regularExpression, IProject[] projectList, Map<IProject, Set<ICConfigurationDescription>> cfgMap) {
		try {
			int separatorIndex = regularExpression.indexOf('/');

			String projectRegEx;
			String configRegEx;
			if(separatorIndex == -1 || separatorIndex == regularExpression.length()-1) {
				// build all configurations for this project
				projectRegEx = regularExpression;
				configRegEx = MATCH_ALL_CONFIGS;
			} else {
				projectRegEx = regularExpression.substring(0, separatorIndex);
				configRegEx = regularExpression.substring(separatorIndex + 1, regularExpression.length());
			}

			Pattern projectPattern = Pattern.compile(projectRegEx);
			Pattern configPattern = Pattern.compile(configRegEx);

			// Find the projects that match the regular expression
			boolean projectMatched = false;
			boolean configMatched = false;
			for(IProject project : projectList) {
				Matcher projectMatcher = projectPattern.matcher(project.getName());

				if(projectMatcher.matches()) {
					projectMatched = true;
					// Find the configurations that match the regular expression
					ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(project, false);
					if (desc == null) {
						System.err.println(HeadlessBuildMessages.HeadlessBuilder_project + project.getName() + HeadlessBuildMessages.HeadlessBuilder_Not_CDT_Proj);
						continue;
					}
					ICConfigurationDescription[] cfgs = desc.getConfigurations();

					for(ICConfigurationDescription cfg : cfgs) {
						Matcher cfgMatcher = configPattern.matcher(cfg.getName());

						if(cfgMatcher.matches()) {
							configMatched = true;
							// Build this configuration for this project
							Set<ICConfigurationDescription> set = cfgMap.get(project);
							if(set == null)
								set = new HashSet<ICConfigurationDescription>();
							set.add(cfg);
							cfgMap.put(project, set);
						}
					}
				}
			}
			if (!projectMatched)
				System.err.println(HeadlessBuildMessages.HeadlessBuilder_NoProjectMatched + regularExpression + HeadlessBuildMessages.HeadlessBuilder_Skipping2);
			else if (!configMatched)
				System.err.println(HeadlessBuildMessages.HeadlessBuilder_NoConfigMatched + regularExpression + HeadlessBuildMessages.HeadlessBuilder_Skipping2);
		} catch (PatternSyntaxException e) {
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_RegExSyntaxError + e.toString());
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_Skipping + regularExpression + HeadlessBuildMessages.HeadlessBuilder_Quote);
		}
		return cfgMap;
	}

	/*
	 *  Build the given configurations using the specified build type (FULL, CLEAN, INCREMENTAL)
	 */
	private void buildConfigurations(Map<IProject, Set<ICConfigurationDescription>> projConfigs, final IProgressMonitor monitor, final int buildType) throws CoreException {
		for (Map.Entry<IProject, Set<ICConfigurationDescription>> entry : projConfigs.entrySet()) {
			final IProject proj = entry.getKey();
			Set<ICConfigurationDescription> cfgDescs = entry.getValue();

			IConfiguration[] configs = new IConfiguration[cfgDescs.size()];
			int i = 0;
			for (ICConfigurationDescription cfgDesc : cfgDescs)
				configs[i++] = ManagedBuildManager.getConfigurationForDescription(cfgDesc);
			final Map<String, String> map = BuilderFactory.createBuildArgs(configs);

			IWorkspaceRunnable op = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					ICommand[] commands = proj.getDescription().getBuildSpec();
					monitor.beginTask("", commands.length); //$NON-NLS-1$
					for (int i = 0; i < commands.length; i++) {
						if (commands[i].getBuilderName().equals(CommonBuilder.BUILDER_ID)) {
							proj.build(buildType, CommonBuilder.BUILDER_ID, map, new SubProgressMonitor(monitor, 1));
						} else {
							proj.build(buildType, commands[i].getBuilderName(),
							commands[i].getArguments(), new SubProgressMonitor(monitor, 1));
						}
					}
					monitor.done();
				}
			};
			try {
				ResourcesPlugin.getWorkspace().run(op, monitor);
			} finally {
				monitor.done();
			}
		}
	}

	/**
	 * Import a project into the workspace
	 * @param projURIStr base URI string
	 * @param recurse should we recurse down the URI importing all projects?
	 * @return int OK / ERROR
	 */
	private int importProject(String projURIStr, boolean recurse) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProgressMonitor monitor = new PrintingProgressMonitor();
		InputStream in = null;
		try {
			URI project_uri = null;
			try {
				project_uri = URI.create(projURIStr);
			} catch (Exception e) {
				// Will be treated as straightforward path in the case below
			}

			// Handle local paths as well
			if (project_uri == null || project_uri.getScheme() == null) {
				IPath p = new Path(projURIStr).addTrailingSeparator();
				project_uri = URIUtil.toURI(p);

				// Handle relative paths as relative to cwd
				if (project_uri.getScheme() == null) {
					String cwd = System.getProperty("user.dir");  //$NON-NLS-1$
					p = new Path(cwd).addTrailingSeparator();
					p = p.append(projURIStr);
					project_uri = URIUtil.toURI(p);
				}
				if (project_uri.getScheme() == null) {
					System.err.println(HeadlessBuildMessages.HeadlessBuilder_invalid_uri + project_uri);
					return ERROR;
				}
			}

			if (recurse) {
				if (!EFS.getStore(project_uri).fetchInfo().exists()) {
					System.err.println(HeadlessBuildMessages.HeadlessBuilder_Directory + project_uri + HeadlessBuildMessages.HeadlessBuilder_cant_be_found);
					return ERROR;
				}
				for (IFileStore info : EFS.getStore(project_uri).childStores(EFS.NONE, monitor)) {
					if (!info.fetchInfo().isDirectory())
						continue;
					int status = importProject(info.toURI().toString(), recurse);
					if (status != OK)
						return status;
				}
			}

			// Load the project description
			IFileStore fstore = EFS.getStore(project_uri).getChild(".project"); //$NON-NLS-1$
			if (!fstore.fetchInfo().exists()) {
				if (!recurse) {
					System.err.println(HeadlessBuildMessages.HeadlessBuilder_project + project_uri + HeadlessBuildMessages.HeadlessBuilder_cant_be_found);
					return ERROR;
				}
				// .project not found; OK if we're not recursing
				return OK;
			}

			in = fstore.openInputStream(EFS.NONE, monitor);
			IProjectDescription desc = root.getWorkspace().loadProjectDescription(in);

			// Check that a project with the same name doesn't already exist in the workspace
			IProject project = root.getProject(desc.getName());
			if (project.exists()) {
				// It's ok if the project we're importing is the same as one already in the workspace
				if (URIUtil.equals(project.getLocationURI(), project_uri)) {
					project.open(monitor);
					return OK;
				}
				System.err.println(HeadlessBuildMessages.HeadlessBuilder_project + desc.getName() + HeadlessBuildMessages.HeadlessBuilder_already_exists_in_workspace);
				return ERROR;
			}
			// Create and open the project
			// Note that if the project exists directly under the workspace root, we can't #setLocationURI(...)
			if (!URIUtil.equals(org.eclipse.core.runtime.URIUtil.append(
								ResourcesPlugin.getWorkspace().getRoot().getLocationURI(),
								org.eclipse.core.runtime.URIUtil.lastSegment(project_uri)), project_uri))
				desc.setLocationURI(project_uri);
			else
				project_uri = null;
			// Check the URI is valid for a project in this workspace
			if (!root.getWorkspace().validateProjectLocationURI(project, project_uri).equals(Status.OK_STATUS)) {
				System.err.println(HeadlessBuildMessages.HeadlessBuilder_URI + project_uri + HeadlessBuildMessages.HeadlessBuilder_is_not_valid_in_workspace);
				return ERROR;
			}

			project.create(desc, monitor);
			project.open(monitor);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e2) { /* don't care */ }
		}
		return OK;
	}

	private boolean isProjectSuccesfullyBuild(IProject project) {
		boolean result = false;
		try {
			result = project.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE) < IMarker.SEVERITY_ERROR;
		} catch (CoreException e) {
			ManagedBuilderCorePlugin.log(e);
		}
		return result;
	}

	public Object start(IApplicationContext context) throws Exception {
		// Build result: whether projects were built successfully
		boolean buildSuccessful = true;

		// Check its OK to use this workspace as IDEApplication does
		if (!checkInstanceLocation())
			return ERROR;

		IProgressMonitor monitor = new PrintingProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		final boolean isAutoBuilding = root.getWorkspace().isAutoBuilding();
		try {
			{
				// Turn off workspace auto-build
				IWorkspaceDescription desc = root.getWorkspace().getDescription();
				desc.setAutoBuilding(false);
				root.getWorkspace().setDescription(desc);
			}

			if (!root.isAccessible()) {
				System.err.println(HeadlessBuildMessages.HeadlessBuilder_Workspace + root.getLocationURI().toString() + HeadlessBuildMessages.HeadlessBuilder_is_not_accessible);
				return ERROR;
			}

			// Handle user provided arguments
			if (!getArguments((String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS)))
				return ERROR;

			// Set the console environment so build output is echo'd to stdout
			if (System.getProperty("org.eclipse.cdt.core.console") == null) //$NON-NLS-1$
				System.setProperty("org.eclipse.cdt.core.console", "org.eclipse.cdt.core.systemConsole"); //$NON-NLS-1$ //$NON-NLS-2$

			/*
			 * Perform the project import
			 */
			// Import any projects that need importing
			for (String projURIStr : projectsToImport) {
				int status = importProject(projURIStr, false);
				if (status != OK)
					return status;
			}
			for (String projURIStr : projectTreeToImport) {
				int status = importProject(projURIStr, true);
				if (status != OK)
					return status;
			}

			// Hook in our external settings to the build
			HeadlessBuilderExternalSettingsProvider.hookExternalSettingsProvider();

			IProject[] allProjects = root.getProjects();
			// Map from Project -> Configurations to build. We also Build all projects which are clean'd
			Map<IProject, Set<ICConfigurationDescription>> configsToBuild = new HashMap<IProject, Set<ICConfigurationDescription>>();

			/*
			 * Perform the Clean / Build
			 */
			final boolean buildAllConfigs = ACBuilder.needAllConfigBuild();
			try {
				// Clean the projects
				if (cleanAll) {
					// Ensure we clean all the configurations
					ACBuilder.setAllConfigBuild(true);

					System.out.println(HeadlessBuildMessages.HeadlessBuilder_cleaning_all_projects);
					root.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);

					// Reset the build_all_configs preference value to its previous state
					ACBuilder.setAllConfigBuild(buildAllConfigs);
				} else {
					// Resolve the regular expression project names to build configurations
					for (String regEx : projectRegExToClean)
						matchConfigurations(regEx, allProjects, configsToBuild);
					// Clean the list of configurations
					buildConfigurations(configsToBuild, monitor, IncrementalProjectBuilder.CLEAN_BUILD);
				}

				// Build the projects the user wants building
				if (buildAll) {
					// Ensure we build all the configurations
					ACBuilder.setAllConfigBuild(true);

					System.out.println(HeadlessBuildMessages.HeadlessBuilder_building_all);
					root.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
					for(IProject p : root.getProjects())
						buildSuccessful = buildSuccessful && isProjectSuccesfullyBuild(p);
				} else {
					// Resolve the regular expression project names to build configurations
					for (String regEx : projectRegExToBuild)
						matchConfigurations(regEx, allProjects, configsToBuild);
					// Build the list of configurations
					buildConfigurations(configsToBuild, monitor, IncrementalProjectBuilder.FULL_BUILD);
					for(IProject p : configsToBuild.keySet())
						buildSuccessful = buildSuccessful && isProjectSuccesfullyBuild(p);
				}
			} finally {
				// Reset the build_all_configs preference value to its previous state
				ACBuilder.setAllConfigBuild(buildAllConfigs);
				// Unhook the external settings provider
				HeadlessBuilderExternalSettingsProvider.unhookExternalSettingsProvider();
			}
		} finally {
			// Wait for any outstanding jobs to finish
			while (!Job.getJobManager().isIdle())
				Thread.sleep(10);

			// Reset workspace auto-build preference
			IWorkspaceDescription desc = root.getWorkspace().getDescription();
			desc.setAutoBuilding(isAutoBuilding);
			root.getWorkspace().setDescription(desc);
		}

		return buildSuccessful ? OK : ERROR;
	}

    /**
     * Verify that it's safe to use the specified workspace. i.e. that
     * we can write to it and that it's not already locked / in-use.
     *
     * Return true if a valid workspace path has been set and false otherwise.
     *
     * @return true if a valid instance location has been set and false
     *         otherwise
     */
    private boolean checkInstanceLocation() {
        // -data @none was specified but an ide requires workspace
        Location instanceLoc = Platform.getInstanceLocation();
        if (instanceLoc == null || !instanceLoc.isSet()) {
        	System.err.println(HeadlessBuildMessages.HeadlessBuilder_MustSpecifyWorkspace);
            return false;
        }

        // -data "/valid/path", workspace already set
        try {
            // at this point its valid, so try to lock it to prevent concurrent use
            if (!instanceLoc.lock()) {
            	System.err.println(HeadlessBuildMessages.HeadlessBuilder_WorkspaceInUse);
                return false;
            }
            return true;
        } catch (IOException e) {
        	System.err.println(HeadlessBuildMessages.HeadlessBuilder_CouldntLockWorkspace);
        }
        return false;
    }


	/**
	 * Helper method to process expected arguments
	 *
	 * Arguments
	 *   -import     {[uri:/]/path/to/project}
	 *   -importAll  {[uri:/]/path/to/projectTreeURI} Import all projects in the tree
	 *   -build      {project_name_reg_ex/config_name_reg_ex | all}
	 *   -cleanBuild {project_name_reg_ex/config_name_reg_ex | all}
	 *   -I          {include_path} additional include_path to add to tools
	 *   -include    {include_file} additional include_file to pass to tools
	 *   -D          {prepoc_define} addition preprocessor defines to pass to the tools
	 *
	 * Each argument may be specified more than once
	 * @param args String[] of arguments to parse
	 * @return boolean indicating success
	 */
	public boolean getArguments(String[] args) {
		try {
			if (args == null || args.length == 0)
				throw new Exception(HeadlessBuildMessages.HeadlessBuilder_no_arguments);
			for (int i = 0; i < args.length; i++) {
				if ("-import".equals(args[i])) { //$NON-NLS-1$
					projectsToImport.add(args[++i]);
				} else if ("-importAll".equals(args[i])) { //$NON-NLS-1$
					projectTreeToImport.add(args[++i]);
				} else if ("-build".equals(args[i])) { //$NON-NLS-1$
					projectRegExToBuild.add(args[++i]);
				} else if ("-cleanBuild".equals(args[i])) { //$NON-NLS-1$
					projectRegExToClean.add(args[++i]);
				} else if ("-D".equals(args[i])) { //$NON-NLS-1$
					String macro = args[++i];
					String macroVal = ""; //$NON-NLS-1$
					if (macro.indexOf('=') != -1) {
						macroVal = macro.substring(macro.indexOf('=') + 1);
						macro = macro.substring(0, macro.indexOf('='));
					}
					HeadlessBuilderExternalSettingsProvider.additionalSettings.add(new CMacroEntry(macro, macroVal, 0));
				} else if ("-I".equals(args[i])) { //$NON-NLS-1$
					HeadlessBuilderExternalSettingsProvider.additionalSettings.add(new CIncludePathEntry(args[++i], 0));
				} else if ("-include".equals(args[i])) { //$NON-NLS-1$
					HeadlessBuilderExternalSettingsProvider.additionalSettings.add(new CIncludeFileEntry(args[++i], 0));
				} else {
					throw new Exception(HeadlessBuildMessages.HeadlessBuilder_unknown_argument + args[i]);
				}
			}
		} catch (Exception e) {
			// Print usage
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_invalid_argument + args != null ? Arrays.toString(args) : ""); //$NON-NLS-1$
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_Error + e.getMessage());
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_usage);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_usage_import);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_importAll);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_usage_build);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_usage_clean_build);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_InlucdePath);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_IncludeFile);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_PreprocessorDefine);
			return false;
		}

		if (projectRegExToClean.contains("all") || projectRegExToClean.contains("*")) { //$NON-NLS-1$ //$NON-NLS-2$
			cleanAll = true;
			buildAll = true;
			projectRegExToClean.remove("all"); //$NON-NLS-1$
			projectRegExToClean.remove("*"); //$NON-NLS-1$
		}
		if (projectRegExToBuild.contains("all") || projectRegExToBuild.contains("*")) { //$NON-NLS-1$ //$NON-NLS-2$
			buildAll = true;
			projectRegExToBuild.remove("all"); //$NON-NLS-1$
			projectRegExToBuild.remove("*"); //$NON-NLS-1$
		}

		return true;
	}


	public void stop() {
	}

}
