package org.eclipse.cdt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Represents a field declared in a type.
 */
public interface ITypeDef extends ICElement, ISourceManipulation, ISourceReference {
	/**
	 * Return the type beeing alias.
	 */
	String getType() throws CModelException;
}
