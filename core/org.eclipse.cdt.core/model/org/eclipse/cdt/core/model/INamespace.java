package org.eclipse.cdt.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Represents a package declaration in a C translation unit.
 */
public interface INamespace extends ICElement, ISourceManipulation, ISourceReference {
	/**
	 * Returns the name of the package the statement refers to.
	 * This is a handle-only method.
	 */
	String getElementName();
}
