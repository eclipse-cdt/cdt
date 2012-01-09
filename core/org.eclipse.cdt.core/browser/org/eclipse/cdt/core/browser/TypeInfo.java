/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class TypeInfo implements ITypeInfo {

	/**
	 * @since 5.1
	 */
	protected TypeInfo() {
	}
	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public void addReference(ITypeReference location) {
		throw new UnsupportedOperationException();
	}

		/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeReference[] getReferences() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeReference getResolvedReference() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public boolean isReferenced() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean isReferenced(ITypeSearchScope scope) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean isUndefinedType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean canSubstituteFor(ITypeInfo info) {
		throw new UnsupportedOperationException();
	}

	protected boolean isExactMatch(ITypeInfo info) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean exists() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public int getCElementType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public void setCElementType(int type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public IQualifiedTypeName getQualifiedTypeName() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public String getName() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean isEnclosedType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeInfo getEnclosingType(int kinds[]) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeInfo getEnclosingType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeInfo getEnclosingNamespace(boolean includeGlobalNamespace) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean isEnclosingType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean encloses(ITypeInfo info) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean isEnclosed(ITypeInfo info) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean hasEnclosedTypes() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeInfo[] getEnclosedTypes() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeInfo[] getEnclosedTypes(int kinds[]) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ICProject getEnclosingProject() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean isEnclosed(ITypeSearchScope scope) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static boolean isValidType(int type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public void addDerivedReference(ITypeReference location) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeReference[] getDerivedReferences() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean hasSubTypes() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeInfo[] getSubTypes() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean hasSuperTypes() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ITypeInfo[] getSuperTypes() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public ASTAccessVisibility getSuperTypeAccess(ITypeInfo superType) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	@Deprecated
	public boolean isClass() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public int compareTo(Object obj) {
		throw new UnsupportedOperationException();
	}
}
