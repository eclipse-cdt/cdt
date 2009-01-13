/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;


/**
 * Access to high level queries in the index.
 * @since 4.0
 */
public class CHQueries {
	private static final CHNode[] EMPTY_NODES= new CHNode[0];
	
    private CHQueries() {}
    
	/**
	 * Searches for functions and methods that call a given element.
	 */
	public static CHNode[] findCalledBy(CHContentProvider cp, CHNode node, IIndex index, IProgressMonitor pm) 
			throws CoreException {
		CalledByResult result= new CalledByResult();
		ICElement callee= node.getRepresentedDeclaration();
		if (! (callee instanceof ISourceReference)) {
			return EMPTY_NODES;
		}
		boolean done= false;
		int linkageID= node.getLinkageID();
		if (linkageID == -1) {
			final String ct = ((ISourceReference) callee).getTranslationUnit().getContentTypeId();
			if (ct.equals(CCorePlugin.CONTENT_TYPE_CXXHEADER)) {
				// bug 260262: in a header file we need to consider c and c++
				findCalledBy(callee, ILinkage.C_LINKAGE_ID, index, result);
				findCalledBy(callee, ILinkage.CPP_LINKAGE_ID, index, result);
				done= true;
			}
		}
		if (!done) {
			findCalledBy(callee, linkageID, index, result);
		}
		return cp.createNodes(node, result);
	}

	private static void findCalledBy(ICElement callee, int linkageID, IIndex index, CalledByResult result) 
			throws CoreException {
		final ICProject project = callee.getCProject();
		IIndexBinding calleeBinding= IndexUI.elementToBinding(index, callee, linkageID);
		if (calleeBinding != null) {
			findCalledBy(index, calleeBinding, true, project, result);
			IBinding[] overriddenBindings= getOverriddenBindings(index, calleeBinding);
			for (IBinding overriddenBinding : overriddenBindings) {
				findCalledBy(index, overriddenBinding, false, project, result);
			}
		}
	}

	private static IBinding[] getOverriddenBindings(IIndex index, IIndexBinding binding) {
		if (binding instanceof ICPPMethod && !(binding instanceof ICPPConstructor)) {
			try {
				final ArrayList<ICPPMethod> result= new ArrayList<ICPPMethod>();
				final ICPPMethod m= (ICPPMethod) binding;
				final char[] mname= m.getNameCharArray();
				final ICPPClassType mcl= m.getClassOwner();
				if (mcl != null) {
					final IFunctionType mft= m.getType();
					boolean isVirtual= m.isVirtual();
					ICPPMethod[] allMethods= mcl.getMethods();
					for (ICPPMethod method : allMethods) {
						if (CharArrayUtils.equals(mname, method.getNameCharArray()) && !mcl.isSameType(method.getClassOwner())) {
							if (mft.isSameType(method.getType())) {
								isVirtual= isVirtual || method.isVirtual();
								result.add(method);
							}
						}
					}
					if (isVirtual) {
						return result.toArray(new IBinding[result.size()]);
					}
				}
			} catch (DOMException e) {
				// index bindings don't throw DOMExceptions
			}
		}
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	private static IBinding[] getOverridingBindings(IIndex index, IBinding binding) throws CoreException {
		if (binding instanceof ICPPMethod && !(binding instanceof ICPPConstructor)) {
			try {
				final ICPPMethod m= (ICPPMethod) binding;
				if (isVirtual(m)) {
					final ArrayList<ICPPMethod> result= new ArrayList<ICPPMethod>();
					final char[] mname= m.getNameCharArray();
					final ICPPClassType mcl= m.getClassOwner();
					if (mcl != null) {
						final IFunctionType mft= m.getType();
						ICPPClassType[] subclasses= getSubClasses(index, mcl);
						for (ICPPClassType subClass : subclasses) {
							ICPPMethod[] methods= subClass.getDeclaredMethods();
							for (ICPPMethod method : methods) {
								if (CharArrayUtils.equals(mname, method.getNameCharArray()) &&
										mft.isSameType(method.getType())) {
									result.add(method);
								}
							}
						}
						return result.toArray(new IBinding[result.size()]);
					}
				}
			} catch (DOMException e) {
				// index bindings don't throw DOMExceptions
			}
		}
		return IBinding.EMPTY_BINDING_ARRAY;
	}

	private static ICPPClassType[] getSubClasses(IIndex index, ICPPClassType mcl) throws CoreException {
		List<ICPPBinding> result= new LinkedList<ICPPBinding>();
		HashSet<String> handled= new HashSet<String>();
		getSubClasses(index, mcl, result, handled);
		result.remove(0);
		return result.toArray(new ICPPClassType[result.size()]);
	}

	private static void getSubClasses(IIndex index, ICPPBinding classOrTypedef, List<ICPPBinding> result, HashSet<String> handled) throws CoreException {
		try {
			final String key = CPPVisitor.renderQualifiedName(classOrTypedef.getQualifiedName());
			if (!handled.add(key)) {
				return;
			}
		} catch (DOMException e) {
			return;
		}

		if (classOrTypedef instanceof ICPPClassType) {
			result.add(classOrTypedef);
		}

		IIndexName[] names= index.findNames(classOrTypedef, IIndex.FIND_REFERENCES | IIndex.FIND_DEFINITIONS);
		for (IIndexName indexName : names) {
			if (indexName.isBaseSpecifier()) {
				IIndexName subClassDef= indexName.getEnclosingDefinition();
				if (subClassDef != null) {
					IBinding subClass= index.findBinding(subClassDef);
					if (subClass instanceof ICPPBinding) {
						getSubClasses(index, (ICPPBinding) subClass, result, handled);
					}
				}
			}
		}
	}

	private static boolean isVirtual(ICPPMethod m) {
		try {
			if (m.isVirtual()) {
				return true;
			}
			final char[] mname= m.getNameCharArray();
			final ICPPClassType mcl= m.getClassOwner();
			if (mcl != null) {
				final IFunctionType mft= m.getType();
				ICPPMethod[] allMethods= mcl.getMethods();
				for (ICPPMethod method : allMethods) {
					if (CharArrayUtils.equals(mname, method.getNameCharArray()) && mft.isSameType(method.getType())) {
						if (method.isVirtual()) {
							return true;
						}
					}
				}
			}
		} catch (DOMException e) {
			// index bindings don't throw DOMExceptions
		}
		return false;
	}

	private static void findCalledBy(IIndex index, IBinding callee, boolean includeOrdinaryCalls, ICProject project, CalledByResult result) 
			throws CoreException {
		IIndexName[] names= index.findNames(callee, IIndex.FIND_REFERENCES | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		for (IIndexName rname : names) {
			if (includeOrdinaryCalls || rname.couldBePolymorphicMethodCall()) {
				IIndexName caller= rname.getEnclosingDefinition();
				if (caller != null) {
					ICElement elem= IndexUI.getCElementForName(project, index, caller);
					if (elem != null) {
						result.add(elem, rname);
					} 
				}
			}
		}
	}

	/**
	 * Searches for all calls that are made within a given range.
	 */
	public static CHNode[] findCalls(CHContentProvider cp, CHNode node, IIndex index, IProgressMonitor pm) 
			throws CoreException {
		ICElement caller= node.getRepresentedDeclaration();
		CallsToResult result= new CallsToResult();
		IIndexName callerName= IndexUI.elementToName(index, caller);
		if (callerName != null) {
			IIndexName[] refs= callerName.getEnclosedNames();
			for (IIndexName name : refs) {
				IBinding binding= index.findBinding(name);
				if (CallHierarchyUI.isRelevantForCallHierarchy(binding)) {
					IBinding[] virtualOverriders= getOverridingBindings(index, binding);
					ICElement[] defs;
					if (virtualOverriders.length == 0) {
						defs = IndexUI.findRepresentative(index, binding);
					}
					else {
						ArrayList<ICElementHandle> list= new ArrayList<ICElementHandle>();
						list.addAll(Arrays.asList(IndexUI.findRepresentative(index, binding)));
						for (IBinding overrider : virtualOverriders) {
							list.addAll(Arrays.asList(IndexUI.findRepresentative(index, overrider)));
						}
						defs= list.toArray(new ICElement[list.size()]);
					}
					if (defs != null && defs.length > 0) {
						result.add(defs, name);
					}
				}
			}
		}
		return cp.createNodes(node, result);
	}
}
