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

package org.eclipse.cdt.internal.ui.includebrowser;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkingSet;

import org.eclipse.cdt.internal.ui.viewsupport.WorkingSetFilterUI;

public class IBWorkingSetFilter extends ViewerFilter {

    private WorkingSetFilterUI fWorkingSetFilter;
    
    public IBWorkingSetFilter(WorkingSetFilterUI wsFilter) {
        fWorkingSetFilter= wsFilter;
    }
    
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (parentElement instanceof IBNode && element instanceof IBNode) {
            IBNode node= (IBNode) element;
            if (!fWorkingSetFilter.isPartOfWorkingSet(node.getRepresentedTranslationUnit())) {
                return false;
            }
        }
        return true;
    }

    public WorkingSetFilterUI getUI() {
        return fWorkingSetFilter;
    }

    public String getLabel() {
        IWorkingSet ws= fWorkingSetFilter.getWorkingSet();
        if (ws != null) {
            return ws.getLabel();
        }
        return IBMessages.IBViewPart_workspaceScope;
    }
}
