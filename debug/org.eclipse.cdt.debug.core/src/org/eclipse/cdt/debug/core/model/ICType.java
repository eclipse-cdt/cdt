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
package org.eclipse.cdt.debug.core.model;

/**
 * Represents a type of a varibale. 
 * Used by the UI responsible components for variable rendering.
 */
public interface ICType {

	/**
	 * Returns the name of this type.
	 * 
	 * @return the name of this type
	 */
	String getName();

	/**
	 * Returns whether this is an array type.
	 * 
	 * @return whether this is an array type
	 */
	boolean isArray();

	/**
	 * Returns the array dimensions for array types, 
	 * otherwise returns an empty array.
	 * 
	 * @return the array dimensions
	 */
	int[] getArrayDimensions();

	/**
	 * Returns whether this is a structure or a class type.
	 * 
	 * @return whether this is a structure or a class type
	 */
	boolean isStructure();

	/**
	 * Returns whether this is a character type.
	 * 
	 * @return whether this is a character type
	 */
	boolean isCharacter();

	/**
	 * Returns whether this is a floating point type.
	 * 
	 * @return whether this is a floating point type
	 */
	boolean isFloatingPointType();

	/**
	 * Returns whether this is a pointer type.
	 * 
	 * @return whether this is a pointer type
	 */
	boolean isPointer();

	/**
	 * Returns whether this is a reference type.
	 * 
	 * @return whether this is a reference type
	 */
	boolean isReference();

	/**
	 * Returns whether this is an unsigned type.
	 * 
	 * @return whether this is an unsigned type
	 */
	boolean isUnsigned();
}