/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

abstract public class AbstractCLaunchDelegate implements ILaunchConfigurationDelegate {

	abstract public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
		throws CoreException;

	protected String[] getEnvironmentArray(ILaunchConfiguration config) {
		//		Map env = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map) null);
		// TODO create env array;
		return null;
	}

	protected Properties getEnvironmentProperty(ILaunchConfiguration config) {
		Properties prop = new Properties();
		Map env = null;
		try {
			env = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ENVIROMENT_MAP, (Map) null);
		}
		catch (CoreException e) {
		}
		if (env == null)
			return prop;
		Iterator entries = env.entrySet().iterator();
		Entry entry;
		while (entries.hasNext()) {
			entry = (Entry) entries.next();
			prop.setProperty((String) entry.getKey(), (String) entry.getValue());
		}
		return prop;
	}

	protected File getWorkingDir(ILaunchConfiguration config) throws CoreException {
		String path = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
		if (path == null) {
			return null;
		}
		File dir = new File(path);
		if (!dir.isDirectory()) {
			abort(
				"Specified working directory does not exist or is not a directory",
				null,
				ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_DOES_NOT_EXIST);
		}
		return dir;
	}

	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), code, message, exception));
	}

	public ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
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

	public String getProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
	}

	public String getProgramName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String) null);
	}

	/**
	 * Returns the program arguments as a String.
	 *
	 * @return the program arguments as a String
	 */
	public String getProgramArguments(ILaunchConfiguration config) throws CoreException {
		return config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String) null);

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
				while (Character.isWhitespace((char) ch))
					ch = getNext();

				if (ch == '"') {
					v.add(parseString());
				}
				else {
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
					buf.append((char) ch);
					ch = getNext();
				}
			}

			ch = getNext();

			return buf.toString();
		}

		private String parseToken() {
			StringBuffer buf = new StringBuffer();

			while (ch > 0 && !Character.isWhitespace((char) ch)) {
				if (ch == '\\') {
					ch = getNext();
					if (ch > 0) {
						if (ch != '"') { // Only escape double quotes
							buf.append('\\');
						}
						buf.append((char) ch);
						ch = getNext();
					}
					else if (ch == -1) { // Don't lose a trailing backslash
						buf.append('\\');
					}
				}
				else if (ch == '"') {
					buf.append(parseString());
				}
				else {
					buf.append((char) ch);
					ch = getNext();
				}
			}
			return buf.toString();
		}
	}
}
