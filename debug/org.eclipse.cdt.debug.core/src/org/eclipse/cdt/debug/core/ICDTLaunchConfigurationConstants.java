/*******************************************************************************
 * Copyright (c) 2000, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Ken Ryall (Nokia) - bug 118894
 *     Ken Ryall (Nokia) - bug 178731
 *     Alex Collins (Broadcom Corp.) - choose build config automatically
 *     Marc Khouzam (Ericsson) - Migrate dsf.gdb's CMainTab to cdt.launch CMainTab2
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

/**
 * Constants used for attributes in CDT launch configurations.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICDTLaunchConfigurationConstants {

	public static final String CDT_LAUNCH_ID = "org.eclipse.cdt.launch"; //$NON-NLS-1$

	/**
	 * This is the application launch type id.
	 */
	public static final String ID_LAUNCH_C_APP = "org.eclipse.cdt.launch.applicationLaunchType"; //$NON-NLS-1$

	/**
	 * This is the remote application launch type id.
	 *
	 * @since 7.1
	 */
	public static final String ID_LAUNCH_C_REMOTE_APP = "org.eclipse.cdt.launch.remoteApplicationLaunchType"; //$NON-NLS-1$

	/**
	 * This is the attach launch type id.
	 *
	 * @since 6.0
	 */
	public static final String ID_LAUNCH_C_ATTACH = "org.eclipse.cdt.launch.attachLaunchType"; //$NON-NLS-1$

	/**
	 * This is the post-mortem launch type id.
	 *
	 * @since 6.0
	 */
	public static final String ID_LAUNCH_C_POST_MORTEM = "org.eclipse.cdt.launch.postmortemLaunchType"; //$NON-NLS-1$

	/**
	 * Specifies the default launch delegate for a Local Debug session
	 * @since 7.0
	 */
	public static final String PREFERRED_DEBUG_LOCAL_LAUNCH_DELEGATE = "org.eclipse.cdt.dsf.gdb.launch.localCLaunch"; //$NON-NLS-1$

	/**
	 * Specifies the default launch delegate for a Remote Debug session.
	 * This default is part of the optional plugin org.eclipse.cdt.launch.remote.  If that plugin is not installed
	 * then we won't set a default, which is ok since we only have one other delegate
	 * (which is org.eclipse.cdt.dsf.gdb.launch.remoteCLaunch).
	 * @since 7.1
	 */
	public static final String PREFERRED_DEBUG_REMOTE_LAUNCH_DELEGATE = "org.eclipse.rse.remotecdt.dsf.debug"; //$NON-NLS-1$

	/**
	 * Specifies the default launch delegate for an Attach Debug session
	 * @since 7.0
	 */
	public static final String PREFERRED_DEBUG_ATTACH_LAUNCH_DELEGATE = "org.eclipse.cdt.dsf.gdb.launch.attachCLaunch"; //$NON-NLS-1$

	/**
	 * Specifies the default launch delegate for a Post Mortem Debug session
	 * @since 7.0
	 */
	public static final String PREFERRED_DEBUG_POSTMORTEM_LAUNCH_DELEGATE = "org.eclipse.cdt.dsf.gdb.launch.coreCLaunch"; //$NON-NLS-1$

	/**
	 * Specifies the default launch delegate for a Run mode session
	 * @since 7.0
	 */
	public static final String PREFERRED_RUN_LAUNCH_DELEGATE = "org.eclipse.cdt.cdi.launch.localCLaunch"; //$NON-NLS-1$

	/**
	 * Identifier for the C/C++ program process type, which is annotated on processes created
	 * by the C/C++ application launch delegate.
	 *
	 * (value <code>"C/C++"</code>).
	 */
	public static final String ID_PROGRAM_PROCESS_TYPE = "C/C++"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a name of a C/C++
	 * project associated with a C/C++ launch configuration.
	 */
	public static final String ATTR_PROJECT_NAME = CDT_LAUNCH_ID + ".PROJECT_ATTR"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value constants for build before launch.
	 *
	 * @since 7.0
	 */
	public static final int BUILD_BEFORE_LAUNCH_DISABLED = 0;
	/** @since 7.0 */
	public static final int BUILD_BEFORE_LAUNCH_ENABLED = 1;
	/** @since 7.0 */
	public static final int BUILD_BEFORE_LAUNCH_USE_WORKSPACE_SETTING = 2;

	/**
	 * Launch configuration attribute key. The value is the ID of the project's
	 * build configuration that should be used when a build is required before launch.
	 */
	/** @since 7.0 */
	public static final String ATTR_BUILD_BEFORE_LAUNCH = CDT_LAUNCH_ID + ".ATTR_BUILD_BEFORE_LAUNCH_ATTR"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the ID of the project's
	 * build configuration that should be used when a build is required before launch.
	 */
	public static final String ATTR_PROJECT_BUILD_CONFIG_ID = CDT_LAUNCH_ID + ".PROJECT_BUILD_CONFIG_ID_ATTR"; //$NON-NLS-1$

	/**
	 * Automatically choose build configuration for launch key. The value indicates whether the ID of the build configuration
	 * to be built before launch should be calculated based on the path to the application being launched.
	 * @since 7.1
	 */
	public static final String ATTR_PROJECT_BUILD_CONFIG_AUTO = CDT_LAUNCH_ID + ".PROJECT_BUILD_CONFIG_AUTO_ATTR"; //$NON-NLS-1$

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
	@Deprecated
	public static final String ATTR_PROGRAM_ENVIROMENT_INHERIT = CDT_LAUNCH_ID + ".ENVIRONMENT_INHERIT"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a Map specifying the
	 * environment to use when launching a C/C++ application.
	 *
	 * @deprecated - see ILaunchManager.ATTR_ENVIRONMENT_VARIABLES
	 */
	@Deprecated
	public static final String ATTR_PROGRAM_ENVIROMENT_MAP = CDT_LAUNCH_ID + ".ENVIRONMENT_MAP"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the platform string of
	 * the launch configuration
	 */
	public static final String ATTR_PLATFORM = CDT_LAUNCH_ID + ".PLATFFORM"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean specifying whether
	 * to connect a terminal to the processed stdin/stdout
	 */
	public static final String ATTR_USE_TERMINAL = CDT_LAUNCH_ID + ".use_terminal"; //$NON-NLS-1$

	public static final boolean USE_TERMINAL_DEFAULT = true;

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
	 * Launch configuration attribute key. The value is a String specifying
	 * the symbol to use for the main breakpoint.
	 */
	public static final String ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL = CDT_LAUNCH_ID + ".DEBUGGER_STOP_AT_MAIN_SYMBOL"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a String specifying
	 * the register groups memento.
	 */
	public static final String ATTR_DEBUGGER_REGISTER_GROUPS = CDT_LAUNCH_ID + ".DEBUGGER_REGISTER_GROUPS"; //$NON-NLS-1$

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
	 * debugger (a ICDTLaunchConfigurationConstants.DEBUGGER_MODE_XXXXX constant)
	 */
	public static final String ATTR_DEBUGGER_START_MODE = CDT_LAUNCH_ID + ".DEBUGGER_START_MODE"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether to enable variable bookkeeping.
	 */
	public static final String ATTR_DEBUGGER_ENABLE_VARIABLE_BOOKKEEPING = CDT_LAUNCH_ID
			+ ".ENABLE_VARIABLE_BOOKKEEPING"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean specifying
	 * whether to enable register bookkeeping.
	 */
	public static final String ATTR_DEBUGGER_ENABLE_REGISTER_BOOKKEEPING = CDT_LAUNCH_ID
			+ ".ENABLE_REGISTER_BOOKKEEPING"; //$NON-NLS-1$

	/**
	 * launch configuration attribute key.  The value is a string specifying the protocol to
	 * use.  For now only "mi", "mi1", "m2", "mi3" are supported.
	 * @deprecated
	 */
	@Deprecated
	public static final String ATTR_DEBUGGER_PROTOCOL = CDT_LAUNCH_ID + ".protocol"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a global variables'
	 * memento.
	 */
	public static final String ATTR_DEBUGGER_GLOBAL_VARIABLES = CDT_LAUNCH_ID + ".GLOBAL_VARIABLES"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a format list'
	 * memento.
	 */
	public static final String ATTR_DEBUGGER_FORMAT = CDT_LAUNCH_ID + ".FORMAT"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a memory blocks' memento.
	 */
	public static final String ATTR_DEBUGGER_MEMORY_BLOCKS = CDT_LAUNCH_ID + ".MEMORY_BLOCKS"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a String specifying the type of post mortem launch.
	 * Note that we use the prefix "org.eclipse.cdt.dsf.gdb" for backwards-compatibility.
	 * See bug 476589.
	 * @since 7.7
	 */
	public static final String ATTR_DEBUGGER_POST_MORTEM_TYPE = "org.eclipse.cdt.dsf.gdb" + ".POST_MORTEM_TYPE"; //$NON-NLS-1$  //$NON-NLS-2$

	/**
	 * @since 8.3
	 */
	public final static String ATTR_LOCATION = CDT_LAUNCH_ID + ".ATTR_LOCATION"; //$NON-NLS-1$

	/**
	 * @since 8.3
	 */
	public final static String ATTR_TOOL_ARGUMENTS = CDT_LAUNCH_ID + ".ATTR_TOOL_ARGUMENTS"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_STOP_AT_MAIN.
	 */
	public static boolean DEBUGGER_STOP_AT_MAIN_DEFAULT = true;

	/**
	 * Launch configuration attribute value. The key is
	 * DEBUGGER_STOP_AT_MAIN_SYMBOL.
	 */
	public static String DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT = "main"; //$NON-NLS-1$

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
	 * Possible attribute value for the key is ATTR_DEBUGGER_POST_MORTEM_TYPE.
	 * Indicates a core file.
	 *
	 * @since 7.7
	 */
	public static final String DEBUGGER_POST_MORTEM_CORE_FILE = "CORE_FILE"; //$NON-NLS-1$

	/**
	 * Possible attribute value for the key is ATTR_DEBUGGER_POST_MORTEM_TYPE.
	 * Indicates a trace data file.
	 *
	 * @since 7.7
	 */
	public static final String DEBUGGER_POST_MORTEM_TRACE_FILE = "TRACE_FILE"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is ATTR_DEBUGGER_POST_MORTEM_TYPE.
	 * @since 7.7
	 */
	public static final String DEBUGGER_POST_MORTEM_TYPE_DEFAULT = DEBUGGER_POST_MORTEM_CORE_FILE;

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
