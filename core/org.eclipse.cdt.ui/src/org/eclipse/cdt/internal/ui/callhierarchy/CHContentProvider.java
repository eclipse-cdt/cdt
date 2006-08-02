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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.missingapi.CIndexQueries;
import org.eclipse.cdt.internal.ui.missingapi.CIndexQueries.IPDOMReference;
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

	public Object[] syncronouslyComputeChildren(Object parentElement) {
		if (parentElement instanceof ICElement) {
			ICElement element = (ICElement) parentElement;
			return new Object[] { new CHNode(null, null, element, 0) };
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

	public Object[] asyncronouslyComputeChildren(Object parentElement,
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
			if (elem instanceof ISourceReference) {
				ISourceReference sf= (ISourceReference) elem;
				ISourceRange range = sf.getSourceRange();
				ITranslationUnit tu = sf.getTranslationUnit();
				if (tu != null) {
					ILanguage language = tu.getLanguage();
					IASTTranslationUnit ast = language.getASTTranslationUnit(tu, ILanguage.AST_SKIP_ALL_HEADERS | ILanguage.AST_USE_INDEX);
					IASTName[] names = language.getSelectedNames(ast, range.getIdStartPos(), range.getIdLength());
					if (names.length > 0) {
						IASTName name= names[names.length-1];
						IPDOMReference[] refs= CIndexQueries.getInstance().findReferences(tu, name, NPM);
						HashMap refsPerElement= new HashMap();
						for (int i = 0; i < refs.length; i++) {
							IPDOMReference reference = refs[i];
							ICElement caller= findCaller(reference);
							if (caller != null) {
								addToMap(caller, reference, refsPerElement);
							}
						}
						ArrayList result= new ArrayList();
						for (Iterator iter= refsPerElement.entrySet().iterator(); iter.hasNext(); ) {
							Map.Entry entry= (Map.Entry) iter.next();
							ICElement element= (ICElement) entry.getKey();
							List references= (List) entry.getValue();
							if (!references.isEmpty()) {
								IPDOMReference firstRef= (IPDOMReference) references.get(0);
								CHNode node= new CHNode(parent, new CHReferenceInfo(firstRef.getOffset()),
										element, firstRef.getTimestamp());
								Iterator iterator = references.iterator();
								for (iterator.next(); iterator.hasNext(); ) {
									IPDOMReference nextRef = (IPDOMReference) iterator.next();
									node.addReference(new CHReferenceInfo(nextRef.getOffset()));
								}
								result.add(node);
							}
						}
						return result.toArray();
					}
				}
			}
		} catch (CModelException e) {
			CUIPlugin.getDefault().log(e);
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		} catch (InterruptedException e) {
		}
		return NO_CHILDREN;
	}

	private ICElement findCaller(IPDOMReference reference) throws CoreException {
		ITranslationUnit tu= reference.getTranslationUnit();
		return findCaller(tu, reference.getOffset());
	}

	private ICElement findCaller(ICElement element, int offset) throws CModelException {
		// mstodo use position tracker
		if (element == null || (element instanceof IFunctionDeclaration)) {
			return element;
		}
		if (element instanceof IParent) {
			ICElement[] children= ((IParent) element).getChildren();
			for (int i = 0; i < children.length; i++) {
				ICElement child = children[i];
				if (child instanceof ISourceReference) {
					ISourceRange sr= ((ISourceReference) child).getSourceRange();
					int startPos= sr.getStartPos();
					if (startPos <= offset && offset < startPos + sr.getLength()) {
						return findCaller(child, offset);
					}
				}
			}
		}
		return null;
	}

	private void addToMap(ICElement caller, IPDOMReference reference, Map map) {
		List list= (List) map.get(caller);
		if (list == null) {
			list= new ArrayList();
			map.put(caller, list);
		}
		list.add(reference);
	}

	private Object[] asyncronouslyComputeRefersTo(CHNode parent, ICElement elem) {
		// mstodo Auto-generated method stub
		return NO_CHILDREN;
	}

	public void setComputeReferencedBy(boolean value) {
		fComputeReferencedBy = value;
	}

	public boolean getComputeReferencedBy() {
		return fComputeReferencedBy;
	}
}
