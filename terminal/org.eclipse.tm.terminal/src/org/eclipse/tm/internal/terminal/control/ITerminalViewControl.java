/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnectorInfo;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

/**
 * @author Michael Scharf
 *
 */
public interface ITerminalViewControl {
    boolean isEmpty();
	void setFont(Font font);
	void setInvertedColors(boolean invert);
	Font getFont();
	Control getControl();
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
    ITerminalConnectorInfo[] getConnectors();
    void setFocus();
    ITerminalConnectorInfo getTerminalConnectorInfo();
    void setConnector(ITerminalConnectorInfo connector);
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
}