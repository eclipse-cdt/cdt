/*******************************************************************************
 * Copyright (c) 2012 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.util;


// ---------------------------------------------------------------------------
// Event
// ---------------------------------------------------------------------------

/**
 * Base class for events
 */
public class Event
{
	
	// --- event types ---
	
	/** Event type constant */
	public static final int UNDEFINED = 0;
	

	// --- members ---
	
	/** Source of the event */
	protected Object m_source = null;
	
	/** Type of event */
	protected int m_type = UNDEFINED;
	

	// --- constructors/destructors ---
	
	/** Constructor */
	public Event(Object source) {
		this(source, UNDEFINED);
	}
	
	/** Constructor */
	public Event(Object source, int type) {
		m_source = source;
		m_type = type;
	}
	
	/** Dispose method */
	public void dispose() {
		m_source = null;
	}

	
	// --- Object methods ---
	
	/** Returns string representation of event */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(getClass().getSimpleName());
		result.append("[");
		if (m_type != UNDEFINED) {
			result.append(typeToString(m_type));
		}
		result.append("]");
		return result.toString();
	}
	
	/** Converts event type to string */
	public String typeToString(int type) {
		String result = "";
		switch (type) {
			case UNDEFINED:
				result = "UNDEFINED"; break;
			default:
				result = "OTHER(" + type +")";
				break;
		}
		return result;
	}


	// --- accessors ---
	
	/** Gets source of the event */
	public Object getSource() {
		return m_source;
	}
	
	/**
	 * Gets type of event
	 */
	public int getType() {
		return m_type;
	}
	
	/**
	 * Returns true if event has specified type.
	 */
	public boolean isType(int type) {
		return (m_type == type);
	}
}
