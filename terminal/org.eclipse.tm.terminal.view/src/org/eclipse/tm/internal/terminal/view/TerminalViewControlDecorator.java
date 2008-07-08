/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import java.io.UnsupportedEncodingException;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.internal.terminal.control.ICommandInputField;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

// TODO (scharf): this decorator is only there to deal with the common
// actions. Find a better solution.
public class TerminalViewControlDecorator implements ITerminalViewControl {
	ITerminalViewControl fViewContoler;

	public void clearTerminal() {
		fViewContoler.clearTerminal();
	}

	public void connectTerminal() {
		fViewContoler.connectTerminal();
	}

	public void copy() {
		fViewContoler.copy();
	}

	public void disconnectTerminal() {
		fViewContoler.disconnectTerminal();
	}

	public void disposeTerminal() {
		fViewContoler.disposeTerminal();
	}

	public int getBufferLineLimit() {
		return fViewContoler.getBufferLineLimit();
	}

	public Clipboard getClipboard() {
		return fViewContoler.getClipboard();
	}

	public ICommandInputField getCommandInputField() {
		return fViewContoler.getCommandInputField();
	}

	public ITerminalConnector[] getConnectors() {
		return fViewContoler.getConnectors();
	}

	public Control getControl() {
		return fViewContoler.getControl();
	}

	public String getEncoding() {
		return fViewContoler.getEncoding();
	}

	public Font getFont() {
		return fViewContoler.getFont();
	}

	public Control getRootControl() {
		return fViewContoler.getRootControl();
	}

	public String getSelection() {
		return fViewContoler.getSelection();
	}

	public String getSettingsSummary() {
		return fViewContoler.getSettingsSummary();
	}

	public TerminalState getState() {
		return fViewContoler.getState();
	}

	public ITerminalConnector getTerminalConnector() {
		return fViewContoler.getTerminalConnector();
	}

	public boolean isConnected() {
		return fViewContoler.isConnected();
	}

	public boolean isDisposed() {
		return fViewContoler.isDisposed();
	}

	public boolean isEmpty() {
		return fViewContoler.isEmpty();
	}

	public boolean isScrollLock() {
		return fViewContoler.isScrollLock();
	}

	public void paste() {
		fViewContoler.paste();
	}

	public boolean pasteString(String string) {
		return fViewContoler.pasteString(string);
	}

	public void selectAll() {
		fViewContoler.selectAll();
	}

	public void sendKey(char arg0) {
		fViewContoler.sendKey(arg0);
	}

	public void setBufferLineLimit(int bufferLineLimit) {
		fViewContoler.setBufferLineLimit(bufferLineLimit);
	}

	public void setCommandInputField(ICommandInputField inputField) {
		fViewContoler.setCommandInputField(inputField);
	}

	public void setConnector(ITerminalConnector connector) {
		fViewContoler.setConnector(connector);
	}

	public void setEncoding(String encoding) throws UnsupportedEncodingException {
		fViewContoler.setEncoding(encoding);
	}

	public void setFocus() {
		fViewContoler.setFocus();
	}

	public void setFont(Font font) {
		fViewContoler.setFont(font);
	}

	public void setInvertedColors(boolean invert) {
		fViewContoler.setInvertedColors(invert);
	}

	public void setScrollLock(boolean on) {
		fViewContoler.setScrollLock(on);
	}

	public ITerminalViewControl getViewContoler() {
		return fViewContoler;
	}

	public void setViewContoler(ITerminalViewControl viewContoler) {
		fViewContoler = viewContoler;
	}
}
