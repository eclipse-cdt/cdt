package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
