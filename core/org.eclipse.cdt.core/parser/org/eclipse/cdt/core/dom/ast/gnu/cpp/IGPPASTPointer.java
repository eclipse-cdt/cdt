/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.IASTPointer;

/**
 * g++ allows for restrict pointers.
 * 
 * @author jcamelon
 */
public interface IGPPASTPointer extends IASTPointer {

	/**
	 * Is this pointer a restrict pointer?
	 * 
	 * @return boolean
	 */
	public boolean isRestrict();

	/**
	 * Set restrict-keyword-encountered to true or false.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setRestrict(boolean value);
}
