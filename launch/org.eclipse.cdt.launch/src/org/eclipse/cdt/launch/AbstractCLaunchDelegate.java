/**********************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.spawner.EnvironmentReader;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

abstract public class AbstractCLaunchDelegate extends LaunchConfigurationDelegate {

	/**
	 * The project containing the programs file being launched
	 */
	private IProject project;
	/**
	 * A list of prequisite projects ordered by their build order.
	 */
	private List orderedProjects;

	abstract public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException;

	/**
	 * Return the save environment variables in the configuration. The array
	 * does not include the default environment of the target. array[n] :
	 * name=value
	 */
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
	 */
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
	 * Return the default Environment of the target.
	 */
	protected Properties getDefaultEnvironment() {
		return EnvironmentReader.getEnvVars();
	}

	/**
	 * Expand the variable with the format ${key}. example: HOME=/foobar NEWHOME =
	 * ${HOME}/project The environement NEWHOME will be /foobar/project.
	 */
	protected Properties expandEnvironment(ILaunchConfiguration config) {
		return expandEnvironment(getEnvironmentProperty(config));
	}

	/**
	 * Expand the variable with the format ${key}. example: HOME=/foobar NEWHOME =
	 * ${HOME}/project The environement NEWHOME will be /foobar/project.
	 */
	protected Properties expandEnvironment(Properties props) {
		Enumeration names = props.propertyNames();
		if (names != null) {
			while (names.hasMoreElements()) {
				String key = (String)names.nextElement();
				String value = props.getProperty(key);
				if (value != null && value.indexOf('$') != -1) {
					StringBuffer sb = new StringBuffer();
					StringBuffer param = new StringBuffer();
					char prev = '\n';
					char ch = prev;
					boolean inMacro = false;
					boolean inSingleQuote = false;

					for (int i = 0; i < value.length(); i++) {
						ch = value.charAt(i);
						switch (ch) {
							case '\'' :
								if (prev != '\\') {
									inSingleQuote = !inSingleQuote;
								}
								break;

							case '$' :
								if (!inSingleQuote && prev != '\\') {
									if (i < value.length() && value.indexOf('}', i) > 0) {
										char c = value.charAt(i + 1);
										if (c == '{') {
											param.setLength(0);
											inMacro = true;
											prev = ch;
											continue;
										}
									}
								}
								break;

							case '}' :
								if (inMacro) {
									inMacro = false;
									String v = null;
									String p = param.toString();
									/*
									 * Search in the current property only if it
									 * is not the same name.
									 */
									if (!p.equals(key)) {
										v = props.getProperty(p);
									}
									/* Fallback to the default Environemnt. */
									if (v == null) {
										Properties def = getDefaultEnvironment();
										if (def != null) {
											v = def.getProperty(p);
										}
									}
									if (v != null) {
										sb.append(v);
									}
									param.setLength(0);
									/* Skip the trailing } */
									prev = ch;
									continue;
								}
								break;
						} /* switch */

						if (!inMacro) {
							sb.append(ch);
						} else {
							/* Do not had the '{' */
							if (! (ch == '{' && prev == '$')) {
								param.append(ch);
							}
						}
						prev = (ch == '\\' && prev == '\\') ? '\n' : ch;
					} /* for */
					props.setProperty(key, sb.toString());
				} /* !if (value ..) */
			} /* while() */
		} /* if (names != null) */
		return props;
	}

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

	protected IPath getWorkingDirectoryPath(ILaunchConfiguration config) throws CoreException {
		String path = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);
		if (path != null) {
			return new Path(path);
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
		MultiStatus status = new MultiStatus(getPluginID(), code, message, exception);
		status.add(new Status(IStatus.ERROR, getPluginID(), code, exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
				exception));
		throw new CoreException(status);
	}

	protected void cancel(String message, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.OK, getPluginID(), code, message, null));
	}

	abstract protected String getPluginID();

	public static ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
				if (cProject != null && cProject.exists()) {
					return cProject;
				}
			}
		}
		return null;
	}

	public static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}

	public static String getProgramName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
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
	protected void setSourceLocator(ILaunch launch, ILaunchConfiguration configuration) throws CoreException {
		//  set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			IPersistableSourceLocator sourceLocator;
			String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (id == null) {
				ICProject cProject = getCProject(configuration);
				if (cProject == null) {
					abort(LaunchUIPlugin.getResourceString("Launch.common.Project_does_not_exist"), null, //$NON-NLS-1$
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
		return config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);

	}
	/**
	 * Returns the program arguments as an array of individual arguments.
	 * 
	 * @return the program arguments as an array of individual arguments
	 */
	public String[] getProgramArgumentsArray(ILaunchConfiguration config) throws CoreException {
		return parseArguments(getProgramArguments(config));
	}

	private static String[] parseArguments(String args) {
		if (args == null)
			return new String[0];
		ArgumentParser parser = new ArgumentParser(args);
		String[] res = parser.parseArguments();

		return res;
	}

	protected ICDebugConfiguration getDebugConfig(ILaunchConfiguration config) throws CoreException {
		ICDebugConfiguration dbgCfg = null;
		try {
			dbgCfg = CDebugCorePlugin.getDefault().getDebugConfiguration(
					config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, "")); //$NON-NLS-1$
		} catch (CoreException e) {
			IStatus status = new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(),
					ICDTLaunchConfigurationConstants.ERR_DEBUGGER_NOT_INSTALLED,
					LaunchUIPlugin.getResourceString("AbstractCLaunchDelegate.Debugger_not_installed"), //$NON-NLS-1$
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
	protected String renderDebuggerProcessLabel() {
		String format = "{0} ({1})"; //$NON-NLS-1$
		String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[]{
				LaunchUIPlugin.getResourceString("AbstractCLaunchDelegate.Debugger_Process"), timestamp}); //$NON-NLS-1$
	}

	protected ICProject verifyCProject(ILaunchConfiguration config) throws CoreException {
		String name = getProjectName(config);
		if (name == null) {
			abort(LaunchUIPlugin.getResourceString("AbstractCLaunchDelegate.C_Project_not_specified"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
		}
		ICProject cproject = getCProject(config);
		if (cproject == null) {
			IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (!proj.exists()) {
				abort(LaunchUIPlugin.getFormattedResourceString("AbstractCLaunchDelegate.Project_NAME_does_not_exist", name), null, //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
			} else if (!proj.isOpen()) {
				abort(LaunchUIPlugin.getFormattedResourceString("AbstractCLaunchDelegate.Project_NAME_is_closed", name), null, //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
			}
			abort(LaunchUIPlugin.getResourceString("AbstractCLaunchDelegate.Not_a_C_CPP_project"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
		}
		return cproject;
	}

	protected IFile getProgramFile(ILaunchConfiguration config) throws CoreException {
		ICProject cproject = verifyCProject(config);
		String fileName = getProgramName(config);
		if (fileName == null) {
			abort(LaunchUIPlugin.getResourceString("AbstractCLaunchDelegate.Program_file_not_specified"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROGRAM);
		}

		IFile programPath = ((IProject)cproject.getResource()).getFile(fileName);
		if (programPath == null || !programPath.exists() || !programPath.getLocation().toFile().exists()) {
			abort(LaunchUIPlugin.getResourceString("AbstractCLaunchDelegate.Program_file_does_not_exist"), //$NON-NLS-1$
					new FileNotFoundException(LaunchUIPlugin.getFormattedResourceString(
							"AbstractCLaunchDelegate.PROGRAM_PATH_not_found", programPath.getLocation().toOSString())), //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
		return programPath;
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
			ICProject cp = getCProject(configuration);
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
				abort(LaunchUIPlugin.getResourceString("AbstractCLaunchDelegate.Working_directory_does_not_exist"), //$NON-NLS-1$
						new FileNotFoundException(LaunchUIPlugin.getFormattedResourceString(
								"AbstractCLaunchDelegate.PROGRAM_PATH_not_found", path.toOSString())), //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
			} else {
				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
				if (res instanceof IContainer && res.exists()) {
					return res.getLocation().toFile();
				}
				abort(LaunchUIPlugin.getResourceString("AbstractCLaunchDelegate.Working_directory_does_not_exist"), //$NON-NLS-1$
						new FileNotFoundException(LaunchUIPlugin.getFormattedResourceString(
								"AbstractCLaunchDelegate.PROGRAM_PATH_does_not_exist", path.toOSString())), //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
			}
		}
		return null;
	}

	private static class ArgumentParser {

		private String fArgs;
		private int fIndex = 0;
		private int ch = -1;

		public ArgumentParser(String args) {
			fArgs = args;
		}

		public String[] parseArguments() {
			ArrayList v = new ArrayList();

			ch = getNext();
			while (ch > 0) {
				while (Character.isWhitespace((char)ch))
					ch = getNext();

				if (ch == '"') {
					v.add(parseString());
				} else {
					v.add(parseToken());
				}
			}

			String[] result = new String[v.size()];
			v.toArray(result);
			return result;
		}

		private int getNext() {
			if (fIndex < fArgs.length())
				return fArgs.charAt(fIndex++);
			return -1;
		}

		private String parseString() {
			StringBuffer buf = new StringBuffer();
			ch = getNext();
			while (ch > 0 && ch != '"') {
				if (ch == '\\') {
					ch = getNext();
					if (ch != '"') { // Only escape double quotes
						buf.append('\\');
					}
				}
				if (ch > 0) {
					buf.append((char)ch);
					ch = getNext();
				}
			}

			ch = getNext();

			return buf.toString();
		}

		private String parseToken() {
			StringBuffer buf = new StringBuffer();

			while (ch > 0 && !Character.isWhitespace((char)ch)) {
				if (ch == '\\') {
					ch = getNext();
					if (ch > 0) {
						if (ch != '"') { // Only escape double quotes
							buf.append('\\');
						}
						buf.append((char)ch);
						ch = getNext();
					} else if (ch == -1) { // Don't lose a trailing backslash
						buf.append('\\');
					}
				} else if (ch == '"') {
					buf.append(parseString());
				} else {
					buf.append((char)ch);
					ch = getNext();
				}
			}
			return buf.toString();
		}
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
	 *             if an exception occurrs while building
	 */
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {

		if (orderedProjects != null) {
			monitor.beginTask(LaunchUIPlugin.getResourceString("AbstractCLaunchConfigurationDelegate.building_projects"), //$NON-NLS-1$
					orderedProjects.size() + 1);

			for (Iterator i = orderedProjects.iterator(); i.hasNext();) {
				IProject proj = (IProject)i.next();
				monitor.subTask(LaunchUIPlugin.getResourceString("AbstractCLaunchConfigurationDelegate.building") + proj.getName()); //$NON-NLS-1$
				proj.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
			}

			monitor.subTask(LaunchUIPlugin.getResourceString("AbstractLaunchConfigurationDelegate.building") + project.getName()); //$NON-NLS-1$
			project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		}
		monitor.done();
		return false; //don't build. I already did it or I threw an exception.
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
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		try {
			boolean continueLaunch = true;
			if (orderedProjects != null) {
				monitor.beginTask(LaunchUIPlugin.getResourceString("AbstractCLaunchConfigurationDelegate.searching_for_errors"), //$NON-NLS-1$
						orderedProjects.size() + 1);

				boolean compileErrorsInProjs = false;

				//check prerequisite projects for compile errors.
				for (Iterator i = orderedProjects.iterator(); i.hasNext();) {
					IProject proj = (IProject)i.next();
					monitor.subTask(LaunchUIPlugin.getResourceString("AbstractCLaunchConfigurationDelegate.searching_for_errors_in") //$NON-NLS-1$
							+ proj.getName());
					compileErrorsInProjs = existsErrors(proj);
					if (compileErrorsInProjs) {
						break;
					}
				}

				//check current project, if prerequite projects were ok
				if (!compileErrorsInProjs) {
					monitor.subTask(LaunchUIPlugin.getResourceString("AbstractCLaunchConfigurationDelegate.searching_for_errors_in") //$NON-NLS-1$
							+ project.getName());
					compileErrorsInProjs = existsErrors(project);
				}

				//if compile errors exist, ask the user before continuing.
				if (compileErrorsInProjs) {
					IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);
					if (prompter != null) {
						continueLaunch = ((Boolean)prompter.handleStatus(complileErrorPromptStatus, null)).booleanValue();
					}
				}
			}
			return continueLaunch;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Searches for compile errors in the specified project
	 * 
	 * @param proj
	 *            The project to search
	 * @return true if compile errors exist, otherwise false
	 */
	private boolean existsErrors(IProject proj) throws CoreException {
		IMarker[] markers = proj.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

		if (markers.length > 0) {
			for (int j = 0; j < markers.length; j++) {
				if ( ((Integer)markers[j].getAttribute(IMarker.SEVERITY)).intValue() == IMarker.SEVERITY_ERROR) {
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
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		// build project list
		if (monitor != null) {
			monitor.subTask(LaunchUIPlugin.getResourceString("AbstractCLaunchConfigurationDelegate.20")); //$NON-NLS-1$
		}
		orderedProjects = null;
		ICProject cProject = getCProject(configuration);
		if (cProject != null) {
			project = cProject.getProject();
			HashSet projectSet = new HashSet();
			getReferencedProjectSet(project, projectSet);
			orderedProjects = getBuildOrder(new ArrayList(projectSet));
		}
		// do generic launch checks
		return super.preLaunchCheck(configuration, mode, monitor);
	}

}