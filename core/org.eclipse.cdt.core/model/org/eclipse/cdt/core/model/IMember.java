/**********************************************************************
Copyright (c) 2002, 2004 IBM Rational Software and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
    IBM Rational Software - Initial API and implementation
**********************************************************************/
package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * Common protocol for C elements that can be members of types.
 * This set consists of <code>IType</code>, <code>IMethod</code>, 
 * <code>IField</code>.
 */
public interface IMember extends IDeclaration {

	/**
	 * Returns the member's visibility
	 * V_PRIVATE = 0 V_PROTECTED = 1 V_PUBLIC = 2
	 * @return int
	 */
	public ASTAccessVisibility getVisibility() throws CModelException;	
	
}
