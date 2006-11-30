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
package org.eclipse.dd.dsf.debug.ui.viewmodel.register;

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
public class RegisterVMProvider extends AbstractDMVMProvider {
    public RegisterVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);
        IVMRootLayoutNode debugViewSelection = new DebugViewSelectionRootLayoutNode(this); 
        IVMLayoutNode registerGroupNode = new RegisterGroupLayoutNode(this, getSession());
        debugViewSelection.setChildNodes(new IVMLayoutNode[] { registerGroupNode });
        IVMLayoutNode registerNode = new RegisterLayoutNode(this, getSession());
        registerGroupNode.setChildNodes(new IVMLayoutNode[] { registerNode });
        setRootLayoutNode(debugViewSelection);
    }

    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return new RegisterColumnPresentation();
    }
    
    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return RegisterColumnPresentation.ID;
    }
}
