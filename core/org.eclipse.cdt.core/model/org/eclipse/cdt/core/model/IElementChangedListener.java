package org.eclipse.cdt.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An element changed listener receives notification of changes to C elements
 * maintained by the C model.
 */
public interface IElementChangedListener {
	
	/**
	 * Notifies that one or more attributes of one or more C elements have changed.
	 * The specific details of the change are described by the given event.
	 *
	 * @param event the change event
	 */
	public void elementChanged(ElementChangedEvent event);
}
