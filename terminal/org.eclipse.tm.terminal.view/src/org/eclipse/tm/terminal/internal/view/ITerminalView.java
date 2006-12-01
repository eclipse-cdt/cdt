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

package org.eclipse.tm.terminal.internal.view;

public interface ITerminalView {

	/**
	 * Display a new Terminal view.  This method is called when the user clicks the New
	 * Terminal button in any Terminal view's toolbar.
	 */
	public void onTerminalNewTerminal();
	public void onTerminalConnect();
	public void onTerminalDisconnect();
	public void onTerminalSettings();
	public void onTerminalFontChanged();
	public void onEditCopy();
	public void onEditCut();
	public void onEditPaste();
	public void onEditClearAll();
	public void onEditSelectAll();
}
