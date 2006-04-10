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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Item;

/**
 * Interface of event sent when a remote system resource changes.
 * These events are mainly for internal use. BPs/ISVs should instead 
 *  fire and monitor for {@link org.eclipse.rse.model.ISystemModelChangeEvent}.
 * @see org.eclipse.rse.model.ISystemResourceChangeEvents
 */
public interface ISystemResourceChangeEvent extends ISystemResourceChangeEvents
{	
    /**
     * Returns an object identifying the source of this event.
     *
     * @return an object identifying the source of this event
     * @see java.util.EventObject
     */
    public Object getSource();
    
	/**
	 * For multi-target events, return the array of source targets.
	 */
	public Object[] getMultiSource();
	
    /**
     * Returns the parent of the object source. Only quaranteed to
     *  be set for additions and deletions.
     *
     * @return an object identifying the parent of the source of this event
     */
    public Object getParent();
	/**
	 * Set the parent
	 */
	public void setParent(Object parent);
    
    /**
     * Returns the grandparent of the object source. Only quaranteed to
     *  be set for special case events, which have a special contract with
     *  the SystemView
     *
     * @return an object identifying the grandparent of the source of this event
     */
    public Object getGrandParent();    
	/**
	 * Return the position value. Used in ADD events.
	 * @return position to add the new item to. A negative number indicates an append operation
	 */
	public int getPosition();
	/**
	 * Set the position value. Used in ADD events.
	 * @param position zero-based position to insert the new item. A negative number indicates an append operation
	 */
	public void setPosition(int position);
	
	/**
	 * For relative add events, return the previous node this is being added after
	 */
	public Object getRelativePrevious();
	/**
	 * For relative add events, set the previous node this is being added after
	 */
	public void setRelativePrevious(Object previousObject);

    /**
     * Returns the type of the event.
     * @see org.eclipse.rse.model.ISystemResourceChangeEvents
     * @return a type that is one of the constants in this interface
     */
    public int getType();
    /**
     * Set the type
     * @see org.eclipse.rse.model.ISystemResourceChangeEvents
     */
    public void setType(int type);

    /**
     * Set the originating viewer. For some events, this allows responding viewers to decide if the event applies to them
     */
    public void setOriginatingViewer(Viewer viewer);
    /**
     * Get the originating viewer. For some events, this allows responding viewers to decide if the event applies to them
     */
    public Viewer getOriginatingViewer();

	/**
	 * Set the viewer Item of the currently selected object. This is a clue when we want to 
	 *  expand and select only the specific instance of this widget in this view.
	 */
	public void setViewerItem(Item item);
	/**
	 * Get the viewer Item of the currently selected object. This is a clue when we want to 
	 *  expand and select only the specific instance of this widget in this view.
	 */
	public Item getViewerItem();
}