/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser;

import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.browser.IFunctionInfo;
import org.eclipse.cdt.core.browser.IQualifiedTypeName;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.ITypeSearchScope;
import org.eclipse.cdt.core.browser.QualifiedTypeName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Type info object needed to support search for local variables.
 * @since 5.0
 */
public class ASTTypeInfo implements ITypeInfo, IFunctionInfo {
	private static int hashCode(String[] array) {
		int prime = 31;
		if (array == null)
			return 0;
		int result = 1;
		for (String element : array) {
			result = prime * result + (element == null ? 0 : element.hashCode());
		}
		return result;
	}

	private final String[] fqn;
	private final int elementType;
	private final String[] params;
	private final String returnType;
	private ASTTypeReference reference; 
	
	/**
	 * Creates a type info suitable for the binding.
	 * @param name the name to create the type info object for.
	 */
	public static ASTTypeInfo create(IASTName name) {
		try {
			String[] fqn;
			int elementType;
			final IBinding binding = name.resolveBinding();
			final ASTTypeReference ref= createReference(name);
			elementType = IndexModelUtil.getElementType(binding);
			if (binding instanceof ICPPBinding) {
				fqn= ((ICPPBinding)binding).getQualifiedName();
			} 
			else if (binding instanceof IField) {
				IField field= (IField) binding;
				ICompositeType owner= field.getCompositeTypeOwner();
				fqn= new String[] {owner.getName(), field.getName()};	
			}
			else {
				fqn= new String[] {binding.getName()};
			}
			if (binding instanceof IFunction) {
				final IFunction function= (IFunction)binding;
				final String[] paramTypes= IndexModelUtil.extractParameterTypes(function);
				final String returnType= IndexModelUtil.extractReturnType(function);
				return new ASTTypeInfo(fqn, elementType, paramTypes, returnType, ref);
			}
			return new ASTTypeInfo(fqn, elementType, null, null, ref);
		} catch (DOMException e) {
			Assert.isTrue(false);
		}

		return null;
	}


	private ASTTypeInfo(String[] fqn, int elementType, String[] params, String returnType, ASTTypeReference reference) {
		Assert.isNotNull(reference);
		this.fqn= fqn;
		this.elementType= elementType;
		this.params= params;
		this.returnType= returnType;
		this.reference= reference;
	}
	
	public int getCElementType() {
		return elementType;
	}

	public String getName() {
		return fqn[fqn.length-1];
	}

	public IQualifiedTypeName getQualifiedTypeName() {
		return new QualifiedTypeName(fqn);
	}

	public ITypeReference getResolvedReference() {
		return reference;
	}

	public ITypeReference[] getReferences() {
		return new ITypeReference[] {reference};
	}

	public ICProject getEnclosingProject() {
		if(getResolvedReference()!=null) {
			IProject project = reference.getProject();
			if(project!=null) {
				return CCorePlugin.getDefault().getCoreModel().getCModel().getCProject(project.getName());
			}
		}
		return null;
	}

	public String[] getParameters() {
		return params;
	}

	/*
	 * @see org.eclipse.cdt.internal.core.browser.IFunctionInfo#getReturnType()
	 */
	public String getReturnType() {
		return returnType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elementType;
		result = prime * result + ASTTypeInfo.hashCode(fqn);
		result = prime * result + ASTTypeInfo.hashCode(params);
		return result;
	}

	/**
	 * Type info objects are equal if they compute the same references.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ASTTypeInfo other = (ASTTypeInfo) obj;
		if (elementType != other.elementType)
			return false;
		if (!Arrays.equals(fqn, other.fqn))
			return false;
		if (!Arrays.equals(params, other.params))
			return false;
		return true;
	}

	public IIndexFileLocation getIFL() {
		return reference.getIFL();
	}

	private static ASTTypeReference createReference(IASTName name) {
		IASTFileLocation floc= name.getFileLocation();
		if (floc != null) {
			String filename= floc.getFileName();
			IIndexFileLocation ifl= IndexLocationFactory.getIFLExpensive(filename);
			String fullPath= ifl.getFullPath();
			if (fullPath != null) {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullPath));
				if (file != null) {
					return new ASTTypeReference(ifl, name.resolveBinding(), file, 
							floc.getNodeOffset(), floc.getNodeLength());
				}
			} else {
				IPath path = URIUtil.toPath(ifl.getURI());
				if (path != null) {
					return new ASTTypeReference(ifl, name.resolveBinding(), path,
							floc.getNodeOffset(), floc.getNodeLength());
				}
			}
		}
		return null;
	}
	
	@Deprecated
	public void addDerivedReference(ITypeReference location) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public void addReference(ITypeReference location) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean canSubstituteFor(ITypeInfo info) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean encloses(ITypeInfo info) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean exists() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public ITypeReference[] getDerivedReferences() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public ITypeInfo[] getEnclosedTypes() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public ITypeInfo[] getEnclosedTypes(int[] kinds) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public ITypeInfo getEnclosingNamespace(boolean includeGlobalNamespace) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public ITypeInfo getEnclosingType() {
		// TODO not sure
		return null;
	}

	@Deprecated
	public ITypeInfo getEnclosingType(int[] kinds) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public ITypeInfo getRootNamespace(boolean includeGlobalNamespace) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public ITypeInfo[] getSubTypes() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public ASTAccessVisibility getSuperTypeAccess(ITypeInfo subType) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public ITypeInfo[] getSuperTypes() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean hasEnclosedTypes() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean hasSubTypes() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean hasSuperTypes() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean isClass() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean isEnclosed(ITypeInfo info) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean isEnclosed(ITypeSearchScope scope) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean isEnclosedType() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean isEnclosingType() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean isReferenced(ITypeSearchScope scope) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean isUndefinedType() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public void setCElementType(int type) {
		throw new UnsupportedOperationException();
	}
}
