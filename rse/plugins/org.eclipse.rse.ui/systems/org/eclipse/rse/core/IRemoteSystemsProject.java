/********************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core;

import org.eclipse.core.resources.IProject;

/**
 * Therei is exactly one remote systems project. It is created by the plugin if it does 
 * not exist already. It is never created by the user.
 * <p>
 */
public interface IRemoteSystemsProject 
{
    /**
     * Returns the <code>IProject</code> on which this <code>IJavaProject</code>
     * was created. This is handle-only method.
     */
    IProject getProject();
}