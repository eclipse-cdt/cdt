/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITypeInfo {

	public static final int KNOWN_TYPES[] = { ICElement.C_NAMESPACE, ICElement.C_CLASS, ICElement.C_STRUCT,
			ICElement.C_UNION, ICElement.C_ENUMERATION, ICElement.C_TYPEDEF };

	/**
	 * Returns the CElement type.
	 * @return ICElement.C_NAMESPACE, C_CLASS, C_STRUCT, C_UNION, C_ENUMERATION, or C_TYPEDEF,
	 * or zero if unknown type.
	 */
	public int getCElementType();

	/**
	 * Returns the type name.
	 */
	public String getName();

	/**
	 * Returns the qualified type name.
	 */
	public IQualifiedTypeName getQualifiedTypeName();

	/**
	 * Returns the originating locations where this type was declared.
	 * @return all known source references, or an empty
	 * array if none found.
	 */
	public ITypeReference[] getReferences();

	/**
	 * Returns the real location where type was declared.
	 *
	 * @return the parsed source reference (with offset and length),
	 * or <code>null</code> if not found.
	 */
	public ITypeReference getResolvedReference();

	/**
	 * Returns the enclosing project.
	 */
	public ICProject getEnclosingProject();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public void setCElementType(int type);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean exists();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean isUndefinedType();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean isEnclosedType();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ITypeInfo getEnclosingType();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ITypeInfo getEnclosingNamespace(boolean includeGlobalNamespace);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ITypeInfo getEnclosingType(int[] kinds);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean isEnclosingType();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean hasEnclosedTypes();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean encloses(ITypeInfo info);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean isEnclosed(ITypeInfo info);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ITypeInfo[] getEnclosedTypes();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ITypeInfo[] getEnclosedTypes(int kinds[]);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean isEnclosed(ITypeSearchScope scope);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public void addReference(ITypeReference location);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean canSubstituteFor(ITypeInfo info);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean hasSubTypes();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean hasSuperTypes();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ITypeInfo[] getSubTypes();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ITypeInfo[] getSuperTypes();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ASTAccessVisibility getSuperTypeAccess(ITypeInfo subType);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public void addDerivedReference(ITypeReference location);

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public ITypeReference[] getDerivedReferences();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean isClass();

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 * @deprecated
	 */
	@Deprecated
	public boolean isReferenced(ITypeSearchScope scope);
}
