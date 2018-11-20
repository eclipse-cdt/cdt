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
 * DebugEnumType
 *
 */
public class DebugEnumType extends DebugType {

	DebugEnumField[] fields;
	String name;

	/**
	 *
	 */
	public DebugEnumType(String name, DebugEnumField[] fields) {
		this.name = name;
		this.fields = fields;
	}

	public DebugEnumField[] getDebugEnumFields() {
		return fields;
	}

	public String getName() {
		return name;
	}

}
