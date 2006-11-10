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
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMProvider;
import org.eclipse.debug.internal.ui.viewers.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;

/**
 * 
 */
@SuppressWarnings("restriction")
public class RegisterVMProvider extends VMProvider {
    public RegisterVMProvider(DsfSession session, IPresentationContext context) {
        super(session, null);
        IVMRootLayoutNode debugViewSelection = new DebugViewSelectionRootLayoutNode(
            getSession().getExecutor(), context.getPart().getSite().getWorkbenchWindow() ); 
        IVMLayoutNode registerGroupNode = new RegisterGroupLayoutNode(getSession());
        debugViewSelection.setChildNodes(new IVMLayoutNode[] { registerGroupNode });
        IVMLayoutNode registerNode = new RegisterLayoutNode(getSession());
        registerGroupNode.setChildNodes(new IVMLayoutNode[] { registerNode });
        setRootLayoutNode(debugViewSelection);
    }
    
    @Override
    public IColumnPresentation createColumnPresentation(Object element) {
        return new RegisterColumnPresentation();
    }
    
    @Override
    public String getColumnPresentationId(Object element) {
        return RegisterColumnPresentation.ID;
    }
}
