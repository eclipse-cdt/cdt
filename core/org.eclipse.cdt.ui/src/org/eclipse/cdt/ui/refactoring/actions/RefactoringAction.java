/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.refactoring.utils.EclipseObjects;

/**
 * Common base class for refactoring actions
 * @since 5.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class RefactoringAction extends Action {
    protected ITextEditor fEditor;
    private IWorkbenchSite fSite;
	private ICElement fElement;

	public RefactoringAction(String label) {
		super(label);
	}

    public void setEditor(IEditorPart editor) {
        fEditor= null;
        fSite= null;
        if (editor instanceof ITextEditor) {
            fEditor= (ITextEditor) editor;
        }
        setEnabled(fEditor != null);
    }

	public void setSite(IWorkbenchSite site) {
        fEditor= null;
        fSite= site;
	}
	
    @Override
	public final void run() {
    	EclipseObjects.getActivePage().saveAllEditors(true);
    	if (EclipseObjects.getActivePage().getDirtyEditors().length != 0) {
    		return;
    	}
    	if (fEditor != null) {
            ISelectionProvider provider= fEditor.getSelectionProvider();
            if (provider != null) {
                ISelection s= provider.getSelection();
                if (s instanceof ITextSelection) {
            		IWorkingCopy wc= CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
            		if (wc != null)
            			run(fEditor.getSite(), wc, (ITextSelection) s);
                }
            }
        } else if (fSite != null) {
            if (fElement != null) {
            	run(fSite, fElement);
            }                        
        }            
    }

	public void updateSelection(ICElement elem) {
		fElement= elem;
		setEnabled(elem != null);
	}

    public abstract void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s);
    public abstract void run(IShellProvider shellProvider, ICElement elem);
}
