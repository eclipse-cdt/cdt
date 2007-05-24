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

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeContentProvider;
import org.eclipse.cdt.internal.ui.viewsupport.WorkingSetFilterUI;

/** 
 * This is the content provider for the call hierarchy.
 */
public class CHContentProvider extends AsyncTreeContentProvider {

	private static final IProgressMonitor NPM = new NullProgressMonitor();
	private boolean fComputeReferencedBy = true;
	private WorkingSetFilterUI fFilter;

	/**
	 * Constructs the content provider.
	 */
	public CHContentProvider(Display disp) {
		super(disp);
	}

	public Object getParent(Object element) {
		if (element instanceof CHNode) {
			CHNode node = (CHNode) element;
			return node.getParent();
		}
		return super.getParent(element);
	}

	protected Object[] syncronouslyComputeChildren(Object parentElement) {
		if (parentElement instanceof ICElement) {
			ICElement element = (ICElement) parentElement;
			ITranslationUnit tu= CModelUtil.getTranslationUnit(element);
			return new Object[] { new CHNode(null, tu, 0, element) };
		}
		if (parentElement instanceof CHMultiDefNode) {
			return ((CHMultiDefNode) parentElement).getChildNodes();
		}
		if (parentElement instanceof CHNode) {
			CHNode node = (CHNode) parentElement;
			if (node.isRecursive() || node.getRepresentedDeclaration() == null) {
				return NO_CHILDREN;
			}
			if (fComputeReferencedBy) {
				if (node.isInitializer()) {
					return NO_CHILDREN;
				}
			}
			else if (node.isVariable() || node.isMacro()) { 
				return NO_CHILDREN;
			}
			
		}
		// allow for async computation
		return null;
	}

	protected Object[] asyncronouslyComputeChildren(Object parentElement,
			IProgressMonitor monitor) {
		if (parentElement instanceof CHNode) {
			CHNode node = (CHNode) parentElement;
			try {
				if (fComputeReferencedBy) {
					return asyncronouslyComputeReferencedBy(node);
				}
				else {
					return asyncronouslyComputeRefersTo(node);
				}
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			} catch (InterruptedException e) {
			}
		}
		return NO_CHILDREN;
	}
	
	private Object[] asyncronouslyComputeReferencedBy(CHNode parent) throws CoreException, InterruptedException {
		ICProject[] scope= CoreModel.getDefault().getCModel().getCProjects();
		IIndex index= CCorePlugin.getIndexManager().getIndex(scope);
		index.acquireReadLock();
		try {
			return CHQueries.findCalledBy(this, parent, index, NPM);
		}
		finally {
			index.releaseReadLock();
		}
	}

	private Object[] asyncronouslyComputeRefersTo(CHNode parent) throws CoreException, InterruptedException {
		ICProject[] scope= CoreModel.getDefault().getCModel().getCProjects();
		IIndex index= CCorePlugin.getIndexManager().getIndex(scope);
		index.acquireReadLock();
		try {
			return CHQueries.findCalls(this, parent, index, NPM);
		}
		finally {
			index.releaseReadLock();
		}
	}

	public void setComputeReferencedBy(boolean value) {
		fComputeReferencedBy = value;
	}

	public boolean getComputeReferencedBy() {
		return fComputeReferencedBy;
	}

	public void setWorkingSetFilter(WorkingSetFilterUI filterUI) {
		fFilter= filterUI;
		recompute();
	}

	CHNode[] createNodes(CHNode node, CalledByResult result) throws CoreException {
		ArrayList nodes= new ArrayList();
		ICElement[] elements= result.getElements();
		for (int i = 0; i < elements.length; i++) {
			ICElement element = elements[i];
			if (element != null) {
				if (fFilter == null || fFilter.isPartOfWorkingSet(element)) {
					IIndexName[] refs= result.getReferences(element);
					if (refs != null && refs.length > 0) {
						CHNode newNode = createRefbyNode(node, element, refs);
						nodes.add(newNode);
					}
				}
			}
		}
		return (CHNode[]) nodes.toArray(new CHNode[nodes.size()]);
	}
	
	private CHNode createRefbyNode(CHNode parent, ICElement element, IIndexName[] refs) throws CoreException {
		ITranslationUnit tu= CModelUtil.getTranslationUnit(element);
		CHNode node= new CHNode(parent, tu, refs[0].getFile().getTimestamp(), element);
		if (element instanceof IVariable || element instanceof IEnumerator) {
			node.setInitializer(true);
		}
		for (int i = 0; i < refs.length; i++) {
			IIndexName reference = refs[i];
			node.addReference(new CHReferenceInfo(reference.getNodeOffset(), reference.getNodeLength()));
		}
		node.sortReferencesByOffset();
		return node;
	}

	CHNode[] createNodes(CHNode node, CallsToResult callsTo) throws CoreException {
		ITranslationUnit tu= CModelUtil.getTranslationUnit(node.getRepresentedDeclaration());
		ArrayList result= new ArrayList();
		CElementSet[] elementSets= callsTo.getElementSets();
		for (int i = 0; i < elementSets.length; i++) {
			CElementSet set = elementSets[i];
			if (!set.isEmpty()) {
				IIndexName[] refs= callsTo.getReferences(set);
				ICElement[] elements= set.getElements(fFilter);
				if (elements.length > 0) {
					CHNode childNode = createReftoNode(node, tu, elements, refs);
					result.add(childNode);
				}
			}
		}
		return (CHNode[]) result.toArray(new CHNode[result.size()]);
	}

	private CHNode createReftoNode(CHNode parent, ITranslationUnit tu, ICElement[] elements, IIndexName[] references) throws CoreException {
		assert elements.length > 0;

		CHNode node;
		long timestamp= references[0].getFile().getTimestamp();
		
		if (elements.length == 1) {
			node= new CHNode(parent, tu, timestamp, elements[0]);
		}
		else {
			node= new CHMultiDefNode(parent, tu, timestamp, elements);
		}
		
		for (int i = 0; i < references.length; i++) {
			IIndexName reference = references[i];
			node.addReference(new CHReferenceInfo(reference.getNodeOffset(), reference.getNodeLength()));
		}
		node.sortReferencesByOffset();
		return node;
	}
}
