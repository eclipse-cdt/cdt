package org.eclipse.cdt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Represents the declaration of a variable.
 */
public interface IVariableDeclaration extends ICElement, ISourceManipulation, ISourceReference {

	public String getTypeName();
	public void setTypeName(String type);
	public int getAccessControl();
}
