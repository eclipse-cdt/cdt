/*******************************************************************************
 * Copyright (c) 2005, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Fran Litterio (Wind River) - initial API and implementation 
 * Helmut Haigermoser (Wind River) - repackaged 
 * Ted Williams (Wind River) - repackaged into org.eclipse namespace 
 * Michael Scharf (Wind River) - split into core, view and connector plugins 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

/**
 * This interface defines symbolic constants for numeric TELNET protocol command and
 * option codes.  Any class that needs to use these constants must implement this
 * interface.  The meanings of these constants are defined in the various TELNET RFCs
 * (RFC 854 to RFC 861, and others).
 */
interface TelnetCodes
{
    /** Command code: Subnegotiation End. */
    static final byte TELNET_SE         = (byte)240;

    /** Command code: No-op. */
    static final byte TELNET_NOP        = (byte)241;

    /** Command code: Data Mark. */
    static final byte TELNET_DM         = (byte)242;

    /** Command code: Break. */
    static final byte TELNET_BREAK      = (byte)243;

    /** Command code: Interrupt Process. */
    static final byte TELNET_IP         = (byte)244;

    /** Command code: Abort Output. */
    static final byte TELNET_AO         = (byte)245;

    /** Command code: Are You There. */
    static final byte TELNET_AYT        = (byte)246;

    /** Command code: Erase Character. */
    static final byte TELNET_EC         = (byte)247;

    /** Command code: Erase Line. */
    static final byte TELNET_EL         = (byte)248;

    /** Command code: Go Ahead. */
    static final byte TELNET_GA         = (byte)249;

    /** Command code: Subnegotiation Begin. */
    static final byte TELNET_SB         = (byte)250;

    /** Command code: Will. */
    static final byte TELNET_WILL       = (byte)251;

    /** Command code: Won't. */
    static final byte TELNET_WONT       = (byte)252;

    /** Command code: Do. */
    static final byte TELNET_DO         = (byte)253;

    /** Command code: Don't. */
    static final byte TELNET_DONT       = (byte)254;

    /** Command code: Interpret As Command. */
    static final byte TELNET_IAC        = (byte)255;

    /** Command code: IS. */
    static final byte TELNET_IS         = 0;

    /** Command code: SEND. */
    static final byte TELNET_SEND       = 1;


    /** Option code: Transmit Binary option. */
    static final byte TELNET_OPTION_TRANSMIT_BINARY     = 0;

    /** Option code: Echo option. */
    static final byte TELNET_OPTION_ECHO                = 1;

    /** Option code: Suppress Go Ahead option. */
    static final byte TELNET_OPTION_SUPPRESS_GA         = 3;

    /** Option code: Terminal Type */
    static final byte TELNET_OPTION_TERMINAL_TYPE       = 24;

    /** Option code: Negotitate About Window Size (NAWS) */
    static final byte TELNET_OPTION_NAWS                = 31;
}
