package org.eclipse.cdt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
	public static final int POST_CHANGE = 1;
	public static final int PRE_AUTO_BUILD = 2;
	public static final int 	POST_RECONCILE = 4;	
	/**
	 * Creates an new element changed event (based on a <code>ICElementDelta</code>).
	 *
	 * @param delta the C element delta.
	 */
	public ElementChangedEvent(ICElementDelta delta) {
		super(delta);
	}
	/**
	 * Returns the delta describing the change.
	 *
	 */
	public ICElementDelta getDelta() {
		return (ICElementDelta) source;
	}
}
