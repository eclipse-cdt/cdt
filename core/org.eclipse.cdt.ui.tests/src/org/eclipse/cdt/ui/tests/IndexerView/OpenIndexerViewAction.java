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
package org.eclipse.cdt.ui.tests.IndexerView;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.core.resources.IProject;
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
public class OpenIndexerViewAction implements IViewActionDelegate,
        IEditorActionDelegate, IObjectActionDelegate {

    IViewPart viewPart = null;
    IProject proj = null;
    public static int numViewsOpen=0;
    
    public void init(IViewPart view) {
        this.viewPart = view;
    }

    public void run(IAction action) {
        IViewPart tempView = null;

        try {
            tempView = viewPart.getSite().getPage().showView(IndexerView.VIEW_ID);
            if (tempView instanceof IndexerView)
                ((IndexerView)tempView).setProject(proj);
            
            OpenIndexerViewAction.numViewsOpen++;
        } catch (PartInitException pie) {}
        
        if (tempView != null) {
            if (tempView instanceof IndexerView) {
                ((IndexerView)tempView).clearIndexers();
                ICDTIndexer indexer = CCorePlugin.getDefault().getCoreModel().getIndexManager().getIndexerForProject(proj); 
                ((IndexerView)tempView).appendIndexer(indexer);
                ((IndexerView)tempView).setContentProvider(((IndexerView)tempView).new ViewContentProvider());
            }
        }

        viewPart.getSite().getPage().activate(tempView);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection &&
            ((IStructuredSelection)selection).getFirstElement() instanceof CProject) {
            proj = ((CProject)((IStructuredSelection)selection).getFirstElement()).getProject();
        }
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        // TODO Auto-generated method stub
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // TODO Auto-generated method stub
    }

}
