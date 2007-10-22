/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.modules;

import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.dm.DMVMRootLayoutNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * 
 */
@SuppressWarnings("restriction")
public class ModulesVMProvider extends AbstractDMVMProvider {
    /*
     *  Current default for register formatting.
     */
    public ModulesVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);
        
        /*
         *  Create the top level node to deal with the root selection.
         */
        IVMRootLayoutNode debugViewSelection = new DMVMRootLayoutNode(this);
        
        /*
         *  Create the Group nodes next. They represent the first level shown in the view.
         */
        IVMLayoutNode modulesNode = new ModulesLayoutNode(this, getSession());
        debugViewSelection.setChildNodes(new IVMLayoutNode[] { modulesNode });
        
        /*
         *  Now set this schema set as the layout set.
         */
        setRootLayoutNode(debugViewSelection);
    }
}
