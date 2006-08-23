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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.corext.util.CModelUtil;

import org.eclipse.cdt.internal.ui.missingapi.CElementSet;
import org.eclipse.cdt.internal.ui.missingapi.CIndexQueries;
import org.eclipse.cdt.internal.ui.missingapi.CIndexReference;
import org.eclipse.cdt.internal.ui.missingapi.CalledByResult;
import org.eclipse.cdt.internal.ui.missingapi.CallsToResult;
import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeContentProvider;

/** 
 * This is the content provider for the call hierarchy.
 */
public class CHContentProvider extends AsyncTreeContentProvider {

	private static final IProgressMonitor NPM = new NullProgressMonitor();
	private boolean fComputeReferencedBy = true;

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
		if (parentElement instanceof CHNode) {
			CHNode node = (CHNode) parentElement;
			if (node.isRecursive() || node.getRepresentedDeclaration() == null) {
				return NO_CHILDREN;
			}
			if (!fComputeReferencedBy && (node.isVariable() || node.isMacro())) { 
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
			CalledByResult calledBy= CIndexQueries.getInstance().findCalledBy(elem, NPM);
			ArrayList result= new ArrayList();
			
			ICElement[] elements= calledBy.getElements();
			for (int i = 0; i < elements.length; i++) {
				ICElement element = elements[i];
				CIndexReference[] refs= calledBy.getReferences(element);
				if (refs != null && refs.length > 0) {
					CHNode node = createRefbyNode(parent, element, refs);
					result.add(node);
				}
			}
			return result.toArray();
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (InterruptedException e) {
		}
		return NO_CHILDREN;
	}

	private CHNode createRefbyNode(CHNode parent, ICElement element, CIndexReference[] refs) {
		ITranslationUnit tu= CModelUtil.getTranslationUnit(element);
		CHNode node= new CHNode(parent, tu, refs[0].getTimestamp(), element);
		for (int i = 0; i < refs.length; i++) {
			CIndexReference reference = refs[i];
			node.addReference(new CHReferenceInfo(reference.getOffset(), reference.getLength()));
		}
		return node;
	}

	private CHNode createReftoNode(CHNode parent, ITranslationUnit tu, ICElement[] elements, CIndexReference[] references) {
		CIndexReference firstRef= references[0];
		CHNode node= new CHNode(parent, tu, firstRef.getTimestamp(), elements[0]);
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
				CallsToResult callsTo= CIndexQueries.getInstance().findCallsToInRange(tu, new Region(range.getStartPos(), range.getLength()), NPM);
				ArrayList result= new ArrayList();
				CElementSet[] elementSets= callsTo.getElementSets();
				for (int i = 0; i < elementSets.length; i++) {
					CElementSet set = elementSets[i];
					if (!set.isEmpty()) {
						CIndexReference[] refs= callsTo.getReferences(set);
						CHNode node = createReftoNode(parent, tu, set.getElements(), refs);
						result.add(node);
					}
				}
				return result.toArray();
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (InterruptedException e) {
		}
		return NO_CHILDREN;
	}

	public void setComputeReferencedBy(boolean value) {
		fComputeReferencedBy = value;
	}

	public boolean getComputeReferencedBy() {
		return fComputeReferencedBy;
	}
}
