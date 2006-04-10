/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.widgets;

import org.eclipse.rse.core.subsystems.IServerLauncherProperties;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * An interface implemented by server launchers in order to prompt for the
 *  properties of that server launcher.
 * @see org.eclipse.rse.core.subsystems.IServerLauncherProperties
 */
public interface IServerLauncherForm
{
	/**
	 * Create the contents of the form
	 */
	public abstract Control createContents(Composite parent);
	
	/*
	 * Sets the hostname associated with this form
	 */
	public void setHostname(String hostname);
	
	/**
	 * Set the initial values for the widgets, based on the server launcher values
	 */
	public void initValues(IServerLauncherProperties launcher);
	/**
	 * Verify the contents of the widgets, when OK is pressed.
	 * Return true if all is well, false if an error found.
	 */
	public boolean verify();
	/**
	 * Update the actual values in the server launcher, from the widgets. Called on successful press of OK. 
	 * @return true if all went well, false if something failed for some reason.
	 */
	public boolean updateValues(IServerLauncherProperties launcher);
	
	/**
	 * Did anythign change?
	 * @return
	 */
	public boolean isDirty();
}