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
