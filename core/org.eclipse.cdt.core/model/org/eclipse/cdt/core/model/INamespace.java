package org.eclipse.cdt.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Represents a package declaration in a C translation unit.
 */
public interface INamespace extends ICElement, IParent, ISourceManipulation, ISourceReference {
	
	String getTypeName();
}
