/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson   - Initial API and implementation
 *     Ericsson   - Added support for Mac OS
 *     Sergey Prigogin (Google)
 *     Marc Khouzam (Ericsson) - Add timer when fetching GDB version (Bug 376203)
 *     Marc Khouzam (Ericsson) - Better error reporting when obtaining GDB version (Bug 424996)
 *     Iulia Vasii (Freescale Semiconductor) - Separate GDB command from its arguments (Bug 445360)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public class LaunchUtils {
	
   	/**
	 * Verify the following things about the project:
	 * - is a valid project name given
	 * - does the project exist
	 * - is the project open
	 * - is the project a C/C++ project
	 */
	public static ICProject verifyCProject(ILaunchConfiguration configuration) throws CoreException {
		String name = getProjectName(configuration);
		if (name == null) {
			abort(LaunchMessages.getString("AbstractCLaunchDelegate.C_Project_not_specified"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
			return null;
		}
		ICProject cproject = getCProject(configuration);
		if (cproject == null && !name.isEmpty()) {
			IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (!proj.exists()) {
				abort(LaunchMessages.getFormattedString("AbstractCLaunchDelegate.Project_NAME_does_not_exist", name), null, //$NON-NLS-1$
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
	 * Verify that program name of the configuration can be found as a file.
	 * 
	 * @return Absolute path of the program location
	 */
	public static IPath verifyProgramPath(ILaunchConfiguration configuration, ICProject cproject) throws CoreException {
		String programName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
		if (programName == null) {
			// Auto-detect executable
			if (cproject != null) {
				// Check if there is only one executable in the project. If so, use that as the program path.
				List<IBinary> executables = CDebugUtils.getExecutables(cproject.getProject());
				if (executables.size() == 1) {
					ILaunchConfigurationWorkingCopy workingCopy = configuration.getWorkingCopy();
					programName = executables.get(0).getResource().getProjectRelativePath().toString();
					workingCopy.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, programName);
					configuration = workingCopy.doSave();
				}
			}

			if (programName == null) {
				abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_not_specified"), null, //$NON-NLS-1$
					  ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROGRAM);
			}
		}
        programName = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(programName);

		IPath programPath = new Path(programName);    			 
		if (programPath.isEmpty()) {
			abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_does_not_exist"), null, //$NON-NLS-1$
				  ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
		
		if (!programPath.isAbsolute() && cproject != null) {
			// Find the specified program within the specified project
   			IFile wsProgramPath = cproject.getProject().getFile(programPath);
   			programPath = wsProgramPath.getLocation();
		}
		
		if (!programPath.toFile().exists()) {
			abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_does_not_exist"), //$NON-NLS-1$
				  new FileNotFoundException(
						  LaunchMessages.getFormattedString("AbstractCLaunchDelegate.PROGRAM_PATH_not_found",  //$NON-NLS-1$ 
						                                    programPath.toOSString())),
				  ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
		
		return programPath;
	}

	/**
	 * Verify that the executable path points to a valid binary file.
	 * 
	 * @return An object representing the binary file. 
	 */
	public static IBinaryObject verifyBinary(ILaunchConfiguration configuration, IPath exePath) throws CoreException {
		ICProject cproject = getCProject(configuration); 
		if (cproject != null) {
			ICConfigExtensionReference[] parserRefs = CCorePlugin.getDefault().getDefaultBinaryParserExtensions(cproject.getProject());
			for (ICConfigExtensionReference parserRef : parserRefs) {
				try {
					IBinaryParser parser = CoreModelUtil.getBinaryParser(parserRef);
					IBinaryObject exe = (IBinaryObject)parser.getBinary(exePath);
					if (exe != null) {
						return exe;
					}
				} catch (ClassCastException e) {
				} catch (IOException e) {
				}
			}
		}

		IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
		try {
			return (IBinaryObject)parser.getBinary(exePath);
		} catch (ClassCastException e) {
		} catch (IOException e) {
		}
		
		abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_is_not_a_recognized_executable"), //$NON-NLS-1$
				  new FileNotFoundException(
						  LaunchMessages.getFormattedString("AbstractCLaunchDelegate.Program_is_not_a_recognized_executable",  //$NON-NLS-1$
								  						    exePath.toOSString())),
		          ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_BINARY);
		
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
	private static void abort(String message, Throwable exception, int code) throws CoreException {
		MultiStatus status = new MultiStatus(GdbPlugin.PLUGIN_ID, code, message, exception);
		status.add(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, code, 
				              exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
				              exception));
		throw new CoreException(status);
	}

	/**
	 * Returns an ICProject based on the project name provided in the configuration.
	 * First look for a project by name, and then confirm it is a C/C++ project.
	 */
	public static ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (!projectName.isEmpty()) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
				if (cProject != null && cProject.exists()) {
					return cProject;
				}
			}
		}
		return null;
	}

	private static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}
	
    public static IPath getGDBPath(ILaunchConfiguration configuration) {
		String defaultGdbCommand = Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
                IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND,
                IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT, null);

        IPath retVal = new Path(defaultGdbCommand);
        try {
        	String gdb = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, defaultGdbCommand);
        	gdb = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(gdb, false);        	
        	retVal = new Path(gdb);
        } catch (CoreException e) {
        }
        return retVal;
    }

    /**
     * Find gdb version info from a string object which is supposed to
     * contain output text of "gdb --version" command.
     *   
     * @param versionOutput 
     * 		output text from "gdb --version" command .
     * @return 
     * 		String representation of version of gdb such as "6.8" on success;
     *      empty string otherwise.
     * @since 2.0
     */
	public static String getGDBVersionFromText(String versionOutput) {
        String version = "";//$NON-NLS-1$
        
		// These are the GDB version patterns I have seen up to now
		// The pattern works for all of them extracting the version of 6.8.50.20080730
		// GNU gdb 6.8.50.20080730
		// GNU gdb (GDB) 6.8.50.20080730-cvs
		// GNU gdb (Ericsson GDB 1.0-10) 6.8.50.20080730-cvs
        // GNU gdb (GDB) Fedora (7.0-3.fc12)
        // GNU gdb Red Hat Linux (6.3.0.0-1.162.el4rh)
        // GNU gdb (GDB) STMicroelectronics/Linux Base 7.4-71 [build Mar  1 2013]

        Pattern pattern = Pattern.compile(" gdb( \\(.*?\\))? (\\D* )*\\(?(\\d*(\\.\\d*)*)",  Pattern.MULTILINE); //$NON-NLS-1$

		Matcher matcher = pattern.matcher(versionOutput);
		if (matcher.find()) {
			version = matcher.group(3);
			// Temporary for cygwin, until GDB 7 is released
			// Any cygwin GDB staring with 6.8 should be treated as plain 6.8
			if (versionOutput.toLowerCase().indexOf("cygwin") != -1 && //$NON-NLS-1$
					version.startsWith("6.8")) { //$NON-NLS-1$
				version = "6.8"; //$NON-NLS-1$
			}
		}

        return version;
	}
	
	/**
	 * This method actually launches 'gdb --version' to determine the version
	 * of the GDB that is being used.  This method should ideally be called
	 * only once per session and the resulting version string stored for future uses.
	 * 
	 * A timeout is scheduled which will kill the process if it takes too long.
	 * 
 	 * @deprecated Replaced with GdbLaunch.getLaunchEnvironment()
	 */
	@Deprecated
	public static String getGDBVersion(final ILaunchConfiguration configuration) throws CoreException {        
        String cmd = getGDBPath(configuration).toOSString() + " --version"; //$NON-NLS-1$
        
        // Parse cmd to properly handle spaces and such things (bug 458499)
		String[] args = CommandLineUtil.argumentsToArray(cmd);
        
        Process process = null;
        Job timeoutJob = null;
        try {
        	process = ProcessFactory.getFactory().exec(args, getLaunchEnvironment(configuration));

            // Start a timeout job to make sure we don't get stuck waiting for
            // an answer from a gdb that is hanging
            // Bug 376203
        	final Process finalProc = process;
            timeoutJob = new Job("GDB version timeout job") { //$NON-NLS-1$
    			{ setSystem(true); }
    			@Override
    			protected IStatus run(IProgressMonitor arg) {
    				// Took too long.  Kill the gdb process and 
    				// let things clean up.
    	        	finalProc.destroy();
    				return Status.OK_STATUS;
    			}
    		};
    		timeoutJob.schedule(10000);

        	String streamOutput = readStream(process.getInputStream());

        	String gdbVersion = getGDBVersionFromText(streamOutput);
        	if (gdbVersion == null || gdbVersion.isEmpty()) {
        		Exception detailedException = null;
        		if (!streamOutput.isEmpty()) {
        			// We got some output but couldn't parse it.  Make that output visible to the user in the error dialog.
        			detailedException = new Exception("Unexpected output format: \n\n" + streamOutput);  //$NON-NLS-1$        		
        		} else {
        			// We got no output.  Check if we got something on the error stream.
        			streamOutput = readStream(process.getErrorStream());
        			if (!streamOutput.isEmpty()) {
        				detailedException = new Exception(streamOutput);
        			}
        		}
        		
        		throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, 
        				"Could not determine GDB version using command: " + StringUtil.join(args, " "), //$NON-NLS-1$ //$NON-NLS-2$ 
        				detailedException));
        	}
        	return gdbVersion;
        } catch (IOException e) {
        	throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, 
        			"Error with command: " + StringUtil.join(args, " "), e));//$NON-NLS-1$ //$NON-NLS-2$
        } finally {
        	// If we get here we are obviously not stuck reading the stream so we can cancel the timeout job.
        	// Note that it may already have executed, but that is not a problem.
        	if (timeoutJob != null) {
        		timeoutJob.cancel();
        	}

        	if (process != null) {
        		process.destroy();
        	}
        }
	}
	
	/**
	 * Compares two version numbers.
	 * Returns -1, 0, or 1 if v1 is less than, equal to, or greater than v2, respectively.
	 * @param v1 The first version
	 * @param v2 The second version
	 * @return -1, 0, or 1 if v1 is less than, equal to, or greater than v2, respectively.
	 * @since 4.8
	 */
	public static int compareVersions(String v1, String v2) {
		if (v1 == null || v2 == null) throw new NullPointerException();
		
		String[] v1Parts = v1.split("\\."); //$NON-NLS-1$
		String[] v2Parts = v2.split("\\."); //$NON-NLS-1$
		for (int i = 0; i < v1Parts.length && i < v2Parts.length; i++) {			
			try {
				int v1PartValue = Integer.parseInt(v1Parts[i]);
				int v2PartValue = Integer.parseInt(v2Parts[i]);

				if (v1PartValue > v2PartValue) {
					return 1;
				} else if (v1PartValue < v2PartValue) {
					return -1;
				}
			} catch (NumberFormatException e) {
				// Non-integer part, ignore it
				continue;
			}
		}
		
		// If we get here is means the versions are still equal
		// but there could be extra parts to examine
		
		if (v1Parts.length < v2Parts.length) {
			// v2 has extra parts, which implies v1 is a lower version (e.g., v1 = 7.9 v2 = 7.9.1)
			// unless each extra part is 0, in which case the two versions are equal (e.g., v1 = 7.9 v2 = 7.9.0)
			for (int i = v1Parts.length; i < v2Parts.length; i++) {
				try {
					if (Integer.parseInt(v2Parts[i]) != 0) {
						return -1;
					}
				} catch (NumberFormatException e) {
					// Non-integer part, ignore it
					continue;
				}
			}
		}
		if (v1Parts.length > v2Parts.length) {
			// v1 has extra parts, which implies v1 is a higher version (e.g., v1 = 7.9.1 v2 = 7.9)
			// unless each extra part is 0, in which case the two versions are equal (e.g., v1 = 7.9.0 v2 = 7.9)
			for (int i = v2Parts.length; i < v1Parts.length; i++) {
				try {
					if (Integer.parseInt(v1Parts[i]) != 0) {
						return 1;
					}
				} catch (NumberFormatException e) {
					// Non-integer part, ignore it
					continue;
				}
			}
		}

		return 0;
	}

	/**
	 * Read from the specified stream and return what was read.
	 * 
	 * @param stream The input stream to be used to read the data.  This method will close the stream.
	 * @return The data read from the stream
	 * @throws IOException If an IOException happens when reading the stream
	 */
	private static String readStream(InputStream stream) throws IOException {
        StringBuilder cmdOutput = new StringBuilder(200);
        try {
        	Reader r = new InputStreamReader(stream);
        	BufferedReader reader = new BufferedReader(r);
        	
        	String line;
        	while ((line = reader.readLine()) != null) {
        		cmdOutput.append(line);
        		cmdOutput.append('\n');
        	}
        	return cmdOutput.toString();
        } finally {
        	// Cleanup to avoid leaking pipes
        	// Bug 345164
        	if (stream != null) {
				try { 
					stream.close(); 
				} catch (IOException e) {}
        	}
        }
	}
        
	public static boolean getIsAttach(ILaunchConfiguration config) {
    	try {
    		String debugMode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
    		if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
    			return false;
    		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
    			return true;
    		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
    			return false;
    		} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE)) {
    			return false;
    		} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE_ATTACH)) {
    		    return true;
    	    }
    	} catch (CoreException e) {    		
    	}
    	return false;
    }
	
	public static SessionType getSessionType(ILaunchConfiguration config) {
    	try {
    		String debugMode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
    		if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
    			return SessionType.LOCAL;
    		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
    			return SessionType.LOCAL;
    		} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
    			return SessionType.CORE;
    		} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE)) {
    			return SessionType.REMOTE;
    		} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE_ATTACH)) {
    		    return SessionType.REMOTE;
    	    } else {
    	    	assert false : "Unexpected session-type attribute in launch config: " + debugMode;  //$NON-NLS-1$
    	    }
    	} catch (CoreException e) {    		
    	}
    	return SessionType.LOCAL;
    }
	
	/**
	 * Gets the CDT environment from the CDT project's configuration referenced by the
	 * launch
	 * @since 3.0
	 * @deprecated Replaced with GdbLaunch.getLaunchEnvironment()
	 */
	@Deprecated
	public static String[] getLaunchEnvironment(ILaunchConfiguration config) throws CoreException {
		// Get the project
		String projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		if (projectName == null) {
			return null;
		}
		projectName = projectName.trim();
		if (projectName.length() == 0) {
			return null;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null || !project.isAccessible()) {
			return null;
		}

		ICProjectDescription projDesc =	CoreModel.getDefault().getProjectDescription(project, false);

		// Not a CDT project?
		if (projDesc == null) {
		    return null;
		}

		String buildConfigID = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, ""); //$NON-NLS-1$
		ICConfigurationDescription cfg = null;
		if (buildConfigID.length() != 0) {
		    cfg = projDesc.getConfigurationById(buildConfigID);
		}

		// if configuration is null fall-back to active
		if (cfg == null) {
		    cfg = projDesc.getActiveConfiguration();
		}

		// Environment variables and inherited vars
		HashMap<String, String> envMap = new HashMap<String, String>();
		IEnvironmentVariable[] vars = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cfg, true);
		for (IEnvironmentVariable var : vars) {
			envMap.put(var.getName(), var.getValue());
		}

		// Add variables from build info
		ICdtVariable[] buildVars = CCorePlugin.getDefault().getCdtVariableManager().getVariables(cfg);
		for (ICdtVariable var : buildVars) {
			try {
				// The project_classpath variable contributed by JDT is useless for running C/C++
				// binaries, but it can be lethal if it has a very large value that exceeds shell
				// limit. See http://bugs.eclipse.org/bugs/show_bug.cgi?id=408522
				if (!"project_classpath".equals(var.getName())) {//$NON-NLS-1$
					envMap.put(var.getName(), var.getStringValue());
				}
			} catch (CdtVariableException e) {
				// Some Eclipse dynamic variables can't be resolved dynamically... we don't care.
			}
		}

		// Turn it into an envp format
		List<String> strings= new ArrayList<String>(envMap.size());
		for (Entry<String, String> entry : envMap.entrySet()) {
			StringBuffer buffer= new StringBuffer(entry.getKey());
			buffer.append('=').append(entry.getValue());
			strings.add(buffer.toString());
		}

		return strings.toArray(new String[strings.size()]);
	}
	
	/**
	 * Returns <code>true</code> if the launch is meant to be in Non-Stop mode.
	 * Returns <code>false</code> otherwise.
	 * 
	 * @since 4.0
	 */
	public static boolean getIsNonStopMode(ILaunchConfiguration config) {
		try {
			return config.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
					                   getIsNonStopModeDefault());
    	} catch (CoreException e) {    		
    	}
    	return false;
    }
	
	/**
	 * Returns workspace-level default for the Non-Stop mode.
	 * 
	 * @since 4.0
	 */
	public static boolean getIsNonStopModeDefault() {
		return Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_DEFAULT_NON_STOP,
				IGDBLaunchConfigurationConstants.DEBUGGER_NON_STOP_DEFAULT, null);
    }
	
	/**
	 * Returns workspace-level default for the stop at main option.
	 * 
	 * @since 4.0
	 */
	public static boolean getStopAtMainDefault() {
		return Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN,
				ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_DEFAULT, null);
    }
	
	/**
	 * Returns workspace-level default for the stop at main symbol.
	 * 
	 * @since 4.0
	 */
	public static String getStopAtMainSymbolDefault() {
		return Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL,
				ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT, null);
    }
	
	/**
	 * Returns <code>true</code> if the launch is meant to be for post-mortem
	 * tracing.  Returns <code>false</code> otherwise.
	 * 
	 * @since 4.0
	 */
	public static boolean getIsPostMortemTracing(ILaunchConfiguration config) {
		SessionType sessionType = LaunchUtils.getSessionType(config);
		if (sessionType == SessionType.CORE) {
			try {
				String coreType = config.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_POST_MORTEM_TYPE,
						IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TYPE_DEFAULT);
				return coreType.equals(IGDBLaunchConfigurationConstants.DEBUGGER_POST_MORTEM_TRACE_FILE);
			} catch (CoreException e) {    		
			}
		}
    	return false;
    }
}

