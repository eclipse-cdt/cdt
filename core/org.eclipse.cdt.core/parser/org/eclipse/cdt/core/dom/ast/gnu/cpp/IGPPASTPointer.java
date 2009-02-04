/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.IASTPointer;

/**
 * g++ allows for restrict pointers.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
	
	/**
	 * @since 5.1
	 */
	public IGPPASTPointer copy();
}
