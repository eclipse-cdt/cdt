/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMTypeInfo implements ITypeInfo {

	public void addDerivedReference(ITypeReference location) {
		// TODO Auto-generated method stub

	}

	public void addReference(ITypeReference location) {
		// TODO Auto-generated method stub

	}

	public boolean canSubstituteFor(ITypeInfo info) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean encloses(ITypeInfo info) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getCElementType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ITypeReference[] getDerivedReferences() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeInfo[] getEnclosedTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeInfo[] getEnclosedTypes(int[] kinds) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeInfo getEnclosingNamespace(boolean includeGlobalNamespace) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICProject getEnclosingProject() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeInfo getEnclosingType() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeInfo getEnclosingType(int[] kinds) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public IQualifiedTypeName getQualifiedTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeReference[] getReferences() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeReference getResolvedReference() {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeInfo[] getSubTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTAccessVisibility getSuperTypeAccess(ITypeInfo subType) {
		// TODO Auto-generated method stub
		return null;
	}

	public ITypeInfo[] getSuperTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasEnclosedTypes() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasSubTypes() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasSuperTypes() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isClass() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnclosed(ITypeInfo info) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnclosed(ITypeSearchScope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnclosedType() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnclosingType() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReferenced(ITypeSearchScope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUndefinedType() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCElementType(int type) {
		// TODO Auto-generated method stub

	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
