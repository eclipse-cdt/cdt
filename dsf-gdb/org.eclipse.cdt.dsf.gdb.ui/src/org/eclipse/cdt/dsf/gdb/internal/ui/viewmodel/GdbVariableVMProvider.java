/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *     Axel Mueller            - Bug 306555 - Add support for cast to type / view as array (IExpressions2)     
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.DsfCastToTypeSupport;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMProvider;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * A specialization of VariableVMProvider that uses a GDB-specific variable VM
 * node. To understand why this is necessary, see GdbVariableVMNode.
 */
@SuppressWarnings("restriction")
public class GdbVariableVMProvider extends VariableVMProvider {

	/**
	 * Constructor (passthru)
	 */
	public GdbVariableVMProvider(AbstractVMAdapter adapter,
			IPresentationContext context, DsfSession session) {
		super(adapter, context, session);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMProvider#configureLayout(org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess)
     */
    @Override
	protected void configureLayout() {
        // Create the variable data access routines.
        SyncVariableDataAccess varAccess = new SyncVariableDataAccess(getSession()) ;
    	
        // Create the top level node to deal with the root selection.
        IRootVMNode rootNode = new RootDMVMNode(this);
        setRootNode(rootNode);
        
        // Create the next level which represents members of structs/unions/enums and elements of arrays.
        IVMNode subExpressioNode = new GdbVariableVMNode(this, getSession(), varAccess);
        addChildNodes(rootNode, new IVMNode[] { subExpressioNode });

		/* Wire up the casting support. IExpressions2 service is always available
		 * for gdb. No need to call hookUpCastingSupport */
		((VariableVMNode) subExpressioNode).setCastToTypeSupport(
				new DsfCastToTypeSupport(getSession(), GdbVariableVMProvider.this, varAccess));
        
        // Configure the sub-expression node to be a child of itself.  This way the content
        // provider will recursively drill-down the variable hierarchy.
        addChildNodes(subExpressioNode, new IVMNode[] { subExpressioNode });
    }
}
