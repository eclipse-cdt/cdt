/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.dom.ast.c;

import org.eclipse.cdt.core.dom.ast.IASTPointer;

/**
 * C-specific pointer. (includes restrict modifier).
 * 
 * @author jcamelon
 */
public interface ICASTPointer extends IASTPointer {

	/**
	 * Is this a restrict pointer?
	 * 
	 * @return isRestrict boolean
	 */
	boolean isRestrict();

	/**
	 * Set this pointer to be restrict pointer.
	 * 
	 * @param value
	 */
	void setRestrict(boolean value);

}
