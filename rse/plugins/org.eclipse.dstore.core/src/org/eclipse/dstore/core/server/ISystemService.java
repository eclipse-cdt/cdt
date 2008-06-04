/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 *  The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: Noriaki Takatsu and Masao Nishimoto
 *
 * Contributors:
 *  Noriaki Takatsu (IBM)  - [220126] [dstore][api][breaking] Single process server for multiple clients
 *******************************************************************************/


package org.eclipse.dstore.core.server;

import org.eclipse.dstore.core.model.Client;

/**
 * @since 3.0
 */
public interface ISystemService
{
	/**
     * This method is used to establish a thread-level security.
     *
     * @param client the object of the client
     */
    public void setThreadSecurity(Client client);

    /**
     * This method is used to execute run() in a thread assigned
     * from thread pools.
     *
     * @param securedThread the securedThread object that implements
     *        Runnable.
     */
    public void executeThread(SecuredThread securedThread);
}
