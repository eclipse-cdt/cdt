/*******************************************************************************
 * Copyright (c) 2007, 2022 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andy Jin - Hardware debugging UI improvements, bug 229946
 *     John Dallaway - Disable reset and halt by default, bug 529171
 *     Torbjörn Svensson (STMicroelectronics) - Bug 535024
 *     John Dallaway - Sort JTAG device list, bug 560186
 *     John Dallaway - Eliminate deprecated API, bug 566462
 *     John Dallaway - Support multiple remote debug protocols, bug 535143
 *******************************************************************************/

package org.eclipse.cdt.debug.gdbjtag.core;

/**
 * @author Doug Schaefer
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGDBJtagConstants {

	// Debugger
	public static final String ATTR_USE_REMOTE_TARGET = Activator.PLUGIN_ID + ".useRemoteTarget"; //$NON-NLS-1$
	public static final String ATTR_IP_ADDRESS = Activator.PLUGIN_ID + ".ipAddress"; //$NON-NLS-1$
	public static final String ATTR_PORT_NUMBER = Activator.PLUGIN_ID + ".portNumber"; //$NON-NLS-1$
	/** @since 9.2 */
	public static final String ATTR_JTAG_DEVICE_ID = Activator.PLUGIN_ID + ".jtagDeviceId"; //$NON-NLS-1$

	public static final boolean DEFAULT_USE_REMOTE_TARGET = true;
	public static final String DEFAULT_IP_ADDRESS = "unspecified-ip-address"; //$NON-NLS-1$
	public static final int DEFAULT_PORT_NUMBER = 0;
	public static final String ATTR_INIT_COMMANDS = Activator.PLUGIN_ID + ".initCommands"; //$NON-NLS-1$
	public static final String ATTR_DELAY = Activator.PLUGIN_ID + ".delay"; //$NON-NLS-1$
	public static final String ATTR_DO_RESET = Activator.PLUGIN_ID + ".doReset"; //$NON-NLS-1$
	public static final String ATTR_DO_HALT = Activator.PLUGIN_ID + ".doHalt"; //$NON-NLS-1$
	public static final String ATTR_LOAD_IMAGE = Activator.PLUGIN_ID + ".loadImage"; //$NON-NLS-1$
	public static final String ATTR_LOAD_SYMBOLS = Activator.PLUGIN_ID + ".loadSymbols"; //$NON-NLS-1$
	public static final String ATTR_IMAGE_FILE_NAME = Activator.PLUGIN_ID + ".imageFileName"; //$NON-NLS-1$
	public static final String ATTR_SYMBOLS_FILE_NAME = Activator.PLUGIN_ID + ".symbolsFileName"; //$NON-NLS-1$
	public static final String ATTR_IMAGE_OFFSET = Activator.PLUGIN_ID + ".imageOffset"; //$NON-NLS-1$
	public static final String ATTR_SYMBOLS_OFFSET = Activator.PLUGIN_ID + ".symbolsOffset"; //$NON-NLS-1$
	public static final String ATTR_SET_PC_REGISTER = Activator.PLUGIN_ID + ".setPcRegister"; //$NON-NLS-1$
	public static final String ATTR_PC_REGISTER = Activator.PLUGIN_ID + ".pcRegister"; //$NON-NLS-1$
	public static final String ATTR_SET_STOP_AT = Activator.PLUGIN_ID + ".setStopAt"; //$NON-NLS-1$
	public static final String ATTR_STOP_AT = Activator.PLUGIN_ID + ".stopAt"; //$NON-NLS-1$
	public static final String ATTR_SET_RESUME = Activator.PLUGIN_ID + ".setResume"; //$NON-NLS-1$
	public static final String ATTR_RUN_COMMANDS = Activator.PLUGIN_ID + ".runCommands"; //$NON-NLS-1$
	/** @since 10.6 */
	public static final String ATTR_PROTOCOL = Activator.PLUGIN_ID + ".protocol"; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String ATTR_CONNECTION = Activator.PLUGIN_ID + ".connection"; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String ATTR_USE_PROJ_BINARY_FOR_IMAGE = Activator.PLUGIN_ID + ".useProjBinaryForImage"; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String ATTR_USE_FILE_FOR_IMAGE = Activator.PLUGIN_ID + ".useFileForImage"; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String ATTR_USE_PROJ_BINARY_FOR_SYMBOLS = Activator.PLUGIN_ID + ".useProjBinaryForSymbols"; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String ATTR_USE_FILE_FOR_SYMBOLS = Activator.PLUGIN_ID + ".useFileForSymbols"; //$NON-NLS-1$

	public static final boolean DEFAULT_DO_RESET = false;
	public static final boolean DEFAULT_DO_HALT = false;
	public static final int DEFAULT_DELAY = 0;
	public static final boolean DEFAULT_LOAD_IMAGE = true;
	public static final boolean DEFAULT_LOAD_SYMBOLS = true;
	public static final boolean DEFAULT_SET_PC_REGISTER = false;
	public static final boolean DEFAULT_SET_STOP_AT = false;
	public static final boolean DEFAULT_SET_RESUME = false;
	public static final boolean DEFAULT_USE_DEFAULT_RUN = true;

	/** @since 10.6 */
	public static final String DEFAULT_PROTOCOL = "remote"; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String DEFAULT_CONNECTION = "unspecified-ip-address:unspecified-port-number"; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String DEFAULT_INIT_COMMANDS = ""; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String DEFAULT_IMAGE_FILE_NAME = ""; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String DEFAULT_SYMBOLS_FILE_NAME = ""; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String DEFAULT_RUN_COMMANDS = ""; //$NON-NLS-1$
	/** @since 7.0 */
	public static final boolean DEFAULT_USE_PROJ_BINARY_FOR_IMAGE = true;
	/** @since 7.0 */
	public static final boolean DEFAULT_USE_FILE_FOR_IMAGE = false;
	/** @since 7.0 */
	public static final boolean DEFAULT_USE_PROJ_BINARY_FOR_SYMBOLS = true;
	/** @since 7.0 */
	public static final boolean DEFAULT_USE_FILE_FOR_SYMBOLS = false;
	/** @since 7.0 */
	public static final String DEFAULT_IMAGE_OFFSET = ""; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String DEFAULT_SYMBOLS_OFFSET = ""; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String DEFAULT_PC_REGISTER = ""; //$NON-NLS-1$
	/** @since 7.0 */
	public static final String DEFAULT_STOP_AT = ""; //$NON-NLS-1$
	/** @since 9.2 */
	public static final String DEFAULT_JTAG_DEVICE_NAME = ""; //$NON-NLS-1$

}
