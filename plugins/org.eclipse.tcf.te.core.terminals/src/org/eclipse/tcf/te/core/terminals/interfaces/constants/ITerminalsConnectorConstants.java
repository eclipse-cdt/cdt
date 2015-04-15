/*******************************************************************************
 * Copyright (c) 2011 - 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361352] [TERMINALS][SSH] Add SSH terminal support
 *******************************************************************************/
package org.eclipse.tcf.te.core.terminals.interfaces.constants;

import org.eclipse.tcf.te.core.terminals.interfaces.ITerminalServiceOutputStreamMonitorListener;


/**
 * Defines the terminals connector constants.
 */
public interface ITerminalsConnectorConstants {

	/**
	 * Property: The unique id of the terminals view to open.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_ID = "id"; //$NON-NLS-1$

	/**
	 * Property: The unique secondary id of the terminals view to open.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SECONDARY_ID = "secondaryId"; //$NON-NLS-1$

	/**
	 * Property: The title of the terminal tab to open.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_TITLE = "title"; //$NON-NLS-1$

	/**
	 * Property: The encoding of the terminal tab to open.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_ENCODING = "encoding"; //$NON-NLS-1$

	/**
	 * Property: Custom data object to associate with the terminal tab.
	 * <p>
	 * Property Type: {@link Object}
	 */
	public static final String PROP_DATA = "data"; //$NON-NLS-1$

	/**
	 * Property: External selection to associate with the terminal tab.
	 * <p>
	 * Property Type: {@link org.eclipse.jface.viewers.ISelection}
	 */
	public static final String PROP_SELECTION = "selection"; //$NON-NLS-1$

	/**
	 * Property: Flag to force a new terminal tab.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_FORCE_NEW = "terminal.forceNew"; //$NON-NLS-1$

	/**
	 * Property: Terminals launcher delegate id.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_DELEGATE_ID = "delegateId"; //$NON-NLS-1$

	/**
	 * Property: Specific terminal connector type id. Allows clients to
	 *           override the specifically used terminal connector
	 *           implementation for a given type.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_TERMINAL_CONNECTOR_ID = "tm.terminal.connector.id"; //$NON-NLS-1$

	// ***** Generic terminals connector properties *****

	/**
	 * Property: Timeout to be passed to the terminal connector. The specific terminal
	 *           connector implementation may interpret this value differently. If not
	 *           set, the terminal connector may use a default value.
	 * <p>
	 * Property Type: {@link Integer}
	 */
	public static final String PROP_TIMEOUT = "timeout"; //$NON-NLS-1$

	/**
	 * Property: Flag to control if a local echo is needed from the terminal widget.
	 *           <p>Typical for process and streams terminals.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_LOCAL_ECHO = "localEcho"; //$NON-NLS-1$

	/**
	 * Property: Data flag to tell the terminal to not reconnect when hitting enter
	 *           in a disconnected terminal.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_DATA_NO_RECONNECT = "data.noReconnect"; //$NON-NLS-1$

	/**
	 * Property: The line separator expected by the remote terminal on input streams and
	 *           send by the remote terminal on output streams.
	 *           <p>Typical for process and streams terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_LINE_SEPARATOR = "lineSeparator"; //$NON-NLS-1$

	/**
	 * Property: The list of stdout listeners to attach to the corresponding stream monitor.
	 *           <p>Typical for process and streams terminals.
	 * <p>
	 * Property Type: {@link ITerminalServiceOutputStreamMonitorListener} array
	 */
	public static final String PROP_STDOUT_LISTENERS = "stdoutListeners"; //$NON-NLS-1$

	/**
	 * Property: The list of stderr listeners to attach to the corresponding stream monitor.
	 *           <p>Typical for process and streams terminals.
	 * <p>
	 * Property Type: {@link ITerminalServiceOutputStreamMonitorListener} array
	 */
	public static final String PROP_STDERR_LISTENERS = "stderrListeners"; //$NON-NLS-1$

	/**
	 * Property: If set to <code>true</code>, backslashes are translated to
	 *           slashes before pasting the text to the terminal widget.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_TRANSLATE_BACKSLASHES_ON_PASTE = "translateBackslashesOnPaste"; //$NON-NLS-1$

	// ***** IP based terminals connector properties *****

	/**
	 * Property: Host name or IP address the terminal server is running.
	 *           <p>Typical for telnet or ssh terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_IP_HOST = "ip.host"; //$NON-NLS-1$

	/**
	 * Property: Port at which the terminal server is providing the console input and output.
	 *           <p>Typical for telnet or ssh terminals.
	 * <p>
	 * Property Type: {@link Integer}
	 */
	public static final String PROP_IP_PORT = "ip.port"; //$NON-NLS-1$

	/**
	 * Property: An offset to add to the specified port number.
	 *           <p>Typical for telnet or ssh terminals.
	 * <p>
	 * Property Type: {@link Integer}
	 */
	public static final String PROP_IP_PORT_OFFSET = "ip.port.offset"; //$NON-NLS-1$

	// ***** Process based terminals connector properties *****

	/**
	 * Property: Process image path.
	 * 			 <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_PROCESS_PATH = "process.path"; //$NON-NLS-1$

	/**
	 * Property: Process arguments.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_PROCESS_ARGS = "process.args"; //$NON-NLS-1$

	/**
	 * Property: Process arguments.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_PROCESS_WORKING_DIR = "process.working_dir"; //$NON-NLS-1$

	/**
	 * Property: Process environment.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link String} array
	 */
	public static final String PROP_PROCESS_ENVIRONMENT = "process.environment"; //$NON-NLS-1$

	/**
	 * Property: Flag to merge process environment with native environment.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_PROCESS_MERGE_ENVIRONMENT = "process.environment.merge"; //$NON-NLS-1$

	/**
	 * Property: Runtime process instance.
     *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link Process}
	 */
	public static final String PROP_PROCESS_OBJ = "process"; //$NON-NLS-1$

	/**
	 * Property: Runtime process PTY instance.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link org.eclipse.cdt.utils.pty.PTY}
	 */
	public static final String PROP_PTY_OBJ = "pty"; //$NON-NLS-1$

	// ***** Streams based terminals connector properties *****

	/**
	 * Property: Stdin streams instance.
	 *           <p>Typical for streams terminals.
	 * <p>
	 * Property Type: {@link OutputStream}
	 */
	public static final String PROP_STREAMS_STDIN = "streams.stdin"; //$NON-NLS-1$

	/**
	 * Property: Stdout streams instance.
	 *           <p>Typical for streams terminals.
	 * <p>
	 * Property Type: {@link InputStream}
	 */
	public static final String PROP_STREAMS_STDOUT = "streams.stdout"; //$NON-NLS-1$

	/**
	 * Property: Stderr streams instance.
	 *           <p>Typical for streams terminals.
	 * <p>
	 * Property Type: {@link InputStream}
	 */
	public static final String PROP_STREAMS_STDERR = "streams.stderr"; //$NON-NLS-1$

	// ***** Ssh specific properties *****

	/**
	 * Property: ssh keep alive value.
	 * <p>
	 * Property Type: {@link Integer}
	 */
	public static final String PROP_SSH_KEEP_ALIVE = "ssh.keep_alive"; //$NON-NLS-1$

	/**
	 * Property: Ssh password.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SSH_PASSWORD = "ssh.password"; //$NON-NLS-1$

	/**
	 * Property: Ssh user.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SSH_USER = "ssh.user"; //$NON-NLS-1$

	// ***** Serial specific properties *****

	/**
	 * The serial device name.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_DEVICE = "serial.device"; //$NON-NLS-1$

	/**
	 * The baud rate.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_BAUD_RATE = "serial.baudrate"; //$NON-NLS-1$

	/**
	 * The data bits
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_DATA_BITS = "serial.databits"; //$NON-NLS-1$

	/**
	 * The parity
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_PARITY = "serial.parity"; //$NON-NLS-1$

	/**
	 * The stop bits
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_STOP_BITS = "serial.stopbits"; //$NON-NLS-1$

	/**
	 * The flow control
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_FLOW_CONTROL = "serial.flowcontrol"; //$NON-NLS-1$
}
