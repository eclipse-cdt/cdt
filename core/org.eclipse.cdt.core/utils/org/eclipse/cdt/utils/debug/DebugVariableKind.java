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
 * DebugVariableKind
 *
 */
public final class DebugVariableKind {
	
	/* What is it ?? .  */
	public final static DebugVariableKind UNKNOWN = new DebugVariableKind(0);
	/* global variable  */
	public final static DebugVariableKind GLOBAL = new DebugVariableKind(1);
	/* static variable.  */
	public final static DebugVariableKind STATIC = new DebugVariableKind(2);
	/* local static variable.  */
	public final static DebugVariableKind LOCAL_STATIC = new DebugVariableKind(3);
	/* local variable.  */
	public final static DebugVariableKind LOCAL = new DebugVariableKind(4);
	/* variable is in register.  */
	public final static DebugVariableKind REGISTER = new DebugVariableKind(5);
 
	private int id;
	/**
	 * 
	 */
	private DebugVariableKind(int id) {
		this.id = id;
	}

	public boolean equals(Object obj) {
		if (obj instanceof DebugVariableKind) {
			DebugVariableKind kind = (DebugVariableKind)obj;
			return kind.id == id;
		}
		return super.equals(obj);
	}
}
