package org.eclipse.cdt.core.model;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Place holder of the inherited class from struct or class(IStructure).
 */
public interface IInheritance {
	/**
	 * Return the inherited structures names.
	 */
	public String[] getSuperClassesNames();
	/**
	 * Returns the super class access : ASTAccessVisibility 
	 */
	public ASTAccessVisibility getSuperClassAccess(String name);
}
