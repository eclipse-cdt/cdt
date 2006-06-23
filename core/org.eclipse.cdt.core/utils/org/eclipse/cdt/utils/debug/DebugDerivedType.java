/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
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
