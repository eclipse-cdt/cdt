/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeContentProvider;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.cdt.internal.ui.viewsupport.WorkingSetFilterUI;

/** 
 * This is the content provider for the call hierarchy.
 */
public class CHContentProvider extends AsyncTreeContentProvider {

	private static final IProgressMonitor NPM = new NullProgressMonitor();
	private boolean fComputeReferencedBy = true;
	private WorkingSetFilterUI fFilter;
	private CHViewPart fView;

	/**
	 * Constructs the content provider.
	 */
	public CHContentProvider(CHViewPart view, Display disp) {
		super(disp);
		fView= view;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof CHNode) {
			CHNode node = (CHNode) element;
			return node.getParent();
		}
		return super.getParent(element);
	}

	@Override
	protected Object[] syncronouslyComputeChildren(Object parentElement) {
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
			else if (node.isVariableOrEnumerator() || node.isMacro()) { 
				return NO_CHILDREN;
			}
			
		}
		// allow for async computation
		return null;
	}

	@Override
	protected Object[] asyncronouslyComputeChildren(Object parentElement, IProgressMonitor monitor) {
		try {
			if (parentElement instanceof ICElement) {
				return asyncComputeRoot((ICElement) parentElement);
			}

			if (parentElement instanceof CHNode) {
				CHNode node = (CHNode) parentElement;
				if (fComputeReferencedBy) {
					return asyncronouslyComputeReferencedBy(node);
				}
				return asyncronouslyComputeRefersTo(node);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return NO_CHILDREN;
	}
	
	private Object[] asyncComputeRoot(final ICElement input) throws CoreException, InterruptedException {
		IIndex index= CCorePlugin.getIndexManager().getIndex(input.getCProject());
		index.acquireReadLock();
		try {
			ICElement element= input;
			if (!IndexUI.isIndexed(index, input)) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						fView.reportNotIndexed(input);
					}
				});
			} 
			else {
				element= IndexUI.attemptConvertionToHandle(index, input);
				final ICElement finalElement= element;
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						fView.reportInputReplacement(input, finalElement);
					}
				});
			}
			ITranslationUnit tu= CModelUtil.getTranslationUnit(element);
			if (!fComputeReferencedBy && element instanceof IMethod) {
				IIndexName methodName= IndexUI.elementToName(index, element);
				if (methodName != null) {
					IBinding methodBinding= index.findBinding(methodName);
					if (methodBinding instanceof ICPPMethod) {
						ICElement[] defs= CHQueries.findOverriders(index, (ICPPMethod) methodBinding);
						if (defs != null && defs.length > 0) {
							return new Object[] { new CHMultiDefNode(null, tu, 0, defs, methodBinding.getLinkage().getLinkageID()) };
						}
					}
				}
			}
			return new Object[] { new CHNode(null, tu, 0, element, -1) };
		}
		finally {
			index.releaseReadLock();
		}
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
		ArrayList<CHNode> nodes= new ArrayList<CHNode>();
		ICElement[] elements= result.getElements();
		for (ICElement element : elements) {
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
		return nodes.toArray(new CHNode[nodes.size()]);
	}
	
	private CHNode createRefbyNode(CHNode parent, ICElement element, IIndexName[] refs) throws CoreException {
		ITranslationUnit tu= CModelUtil.getTranslationUnit(element);
		final IIndexFile file = refs[0].getFile();
		CHNode node= new CHNode(parent, tu, file.getTimestamp(), element, file.getLinkageID());
		if (element instanceof IVariable || element instanceof IEnumerator) {
			node.setInitializer(true);
		}
		boolean readAccess= false;
		boolean writeAccess= false;
		for (IIndexName reference : refs) {
			node.addReference(new CHReferenceInfo(reference.getNodeOffset(), reference.getNodeLength()));
			readAccess= (readAccess || reference.isReadAccess());
			writeAccess= (writeAccess || reference.isWriteAccess());
		}
		node.setRWAccess(readAccess, writeAccess);
		node.sortReferencesByOffset();
		return node;
	}

	CHNode[] createNodes(CHNode node, CallsToResult callsTo) throws CoreException {
		ITranslationUnit tu= CModelUtil.getTranslationUnit(node.getRepresentedDeclaration());
		ArrayList<CHNode> result= new ArrayList<CHNode>();
		CElementSet[] elementSets= callsTo.getElementSets();
		for (CElementSet elementSet : elementSets) {
			CElementSet set = elementSet;
			if (!set.isEmpty()) {
				IIndexName[] refs= callsTo.getReferences(set);
				ICElement[] elements= set.getElements(fFilter);
				if (elements.length > 0) {
					CHNode childNode = createReftoNode(node, tu, elements, refs);
					result.add(childNode);
				}
			}
		}
		return result.toArray(new CHNode[result.size()]);
	}

	private CHNode createReftoNode(CHNode parent, ITranslationUnit tu, ICElement[] elements, IIndexName[] references) throws CoreException {
		assert elements.length > 0;

		final IIndexFile file = references[0].getFile();
		final long timestamp= file.getTimestamp();
		final int linkageID= file.getLinkageID();
		
		CHNode node;
		if (elements.length == 1) {
			node= new CHNode(parent, tu, timestamp, elements[0], linkageID);
		} else {
			node= new CHMultiDefNode(parent, tu, timestamp, elements, linkageID);
		}
		
		boolean readAccess= false;
		boolean writeAccess= false;
		for (IIndexName reference : references) {
			node.addReference(new CHReferenceInfo(reference.getNodeOffset(), reference.getNodeLength()));
			readAccess= (readAccess || reference.isReadAccess());
			writeAccess= (writeAccess || reference.isWriteAccess());
		}
		node.sortReferencesByOffset();
		node.setRWAccess(readAccess, writeAccess);
		return node;
	}
}
