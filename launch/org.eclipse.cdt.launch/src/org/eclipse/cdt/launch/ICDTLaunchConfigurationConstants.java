package org.eclipse.cdt.launch;

/*
 * (c) Copyright QNX Software System 2002.
 * All Rights Reserved.
 */

import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;

public interface ICDTLaunchConfigurationConstants {
	/**
	 * Launch configuration attribute key. The value is a name of
	 * a C/C++ project associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_PROJECT_NAME = LaunchUIPlugin.getUniqueIdentifier() + ".PROJECT_ATTR"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a string specifying
	 * application a C/C++ launch configuration.
	 */
	public static final String ATTR_PROGRAM_NAME = LaunchUIPlugin.getUniqueIdentifier() + ".PROGRAM_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a string specifying
	 * application arguments for a C/C++ launch configuration, as they should appear
	 * on the command line.
	 */
	public static final String ATTR_PROGRAM_ARGUMENTS = LaunchUIPlugin.getUniqueIdentifier() + ".PROGRAM_ARGUMENTS"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a string specifying a
	 * path to the working directory to use when launching a the application.
	 * When unspecified, the working directory is inherited from the current process.
	 * When specified as an absolute path, the path represents a path in the local
	 * file system. When specified as a full path, the path represents a workspace
	 * relative path.
	 */
	public static final String ATTR_WORKING_DIRECTORY = LaunchUIPlugin.getUniqueIdentifier() + ".WORKING_DIRECTORY"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean specifying 
	 * whether the current enviroment should be inherited when the application
	 * is launched. 
	 */
	public static final String ATTR_PROGRAM_ENVIROMENT_INHERIT = LaunchUIPlugin.getUniqueIdentifier() + ".ENVIRONMENT_INHERIT"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a Map specifying the
	 * environment to use when launching a C/C++ application. 
	 */
	public static final String ATTR_PROGRAM_ENVIROMENT_MAP = LaunchUIPlugin.getUniqueIdentifier() + ".ENVIRONMENT_MAP"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute key. The value is the platform string of the launch configuration
	 */
	public static final String ATTR_PLATFORM = LaunchUIPlugin.getUniqueIdentifier() + ".PLATFFORM"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the debugger id
	 * used when launching a C/C++ application for debug.
	 */
	public static final String ATTR_DEBUGGER_ID = LaunchUIPlugin.getUniqueIdentifier() + ".DEBUGGER_ID"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the platform string of the launch configuration
	 */
	public static final String ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP = LaunchUIPlugin.getUniqueIdentifier() + ".DEBUGGER_SPECIFIC_ATTRS_MAP"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute key. The value is a boolean specifying whether to stop at main().
	 */
	public static final String ATTR_DEBUGGER_STOP_AT_MAIN = LaunchUIPlugin.getUniqueIdentifier() + ".DEBUGGER_STOP_AT_MAIN"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the startup mode for the debugger.
	 */
	public static final String ATTR_DEBUGGER_START_MODE = LaunchUIPlugin.getUniqueIdentifier() + ".DEBUGGER_START_MODE"; //$NON-NLS-1$
	
	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_START_MODE.
	 * Startup debugger running the program.
	 */
	public static String DEBUGGER_MODE_RUN = "run";

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_START_MODE.
	 * Startup debugger and attach to running process.
	 */
	public static String DEBUGGER_MODE_ATTACH = "attach";

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_START_MODE.
	 * Startup debugger to view a core file.
	 */
	public static String DEBUGGER_MODE_CORE = "core";

	/**
	 * Status code indicating that the Eclipse runtime does not support
	 * launching a program with a working directory. This feature is only
	 * available if Eclipse is run on a 1.3 runtime or higher.
	 * <p>
	 * A status handler may be registered for this error condition,
	 * and should return a Boolean indicating whether the program
	 * should be relaunched with the default working directory.
	 * </p>
	 */
	public static final int ERR_WORKING_DIRECTORY_NOT_SUPPORTED = 100;
	
	/**
	 * Status code indicating the specified working directory
	 * does not exist.
	 */
	public static final int ERR_WORKING_DIRECTORY_DOES_NOT_EXIST = 101;	

	/**
	 * Status code indicating that the CDT debugger is missing
	 * <p>
	 * A status handler may be registered for this error condition,
	 * and should return a String indicating which debugger to use.
	 * </p>
	 */
	public static final int ERR_DEBUGGER_NOT_INSTALLED = 102;

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int ERR_INTERNAL_ERROR = 150;			
}
