/*******************************************************************************
 * Copyright (c) 2005, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [267181] Fix telnet option negotiation loop
 * Anton Leherbauer (Wind River) - [453393] Add support for copying wrapped lines without line break
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * This class represents a single TELNET protocol option at one endpoint of a TELNET
 * connection.  This class encapsulates the endpoint associated with the option (local
 * or remote), the current state of the option (enabled or disabled), the desired state
 * of the option, the current state of the negotiation, an OutputStream that allows
 * communication with the remote endpoint, and the number of negotiations that have
 * started within this connection. <p>
 *
 * In addition to encapsulating the above state, this class performs option negotiation
 * to attempt to achieve the desired option state.  For some options, this class also
 * performs option sub-negotiation. <p>
 *
 * IMPORTANT: Understanding this code requires understanding the TELNET protocol and
 * TELNET option processing. <p>
 *
 * @author Fran Litterio (francis.litterio@windriver.com)
 */
class TelnetOption implements TelnetCodes
{
    /**
     * This array of Strings maps an integer TELNET option code value to the symbolic
     * name of the option.  Array elements of the form "?" represent unassigned option
     * values.
     */
    protected static final String[] optionNames =
    {
        "BINARY",                               // 0 //$NON-NLS-1$
        "ECHO",                                 // 1 //$NON-NLS-1$
        "RECONNECTION",                         // 2  //$NON-NLS-1$
        "SUPPRESS GO AHEAD",                    // 3  //$NON-NLS-1$
        "MSG SIZE NEGOTIATION",                 // 4  //$NON-NLS-1$
        "STATUS",                               // 5  //$NON-NLS-1$
        "TIMING MARK",                          // 6  //$NON-NLS-1$
        "REMOTE CTRL TRANS+ECHO",               // 7  //$NON-NLS-1$
        "OUTPUT LINE WIDTH",                    // 8  //$NON-NLS-1$
        "OUTPUT PAGE SIZE",                     // 9  //$NON-NLS-1$
        "OUTPUT CR DISPOSITION",                // 10 //$NON-NLS-1$
        "OUTPUT HORIZ TABSTOPS",                // 11 //$NON-NLS-1$
        "OUTPUT HORIZ TAB DISPOSITION",         // 12 //$NON-NLS-1$
        "OUTPUT FORMFEED DISPOSITION",          // 13 //$NON-NLS-1$
        "OUTPUT VERTICAL TABSTOPS",             // 14 //$NON-NLS-1$
        "OUTPUT VT DISPOSITION",                // 15 //$NON-NLS-1$
        "OUTPUT LF DISPOSITION",                // 16 //$NON-NLS-1$
        "EXTENDED ASCII",                       // 17 //$NON-NLS-1$
        "LOGOUT",                               // 18 //$NON-NLS-1$
        "BYTE MACRO",                           // 19 //$NON-NLS-1$
        "DATA ENTRY TERMINAL",                  // 20 //$NON-NLS-1$
        "SUPDUP",                               // 21 //$NON-NLS-1$
        "SUPDUP OUTPUT",                        // 22 //$NON-NLS-1$
        "SEND LOCATION",                        // 23 //$NON-NLS-1$
        "TERMINAL TYPE",                        // 24 //$NON-NLS-1$
        "END OF RECORD",                        // 25 //$NON-NLS-1$
        "TACACS USER IDENTIFICATION",           // 26 //$NON-NLS-1$
        "OUTPUT MARKING",                       // 27 //$NON-NLS-1$
        "TERMINAL LOCATION NUMBER",             // 28 //$NON-NLS-1$
        "3270 REGIME",                          // 29 //$NON-NLS-1$
        "X.3 PAD",                              // 30 //$NON-NLS-1$
        "NEGOTIATE ABOUT WINDOW SIZE",          // 31 //$NON-NLS-1$
        "TERMINAL SPEED",                       // 32 //$NON-NLS-1$
        "REMOTE FLOW CONTROL",                  // 33 //$NON-NLS-1$
        "LINEMODE",                             // 34 //$NON-NLS-1$
        "X DISPLAY LOCATION",                   // 35 //$NON-NLS-1$
        "ENVIRONMENT OPTION",                   // 36 //$NON-NLS-1$
        "AUTHENTICATION OPTION",                // 37 //$NON-NLS-1$
        "ENCRYPTION OPTION",                    // 38 //$NON-NLS-1$
        "NEW ENVIRONMENT OPTION",               // 39 //$NON-NLS-1$
        "TN3270E",                              // 40 //$NON-NLS-1$
        "XAUTH",                                // 41 //$NON-NLS-1$
        "CHARSET",                              // 42 //$NON-NLS-1$
        "REMOTE SERIAL PORT",                   // 43 //$NON-NLS-1$
        "COM PORT CONTROL OPTION",              // 44 //$NON-NLS-1$
        "SUPPRESS LOCAL ECHO",                  // 45 //$NON-NLS-1$
        "START TLS",                            // 46 //$NON-NLS-1$
        "KERMIT",                               // 47 //$NON-NLS-1$
        "SEND URL",                             // 48 //$NON-NLS-1$
        "FORWARD X",                            // 49 //$NON-NLS-1$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?",       // 50 ... //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?",                      // ... 137 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        "TELOPT PRAGMA LOGON",                  // 138 //$NON-NLS-1$
        "TELOPT SSPI LOGON",                    // 139 //$NON-NLS-1$
        "TELOPT PRAGMA HEARTBEAT",              // 140 //$NON-NLS-1$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?",       // 141 ... //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?", "?", "?", "?", "?", "?", "?", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
        "?", "?", "?", "?",                                     // ... 254 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        "EXTENDED OPTIONS LIST"                 // 255 //$NON-NLS-1$
    };

    /**
     * Negotiation state: Negotiation not yet started for this option. <p>
     *
     * This constant and the others having similar names represent the states of a
     * finite state automaton (FSA) that tracks the negotiation state of this option.
     * The initial state is NEGOTIATION_NOT_STARTED.  The state machine is as follows
     * (with transitions labeled with letters in parentheses): <p>
     *
     * <pre>
     *     NEGOTIATION_NOT_STARTED -----> {@link #NEGOTIATION_IN_PROGRESS}
     *                         |    (A)      |        ^
     *                      (C)|          (B)|        |(D)
     *                         |             V        |
     *                         +--------> {@link #NEGOTIATION_DONE}
     * </pre> <p>
     *
     * Once the FSA leaves state NEGOTIATION_NOT_STARTED, it never returns to that
     * state.  Transition A happens when the local endpoint sends an option command
     * before receiving a command for the same option from the remote endpoint. <p>
     *
     * Transition B happens when the local endpoint receives a reply to an option
     * command sent earlier by the local endpoint.  Receipt of that reply terminates
     * the negotiation. <p>
     *
     * Transition D happens after negotiation is done and "something changes" (see the
     * RFCs for the definition of "something changes").  Either endpoint can
     * re-negotiate an option after a previous negotiation, but only if some external
     * influence (such as the user or the OS) causes it to do so.  Re-negotiation must
     * start more than {@link #NEGOTIATION_IGNORE_DURATION} milliseconds after the FSA
     * enters state NEGOTIATION_DONE or it will be ignored.  This is how this client
     * prevents negotiation loops. <p>
     *
     * Transition C happens when the local endpoint receives an option command from the
     * remote endpoint before sending a command for the same option.  In that case, the
     * local endpoint replies immediately with an option command and the negotiation
     * terminates. <p>
     *
     * Some TELNET servers (e.g., the Solaris server), after sending WILL and receiving
     * DONT, will reply with a superfluous WONT.  Any such superfluous option command
     * received from the remote endpoint while the option's FSA is in state
     * {@link #NEGOTIATION_DONE} will be ignored by the local endpoint.
     */
    protected static final int NEGOTIATION_NOT_STARTED = 0;

    /** Negotiation state: Negotiation is in progress for this option. */
    protected static final int NEGOTIATION_IN_PROGRESS = 1;

    /** Negotiation state: Negotiation has terminated for this option. */
    protected static final int NEGOTIATION_DONE = 2;

    /**
     * The number of milliseconds following the end of negotiation of this option
     * before which the remote endpoint can re-negotiate the option.  Any option
     * command received from the remote endpoint before this time passes is ignored.
     * This is used to prevent option negotiation loops.
     *
     * @see #ignoreNegotiation()
     * @see #negotiationCompletionTime
     */
    protected static final int NEGOTIATION_IGNORE_DURATION = 30000;

    /**
     * This field holds the current negotiation state for this option.
     */
    protected int negotiationState = NEGOTIATION_NOT_STARTED;

    /**
     * This field holds the time when negotiation of this option most recently
     * terminated (i.e., entered state {@link #NEGOTIATION_DONE}).  This is used to
     * determine whether an option command received from the remote endpoint after
     * negotiation has terminated for this option is to be ignored or interpreted as
     * the start of a new negotiation.
     *
     * @see #NEGOTIATION_IGNORE_DURATION
     */
    protected Date negotiationCompletionTime = new Date(0);

    /**
     * Holds the total number of negotiations that have completed for this option.
     */
    protected int negotiationCount = 0;

    /**
     * Holds the integer code representing the option.
     */
    protected byte option = 0;

    /**
     * Holds the OutputStream object that allows data to be sent to the remote endpoint
     * of the TELNET connection.
     */
    protected OutputStream outputStream;

    /**
     * True if this option is for the local endpoint, false for the remote endpoint.
     */
    protected boolean local = true;

    /**
     * This field is true if the option is enabled, false if it is disabled.  All
     * options are initially disabled until they are negotiated to be enabled. <p>
     */
    protected boolean enabled = false;

    /**
     * This field is true if the client desires the option to be enabled, false if the
     * client desires the option to be disabled.  This field does not represent the
     * remote's endpoints desire (as expressed via WILL and WONT commands) -- it
     * represnet the local endpoint's desire. <p>
     *
     * @see #setDesired(boolean)
     */
    protected boolean desired = false;

    /**
	 * Constructor. <p>
	 *
	 * @param option            The integer code of this option.
	 * @param desired           Whether we desire this option to be enabled.
	 * @param local             Whether this option is for the local or remote endpoint.
	 * @param outputStream      A stream used to negotiate with the remote endpoint.
	 */
	TelnetOption(byte option, boolean desired, boolean local,
			OutputStream outputStream) {
		this.option = option;
		this.desired = desired;
		this.local = local;
		this.outputStream = outputStream;
	}

	/**
	 * @return Returns a String containing the name of the TELNET option specified in
	 *         parameter <i>option</i>.
	 */
	public String optionName() {
		return optionNames[option];
	}

	/**
	 * Returns true if this option is enabled, false if it is disabled. <p>
	 *
	 * @return Returns true if this option is enabled, false if it is disabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Enables this option if <i>newValue</i> is true, otherwise disables this
	 * option. <p>
	 *
	 * @param newValue          True if this option is to be enabled, false otherwise.
	 */
	public void setEnabled(boolean newValue) {
		Logger.log("Enabling " + (local ? "local" : "remote") + " option " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				optionName());
		enabled = newValue;
	}

	/**
	 * Returns true if the local endpoint desires this option to be enabled, false if
	 * not.  It is not an error for the value returned by this method to differ from
	 * the value returned by isEnabled().  The value returned by this method can change
	 * over time, reflecting the local endpoint's changing desire regarding the
	 * option. <p>
	 *
	 * NOTE: Even if this option represents a remote endpoint option, the return value
	 * of this method represents the local endpint's desire regarding the remote
	 * option. <p>
	 *
	 * @return Returns true if the local endpoint desires this option to be enabled,
	 *         false if not.
	 */
	public boolean isDesired() {
		return desired;
	}

	/**
	 * Sets our desired value for this option.  Note that the option can be desired
	 * when <i>enabled</i> is false, and the option can be undesired when
	 * <i>enabled</i> is true, though the latter state should not persist, since either
	 * endpoint can disable any option at any time. <p>
	 *
	 * @param newValue          True if we desire this option to be enabled, false if
	 *                          we desire this option to be disabled.
	 */
	public void setDesired(boolean newValue) {
		if (newValue)
			Logger.log("Setting " + (local ? "local" : "remote") + " option " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					optionName() + " as desired."); //$NON-NLS-1$

		desired = newValue;
	}

	/**
	 * Call this method to request that negotiation begin for this option.  This method
	 * does nothing if negotiation for this option has already started or is already
	 * complete.  If negotiation has not yet started for this option and the local
	 * endpoint desires this option to be enabled, then we send a WILL or DO command to
	 * the remote endpoint.
	 */
	public void negotiate() {
		if (negotiationState == NEGOTIATION_NOT_STARTED && desired) {
			if (local) {
				Logger
						.log("Starting negotiation for local option " + optionName()); //$NON-NLS-1$
				sendWill();
			} else {
				Logger
						.log("Starting negotiation for remote option " + optionName()); //$NON-NLS-1$
				sendDo();
			}

			negotiationState = NEGOTIATION_IN_PROGRESS;
		}
	}

	/**
	 * This method is called whenever we receive a WILL command from the remote
	 * endpoint.
	 */
	public void handleWill() {
		if (negotiationState == NEGOTIATION_DONE && ignoreNegotiation()) {
			Logger
					.log("Ignoring superfluous WILL command from remote endpoint."); //$NON-NLS-1$
			return;
		}

		if (negotiationState == NEGOTIATION_IN_PROGRESS) {
			if (desired) {
				// We sent DO and server replied with WILL.  Enable the option, and end
				// this negotiation.

				enabled = true;
				Logger.log("Enabling remote option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				endNegotiation();
			} else {
				// This should never happen!  We sent DONT and the server replied with
				// WILL.  Bad server.  No soup for you.  Disable the option, and end
				// this negotiation.

				Logger.log("Server answered DONT with WILL!"); //$NON-NLS-1$
				enabled = false;
				Logger.log("Disabling remote option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				endNegotiation();
			}
		} else {
			if (desired) {
				// Server sent WILL, so we reply with DO.  Enable the option, and end
				// this negotiation.

				sendDo();
				enabled = true;
				Logger.log("Enabling remote option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				endNegotiation();
			} else {
				// Server sent WILL, so we reply with DONT.  Disable the option, and
				// end this negotiation.

				sendDont();
				enabled = false;
				Logger.log("Disabling remote option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				endNegotiation();
			}
		}
	}

	/**
	 * Handles a WONT command sent by the remote endpoint for this option.  The value
	 * of <i>desired</i> doesn't matter in this method, because the remote endpoint is
	 * forcing the option to be disabled.
	 */
	public void handleWont() {
		if (negotiationState == NEGOTIATION_DONE && ignoreNegotiation()) {
			Logger
					.log("Ignoring superfluous WONT command from remote endpoint."); //$NON-NLS-1$
			return;
		}

		if (negotiationState == NEGOTIATION_IN_PROGRESS) {
			// We sent DO or DONT and server replied with WONT.  Disable the
			// option, and end this negotiation.

			enabled = false;
			Logger.log("Disabling remote option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
			endNegotiation();
		} else {
			// Server sent WONT, so we reply with DONT.  Disable the option, and
			// end this negotiation.

			sendDont();
			enabled = false;
			Logger.log("Disabling remote option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
			endNegotiation();
		}
	}

	/**
	 * Handles a DO command sent by the remote endpoint for this option.
	 */
	public void handleDo() {
		if (negotiationState == NEGOTIATION_DONE && ignoreNegotiation()) {
			Logger.log("Ignoring superfluous DO command from remote endpoint."); //$NON-NLS-1$
			return;
		}

		if (negotiationState == NEGOTIATION_IN_PROGRESS) {
			if (desired) {
				// We sent WILL and server replied with DO.  Enable the option, and end
				// this negotiation.

				enabled = true;
				Logger.log("Enabling local option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				endNegotiation();
			} else {
				// We sent WONT and server replied with DO.  This should never happen!
				// Bad server.  No soup for you.  Disable the option, and end this
				// negotiation.

				Logger.log("Server answered WONT with DO!"); //$NON-NLS-1$
				enabled = false;
				Logger.log("Disabling local option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				endNegotiation();
			}
		} else {
			if (desired) {
				// Server sent DO, so we reply with WILL.  Enable the option, and end
				// this negotiation.

				sendWill();
				enabled = true;
				Logger.log("Enabling local option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				endNegotiation();
			} else {
				// Server sent DO, so we reply with WONT.  Disable the option, and end
				// this negotiation.

				sendWont();
				enabled = false;
				Logger.log("Disabling local option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				endNegotiation();
			}
		}
	}

	/**
	 * Handles a DONT command sent by the remote endpoint for this option.  The value
	 * of <i>desired</i> doesn't matter in this method, because the remote endpoint is
	 * forcing the option to be disabled.
	 */
	public void handleDont() {
		if (negotiationState == NEGOTIATION_DONE && ignoreNegotiation()) {
			Logger
					.log("Ignoring superfluous DONT command from remote endpoint."); //$NON-NLS-1$
			return;
		}

		if (negotiationState == NEGOTIATION_IN_PROGRESS) {
			// We sent WILL or WONT and server replied with DONT.  Disable the
			// option, and end this negotiation.

			enabled = false;
			Logger.log("Disabling local option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
			endNegotiation();
		} else {
			// Server sent DONT, so we reply with WONT.  Disable the option, and end
			// this negotiation.

			sendWont();
			enabled = false;
			Logger.log("Disabling local option " + optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
			endNegotiation();
		}
	}

	/**
	 * This method handles a subnegotiation command received from the remote endpoint.
	 * Currently, the only subnegotiation we handle is when the remote endpoint
	 * commands us to send our terminal type (which is "xterm").
	 *
	 * @param subnegotiationData        An array of bytes containing a TELNET
	 *                                  subnegotiation command received from the
	 *                                  remote endpoint.
	 * @param count                     The number of bytes in array
	 *                                  subnegotiationData to examine.
	 */
	public void handleSubnegotiation(byte[] subnegotiationData, int count) {
		switch (option) {
		case TELNET_OPTION_TERMINAL_TYPE:
			if (subnegotiationData[1] != TELNET_SEND) {
				// This should never happen!
				Logger
						.log("Invalid TERMINAL-TYPE subnegotiation command from remote endpoint: " + //$NON-NLS-1$
								(subnegotiationData[1] & 0xff));
				break;
			}

			// Tell the remote endpoint our terminal type is "ansi" using this sequence
			// of TELNET protocol bytes:
			//
			//    IAC SB TERMINAL-TYPE IS x t e r m IAC SE

			byte[] terminalTypeData = { TELNET_IAC, TELNET_SB,
					TELNET_OPTION_TERMINAL_TYPE, TELNET_IS, (byte) 'x',
					(byte) 't', (byte) 'e', (byte) 'r', (byte) 'm', TELNET_IAC, TELNET_SE };

			try {
				outputStream.write(terminalTypeData);
			} catch (IOException ex) {
				Logger.log("IOException sending TERMINAL-TYPE subnegotiation!"); //$NON-NLS-1$
				Logger.logException(ex);
			}
			break;

		default:
			// This should never happen!
			Logger
					.log("SHOULD NOT BE REACHED: Called for option " + optionName()); //$NON-NLS-1$
			break;
		}
	}

	/**
	 * This method sends a subnegotiation command to the remote endpoint.
	 *
	 * @param subnegotiationData        An array of Objects holding data to be used
	 *                                  when generating the outbound subnegotiation
	 *                                  command.
	 */
	public void sendSubnegotiation(Object[] subnegotiationData) {
		switch (option) {
		case TELNET_OPTION_NAWS:
			// Get the width and height of the view and send it to the remote
			// endpoint using this sequence of TELNET protocol bytes:
			//
			//    IAC SB NAWS <width-highbyte> <width-lowbyte> <height-highbyte>
			//    <height-lowbyte> IAC SE

			byte[] NAWSData = { TELNET_IAC, TELNET_SB, TELNET_OPTION_NAWS, 0,
					0, 0, 0, TELNET_IAC, TELNET_SE };
			int width = ((Integer) subnegotiationData[0]).intValue();
			int height = ((Integer) subnegotiationData[1]).intValue();

			NAWSData[3] = (byte) ((width >>> 8) & 0xff); // High order byte of width.
			NAWSData[4] = (byte) (width & 0xff); // Low order byte of width.
			NAWSData[5] = (byte) ((height >>> 8) & 0xff); // High order byte of height.
			NAWSData[6] = (byte) (height & 0xff); // Low order byte of height.

			Logger
					.log("sending terminal size to remote endpoint: width = " + width + //$NON-NLS-1$
							", height = " + height + "."); //$NON-NLS-1$ //$NON-NLS-2$

			// This final local variable is a hack to get around the fact that inner
			// classes cannot reference a non-final local variable in a lexically
			// enclosing scope.

			final byte[] NAWSDataFinal = NAWSData;

			// Send the NAWS data in a new thread.  The current thread is the display
			// thread, and calls to write() can block, but blocking the display thread
			// is _bad_ (it hangs the GUI).

			Thread t=new Thread() {
				public void run() {
					try {
						outputStream.write(NAWSDataFinal);
					} catch (IOException ex) {
						Logger.log("IOException sending NAWS subnegotiation!"); //$NON-NLS-1$
						Logger.logException(ex);
					}
				}
			};
			t.setDaemon(true);
			t.start();
			break;

		default:
			// This should never happen!
			Logger
					.log("SHOULD NOT BE REACHED: Called for option " + optionName()); //$NON-NLS-1$
			break;
		}
	}

	/**
	 * This method returns true if there has not yet been any negotiation of this
	 * option.
	 *
	 * @return Returns true if there has not yet been any negotiation of this option.
	 */
	protected boolean notYetNegotiated() {
		return negotiationState == NEGOTIATION_NOT_STARTED;
	}

	/**
	 * This method terminates the current negotiation and records the time at which the
	 * negotiation terminated.
	 */
	protected void endNegotiation() {
		Logger.log("Ending negotiation #" + negotiationCount + " for " + //$NON-NLS-1$ //$NON-NLS-2$
				(local ? "local" : "remote") + " option " + optionName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		negotiationState = NEGOTIATION_DONE;
		negotiationCompletionTime.setTime(System.currentTimeMillis());
		++negotiationCount;
	}

	/**
	 * This method determines whether or not to ignore what appears to be a new
	 * negotiation initiated by the remote endpoint.  This is needed because some
	 * TELNET servers send superfluous option commands that a naive client might
	 * interpret as the start of a new negotiation.  If the superfluous command is not
	 * ignored, an option negotiation loop can result (which is bad).  For details
	 * about the superfluous commands sent by some servers, see the documentation for
	 * {@link #NEGOTIATION_NOT_STARTED}. <p>
	 *
	 * The current implementation of this method returns true if the new negotiation
	 * starts within NEGOTIATION_IGNORE_DURATION seconds of the end of the previous
	 * negotiation of this option. <p>
	 *
	 * @return Returns true if the new negotiation should be ignored, false if not.
	 */
	protected boolean ignoreNegotiation() {
		return (System.currentTimeMillis() - negotiationCompletionTime.getTime()) < NEGOTIATION_IGNORE_DURATION;
	}

	/**
	 * Sends a DO command to the remote endpoint for this option.
	 */
	protected void sendDo() {
		Logger.log("Sending DO " + optionName()); //$NON-NLS-1$
		sendCommand(TELNET_DO);
	}

	/**
	 * Sends a DONT command to the remote endpoint for this option.
	 */
	protected void sendDont() {
		Logger.log("Sending DONT " + optionName()); //$NON-NLS-1$
		sendCommand(TELNET_DONT);
	}

	/**
	 * Sends a WILL command to the remote endpoint for this option.
	 */
	protected void sendWill() {
		Logger.log("Sending WILL " + optionName()); //$NON-NLS-1$
		sendCommand(TELNET_WILL);
	}

	/**
	 * Sends a WONT command to the remote endpoint for this option.
	 */
	protected void sendWont() {
		Logger.log("Sending WONT " + optionName()); //$NON-NLS-1$
		sendCommand(TELNET_WONT);
	}

	/**
	 * This method sends a WILL/WONT/DO/DONT command to the remote endpoint for this
	 * option.
	 */
	protected void sendCommand(byte command) {
		byte[] data = { TELNET_IAC, 0, 0 };

		data[1] = command;
		data[2] = option;

		try {
			outputStream.write(data);
		} catch (IOException ex) {
			Logger.log("IOException sending command " + command); //$NON-NLS-1$
			Logger.logException(ex);
		}
	}
}
