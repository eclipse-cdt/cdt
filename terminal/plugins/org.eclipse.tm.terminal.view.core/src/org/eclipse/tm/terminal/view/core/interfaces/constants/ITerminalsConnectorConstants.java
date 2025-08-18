/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361352] [TERMINALS][SSH] Add SSH terminal support
 *******************************************************************************/
package org.eclipse.tm.terminal.view.core.interfaces.constants;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;

/**
 * Defines the terminals connector constants.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITerminalsConnectorConstants {

	/**
	 * Property: The unique id of the terminals view to open.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_ID = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_ID;

	/**
	 * Property: The unique secondary id of the terminals view to open.
	 * <p>
	 * Special values:
	 * <ul>
	 * <li>
	 * not present in properties table or {@link #LAST_ACTIVE_SECONDARY_ID} means open on most recent terminal view
	 * </li>
	 * <li>
	 * {@link #ANY_ACTIVE_SECONDARY_ID} means open on any terminal view
	 * </li>
	 * <li>
	 * <code>null</code> means open on the first primary terminal (the one with no secondary id)
	 * </li>
	 * <ul>
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SECONDARY_ID = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SECONDARY_ID;

	/**
	 * Special value for {@link #PROP_SECONDARY_ID} to indicate reuse of the most recent terminal view
	 * @since 4.8
	 */
	public static final String LAST_ACTIVE_SECONDARY_ID = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.LAST_ACTIVE_SECONDARY_ID;

	/**
	 * Special value for {@link #PROP_SECONDARY_ID} to indicate reuse of any terminal view
	 * @since 4.8
	 */
	public static final String ANY_ACTIVE_SECONDARY_ID = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.ANY_ACTIVE_SECONDARY_ID;

	/**
	 * Property: The title of the terminal tab to open.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_TITLE = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_TITLE;

	/**
	 * Property: Flag to disable updating the terminal title from ANSI escape sequences.
	 * <p>
	 * Property Type: {@link Boolean}
	 * @since 4.10
	 */
	public static final String PROP_TITLE_DISABLE_ANSI_TITLE = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_TITLE_DISABLE_ANSI_TITLE;

	/**
	 * Property: The encoding of the terminal tab to open.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_ENCODING = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_ENCODING;

	/**
	 * Property: Custom data object to associate with the terminal tab.
	 * <p>
	 * Property Type: {@link Object}
	 */
	public static final String PROP_DATA = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_DATA;

	/**
	 * Property: External selection to associate with the terminal tab.
	 * <p>
	 * Property Type: {@link org.eclipse.jface.viewers.ISelection}
	 */
	public static final String PROP_SELECTION = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SELECTION;

	/**
	 * Property: Flag to force a new terminal tab.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_FORCE_NEW = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_FORCE_NEW;

	/**
	 * Property: Terminal launcher delegate id.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_DELEGATE_ID = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_DELEGATE_ID;

	/**
	 * Property: Specific terminal connector type id. Allows clients to
	 *           override the specifically used terminal connector
	 *           implementation for a given type.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_TERMINAL_CONNECTOR_ID = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID;

	// ***** Generic terminals connector properties *****

	/**
	 * Property: Timeout to be passed to the terminal connector. The specific terminal
	 *           connector implementation may interpret this value differently. If not
	 *           set, the terminal connector may use a default value.
	 * <p>
	 * Property Type: {@link Integer}
	 */
	public static final String PROP_TIMEOUT = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_TIMEOUT;

	/**
	 * Property: Flag to control if a local echo is needed from the terminal widget.
	 *           <p>Typical for process and streams terminals.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_LOCAL_ECHO = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_LOCAL_ECHO;

	/**
	 * Property: Data flag to tell the terminal to not reconnect when hitting enter
	 *           in a disconnected terminal.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_DATA_NO_RECONNECT = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_DATA_NO_RECONNECT;

	/**
	 * Property: The line separator expected by the remote terminal on input streams and
	 *           send by the remote terminal on output streams.
	 *           <p>Typical for process and streams terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_LINE_SEPARATOR = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_LINE_SEPARATOR;

	/**
	 * Property: The list of stdout listeners to attach to the corresponding stream monitor.
	 *           <p>Typical for process and streams terminals.
	 * <p>
	 * Property Type: {@link ITerminalServiceOutputStreamMonitorListener} array
	 */
	public static final String PROP_STDOUT_LISTENERS = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS;

	/**
	 * Property: The list of stderr listeners to attach to the corresponding stream monitor.
	 *           <p>Typical for process and streams terminals.
	 * <p>
	 * Property Type: {@link ITerminalServiceOutputStreamMonitorListener} array
	 */
	public static final String PROP_STDERR_LISTENERS = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_STDERR_LISTENERS;

	/**
	 * Property: If set to <code>true</code>, backslashes are translated to
	 *           slashes before pasting the text to the terminal widget.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_TRANSLATE_BACKSLASHES_ON_PASTE = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_TRANSLATE_BACKSLASHES_ON_PASTE;

	// ***** IP based terminals connector properties *****

	/**
	 * Property: Host name or IP address the terminal server is running.
	 *           <p>Typical for telnet or ssh terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_IP_HOST = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_IP_HOST;

	/**
	 * Property: Port at which the terminal server is providing the console input and output.
	 *           <p>Typical for telnet or ssh terminals.
	 * <p>
	 * Property Type: {@link Integer}
	 */
	public static final String PROP_IP_PORT = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_IP_PORT;

	/**
	 * Property: An offset to add to the specified port number.
	 *           <p>Typical for telnet or ssh terminals.
	 * <p>
	 * Property Type: {@link Integer}
	 */
	public static final String PROP_IP_PORT_OFFSET = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_IP_PORT_OFFSET;

	// ***** Process based terminals connector properties *****

	/**
	 * Property: Process image path.
	 * 			 <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_PROCESS_PATH = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_PROCESS_PATH;

	/**
	 * Property: Process arguments.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_PROCESS_ARGS = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_PROCESS_ARGS;

	/**
	 * Property: Process arguments.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_PROCESS_WORKING_DIR = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR;

	/**
	 * Property: Process environment.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link String} array
	 */
	public static final String PROP_PROCESS_ENVIRONMENT = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT;

	/**
	 * Property: Flag to merge process environment with native environment.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link Boolean}
	 */
	public static final String PROP_PROCESS_MERGE_ENVIRONMENT = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_PROCESS_MERGE_ENVIRONMENT;

	/**
	 * Property: Runtime process instance.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link Process}
	 */
	public static final String PROP_PROCESS_OBJ = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_PROCESS_OBJ;

	/**
	 * Property: Runtime process PTY instance.
	 *           <p>Typical for process terminals.
	 * <p>
	 * Property Type: {@link org.eclipse.cdt.utils.pty.PTY}
	 */
	public static final String PROP_PTY_OBJ = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_PTY_OBJ;

	// ***** Streams based terminals connector properties *****

	/**
	 * Property: Stdin streams instance.
	 *           <p>Typical for streams terminals.
	 * <p>
	 * Property Type: {@link OutputStream}
	 */
	public static final String PROP_STREAMS_STDIN = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_STREAMS_STDIN;

	/**
	 * Property: Stdout streams instance.
	 *           <p>Typical for streams terminals.
	 * <p>
	 * Property Type: {@link InputStream}
	 */
	public static final String PROP_STREAMS_STDOUT = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_STREAMS_STDOUT;

	/**
	 * Property: Stderr streams instance.
	 *           <p>Typical for streams terminals.
	 * <p>
	 * Property Type: {@link InputStream}
	 */
	public static final String PROP_STREAMS_STDERR = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_STREAMS_STDERR;

	// ***** Ssh specific properties *****

	/**
	 * Property: ssh keep alive value.
	 * <p>
	 * Property Type: {@link Integer}
	 */
	public static final String PROP_SSH_KEEP_ALIVE = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE;

	/**
	 * Property: Ssh password.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SSH_PASSWORD = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SSH_PASSWORD;

	/**
	 * Property: Ssh user.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SSH_USER = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SSH_USER;

	// ***** Serial specific properties *****

	/**
	 * The serial device name.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_DEVICE = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SERIAL_DEVICE;

	/**
	 * The baud rate.
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_BAUD_RATE = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SERIAL_BAUD_RATE;

	/**
	 * The data bits
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_DATA_BITS = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SERIAL_DATA_BITS;

	/**
	 * The parity
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_PARITY = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SERIAL_PARITY;

	/**
	 * The stop bits
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_STOP_BITS = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SERIAL_STOP_BITS;

	/**
	 * The flow control
	 * <p>
	 * Property Type: {@link String}
	 */
	public static final String PROP_SERIAL_FLOW_CONTROL = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_SERIAL_FLOW_CONTROL;

	// ***** Telnet specific properties *****

	/**
	 * The end-of-line sequence to be sent to the server on "Enter".
	 * <p>
	 * Property Type: {@link String}
	 * @since 4.2
	 */
	public static final String PROP_TELNET_EOL = org.eclipse.terminal.view.core.ITerminalsConnectorConstants.PROP_TELNET_EOL;

}
