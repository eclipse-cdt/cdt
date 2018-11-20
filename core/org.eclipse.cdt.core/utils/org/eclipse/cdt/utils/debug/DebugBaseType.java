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
 * DebugType
 *
 */
public class DebugBaseType extends DebugType {

	String typeName;
	int typeSize;
	boolean typeUnSigned;

	public DebugBaseType(String name, int size, boolean unSigned) {
		typeName = name;
		typeSize = size;
		typeUnSigned = unSigned;
	}

	public String getTypeName() {
		return typeName;
	}

	public int sizeof() {
		return typeSize;
	}

	public boolean isUnSigned() {
		return typeUnSigned;
	}
}
