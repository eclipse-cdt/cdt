/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder;

/**
 * A tool settings dialog is used to edit/view parameters passed
 * to a tool as part of a C/'C++ build step. It contains a build
 * configuration tab group.
 * 
 * @see ICToolTabGroup
 * @see ICToolTab
 */
public interface ICBuildConfigDialog {

	/**
	 * Adjusts the enable state of this dialog's buttons
	 * to reflect the state of the active tab group.
	 * <p>
	 * This may be called by to force a button state update.
	 */
	public void updateButtons();
	
	/**
	 * Updates the message (or error message) shown in the message line
	 * to  reflect the state of the currently active tab in the dialog.
	 * <p>
	 * This method may be called to force a message update.
	 */
	public void updateMessage();
	
	/**
	 * Sets the contents of the name field to the given name.
	 * 
	 * @param name new name value
	 */ 
	public void setName(String name);
	
	/**
	 * Returns the tabs currently being displayed, or
	 * <code>null</code> if none.
	 * 
	 * @return currently displayed tabs, or <code>null</code>
	 */
	public ICToolTab[] getTabs();
	
	/**
	 * Returns the currently active <code>ICToolTab</code>
	 * being displayed, or <code>null</code> if there is none.
	 * 
	 * @return currently active <code>ICToolTab</code>, or <code>null</code>.
	 */
	public ICToolTab getActiveTab();
}
