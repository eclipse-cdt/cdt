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
import org.eclipse.cdt.internal.core.browser.util.ArrayUtil;

public class TypeInfo implements ITypeInfo
{
//	protected ITypeCache fTypeCache;
	protected int fElementType;
	protected IQualifiedTypeName fQualifiedName;
	protected ITypeReference[] fSourceRefs = null;
	protected int fSourceRefsCount = 0;
	protected ITypeReference[] fDerivedSourceRefs = null;
	protected int fDerivedSourceRefsCount = 0;

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
	
	public ITypeReference getResolvedReference() {
		for (int i = 0; i < fSourceRefsCount; ++i) {
			ITypeReference location = fSourceRefs[i];
			if (location.isLineNumber() )
                return location;
            if( location.getLength() != 0) {
				return location;
			}
		}
		return null;
	}

	public boolean isReferenced() {
		return (fSourceRefs != null || fDerivedSourceRefs != null);
	}
	
	public boolean isReferenced(ITypeSearchScope scope) {
		if (scope == null || scope.isWorkspaceScope())
			return true;

		// check if path is in scope
		for (int i = 0; i < fSourceRefsCount; ++i) {
			ITypeReference location = fSourceRefs[i];
			if (scope.encloses(location.getPath()))
				return true;
		}
		for (int i = 0; i < fDerivedSourceRefsCount; ++i) {
			ITypeReference location = fDerivedSourceRefs[i];
			if (scope.encloses(location.getPath()))
				return true;
		}
		
		return false;
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
			ICProject project1 = getEnclosingProject();
			ICProject project2 = info.getEnclosingProject();
			if (project1 == null && project2 == null)
				return true;
			if (project1 == null || project2 == null)
				return false;
			return project1.equals(project2);
		}
		return false;
	}
	
	public boolean exists() {
//		return fTypeCache != null;
		return true;
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
		return (fQualifiedName.isQualified());
	}

	public ITypeInfo getEnclosingType(int kinds[]) {
//		if (fTypeCache != null) {
//			return fTypeCache.getEnclosingType(this, kinds);
//		}
		return null;
	}

	public ITypeInfo getEnclosingType() {
		return getEnclosingType(KNOWN_TYPES);
	}
	
	public ITypeInfo getEnclosingNamespace(boolean includeGlobalNamespace) {
//		if (fTypeCache != null) {
//			return fTypeCache.getEnclosingNamespace(this, includeGlobalNamespace);
//		}
		return null;
	}

	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace) {
//		if (fTypeCache != null) {
//			return fTypeCache.getRootNamespace(this, includeGlobalNamespace);
//		}
		return null;
	}
	
	public boolean isEnclosingType() {
		return (fElementType == ICElement.C_NAMESPACE
			|| fElementType == ICElement.C_CLASS
			|| fElementType == ICElement.C_STRUCT);
	}
	
	public boolean encloses(ITypeInfo info) {
//		if (isEnclosingType() && fTypeCache == info.getCache()) {
//			return fQualifiedName.isPrefixOf(info.getQualifiedTypeName());
//		}
		return false;
	}
	
	public boolean isEnclosed(ITypeInfo info) {
		return info.encloses(this);
	}

	public boolean hasEnclosedTypes() {
//		if (isEnclosingType() && fTypeCache != null) {
//			return fTypeCache.hasEnclosedTypes(this);
//		}
		return false;
	}
	
	public ITypeInfo[] getEnclosedTypes() {
		return getEnclosedTypes(KNOWN_TYPES);
	}

	public ITypeInfo[] getEnclosedTypes(int kinds[]) {
//		if (fTypeCache != null) {
//			return fTypeCache.getEnclosedTypes(this, kinds);
//		}
		return EMPTY_TYPES;
	}

	public ICProject getEnclosingProject() {
//		if (fTypeCache != null) {
//			return fTypeCache.getProject();
//		}
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
		ICProject project = getEnclosingProject();
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
	
	public void addDerivedReference(ITypeReference location) {
		if (fDerivedSourceRefs == null) {
		    fDerivedSourceRefs = new ITypeReference[INITIAL_REFS_SIZE];
		    fDerivedSourceRefsCount = 0;
		} else if (fDerivedSourceRefsCount == fDerivedSourceRefs.length) {
			ITypeReference[] refs = new ITypeReference[fDerivedSourceRefs.length + REFS_GROW_BY];
			System.arraycopy(fDerivedSourceRefs, 0, refs, 0, fDerivedSourceRefsCount);
			fDerivedSourceRefs = refs;
		}
		fDerivedSourceRefs[fDerivedSourceRefsCount] = location;
		++fDerivedSourceRefsCount;
	}
	
	public ITypeReference[] getDerivedReferences() {
		if (fDerivedSourceRefs != null) {
			ITypeReference[] refs = new ITypeReference[fDerivedSourceRefsCount];
			System.arraycopy(fDerivedSourceRefs, 0, refs, 0, fDerivedSourceRefsCount);
			return refs;
		}
		return null;
	}
	
	public boolean hasSubTypes() {
		return (fDerivedSourceRefs != null);
	}
	
	public ITypeInfo[] getSubTypes() {
//		if (fTypeCache != null) {
//			return fTypeCache.getSubtypes(this);
//		}
		return null;
	}
	
	public boolean hasSuperTypes() {
//		if (fTypeCache != null) {
//			return (fTypeCache.getSupertypes(this) != null);
//		}
		return false;
//		return true;	//TODO can't know this until we parse
	}
	
	public ITypeInfo[] getSuperTypes() {
//		if (fTypeCache != null) {
//			return fTypeCache.getSupertypes(this);
//		}
		return null;
	}

	public ASTAccessVisibility getSuperTypeAccess(ITypeInfo superType) {
//		if (fTypeCache != null) {
//			return fTypeCache.getSupertypeAccess(this, superType);
//		}
		return null;
	}

	public boolean isClass() {
		return (fElementType == ICElement.C_CLASS
				|| fElementType == ICElement.C_STRUCT);
	}
}
