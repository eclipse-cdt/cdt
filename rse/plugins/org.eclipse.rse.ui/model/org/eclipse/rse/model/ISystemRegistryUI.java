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
package org.eclipse.rse.model;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.swt.dnd.Clipboard;

/**
 * Registry or front door for all remote system connections.
 */
public interface ISystemRegistryUI extends ISystemRegistry {

    /**
     * Update an existing host given the new information.
     * This method:
     * <ul>
     *  <li>calls the setXXX methods on the given host object, updating the information in it.
     *  <li>save the host's host pool to disk
     *  <li>fires an ISystemResourceChangeEvent event of type EVENT_CHANGE to all registered listeners
     *  <li>if the system type or host name is changed, calls disconnect on each associated subsystem.
     *       We must do this because a host name changes fundamentally affects the connection, 
     *       rendering any information currently displayed under
     *       that host obsolete.
     * </ul>
     * <p>
     * @param host the host to be updated
     * @param systemType system type matching one of the system type names defined via the
     *                    systemTypes extension point.
     * @param connectionName unique connection name.
     * @param hostName ip name of host.
     * @param description optional description of the host. Can be null.
     * @param defaultUserId userId to use as the default for the subsystems under this host.
     * @param defaultUserIdLocation one of the constants in {@link org.eclipse.rse.core.IRSEUserIdConstants}
     *   that tells us where to set the user Id
     */
    public void updateHost(IHost host, String systemType, String connectionName,
                                 String hostName, String description,
                                 String defaultUserId, int defaultUserIdLocation);
    
    /**
     * Returns the clipboard used for copy actions
     */
    public Clipboard getSystemClipboard();
}
