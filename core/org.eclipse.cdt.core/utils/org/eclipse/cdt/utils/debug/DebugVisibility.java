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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DebugVisibility) {
			DebugVisibility kind = (DebugVisibility) obj;
			return kind.id == id;
		}
		return super.equals(obj);
	}
}
