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
 *   Ioana Grigoropol (Intel)      - [411343] Provide access to readers in host shell
 ********************************************************************************/

package org.eclipse.rse.services.shells;

import java.io.BufferedReader;


public abstract class AbstractHostShell implements IHostShell
{

	public void addOutputListener(IHostShellOutputListener listener)
	{
		IHostShellOutputReader outReader = getStandardOutputReader();
		if (outReader != null)
		{
			outReader.addOutputListener(listener);
		}
		IHostShellOutputReader errReader = getStandardErrorReader();
		if (errReader != null)
		{
			errReader.addOutputListener(listener);
		}
	}
	
	/**
	 * @since 3.3
	 */
	public BufferedReader getReader(boolean isErrorReader) {
		return null;
	}

}