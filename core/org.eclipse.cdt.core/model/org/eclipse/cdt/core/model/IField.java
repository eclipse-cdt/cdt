package org.eclipse.cdt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Represents a field(variable) declared in an IStructure(struct, class, union).
 */
public interface IField extends IMember, IVariableDeclaration {

	/**
	 * Returns whether this storage specifier is mutable for the member.
	 *
	 * @exception CModelException if this element does not exist or if an
	 *      exception occurs while accessing its corresponding resource.
	 */
	public boolean isMutable();
}
