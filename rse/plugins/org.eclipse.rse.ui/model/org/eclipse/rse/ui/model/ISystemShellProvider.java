/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 ********************************************************************************/
package org.eclipse.rse.ui.model;

import org.eclipse.swt.widgets.Shell;

/**
 * Interface that viewers implement in order to give RSE actions 
 * access to the Shell in which they are embedded.
 */
public interface ISystemShellProvider {

	/**
	 * This method will be called to return the shell for your viewer.
	 * @return the shell for your viewer.
	 */
	public Shell getShell();

}
