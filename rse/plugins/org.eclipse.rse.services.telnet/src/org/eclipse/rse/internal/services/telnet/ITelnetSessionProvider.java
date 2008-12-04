/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Sheldon D'souza (Celunite)   - adapted from ISshSessionProvider
 * Sheldon D'souza (Celunite)   - [187301] support multiple telnet shells
 * Anna Dushistova (MontaVista) - [240523] [rseterminals] Provide a generic adapter factory that adapts any ITerminalService to an IShellService
 *******************************************************************************/
package org.eclipse.rse.internal.services.telnet;

import org.apache.commons.net.telnet.TelnetClient;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ITelnetSessionProvider {
	
	/**
	 * Create a new Commons.Net TelnetClient.
	 * @param monitor progress monitor
	 * @return a new Commons.Net TelnetClient for the given connection, already authenticated
	 * @throws Exception in case of any error
	 */
	public TelnetClient makeNewTelnetClient(IProgressMonitor monitor) throws Exception ;

	/**
	 * Initialize a new Commons.Net TelnetClient with a given ptyType.
	 * @param client telnet client already created
	 * @param monitor progress monitor
	 * @return authenticated client for the given connection
	 * @throws Exception in case of any error
	 */
	public TelnetClient makeNewTelnetClient(TelnetClient client, IProgressMonitor monitor) throws Exception ;
	
}
