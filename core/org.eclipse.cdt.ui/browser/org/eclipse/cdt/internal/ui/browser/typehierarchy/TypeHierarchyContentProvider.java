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
package org.eclipse.cdt.internal.ui.browser.typehierarchy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.browser.typehierarchy.ITypeHierarchy;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMember;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Base class for content providers for type hierarchy viewers.
 * Implementors must override 'getTypesInHierarchy'.
 * Java delta processing is also performed by the content provider
 */
public abstract class TypeHierarchyContentProvider implements ITreeContentProvider //, IWorkingCopyProvider {
{
	protected static final Object[] NO_ELEMENTS= new Object[0];
	
	protected TypeHierarchyLifeCycle fTypeHierarchy;
	protected IMember[] fMemberFilter;
	
	protected TreeViewer fViewer;

	private ViewerFilter fWorkingSetFilter;
	
	public TypeHierarchyContentProvider(TypeHierarchyLifeCycle lifecycle) {
		fTypeHierarchy= lifecycle;
		fMemberFilter= null;
		fWorkingSetFilter= null;
	}
	
	/**
	 * Sets members to filter the hierarchy for. Set to <code>null</code> to disable member filtering.
	 * When member filtering is enabled, the hierarchy contains only types that contain
	 * an implementation of one of the filter members and the members themself.
	 * The hierarchy can be empty as well.
	 */
	public final void setMemberFilter(IMember[] memberFilter) {
		fMemberFilter= memberFilter;
	}
	
	/**
	 * The members to filter or <code>null</code> if member filtering is disabled.
	 */
	public IMember[] getMemberFilter() {
		return fMemberFilter;
	}
	
	/**
	 * Sets a filter representing a working set or <code>null</code> if working sets are disabled.
	 */
	public void setWorkingSetFilter(ViewerFilter filter) {
		fWorkingSetFilter= filter;
	}
		
	
	protected final ITypeHierarchy getHierarchy() {
		return fTypeHierarchy.getHierarchy();
	}
	
	
	/* (non-Javadoc)
	 * @see IReconciled#providesWorkingCopies()
	 */
	public boolean providesWorkingCopies() {
		return true;
	}		
	
	
	/*
	 * Called for the root element
	 * @see IStructuredContentProvider#getElements	 
	 */
	public Object[] getElements(Object parent) {
		ArrayList types= new ArrayList();
		getRootTypes(types);
		for (int i= types.size() - 1; i >= 0; i--) {
		    ICElement curr= (ICElement) types.get(i);
			try {
				if (!isInTree(curr)) {
					types.remove(i);
				}
			} catch (CModelException e) {
				// ignore
			}
		}
		return types.toArray();
	}
	
	protected void getRootTypes(List res) {
		ITypeHierarchy hierarchy= getHierarchy();
		if (hierarchy != null) {
		    ICElement input= hierarchy.getType();
			if (input != null) {
				res.add(input);
			}
			// opened on a region: dont show
		}
	}
	
	/**
	 * Hook to overwrite. Filter will be applied on the returned types
	 */	
	protected abstract void getTypesInHierarchy(ICElement type, List res);
	
	/**
	 * Hook to overwrite. Return null if parent is ambiguous.
	 */	
	protected abstract ICElement[] getParentTypes(ICElement type);	
	
	
	private boolean isInScope(ICElement type) {
		if (fWorkingSetFilter != null && !fWorkingSetFilter.select(null, null, type)) {
			return false;
		}
		
//		ICElement input= fTypeHierarchy.getInputElement();
//		int inputType= input.getElementType();
//		if (inputType ==  ICElement.TYPE) {
//			return true;
//		}
//		
//		ICElement parent= type.getAncestor(input.getElementType());
//		if (inputType == ICElement.PACKAGE_FRAGMENT) {
//			if (parent == null || parent.getElementName().equals(input.getElementName())) {
//				return true;
//			}
//		} else if (input.equals(parent)) {
//			return true;
//		}
		return true;
	}
	
	/*
	 * Called for the tree children.
	 * @see ITreeContentProvider#getChildren
	 */	
	public Object[] getChildren(Object element) {
		if (element instanceof ICElement) {
			try {
			    ICElement type= (ICElement)element;
	
				List children= new ArrayList();
				if (fMemberFilter != null) {
					addFilteredMemberChildren(type, children);
				}
	
				addTypeChildren(type, children);
				
				return children.toArray();
			} catch (CModelException e) {
				// ignore
			}
		}
		return NO_ELEMENTS;
	}
	
	/*
	 * @see ITreeContentProvider#hasChildren
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof ICElement) {
			try {
			    ICElement type= (ICElement) element;
				return hasTypeChildren(type) || (fMemberFilter != null && hasMemberFilterChildren(type));
			} catch (CModelException e) {
				return false;
			}			
		}
		return false;
	}	
	
	private void addFilteredMemberChildren(ICElement parent, List children) throws CModelException {
		IMethodDeclaration[] methods= TypeUtil.getMethods(parent);
		if (methods != null && methods.length > 0) {
			for (int i= 0; i < fMemberFilter.length; i++) {
				IMember member= fMemberFilter[i];
				if (parent.equals(TypeUtil.getDeclaringClass(member))) {
					if (!children.contains(member)) {
						children.add(member);
					}
				} else if (member instanceof IMethodDeclaration) {
				    IMethodDeclaration curr= (IMethodDeclaration)member;
				    IMethodDeclaration meth= TypeUtil.findMethod(curr.getElementName(), curr.getParameterTypes(), curr.isConstructor(), curr.isDestructor(), methods);
					if (meth != null && !children.contains(meth)) {
						children.add(meth);
					}
				}
			}
		}		
	}
		
	private void addTypeChildren(ICElement type, List children) throws CModelException {
		ArrayList types= new ArrayList();
		getTypesInHierarchy(type, types);
		int len= types.size();
		for (int i= 0; i < len; i++) {
		    ICElement curr= (ICElement) types.get(i);
			if (isInTree(curr)) {
				children.add(curr);
			}
		}
	}
	
	protected final boolean isInTree(ICElement type) throws CModelException {
		if (isInScope(type)) {
			if (fMemberFilter != null) {
				return hasMemberFilterChildren(type) || hasTypeChildren(type);
			}
			return true;
		}
		return hasTypeChildren(type);
	}
	
	private boolean hasMemberFilterChildren(ICElement type) throws CModelException {
	    IMethodDeclaration[] methods= TypeUtil.getMethods(type);
	    if (methods != null && methods.length > 0) {
			for (int i= 0; i < fMemberFilter.length; i++) {
				IMember member= fMemberFilter[i];
				if (type.equals(TypeUtil.getDeclaringClass(member))) {
					return true;
				} else if (member instanceof IMethodDeclaration) {
				    IMethodDeclaration curr= (IMethodDeclaration)member;
				    IMethodDeclaration meth= TypeUtil.findMethod(curr.getElementName(), curr.getParameterTypes(), curr.isConstructor(), curr.isDestructor(), methods);
					if (meth != null) {
						return true;
					}
				}
			}
	    }
		return false;
	}
	
	
	private boolean hasTypeChildren(ICElement type) throws CModelException {
		ArrayList types= new ArrayList();
		getTypesInHierarchy(type, types);
		int len= types.size();
		for (int i= 0; i < len; i++) {
		    ICElement curr= (ICElement) types.get(i);
			if (isInTree(curr)) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * @see IContentProvider#inputChanged
	 */
	public void inputChanged(Viewer part, Object oldInput, Object newInput) {
		Assert.isTrue(part instanceof TreeViewer);
		fViewer= (TreeViewer)part;
	}
	
	/*
	 * @see IContentProvider#dispose
	 */	
	public void dispose() {
	}
	
	/*
	 * @see ITreeContentProvider#getParent
	 */
	public Object getParent(Object element) {
		if (element instanceof IMember) {
			IMember member= (IMember) element;
//			if (member.getElementType() == ICElement.TYPE) {
			if (TypeUtil.isClassOrStruct(member)) {
				ICElement[] parents= getParentTypes(member);
				if (parents != null && parents.length == 1)
				    return parents[0];
			}
			return TypeUtil.getDeclaringClass(member);
		}
		return null;
	}
}
