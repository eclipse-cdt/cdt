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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.browser.cache.ITypeCache;
import org.eclipse.core.resources.IProject;

public class TypeInfo implements ITypeInfo, Comparable
{
	private ITypeCache fTypeCache;
	private int fElementType;
	private QualifiedTypeName fQualifiedName;
	private Set fSourceRefs = new HashSet();

	public TypeInfo(int elementType, IQualifiedTypeName typeName, ITypeCache typeCache) {
		fElementType = elementType;
		fQualifiedName = new QualifiedTypeName(typeName);
		fTypeCache = typeCache;
	}

	public void addReference(ITypeReference location) {
		fSourceRefs.add(location);
	}
	
	public ITypeReference[] getReferences() {
		return (ITypeReference[]) fSourceRefs.toArray(new ITypeReference[fSourceRefs.size()]);
	}

	public ITypeReference getResolvedReference() {
		for (Iterator i = fSourceRefs.iterator(); i.hasNext(); ) {
			ITypeReference location = (ITypeReference) i.next();
			if (location.getLength() != 0) {
				return location;
			}
		}
		return null;
	}

	public boolean isReferenced() {
		return !fSourceRefs.isEmpty();
	}
	
	public boolean isUndefinedType() {
		return fElementType == 0;
	}
	
	public boolean isQualifierType() {
		return (fElementType == ICElement.C_NAMESPACE
			|| fElementType == ICElement.C_CLASS
			|| fElementType == ICElement.C_STRUCT);
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
	
	public ITypeInfo getEnclosingType() {
		ITypeInfo enclosingType = null;
		if (fTypeCache != null) {
			IQualifiedTypeName parentName = fQualifiedName.getEnclosingTypeName();
			if (parentName != null) {
				ITypeInfo[] types = fTypeCache.getTypes(parentName);
				for (int i = 0; i < types.length; ++i) {
					ITypeInfo info = types[i];
					if (info.isQualifierType()) {
						enclosingType = info;
						break;
					} else if (info.isUndefinedType()) {
						enclosingType = info;
						// don't break, in case we can still find a defined type
					}
				}
			}
		}
		return enclosingType;
	}
	
	public IProject getEnclosingProject() {
		if (fTypeCache != null) {
			return fTypeCache.getProject();
		} else {
			return null;
		}
	}

	public String toString() {
		return fQualifiedName.toString();
	}
	
	public boolean isEnclosed(ITypeSearchScope scope) {
		if (scope == null || scope.isWorkspaceScope())
			return true;

		// check if path is in scope
		for (Iterator i = fSourceRefs.iterator(); i.hasNext(); ) {
			ITypeReference location = (ITypeReference) i.next();
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
		ITypeInfo info= (TypeInfo)obj;
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
		switch (type) {
			case ICElement.C_NAMESPACE:
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
			case ICElement.C_UNION:
			case ICElement.C_ENUMERATION:
			case ICElement.C_TYPEDEF:
				return true;
			
			default:
				return false;
		}
	}
}
