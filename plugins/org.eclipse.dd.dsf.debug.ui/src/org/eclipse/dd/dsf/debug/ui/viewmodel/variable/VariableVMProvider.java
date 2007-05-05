/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.variable;

import org.eclipse.dd.dsf.debug.ui.viewmodel.DebugViewSelectionRootLayoutNode;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * 
 */
@SuppressWarnings("restriction")
public class VariableVMProvider extends AbstractDMVMProvider {
    public VariableVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);
        IVMRootLayoutNode debugViewSelectionNode = new DebugViewSelectionRootLayoutNode(this); 
        IVMLayoutNode localsNode = new LocalsLayoutNode(this, getSession());
        debugViewSelectionNode.setChildNodes(new IVMLayoutNode[] { localsNode });
        setRootLayoutNode(debugViewSelectionNode);
    }

    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return new VariableColumnPresentation();
    }
    
    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return VariableColumnPresentation.ID;
    }
}
