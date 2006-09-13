/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.missingapi.CElementSet;
import org.eclipse.cdt.internal.ui.missingapi.CIndexQueries;
import org.eclipse.cdt.internal.ui.missingapi.CIndexReference;
import org.eclipse.cdt.internal.ui.missingapi.CalledByResult;
import org.eclipse.cdt.internal.ui.missingapi.CallsToResult;
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
			ICElement elem= node.getRepresentedDeclaration();
			if (elem != null) {
				// mstodo !! for demo only !!
				if (elem.getElementName().equals("slow")) { //$NON-NLS-1$
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// mstodo !! for demo only !!
				if (fComputeReferencedBy) {
					return asyncronouslyComputeReferencedBy(node, elem);
				}
				else {
					return asyncronouslyComputeRefersTo(node, elem);
				}
			}
		}
		return NO_CHILDREN;
	}
	
	private Object[] asyncronouslyComputeReferencedBy(CHNode parent, ICElement elem) {
		try {
			ICProject[] scope= CoreModel.getDefault().getCModel().getCProjects();
			CalledByResult calledBy= CIndexQueries.getInstance().findCalledBy(scope, elem, NPM);
			ArrayList result= new ArrayList();
			
			ICElement[] elements= calledBy.getElements();
			for (int i = 0; i < elements.length; i++) {
				ICElement element = elements[i];
				if (element != null) {
					if (fFilter == null || fFilter.isPartOfWorkingSet(element)) {
						CIndexReference[] refs= calledBy.getReferences(element);
						if (refs != null && refs.length > 0) {
							CHNode node = createRefbyNode(parent, element, refs);
							result.add(node);
						}
					}
				}
			}
			return result.toArray();
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		return NO_CHILDREN;
	}

	private CHNode createRefbyNode(CHNode parent, ICElement element, CIndexReference[] refs) {
		ITranslationUnit tu= CModelUtil.getTranslationUnit(element);
		CHNode node= new CHNode(parent, tu, refs[0].getTimestamp(), element);
		if (element instanceof IVariable || element instanceof IEnumerator) {
			node.setInitializer(true);
		}
		Arrays.sort(refs, CIndexReference.COMPARE_OFFSET);
		for (int i = 0; i < refs.length; i++) {
			CIndexReference reference = refs[i];
			node.addReference(new CHReferenceInfo(reference.getOffset(), reference.getLength()));
		}
		return node;
	}

	private CHNode createReftoNode(CHNode parent, ITranslationUnit tu, ICElement[] elements, CIndexReference[] references) {
		assert elements.length > 0;

		CHNode node;
		long timestamp= references[0].getTimestamp();
		
		if (elements.length == 1) {
			node= new CHNode(parent, tu, timestamp, elements[0]);
		}
		else {
			node= new CHMultiDefNode(parent, tu, timestamp, elements);
		}
		
		Arrays.sort(references, CIndexReference.COMPARE_OFFSET);		
		for (int i = 0; i < references.length; i++) {
			CIndexReference reference = references[i];
			node.addReference(new CHReferenceInfo(reference.getOffset(), reference.getLength()));
		}
		return node;
	}

	private Object[] asyncronouslyComputeRefersTo(CHNode parent, ICElement elem) {
		try {
			if (elem instanceof ISourceReference) {
				ISourceReference sf= (ISourceReference) elem;
				ITranslationUnit tu= sf.getTranslationUnit();
				ISourceRange range= sf.getSourceRange();
				ICProject[] scope= CoreModel.getDefault().getCModel().getCProjects();
				CallsToResult callsTo= CIndexQueries.getInstance().findCallsInRange(scope, tu, new Region(range.getStartPos(), range.getLength()), NPM);
				ArrayList result= new ArrayList();
				CElementSet[] elementSets= callsTo.getElementSets();
				for (int i = 0; i < elementSets.length; i++) {
					CElementSet set = elementSets[i];
					if (!set.isEmpty()) {
						CIndexReference[] refs= callsTo.getReferences(set);
						ICElement[] elements= set.getElements(fFilter);
						if (elements.length > 0) {
							CHNode node = createReftoNode(parent, tu, elements, refs);
							result.add(node);
						}
					}
				}
				return result.toArray();
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
		return NO_CHILDREN;
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
}
