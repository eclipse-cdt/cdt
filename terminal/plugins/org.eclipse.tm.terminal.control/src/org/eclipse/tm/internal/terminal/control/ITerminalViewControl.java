/*******************************************************************************
 * Copyright (c) 2006, 2021 Wind River Systems, Inc. and others.
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
 * Martin Oberhuber (Wind River) - [265352][api] Allow setting fonts programmatically
 * Davy Landman (CWI) - [475267][api] Allow custom mouse listeners
 ******************************************************************************/
package org.eclipse.tm.internal.terminal.control;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

/**
 * @author Michael Scharf
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITerminalViewControl {
	/**
	 * Set the encoding that the Terminal uses to decode byte streams into
	 * characters.
	 *
	 * @see ITerminalControl#setEncoding(String)
	 * @since org.eclipse.tm.terminal 2.0
	 * @deprecated Use {@link #setCharset(Charset)} and do the error handling in the UI code.
	 */
	@Deprecated
	void setEncoding(String encoding) throws UnsupportedEncodingException;

	/**
	 * Set the charset that the Terminal uses to decode byte streams into
	 * characters.
	 *
	 * @see ITerminalControl#setCharset(Charset)
	 * @since 5.3
	 */
	void setCharset(Charset charset);

	/**
	 * Get the Terminal's current encoding.
	 *
	 * @return the current Encoding of the Terminal.
	 * @see ITerminalControl#getEncoding()
	 * @since org.eclipse.tm.terminal 2.0
	 * @deprecated Use {@link #getCharset()} and call {@link Charset#name()} on the result
	 */
	@Deprecated
	String getEncoding();

	/**
	 * @return the non-<code>null</code> current Charset of the Terminal.
	 * @see ITerminalControl#getCharset()
	 * @since 5.3
	 */
	Charset getCharset();

	boolean isEmpty();

	/**
	 * Sets the Terminal font
	 * @deprecated use {@link #setFont(String)} in order to support bold and italic variants of the given font
	 * @param font
	 */
	@Deprecated
	void setFont(Font font);

	/**
	 * Sets the font for the Terminal, using a JFace symbolic font name, such
	 * that bold and italic variants can be leveraged.
	 * @since 3.2
	 * @param fontName
	 */
	void setFont(String fontName);

	void setInvertedColors(boolean invert);

	/**
	 * @since 5.1
	 */
	boolean isInvertedColors();

	Font getFont();

	/**
	 * @return the text control
	 */
	Control getControl();

	/**
	 * @return the root of all controls
	 */
	Control getRootControl();

	boolean isDisposed();

	void selectAll();

	void clearTerminal();

	void copy();

	void paste();

	String getSelection();

	TerminalState getState();

	Clipboard getClipboard();

	void disconnectTerminal();

	void disposeTerminal();

	String getSettingsSummary();

	ITerminalConnector[] getConnectors();

	void setFocus();

	ITerminalConnector getTerminalConnector();

	void setConnector(ITerminalConnector connector);

	void connectTerminal();

	/**
	 * @param write a single character to terminal
	 */
	void sendKey(char arg0);

	/**
	 * @param string write string to terminal
	 */
	public boolean pasteString(String string);

	boolean isConnected();

	/**
	 * @param inputField null means no input field is shown
	 */
	void setCommandInputField(ICommandInputField inputField);

	/**
	 * @return null or the current input field
	 */
	ICommandInputField getCommandInputField();

	/**
	 * @return the maximum number of lines to display
	 * in the terminal view. -1 means unlimited.
	 */
	public int getBufferLineLimit();

	/**
	 * @param bufferLineLimit the maximum number of lines to show
	 * in the terminal view. -1 means unlimited.
	 */
	public void setBufferLineLimit(int bufferLineLimit);

	boolean isScrollLock();

	void setScrollLock(boolean on);

	/**
	 * @since 4.1
	 * @param listener may be a {@link ITerminalMouseListener2} for extra callbacks
	 */
	void addMouseListener(ITerminalMouseListener listener);

	/**
	 * @since 4.1
	 * @param listener may be a {@link ITerminalMouseListener2} for extra callbacks
	 */
	void removeMouseListener(ITerminalMouseListener listener);

	/**
	 * @since 5.1
	 */
	void setTerminalTitle(String newTitle);

	/**
	 * @since 5.2
	 */
	String getHoverSelection();
}
