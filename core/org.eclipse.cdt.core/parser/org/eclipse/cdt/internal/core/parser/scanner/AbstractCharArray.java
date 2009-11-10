/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

/**
 * Abstract class for providing input to the lexer.
 */
public abstract class AbstractCharArray {

	/** 
	 * Checks whether the given offset is valid for this array. Subclasses may assume
	 * that offset is non-negative.
	 */
	public abstract boolean isValidOffset(int offset);

	/**
	 * Returns the limit for valid offsets or -1 if it is unknown. All offsets below
	 * the given limit are guaranteed to be valid.
	 */
	public abstract int getLimit();
	
	/**
	 * Returns the character at the given position, subclasses do not have to do range checks.
	 */
	public abstract char get(int offset);

	/**
	 * Copy a range of characters to the given destination. Subclasses do not have to do any
	 * range checks.
	 */
	public abstract void arraycopy(int offset, char[] destination, int destinationPos, int length);
}
