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
 * Martin Oberhuber (Wind River) - [203500] Support encodings for SSH Sftp paths
 *******************************************************************************/
package org.eclipse.rse.internal.services.ssh;

import com.jcraft.jsch.Session;

public interface ISshSessionProvider 
{
	/* Return an active SSH session from a ConnectorService. */
	public Session getSession();
	
	/* Inform the connectorService that a session has been lost. */
	public void handleSessionLost();
	
	/* Return the encoding to be used for file and directory names */
	public String getControlEncoding();
	
}
