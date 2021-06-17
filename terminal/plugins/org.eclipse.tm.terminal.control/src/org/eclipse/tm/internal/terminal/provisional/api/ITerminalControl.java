/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [204796] Terminal should allow setting the encoding to use
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 * Anton Leherbauer (Wind River) - [433751] Add option to enable VT100 line wrapping mode
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Represents the terminal view as seen by a terminal connection.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the <a href="http://www.eclipse.org/tm/">Target Management</a> team.
 * </p>
 *
 * @author Michael Scharf
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITerminalControl {

	/**
	 * @return the current state of the connection
	 */
	TerminalState getState();

	/**
	 * @param state
	 */
	void setState(TerminalState state);

	/**
	 * Setup the terminal control within the given parent composite.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
	void setupTerminal(Composite parent);

	/**
	 * A shell to show dialogs.
	 * @return the shell in which the terminal is shown.
	 */
	Shell getShell();

	/**
	 * Set the encoding that the Terminal uses to decode bytes from the
	 * Terminal-to-remote-Stream into Unicode Characters used in Java; or, to
	 * encode Characters typed by the user into bytes sent over the wire to the
	 * remote.
	 *
	 * By default, the local Platform Default Encoding is used. Also note that
	 * the encoding must not be applied in case the terminal stream is processed
	 * by some data transfer protocol which requires binary data.
	 *
	 * Validity of the encoding set here is not checked. Since some encodings do
	 * not cover the entire range of Unicode characters, it can happen that a
	 * particular Unicode String typed in by the user can not be encoded into a
	 * byte Stream with the encoding specified. and UnsupportedEncodingException
	 * will be thrown in this case at the time the String is about to be
	 * processed.
	 *
	 * The concrete encoding to use can either be specified manually by a user,
	 * by means of a dialog, or a connector can try to obtain it automatically
	 * from the remote side e.g. by evaluating an environment variable such as
	 * LANG on UNIX systems.
	 *
	 * @since org.eclipse.tm.terminal 2.0
	 * @deprecated Use {@link #setCharset(Charset)} and do the error handling in the UI code.
	 */
	@Deprecated
	void setEncoding(String encoding) throws UnsupportedEncodingException;

	/**
	 * Set the charset that the Terminal uses to decode bytes from the
	 * Terminal-to-remote-Stream into Unicode Characters used in Java; or, to
	 * encode Characters typed by the user into bytes sent over the wire to the
	 * remote.
	 *
	 * By default, the local Platform Default charset is used. Also note that
	 * the encoding must not be applied in case the terminal stream is processed
	 * by some data transfer protocol which requires binary data.
	 *
	 * Validity of the charset set here is not checked. Since some encodings do
	 * not cover the entire range of Unicode characters, it can happen that a
	 * particular Unicode String typed in by the user can not be encoded into a
	 * byte Stream with the encoding specified. and UnsupportedEncodingException
	 * will be thrown in this case at the time the String is about to be
	 * processed.
	 *
	 * The concrete encoding to use can either be specified manually by a user,
	 * by means of a dialog, or a connector can try to obtain it automatically
	 * from the remote side e.g. by evaluating an environment variable such as
	 * LANG on UNIX systems.
	 *
	 * @param charset Charset to use, or <code>null</code> for platform's default charset.
	 *
	 * @since 5.3
	 */
	void setCharset(Charset charset);

	/**
	 * Return the current encoding. That's interesting when the previous
	 * setEncoding() call failed and the fallback default encoding should be
	 * queried, such that e.g. a combobox with encodings to choose can be
	 * properly initialized.
	 *
	 * @return the current Encoding of the Terminal.
	 * @since org.eclipse.tm.terminal 2.0
	 * @deprecated Use {@link #getCharset()} and call {@link Charset#name()} on the result
	 */
	@Deprecated
	String getEncoding();

	/**
	 * Return the current charset.
	 *
	 * @return the non-<code>null</code> current charset of the Terminal
	 * @since 5.3
	 */
	Charset getCharset();

	/**
	 * Show a text in the terminal. If puts newlines at the beginning and the
	 * end.
	 *
	 * @param text TODO: Michael Scharf: Is this really needed?
	 */
	void displayTextInTerminal(String text);

	/**
	 * @return a stream used to write to the terminal. Any bytes written to this
	 * stream appear in the terminal or are interpreted by the emulator as
	 * control sequences. The stream in the opposite direction, terminal
	 * to remote is in {@link ITerminalConnector#getTerminalToRemoteStream()}.
	 */
	OutputStream getRemoteToTerminalOutputStream();

	/**
	 * Set the title of the terminal view.
	 * @param title
	 */
	void setTerminalTitle(String title);

	/**
	 * Show an error message during connect.
	 * @param msg
	 * TODO: Michael Scharf: Should be replaced by a better error notification mechanism!
	 */
	void setMsg(String msg);

	/**
	 * Sets if or if not the terminal view control should try to reconnect
	 * the terminal connection if the user hits ENTER in a closed terminal.
	 * <p>
	 * Reconnect on ENTER if terminal is closed is enabled by default.
	 *
	 * @param on <code>True</code> to enable the reconnect, <code>false</code> to disable it.
	 */
	void setConnectOnEnterIfClosed(boolean on);

	/**
	 * Returns if or if not the terminal view control should try to reconnect
	 * the terminal connection if the user hits ENTER in a closed terminal.
	 *
	 * @return <code>True</code> the reconnect is enabled, <code>false</code> if disabled.
	 */
	boolean isConnectOnEnterIfClosed();

	/**
	 * Enables VT100 line wrapping mode (default is off).
	 * This corresponds to the VT100 'eat_newline_glitch' terminal capability.
	 * If enabled, writing to the rightmost column does not cause
	 * an immediate wrap to the next line. Instead the line wrap occurs on the
	 * next output character.
	 *
	 * @param enable  whether to enable or disable VT100 line wrapping mode
	 */
	void setVT100LineWrapping(boolean enable);

	/**
	 * @return whether VT100 line wrapping mode is enabled
	 */
	boolean isVT100LineWrapping();
}
