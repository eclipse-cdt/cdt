/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.core.resources.IProject;

/**
 * Type information.
 */
public interface ITypeInfo {

	/**
	 * Gets the CElement type.
	 */
	public int getCElementType();

	/**
	 * Sets the CElement type.
	 */
	public void setCElementType(int type);

	/**
	 * Returns true if the element type is unknown.
	 */
	public boolean isUndefinedType();
	
	/**
	 * Returns true if this type can enclose other types,
	 * i.e. it is a namespace, class, or struct.
	 */
	public boolean isQualifierType();

	/**
	 * Gets the type name.
	 */
	public String getName();

	/**
	 * Gets the qualified type name.
	 */
	public IQualifiedTypeName getQualifiedTypeName();

	/**
	 * Gets the enclosing type.
	 */
	public ITypeInfo getEnclosingType();
	
	/**
	 * Gets the enclosing project.
	 */
	public IProject getEnclosingProject();
	
	/**
	 * Returns true if type is enclosed in the given scope.
	 */
	public boolean isEnclosed(ITypeSearchScope scope);
	
	/**
	 * Adds a source reference.
	 */
	public void addReference(ITypeReference location);

	/**
	 * Returns all known source references.
	 */
	public ITypeReference[] getReferences();

	/**
	 * Returns parsed source location with offset and length.
	 */
	public ITypeReference getResolvedReference();
}
