/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser.typehierarchy;

import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CElement;

/*
 * Collects changes (reported through fine-grained deltas) that can affect a type hierarchy.
 */
public class ChangeCollector {
	
	/*
	 * A table from ICElements to TypeDeltas
	 */
	HashMap changes = new HashMap();
	
	TypeHierarchy hierarchy;
	
	public ChangeCollector(TypeHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}
	
	/*
	 * Adds the children of the given delta to the list of changes.
	 */
	private void addAffectedChildren(ICElementDelta delta) throws CModelException {
//		ICElementDelta[] children = delta.getAffectedChildren();
//		for (int i = 0, length = children.length; i < length; i++) {
//			ICElementDelta child = children[i];
//			ICElement childElement = child.getElement();
//			switch (childElement.getElementType()) {
//				case ICElement.IMPORT_CONTAINER:
//					addChange((IImportContainer)childElement, child);
//					break;
//				case ICElement.IMPORT_DECLARATION:
//					addChange((IImportDeclaration)childElement, child);
//					break;
//				case ICElement.TYPE:
//					addChange((ICElement)childElement, child);
//					break;
//				case ICElement.INITIALIZER:
//				case ICElement.FIELD:
//				case ICElement.METHOD:
//					addChange((IMember)childElement, child);
//					break;
//			}
//		}
	}
	
	/*
	 * Adds the given delta on a compilation unit to the list of changes.
	 */
	public void addChange(ITranslationUnit cu, ICElementDelta newDelta) throws CModelException {
//		int newKind = newDelta.getKind();
//		switch (newKind) {
//			case ICElementDelta.ADDED:
//				ArrayList allTypes = new ArrayList();
//				getAllTypesFromElement(cu, allTypes);
//				for (int i = 0, length = allTypes.size(); i < length; i++) {
//					ICElement type = (ICElement)allTypes.get(i);
//					addTypeAddition(type, (SimpleDelta)this.changes.get(type));
//				}
//				break;
//			case ICElementDelta.REMOVED:
//				allTypes = new ArrayList();
//				getAllTypesFromHierarchy((JavaElement)cu, allTypes);
//				for (int i = 0, length = allTypes.size(); i < length; i++) {
//					ICElement type = (ICElement)allTypes.get(i);
//					addTypeRemoval(type, (SimpleDelta)this.changes.get(type));
//				}
//				break;
//			case ICElementDelta.CHANGED:
//				addAffectedChildren(newDelta);
//				break;
//		}
	}
	
/*	private void addChange(IImportContainer importContainer, ICElementDelta newDelta) throws CModelException {
		int newKind = newDelta.getKind();
		if (newKind == ICElementDelta.CHANGED) {
			addAffectedChildren(newDelta);
			return;
		}
		SimpleDelta existingDelta = (SimpleDelta)this.changes.get(importContainer);
		if (existingDelta != null) {
			switch (newKind) {
				case ICElementDelta.ADDED:
					if (existingDelta.getKind() == ICElementDelta.REMOVED) {
						// REMOVED then ADDED
						this.changes.remove(importContainer);
					}
					break;
				case ICElementDelta.REMOVED:
					if (existingDelta.getKind() == ICElementDelta.ADDED) {
						// ADDED then REMOVED
						this.changes.remove(importContainer);
					}
					break;
					// CHANGED handled above
			}
		} else {
			SimpleDelta delta = new SimpleDelta();
			switch (newKind) {
				case ICElementDelta.ADDED:
					delta.added();
					break;
				case ICElementDelta.REMOVED:
					delta.removed();
					break;
			}
			this.changes.put(importContainer, delta);
		}
	}
	
	private void addChange(IImportDeclaration importDecl, ICElementDelta newDelta) {
		SimpleDelta existingDelta = (SimpleDelta)this.changes.get(importDecl);
		int newKind = newDelta.getKind();
		if (existingDelta != null) {
			switch (newKind) {
				case ICElementDelta.ADDED:
					if (existingDelta.getKind() == ICElementDelta.REMOVED) {
						// REMOVED then ADDED
						this.changes.remove(importDecl);
					}
					break;
				case ICElementDelta.REMOVED:
					if (existingDelta.getKind() == ICElementDelta.ADDED) {
						// ADDED then REMOVED
						this.changes.remove(importDecl);
					}
					break;
				// CHANGED cannot happen for import declaration
			}
		} else {
			SimpleDelta delta = new SimpleDelta();
			switch (newKind) {
				case ICElementDelta.ADDED:
					delta.added();
					break;
				case ICElementDelta.REMOVED:
					delta.removed();
					break;
			}
			this.changes.put(importDecl, delta);
		}
	}
*/
	
	/*
	 * Adds a change for the given member (a method, a field or an initializer) and the types it defines.
	 */
	private void addChange(IMember member, ICElementDelta newDelta) throws CModelException {
//		int newKind = newDelta.getKind();
//		switch (newKind) {
//			case ICElementDelta.ADDED:
//				ArrayList allTypes = new ArrayList();
//				getAllTypesFromElement(member, allTypes);
//				for (int i = 0, length = allTypes.size(); i < length; i++) {
//					ICElement innerType = (ICElement)allTypes.get(i);
//					addTypeAddition(innerType, (SimpleDelta)this.changes.get(innerType));
//				}
//				break;
//			case ICElementDelta.REMOVED:
//				allTypes = new ArrayList();
//				getAllTypesFromHierarchy((JavaElement)member, allTypes);
//				for (int i = 0, length = allTypes.size(); i < length; i++) {
//					ICElement type = (ICElement)allTypes.get(i);
//					addTypeRemoval(type, (SimpleDelta)this.changes.get(type));
//				}
//				break;
//			case ICElementDelta.CHANGED:
//				addAffectedChildren(newDelta);
//				break;
//		}
	}
	
	/*
	 * Adds a change for the given type and the types it defines.
	 */
	private void addChange(ICElement type, ICElementDelta newDelta) throws CModelException {
//		 int newKind = newDelta.getKind();
//		SimpleDelta existingDelta = (SimpleDelta)this.changes.get(type);
//		switch (newKind) {
//			case ICElementDelta.ADDED:
//				addTypeAddition(type, existingDelta);
//				ArrayList allTypes = new ArrayList();
//				getAllTypesFromElement(type, allTypes);
//				for (int i = 0, length = allTypes.size(); i < length; i++) {
//					ICElement innerType = (ICElement)allTypes.get(i);
//					addTypeAddition(innerType, (SimpleDelta)this.changes.get(innerType));
//				}
//				break;
//			case ICElementDelta.REMOVED:
//				addTypeRemoval(type, existingDelta);
//				allTypes = new ArrayList();
//				getAllTypesFromHierarchy((JavaElement)type, allTypes);
//				for (int i = 0, length = allTypes.size(); i < length; i++) {
//					ICElement innerType = (ICElement)allTypes.get(i);
//					addTypeRemoval(innerType, (SimpleDelta)this.changes.get(innerType));
//				}
//				break;
//			case ICElementDelta.CHANGED:
//				addTypeChange(type, newDelta.getFlags(), existingDelta);
//				addAffectedChildren(newDelta);
//				break;
//		}
	}

/*	private void addTypeAddition(ICElement type, SimpleDelta existingDelta) throws CModelException {
		if (existingDelta != null) {
			switch (existingDelta.getKind()) {
				case ICElementDelta.REMOVED:
					// REMOVED then ADDED
					boolean hasChange = false;
					if (hasSuperTypeChange(type)) {
						existingDelta.superTypes();
						hasChange = true;
					} 
					if (hasVisibilityChange(type)) {
						existingDelta.modifiers();
						hasChange = true;
					}
					if (!hasChange) {
						this.changes.remove(type);
					}
					break;
					// CHANGED then ADDED
					// or ADDED then ADDED: should not happen
			}
		} else {
			// check whether the type addition affects the hierarchy
			String typeName = type.getElementName();
			if (this.hierarchy.hasSupertype(typeName) 
					|| this.hierarchy.subtypesIncludeSupertypeOf(type) 
					|| this.hierarchy.missingTypes.contains(typeName)) {
				SimpleDelta delta = new SimpleDelta();
				delta.added();
				this.changes.put(type, delta);
			}
		}
	}
*/	
/*	private void addTypeChange(ICElement type, int newFlags, SimpleDelta existingDelta) throws CModelException {
		if (existingDelta != null) {
			switch (existingDelta.getKind()) {
				case ICElementDelta.CHANGED:
					// CHANGED then CHANGED
					int existingFlags = existingDelta.getFlags();
					boolean hasChange = false;
					if ((existingFlags & ICElementDelta.F_SUPER_TYPES) != 0
							&& hasSuperTypeChange(type)) {
						existingDelta.superTypes();
						hasChange = true;
					} 
					if ((existingFlags & ICElementDelta.F_MODIFIERS) != 0
							&& hasVisibilityChange(type)) {
						existingDelta.modifiers();
						hasChange = true;
					}
					if (!hasChange) {
						// super types and visibility are back to the ones in the existing hierarchy
						this.changes.remove(type);
					}
					break;
					// ADDED then CHANGED: leave it as ADDED
					// REMOVED then CHANGED: should not happen
			}
		} else {
			// check whether the type change affects the hierarchy
			SimpleDelta typeDelta = null;
			if ((newFlags & ICElementDelta.F_SUPER_TYPES) != 0 
					&& this.hierarchy.includesTypeOrSupertype(type)) {
				typeDelta = new SimpleDelta();
				typeDelta.superTypes();
			}
			if ((newFlags & ICElementDelta.F_MODIFIERS) != 0
					&& this.hierarchy.hasSupertype(type.getElementName())) {
				if (typeDelta == null) {
					typeDelta = new SimpleDelta();
				}
				typeDelta.modifiers();
			}
			if (typeDelta != null) {
				this.changes.put(type, typeDelta);
			}
		}
	}
*/
/*	private void addTypeRemoval(ICElement type, SimpleDelta existingDelta) {
		if (existingDelta != null) {
			switch (existingDelta.getKind()) {
				case ICElementDelta.ADDED:
					// ADDED then REMOVED
					this.changes.remove(type);
					break;
				case ICElementDelta.CHANGED:
					// CHANGED then REMOVED
					existingDelta.removed();
					break;
					// REMOVED then REMOVED: should not happen
			}
		} else {
			// check whether the type removal affects the hierarchy
			if (this.hierarchy.contains(type)) {
				SimpleDelta typeDelta = new SimpleDelta();
				typeDelta.removed();
				this.changes.put(type, typeDelta);
			}
		}
	}
*/	
	/*
	 * Returns all types defined in the given element excluding the given element.
	 */
	private void getAllTypesFromElement(ICElement element, ArrayList allTypes) throws CModelException {
		switch (element.getElementType()) {
			case ICElement.C_UNIT:
				ICElement[] types = TypeUtil.getTypes((ITranslationUnit)element);
				for (int i = 0, length = types.length; i < length; i++) {
					ICElement type = types[i];
					allTypes.add(type);
					getAllTypesFromElement(type, allTypes);
				}
				break;
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
//				types = ((ICElement)element).getTypes();
			    types = TypeUtil.getTypes(element);
				for (int i = 0, length = types.length; i < length; i++) {
					ICElement type = types[i];
					allTypes.add(type);
					getAllTypesFromElement(type, allTypes);
				}
				break;
//			case ICElement.INITIALIZER:
//			case ICElement.FIELD:
			case ICElement.C_METHOD:
			    if (element instanceof IParent) {
					ICElement[] children = ((IParent)element).getChildren();
					for (int i = 0, length = children.length; i < length; i++) {
						ICElement type = (ICElement)children[i];
						allTypes.add(type);
						getAllTypesFromElement(type, allTypes);
					}
			    }
				break;
		}
	}
	
	/*
	 * Returns all types in the existing hierarchy that have the given element as a parent.
	 */
	private void getAllTypesFromHierarchy(CElement element, ArrayList allTypes) {
		switch (element.getElementType()) {
			case ICElement.C_UNIT:
				ArrayList types = (ArrayList)this.hierarchy.files.get(element);
				if (types != null) {
					allTypes.addAll(types);
				}
				break;
			case ICElement.C_CLASS:
			case ICElement.C_STRUCT:
//			case ICElement.INITIALIZER:
//			case ICElement.FIELD:
			case ICElement.C_METHOD:
				types = (ArrayList)this.hierarchy.files.get(((IMember)element).getTranslationUnit());
				if (types != null) {
					for (int i = 0, length = types.size(); i < length; i++) {
						ICElement type = (ICElement)types.get(i);
						if (element.isAncestorOf(type)) {
							allTypes.add(type);
						}
					}
				}
				break;
		}
	}
	
	private boolean hasSuperTypeChange(ICElement type) throws CModelException {
//		// check super class
//		ICElement superclass = this.hierarchy.getSuperclass(type);
//		String existingSuperclassName = superclass == null ? null : superclass.getElementName();
//		String newSuperclassName = type.getSuperclassName();
//		if (existingSuperclassName != null && !existingSuperclassName.equals(newSuperclassName)) {
//			return true;
//		}
//		
//		// check super interfaces
//		ICElement[] existingSuperInterfaces = this.hierarchy.getSuperInterfaces(type);
//		String[] newSuperInterfaces = type.getSuperInterfaceNames();
//		if (existingSuperInterfaces.length != newSuperInterfaces.length) {
//			return true;
//		}
//		for (int i = 0, length = newSuperInterfaces.length; i < length; i++) {
//			String superInterfaceName = newSuperInterfaces[i];
//			if (!superInterfaceName.equals(newSuperInterfaces[i])) {
//				return true;
//			}
//		}
		
		return false;
	}
	
	private boolean hasVisibilityChange(ICElement type) throws CModelException {
//		int existingFlags = this.hierarchy.getCachedFlags(type);
//		int newFlags = type.getFlags();
//		return existingFlags != newFlags;
	    return false;
	}

	/*
	 * Whether the hierarchy needs refresh according to the changes collected so far.
	 */
	public boolean needsRefresh() {
		return changes.size() != 0;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator iterator = this.changes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			buffer.append(((CElement)entry.getKey()).toDebugString());
			buffer.append(entry.getValue());
			if (iterator.hasNext()) {
				buffer.append('\n');
			}
		}
		return buffer.toString();
	}
}
