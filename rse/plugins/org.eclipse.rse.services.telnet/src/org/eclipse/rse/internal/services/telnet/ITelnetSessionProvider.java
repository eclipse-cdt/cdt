/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;

public interface ITelnetSessionProvider {

	/**
	 * Create a new Commons.Net TelnetClient, and authenticate it with the
	 * remote.
	 *
	 * @param monitor progress monitor
	 * @return a new Commons.Net TelnetClient for the given connection, already
	 *         authenticated
	 * @throws Exception in case of any error
	 */
	public TelnetClient makeNewTelnetClient(IProgressMonitor monitor) throws Exception ;

	/**
	 * Authenticate an existing Commons.Net TelnetClient connection with the
	 * remote, using the credentials known to RSE. Depending on configuration
	 * options, this may answer the remote "login:" and "password:" prompts to
	 * come up with an authenticated client.
	 *
	 * By passing in a pre-existing TelnetClient instance, this method allows
	 * for fine-tuning the TelnetClient options such as ECHO handling through
	 * the Commons.Net APIs before using RSE to authenticate the client.
	 *
	 * Example:
	 *
	 * <pre>
	 * TelnetClient client = new TelnetClient(&quot;vt100&quot;);
	 * client.addOptionHandler(new EchoOptionHandler(false, true, true, true));
	 * client = fSessionProvider.loginTelnetClient(client, new NullProgressMonitor());
	 * </pre>
	 *
	 * @param client telnet client already created
	 * @param monitor progress monitor
	 * @return authenticated client for the given connection, or
	 *         <code>null</code> in case the user cancelled the login through
	 *         the progress monitor. The passed-in client is disconnected in
	 *         this case.
	 * @throws SystemMessageException in case of an error while authenticating
	 *             (such as timeout, communications error, or failure matching
	 *             expected prompt). The passed-in TelnetClient remains
	 *             connected in this case.
	 * @since 2.0
	 */
	public TelnetClient loginTelnetClient(TelnetClient client, IProgressMonitor monitor) throws SystemMessageException;

}
