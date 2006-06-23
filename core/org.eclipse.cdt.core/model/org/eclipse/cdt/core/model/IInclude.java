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
package org.eclipse.cdt.core.model;

/**
 * Represents an include declaration in a C translation unit.
 */
public interface IInclude extends ICElement, ISourceReference, ISourceManipulation {
	/**
	 * Returns the name that of the included file. 
	 * For example, for the statement <code>"#include <stdio.h></code>,
	 * this returns <code>"stdio.h"</code>.
	 */
	String getIncludeName();

	/**
	 * Returns whether the included was search on "standard places" like /usr/include first .
	 * An include is standard if it starts with <code>"\<"</code>.
	 * For example, <code>"#include \<stdio.h\>"</code> returns true and
	 * <code>"#include "foobar.h"</code> returns false.
	 */
	boolean isStandard();
	
	public String getFullFileName(); 
	
	public boolean isLocal(); 
}
