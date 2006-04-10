/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core.comm;

import java.net.Socket;

/**
 * This interface allows any class to use the RSE communications daemon to 
 * accept socket requests from a remote function.  When a the RSE communications
 * daemon accepts a socket request it reads the first 4 bytes, converts this to 
 * an integer and compares the integer against all registered 
 * ISystemCommunicationDaemonListeners, handing the socket off to the first match.
 */
public interface ISystemCommunicationsDaemonHandler {


	/**
	 * The handleRequest method is invoked by the RSE communications daemon when
	 * a new socket connection is established and the requestKey matches for this
	 * handler.  The handleRequest method will be invoked on a new thread.
	 * <br><br>
	 * It is the implementors responsability to close the socket and clean
	 * up all associated resources (like the InputStream) when finished.
	 * 
	 * @param socket The communications socket connected to the remote peer.  The only 
	 * thing read of the socket will have been the request key.
	 */
	public void handleRequest(Socket socket, int requestKey);

}