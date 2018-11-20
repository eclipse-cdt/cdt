/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
 * DebugDerivedType
 *
 */
public abstract class DebugDerivedType extends DebugType {

	DebugType component;

	/**
	 *
	 */
	public DebugDerivedType(DebugType type) {
		component = type;
	}

	public DebugType getComponentType() {
		return component;
	}

	public void setComponentType(DebugType type) {
		component = type;
	}

}
