/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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
 * DebugCrossRefType
 *  
 */
public class DebugCrossRefType extends DebugDerivedType {

	String name;
	String xName;

	/**
	 *  
	 */
	public DebugCrossRefType(DebugType type, String name, String xName) {
		super(type);
		this.name = name;
		this.xName = xName;
	}

	public String getName() {
		return name;
	}

	public String getCrossRefName() {
		return xName;
	}

}
