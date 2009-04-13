/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ericsson   - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;

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
		if (cproject == null && name.length() > 0) {
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
	 * Verify that program name of the configuration can be found as a file.
	 * 
	 * @return Absolute path of the program location
	 */
	public static IPath verifyProgramPath(ILaunchConfiguration configuration, ICProject cproject) throws CoreException {
		String programName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
		if (programName == null) {
			abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_not_specified"), null, //$NON-NLS-1$
				  ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
		}
		
		IPath programPath = new Path(programName);    			 
		if (programPath.isEmpty()) {
			abort(LaunchMessages.getString("AbstractCLaunchDelegate.Program_file_does_not_exist"), null, //$NON-NLS-1$
				  ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
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
		ICExtensionReference[] parserRefs = CCorePlugin.getDefault().getBinaryParserExtensions(getCProject(configuration).getProject());
		for (ICExtensionReference parserRef : parserRefs) {
			try {
				IBinaryParser parser = (IBinaryParser)parserRef.createExtension();
				IBinaryObject exe = (IBinaryObject)parser.getBinary(exePath);
				if (exe != null) {
					return exe;
				}
			} catch (ClassCastException e) {
			} catch (IOException e) {
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

	private static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}
	
    public static IPath getGDBPath(ILaunchConfiguration configuration) {
        IPath retVal = new Path(IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
        try {
            retVal = new Path(configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, 
            		                                     IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT));
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
		Pattern pattern = Pattern.compile(" gdb( \\(.*?\\))? (\\d*(\\.\\d*)*)",  Pattern.MULTILINE); //$NON-NLS-1$

		Matcher matcher = pattern.matcher(versionOutput);
		if (matcher.find()) {
			version = matcher.group(2);
			// Temporary for cygwin, until GDB 7 is released
			// Any cygwin GDB staring with 6.8 should be treated as plain 6.8
			if (versionOutput.toLowerCase().indexOf("cygwin") != -1 && //$NON-NLS-1$
					version.startsWith("6.8")) { //$NON-NLS-1$
				version = "6.8"; //$NON-NLS-1$
			}
		}

        return version;
	}
	
	public static String getGDBVersion(final ILaunchConfiguration configuration) throws CoreException {        
        Process process = null;
        String cmd = getGDBPath(configuration).toOSString() + " --version"; //$NON-NLS-1$ 
        try {                        
        	process = ProcessFactory.getFactory().exec(cmd);
        } catch(IOException e) {
        	throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, 
        			"Error while launching command: " + cmd, e.getCause()));//$NON-NLS-1$
        }

        StringBuilder cmdOutput = new StringBuilder(200);
        try {
        	InputStream stream = process.getInputStream();
        	Reader r = new InputStreamReader(stream);
        	BufferedReader reader = new BufferedReader(r);
        	
        	String line;
        	while ((line = reader.readLine()) != null) {
        		cmdOutput.append(line);
        	}
        } catch (IOException e) {
        	throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, 
        			"Error reading GDB STDOUT after sending: " + cmd, e.getCause()));//$NON-NLS-1$
        }

        return getGDBVersionFromText(cmdOutput.toString());
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
    	    }
    	} catch (CoreException e) {    		
    	}
    	return SessionType.LOCAL;
    }
}

