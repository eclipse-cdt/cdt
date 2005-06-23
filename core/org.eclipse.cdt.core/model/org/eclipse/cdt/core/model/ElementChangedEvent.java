package org.eclipse.cdt.core.model;

/*******************************************************************************
 * Copyright (c) 2001 - 2002 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
import java.util.EventObject;

/**
 * An element changed event describes a change to the structure or contents
 * of a tree of C elements. The changes to the elements are described by
 * the associated delta object carried by this event.
 *
 * @see IElementChangedListener
 * @see ICElementDelta
 */
public class ElementChangedEvent extends EventObject {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257572793326252855L;
	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of creations, deletions, and modifications
	 * to one or more C element(s) expressed as a hierarchical
	 * C element delta as returned by <code>getDelta()</code>.
	 *
	 * Note: this notification occurs during the corresponding POST_CHANGE
	 * resource change notification, and contains a full delta accounting for
	 * any CModel operation  and/or resource change.
	 *
	 * @see ICElementDelta
	 * @see org.eclipse.core.resources.IResourceChangeEvent
	 * @see #getDelta()
	 * @since 2.0
	 */
	public static final int POST_CHANGE = 1;
	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of creations, deletions, and modifications
	 * to one or more C element(s) expressed as a hierarchical
	 * C element delta as returned by <code>getDelta</code>.
	 *
	 * Note: this notification occurs during the corresponding PRE_AUTO_BUILD
	 * resource change notification. The delta, which is notified here, only contains
	 * information relative to the previous CModel operations (in other words, 
	 * it ignores the possible resources which have changed outside C operations). 
	 * In particular, it is possible that the CModel be inconsistent with respect to
	 * resources, which got modified outside CModel operations (it will only be
	 * fully consistent once the POST_CHANGE notification has occurred).
	 * 
	 * @see ICElementDelta
	 * @see org.eclipse.core.resources.IResourceChangeEvent
	 * @see #getDelta()
	 * @since 2.0
	 * @deprecated - no longer used, such deltas are now notified during POST_CHANGE
	 */
	public static final int PRE_AUTO_BUILD = 2;
	/**
	 * Event type constant (bit mask) indicating an after-the-fact 
	 * report of creations, deletions, and modifications
	 * to one or more C element(s) expressed as a hierarchical
	 * C element delta as returned by <code>getDelta</code>.
	 *
	 * Note: this notification occurs as a result of a working copy reconcile
	 * operation.
	 *
	 * @see ICElementDelta
	 * @see org.eclipse.core.resources.IResourceChangeEvent
	 * @see #getDelta()
	 * @since 2.0
	 */
	public static final int 	POST_RECONCILE = 4;	
	/*
	 * Event type indicating the nature of this event. 
	 * It can be a combination either:
	 *  - POST_CHANGE
	 *  - PRE_AUTO_BUILD
	 *  - POST_RECONCILE
	 */
	private int type; 
	/**
	 * Creates an new element changed event (based on a <code>ICElementDelta</code>).
	 *
	 * @param delta the C element delta.
	 */
	public ElementChangedEvent(ICElementDelta delta, int type) {
		super(delta);
		this.type = type;
	}
	/**
	 * Returns the delta describing the change.
	 *
	 */
	public ICElementDelta getDelta() {
		return (ICElementDelta) source;
	}
	/**
	 * Returns the type of event being reported.
	 *
	 * @return one of the event type constants
	 * @see #POST_CHANGE
	 * @see #PRE_AUTO_BUILD
	 * @see #POST_RECONCILE
	 * @since 2.0
	 */
	public int getType() {
		return this.type;
	}
}
