/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
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
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
s ********************************************************************************/

package org.eclipse.rse.core.events;

/**
 * Interface of event sent when a remote system resource changes.
 * 
 * These events are mainly for internal use. BPs/ISVs should instead 
 * fire and monitor for {@link org.eclipse.rse.core.events.ISystemModelChangeEvent}.
 * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
 */
public interface ISystemResourceChangeEvent
{	
    /**
     * Returns an object identifying the source of this event.
     * @see java.util.EventObject
     *
     * @return an object identifying the source of this event
     */
    public Object getSource();
    
	/**
	 * For multi-target events, return the array of source targets.
	 * @return array of source objects.
	 */
	public Object[] getMultiSource();
	
    /**
     * Returns the parent of the object source.
     * Only guaranteed to be set for additions and deletions.
     *
     * @return an object identifying the parent of the source of this event,
     *     or <code>null</code> if not applicable.
     */
    public Object getParent();
    
	/**
	 * Set the parent object of this event.
	 * @param parent the parent object.
	 */
	public void setParent(Object parent);
    
    /**
     * Returns the grandparent of the object source.
     * Only guaranteed to be set for special case events, which have a
     * special contract with the SystemView.
     *
     * @return an object identifying the grandparent of the source of this event
     */
    public Object getGrandParent();
    
	/**
	 * Return the position value for adding a new item.
	 * Used in ADD events. A negative number indicates an append operation.
	 * 
	 * @return position to add the new item to. 
	 */
	public int getPosition();
	
	/**
	 * Set the position value for adding a new item.
	 * Used in ADD events. A negative number indicates an append operation.
	 * 
	 * @param position zero-based position to insert the new item. 
	 */
	public void setPosition(int position);
	
	/**
	 * For relative add events, return the previous node this is being added after.
	 * @return the previous node for adding a new node after.
	 */
	public Object getRelativePrevious();
	
	/**
	 * For relative add events, set the previous node this is being added after.
	 * @param previousObject the object after which to add the new object.
	 */
	public void setRelativePrevious(Object previousObject);

    /**
     * Returns the type of the event.
     * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
     * @return a type that is one of the constants in ISystemResourceChangeEvents.
     */
    public int getType();
    
    /**
     * Set the type of this event
     * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
     * @param type a type that is one of the constants in ISystemResourceChangeEvents.
     */
    public void setType(int type);

}