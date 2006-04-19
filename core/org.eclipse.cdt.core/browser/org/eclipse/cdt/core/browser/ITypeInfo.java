/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * Type information.
 */
public interface ITypeInfo extends Comparable {

	public static final int KNOWN_TYPES[] = {
		ICElement.C_NAMESPACE,
		ICElement.C_CLASS,
		ICElement.C_STRUCT,
		ICElement.C_UNION,
		ICElement.C_ENUMERATION,
		ICElement.C_TYPEDEF
	};
		
	/**
	 * Gets the CElement type.
	 * @return ICElement.C_NAMESPACE, C_CLASS, C_STRUCT, C_UNION, C_ENUMERATION, or C_TYPEDEF,
	 * or zero if unknown type.
	 */
	public int getCElementType();

	/**
	 * Sets the CElement type.
	 */
	public void setCElementType(int type);

	/**
	 * Gets the type name.
	 */
	public String getName();

	/**
	 * Gets the qualified type name.
	 */
	public IQualifiedTypeName getQualifiedTypeName();

	/**
	 * Returns true if the type exists.
	 */
	public boolean exists();

	/**
	 * Returns true if the element type is unknown.
	 */
	public boolean isUndefinedType();
	
	/**
	 * Returns true if this type is enclosed by other types,
	 * i.e. declared an inside another namespace or class.
	 */
	public boolean isEnclosedType();

	/** Gets the enclosing type, i.e. the outer class or namespace which contains this type.
	 * @return the enclosing type, or <code>null</code> if not found.
	 */
	public ITypeInfo getEnclosingType();

	/** Gets the enclosing namespace for this type.
	 * @return the enclosing namespace, or <code>null</code> if none exists.
	 */
	public ITypeInfo getEnclosingNamespace(boolean includeGlobalNamespace);

	/** Gets the first enclosing type which matches one of the given kinds.
	 * @param kinds Array containing CElement types: C_NAMESPACE, C_CLASS, C_STRUCT
	 * @return the enclosing type, or <code>null</code> if not found.
	 */
	public ITypeInfo getEnclosingType(int[] kinds);

	/** Gets the root namespace, i.e. the outermost namespace
	 * which contains this type.
	 * @param includeGlobalNamespace <code>true</code> if the global (default) namespace should be returned
	 * @return the namespace, or <code>null</code> if not found.
	 */
	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace);
	
	/**
	 * Returns true if this type is capable of enclosing other types,
	 * i.e. it is a namespace, class, or struct.
	 */
	public boolean isEnclosingType();

	/**
	 * Returns true if this type encloses other types, i.e. contains
	 * inner classes or namespaces.
	 */
	public boolean hasEnclosedTypes();

	/**
	 * Returns true if this type encloses the given type.
	 */
	public boolean encloses(ITypeInfo info);
	
	/**
	 * Returns true if this type is enclosed by the given type.
	 */
	public boolean isEnclosed(ITypeInfo info);
	
	/** Gets the enclosed types, i.e. inner classes or classes inside this namespace.
	 * @return array of inner types, or empty array if not found.
	 */
	public ITypeInfo[] getEnclosedTypes();
	
	/** Gets the enclosed types, i.e. inner classes or classes inside this namespace.
	 * @param kinds Array containing CElement types: C_NAMESPACE, C_CLASS, C_STRUCT,
	 *              C_UNION, C_ENUMERATION, C_TYPEDEF
	 * @return array of inner types, or empty array if not found.
	 */
	public ITypeInfo[] getEnclosedTypes(int kinds[]);

	/**
	 * Gets the enclosing project.
	 */
	public ICProject getEnclosingProject();
	
	/**
	 * Returns true if type is enclosed in the given scope.
	 */
	public boolean isEnclosed(ITypeSearchScope scope);
	
	/**
	 * Adds a source reference.
	 */
	public void addReference(ITypeReference location);

	/** Gets the originating locations where this type was declared.
	 * @return all known source references, or an empty
	 * array if none found.
	 */
	public ITypeReference[] getReferences();

	/** Gets the real location where type was declared.
	 * @return the parsed source reference (with offset and length),
	 * or <code>null</code> if not found.
	 */
	public ITypeReference getResolvedReference();
	
	/**
	 * Returns true if the type can be substituted.
	 */
	public boolean canSubstituteFor(ITypeInfo info);

	/**
	 * Returns true if other types extend this type.
	 */
	public boolean hasSubTypes();
	
	/** Gets all types which extend this type.
	 * @return array of types, or <code>null</code> if none found.
	 */
	public ITypeInfo[] getSubTypes();
	
	/**
	 * Returns true if this type has base classes.
	 */
	public boolean hasSuperTypes();
	
	/** Gets the base classes.
	 * @return array of types, or <code>null</code> if none found.
	 */
	public ITypeInfo[] getSuperTypes();
	
	/**
	 * Gets the base class access visibility (PRIVATE, PROTECTED, PUBLIC)
	 */
	public ASTAccessVisibility getSuperTypeAccess(ITypeInfo subType);

	/**
	 * Adds a derived class reference, i.e. this type is used as
	 * a base class at the given location.
	 */
	public void addDerivedReference(ITypeReference location);

	/** Gets the originating locations where this type was
	 * used as a base class.
	 * @return all known source references, or an empty
	 * array if none found.
	 */
	public ITypeReference[] getDerivedReferences();
	
	/**
	 * Returns true if the type is a class or struct.
	 */
	public boolean isClass();

	/**
	 * Returns true if type is referenced in the given scope.
	 */
	public boolean isReferenced(ITypeSearchScope scope);
}
