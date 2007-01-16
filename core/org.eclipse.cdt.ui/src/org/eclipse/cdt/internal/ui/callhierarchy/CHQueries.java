/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.callhierarchy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceReference;

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
	 * @throws DOMException 
	 */
	public static CHNode[] findCalledBy(CHContentProvider cp, CHNode node, 
			IIndex index, IProgressMonitor pm) throws CoreException, DOMException {
		CalledByResult result= new CalledByResult();
		ICElement callee= node.getRepresentedDeclaration();
		if (! (callee instanceof ISourceReference)) {
			return EMPTY_NODES;
		}
		IBinding calleeBinding= IndexUI.elementToBinding(index, callee);
		findCalledBy(index, calleeBinding, callee.getCProject(), result);	
		
		return cp.createNodes(node, result);
	}

	private static void findCalledBy(IIndex index, IBinding callee, ICProject project, CalledByResult result) throws CoreException, DOMException {
		if (callee != null) {
			IIndexName[] names= index.findReferences(callee);
			for (int i = 0; i < names.length; i++) {
				IIndexName rname = names[i];
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
	 * @throws DOMException 
	 */
	public static CHNode[] findCalls(CHContentProvider cp, CHNode node, IIndex index, IProgressMonitor pm) throws CoreException, DOMException {
		ICElement caller= node.getRepresentedDeclaration();
		CallsToResult result= new CallsToResult();
		IIndexName callerName= IndexUI.elementToName(index, caller);
		if (callerName != null) {
			IIndexName[] refs= callerName.getEnclosedNames();
			for (int i = 0; i < refs.length; i++) {
				IIndexName name = refs[i];
				IBinding binding= index.findBinding(name);
				if (CallHierarchyUI.isRelevantForCallHierarchy(binding)) {
					ICElement[] defs = IndexUI.findRepresentative(index, binding);
					if (defs != null && defs.length > 0) {
						result.add(defs, name);
					}
				}
			}
		}
		return cp.createNodes(node, result);
	}
}
