/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug;

/**
 * DebugVisibility
 *
 */
public final class DebugVisibility {
	
	/* What is it ?? .  */
	public final static DebugVisibility UNKNOWN = new DebugVisibility(0);
	/* public field  */
	public final static DebugVisibility PUBLIC = new DebugVisibility(1);
	/* protected field.  */
	public final static DebugVisibility PROTECTED = new DebugVisibility(2);
	/* private field.  */
	public final static DebugVisibility PRIVATE = new DebugVisibility(3);
 
	private int id;
	/**
	 * 
	 */
	private DebugVisibility(int id) {
		this.id = id;
	}

	public boolean equals(Object obj) {
		if (obj instanceof DebugVisibility) {
			DebugVisibility kind = (DebugVisibility)obj;
			return kind.id == id;
		}
		return super.equals(obj);
	}
}
