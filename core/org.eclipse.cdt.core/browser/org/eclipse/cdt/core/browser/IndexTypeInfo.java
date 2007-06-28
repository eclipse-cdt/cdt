/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		QNX - Initial API and implementation
 * 		IBM Corporation
 *      Andrew Ferguson (Symbian)
 *      Anton Leherbauer (Wind River Systems)
 *      Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.browser.IndexTypeReference;
import org.eclipse.cdt.internal.core.browser.util.IndexModelUtil;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 *
 */
public class IndexTypeInfo implements ITypeInfo, IFunctionInfo {
	private final String[] fqn;
	private final int elementType;
	private final IIndex index;
	private final String[] params;
	private final String returnType;
	private ITypeReference reference; // lazily constructed
	
	/**
	 * Creates a typeinfo suitable for the binding.
	 * @since 4.0.1
	 */
	public static IndexTypeInfo create(IIndex index, IIndexBinding binding) {
		String[] fqn;
		int elementType;
		try {
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
				return new IndexTypeInfo(fqn, elementType, paramTypes, returnType, null);
			}
		} catch (DOMException e) {
			// index bindings don't throw DOMExceptions.
			throw new AssertionError();
		}

		return new IndexTypeInfo(fqn, elementType, index);
	}

	/**
	 * @deprecated, use {@link #create(IIndex, IBinding)}.
	 */
	public IndexTypeInfo(String[] fqn, int elementType, IIndex index) {
		this.fqn = fqn;
		this.elementType = elementType;
		this.index = index;
		this.params= null;
		this.returnType= null;
	}

	/**
	 * @deprecated, use {@link #create(IIndex, IBinding)}.
	 */
	public IndexTypeInfo(String[] fqn, int elementType, String[] params, String returnType, IIndex index) {
		this.fqn = fqn;
		this.elementType = elementType;
		this.params= params;
		this.returnType= returnType;
		this.index = index;
	}
	
	public IndexTypeInfo(IndexTypeInfo rhs, ITypeReference ref) {
		this(rhs.fqn, rhs.elementType, rhs.params, rhs.returnType, rhs.index);
		this.reference= ref;
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
		if(getResolvedReference()!=null) {
			IProject project = reference.getProject();
			if(project!=null) {
				return CCorePlugin.getDefault().getCoreModel().getCModel().getCProject(project.getName());
			}
		}
		return null;
	}

	public ITypeInfo getEnclosingType() {
		// TODO not sure
		return null;
	}

	public ITypeInfo getEnclosingType(int[] kinds) {
		throw new PDOMNotImplementedError();
	}

	public String getName() {
		return fqn[fqn.length-1];
	}

	public IQualifiedTypeName getQualifiedTypeName() {
		return new QualifiedTypeName(fqn);
	}

	public ITypeReference getResolvedReference() {
		if(reference==null) {
			try {
				index.acquireReadLock();

				char[][] cfqn = new char[fqn.length][];
				for(int i=0; i<fqn.length; i++)
					cfqn[i] = fqn[i].toCharArray();

				IIndexBinding[] ibs = index.findBindings(cfqn, new IndexFilter() {
					public boolean acceptBinding(IBinding binding) {
						boolean sameType= IndexModelUtil.bindingHasCElementType(binding, new int[]{elementType});
						if (sameType && binding instanceof IFunction && params != null) {
							try {
								String[]otherParams= IndexModelUtil.extractParameterTypes((IFunction)binding);
								return Arrays.equals(params, otherParams);
							} catch (DOMException exc) {
								CCorePlugin.log(exc);				
							}
						}
						return sameType;
					}
				}, new NullProgressMonitor());
				if(ibs.length>0) {
					IIndexName[] names;
					names= index.findNames(ibs[0], IIndex.FIND_DEFINITIONS);
					if (names.length == 0) {
						names= index.findNames(ibs[0], IIndex.FIND_DECLARATIONS);
					}
					for (int i = 0; i < names.length; i++) {
						reference= createReference(ibs[0], names[i]);
						if (reference != null) {
							break;
						}
					}
				}
			} catch(CoreException ce) {
				CCorePlugin.log(ce);				
			} catch (InterruptedException ie) {
				CCorePlugin.log(ie);
			} finally {
				index.releaseReadLock();
			}
		}
		return reference;
	}

	private IndexTypeReference createReference(IIndexBinding binding, IIndexName indexName) throws CoreException {
		IIndexFileLocation ifl = indexName.getFile().getLocation();
		String fullPath = ifl.getFullPath();
		if (fullPath != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullPath));
			if(file!=null) {
				return new IndexTypeReference(
						binding, file, file.getProject(), indexName.getNodeOffset(), indexName.getNodeLength()
				);
			}
		} else {
			IPath path = URIUtil.toPath(ifl.getURI());
			if(path!=null) {
				return new IndexTypeReference(
						binding, path, null, indexName.getNodeOffset(), indexName.getNodeLength()
				);
			}
		}
		return null;
	}

	public ITypeReference[] getReferences() {
		List references= new ArrayList();
		try {
			index.acquireReadLock();

			char[][] cfqn = new char[fqn.length][];
			for(int i=0; i<fqn.length; i++)
				cfqn[i] = fqn[i].toCharArray();

			IIndexBinding[] ibs = index.findBindings(cfqn, new IndexFilter() {
				public boolean acceptBinding(IBinding binding) {
					boolean sameType= IndexModelUtil.bindingHasCElementType(binding, new int[]{elementType});
					if (sameType && binding instanceof IFunction && params != null) {
						try {
							String[]otherParams= IndexModelUtil.extractParameterTypes((IFunction)binding);
							return Arrays.equals(params, otherParams);
						} catch (DOMException exc) {
							CCorePlugin.log(exc);				
						}
					}
					return sameType;
				}
			}, new NullProgressMonitor());
			for (int i = 0; i < ibs.length; i++) {
				IIndexBinding binding = ibs[i];
				IIndexName[] names;
				names= index.findNames(binding, IIndex.FIND_DEFINITIONS);
				if (names.length == 0) {
					names= index.findNames(ibs[0], IIndex.FIND_DECLARATIONS);
				}
				for (int j = 0; j < names.length; j++) {
					IIndexName indexName = names[j];
					IndexTypeReference ref= createReference(binding, indexName);
					if (ref != null) {
						references.add(ref);
					}
				}
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);				
		} catch (InterruptedException ie) {
			CCorePlugin.log(ie);
		} finally {
			index.releaseReadLock();
		}
		return (IndexTypeReference[]) references.toArray(new IndexTypeReference[references.size()]);
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

	/*
	 * @see org.eclipse.cdt.internal.core.browser.IFunctionInfo#getParameters()
	 */
	public String[] getParameters() {
		return params;
	}

	/*
	 * @see org.eclipse.cdt.internal.core.browser.IFunctionInfo#getReturnType()
	 */
	public String getReturnType() {
		return returnType;
	}

}
