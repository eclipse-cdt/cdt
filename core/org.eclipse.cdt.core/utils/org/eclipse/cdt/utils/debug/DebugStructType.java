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
 * DebugStructType
 *
 */
public class DebugStructType extends DebugType {

	String name;
	int size;
	boolean isUnion;
	final static DebugField[] EMPTY_FIELDS = new DebugField[0];
	DebugField[] fields;

	/**
	 *
	 */
	public DebugStructType(String name, int size, boolean union) {
		this.name = name;
		this.size = size;
		this.isUnion = union;
		fields = EMPTY_FIELDS;
	}

	public int getSize() {
		return size;
	}

	public String getName() {
		return name;
	}

	public boolean isUnion() {
		return isUnion;
	}

	public DebugField[] getDebugFields() {
		return fields;
	}

	public void addField(DebugField field) {
		DebugField[] fs = new DebugField[fields.length + 1];
		System.arraycopy(fields, 0, fs, 0, fields.length);
		fs[fields.length] = field;
		fields = fs;
	}

}
