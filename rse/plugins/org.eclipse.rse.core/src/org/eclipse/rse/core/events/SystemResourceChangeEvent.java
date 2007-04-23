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
 ********************************************************************************/

package org.eclipse.rse.core.events;
import java.util.EventObject;

/**
 * Event object sent to SystemResourceChangeListeners when a
 * remote system object is created, changed, removed, etc.
 * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
 */
public class SystemResourceChangeEvent
	   extends EventObject
	   implements ISystemResourceChangeEvent
{
	private static final long serialVersionUID = 1;
	private Object parent,grandparent, prevObj;
	private Object[] multiSource;
	private int type;
	private int position = -1;
	
	/**
	 * Constructor for SystemResourceChangeEvent.
     * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
	 * @param source The object that was added,deleted,renamed,changed.
	 * @param type The type of event, one of ISystemChangeEvent constants.
	 * @param parent The parent of the object that was added or deleted.
	 */
	public SystemResourceChangeEvent(Object source, int type, Object parent)
	{
		super(source);
		setType(type);
		setParent(parent);
	}
	/**
	 * Constructor for SystemResourceChangeEvent when the source is multipe resources.
     * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
	 * @param source The array of objects that were added,deleted,renamed,changed.
	 * @param type The type of event, one of ISystemChangeEvent constants.
	 * @param parent The parent of the object that was added or deleted.
	 */
	public SystemResourceChangeEvent(Object[] source, int type, Object parent)
	{
		super(((source!=null) && (source.length>0)) ? source[0] : "nada"); // defect 42112 //$NON-NLS-1$
		this.multiSource = source;
		this.type = type;
		this.parent = parent;
	}	

	/**
	 * For multi-target events, return the array of source targets.
	 */
	public Object[] getMultiSource()
	{
		return multiSource;
	}

	/**
	 * Return the parent of the object added or removed.
	 * @see ISystemResourceChangeEvent#getParent()
	 */
	public Object getParent()
	{
		return parent;
	}
	/**
	 * Set the parent
	 */
	public void setParent(Object parent)
	{
		this.parent = parent;
	}

	/**
	 * Return the grand parent of the object added or removed.
	 * @see ISystemResourceChangeEvent#getParent()
	 */
	public Object getGrandParent()
	{
		return grandparent;
	}
	/**
	 * Set the grand parent of the object added or removed.
	 */
	public void setGrandParent(Object grandparent)
	{
		this.grandparent = grandparent;
	}
	/**
	 * Return the position value. Used in ADD events.
	 * @return position to add the new item to. A negative number indicates an append operation
	 */
	public int getPosition()
	{
		return position;
	}
	/**
	 * Set the position value. Used in ADD events.
	 * @param position zero-based position to insert the new item. A negative number indicates an append operation
	 */
	public void setPosition(int position)
	{
		this.position = position;
	}

	/**
	 * Return the type of the event (add, change, remove, rename, change, property change).
     * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
	 * @see org.eclipse.rse.core.events.ISystemResourceChangeEvent
	 * @see org.eclipse.rse.core.events.ISystemResourceChangeEvent#getType()
	 */
	public int getType()
	{
		return type;
	}
    /**
     * Set the type
     * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
     */
    public void setType(int type)
    {
    	this.type = type;
    }
    
	/**
	 * For relative add events, return the previous node this is being added after
	 */
	public Object getRelativePrevious()
	{
		return prevObj;
	}
	/**
	 * For relative add events, set the previous node this is being added after
	 */
	public void setRelativePrevious(Object previousObject)
	{
		this.prevObj = previousObject;
	}

}