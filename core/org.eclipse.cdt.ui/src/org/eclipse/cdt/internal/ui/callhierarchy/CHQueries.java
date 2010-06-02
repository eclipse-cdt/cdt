/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;

import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
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
		if (!(callee instanceof ISourceReference)) {
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
			findCalledBy1(index, calleeBinding, true, project, result);
			if (calleeBinding instanceof ICPPMethod) {
				try {
					IBinding[] overriddenBindings= ClassTypeHelper.findOverridden((ICPPMethod) calleeBinding);
					for (IBinding overriddenBinding : overriddenBindings) {
						findCalledBy1(index, overriddenBinding, false, project, result);
					}
				} catch (DOMException e) {
					// index bindings don't throw DOMExceptions
				}
			}
		}
	}

	private static void findCalledBy1(IIndex index, IBinding callee, boolean includeOrdinaryCalls,
			ICProject project, CalledByResult result) throws CoreException {
		findCalledBy2(index, callee, includeOrdinaryCalls, project, result);
		List<? extends IBinding> specializations = IndexUI.findSpecializations(callee);
		for (IBinding spec : specializations) {
			findCalledBy2(index, spec, includeOrdinaryCalls, project, result);
		}
	}


	private static void findCalledBy2(IIndex index, IBinding callee, boolean includeOrdinaryCalls, ICProject project, CalledByResult result) 
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
					ICElement[] defs= null;
					if (binding instanceof ICPPMethod) {
						defs = findOverriders(index, (ICPPMethod) binding);
					}
					if (defs == null) {
						defs= IndexUI.findRepresentative(index, binding);
					}
					if (defs != null && defs.length > 0) {
						result.add(defs, name);
					}
				}
			}
		}
		return cp.createNodes(node, result);
	}

	/**
	 * Searches for overriders of method and converts them to ICElement, returns null, if there are none.
	 */
	static ICElement[] findOverriders(IIndex index, ICPPMethod binding)	throws CoreException {
		try {
			IBinding[] virtualOverriders= ClassTypeHelper.findOverriders(index, binding);
			if (virtualOverriders.length > 0) {
				ArrayList<ICElementHandle> list= new ArrayList<ICElementHandle>();
				list.addAll(Arrays.asList(IndexUI.findRepresentative(index, binding)));
				for (IBinding overrider : virtualOverriders) {
					list.addAll(Arrays.asList(IndexUI.findRepresentative(index, overrider)));
				}
				return list.toArray(new ICElement[list.size()]);
			}
		} catch (DOMException e) {
			// index bindings don't throw DOMExceptions
		}
		return null;
	}
}
