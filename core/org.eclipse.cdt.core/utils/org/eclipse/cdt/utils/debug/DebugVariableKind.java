/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DebugVariableKind) {
			DebugVariableKind kind = (DebugVariableKind) obj;
			return kind.id == id;
		}
		return super.equals(obj);
	}
}
