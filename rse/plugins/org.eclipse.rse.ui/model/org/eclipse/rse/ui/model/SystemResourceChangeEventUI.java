/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * David Dykstal (IBM) - [226561] supply API markup in the javadoc
 *******************************************************************************/

package org.eclipse.rse.ui.model;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.swt.widgets.Item;

/**
 * Event object sent to SystemResourceChangeListeners when a
 * remote system object is created, changed, removed, etc.
 * This extends the base event object to include a reference to an 
 * orginating viewer.
 * @noextend This class is not intended to be subclassed by clients.
 * The class is complete as is.
 * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
 */
public class SystemResourceChangeEventUI extends SystemResourceChangeEvent {

	private static final long serialVersionUID = 1;
	private Viewer   originatingViewer;
	private Item item;

	/**
	 * Constructor for SystemResourceChangeEvent.
     * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
	 * @param source The object that was added,deleted,renamed,changed.
	 * @param type The type of event, one of ISystemChangeEvent constants.
	 * @param parent The parent of the object that was added or deleted.
	 */
	public SystemResourceChangeEventUI(Object source, int type, Object parent)
	{
		super(source, type, parent);
	}
	
	/**
	 * Constructor for SystemResourceChangeEvent when the source is multipe resources.
     * @see org.eclipse.rse.core.events.ISystemResourceChangeEvents
	 * @param source The array of objects that were added,deleted,renamed,changed.
	 * @param type The type of event, one of ISystemChangeEvent constants.
	 * @param parent The parent of the object that was added or deleted.
	 */
	public SystemResourceChangeEventUI(Object[] source, int type, Object parent)
	{
		super(source, type, parent);
	}	

    /**
     * Set the originating viewer.
     * For some events, this allows responding viewers to decide
     * if the event applies to them.
     * @param viewer the originating Viewer.
     */
    public void setOriginatingViewer(Viewer viewer)
    {
    	this.originatingViewer = viewer;
    }
    
    /**
     * Get the originating viewer.
     * For some events, this allows responding viewers to decide
     * if the event applies to them.
     * @return the originating Viewer.
     */
    public Viewer getOriginatingViewer()
    {
    	return originatingViewer;
    }    

	/**
	 * Set the viewer Item of the currently selected object.
	 * This is a clue when we want to expand and select only the specific 
	 * instance of this widget in this view.
	 * @param item the viewer item of the selected object.
	 */
	public void setViewerItem(Item item)
	{
		this.item = item;
	}

	/**
	 * Get the viewer Item of the currently selected object.
	 * This is a clue when we want to expand and select only the specific 
	 * instance of this widget in this view.
	 * @return the viewer item of the selected object.
	 */
	public Item getViewerItem()
	{
		return item;
	}
    
}
