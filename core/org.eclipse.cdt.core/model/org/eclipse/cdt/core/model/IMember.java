package org.eclipse.cdt.core.model;

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

	static final int V_PUBLIC = 0;
	static final int V_PROTECTED = 1;
	static final int V_PRIVATE = 2;

	/**
	 * Returns the member's visibility
	 * V_PRIVATE = 0 V_PROTECTED = 1 V_PUBLIC = 2
	 * @return int
	 */
	public int getVisibility();	
	
}
