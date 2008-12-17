/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
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
import org.eclipse.cdt.internal.core.browser.IndexModelUtil;
import org.eclipse.cdt.internal.core.browser.IndexTypeReference;
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
 * @noextend This class is not intended to be subclassed by clients.
 */
public class IndexTypeInfo implements ITypeInfo, IFunctionInfo {
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
	private final IIndexFileLocation fileLocal;
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
		IIndexFileLocation flsq= null;
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
			try {
				IIndexFile file= binding.getLocalToFile();
				if (file != null) {
					flsq= file.getLocation();
				}
			} catch (CoreException e) {
			}
			if (binding instanceof IFunction) {
				final IFunction function= (IFunction)binding;
				final String[] paramTypes= IndexModelUtil.extractParameterTypes(function);
				final String returnType= IndexModelUtil.extractReturnType(function);
				return new IndexTypeInfo(fqn, flsq, elementType, index, paramTypes, returnType, null);
			}
		} catch (DOMException e) {
			// index bindings don't throw DOMExceptions.
			throw new AssertionError();
		}

		return new IndexTypeInfo(fqn, flsq, elementType, index, null, null, null);
	}

	/**
	 * Creates a type info object suitable for a macro.
	 * @param index a non-null index in which to locate references
	 * @param macro a macro to create a type info for 
	 * @since 4.0.1
	 */
	public static IndexTypeInfo create(IIndex index, IIndexMacro macro) {
		final char[] name= macro.getNameCharArray();
		final char[][] ps= macro.getParameterList();
		String[] params= null;
		if (ps != null) {
			params= new String[ps.length];
			int i=-1;
			for (char[] p : ps) {
				params[++i]= new String(p);
			}
		}
		return new IndexTypeInfo(new String[] {new String(name)}, ICElement.C_MACRO, params, null, index);
	}

	private IndexTypeInfo(String[] fqn, IIndexFileLocation fileLocal, int elementType, IIndex index, String[] params, String returnType, ITypeReference reference) {
		Assert.isTrue(index != null);
		this.fqn= fqn;
		this.fileLocal= fileLocal;
		this.elementType= elementType;
		this.index= index;
		this.params= params;
		this.returnType= returnType;
		this.reference= reference;
	}
	
	/**
	 * @deprecated, use {@link #create(IIndex, IIndexBinding)}.
	 */
	public IndexTypeInfo(String[] fqn, int elementType, IIndex index) {
		this(fqn, null, elementType, index, null, null, null);
	}

	/**
	 * @deprecated, use {@link #create(IIndex, IIndexBinding)}.
	 */
	public IndexTypeInfo(String[] fqn, int elementType, String[] params, String returnType, IIndex index) {
		this(fqn, null, elementType, index, params, returnType, null);
	}
	
	public IndexTypeInfo(IndexTypeInfo rhs, ITypeReference ref) {
		this(rhs.fqn, rhs.fileLocal, rhs.elementType, rhs.index, rhs.params, rhs.returnType, ref);
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

				IIndexBinding[] ibs = findBindings();
				if(ibs.length>0) {
					IIndexName[] names;
					names= index.findNames(ibs[0], IIndex.FIND_DEFINITIONS);
					if (names.length == 0) {
						names= index.findNames(ibs[0], IIndex.FIND_DECLARATIONS);
					}
					for (IIndexName name : names) {
						reference= createReference(ibs[0], name);
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

	private IIndexBinding[] findBindings() throws CoreException {
		char[][] cfqn = new char[fqn.length][];
		for(int i=0; i<fqn.length; i++)
			cfqn[i] = fqn[i].toCharArray();

		IIndexBinding[] ibs = index.findBindings(cfqn, new IndexFilter() {
			@Override
			public boolean acceptBinding(IBinding binding) {
				if (!IndexModelUtil.bindingHasCElementType(binding, new int[]{elementType})) {
					return false;
				}
				try {
					if (fileLocal == null) {
						if (((IIndexBinding) binding).isFileLocal()) {
							return false;
						}
					}
					else {
						IIndexFile localToFile= ((IIndexBinding) binding).getLocalToFile();
						if (localToFile == null || !fileLocal.equals(localToFile.getLocation())) {
							return false;
						}
					}
					if (binding instanceof IFunction && params != null) {
						String[]otherParams= IndexModelUtil.extractParameterTypes((IFunction)binding);
						if (!Arrays.equals(params, otherParams)) {
							return false;
						}
					}
				} catch (CoreException e) {
					CCorePlugin.log(e);
				} catch (DOMException e) {
					CCorePlugin.log(e);
				}
				return true;
			}
		}, new NullProgressMonitor());
		return ibs;
	}

	private ITypeReference createMacroReference() {
		try {
			index.acquireReadLock();

			IIndexMacro[] macros = index.findMacros(fqn[0].toCharArray(), IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			if(macros.length>0) {
				for (IIndexMacro macro : macros) {
					reference= createReference(macro);
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
		IIndexName def= macro.getDefinition();
		if (def != null) {
			return createReference(macro, def);
		}
		return null;
	}

	public ITypeReference[] getReferences() {
		if (elementType == ICElement.C_MACRO) {
			return getMacroReferences();
		}
		
		List<IndexTypeReference> references= new ArrayList<IndexTypeReference>();
		try {
			index.acquireReadLock();
			IIndexBinding[] ibs= findBindings();
			HashMap<IIndexFileLocation, IIndexFile> iflMap= new HashMap<IIndexFileLocation, IIndexFile>();
			for (IIndexBinding binding : ibs) {
				IIndexName[] names;
				names= index.findNames(binding, IIndex.FIND_DEFINITIONS);
				if (names.length == 0) {
					names= index.findNames(binding, IIndex.FIND_DECLARATIONS);
				}
				for (IIndexName indexName : names) {
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
		return references.toArray(new IndexTypeReference[references.size()]);
	}


	private ITypeReference[] getMacroReferences() {
		List<IndexTypeReference> references= new ArrayList<IndexTypeReference>();
		try {
			index.acquireReadLock();

			char[] cfn= fqn[0].toCharArray();
			IIndexMacro[] ibs = index.findMacros(cfn, IndexFilter.ALL_DECLARED, new NullProgressMonitor());
			// in case a file is represented multiple times in the index then we take references from
			// one of those, only.
			HashMap<IIndexFileLocation, IIndexFile> iflMap= new HashMap<IIndexFileLocation, IIndexFile>();
			for (IIndexMacro macro : ibs) {
				if (checkParameters(macro.getParameterList())) {
					if (checkFile(iflMap, macro.getFile())) {
						IndexTypeReference ref= createReference(macro);
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
		return references.toArray(new IndexTypeReference[references.size()]);
	}

	private boolean checkParameters(char[][] parameterList) {
		if (parameterList == null) {
			return params == null;
		}
		if (params == null || parameterList.length != params.length) {
			return false;
		}
		for (int i = 0; i < parameterList.length; i++) {
			if (!params[i].equals(new String(parameterList[i]))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Makes sure that per file only refs from one IIndexFile object are taken.
	 */
	private boolean checkFile(HashMap<IIndexFileLocation, IIndexFile> iflMap, IIndexFile file) throws CoreException {
		IIndexFileLocation ifl= file.getLocation();
		IIndexFile otherFile= iflMap.get(ifl);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elementType;
		result = prime * result + ((fileLocal == null) ? 0 : fileLocal.hashCode());
		result = prime * result + IndexTypeInfo.hashCode(fqn);
		result = prime * result + IndexTypeInfo.hashCode(params);
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
		IndexTypeInfo other = (IndexTypeInfo) obj;
		if (elementType != other.elementType)
			return false;
		if (fileLocal == null) {
			if (other.fileLocal != null)
				return false;
		} else if (!fileLocal.equals(other.fileLocal))
			return false;
		if (!Arrays.equals(fqn, other.fqn))
			return false;
		if (!Arrays.equals(params, other.params))
			return false;
		return true;
	}
	
	/**
	 * @since 5.1
	 */
	public boolean isFileLocal() {
		return fileLocal != null;
	}
}
