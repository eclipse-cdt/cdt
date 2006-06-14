/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
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
import java.util.EventObject;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Item;


/**
 * Event object sent to SystemResourceChangeListeners when a
 * remote system object is created, changed, removed, etc.
 * @see org.eclipse.rse.model.ISystemResourceChangeEvents
 */
public class SystemResourceChangeEvent
	   extends EventObject
	   implements ISystemResourceChangeEvent
{
	private static final long serialVersionUID = 1;
	private Object parent,grandparent, prevObj;
	private Object[] multiSource;
	private Viewer   originatingViewer;
	private int type;
	private int position = -1;
	private Item item;
	
	/**
	 * Constructor for SystemResourceChangeEvent.
     * @see org.eclipse.rse.model.ISystemResourceChangeEvents
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
     * @see org.eclipse.rse.model.ISystemResourceChangeEvents
	 * @param source The array of objects that were added,deleted,renamed,changed.
	 * @param type The type of event, one of ISystemChangeEvent constants.
	 * @param parent The parent of the object that was added or deleted.
	 */
	public SystemResourceChangeEvent(Object[] source, int type, Object parent)
	{
		super(((source!=null) && (source.length>0)) ? source[0] : "nada"); // defect 42112
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
	 * Set the viewer Item of the currently selected object. This is a clue when we want to 
	 *  expand and select only the specific instance of this widget in this view.
	 */
	public void setViewerItem(Item item)
	{
		this.item = item;
	}


	/**
	 * Return the type of the event (add, change, remove, rename, change, property change).
     * @see org.eclipse.rse.model.ISystemResourceChangeEvents
	 * @see org.eclipse.rse.model.ISystemResourceChangeEvent
	 * @see org.eclipse.rse.model.ISystemResourceChangeEvent#getType()
	 */
	public int getType()
	{
		return type;
	}
    /**
     * Set the type
     * @see org.eclipse.rse.model.ISystemResourceChangeEvents
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

    /**
     * Set the originating viewer. For some events, this allows responding viewers to decide if the event applies to them
     */
    public void setOriginatingViewer(Viewer viewer)
    {
    	this.originatingViewer = viewer;
    }    
    /**
     * Get the originating viewer. For some events, this allows responding viewers to decide if the event applies to them
     */
    public Viewer getOriginatingViewer()
    {
    	return originatingViewer;
    }    
	/**
	 * Get the viewer Item of the currently selected object. This is a clue when we want to 
	 *  expand and select only the specific instance of this widget in this view.
	 */
	public Item getViewerItem()
	{
		return item;
	}
    
}