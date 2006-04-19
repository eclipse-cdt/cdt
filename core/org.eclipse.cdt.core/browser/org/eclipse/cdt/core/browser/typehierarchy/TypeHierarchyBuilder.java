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
package org.eclipse.cdt.core.browser.typehierarchy;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.browser.cache.TypeCacheManager;
import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

public class TypeHierarchyBuilder {
	
	public TypeHierarchyBuilder() {
	}
	
	public ITypeHierarchy createTypeHierarchy(ITypeInfo info, boolean enableIndexing, IProgressMonitor monitor) throws CModelException {
		TypeHierarchy typeHierarchy = new TypeHierarchy(info);
		Set processedTypes = new HashSet();
		addSuperClasses(typeHierarchy, info, processedTypes, enableIndexing, monitor);

		typeHierarchy.addRootType(info);
		processedTypes.clear();
		addSubClasses(typeHierarchy, info, processedTypes, enableIndexing, monitor);

		return typeHierarchy;
	}

	private void addSuperClasses(TypeHierarchy typeHierarchy, ITypeInfo type, Set processedTypes, boolean enableIndexing, IProgressMonitor monitor) throws CModelException {
		if (type.hasSuperTypes()) {
			ITypeInfo[] superTypes = TypeCacheManager.getInstance().locateSuperTypesAndWait(type, enableIndexing, Job.SHORT, monitor);
			if (superTypes == null)
				throw new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST));
			
			for (int i = 0; i < superTypes.length; ++i) {
				ITypeInfo superType = superTypes[i];
				
				// recursively process sub sub classes
				if (!processedTypes.contains(superType)) {
					processedTypes.add(superType);
					addSuperClasses(typeHierarchy, superType, processedTypes, enableIndexing, monitor);
				}
				
				ASTAccessVisibility access = type.getSuperTypeAccess(superType);
				
				typeHierarchy.addSuperType(type, superType, access);
			}
		} else {
			typeHierarchy.addRootType(type);
		}
	}

	private void addSubClasses(TypeHierarchy typeHierarchy, ITypeInfo type, Set processedTypes, boolean enableIndexing, IProgressMonitor monitor) throws CModelException {
		if (type.hasSubTypes()) {
			ITypeInfo[] subTypes = TypeCacheManager.getInstance().locateSubTypesAndWait(type, enableIndexing, Job.SHORT, monitor);
			if (subTypes == null)
				throw new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST));
			
			for (int i = 0; i < subTypes.length; ++i) {
				ITypeInfo subType = subTypes[i];
				
				// recursively process sub sub classes
				if (!processedTypes.contains(subType)) {
					processedTypes.add(subType);
					addSubClasses(typeHierarchy, subType, processedTypes, enableIndexing, monitor);
				}
				
				typeHierarchy.addSubType(type, subType);
			}
		}
	}

/*
 	private IStructure findCElementForType(ITypeInfo info, boolean enableIndexing, IProgressMonitor monitor) throws CModelException {
		if (!info.exists())
			throw new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST));

		if (!info.isClass())
			throw new CModelException(new CModelStatus(ICModelStatusConstants.INVALID_ELEMENT_TYPES));
	
		// first we need to resolve the type location
		ITypeReference location = TypeCacheManager.getInstance().locateTypeAndWait(info, enableIndexing, Job.SHORT, monitor);
		if (location == null)
			throw new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST));
		
		ICElement cElem = location.getCElement();
		if (cElem == null)
			throw new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST));
		
		if (!(cElem instanceof IStructure))
			throw new CModelException(new CModelStatus(ICModelStatusConstants.INVALID_ELEMENT_TYPES));
		
		IStructure cClass = (IStructure)cElem;
	
		// check if type exists in cache
		ITypeInfo type = TypeCacheManager.getInstance().getTypeForElement(cElem);
		if (type == null || !type.equals(info))
			throw new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST));
		
		return cClass;
	}
	
	private ITypeInfo findTypeInCache(ITypeCache cache, String name) {
		IQualifiedTypeName qualName = new QualifiedTypeName(name);
		ITypeInfo[] superTypes = cache.getTypes(qualName);
		for (int i = 0; i < superTypes.length; ++i) {
			ITypeInfo superType = superTypes[i];
			if (superType.isClass()) {
				return superType;
			}
		}
		return null;
	}
*/
	
}
