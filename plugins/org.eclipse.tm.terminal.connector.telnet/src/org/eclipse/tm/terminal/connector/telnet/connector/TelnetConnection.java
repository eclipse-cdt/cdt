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
 * Michael Scharf (Wind River) - [209665] Add ability to log byte streams from terminal
 * Alex Panchenko (Xored) - [277061]  TelnetConnection.isConnected() should check if socket was not closed
 * Uwe Stieber (Wind River) - [281329] Telnet connection not handling "SocketException: Connection reset" correct
 * Nils Hagge (Siemens AG) - [276023] close socket streams after connection is disconnected 
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

/**
 * This class encapsulates a TELNET connection to a remote server. It processes
 * incoming TELNET protocol data and generates outbound TELNET protocol data. It
 * also manages two sets of TelnetOption objects: one for the local endpoint and
 * one for the remote endpoint.
 * <p>
 *
 * IMPORTANT: Understanding this code requires understanding the TELNET protocol
 * and TELNET option processing, as defined in the RFCs listed below.
 * <p>
 *
 * @author Fran Litterio (francis.litterio@windriver.com)
 *
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc854.txt">RFC 854</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc855.txt">RFC 855</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc856.txt">RFC 856</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc857.txt">RFC 857</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc858.txt">RFC 858</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc859.txt">RFC 859</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc860.txt">RFC 860</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc861.txt">RFC 861</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc1091.txt">RFC 1091</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc1096.txt">RFC 1096</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc1073.txt">RFC 1073</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc1079.txt">RFC 1079</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc1143.txt">RFC 1143</a>
 * @see <a href="ftp://ftp.rfc-editor.org/in-notes/rfc1572.txt">RFC 1572</a>
 */
public class TelnetConnection extends Thread implements TelnetCodes {
	/**
	 * TELNET connection state: Initial state.
	 */
	protected static final int STATE_INITIAL = 0;

	/**
	 * TELNET connection state: Last byte processed was IAC code. code.
	 */
	protected static final int STATE_IAC_RECEIVED = 1;

	/**
	 * TELNET connection state: Last byte processed was WILL code. code.
	 */
	protected static final int STATE_WILL_RECEIVED = 2;

	/**
	 * TELNET connection state: Last byte processed was WONT code.
	 */
	protected static final int STATE_WONT_RECEIVED = 3;

	/**
	 * TELNET connection state: Last byte processed was DO code.
	 */
	protected static final int STATE_DO_RECEIVED = 4;

	/**
	 * TELNET connection state: Last byte processed was DONT code.
	 */
	protected static final int STATE_DONT_RECEIVED = 5;

	/**
	 * TELNET connection state: Last byte processed was SB.
	 */
	protected static final int STATE_SUBNEGOTIATION_STARTED = 6;

	/**
	 * TELNET connection state: Currently receiving sub-negotiation data.
	 */
	protected static final int STATE_RECEIVING_SUBNEGOTIATION = 7;

	/**
	 * Size of buffer for processing data received from remote endpoint.
	 */
	protected static final int BUFFER_SIZE = 2048;

	/**
	 * Holds raw bytes received from the remote endpoint, prior to any TELNET
	 * protocol processing.
	 */
	protected byte[] rawBytes = new byte[BUFFER_SIZE];

	/**
	 * Holds incoming network data after the TELNET protocol bytes have been
	 * processed and removed.
	 */
	protected byte[] processedBytes = new byte[BUFFER_SIZE];

	/**
	 * This field holds a StringBuffer containing text recently received from
	 * the remote endpoint (after all TELNET protocol bytes have been processed
	 * and removed).
	 */
	protected StringBuffer processedStringBuffer = new StringBuffer(BUFFER_SIZE);

	/**
	 * Holds the current state of the TELNET protocol processor.
	 */
	protected int telnetState = STATE_INITIAL;

	/**
	 * This field is true if the remote endpoint is a TELNET server, false if
	 * not. We set this to true if and only if the remote endpoint sends
	 * recognizable TELNET protocol data. We do not assume that the remote
	 * endpoint is a TELNET server just because it is listening on port 23. This
	 * allows us to successfully connect to a TELNET server listening on a port
	 * other than 23.
	 * <p>
	 *
	 * When this field first changes from false to true, we send all WILL or DO
	 * commands to the remote endpoint.
	 * <p>
	 *
	 * @see #telnetServerDetected()
	 */
	protected boolean remoteIsTelnetServer = false;

	/**
	 * An array of TelnetOption objects representing the local endpoint's TELNET
	 * options. The array is indexed by the numeric TELNET option code.
	 */
	protected TelnetOption[] localOptions = new TelnetOption[256];

	/**
	 * An array of TelnetOption objects representing the remote endpoint's
	 * TELNET options. The array is indexed by the numeric TELNET option code.
	 */
	protected TelnetOption[] remoteOptions = new TelnetOption[256];

	/**
	 * An array of bytes that holds the TELNET subnegotiation command most
	 * recently received from the remote endpoint. This array does _not_ include
	 * the leading IAC SB bytes, nor does it include the trailing IAC SE bytes.
	 * The first byte of this array is always a TELNET option code.
	 */
	protected byte[] receivedSubnegotiation = new byte[128];

	/**
	 * This field holds the index into array {@link #receivedSubnegotiation} of
	 * the next unused byte. This is used by method
	 * {@link #processTelnetProtocol(int)} when the state machine is in states
	 * {@link #STATE_SUBNEGOTIATION_STARTED} and {@link
	 * #STATE_RECEIVING_SUBNEGOTIATION}.
	 */
	protected int nextSubnegotiationByteIndex = 0;

	/**
	 * This field is true if an error occurs while processing a subnegotiation
	 * command.
	 *
	 * @see #processTelnetProtocol(int)
	 */
	protected boolean ignoreSubnegotiation = false;

	/**
	 * This field holds the width of the Terminal screen in columns.
	 */
	protected int width = 0;

	/**
	 * This field holds the height of the Terminal screen in rows.
	 */
	protected int height = 0;

	/**
	 * This field holds a reference to the {@link ITerminalControl} singleton.
	 */
	protected TelnetConnector terminalControl;

	/**
	 * This method holds the Socket object for the TELNET connection.
	 */
	protected Socket socket;

	/**
	 * This field holds a reference to an {@link InputStream} object used to
	 * receive data from the remote endpoint.
	 */
	protected InputStream inputStream;

	/**
	 * This field holds a reference to an {@link OutputStream} object used to
	 * send data to the remote endpoint.
	 */
	protected OutputStream outputStream;

	/**
	 * UNDER CONSTRUCTION
	 */
	protected boolean localEcho = true;

	/**
	 * This constructor just initializes some internal object state from its
	 * arguments.
	 */
	public TelnetConnection(TelnetConnector terminalControl, Socket socket) throws IOException {
		super();

		Logger.log("entered"); //$NON-NLS-1$

		this.terminalControl = terminalControl;
		this.socket = socket;

		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();

		initializeOptions();
	}

	/**
	 * Returns true if the TCP connection represented by this object is
	 * connected, false otherwise.
	 */
	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	/**
	 * Returns true if the TCP connection represented by this object is
	 * connected and the remote endpoint is a TELNET server, false otherwise.
	 */
	public boolean isRemoteTelnetServer() {
		return remoteIsTelnetServer;
	}

	/**
	 * This method sets the terminal width and height to the supplied values. If
	 * either new value differs from the corresponding old value, we initiate a
	 * NAWS subnegotiation to inform the remote endpoint of the new terminal
	 * size.
	 */
	public void setTerminalSize(int newWidth, int newHeight) {
		Logger.log("Setting new size: width = " + newWidth + ", height = " + newHeight); //$NON-NLS-1$ //$NON-NLS-2$
		if (!isConnected() || !isRemoteTelnetServer())
			return;
		boolean sizeChanged = false;

		if (newWidth != width || newHeight != height)
			sizeChanged = true;

		width = newWidth;
		height = newHeight;

		if (sizeChanged && remoteIsTelnetServer && localOptions[TELNET_OPTION_NAWS].isEnabled()) {
			Integer[] sizeData = { new Integer(width), new Integer(height) };

			localOptions[TELNET_OPTION_NAWS].sendSubnegotiation(sizeData);
		}
	}

	/**
	 * Returns true if local echoing is enabled for this TCP connection, false
	 * otherwise.
	 */
	public boolean localEcho() {
		return localEcho;
	}

	private void displayTextInTerminal(String string) {
		terminalControl.displayTextInTerminal(string);
	}

	/**
	 * This method runs in its own thread. It reads raw bytes from the TELNET
	 * connection socket, processes any TELNET protocol bytes (and removes
	 * them), and passes the remaining bytes to a TerminalDisplay object for
	 * display.
	 */
	public void run() {
		Logger.log("Entered"); //$NON-NLS-1$

		try {
			while (socket.isConnected()) {
				int nRawBytes = inputStream.read(rawBytes);

				if (nRawBytes == -1) {
					// End of input on inputStream.
					Logger.log("End of input reading from socket!"); //$NON-NLS-1$

					// Announce to the user that the remote endpoint has closed the
					// connection.

					displayTextInTerminal(TelnetMessages.CONNECTION_CLOSED_BY_FOREIGN_HOST);

					// Tell the ITerminalControl object that the connection is
					// closed.
					terminalControl.setState(TerminalState.CLOSED);
					break;
				} else {
					// Process any TELNET protocol data that we receive. Don't
					// send any TELNET protocol data until we are sure the remote
					// endpoint is a TELNET server.

					int nProcessedBytes = processTelnetProtocol(nRawBytes);

					if (nProcessedBytes > 0) {
						terminalControl.getRemoteToTerminalOutputStream().write(processedBytes, 0, nProcessedBytes);
					}
				}
			}
		} catch (SocketException ex) {
			String message = ex.getMessage();

			// A "socket closed" exception is normal here. It's caused by the
			// user clicking the disconnect button on the Terminal view toolbar.

			if (message != null && !message.equalsIgnoreCase("Socket closed") && !message.equalsIgnoreCase("Connection reset")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				Logger.logException(ex);
			}

		} catch (Exception ex) {
			Logger.logException(ex);
		} finally {
			// Tell the ITerminalControl object that the connection is closed.
			terminalControl.setState(TerminalState.CLOSED);
			try { inputStream.close(); } catch(IOException ioe) { /*ignore*/ }
			try { outputStream.close(); } catch(IOException ioe) { /*ignore*/ }
		}
	}

	/**
	 * This method initializes the localOptions[] and remoteOptions[] arrays so
	 * that they contain references to TelnetOption objects representing our
	 * desired state for each option. The goal is to achieve server-side
	 * echoing, suppression of Go Aheads, and to send the local terminal type
	 * and size to the remote endpoint.
	 */
	protected void initializeOptions() {
		// First, create all the TelnetOption objects in the "undesired" state.

		for (int i = 0; i < localOptions.length; ++i) {
			localOptions[i] = new TelnetOption((byte) i, false, true, outputStream);
		}

		for (int i = 0; i < localOptions.length; ++i) {
			remoteOptions[i] = new TelnetOption((byte) i, false, false, outputStream);
		}

		// Next, set some of the options to the "desired" state. The options we
		// desire to be enabled are as follows:
		//
		// TELNET Option Desired for Desired for
		// Name and Code Local Endpoint Remote Endpoint
		// --------------------- -------------- ---------------
		// Echo (1) No Yes
		// Suppress Go Ahead (3) Yes Yes
		// Terminal Type (24) Yes Yes
		// NAWS (31) Yes Yes
		//
		// All other options remain in the "undesired" state, and thus will be
		// disabled (since either endpoint can force any option to be disabled by simply
		// answering WILL with DONT and DO with WONT).

		localOptions[TELNET_OPTION_ECHO].setDesired(false);
		remoteOptions[TELNET_OPTION_ECHO].setDesired(true);

		localOptions[TELNET_OPTION_SUPPRESS_GA].setDesired(true);
		remoteOptions[TELNET_OPTION_SUPPRESS_GA].setDesired(true);

		localOptions[TELNET_OPTION_TERMINAL_TYPE].setDesired(true);
		remoteOptions[TELNET_OPTION_TERMINAL_TYPE].setDesired(true);

		localOptions[TELNET_OPTION_NAWS].setDesired(true);
		remoteOptions[TELNET_OPTION_NAWS].setDesired(true);
	}

	/**
	 * Process TELNET protocol data contained in the first <i>count</i> bytes
	 * of <i>rawBytes</i>. This function preserves its state between calls,
	 * because a multi-byte TELNET command might be split between two (or more)
	 * calls to this function. The state is preserved in field <i>telnetState</i>.
	 * This function implements an FSA that recognizes TELNET option codes.
	 * TELNET option sub-negotiation is delegated to instances of TelnetOption.
	 *
	 * @return The number of bytes remaining in the buffer after removing all
	 *         TELNET protocol bytes.
	 */
	//TELNET option state is stored in instances of TelnetOption.
	protected int processTelnetProtocol(int count) {
		// This is too noisy to leave on all the time.
		// Logger.log("Processing " + count + " bytes of data.");

		int nextProcessedByte = 0;

		for (int byteIndex = 0; byteIndex < count; ++byteIndex) {
			// It is possible for control to flow through the below code such
			// that nothing happens. This happens when array rawBytes[] contains no
			// TELNET protocol data.

			byte inputByte = rawBytes[byteIndex];

			switch (telnetState) {
			case STATE_INITIAL:
				if (inputByte == TELNET_IAC) {
					telnetState = STATE_IAC_RECEIVED;
				} else {
					// It's not an IAC code, so just append it to
					// processedBytes.

					processedBytes[nextProcessedByte++] = rawBytes[byteIndex];
				}
				break;

			case STATE_IAC_RECEIVED:
				switch (inputByte) {
				case TELNET_IAC:
					// Two IAC bytes in a row are translated into one byte with
					// the
					// value 0xff.

					processedBytes[nextProcessedByte++] = (byte) 0xff;
					telnetState = STATE_INITIAL;
					break;

				case TELNET_WILL:
					telnetState = STATE_WILL_RECEIVED;
					break;

				case TELNET_WONT:
					telnetState = STATE_WONT_RECEIVED;
					break;

				case TELNET_DO:
					telnetState = STATE_DO_RECEIVED;
					break;

				case TELNET_DONT:
					telnetState = STATE_DONT_RECEIVED;
					break;

				case TELNET_SB:
					telnetState = STATE_SUBNEGOTIATION_STARTED;
					break;

				// Commands to consume and ignore.

				// Data Mark (DM). This is sent by a TELNET server following an
				// IAC sent as TCP urgent data. It should cause the client to
				// skip all not yet processed non-TELNET-protocol data preceding the
				// DM byte. However, Java 1.4.x has no way to inform clients of
				// class Socket that urgent data is available, so we simply ignore the
				// "IAC DM" command. Since the IAC is sent as TCP urgent data,
				// the Socket must be put into OOB-inline mode via a call to
				// setOOBInline(true), otherwise the IAC is silently dropped by
				// Java and only the DM arrives (leaving the user to see a
				// spurious ISO Latin-1 character).
				case TELNET_DM:

				case TELNET_NOP: // No-op.
				case TELNET_GA: // Go Ahead command. Meaningless on a full-duplex link.
				case TELNET_IP: // Interupt Process command. Server should never send this.
				case TELNET_AO: // Abort Output command. Server should never send this.
				case TELNET_AYT: // Are You There command. Server should never send this.
				case TELNET_EC: // Erase Character command. Server should never send this.
				case TELNET_EL: // Erase Line command. Server should never send this.
					telnetState = STATE_INITIAL;
					break;

				default:
					// Unrecognized command! This should never happen.
					Logger.log("processTelnetProtocol: UNRECOGNIZED TELNET PROTOCOL COMMAND: " + //$NON-NLS-1$
							inputByte);
					telnetState = STATE_INITIAL;
					break;
				}
				break;

			// For the next four cases, WILL and WONT commands affect the state
			// of remote options, and DO and DONT commands affect the state of
			// local options.

			case STATE_WILL_RECEIVED:
				Logger.log("Received WILL " + localOptions[inputByte].optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				remoteOptions[inputByte].handleWill();
				telnetState = STATE_INITIAL;
				telnetServerDetected();
				break;

			case STATE_WONT_RECEIVED:
				Logger.log("Received WONT " + localOptions[inputByte].optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				remoteOptions[inputByte].handleWont();
				telnetState = STATE_INITIAL;
				telnetServerDetected();
				break;

			case STATE_DO_RECEIVED:
				Logger.log("Received DO " + localOptions[inputByte].optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				localOptions[inputByte].handleDo();
				telnetState = STATE_INITIAL;
				telnetServerDetected();
				break;

			case STATE_DONT_RECEIVED:
				Logger.log("Received DONT " + localOptions[inputByte].optionName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
				localOptions[inputByte].handleDont();
				telnetState = STATE_INITIAL;
				telnetServerDetected();
				break;

			case STATE_SUBNEGOTIATION_STARTED:
				Logger.log("Starting subnegotiation for option " + //$NON-NLS-1$
						localOptions[inputByte].optionName() + "."); //$NON-NLS-1$

				// First, zero out the array of received subnegotiation butes.

				for (int i = 0; i < receivedSubnegotiation.length; ++i)
					receivedSubnegotiation[i] = 0;

				// Forget about any previous subnegotiation errors.

				ignoreSubnegotiation = false;

				// Then insert this input byte into the array and enter state
				// STATE_RECEIVING_SUBNEGOTIATION, where we will gather the
				// remaining subnegotiation bytes.

				nextSubnegotiationByteIndex = 0;
				receivedSubnegotiation[nextSubnegotiationByteIndex++] = inputByte;
				telnetState = STATE_RECEIVING_SUBNEGOTIATION;
				break;

			case STATE_RECEIVING_SUBNEGOTIATION:
				if (inputByte == TELNET_IAC) {
					// Handle double IAC bytes. From RFC 855: "if parameters
					// in an option 'subnegotiation' include a byte with a value
					// of 255, it is necessary to double this byte in accordance
					// the general TELNET rules."

					if (nextSubnegotiationByteIndex > 0
							&& receivedSubnegotiation[nextSubnegotiationByteIndex - 1] == TELNET_IAC) {
						// The last input byte we received in this
						// subnegotiation was IAC, so this is a double IAC. Leave the previous IAC
						// in the receivedSubnegotiation[] array and drop the current
						// one (thus translating a double IAC into a single IAC).

						Logger.log("Double IAC in subnegotiation translated into single IAC."); //$NON-NLS-1$
						break;
					}

					// Append the IAC byte to receivedSubnegotiation[]. If there
					// is no room for the IAC byte, it overwrites the last byte,
					// because we need to know when the subnegotiation ends, and that is
					// marked by an "IAC SE" command.

					if (nextSubnegotiationByteIndex < receivedSubnegotiation.length) {
						receivedSubnegotiation[nextSubnegotiationByteIndex++] = inputByte;
					} else {
						receivedSubnegotiation[receivedSubnegotiation.length - 1] = inputByte;
					}
					break;
				}

				// Handle an "IAC SE" command, which marks the end of the
				// subnegotiation. An SE byte by itself might be a legitimate
				// part of the subnegotiation data, so don't do anything unless the SE
				// is immediately preceded by an IAC.

				if (inputByte == TELNET_SE && receivedSubnegotiation[nextSubnegotiationByteIndex - 1] == TELNET_IAC) {
					Logger.log("Found SE code marking end of subnegotiation."); //$NON-NLS-1$

					// We are done receiving the subnegotiation command. Now
					// process it. We always use the option object stored in array
					// localOptions[] to process the received subnegotiation.
					// This is an arbitrary decision, but it is sufficient for handling
					// options TERMINAL-TYPE and NAWS, which are the only options that
					// we subnegotiate (presently). If, in the future,subnegotiations
					// need to be handled by option objects stored in both
					// localOptions[] and remoteOptions[], then some mechanism
					// to choose the correct option object must be implemented.
					//
					// Also, if ignoreSubnegotiation is true, there was an error
					// while receiving the subnegotiation, so we must not process the
					// command, and instead just return to the initial state.

					if (!ignoreSubnegotiation) {
						// Remove the trailing IAC byte from
						// receivedSubnegotiation[].

						receivedSubnegotiation[nextSubnegotiationByteIndex - 1] = 0;

						int subnegotiatedOption = receivedSubnegotiation[0];

						localOptions[subnegotiatedOption].handleSubnegotiation(receivedSubnegotiation,
								nextSubnegotiationByteIndex);
					} else {
						Logger.log("NOT CALLING handleSubnegotiation() BECAUSE OF ERRORS!"); //$NON-NLS-1$
					}

					// Return to the initial state.

					telnetState = STATE_INITIAL;
				}

				// Check whether the receivedSubnegotiation[] array is full.

				if (nextSubnegotiationByteIndex >= receivedSubnegotiation.length) {
					// This should not happen. Array receivedSubnegotiation can
					// hold 128 bytes, and no TELNET option that we perform
					// subnegotiation for requires that many bytes in a subnegotiation command.
					// In the interest of robustness, we handle this case by ignoring all
					// remaining subnegotiation bytes until we receive the IAC SE
					// command that ends the subnegotiation. Also, we set
					// ignoreSubnegotiation to true to prevent a call to
					// handleSubnegotiation() when the IAC SE command arrives.

					Logger.log("SUBNEGOTIATION BUFFER FULL!"); //$NON-NLS-1$
					ignoreSubnegotiation = true;
				} else {
					Logger.log("Recording subnegotiation byte " + (inputByte & 0xff)); //$NON-NLS-1$

					receivedSubnegotiation[nextSubnegotiationByteIndex++] = inputByte;
				}
				break;

			default:
				// This should _never_ happen! If it does, it means there is a
				// bug in this FSA. For robustness, we return to the initial state.

				Logger.log("INVALID TELNET STATE: " + telnetState); //$NON-NLS-1$
				telnetState = STATE_INITIAL;
				break;
			}
		}

		// Return the number of bytes of processed data (i.e., number of bytes
		// of raw data minus TELNET control bytes). This value can be zero.

		return nextProcessedByte;
	}

	/**
	 * This method is called whenever we receive a valid TELNET protocol command
	 * from the remote endpoint. When it is called for the first time for this
	 * connection, we negotiate all options that we desire to be enabled.
	 * <p>
	 *
	 * This method does not negotiate options that we do not desire to be
	 * enabled, because all options are initially disabled.
	 * <p>
	 */
	protected void telnetServerDetected() {
		if (!remoteIsTelnetServer) {
			// This block only executes once per TelnetConnection instance.

			localEcho = false;

			Logger.log("Detected TELNET server."); //$NON-NLS-1$

			remoteIsTelnetServer = true;

			for (int i = 0; i < localOptions.length; ++i) {
				if (localOptions[i].isDesired()) {
					localOptions[i].negotiate();
				}
			}

			for (int i = 0; i < remoteOptions.length; ++i) {
				if (remoteOptions[i].isDesired()) {
					remoteOptions[i].negotiate();
				}
			}
		}
	}
}
