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
import org.eclipse.rse.model.ISystemContainer;
import org.eclipse.rse.model.ISystemContentsType;

/**
 * @author dmcknigh
 */
public interface IRemoteContainer extends ISystemContainer
{
    
	/**
     * Returns whether the object has contents of a particular type associated with the specified filter string. 
     * @param contentsType type of contents
     * @param filter criteria for contained contents 
     * @return <code>true</code> if the object has contents, <code>false</code> otherwise.
     */
    public boolean hasContents(ISystemContentsType contentsType, String filter);
                
    /**
	 * Returns the contents of the object. 
	 * @param contentsType type of contents
	 * @param filter criteria for contained contents.
	 * @return an array of contents.
	 */
    public Object[] getContents(ISystemContentsType contentsType, String filter);

    /*
     * Replace occurrences of cached object with new object
     */
    public void replaceContent(Object oldObject, Object newObject);
     
    /**
     * Sets the contents of this object that match a particular filter
     * @param contentsType type of contents
     * @param filter matching criteria for the contained objects
     * @param con the contained objects that match the filter
     */
    public void setContents(ISystemContentsType contentsType, String filter, Object[] con);
    
    /**
     * Copies the persistable contents from this one to another one
     * @param container the container to copy contents to
     */
    public void copyContentsTo(IRemoteContainer container);
}