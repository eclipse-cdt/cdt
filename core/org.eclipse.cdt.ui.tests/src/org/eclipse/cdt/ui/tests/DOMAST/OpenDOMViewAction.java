/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * @author dsteffle
 */
public class OpenDOMViewAction implements IViewActionDelegate, IEditorActionDelegate, IObjectActionDelegate {

	IViewPart viewPart = null;
	ISelection selection = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		this.viewPart = view;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		// TODO Auto-generated method stub

		Object obj = ((IStructuredSelection) selection).getFirstElement();
		ITranslationUnit tu = null;
		IEditorPart part = null;
		if (obj instanceof ISourceReference) {
			tu = ((ISourceReference) obj).getTranslationUnit();
			if (tu != null) {
				if (viewPart != null) {
					if (obj instanceof ICElement) {
						try {
							part = EditorUtility.openInEditor(obj);
						} catch (CModelException cme) {
						} catch (PartInitException pie) {
						}

					}
				}
			}
		}

		IViewPart tempView = null;

		try {
			tempView = viewPart.getSite().getPage().showView(DOMAST.VIEW_ID);
		} catch (PartInitException pie) {
		}

		if (tempView != null) {
			if (tempView instanceof DOMAST) {
				((DOMAST) tempView).setTranslationUnit(tu);
				((DOMAST) tempView).setPart(part);
				((DOMAST) tempView).setContentProvider(((DOMAST) tempView).new ViewContentProvider(tu));
			}
		}

		viewPart.getSite().getPage().activate(tempView);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
