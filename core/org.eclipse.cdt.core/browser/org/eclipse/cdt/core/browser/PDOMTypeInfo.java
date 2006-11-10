/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		QNX - Initial API and implementation
 * 		IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.core.browser;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMTypeInfo implements ITypeInfo {

	private final IBinding binding;
	private final int elementType;
	private final ICProject project;

	public PDOMTypeInfo(IBinding binding, int elementType, ICProject project) {
		this.binding = binding;
		this.elementType = elementType;
		this.project = project;
	}
	
	public void addDerivedReference(ITypeReference location) {
		throw new PDOMNotImplementedError();
	}

	public void addReference(ITypeReference location) {
		throw new PDOMNotImplementedError();
	}

	public boolean canSubstituteFor(ITypeInfo info) {
		throw new PDOMNotImplementedError();
	}

	public boolean encloses(ITypeInfo info) {
		throw new PDOMNotImplementedError();
	}

	public boolean exists() {
		throw new PDOMNotImplementedError();
	}

	public int getCElementType() {
		return elementType;
	}

	public ITypeReference[] getDerivedReferences() {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo[] getEnclosedTypes() {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo[] getEnclosedTypes(int[] kinds) {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo getEnclosingNamespace(boolean includeGlobalNamespace) {
		throw new PDOMNotImplementedError();
	}

	public ICProject getEnclosingProject() {
		return project;
	}

	public ITypeInfo getEnclosingType() {
		// TODO not sure
		return null;
	}

	public ITypeInfo getEnclosingType(int[] kinds) {
		throw new PDOMNotImplementedError();
	}

	public String getName() {
		return binding.getName();
	}

	public IQualifiedTypeName getQualifiedTypeName() {
		String qn;
		if(binding instanceof ICPPBinding) {
			try {
				qn = CPPVisitor.renderQualifiedName(((ICPPBinding)binding).getQualifiedName());
			} catch(DOMException de) {
				CCorePlugin.log(de); // can't happen when (binding instanceof PDOMBinding)
				return null;
			}
		} else {
			qn = binding.getName();
		}
		
		return new QualifiedTypeName(qn);
	}

	public ITypeReference[] getReferences() {
		throw new PDOMNotImplementedError();
	}

	public ITypeReference getResolvedReference() {
		try {
			PDOM pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
			IName[] names= pdom.findNames(binding, IIndex.FIND_DEFINITIONS);
			return names != null && names.length > 0 ? new PDOMTypeReference(names[0], project) : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace) {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo[] getSubTypes() {
		throw new PDOMNotImplementedError();
	}

	public ASTAccessVisibility getSuperTypeAccess(ITypeInfo subType) {
		throw new PDOMNotImplementedError();
	}

	public ITypeInfo[] getSuperTypes() {
		throw new PDOMNotImplementedError();
	}

	public boolean hasEnclosedTypes() {
		throw new PDOMNotImplementedError();
	}

	public boolean hasSubTypes() {
		throw new PDOMNotImplementedError();
	}

	public boolean hasSuperTypes() {
		throw new PDOMNotImplementedError();
	}

	public boolean isClass() {
		throw new PDOMNotImplementedError();
	}

	public boolean isEnclosed(ITypeInfo info) {
		throw new PDOMNotImplementedError();
	}

	public boolean isEnclosed(ITypeSearchScope scope) {
		throw new PDOMNotImplementedError();
	}

	public boolean isEnclosedType() {
		throw new PDOMNotImplementedError();
	}

	public boolean isEnclosingType() {
		throw new PDOMNotImplementedError();
	}

	public boolean isReferenced(ITypeSearchScope scope) {
		throw new PDOMNotImplementedError();
	}

	public boolean isUndefinedType() {
		throw new PDOMNotImplementedError();
	}

	public void setCElementType(int type) {
		throw new PDOMNotImplementedError();
	}

	public int compareTo(Object arg0) {
		throw new PDOMNotImplementedError();
	}

}
