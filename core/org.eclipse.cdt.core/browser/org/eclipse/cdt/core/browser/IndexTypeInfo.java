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
import java.util.HashMap;
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
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.browser.IndexTypeReference;
import org.eclipse.cdt.internal.core.browser.util.IndexModelUtil;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 *
 */
public class IndexTypeInfo implements ITypeInfo, IFunctionInfo {
	private static int hashCode(String[] array) {
		int prime = 31;
		if (array == null)
			return 0;
		int result = 1;
		for (int index = 0; index < array.length; index++) {
			result = prime * result + (array[index] == null ? 0 : array[index].hashCode());
		}
		return result;
	}

	private final String[] fqn;
	private final int elementType;
	private final IIndex index;
	private final String[] params;
	private final String returnType;
	private ITypeReference reference; // lazily constructed
	
	/**
	 * Creates a type info suitable for the binding.
	 * @param index a non-null index in which to locate references
	 * @param binding
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
				return new IndexTypeInfo(fqn, elementType, paramTypes, returnType, index);
			}
		} catch (DOMException e) {
			// index bindings don't throw DOMExceptions.
			throw new AssertionError();
		}

		return new IndexTypeInfo(fqn, elementType, index);
	}

	/**
	 * Creates a type info object suitable for a macro.
	 * @param index a non-null index in which to locate references
	 * @param binding
	 * @since 4.0.1
	 */
	public static IndexTypeInfo create(IIndex index, IIndexMacro macro) {
		final char[] name= macro.getName();
		return new IndexTypeInfo(new String[] {new String(name)}, ICElement.C_MACRO, index);
	}

	private IndexTypeInfo(String[] fqn, int elementType, IIndex index, String[] params, String returnType, ITypeReference reference) {
		Assert.isTrue(index != null);
		this.fqn= fqn;
		this.elementType= elementType;
		this.index= index;
		this.params= params;
		this.returnType= returnType;
		this.reference= reference;
	}
	
	/**
	 * @deprecated, use {@link #create(IIndex, IBinding)}.
	 */
	public IndexTypeInfo(String[] fqn, int elementType, IIndex index) {
		this(fqn, elementType, index, null, null, null);
	}

	/**
	 * @deprecated, use {@link #create(IIndex, IBinding)}.
	 */
	public IndexTypeInfo(String[] fqn, int elementType, String[] params, String returnType, IIndex index) {
		this(fqn, elementType, index, params, returnType, null);
	}
	
	public IndexTypeInfo(IndexTypeInfo rhs, ITypeReference ref) {
		this(rhs.fqn, rhs.elementType, rhs.index, rhs.params, rhs.returnType, ref);
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
			if (elementType == ICElement.C_MACRO) {
				return createMacroReference();
			}
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

	private ITypeReference createMacroReference() {
		try {
			index.acquireReadLock();

			IIndexMacro[] macros = index.findMacros(fqn[0].toCharArray(), IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			if(macros.length>0) {
				for (int i = 0; i < macros.length; i++) {
					reference= createReference(macros[i]);
					if (reference != null) {
						break;
					}
				}
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);				
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		} finally {
			index.releaseReadLock();
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

	private IndexTypeReference createReference(IIndexMacro macro) throws CoreException {
		IIndexFileLocation ifl = macro.getFile().getLocation();
		String fullPath = ifl.getFullPath();
		if (fullPath != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullPath));
			if(file!=null) {
				return new IndexTypeReference( 
						macro, file, file.getProject(), macro.getNodeOffset(), macro.getNodeLength()
				);
			}
		} else {
			IPath path = URIUtil.toPath(ifl.getURI());
			if(path!=null) {
				return new IndexTypeReference(
						macro, path, null, macro.getNodeOffset(), macro.getNodeLength()
				);
			}
		}
		return null;
	}

	public ITypeReference[] getReferences() {
		if (elementType == ICElement.C_MACRO) {
			return getMacroReferences();
		}
		
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
			// in case a file is represented multiple times in the index then we take references from
			// one of those, only.
			HashMap iflMap= new HashMap();
			for (int i = 0; i < ibs.length; i++) {
				IIndexBinding binding = ibs[i];
				IIndexName[] names;
				names= index.findNames(binding, IIndex.FIND_DEFINITIONS);
				if (names.length == 0) {
					names= index.findNames(binding, IIndex.FIND_DECLARATIONS);
				}
				for (int j = 0; j < names.length; j++) {
					IIndexName indexName = names[j];
					if (checkFile(iflMap, indexName.getFile())) {
						IndexTypeReference ref= createReference(binding, indexName);
						if (ref != null) {
							references.add(ref);
						}
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


	private ITypeReference[] getMacroReferences() {
		List references= new ArrayList();
		try {
			index.acquireReadLock();

			char[] cfn= fqn[0].toCharArray();
			IIndexMacro[] ibs = index.findMacros(cfn, IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			// in case a file is represented multiple times in the index then we take references from
			// one of those, only.
			HashMap iflMap= new HashMap();
			for (int i = 0; i < ibs.length; i++) {
				IIndexMacro macro= ibs[i];
				if (checkFile(iflMap, macro.getFile())) {
					IndexTypeReference ref= createReference(macro);
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

	/**
	 * Makes sure that per file only refs from one IIndexFile object are taken.
	 */
	private boolean checkFile(HashMap iflMap, IIndexFile file) throws CoreException {
		IIndexFileLocation ifl= file.getLocation();
		IIndexFile otherFile= (IIndexFile) iflMap.get(ifl);
		if (otherFile == null) {
			iflMap.put(ifl, file);
			return true;
		}
		return otherFile.equals(file);
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

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elementType;
		result = prime * result + IndexTypeInfo.hashCode(fqn);
		result = prime * result + IndexTypeInfo.hashCode(params);
		return result;
	}

	/**
	 * Type info objects are equal if they compute the same references.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexTypeInfo other = (IndexTypeInfo) obj;
		if (elementType != other.elementType)
			return false;
		if (!Arrays.equals(fqn, other.fqn))
			return false;
		if (!Arrays.equals(params, other.params))
			return false;
		return true;
	}
}
