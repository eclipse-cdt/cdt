/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/
package org.eclipse.tm.terminal.control;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.tm.terminal.TerminalState;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.tm.terminal.ITerminalConnector;

/**
 * @author Michael Scharf
 *
 */
public interface ITerminalViewControl {
    boolean isEmpty();
    void onFontChanged();
    StyledText getCtlText();
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
    String getStatusString(String status);
    ITerminalConnector[] getConnectors();
    void setFocus();
    ITerminalConnector getTerminalConnection();
    void setConnector(ITerminalConnector connector);
    void connectTerminal();
    void sendKey(char arg0);
    boolean isConnected();

}