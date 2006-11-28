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

package org.eclipse.dstore.core.model;

public interface IDataStoreConstants 
{
    public static final String PASSWORD_EXPIRED = "password expired"; //$NON-NLS-1$
    public static final String NEW_PASSWORD_INVALID = "new password not valid"; //$NON-NLS-1$
    public static final String AUTHENTICATION_FAILED = "Authentification Failed"; //$NON-NLS-1$
    public static final String CONNECTED = "connected"; //$NON-NLS-1$
    public static final String UNKNOWN_PROBLEM = "unknown problem connecting to server"; //$NON-NLS-1$
    public static final String SERVER_FAILURE = "server failure: "; //$NON-NLS-1$
    public static final String ATTEMPT_RECONNECT = "attempt reconnect"; //$NON-NLS-1$
    public static final String PORT_OUT_RANGE = "specified port out of range:"; //$NON-NLS-1$
    
    public static final String DATASTORE_SPIRIT_DESCRIPTOR = "datastore.spirit"; //$NON-NLS-1$
    public static final String C_START_SPIRIT = "C_START_SPIRIT"; //$NON-NLS-1$
}
