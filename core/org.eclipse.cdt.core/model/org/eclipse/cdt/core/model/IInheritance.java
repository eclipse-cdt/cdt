package org.eclipse.cdt.core.model;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 * Place holder of the inherited class from struct or class(IStructure).
 */
public interface IInheritance {
	/**
	 * Return the inherited structures.
	 */
	public IStructure [] getBaseTypes() throws CModelException;

	/**
	 * Return the access control for each inherited structure.
	 */
	public int getAccessControl(int pos) throws CModelException;
}
