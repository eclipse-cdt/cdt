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

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.internal.core.browser.cache.ITypeCache;
import org.eclipse.cdt.internal.core.browser.util.ArrayUtil;
import org.eclipse.core.resources.IProject;

public class TypeInfo implements ITypeInfo
{
	protected ITypeCache fTypeCache;
	protected int fElementType;
	protected IQualifiedTypeName fQualifiedName;
	protected ITypeReference[] fSourceRefs = null;
	protected int fSourceRefsCount = 0;

	protected final static int INITIAL_REFS_SIZE = 1;
	protected final static int REFS_GROW_BY = 2;
	protected final static ITypeInfo[] EMPTY_TYPES = new ITypeInfo[0];

	public TypeInfo(int elementType, IQualifiedTypeName typeName) {
		fElementType = elementType;
		fQualifiedName = typeName;
	}

	public void addReference(ITypeReference location) {
		if (fSourceRefs == null) {
			fSourceRefs = new ITypeReference[INITIAL_REFS_SIZE];
			fSourceRefsCount = 0;
		} else if (fSourceRefsCount == fSourceRefs.length) {
			ITypeReference[] refs = new ITypeReference[fSourceRefs.length + REFS_GROW_BY];
			System.arraycopy(fSourceRefs, 0, refs, 0, fSourceRefsCount);
			fSourceRefs = refs;
		}
		fSourceRefs[fSourceRefsCount] = location;
		++fSourceRefsCount;
	}
	
	public ITypeReference[] getReferences() {
		if (fSourceRefs != null) {
			ITypeReference[] refs = new ITypeReference[fSourceRefsCount];
			System.arraycopy(fSourceRefs, 0, refs, 0, fSourceRefsCount);
			return refs;
		}
		return null;
	}
	
	public ICElement getCElement() {
		ITypeReference ref = getResolvedReference();
		if (ref != null) {
			ICElement[] elems = ref.getCElements();
			if (elems.length > 1) {
				for (int i = 0; i < elems.length; ++i) {
					ICElement elem = elems[i];
					if (elem.getElementType() == fElementType && elem.getElementName().equals(getName())) {
						//TODO should check fully qualified name
						return elem;
					}
					if (elem instanceof ITypeDef && ((ITypeDef)elem).getTypeName().equals(getName())) {
						//TODO should check fully qualified name
						return elem;
					}
				}
			} else if (elems.length == 1) {
				return elems[0];
			}
		}
		return null;
	}

	public ITypeReference getResolvedReference() {
		for (int i = 0; i < fSourceRefsCount; ++i) {
			ITypeReference location = fSourceRefs[i];
			if (location.getLength() != 0) {
				return location;
			}
		}
		return null;
	}

	public boolean isReferenced() {
		return (fSourceRefs != null);
	}
	
	public boolean isUndefinedType() {
		return fElementType == 0;
	}
	
	public boolean canSubstituteFor(ITypeInfo info) {
		return isExactMatch(info);
	}
	
	protected boolean isExactMatch(ITypeInfo info) {
		if (hashCode() != info.hashCode())
			return false;
		if (fElementType == info.getCElementType() 
			&& fQualifiedName.equals(info.getQualifiedTypeName())) {
			IProject project1 = getEnclosingProject();
			IProject project2 = info.getEnclosingProject();
			if (project1 == null && project2 == null)
				return true;
			if (project1 == null || project2 == null)
				return false;
			return project1.equals(project2);
		}
		return false;
	}
	
	public boolean exists() {
		return fTypeCache != null;
	}
	
	public int getCElementType() {
		return fElementType;
	}

	public void setCElementType(int type) {
		fElementType = type;
	}

	public IQualifiedTypeName getQualifiedTypeName() {
		return fQualifiedName;
	}

	public String getName() {
		return fQualifiedName.getName();
	}
	
	public boolean isEnclosedType() {
		return (fQualifiedName.getEnclosingNames() != null);
	}

	public ITypeInfo getEnclosingType(int kinds[]) {
		if (fTypeCache != null) {
			return fTypeCache.getEnclosingType(this, kinds);
		}
		return null;
	}

	public ITypeInfo getEnclosingType() {
		return getEnclosingType(KNOWN_TYPES);
	}
	
	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace) {
		if (fTypeCache != null) {
			return fTypeCache.getRootNamespace(this, true);
		}
		return null;
	}
	
	public boolean isEnclosingType() {
		return (fElementType == ICElement.C_NAMESPACE
			|| fElementType == ICElement.C_CLASS
			|| fElementType == ICElement.C_STRUCT);
	}
	
	public boolean encloses(ITypeInfo info) {
		if (isEnclosingType() && fTypeCache == info.getCache()) {
			return fQualifiedName.isPrefixOf(info.getQualifiedTypeName());
		}
		return false;
	}
	
	public boolean isEnclosed(ITypeInfo info) {
		return info.encloses(this);
	}

	public boolean hasEnclosedTypes() {
		if (isEnclosingType() && fTypeCache != null) {
			return fTypeCache.hasEnclosedTypes(this);
		}
		return false;
	}
	
	public ITypeInfo[] getEnclosedTypes() {
		return getEnclosedTypes(KNOWN_TYPES);
	}

	public ITypeInfo[] getEnclosedTypes(int kinds[]) {
		if (fTypeCache != null) {
			return fTypeCache.getEnclosedTypes(this, kinds);
		}
		return EMPTY_TYPES;
	}

	public IProject getEnclosingProject() {
		if (fTypeCache != null) {
			return fTypeCache.getProject();
		}
		return null;
	}

	public String toString() {
		return fQualifiedName.toString();
	}
	
	public boolean isEnclosed(ITypeSearchScope scope) {
		if (scope == null || scope.isWorkspaceScope())
			return true;

		// check if path is in scope
		for (int i = 0; i < fSourceRefsCount; ++i) {
			ITypeReference location = fSourceRefs[i];
			if (scope.encloses(location.getPath()))
				return true;
		}

		return false;
	}
	
	public int hashCode() {
		int hashCode = fQualifiedName.hashCode() + fElementType;
		IProject project = getEnclosingProject();
		if (project != null)
			hashCode += project.hashCode();
		return hashCode;
	}
	
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof TypeInfo)) {
			return false;
		}
		return isExactMatch((TypeInfo)obj);
	}
	
	public int compareTo(Object obj) {
		if (obj == this) {
			return 0;
		}
		if( !(obj instanceof TypeInfo)) {
			throw new ClassCastException();
		}
		TypeInfo info= (TypeInfo)obj;
		if (fElementType != info.fElementType)
			return (fElementType < info.fElementType) ? -1 : 1;
		return fQualifiedName.compareTo(info.getQualifiedTypeName());
	}

	public static boolean isValidType(int type) {
		return ArrayUtil.contains(KNOWN_TYPES, type);
	}
	
	public ITypeCache getCache() {
		return fTypeCache;
	}

	public void setCache(ITypeCache typeCache) {
		fTypeCache = typeCache;
	}
}
