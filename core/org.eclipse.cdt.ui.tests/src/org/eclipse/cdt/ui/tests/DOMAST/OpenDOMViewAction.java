/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.tests.DOMAST.DOMAST;
import org.eclipse.core.resources.IFile;
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

	public static final String VIEW_ID = "org.eclipse.cdt.ui.tests.DOMAST.DOMAST"; //$NON-NLS-1$
	IViewPart viewPart = null;
	ISelection selection = null;
	IFile file = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		this.viewPart = view;
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		// TODO Auto-generated method stub
	
		Object obj = ((IStructuredSelection)selection).getFirstElement();
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
						} catch (PartInitException pie) {}
					
					}
				}
			}
		}
		
		IViewPart tempView = null;

		try {
			tempView = viewPart.getSite().getPage().showView(VIEW_ID);
		} catch (PartInitException pie) {}
		
		if (tempView != null) {
			if (tempView instanceof DOMAST) {
				((DOMAST)tempView).setFile(file);
				((DOMAST)tempView).setPart(part);
				if (tu != null) {
					((DOMAST)tempView).setLang(tu.isCXXLanguage() ? ParserLanguage.CPP : ParserLanguage.C);
				}
				((DOMAST)tempView).setContentProvider(((DOMAST)tempView).new ViewContentProvider(file));
			}
		}

		viewPart.getSite().getPage().activate(tempView);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection &&
				((IStructuredSelection)selection).getFirstElement() instanceof TranslationUnit &&
				((TranslationUnit)((IStructuredSelection)selection).getFirstElement()).getResource() instanceof IFile) {
			this.file = (IFile)((TranslationUnit)((IStructuredSelection)selection).getFirstElement()).getResource();
		}
		this.selection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
