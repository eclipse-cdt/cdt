/*******************************************************************************
 * Copyright (c) 2009 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.core;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * A headless builder for CDT with additional features.
 *
 * IApplication ID: org.eclipse.cdt.managedbuilder.core.headlessbuild
 * Provides:
 *   - Import projects :                       -import     {[uri:/]/path/to/project}
 *   - Build projects / the workspace :        -build      {project_name | all}
 *   - Clean build projects / the workspace :  -cleanBuild {project_name | all}
 *
 * Build output is automatically sent to stdout.
 * All CDT configurations are built.
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

	/** Preference Value for building all configurations taken from ACBuilder */
	private static final String PREF_BUILD_ALL_CONFIGS = "build.all.configs.enabled"; //$NON-NLS-1$

	/** Error return status */
	public static final Integer ERROR = -1;
	/** OK return status */
	public static final Integer OK = IApplication.EXIT_OK;

	/** Set of project URIs / paths to import */
	private final Set<String> projectsToImport = new HashSet<String>();
	/** Set of project names to build */
	private final Set<String> projectsToBuild = new HashSet<String>();
	/** Set of project names to clean */
	private final Set<String> projectsToClean = new HashSet<String>();
	private boolean buildAll = false;
	private boolean cleanAll = false;

	public Object start(IApplicationContext context) throws Exception {
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
					}

					// workaround for bug 274863 -- project URIs must end with a Path separator
					String uri_string = project_uri.toASCIIString();
					if (!uri_string.endsWith("/")) { //$NON-NLS-1$
						uri_string += IPath.SEPARATOR;
						project_uri = new URI(uri_string);
					}

					// Load the project description
					IFileStore fstore = EFS.getStore(project_uri.resolve(".project")); //$NON-NLS-1$
					if (!fstore.fetchInfo().exists()) {
						System.err.println(HeadlessBuildMessages.HeadlessBuilder_project + project_uri.resolve(".project") + HeadlessBuildMessages.HeadlessBuilder_cant_be_found); //$NON-NLS-2$
						return ERROR;
					}
					in = fstore.openInputStream(EFS.NONE, monitor);
					IProjectDescription desc = root.getWorkspace().loadProjectDescription(in);

					// Check that a project with the same name doesn't already exist in the workspace
					IProject project = root.getProject(desc.getName());
					if (project.exists()) {
						System.err.println(HeadlessBuildMessages.HeadlessBuilder_project + desc.getName() + HeadlessBuildMessages.HeadlessBuilder_already_exists_in_workspace);
						return ERROR;
					}
					// Check the URI is valid for a project in this workspace
					if (!root.getWorkspace().validateProjectLocationURI(project, project_uri).equals(Status.OK_STATUS)) {
						System.err.println(HeadlessBuildMessages.HeadlessBuilder_URI + project_uri + HeadlessBuildMessages.HeadlessBuilder_is_not_valid_in_workspace);
						return ERROR;
					}

					// Create and open the project
					desc.setLocationURI(project_uri);
					project.create(desc, monitor);
					project.open(monitor);
				} finally {
					if (in != null)
						in.close();
				}
			}

			/*
			 * Perform the Clean / Build
			 */
			final Preferences prefs = CCorePlugin.getDefault().getPluginPreferences();
			final boolean buildAllConfigs = prefs.getBoolean(PREF_BUILD_ALL_CONFIGS);
			try {
				// Ensure we clean / build all the configurations
				prefs.setValue(PREF_BUILD_ALL_CONFIGS, true);

				// Clean the projects
				if (cleanAll) {
					System.out.println(HeadlessBuildMessages.HeadlessBuilder_cleaning_all_projects);
					root.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
				} else {
					for (String project : projectsToClean) {
						IProject prj = root.getProject(project);
						if (!prj.exists()) {
							System.err.println(HeadlessBuildMessages.HeadlessBuilder_clean_failed + project + HeadlessBuildMessages.HeadlessBuilder_16);
							continue;
						}
						prj.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
					}
				}

				// Build the projects the user wants building
				if (buildAll) {
					System.out.println(HeadlessBuildMessages.HeadlessBuilder_building_all);
					root.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				} else {
					for (String project : projectsToBuild) {
						IProject prj = root.getProject(project);
						if (!prj.exists()) {
							System.err.println(HeadlessBuildMessages.HeadlessBuilder_build_failed + project + HeadlessBuildMessages.HeadlessBuilder_16);
							continue;
						}
						prj.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
					}
				}
			} finally {
				// Reset the build_all_configs preference value to its previous state
				prefs.setValue(PREF_BUILD_ALL_CONFIGS, buildAllConfigs);
				CCorePlugin.getDefault().savePluginPreferences();
			}
		} finally {
			// Reset workspace auto-build preference
			IWorkspaceDescription desc = root.getWorkspace().getDescription();
			desc.setAutoBuilding(isAutoBuilding);
			root.getWorkspace().setDescription(desc);
		}
		return OK;
	}

	/**
	 * Helper method to process expected arguments
	 *
	 * Arguments
	 *   -import     {[uri:/]/path/to/project}
	 *   -build      {project_name | all}
	 *   -cleanBuild {projec_name | all}
	 *
	 * Each argument may be specified more than once
	 * @param args
	 * @return boolean indicating success
	 */
	private boolean getArguments(String[] args) {
		try {
			if (args == null || args.length == 0)
				throw new Exception(HeadlessBuildMessages.HeadlessBuilder_no_arguments);
			for (int i = 0; i < args.length; i++) {
				if ("-import".equals(args[i])) { //$NON-NLS-1$
					projectsToImport.add(args[++i]);
				} else if ("-build".equals(args[i])) { //$NON-NLS-1$
					projectsToBuild.add(args[++i]);
				} else if ("-cleanBuild".equals(args[i])) { //$NON-NLS-1$
					projectsToClean.add(args[++i]);
				} else {
					System.err.println(HeadlessBuildMessages.HeadlessBuilder_unknown_argument + args[i]);
				}
			}
		} catch (Exception e) {
			// Print usage
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_invalid_argument + args != null ? Arrays.toString(args) : ""); //$NON-NLS-2$
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_Error + e.getMessage());
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_usage);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_usage_import);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_usage_build);
			System.err.println(HeadlessBuildMessages.HeadlessBuilder_usage_clean_build);
			return false;
		}
		if (projectsToClean.contains("all")) { //$NON-NLS-1$
			cleanAll = true;
			buildAll = true;
			projectsToClean.remove("all"); //$NON-NLS-1$
		}
		if (projectsToBuild.contains("all")) { //$NON-NLS-1$
			buildAll = true;
			projectsToBuild.remove("all"); //$NON-NLS-1$
		}
		// We must build all the projects the user wants build
		projectsToBuild.addAll(projectsToClean);
		return true;
	}


	public void stop() {
	}

}
