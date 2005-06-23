/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.utils.debug;

/**
 * DebugParameterKind
 *
 */
public final class DebugParameterKind {
	
	/* What is it ?? .  */
	public final static DebugParameterKind UNKNOWN = new DebugParameterKind(0);
	/* parameter on the stack*/
	public final static DebugParameterKind STACK = new DebugParameterKind(1);
	/* parameter in register.  */
	public final static DebugParameterKind REGISTER = new DebugParameterKind(2);
	/* parameter by reference.  */
	public final static DebugParameterKind REFERENCE = new DebugParameterKind(3);
	/* register reference parameter.  */
	public final static DebugParameterKind REGISTER_REFERENCE = new DebugParameterKind(4);
 
	private int id;
	/**
	 * 
	 */
	private DebugParameterKind(int id) {
		this.id = id;
	}

	public boolean equals(Object obj) {
		if (obj instanceof DebugParameterKind) {
			DebugParameterKind kind = (DebugParameterKind)obj;
			return kind.id == id;
		}
		return super.equals(obj);
	}
}
