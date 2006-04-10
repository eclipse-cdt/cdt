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

/*
 * Common interface for representing different contents types of
 * artifacts that can be stored in an IRemoteContainer
 */
public interface ISystemContentsType 
{
    /*
     * Indicates the type of this contents
     */
    public String getType();
    
    /*
     * Indicates whether or not the contents
     * can be flushed or not when a container becomes
     * stale.
     */
    public boolean isPersistent();
}