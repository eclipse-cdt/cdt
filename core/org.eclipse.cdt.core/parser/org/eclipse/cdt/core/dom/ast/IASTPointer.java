/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This represents the good ol' * pointer operator.
 * 
 * @author Doug Schaefer
 */
public interface IASTPointer extends IASTPointerOperator {

	// Qualifiers applied to the pointer type
	public boolean isConst();
	public boolean isVolatile();
	
	public void setConst( boolean value );
	public void setVolatile( boolean value );

}
