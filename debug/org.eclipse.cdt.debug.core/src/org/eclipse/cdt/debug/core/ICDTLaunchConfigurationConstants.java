/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others. All rights
 * reserved. This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.debug.core;

public interface ICDTLaunchConfigurationConstants {

	public static final String CDT_LAUNCH_ID = "org.eclipse.cdt.launch"; //$NON-NLS-1$

	/**
	 * This is the launch type id.
	 */
	public static final String ID_LAUNCH_C_APP = "org.eclipse.cdt.launch.localCLaunch"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a name of a C/C++
	 * project associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_PROJECT_NAME = CDT_LAUNCH_ID + ".PROJECT_ATTR"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a string specifying
	 * application a C/C++ launch configuration.
	 */
	public static final String ATTR_PROGRAM_NAME = CDT_LAUNCH_ID + ".PROGRAM_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a string specifying
	 * application arguments for a C/C++ launch configuration, as they should
	 * appear on the command line.
	 */
	public static final String ATTR_PROGRAM_ARGUMENTS = CDT_LAUNCH_ID + ".PROGRAM_ARGUMENTS"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a string specifying a
	 * path to the working directory to use when launching a the application.
	 * When unspecified, the working directory is inherited from the current
	 * process. When specified as an absolute path, the path represents a path
	 * in the local file system. When specified as a full path, the path
	 * represents a workspace relative path.
	 */
	public static final String ATTR_WORKING_DIRECTORY = CDT_LAUNCH_ID + ".WORKING_DIRECTORY"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether the current enviroment should be inherited when the application
	 * is launched.
	 * @deprecated - see ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES
	 */
	public static final String ATTR_PROGRAM_ENVIROMENT_INHERIT = CDT_LAUNCH_ID + ".ENVIRONMENT_INHERIT"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a Map specifying the
	 * environment to use when launching a C/C++ application.
	 * 
	 * @deprecated - see ILaunchManager.ATTR_ENVIRONMENT_VARIABLES
	 */
	public static final String ATTR_PROGRAM_ENVIROMENT_MAP = CDT_LAUNCH_ID + ".ENVIRONMENT_MAP"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the platform string of
	 * the launch configuration
	 */
	public static final String ATTR_PLATFORM = CDT_LAUNCH_ID + ".PLATFFORM"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean specifiying whether
	 * to connect a terminal to the processed stdin/stdout
	 */
	public static final String ATTR_USE_TERMINAL = CDT_LAUNCH_ID + ".use_terminal";

	/**
	 * Launch configuration attribute key. The value is the debugger id used
	 * when launching a C/C++ application for debug.
	 */
	public static final String ATTR_DEBUGGER_ID = CDT_LAUNCH_ID + ".DEBUGGER_ID"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the platform string of
	 * the launch configuration
	 */
	public static final String ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP = CDT_LAUNCH_ID + ".DEBUGGER_SPECIFIC_ATTRS_MAP"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether to stop at main().
	 */
	public static final String ATTR_DEBUGGER_STOP_AT_MAIN = CDT_LAUNCH_ID + ".DEBUGGER_STOP_AT_MAIN"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is an int specifying the
	 * process id to attach to if the ATTR_DEBUGGER_START_MODE is
	 * DEBUGGER_MODE_ATTACH. A non existant value or -1 for this entry indicates
	 * that the user should be asked to supply this value by the launch
	 * delegate. This value is primarily designed to be used by programatic
	 * users of the debug interface.
	 */
	public static final String ATTR_ATTACH_PROCESS_ID = CDT_LAUNCH_ID + ".ATTACH_PROCESS_ID"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a String specifying the
	 * corefile path if the ATTR_DEBUGGER_START_MODE is DEBUGGER_MODE_COREFILE.
	 * A non existant value or null for this entry indicates that the user
	 * should be asked to supply this value by the launch delegate. This value
	 * is primarily designed to be used by programatic users of the debug
	 * interface.
	 */
	public static final String ATTR_COREFILE_PATH = CDT_LAUNCH_ID + ".COREFILE_PATH"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the startup mode for the
	 * debugger.
	 */
	public static final String ATTR_DEBUGGER_START_MODE = CDT_LAUNCH_ID + ".DEBUGGER_START_MODE"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether to enable variable bookkeeping.
	 */
	public static final String ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING = CDT_LAUNCH_ID + ".ENABLE_VARIABLE_BOOKKEEPING"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether to enable register bookkeeping.
	 */
	public static final String ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING = CDT_LAUNCH_ID + ".ENABLE_REGISTER_BOOKKEEPING"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a global variables'
	 * memento.
	 */
	public static final String ATTR_DEBUGGER_GLOBAL_VARIABLES = CDT_LAUNCH_ID + ".GLOBAL_VARIABLES"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_STOP_AT_MAIN.
	 */
	public static boolean DEBUGGER_STOP_AT_MAIN_DEFAULT = true;

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_START_MODE. Startup debugger running the program.
	 */
	public static String DEBUGGER_MODE_RUN = "run"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_START_MODE. Startup debugger and attach to running process.
	 */
	public static String DEBUGGER_MODE_ATTACH = "attach"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_START_MODE. Startup debugger to view a core file.
	 */
	public static String DEBUGGER_MODE_CORE = "core"; //$NON-NLS-1$

	/**
	 * Status code indicating that the Eclipse runtime does not support
	 * launching a program with a working directory. This feature is only
	 * available if Eclipse is run on a 1.3 runtime or higher.
	 * <p>
	 * A status handler may be registered for this error condition, and should
	 * return a Boolean indicating whether the program should be relaunched with
	 * the default working directory.
	 * </p>
	 */
	public static final int ERR_WORKING_DIRECTORY_NOT_SUPPORTED = 100;

	/**
	 * Status code indicating the specified working directory does not exist.
	 */
	public static final int ERR_WORKING_DIRECTORY_DOES_NOT_EXIST = 101;

	/**
	 * Status code indicating a launch configuration does not specify a project
	 * when a project is required.
	 */
	public static final int ERR_UNSPECIFIED_PROJECT = 102;

	/**
	 * Status code indicating a launch configuration does not specify a vaild
	 * project.
	 */
	public static final int ERR_NOT_A_C_PROJECT = 103;

	/**
	 * Status code indicating a launch configuration does not specify a vaild
	 * program.
	 */
	public static final int ERR_PROGRAM_NOT_EXIST = 104;

	/**
	 * Status code indicating a launch configuration does not specify a program
	 * name.
	 */

	public static final int ERR_UNSPECIFIED_PROGRAM = 105;

	/**
	 * Status code indicating that the CDT debugger is missing
	 * <p>
	 * A status handler may be registered for this error condition, and should
	 * return a String indicating which debugger to use.
	 * </p>
	 */
	public static final int ERR_DEBUGGER_NOT_INSTALLED = 106;

	/**
	 * Status code indicating a launch configuration does not specify a vaild
	 * program.
	 */
	public static final int ERR_PROGRAM_NOT_BINARY = 107;

	/**
	 * Status code indicating a the user did not specify a process id
	 */
	public static final int ERR_NO_PROCESSID = 107;

	/**
	 * Status code indicating a the user did not specify a path to a corefile
	 */
	public static final int ERR_NO_COREFILE = 108;

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int ERR_INTERNAL_ERROR = 150;

}