package org.eclipse.cdt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Represents the declaration of a variable.
 */
public interface IVariableDeclaration extends IDeclaration {
	/**
	 * 
	 * @return
	 * @throws CModelException
	 */
	public String getTypeName() throws CModelException;
	
	/**
	 * 
	 * @param type
	 * @throws CModelException
	 */
	public void setTypeName(String type) throws CModelException;
}
