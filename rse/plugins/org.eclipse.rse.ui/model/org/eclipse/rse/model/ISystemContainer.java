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

package org.eclipse.rse.model;

/**
 * @author dmcknigh
 */
public interface ISystemContainer 
{
	
	/**
     * Returns whether the object has contents of a particular type.
     * @param contentsType type of contents 
     * @return <code>true</code> if the object has contents, <code>false</code> otherwise.
     */
    public boolean hasContents(ISystemContentsType contentsType);
            
    /**
	 * Returns all the contents of the object (combining results of all filters 
	 * @param contentsType type of contents
	 * @return an array of contents.
	 */
    public Object[] getContents(ISystemContentsType contentsType);

    
    /**
     * Indicates whether the cached object is stale
     * @return whether the container is stale
     */
    public boolean isStale();
    
    /**
     * Marks the object as stale or not
     * @param isStale whether the object is to be marked stale or not
     */
    public void markStale(boolean isStale);
     
    /**
     * Marks the object as stale or not
     * @param isStale whether the object is to be marked stale or not
     * @param indicates whether or not to clear the cache
     */
    public void markStale(boolean isStale, boolean clearCache);

}