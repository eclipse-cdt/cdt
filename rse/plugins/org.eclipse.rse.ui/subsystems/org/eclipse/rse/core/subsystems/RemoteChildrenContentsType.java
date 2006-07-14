/********************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.core.subsystems;

import org.eclipse.rse.model.ISystemContentsType;

/**
 * Represents contents that are children of a container
 */
public class RemoteChildrenContentsType implements ISystemContentsType
{
    public static String CONTENTS_TYPE_CHILDREN = "contents_children";
    public static RemoteChildrenContentsType _instance = new RemoteChildrenContentsType();
    
    public static RemoteChildrenContentsType getInstance()
    {
        return _instance;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.rse.model.IRemoteContentsType#getType()
     */
    public String getType()
    {
        return CONTENTS_TYPE_CHILDREN;    
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.model.IRemoteContentsType#isPersistent()
     */
    public boolean isPersistent()
    {
        return false;
    }

}