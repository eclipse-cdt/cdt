package org.eclipse.cdt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Represents a global variable.
 */
public interface IVariable extends ICElement , ISourceManipulation, ISourceReference {
	public String getTypeName();
	public void setTypeName(String type);
	public String getInitializer();
	public int getAccessControl() throws CModelException;
}
