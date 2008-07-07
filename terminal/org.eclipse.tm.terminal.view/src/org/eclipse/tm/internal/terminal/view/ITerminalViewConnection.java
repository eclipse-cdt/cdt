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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

/**
 * Represents a connection. The connection might be connected
 * or not.
 *
 */
public interface ITerminalViewConnection {

	/**
	 * @return the summary shown in the status line and
	 * in the drop down box of the connections
	 */
	String getFullSummary();

	/**
	 * @param name the name of the view
	 */
	void setPartName(String name);
	/**
	 * @return the name of the view (never null)
	 */
	String getPartName();
	
	/**
	 * @return an image that represents this connection
	 */
	ImageDescriptor getImageDescriptor();
	/**
	 * @return the control of this connection
	 */
	ITerminalViewControl getCtlTerminal();

	void saveState(ISettingsStore store);

	void loadState(ISettingsStore store);
	
	/**
	 * @return true if the input field is visible 
	 */
	boolean hasCommandInputField();
	/**
	 * @param on turns the input field on
	 */
	void setCommandInputField(boolean on);

	/**
	 * @param state changes of the state (might change the summary)
	 */
	void setState(TerminalState state);

	/**
	 * @param title used in the summary. If null the summary
	 * is created automatically
	 */
	void setTerminalTitle(String title);

	/**
	 * TODO: legacy (needed to read the old state)
	 * @param summary
	 */
	void setSummary(String summary);
}