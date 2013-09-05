/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterGroupVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterRootDMVMNode;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.register.SyncRegisterDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * A specialization of {@link RegisterVMProvider} that uses a GDB-specific variable VM node.
 */
public class GdbRegisterVMProvider extends RegisterVMProvider {

    public GdbRegisterVMProvider( AbstractVMAdapter adapter, IPresentationContext context, DsfSession session ) {
        super( adapter, context, session );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.register.RegisterVMProvider#configureLayout()
     */
    @Override
    protected void configureLayout() {
        DsfSession session = getSession();
        
        /*
         * Create the register data access routines.
         */
        SyncRegisterDataAccess regAccess = new SyncRegisterDataAccess( session );
        SyncVariableDataAccess varAccess = new SyncVariableDataAccess( session );
        
        /*
         * Create the top level node to deal with the root selection.
         */
        IRootVMNode rootNode = new RegisterRootDMVMNode( this );
        
        /*
         * Create the group and register nodes next. Groups and group-less registers represent 
         * the first level shown in the view.
         */
        IVMNode registerGroupNode = new RegisterGroupVMNode( this, getSession(), regAccess );
        IVMNode registerNode = new GdbRegisterVMNode( this, getSession(), varAccess );
        addChildNodes( rootNode, new IVMNode[] { registerGroupNode, registerNode } );

        /*
         * Create the level for the registers that are members of a group.
         */
        addChildNodes( registerGroupNode, new IVMNode[] { registerNode } );

        /*
         * Create the next level which represents members of structs/unions/enums 
         * and elements of arrays.
         */
        VariableVMNode subExpressioNode = new GdbVariableVMNode( this, getSession(), varAccess );
        addChildNodes( registerNode, new IVMNode[] { subExpressioNode } );
        
        /*
         * Configure the sub-expression node to be a child of itself.  This way the content
         * provider will recursively drill-down the variable hierarchy.
         */
        addChildNodes( subExpressioNode, new IVMNode[] { subExpressioNode } );

        /*
         * Now set this schema set as the layout set.
         */
        setRootNode( rootNode );
    }
}
